package com.sencorsta.ids.core.net.handle;

import com.sencorsta.ids.core.application.Application;
import com.sencorsta.ids.core.net.protocol.RpcMessage;
import com.sencorsta.ids.core.processor.MessageProcessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * IO处理器，所有接收到的消息put到队列里，等待处理器分发处理
 *
 * @author ICe
 */
@Slf4j
@ChannelHandler.Sharable
public final class RpcChannelHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.trace("RpcServer添加:" + ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("RpcServer异常:" + ctx.channel() + " " + cause.getMessage());
        //cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        //Out.info("handlerRemoved : ", channel);
        //GGame.getInstance().onServiceClose(channel);
        log.trace("RpcServer移除:" + ctx.channel());
        Application.instance().onServiceClose(channel);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
        RpcMessage msg = (RpcMessage) packet;
        //log.trace("RpcServer新消息: " + msg);
        msg.setChannel(ctx.channel());
        MessageProcessor.incomeMessage(msg);
    }

}
