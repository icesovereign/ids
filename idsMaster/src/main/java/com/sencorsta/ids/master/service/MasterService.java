package com.sencorsta.ids.master.service;


import com.sencorsta.ids.core.application.master.request.GetTotalServerRequest;
import com.sencorsta.ids.core.application.master.request.JoinMasterRequest;
import com.sencorsta.ids.core.application.master.request.PingMasterRequest;
import com.sencorsta.ids.core.application.master.response.GetTotalServerResponse;
import com.sencorsta.ids.core.application.master.response.JoinMasterResponse;
import com.sencorsta.ids.core.application.master.response.PingMasterResponse;
import com.sencorsta.ids.core.entity.ErrorCode;
import com.sencorsta.ids.core.entity.IdsResponse;
import io.netty.channel.Channel;

/**
 * @author ICe
 */
public interface MasterService {
    /**
     * 测试方法
     *
     * @param s
     * @return
     */
    String helloWorld(String s);

    /**
     * ping master
     *
     * @param data
     * @param channel
     * @return
     */
    IdsResponse<PingMasterResponse> pingMaster(PingMasterRequest data, Channel channel) throws ErrorCode;

    /**
     * 加入master
     *
     * @param data
     * @param channel
     * @return
     */
    IdsResponse<JoinMasterResponse> joinMaster(JoinMasterRequest data, Channel channel) throws ErrorCode;

    /**
     * 获取服务器列表
     *
     * @param data
     * @return
     */
    IdsResponse<GetTotalServerResponse> getTotalServer(GetTotalServerRequest data);
}
