package com.sencorsta.ids.core.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.system.SystemUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sencorsta.ids.core.application.master.request.GetTotalServerRequest;
import com.sencorsta.ids.core.application.master.request.JoinMasterRequest;
import com.sencorsta.ids.core.application.master.response.GetTotalServerResponse;
import com.sencorsta.ids.core.application.master.response.JoinMasterResponse;
import com.sencorsta.ids.core.config.ConfigGroup;
import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.constant.ProtocolTypeConstant;
import com.sencorsta.ids.core.constant.SerializeTypeConstant;
import com.sencorsta.ids.core.entity.IdsRequest;
import com.sencorsta.ids.core.entity.IdsResponse;
import com.sencorsta.ids.core.entity.Server;
import com.sencorsta.ids.core.entity.annotation.Autowired;
import com.sencorsta.ids.core.entity.annotation.Service;
import com.sencorsta.ids.core.net.protocol.RpcMessage;
import com.sencorsta.ids.core.processor.MessageProcessor;
import com.sencorsta.ids.core.service.MasterClientService;
import com.sencorsta.utils.object.Jsons;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 实现类
 *
 * @author ICe
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MasterClientServiceImpl implements MasterClientService {
    Server localServer;

    @Override
    public void getTotalServer(Channel channel) {
        GetTotalServerRequest getTotalServerRequest = new GetTotalServerRequest();
        getTotalServerRequest.setFreeMemory(SystemUtil.getFreeMemory());
        RpcMessage message = new RpcMessage(ProtocolTypeConstant.TYPE_RPC_REQ);
        message.setMethod("/master/getTotalServer");
        message.setSerializeType(SerializeTypeConstant.TYPE_JSON);
        message.setJsonData(getTotalServerRequest);
        message.setChannel(channel);

        RpcMessage responseMsg = MessageProcessor.request(message);
        if (ObjectUtil.isNotNull(responseMsg)) {
            IdsResponse<GetTotalServerResponse> getTotalServerResponse = Jsons.toBean(responseMsg.getData(), new TypeReference<IdsResponse<GetTotalServerResponse>>() {
            });
            if (ObjectUtil.isNotNull(getTotalServerResponse) && getTotalServerResponse.getCode() == 0) {
                GetTotalServerResponse data = getTotalServerResponse.getData();
                onTotalServer(data.getTotalServers(), responseMsg.getChannel());
            }
        }
    }

    @Override
    public void joinMaster(Channel channel) {
        JoinMasterRequest joinMasterRequest = new JoinMasterRequest();

        RpcMessage message = new RpcMessage(ProtocolTypeConstant.TYPE_RPC_REQ);
        message.setMethod("/master/joinMaster");
        message.setSerializeType(SerializeTypeConstant.TYPE_JSON);

        if (localServer == null) {
            localServer = new Server();
            GlobalConfig config = GlobalConfig.instance();
            localServer.setType(config.getStr("server.type", ConfigGroup.core.getName(), null));
            localServer.setHost(config.getStr("host", ConfigGroup.server.getName(), null));
            localServer.setPublicHost(config.getStr("host.public", ConfigGroup.server.getName(), null));
            localServer.setPort(config.getInt("port", ConfigGroup.server.getName(), null));
            localServer.setFreeMemory(SystemUtil.getFreeMemory());
            localServer.setMaxMemory(SystemUtil.getMaxMemory());
        }
        joinMasterRequest.setServer(localServer);
        message.setJsonData(joinMasterRequest);
        message.setChannel(channel);
        RpcMessage response = MessageProcessor.request(message);
        if (ObjectUtil.isNotNull(response)) {
            IdsResponse<JoinMasterResponse> joinMasterResponse = Jsons.toBean(response.getData(), new TypeReference<IdsResponse<JoinMasterResponse>>() {
            });
            if (ObjectUtil.isNotNull(joinMasterResponse) && joinMasterResponse.getCode() == 0) {
                JoinMasterResponse data = joinMasterResponse.getData();
                localServer.setSid(data.getSid());
                log.info("注册master成功啦😀! sid:{}", data.getSid());
            } else {
                log.info("注册master失败😭:{} ", joinMasterResponse);
            }
        }
    }

    @Override
    public IdsResponse<Object> pingMaster(IdsRequest<Object> request) {
        return null;
    }

    @Override
    public void onTotalServer(Map<String, List<Server>> data, Channel channel) {
        log.debug("收到master服务器");
//        ProxyClient.maintenanceList(json.getJSONObject("data").getJSONObject("totalServers"));
    }
}
