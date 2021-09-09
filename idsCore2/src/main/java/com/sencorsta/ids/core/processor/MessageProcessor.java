package com.sencorsta.ids.core.processor;

import com.sencorsta.ids.core.config.ConfigGroup;
import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.entity.MethodProxy;
import com.sencorsta.ids.core.net.protocol.RpcMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 消息处理器
 *
 * @author daibin
 */
@Slf4j
public class MessageProcessor {

    private final static LinkedBlockingQueue<RpcMessage> INCOMING_MESSAGE_QUEUE = new LinkedBlockingQueue<>(Integer.MAX_VALUE);

    private static final int CAPACITY = GlobalConfig.instance().getInt("executor.capacity", ConfigGroup.performance.getName(), 10000);
    private static final SynchronousQueue<Runnable> EXECUTOR_QUEUE = new SynchronousQueue<>();
    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(0, CAPACITY, 60L, TimeUnit.SECONDS, EXECUTOR_QUEUE, new IdsThreadFactory("IDS-EXE"));
    private static final int WARN_COUNT = GlobalConfig.instance().getInt("executor.warn", ConfigGroup.performance.getName(), 10000);

    /**
     * 监控线程
     */
    public static final ScheduledExecutorService MONITOR = new ScheduledThreadPoolExecutor(1, new IdsThreadFactory("IDS-MONITOR"));

    /**
     * 方法map
     */
    @Getter
    private static final Map<String, MethodProxy> methodMap = new ConcurrentHashMap<>();
    /**
     * service map
     */
    @Getter
    private static final Map<String, String> serviceMap = new ConcurrentHashMap<>();

    public static void addMethod(String key, MethodProxy method) {
        methodMap.putIfAbsent(key, method);
    }

    public static void addService(String key, String service) {
        serviceMap.putIfAbsent(key, service);
    }

    /**
     * 无论是什么消息全部塞到队列里 释放netty的io线程
     */
    public static void addMessage(RpcMessage msg) {
        try {
            EXECUTOR.execute(new MessageDispatcher(msg));
        } catch (RejectedExecutionException e) {
            log.debug("接收队列已满,消息进入预备队列" + INCOMING_MESSAGE_QUEUE.size());
            if (INCOMING_MESSAGE_QUEUE.size() > WARN_COUNT) {
                log.warn("预备队列偏大 -> " + INCOMING_MESSAGE_QUEUE.size());
            }
            INCOMING_MESSAGE_QUEUE.offer(msg);
        }
    }


}
