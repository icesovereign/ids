package com.sencorsta.ids.core.processor;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import com.sencorsta.ids.core.application.Application;
import com.sencorsta.ids.core.application.proxy.ProxyClient;
import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.constant.ErrorCodeConstant;
import com.sencorsta.ids.core.constant.ProtocolTypeConstant;
import com.sencorsta.ids.core.constant.SerializeTypeConstant;
import com.sencorsta.ids.core.entity.*;
import com.sencorsta.ids.core.net.protocol.MessageJsonFactory;
import com.sencorsta.ids.core.net.protocol.RpcMessage;
import com.sencorsta.ids.core.net.protocol.RpcMessageLock;
import com.sencorsta.utils.object.Jsons;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 消息分发器
 *
 * @author ICe
 */
@Slf4j
@AllArgsConstructor
public class MessageDispatcher implements Runnable {
    private final RpcMessage message;

    @Override
    public void run() {
        log.info("开始处理消息: {}", message.toStringPlus());
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
            case ProtocolTypeConstant.TYPE_PROXY_REQ:
                handleRpcReq(message);
                break;
            // 服务器之间的响应 一般是等待同步的消息 拿到响应后应该能触发解锁操作
            case ProtocolTypeConstant.TYPE_RPC_RES:
                handleRpcRes(message);
                break;
            // 推送一般直接触发对应的逻辑就行了
            case ProtocolTypeConstant.TYPE_RPC_PUSH:
                handleRpcPush(message);
                break;
            // 客户端来的请求
            case ProtocolTypeConstant.TYPE_REQ:
                handleReq(message);
                break;
            // 返回给客户端的响应 直接推送给客户端
            case ProtocolTypeConstant.TYPE_RES:
            case ProtocolTypeConstant.TYPE_PUSH:
            case ProtocolTypeConstant.TYPE_PROXY_RES:
                handleRes(message);
                break;
            default:
                log.warn("未知协议类型：{}", message.getHeader().type);
                break;
        }
    }

    private void handleRes(RpcMessage message) {
        String userId = message.getUserId();
        if (ObjectUtil.isNotEmpty(userId)) {
            Client client = GlobalContainer.CLIENTS.get(userId);
            if (ObjectUtil.isNotEmpty(client)) {
                if (message.getHeader().getType() == ProtocolTypeConstant.TYPE_PROXY_RES) {
                    message.getHeader().setType(ProtocolTypeConstant.TYPE_RES);
                }
                client.sendMsg(message);
            } else {
                log.trace("client 不存在! userId:{}", userId);
            }
        } else {
            log.warn("用户id不存在!");
        }
    }

    private void handleReq(RpcMessage message) {
        //TODO ice 白名单机制

        //判断是否登录过了
        Channel channel = message.getChannel();
        Client client = channel.attr(GlobalConfig.CLIENT_KEY).get();
        if (client == null) {
            //用户没有登陆 生成临时用户id
            String userId = "TEMP_" + UUID.fastUUID();
            message.setUserId(userId);
            Client newClient = new Client();
            newClient.setUserId(userId);
            newClient.setChannel(channel);
            GlobalContainer.CLIENTS.put(userId, newClient);
            channel.attr(GlobalConfig.CLIENT_KEY).set(newClient);
        } else {
            message.setUserId(client.getUserId());
        }
        //先尝试本地是否有此方法
        MethodProxy methodProxy = MessageProcessor.getMETHOD_MAP().get(message.getMethod());
        if (methodProxy != null) {
            handleRpcReq(message);
        } else {
            String type = message.getType();
            if (!Application.instance().SERVER_TYPE.equals(type)) {
                message.getHeader().setType(ProtocolTypeConstant.TYPE_PROXY_REQ);
                ProxyClient.sendByTypeAsync(message, type);
            }
        }
    }

    private void handleRpcRes(RpcMessage message) {
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

    private void handleRpcReq(RpcMessage message) {
        MethodProxy methodProxy = MessageProcessor.getMETHOD_MAP().get(message.getMethod());
        RpcMessage res = MessageJsonFactory.newResMessage(message.getHeader().getType());
        res.setMsgId(message.getMsgId());
        res.setMethod(message.getMethod());
        res.setUserId(message.getUserId());
        if (ObjectUtil.isNotNull(methodProxy)) {
            try {
                Object result = invoke(message, methodProxy);
                byte[] bytes = Jsons.mapper.writeValueAsBytes(result);
                res.setData(bytes);
            } catch (Exception e) {
                res.setErrCode(ErrorCodeConstant.SYSTEM_ERROR.getCode());
                log.error(e.getMessage(), e);
            }
        } else {
            log.debug("没有找到对应的处理方法:{}", message.getMethod());
            res.setErrCode(ErrorCodeConstant.NOT_FIND.getCode());
        }
        log.info("返回消息回调: " + res.toStringPlus());
        message.getChannel().writeAndFlush(res);
    }

    private void handleRpcPush(RpcMessage message) {
        MethodProxy methodProxy = MessageProcessor.getMETHOD_MAP().get(message.getMethod());
        if (ObjectUtil.isNotNull(methodProxy)) {
            try {
                invoke(message, methodProxy);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private Object invoke(RpcMessage message, MethodProxy methodProxy) throws Exception {
        byte[] data = message.getData();
        final Method method = methodProxy.getMethod();
        Class<?> valueType = methodProxy.getValueType();
        Object object;
        if (message.getSerializeType() == SerializeTypeConstant.TYPE_JSON) {
            object = Jsons.toBean(data, valueType);
        } else if (message.getSerializeType() == SerializeTypeConstant.TYPE_STRING) {
            object = new String(data);
        } else {
            object = data;
        }
        if (object == null) {
            object = data;
        }
        IdsRequest<?> idsRequest = new IdsRequest<>(object);
        idsRequest.setChannel(message.getChannel());
        idsRequest.setUserId(message.getUserId());
        try {
            return method.invoke(methodProxy.getObj(), idsRequest);
        } catch (Exception exception) {
            if (exception instanceof InvocationTargetException) {
                Throwable targetException = ((InvocationTargetException) exception).getTargetException();
                if (targetException instanceof ErrorCode) {
                    return new IdsResponse<>(null, (ErrorCode) targetException);
                } else {
                    throw exception;
                }
            } else {
                throw exception;
            }
        }
    }
}
