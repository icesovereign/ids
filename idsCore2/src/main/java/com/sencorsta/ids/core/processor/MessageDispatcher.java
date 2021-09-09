package com.sencorsta.ids.core.processor;

import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.constant.ProtocolTypeConstant;
import com.sencorsta.ids.core.entity.IdsRequest;
import com.sencorsta.ids.core.entity.IdsResponse;
import com.sencorsta.ids.core.entity.MethodProxy;
import com.sencorsta.ids.core.net.protocol.RpcMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
        if (GlobalConfig.isDebug) {
            long sTime = System.currentTimeMillis();
            String method = message.method;
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
        switch (message.header.type) {
            // 服务器之间的请求 理论上必须能找到处理器 找不到就返回错误
            case ProtocolTypeConstant.TYPE_RPC_REQ:
                String methodName = message.getMethod();
                MethodProxy methodProxy = MessageProcessor.getMethodMap().get(methodName);
                if (ObjectUtil.isNotNull(methodProxy)) {
                    try {
                        IdsRequest<byte[]> objectIdsRequest = new IdsRequest<>();
                        objectIdsRequest.setData(message.getData());
                        Class<Object> clazz = ClassUtil.loadClass(methodProxy.getClassName());
                        Object[] objects = getFields(clazz);
                        final Method method = ClassUtil.getDeclaredMethod(clazz, methodProxy.getMethodName(), ClassUtil.getClasses(objectIdsRequest));
                        Object obj = Singleton.get(clazz, objects);
                        IdsResponse<String> result = ReflectUtil.invoke(obj, method, objectIdsRequest);
                        RpcMessage res = new RpcMessage(ProtocolTypeConstant.TYPE_RES);
                        res.setData(result.getData().getBytes());
                        res.setErrMsg(result.getMessage());
                        res.setChannel(message.getChannel());

                        message.getChannel().writeAndFlush(res);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
                break;
            // 服务器之间的响应 一般是等待同步的消息 拿到响应后应该能触发解锁操作
            case ProtocolTypeConstant.TYPE_RPC_RES:
                break;
            // 推送一般直接触发对应的逻辑就行了
            case ProtocolTypeConstant.TYPE_RPC_PUSH:
                break;
            default:
                log.warn("未知协议类型：{}", message.header.type);
                break;
        }
    }

    private Object[] getFields(Class<Object> clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();
        return Arrays.stream(declaredFields).map(e -> {
            String s = MessageProcessor.getServiceMap().get(e.getType().getName());
            if (ObjectUtil.isNotNull(s)) {
                Class<Object> objectClass = ClassUtil.loadClass(s);
                Object[] fields = getFields(objectClass);
                return Singleton.get(objectClass, fields);
            }
            return null;
        }).filter(Objects::nonNull).toArray();
    }
}
