package com.sencorsta.ids.idsgateway.service;


import io.netty.channel.Channel;

/**
 * @author ICe
 */
public interface IdsGatewayService {
    /**
     * 测试方法
     *
     * @param s
     * @return
     */
    String helloWorld(String s,Channel channel);
}
