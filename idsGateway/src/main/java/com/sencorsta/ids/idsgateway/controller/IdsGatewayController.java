package com.sencorsta.ids.idsgateway.controller;

import com.sencorsta.ids.core.entity.IdsRequest;
import com.sencorsta.ids.core.entity.IdsResponse;
import com.sencorsta.ids.core.entity.annotation.Autowired;
import com.sencorsta.ids.core.entity.annotation.Controller;
import com.sencorsta.ids.core.entity.annotation.RequestMapping;
import com.sencorsta.ids.idsgateway.service.IdsGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ICe
 */
@Controller
@RequestMapping("/idsGateway")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class IdsGatewayController {

    private final IdsGatewayService idsGatewayService;

    @RequestMapping("/helloWorld")
    public IdsResponse<String> helloWorld(IdsRequest<byte[]> request) {
        return new IdsResponse<>(idsGatewayService.helloWorld(new String(request.getData()),request.getChannel()));
    }

}
