package com.sencorsta.ids.core.processor;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.constant.ErrorCodeConstant;
import com.sencorsta.ids.core.constant.ProtocolTypeConstant;
import com.sencorsta.ids.core.entity.ErrorCode;
import com.sencorsta.ids.core.entity.IdsRequest;
import com.sencorsta.ids.core.entity.IdsResponse;
import com.sencorsta.ids.core.entity.MethodProxy;
import com.sencorsta.ids.core.net.protocol.MessageFactor;
import com.sencorsta.ids.core.net.protocol.RpcMessage;
import com.sencorsta.ids.core.net.protocol.RpcMessageLock;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 消息分发器
 *
 * @author daibin
 */
@Slf4j
@AllArgsConstructor
public class MessageDispatcher implements Runnable {
    private final RpcMessage message;

    @Override
    public void run() {
        log.info("MessageDispatcher 开始处理消息{}", message.toStringPlus());
        if (GlobalConfig.IS_DEBUG) {
            long sTime = System.currentTimeMillis();
            String method = message.getMethod();
            ScheduledFuture<?> schedule = MessageProcessor.MONITOR.schedule(() -> {
                log.warn("消息处理超时:{}", method);
            }, 10, TimeUnit.SECONDS);
            execute(message);
            schedule.cancel(true);
            if (System.currentTimeMillis() - sTime > 100) {
                log.warn("消息处理时间过高{} -> {}", method, System.currentTimeMillis() - sTime);
            }
        } else {
            execute(message);
        }
    }

    private void execute(RpcMessage message) {
        switch (message.getHeader().getType()) {
            // 服务器之间的请求 理论上必须能找到处理器 找不到就返回错误
            case ProtocolTypeConstant.TYPE_RPC_REQ:
                handleReq(message);
                break;
            // 服务器之间的响应 一般是等待同步的消息 拿到响应后应该能触发解锁操作
            case ProtocolTypeConstant.TYPE_RPC_RES:
                handleRes(message);
                break;
            // 推送一般直接触发对应的逻辑就行了
            case ProtocolTypeConstant.TYPE_RPC_PUSH:
                handlePush(message);
                break;
            default:
                log.warn("未知协议类型：{}", message.getHeader().type);
                break;
        }
    }

    private void handleRes(RpcMessage message) {
        Long msgId = message.getMsgId();
        if (msgId > 0) {
            RpcMessageLock lock = MessageProcessor.LOCKS.get(msgId);
            if (lock != null) {
                lock.getLock().lock();
                try {
                    lock.setMessage(message);
                    lock.getCondition().signal();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                } finally {
                    lock.getLock().unlock();
                }
            } else {
                log.warn("request:{} timeout!!!", message.getMethod());
            }
        }
    }

    private void handleReq(RpcMessage message) {
        MethodProxy methodProxy = MessageProcessor.getMETHOD_MAP().get(message.getMethod());
        RpcMessage res = MessageFactor.newResMessage();
        res.setMsgId(message.getMsgId());
        res.setMethod(message.getMethod());
        if (ObjectUtil.isNotNull(methodProxy)) {
            try {
                Object result = invoke(message, methodProxy);
                byte[] bytes = jsonMapper.writeValueAsBytes(result);
                res.setData(bytes);
            } catch (Exception e) {
                res.setErrCode(ErrorCodeConstant.SYSTEM_ERROR.getCode());
                log.error(e.getMessage(), e);
            }
        } else {
            res.setErrCode(ErrorCodeConstant.NOT_FIND.getCode());
        }
        log.info(res.toStringPlus());
        message.getChannel().writeAndFlush(res);
    }

    private void handlePush(RpcMessage message) {
        MethodProxy methodProxy = MessageProcessor.getMETHOD_MAP().get(message.getMethod());
        if (ObjectUtil.isNotNull(methodProxy)) {
            try {
                invoke(message, methodProxy);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    private Object invoke(RpcMessage message, MethodProxy methodProxy) throws Exception {
        byte[] data = message.getData();
        final Method method = methodProxy.getMethod();
        Class<Object> valueType = methodProxy.getValueType();
        Object object = jsonMapper.readValue(data, valueType);
        if (object == null) {
            object = data;
        }
        IdsRequest<?> idsRequest = new IdsRequest<>(object);
        idsRequest.setChannel(message.getChannel());
        try {
            return method.invoke(methodProxy.getObj(), idsRequest);
        } catch (Exception exception) {
            if (exception instanceof InvocationTargetException) {
                return new IdsResponse<>(null, (ErrorCode) ((InvocationTargetException) exception).getTargetException());
            } else {
                throw exception;
            }
        }
    }
}
