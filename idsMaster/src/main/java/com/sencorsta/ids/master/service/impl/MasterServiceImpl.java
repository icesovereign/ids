package com.sencorsta.ids.master.service.impl;


import com.sencorsta.ids.core.application.master.request.GetTotalServerRequest;
import com.sencorsta.ids.core.application.master.request.JoinMasterRequest;
import com.sencorsta.ids.core.application.master.request.PingMasterRequest;
import com.sencorsta.ids.core.application.master.response.GetTotalServerResponse;
import com.sencorsta.ids.core.application.master.response.JoinMasterResponse;
import com.sencorsta.ids.core.application.master.response.PingMasterResponse;
import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.constant.ErrorCodeConstant;
import com.sencorsta.ids.core.entity.ErrorCode;
import com.sencorsta.ids.core.entity.IdsResponse;
import com.sencorsta.ids.core.entity.Server;
import com.sencorsta.ids.core.entity.annotation.Autowired;
import com.sencorsta.ids.core.entity.annotation.Service;
import com.sencorsta.ids.master.IdsMaster;
import com.sencorsta.ids.master.dao.MasterDao;
import com.sencorsta.ids.master.service.MasterService;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ICe
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MasterServiceImpl implements MasterService {

    private final MasterDao masterDao;

    @Override
    public String helloWorld(String s) {
        String name = masterDao.getName(s);
        log.info("你好啊!!!" + name);
        return name;
    }

    @Override
    public IdsResponse<PingMasterResponse> pingMaster(PingMasterRequest data, Channel channel) throws ErrorCode {
        PingMasterResponse pingMasterResponse = new PingMasterResponse();
        Server server = channel.attr(GlobalConfig.SERVER_KEY).get();
        if (server == null) {
            throw ErrorCodeConstant.MASTER_CAN_NOT_FIND_SERVER;
        }
        long freeMemory = data.getFreeMemory();
        server.setFreeMemory(freeMemory);
        log.trace(server.getInfo());
        return new IdsResponse<>(pingMasterResponse);
    }

    @Override
    public IdsResponse<JoinMasterResponse> joinMaster(JoinMasterRequest data, Channel channel) throws ErrorCode {
        Server server = data.getServer();
        try {
            server.setSid(IdsMaster.getSidByType(server));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw ErrorCodeConstant.MASTER_SID_FULL;
        }
        server.bind(channel);
        IdsMaster.addServer(server);

        JoinMasterResponse joinMasterResponse = new JoinMasterResponse();
        joinMasterResponse.setSid(server.getSid());
        return new IdsResponse<>(joinMasterResponse);
    }

    @Override
    public IdsResponse<GetTotalServerResponse> getTotalServer(GetTotalServerRequest data) {
        GetTotalServerResponse response = new GetTotalServerResponse();
        response.setTotalServers(IdsMaster.instance().totalServers);
        return new IdsResponse<>(response);
    }
}
