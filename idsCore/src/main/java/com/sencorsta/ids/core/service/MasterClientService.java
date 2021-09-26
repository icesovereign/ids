package com.sencorsta.ids.core.service;

import com.sencorsta.ids.core.entity.Server;
import com.sencorsta.ids.core.entity.annotation.Service;
import io.netty.channel.Channel;

import java.util.Map;

/**
 * @author ICe
 */
@Service
public interface MasterClientService {
    /**
     * 获取所有的服务器
     *
     * @param channel 请求体
     */
    void getTotalServer(Channel channel);

    /**
     * 加入master
     *
     * @param channel master的channel
     */
    void joinMaster(Channel channel);

    /**
     * 维持心跳
     *
     * @param request 请求体
     * @return 响应结果
     */
    boolean pingMaster(Channel request);

    /**
     * 收到所有服务器数据包
     *
     * @param data    服务器列表
     * @param channel channel
     */
    void onTotalServer(Map<String, Map<String, Server>> data, Channel channel);
}
