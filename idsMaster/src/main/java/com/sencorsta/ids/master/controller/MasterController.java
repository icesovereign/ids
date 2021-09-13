package com.sencorsta.ids.master.controller;

import com.sencorsta.ids.api.request.GetTotalServerRequest;
import com.sencorsta.ids.api.request.JoinMasterRequest;
import com.sencorsta.ids.api.request.PingMasterRequest;
import com.sencorsta.ids.api.response.GetTotalServerResponse;
import com.sencorsta.ids.api.response.JoinMasterResponse;
import com.sencorsta.ids.api.response.PingMasterResponse;
import com.sencorsta.ids.core.entity.ErrorCode;
import com.sencorsta.ids.core.entity.IdsRequest;
import com.sencorsta.ids.core.entity.IdsResponse;
import com.sencorsta.ids.core.entity.annotation.Autowired;
import com.sencorsta.ids.core.entity.annotation.Controller;
import com.sencorsta.ids.core.entity.annotation.RequestMapping;
import com.sencorsta.ids.master.service.MasterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author daibin
 */
@Controller
@RequestMapping("/master")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MasterController {

    private final MasterService masterService;

    @RequestMapping("/helloWorld")
    public IdsResponse<String> helloWorld(IdsRequest<byte[]> request) {
        return new IdsResponse<>(masterService.helloWorld(new String(request.getData())));
    }

    @RequestMapping("/sleep")
    public IdsResponse<String> sleep(IdsRequest<byte[]> request) {
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new IdsResponse<>(masterService.helloWorld(new String(request.getData())));
    }

    @RequestMapping("/pingMaster")
    public IdsResponse<PingMasterResponse> pingMaster(IdsRequest<PingMasterRequest> request) throws ErrorCode {
        request.getData().setChannel(request.getChannel());
        return masterService.pingMaster(request.getData());
    }

    @RequestMapping("/joinMaster")
    public IdsResponse<JoinMasterResponse> joinMaster(IdsRequest<JoinMasterRequest> request) {
        return masterService.joinMaster(request.getData());
    }

    @RequestMapping("/getTotalServer")
    public IdsResponse<GetTotalServerResponse> getTotalServer(IdsRequest<GetTotalServerRequest> request) {
        return masterService.getTotalServer(request.getData());
    }
}
