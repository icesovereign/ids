package com.sencorsta.ids.core.net.coder;

import com.sencorsta.ids.core.config.GlobalConfig;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequestDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author daibin
 */
@Slf4j
public class IdsHttpDecoder extends HttpRequestDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        if (GlobalConfig.IS_DEBUG) {
            buffer.markReaderIndex();
            byte[] temp = new byte[buffer.readableBytes()];
            buffer.readBytes(temp);
            log.trace("收到HTTP数据:" + "总长度" + temp.length);
            log.trace("\n" + new String(temp));
            buffer.resetReaderIndex();
        }
        super.decode(ctx, buffer, out);
    }

}
