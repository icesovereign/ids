package com.sencorsta.ids.master.service;


import com.sencorsta.ids.api.request.GetTotalServerRequest;
import com.sencorsta.ids.api.request.JoinMasterRequest;
import com.sencorsta.ids.api.request.PingMasterRequest;
import com.sencorsta.ids.api.response.GetTotalServerResponse;
import com.sencorsta.ids.api.response.JoinMasterResponse;
import com.sencorsta.ids.api.response.PingMasterResponse;
import com.sencorsta.ids.core.entity.IdsResponse;

/**
 * @author daibin
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
     * @return
     */
    IdsResponse<PingMasterResponse> pingMaster(PingMasterRequest data);

    /**
     * 加入master
     *
     * @param data
     * @return
     */
    IdsResponse<JoinMasterResponse> joinMaster(JoinMasterRequest data);

    /**
     * 获取服务器列表
     *
     * @param data
     * @return
     */
    IdsResponse<GetTotalServerResponse> getTotalServer(GetTotalServerRequest data);
}
