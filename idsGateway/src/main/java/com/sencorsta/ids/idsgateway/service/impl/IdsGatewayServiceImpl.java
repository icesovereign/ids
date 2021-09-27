package com.sencorsta.ids.idsgateway.service.impl;


import com.sencorsta.ids.core.entity.annotation.Autowired;
import com.sencorsta.ids.core.entity.annotation.Service;
import com.sencorsta.ids.idsgateway.dao.IdsGatewayDao;
import com.sencorsta.ids.idsgateway.service.IdsGatewayService;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ICe
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class IdsGatewayServiceImpl implements IdsGatewayService {

    private final IdsGatewayDao idsGatewayDao;

    @Override
    public String helloWorld(String s,Channel channel) {
        String name = idsGatewayDao.getName(s);
        log.info("你好啊!!!" + name);
        return name;
    }
}
