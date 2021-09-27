package com.sencorsta.ids.appdemo.service;


import io.netty.channel.Channel;

/**
 * @author ICe
 */
public interface AppDemoService {
    /**
     * 测试方法
     *
     * @param s
     * @return
     */
    String helloWorld(String s,Channel channel);
}
