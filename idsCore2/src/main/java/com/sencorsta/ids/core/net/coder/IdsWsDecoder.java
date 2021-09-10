package com.sencorsta.ids.core.net.coder;

import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.net.protocol.Header;
import com.sencorsta.ids.core.net.protocol.RpcMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * 请求消息解码类
 *
 * @author ICe
 */
@Slf4j
public final class IdsWsDecoder extends MessageToMessageDecoder<BinaryWebSocketFrame> {
    private static int __RESPONSE_MAX_LEN = Integer.MAX_VALUE;

    @Override
    protected void decode(ChannelHandlerContext ctx, BinaryWebSocketFrame binaryWebSocketFrame, List<Object> list) throws Exception {
        ByteBuf in = binaryWebSocketFrame.content();
        if (in.readableBytes() < Header.SIZE) {
            return;
        }
        if (GlobalConfig.isDebug) {
            in.markReaderIndex();
            byte[] temp = new byte[in.readableBytes()];
            in.readBytes(temp);
            log.trace("收到数据:{}总长度:{}", Arrays.toString(temp), temp.length);
            in.resetReaderIndex();
        }
        in.readShort();
        in.readShort();
        int len = in.readInt();
        log.trace("长度:{}", len);
        if (len > __RESPONSE_MAX_LEN || len < 0) {
            Channel session = ctx.channel();
            log.warn("包体长度错误");
            session.close();
            return;
        }
        if (in.readableBytes() < len) {
            in.resetReaderIndex();
            return;
        }
        in.resetReaderIndex();
        RpcMessage msg = new RpcMessage();
        msg.decode(in);
        msg.setChannel(ctx.channel());
        list.add(msg);
    }


}
