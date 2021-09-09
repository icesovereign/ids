package com.sencorsta.ids.core.service;

import com.sencorsta.ids.core.entity.IdsRequest;
import com.sencorsta.ids.core.entity.IdsResponse;

/**
 * @author daibin
 */
public interface MasterClientService {
    /**
     * 获取所有的服务器
     *
     * @param request 请求体
     * @return 服务器列表
     */
    IdsResponse<Object> getTotalServer(IdsRequest<Object> request);

    /**
     * 加入master
     *
     * @param request 请求体
     * @return 响应结果
     */
    IdsResponse<Object> joinMaster(IdsRequest<Object> request);

    /**
     * 维持心跳
     *
     * @param request 请求体
     * @return 响应结果
     */
    IdsResponse<Object> pingMaster(IdsRequest<Object> request);
}
