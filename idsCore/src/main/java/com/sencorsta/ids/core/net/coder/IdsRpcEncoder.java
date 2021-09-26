package com.sencorsta.ids.core.net.coder;

import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.net.protocol.RpcMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * 发送消息编码类
 *
 * @author ICe
 */
@Slf4j
public final class IdsRpcEncoder extends MessageToByteEncoder<RpcMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) throws Exception {
        msg.encode(out);
        if (GlobalConfig.IS_DEBUG) {
            out.resetReaderIndex();
            byte[] temp = new byte[out.readableBytes()];
            out.readBytes(temp);
            log.trace("Rpc发送数据:{} 总长度:{}", Arrays.toString(temp), temp.length);
            out.resetReaderIndex();
        }
        ctx.flush();
    }

}
