package com.sencorsta.ids.core.net.handle;

import com.sencorsta.ids.core.net.protocol.RpcMessage;
import com.sencorsta.ids.core.processor.MessageProcessor;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 回调服务器的反馈信息
 *
 * @author ICe
 */
@Sharable
@Slf4j
@NoArgsConstructor
public class RpcClientChannelHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("Client异常:" + ctx.channel() + cause.getMessage());
        ctx.channel().close();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.trace("Client添加:" + ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.trace("Client移除:" + ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
        RpcMessage message = (RpcMessage) packet;
        log.trace("Client收到消息:" + ctx.channel() + message.toStringPlus());
        MessageProcessor.incomeMessage(message);
    }

}