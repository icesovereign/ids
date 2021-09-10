package com.sencorsta.ids.core.net.coder;

import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.net.protocol.RpcMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * 发送消息编码类
 * @author ICe
 */
@Slf4j
public final class IdsWsEncoder extends MessageToMessageEncoder<RpcMessage> {
	@Override
	protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage msg, List<Object> list) throws Exception {
		BinaryWebSocketFrame frame=new BinaryWebSocketFrame();
		ByteBuf out=frame.content();
		msg.encode(out);

		if (GlobalConfig.IS_DEBUG) {
			out.markReaderIndex();
			byte[] temp = new byte[out.readableBytes()];
			out.readBytes(temp);
			log.trace("OpenServer发送数据:{} 总长度:{}", Arrays.toString(temp), temp.length);
			out.resetReaderIndex();
		}

		list.add(frame);
	}
}
