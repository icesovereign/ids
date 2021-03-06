package com.sencorsta.ids.core.application;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.io.unit.DataSizeUtil;
import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sencorsta.ids.core.application.master.MasterClient;
import com.sencorsta.ids.core.config.ConfigGroup;
import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.entity.IdsRequest;
import com.sencorsta.ids.core.entity.MethodProxy;
import com.sencorsta.ids.core.entity.Server;
import com.sencorsta.ids.core.entity.annotation.Component;
import com.sencorsta.ids.core.entity.annotation.Controller;
import com.sencorsta.ids.core.entity.annotation.RequestMapping;
import com.sencorsta.ids.core.entity.annotation.Service;
import com.sencorsta.ids.core.net.innerServer.RpcServerBootstrap;
import com.sencorsta.ids.core.processor.IdsThreadFactory;
import com.sencorsta.ids.core.processor.MessageProcessor;
import com.sencorsta.utils.file.FileUtil;
import com.sencorsta.utils.object.Classes;
import com.sencorsta.utils.string.ColorString;
import com.sencorsta.utils.system.CpuUtil;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ?????????
 *
 * @author ICe
 */
@Slf4j
public abstract class Application {

    /**
     * ????????????
     */
    public final ScheduledExecutorService MAINTAIN = new ScheduledThreadPoolExecutor(1, new IdsThreadFactory("maintain"));

    /**
     * ???????????????
     */
    @Getter
    public final Map<String, Map<String,Server>> totalServers = new ConcurrentHashMap<>();

    /**
     * ????????????
     */
    public String SERVER_TYPE;

    protected static Application instance;

    /**
     * ????????????
     */
    public static Application instance() {
        return instance;
    }

    /**
     * ??????
     */
    public void start(String name) {
        try {
            name = StrUtil.isNotBlank(name) ? name : "app";
            // ??????banner
            System.out.println(getBanner());
            // ???????????????
            doProcess();
            SERVER_TYPE = GlobalConfig.instance().getStr("server.type", ConfigGroup.core.getName(), "server");
            // ??????????????????
            GlobalConfig.instance().printValue();
            // ??????????????????
            scanPackage();
            // ????????????
            RpcServerBootstrap boot = new RpcServerBootstrap(SERVER_TYPE);
            boot.start();
            // ????????????????????????
            addCloseProcess();

            // ??????Master?????????
            masterStart(name);
            // ????????????nacos???master????????????????????????????????????
            soloStart(name);
            // ????????????????????? ?????????
            onStarted();

            log.info("??????????????????" + " -> " + SystemUtil.getOsInfo().getName());
            log.info("CPU" + " -> " + CpuUtil.getCpu() + " " + Runtime.getRuntime().availableProcessors() + "Cores");
            log.info("????????????" + " -> " + DataSizeUtil.format(SystemUtil.getFreeMemory()) + "(??????)/" + DataSizeUtil.format(SystemUtil.getTotalMemory()) + "(?????????)/" + DataSizeUtil.format(SystemUtil.getRuntimeInfo().getUsableMemory()) + "(??????)");
            log.info("??????????????????" + " -> " + DataSizeUtil.format(new File(System.getProperty("user.dir")).getFreeSpace()));
            log.info("Java Version" + " -> " + SystemUtil.getJavaInfo().getVersion());
            String logLevel = GlobalConfig.instance().getStr("log.level", ConfigGroup.core.getName(), "info");
            log.info("Log Level" + " -> " + logLevel);
            String logHome = GlobalConfig.instance().getStr("log.home", ConfigGroup.core.getName(), "./log");
            log.info("Log Home" + " -> " + new File(logHome).getAbsolutePath());
            log.info("Log Size" + " -> " + DataSizeUtil.format(FileUtil.countFiles(logHome)));
            log.info("?????????????????????????????????{}{}{}{}!", ColorString.getColor256Str("????", "173"), ColorString.getColor256Str("????", "246"), ColorString.getColor256Str("????", "61"), ColorString.getColor256Str("????", "15"));

            log.info("Powered by IDS?? V2.0");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.warn("????????????????????????????????????!????????????3????????????....");

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
                    log.warn("??????????????????");
                    System.exit(0);
                }
            }, 3, TimeUnit.SECONDS);
        }
    }

    private void masterStart(String name) throws Exception {
        if (GlobalConfig.instance().getBool("enable", ConfigGroup.master.getName(), true)) {
            try {
                MasterClient.getInstance().start();
                return;
            } catch (Exception e) {
                throw new Exception("master????????????,?????????master????????????,????????????????????????");
            }
        } else {
            log.trace("MasterClient???????????????");
        }
    }

    private void scanPackage() {
        String packageName = ClassUtil.getPackage(this.getClass());
        String packageCore = "com.sencorsta.ids.core";
        Set<Class<?>> classes = new HashSet<>();
        Set<Class<?>> temp1 = ClassScanner.scanPackage(packageName, c -> {
            Annotation[] annotations = c.getAnnotations();
            Annotation annotation = Arrays.stream(annotations).filter(o -> o.annotationType().isAnnotationPresent(Component.class)).findAny().orElse(null);
            return annotation != null;
        });
        Set<Class<?>> temp2 = ClassScanner.scanPackage(packageCore, c -> {
            Annotation[] annotations = c.getAnnotations();
            Annotation annotation = Arrays.stream(annotations).filter(o -> o.annotationType().isAnnotationPresent(Component.class)).findAny().orElse(null);
            return annotation != null;
        });
        classes.addAll(temp1);
        classes.addAll(temp2);
        // ??????????????????????????????component service??????controller
        Set<Class<?>> services = Sets.newHashSet();
        Set<Class<?>> controllers = Sets.newHashSet();
        Set<Class<?>> components = Sets.newHashSet();
        classes.forEach(c -> {
            try {
                if (AnnotationUtil.getAnnotation(c, Controller.class) != null) {
                    controllers.add(c);
                } else if (AnnotationUtil.getAnnotation(c, Service.class) != null) {
                    services.add(c);
                } else {
                    components.add(c);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });

        components.forEach(it -> {
            try {
                processComponent(it);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });

        services.forEach(it -> {
            try {
                processService(it);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });

        controllers.forEach(it -> {
            try {
                processController(it);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });


    }

    private void processComponent(Class<?> c) {
        MessageProcessor.addService(c.getName(), c.getName());
        log.trace("?????? Component {} -> {}", c.getName(), c.getName());
    }

    private void processService(Class<?> c) {
        Class<?>[] interfaces = c.getInterfaces();
        if (ObjectUtil.isNotEmpty(interfaces)) {
            Arrays.stream(interfaces).forEach(i -> {
                MessageProcessor.addService(i.getName(), c.getName());
                log.trace("?????? Service {} -> {}", i.getName(), c.getName());
            });
        }
    }

    private void processController(Class<?> c) {
        RequestMapping annotation = AnnotationUtil.getAnnotation(c, RequestMapping.class);
        String url = annotation == null ? "" : annotation.value();
        log.debug("?????? Controller -> {}", c.getName());
        Class<Object> clazz = ClassUtil.loadClass(c.getName());
        Object[] fields = Classes.getFields(clazz);
        Object instance = Singleton.get(clazz, fields);
        Arrays.stream(c.getMethods()).forEach(m -> {
            RequestMapping fun = AnnotationUtil.getAnnotation(m, RequestMapping.class);
            if (ObjectUtil.isNotNull(fun)) {
                Type genericReturnType = Arrays.stream(m.getGenericParameterTypes()).filter(o -> o instanceof ParameterizedType).findFirst().orElse(null);
                String typeName = "";
                if (ObjectUtil.isNotNull(genericReturnType)) {
                    Type type = Arrays.stream(((ParameterizedType) genericReturnType).getActualTypeArguments()).findFirst().orElse(null);
                    if (ObjectUtil.isNotNull(type)) {
                        typeName = type.getTypeName();
                        ClassUtil.loadClass(typeName);
                    }
                }
                final Method method = ClassUtil.getDeclaredMethod(clazz, m.getName(), IdsRequest.class);
                Class<Object> valueType = ClassUtil.loadClass(typeName);
                MessageProcessor.addMethod(url + fun.value(), new MethodProxy(instance, method, valueType));
                log.trace("?????? RequestMapping -> {} type:{}", url + fun.value(), typeName);
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
            active.append(ColorString.getColor256Str("????", codes.get(index) + ""));
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
     * ??????????????????????????????????????????
     */
    protected void addCloseProcess() {
        Runtime.getRuntime().addShutdownHook(new Thread("ShutdownHook") {
            @Override
            public void run() {
                log.info("???????????????????????????...");
                try {
                    onCloseGame();
                } finally {
                    try {
                        resourceRelease();
                    } finally {
                        //???????????????????????????
                    }
                }
                log.info("??????????????????????????????????????????????????????????????????O(???_???)O~");
            }
        });
    }

    protected void onStarted() {
        log.trace("???????????????????????? Started...");
    }

    protected void onCloseGame() {
        log.trace("???????????????????????? close zone...");
    }

    protected void resourceRelease() {
        log.trace("???????????????????????? resourceRelease...");
    }

    public void onServiceClose(Channel channel) {
        log.trace("???????????????????????? onServiceClose...");
    }

    public void onOpenClose(Channel channel) {
        log.trace("???????????????????????? onOpenClose...");
    }

    public void onUserLeave(String userId) {
        log.trace("???????????????????????? onUserLeave...");
    }

}
