package com.sencorsta.ids.core.processor;

import com.sencorsta.ids.core.config.ConfigGroup;
import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.entity.MethodProxy;
import com.sencorsta.ids.core.net.protocol.RpcMessage;
import com.sencorsta.ids.core.net.protocol.RpcMessageLock;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 消息处理器
 *
 * @author ICe
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
    private static final Map<String, MethodProxy> METHOD_MAP = new ConcurrentHashMap<>();
    /**
     * service map
     */
    @Getter
    private static final Map<String, String> SERVICE_MAP = new ConcurrentHashMap<>();

    public static void addMethod(String key, MethodProxy method) {
        METHOD_MAP.putIfAbsent(key, method);
    }

    public static void addService(String key, String service) {
        SERVICE_MAP.putIfAbsent(key, service);
    }

    /**
     * 响应信号通知键生成器
     */
    public static final AtomicLong UUID = new AtomicLong(System.currentTimeMillis());
    /**
     * 同步调用锁
     */
    public static Map<Long, RpcMessageLock> LOCKS = new ConcurrentHashMap<>();

    /**
     * 无论是什么消息全部塞到队列里 释放netty的io线程
     */
    public static void incomeMessage(RpcMessage msg) {
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

    public static RpcMessage request(RpcMessage req) {
        if (req.getChannel() == null) {
            return null;
        }
        long reqId = UUID.incrementAndGet();
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            log.info("执行同步请求:{}", req.toStringPlus());
            req.setMsgId(reqId);
            Condition condition = lock.newCondition();
            RpcMessageLock look = new RpcMessageLock(lock, condition);
            LOCKS.put(reqId, look);
            req.getChannel().writeAndFlush(req);
            Integer time = GlobalConfig.instance().getInt("request.await", ConfigGroup.performance.getName(), 15000);
            if (!condition.await(time, TimeUnit.MILLISECONDS)) {
                log.trace("msg:{} condition timeout!", req.getMethod());
                return null;
            }
            if (look.getMessage() == null) {
                log.trace("msg:{} getMessage == null!", req.getMethod());
                return null;
            }
            return look.getMessage();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            lock.unlock();
            LOCKS.remove(reqId);
        }
        return null;
    }

    public static void push(RpcMessage push) {
        try {
            log.info("执行异步推送:{}", push.toStringPlus());
            push.getChannel().writeAndFlush(push);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


}
