package com.sencorsta.ids.core.application.master;

import com.sencorsta.ids.core.application.master.request.TotalServerPush;
import com.sencorsta.ids.core.config.ConfigGroup;
import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.entity.IdsRequest;
import com.sencorsta.ids.core.entity.annotation.Autowired;
import com.sencorsta.ids.core.entity.annotation.Controller;
import com.sencorsta.ids.core.entity.annotation.RequestMapping;
import com.sencorsta.ids.core.net.handle.RpcChannelHandler;
import com.sencorsta.ids.core.net.innerClient.RpcClientBootstrap;
import com.sencorsta.ids.core.net.innerClient.RpcCodecFactory;
import com.sencorsta.ids.core.processor.IdsThreadFactory;
import com.sencorsta.ids.core.service.MasterClientService;
import com.sencorsta.utils.object.Classes;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author ICe
 */
@Controller
@RequestMapping("/masterClient")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MasterClient {

    private final MasterClientService masterClientService;

    private static MasterClient instance;

    public static MasterClient getInstance() {
        if (instance == null) {
            instance = (MasterClient) Classes.newInstance(MasterClient.class.getName());
        }
        return instance;
    }

    /**
     * master的链接
     */
    Channel masterChannel;

    /**
     * 是否断线
     */
    boolean disconnect = true;
    /**
     * 维护线程
     */
    public final ScheduledExecutorService MAINTAIN = new ScheduledThreadPoolExecutor(1, new IdsThreadFactory("masterClient"));

    public void start() {
        RpcClientBootstrap bootstrap = new RpcClientBootstrap("master", new RpcCodecFactory(new RpcChannelHandler()));
        Integer interval = GlobalConfig.instance().getInt("heart.interval", ConfigGroup.performance.getName(), 30);
        MAINTAIN.scheduleWithFixedDelay(() -> {
            try {
                if (disconnect) {
                    log.debug("开始连接 master ....");
                    masterChannel = bootstrap.connect(GlobalConfig.instance().getStr("host", ConfigGroup.master.getName(), "0.0.0.0"), GlobalConfig.instance().getInt("port", ConfigGroup.master.getName()));
                    if (masterChannel != null && masterChannel.isActive()) {
                        log.debug("连接成功 master !");
                        masterClientService.joinMaster(masterChannel);
                        disconnect = false;
                    } else {
                        log.warn("连接失败 master {}秒后重试...", interval);
                    }
                }
                if (!masterClientService.pingMaster(masterChannel)) {
                    //master没有响应了
                    log.warn("master 未响应");
                    disconnect = true;
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }, 3, interval, TimeUnit.SECONDS);

    }

    @RequestMapping("/totalServers")
    public void getTotalServer(IdsRequest<TotalServerPush> push) {
        masterClientService.onTotalServer(push.getData().getTotalServers(), push.getChannel());
    }
}
