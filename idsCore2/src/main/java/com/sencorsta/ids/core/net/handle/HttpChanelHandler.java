package com.sencorsta.ids.core.net.handle;


import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;


/**
 * 　　* @description: Http服务器处理程序
 * 　　* @author ICe
 * 　　* @date 2019/6/12 17:12
 */
@Sharable
@Slf4j
public class HttpChanelHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        log.info("收到http消息!");
    }
}
