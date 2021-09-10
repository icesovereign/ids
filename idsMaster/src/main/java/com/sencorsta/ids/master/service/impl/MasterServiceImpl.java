package com.sencorsta.ids.master.service.impl;


import com.sencorsta.ids.api.request.GetTotalServerRequest;
import com.sencorsta.ids.api.request.JoinMasterRequest;
import com.sencorsta.ids.api.request.PingMasterRequest;
import com.sencorsta.ids.api.response.GetTotalServerResponse;
import com.sencorsta.ids.api.response.JoinMasterResponse;
import com.sencorsta.ids.api.response.PingMasterResponse;
import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.constant.ErrorCodeConstant;
import com.sencorsta.ids.core.entity.ErrorCode;
import com.sencorsta.ids.core.entity.IdsResponse;
import com.sencorsta.ids.core.entity.Server;
import com.sencorsta.ids.core.entity.annotation.Autowired;
import com.sencorsta.ids.core.entity.annotation.Service;
import com.sencorsta.ids.master.dao.MasterDao;
import com.sencorsta.ids.master.service.MasterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author daibin
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
    public IdsResponse<PingMasterResponse> pingMaster(PingMasterRequest data) {
        PingMasterResponse pingMasterResponse = new PingMasterResponse();
        Server server = data.getChannel().attr(GlobalConfig.SERVER_KEY).get();
        if (server == null) {
            return new IdsResponse<>(pingMasterResponse, ErrorCodeConstant.NOT_FIND);
        }
        int freeMemory = data.getFreeMemory();
        server.setFreeMemory(freeMemory);
//        Out.trace(server.sid, " ping : ", server.backHost + ":", server.backPort, " 内存占用:",
//                server.maxMemory - server.freeMemory, "/", server.maxMemory);
        return new IdsResponse<>(pingMasterResponse);
    }

    @Override
    public IdsResponse<JoinMasterResponse> joinMaster(JoinMasterRequest data) {
        Server server = data.getServer();

//        try {
//            String ip = channel.remoteAddress().toString();
//            ip = ip.substring(1, ip.indexOf(":"));
//
//            if ((server.backHost == null) && (server.backPort > 0)) {
//                server.backHost = ip;
//            }
//            server.sid = idsMaster.getSidByType(server);
//        } catch (Exception e) {
//            Out.error(e.getMessage());
//            return error(ErrorCodeList.MASTER_SID_FULL);
//        }
//        server.bind(channel);
//        idsMaster.addServer(server);
//
//        JSONObject res = new JSONObject();
//        res.put("sid", server.sid);
//        return response(res);

        return null;
    }

    @Override
    public IdsResponse<GetTotalServerResponse> getTotalServer(GetTotalServerRequest data) {
        return null;
    }
}
