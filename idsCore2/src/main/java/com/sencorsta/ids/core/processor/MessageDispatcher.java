package com.sencorsta.ids.core.processor;

import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.constant.ErrorCodeConstant;
import com.sencorsta.ids.core.constant.ProtocolTypeConstant;
import com.sencorsta.ids.core.entity.IdsRequest;
import com.sencorsta.ids.core.entity.IdsResponse;
import com.sencorsta.ids.core.entity.MethodProxy;
import com.sencorsta.ids.core.net.protocol.MessageFactor;
import com.sencorsta.ids.core.net.protocol.RpcMessage;
import com.sencorsta.ids.core.net.protocol.RpcMessageLock;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
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
                IdsResponse<Object> result = invoke(message, methodProxy);
                byte[] bytes = JSONUtil.toJsonStr(result).getBytes(StandardCharsets.UTF_8);
                //fixme 这里如果用jackson会报错 byte[] bytes = new ObjectMapper().writeValueAsBytes(result);
                res.setData(bytes);
            } catch (Exception e) {
                res.setErrCode(ErrorCodeConstant.SYSTEM_ERROR.getCode());
                log.error(e.getMessage(), e);
            }
        } else {
            res.setErrCode(ErrorCodeConstant.NOT_FIND.getCode());
        }
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

    private IdsResponse<Object> invoke(RpcMessage message, MethodProxy methodProxy) throws IOException {
        byte[] data = message.getData();

        Class<Object> clazz = ClassUtil.loadClass(methodProxy.getClassName());
        final Method method = ClassUtil.getDeclaredMethod(clazz, methodProxy.getMethodName(), IdsRequest.class);

        Object object = null;
        Type genericReturnType = Arrays.stream(method.getGenericParameterTypes()).filter(o -> o instanceof ParameterizedType).findFirst().orElse(null);
        if (ObjectUtil.isNotNull(genericReturnType)) {
            Type type = Arrays.stream(((ParameterizedType) genericReturnType).getActualTypeArguments()).findFirst().orElse(null);
            if (ObjectUtil.isNotNull(type)) {
                object = new ObjectMapper().readValue(data, ClassUtil.loadClass(type.getTypeName()));
            }
        }
        if (object == null) {
            object = data;
        }
        Object[] objects = getFields(clazz);
        Object obj = Singleton.get(clazz, objects);
        IdsRequest<?> idsRequest = new IdsRequest<>(object);
        idsRequest.setChannel(message.getChannel());
        return ReflectUtil.invoke(obj, method, idsRequest);
    }

    protected final <T> IdsResponse<T> getResponses(byte[] data) {
        IdsResponse<T> res = null;
        try {
            TypeReference<IdsResponse<T>> type = new TypeReference<IdsResponse<T>>() {
            };
            res = new ObjectMapper().readValue(data, type);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return res;
    }

    private Object[] getFields(Class<Object> clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();
        return Arrays.stream(declaredFields).map(e -> {
            String s = MessageProcessor.getSERVICE_MAP().get(e.getType().getName());
            if (ObjectUtil.isNotNull(s)) {
                Class<Object> objectClass = ClassUtil.loadClass(s);
                Object[] fields = getFields(objectClass);
                return Singleton.get(objectClass, fields);
            }
            return null;
        }).filter(Objects::nonNull).toArray();
    }
}
