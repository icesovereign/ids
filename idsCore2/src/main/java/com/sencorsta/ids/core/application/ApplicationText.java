package com.sencorsta.ids.core.application;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.io.unit.DataSizeUtil;
import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.system.SystemUtil;
import com.google.common.collect.Lists;
import com.sencorsta.ids.core.config.ConfigGroup;
import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.entity.MethodProxy;
import com.sencorsta.ids.core.entity.annotation.Component;
import com.sencorsta.ids.core.entity.annotation.Controller;
import com.sencorsta.ids.core.entity.annotation.RequestMapping;
import com.sencorsta.ids.core.entity.annotation.Service;
import com.sencorsta.ids.core.net.innerServer.RpcServerBootstrap;
import com.sencorsta.ids.core.processor.IdsThreadFactory;
import com.sencorsta.ids.core.processor.MessageProcessor;
import com.sencorsta.utils.file.FileUtil;
import com.sencorsta.utils.string.ColorString;
import com.sencorsta.utils.system.CpuUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 启动器
 *
 * @author daibin
 */
@Slf4j
public abstract class ApplicationText {

    /**
     * 维护线程
     */
    public final ScheduledExecutorService MAINTAIN = new ScheduledThreadPoolExecutor(1, new IdsThreadFactory("IDS-MAINTAIN"));

    /**
     * 应用类型
     */
    public String SERVER_TYPE;

    protected static ApplicationText instance;

    /**
     * 获取单例
     */
    public static ApplicationText instance() {
        return instance;
    }

    /**
     * 启动
     */
    public void start(String name) {
        try {
            // 打印banner
            System.out.println(getBanner());
            // 打印进度条
            doProcess();
            SERVER_TYPE = GlobalConfig.instance().getStr("server.type", ConfigGroup.core.getName(), "server");
            // 扫描包内组件
            scanPackage();
            // 启动服务
            RpcServerBootstrap boot = new RpcServerBootstrap(SERVER_TYPE);
            boot.start();
            // 打印配置参数
            GlobalConfig.instance().printValue();
            // 添加安全停服线程
            addCloseProcess();

            // 启动Master客户端
            //masterStart(name);
            // 如果不用nacos和master就自己和自己建立一个链接
            soloStart(name);
            // 启动完成后调用 继承用
            onStarted();

            log.info("操作系统环境" + " -> " + SystemUtil.getOsInfo().getName());
            log.info("CPU" + " -> " + CpuUtil.getCpu() + " " + Runtime.getRuntime().availableProcessors() + "Cores");
            log.info("内存大小" + " -> " + DataSizeUtil.format(SystemUtil.getFreeMemory()) + "(可用)/" + DataSizeUtil.format(SystemUtil.getMaxMemory()) + "(已申请)/" + DataSizeUtil.format(SystemUtil.getRuntimeInfo().getUsableMemory()) + "(剩余)");
            log.info("磁盘剩余空间" + " -> " + DataSizeUtil.format(new File(System.getProperty("user.dir")).getFreeSpace()));
            log.info("Java Version" + " -> " + SystemUtil.getJavaInfo().getVersion());
            String logLevel = GlobalConfig.instance().getStr("log.level", ConfigGroup.core.getName(), "info");
            log.info("Log Level" + " -> " + logLevel);
            String logHome = GlobalConfig.instance().getStr("log.home", ConfigGroup.core.getName(), "./log");
            log.info("Log Home" + " -> " + logHome);
            log.info("Log Size" + " -> " + DataSizeUtil.format(FileUtil.countFiles(logHome)));
            log.info("服务已成功启动运行喽，{}{}{}{}!", ColorString.getColor256Str("😺", "173"), ColorString.getColor256Str("😸", "246"), ColorString.getColor256Str("😹", "61"), ColorString.getColor256Str("😻", "15"));

            log.info("Powered by IDS© V2.0");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.warn("❌❌❌服务启动失败❌❌❌!系统将于3秒后关闭....");

            MAINTAIN.scheduleWithFixedDelay(new Runnable() {
                int count = 3;

                @Override
                public void run() {
                    log.warn(count + "...");
                    count--;
                }
            }, 0, 1, TimeUnit.SECONDS);
            MAINTAIN.schedule(new Runnable() {
                @Override
                public void run() {
                    log.warn("⚠系统关闭⚠");
                    System.exit(0);
                }
            }, 3, TimeUnit.SECONDS);
        }
    }

    private void scanPackage() {
        String packageName = ClassUtil.getPackage(this.getClass());
        Set<Class<?>> classes = ClassScanner.scanPackage(packageName, c -> {
            Annotation[] annotations = c.getAnnotations();
            Annotation annotation = Arrays.stream(annotations).filter(o -> o.annotationType().isAnnotationPresent(Component.class)).findAny().orElse(null);
            return annotation != null;
        });
        classes.forEach(c -> {
            try {
                if (AnnotationUtil.getAnnotation(c, Controller.class) != null) {
                    RequestMapping annotation = AnnotationUtil.getAnnotation(c, RequestMapping.class);
                    String url = annotation == null ? "" : annotation.value();
                    log.debug("注册 Controller -> {}", c.getName());
                    Arrays.stream(c.getMethods()).forEach(m -> {
                        RequestMapping fun = AnnotationUtil.getAnnotation(m, RequestMapping.class);
                        if (ObjectUtil.isNotNull(fun)) {
                            MessageProcessor.addMethod(url + fun.value(), new MethodProxy(c.getName(), m.getName()));
                            log.trace("加载 RequestMapping -> {}", url + fun.value());
                        }
                    });
                } else if (AnnotationUtil.getAnnotation(c, Service.class) != null) {
                    Class<?>[] interfaces = c.getInterfaces();
                    if (ObjectUtil.isNotEmpty(interfaces)) {
                        Arrays.stream(interfaces).forEach(i -> {
                            MessageProcessor.addService(i.getName(), c.getName());
                            log.trace("注册 Service {} -> {}", i.getName(), c.getName());
                        });
                    }
                } else {
                    MessageProcessor.addService(c.getName(), c.getName());
                    log.trace("注册 Component {} -> {}", c.getName(), c.getName());
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    AtomicInteger processCount = new AtomicInteger();

    private void doProcess() throws InterruptedException, IOException {
        int count = 50;
        String processHead = "[";
        String processTail = "]";
        StringBuilder negative = new StringBuilder("");
        for (int i = 0; i < count; i++) {
            negative.append("\uD83D\uDE38");
        }
        System.out.write("Starting...".getBytes(GlobalConfig.UTF_8));
        System.out.flush();
        Thread.sleep(300);
        int innerCount = 0;
        while (processCount.get() <= count) {
            synchronized (System.out) {
                Thread.sleep(100);
                extracted(count, processHead, processTail, negative, innerCount);
                if ((innerCount += 1) >= 3) {
                    innerCount = 0;
                    processCount.addAndGet(RandomUtil.randomInt(25));
                }
            }
        }
        processCount.set(count);
        innerCount = 0;
        for (int i = 0; i < 15; i++) {
            innerCount++;
            Thread.sleep(100);
            extracted(count, processHead, processTail, negative, innerCount);
        }
        System.out.write("\r \n".getBytes(GlobalConfig.UTF_8));
        System.out.flush();
    }

    private void extracted(int count, String processHead, String processTail, StringBuilder negative, int innerCount) throws IOException {
        System.out.write("\r".getBytes(GlobalConfig.UTF_8));
        System.out.flush();
        StringBuilder active = new StringBuilder();
        for (int i = 0; i < processCount.get(); i++) {
            int codeNum = codes.size();
            int index = (i + processCount.get() + innerCount) % codeNum;
            active.append(ColorString.getColor256Str("😺", codes.get(index) + ""));
        }
        String s = processCount.get() * 100 / count + "%";
        String finalString = (processHead + active + negative.substring(0, Math.min(negative.length(), (count - processCount.get())) * 2) + processTail + s);
        System.out.write(finalString.getBytes(GlobalConfig.UTF_8));
        System.out.flush();
    }

    List<String> codes = Lists.newArrayList("9", "202", "227", "82", "33", "57", "129");

    private String getBanner() {

        ClassPathResource resource = new ClassPathResource("banner.txt");
        List<String> strings = IoUtil.readLines(resource.getStream(), StandardCharsets.UTF_8, new ArrayList<>());

        StringBuilder banner = new StringBuilder("\n");
        int size = strings.size();
        int codeNum = codes.size();
        for (int i = 0; i < size; i++) {
            int index = codeNum * i / size;
            banner.append(ColorString.getColor256Head(codes.get(index))).append(strings.get(i)).append("\n");
        }
        banner.append(ColorString.getColor256Str("", "1"));
        return banner.toString();
    }


    private void soloStart(String name) {

    }


    /**
     * 关闭程序时保存在线玩家的数据
     */
    protected void addCloseProcess() {
        Runtime.getRuntime().addShutdownHook(new Thread("ShutdownHook") {
            @Override
            public void run() {
                log.info("正在进行安全停服中...");
                try {
                    onCloseGame();
                } finally {
                    try {
                        resourceRelease();
                    } finally {
                        //保存需要保存的数据
                    }
                }
                log.info("服务器已安全停止，可以继续执行后续的工作了，O(∩_∩)O~");
            }
        });
    }

    protected void onStarted() {
        log.trace("‼请继承此方法‼ Started...");
    }

    protected void onCloseGame() {
        log.trace("‼请继承此方法‼ close zone...");
    }

    protected void resourceRelease() {
        log.trace("‼请继承此方法‼ resourceRelease...");
    }

    public void onServiceClose(Channel channel) {
        log.trace("‼请继承此方法‼ onServiceClose...");
    }

    public void onOpenClose(Channel channel) {
        log.trace("‼请继承此方法‼ onOpenClose...");
    }

    public void onUserLeave(String userId) {
        log.trace("‼请继承此方法‼ onUserLeave...");
    }

}
