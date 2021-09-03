package com.sencorsta.ids.core.application;

import ch.qos.logback.core.pattern.color.ANSIConstants;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.io.unit.DataSizeUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.system.SystemUtil;
import com.google.common.collect.Lists;
import com.sencorsta.ids.core.config.ConfigGroup;
import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.net.innerServer.RpcServerBootstrap;
import com.sencorsta.ids.core.processor.IdsThreadFactory;
import com.sencorsta.utils.file.FileUtil;
import com.sencorsta.utils.string.ColorString;
import com.sencorsta.utils.system.CpuUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;


import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 启动器
 *
 * @author daibin
 */
@Slf4j
public class Application {

    /**
     * 维护线程
     */
    public static final ScheduledExecutorService MAINTAIN = new ScheduledThreadPoolExecutor(1, new IdsThreadFactory("IDS-MAINTAIN"));

    /**
     * 应用类型
     */
    public static String SERVER_TYPE;
    /**
     * 单例
     */
    protected static Application instance;

    public Application() {
    }

    private static class SingletonHolder {
        private static final Application INSTANCE = new Application();
    }

    /**
     * 获取单例
     */
    public static Application instance() {
        return Application.SingletonHolder.INSTANCE;
    }

    /**
     * 启动
     */
    public void start(String name) {
        try {
            GlobalConfig.instance();
            log.info(getBanner());
            SERVER_TYPE = GlobalConfig.instance().getStr("server.type", ConfigGroup.core.getName(), "server");

            RpcServerBootstrap boot = new RpcServerBootstrap(SERVER_TYPE);
            boot.start();
            addCloseProcess();

            // 启动Master客户端
            //masterStart(name);
            // 如果不用nacos和master就自己和自己建立一个链接
            soloStart(name);

            // 启动完成后调用 继承用
            onStarted();

            log.info("操作系统环境" + " -> " + SystemUtil.getOsInfo().getName());
            log.info("CPU" + " -> " + CpuUtil.getCpu() + " " + Runtime.getRuntime().availableProcessors() + "Cores");
            log.info("内存大小：" + " -> " + DataSizeUtil.format(SystemUtil.getFreeMemory()) + "(可用)/" + DataSizeUtil.format(SystemUtil.getFreeMemory()) + "(已申请)/" + DataSizeUtil.format(SystemUtil.getRuntimeInfo().getUsableMemory()) + "(剩余)");
            log.info("磁盘剩余空间：" + " -> " + DataSizeUtil.format(new File(System.getProperty("user.dir")).getFreeSpace()));
            log.info("Java Version：" + " -> " + SystemUtil.getJavaInfo().getVersion());
            log.info("Log Size：" + " -> " + DataSizeUtil.format(FileUtil.countFiles(GlobalConfig.instance().getStr("log.home", ConfigGroup.core.getName(), "./log"))));
            log.info("服务已成功启动运行喽，{}{}{}{}!", ColorString.getColor256Str("😺", "173"), ColorString.getColor256Str("😸", "246"), ColorString.getColor256Str("😹", "61"), ColorString.getColor256Str("😻", "15"));
            log.info("Powered by IDS© V2.0");
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
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

    private String getBanner() {

        ClassPathResource resource = new ClassPathResource("banner.txt");
        List<String> strings = cn.hutool.core.io.FileUtil.readLines(resource.getFile(), StandardCharsets.UTF_8);

        List<String> codes =  Lists.newArrayList("9","202","227","82","33","57","129");
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
