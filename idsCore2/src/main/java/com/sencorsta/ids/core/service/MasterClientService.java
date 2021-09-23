package com.sencorsta.ids.core.service;

import com.sencorsta.ids.core.entity.IdsRequest;
import com.sencorsta.ids.core.entity.IdsResponse;
import com.sencorsta.ids.core.entity.Server;
import com.sencorsta.ids.core.entity.annotation.Service;
import io.netty.channel.Channel;

import java.util.List;
import java.util.Map;

/**
 * @author ICe
 */
@Service
public interface MasterClientService {
    /**
     * 获取所有的服务器
     *
     * @param request 请求体
     * @return 服务器列表
     */
    void getTotalServer(Channel channel);

    /**
     * 加入master
     *
     * @param channel master的channel
     * @return 响应结果
     */
    void joinMaster(Channel channel);

    /**
     * 维持心跳
     *
     * @param request 请求体
     * @return 响应结果
     */
    IdsResponse<Object> pingMaster(IdsRequest<Object> request);

    /**
     * 收到所有服务器数据包
     *  @param data
     * @param channel
     */
    void onTotalServer(Map<String, List<Server>> data, Channel channel);
}
