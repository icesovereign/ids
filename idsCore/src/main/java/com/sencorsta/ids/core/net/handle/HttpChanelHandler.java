package com.sencorsta.ids.core.net.handle;


import com.sencorsta.ids.core.net.protocol.HttpMessage;
import com.sencorsta.ids.core.processor.MessageProcessor;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;


/**
 * 　　* @description: Http服务器处理程序
 * 　　* @author ICe
 * 　　* @date 2019/6/12 17:12
 */
@Sharable
@Slf4j
public class HttpChanelHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest request) throws Exception {
        log.info("收到http消息!");
        URI uri = new URI(request.uri());
        request.content().retain();
        String path = uri.getPath();
        HttpMessage httpMessage = new HttpMessage();
        httpMessage.setChannelHandlerContext(channelHandlerContext);
        httpMessage.setRequest(request);
        httpMessage.setMethod(path);
        httpMessage.setChannel(channelHandlerContext.channel());
        MessageProcessor.incomeHttpMessage(httpMessage);
    }
}
