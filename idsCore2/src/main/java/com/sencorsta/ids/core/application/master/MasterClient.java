package com.sencorsta.ids.core.application.master;

import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ObjectUtil;
import com.sencorsta.ids.core.application.Application;
import com.sencorsta.ids.core.application.master.request.GetTotalServerRequest;
import com.sencorsta.ids.core.application.master.request.TotalServerPush;
import com.sencorsta.ids.core.application.master.response.GetTotalServerResponse;
import com.sencorsta.ids.core.config.ConfigGroup;
import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.entity.IdsRequest;
import com.sencorsta.ids.core.entity.IdsResponse;
import com.sencorsta.ids.core.entity.annotation.Autowired;
import com.sencorsta.ids.core.entity.annotation.Controller;
import com.sencorsta.ids.core.entity.annotation.RequestMapping;
import com.sencorsta.ids.core.net.handle.RpcChannelHandler;
import com.sencorsta.ids.core.net.innerClient.RpcClientBootstrap;
import com.sencorsta.ids.core.net.innerClient.RpcCodecFactory;
import com.sencorsta.ids.core.processor.MessageProcessor;
import com.sencorsta.ids.core.service.MasterClientService;
import com.sencorsta.utils.object.Classes;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;


/**
 * @author ICe
 */
@Controller
@RequestMapping("/masterClient")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MasterClient {

    private final MasterClientService masterClientService;

    private static MasterClient instance;

    public static MasterClient getInstance() {
        if (instance == null) {
            instance = (MasterClient) Classes.newInstance(MasterClient.class.getName());
        }
        return instance;
    }

    Channel connect;

    public void start() {
        RpcClientBootstrap bootstrap = new RpcClientBootstrap("client-master", new RpcCodecFactory(new RpcChannelHandler()));
        connect = bootstrap.connect(GlobalConfig.instance().getStr("host", ConfigGroup.master.getName(), "0.0.0.0"), GlobalConfig.instance().getInt("port", ConfigGroup.master.getName()));
        masterClientService.joinMaster(connect);
    }

    @RequestMapping("/totalServers")
    public void getTotalServer(IdsRequest<TotalServerPush> push) {
        masterClientService.onTotalServer(push.getData().getTotalServers(),push.getChannel());
    }
}
