package com.sencorsta.ids.core.net.coder;

import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.net.protocol.Header;
import com.sencorsta.ids.core.net.protocol.RpcMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * 请求消息解码类
 *
 * @author ICe
 */
@Slf4j
public final class IdsRpcDecoder extends ByteToMessageDecoder {

    private static int __RESPONSE_MAX_LEN = Integer.MAX_VALUE;

    public IdsRpcDecoder() {

    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> packets) throws Exception {
        if (in.readableBytes() < Header.SIZE) {
            return;
        }
        in.markReaderIndex();
        in.readShort();
        in.readShort();
        int len = in.readInt();
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
        if (GlobalConfig.isDebug) {
            byte[] temp = new byte[in.readableBytes()];
            in.readBytes(temp);
            log.trace("收到数据:{}总长度:{}", Arrays.toString(temp), temp.length);
            in.resetReaderIndex();
        }

        in.resetReaderIndex();
        RpcMessage msg = new RpcMessage();
        msg.decode(in);
        msg.setChannel(ctx.channel());
        packets.add(msg);
    }

}
