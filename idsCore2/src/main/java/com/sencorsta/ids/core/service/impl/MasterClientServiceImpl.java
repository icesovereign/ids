package com.sencorsta.ids.core.service.impl;

import com.sencorsta.ids.core.entity.IdsRequest;
import com.sencorsta.ids.core.entity.IdsResponse;
import com.sencorsta.ids.core.entity.annotation.Autowired;
import com.sencorsta.ids.core.entity.annotation.Service;
import com.sencorsta.ids.core.service.MasterClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 实现类
 * @author daibin
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MasterClientServiceImpl implements MasterClientService {
    @Override
    public IdsResponse<Object> getTotalServer(IdsRequest<Object> request) {
        return null;
    }

    @Override
    public IdsResponse<Object> joinMaster(IdsRequest<Object> request) {
        return null;
    }

    @Override
    public IdsResponse<Object> pingMaster(IdsRequest<Object> request) {
        return null;
    }
}
