package com.sencorsta.ids.core.net.handle;

import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.net.coder.*;
import com.sencorsta.ids.core.net.handle.RpcChannelHandler;
import com.sencorsta.ids.core.net.innerServer.SSLHandlerProvider;
import com.sencorsta.ids.core.net.protocol.Header;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.util.List;

/**
 * 协议选择器
 * 判断进来的协议是什么 使用不同的解码器处理
 *
 * @author ICe
 */
public class ProtocolChooseHandle extends ByteToMessageDecoder {
    /**
     * 默认暗号长度为23
     */
    private static final int MAX_LENGTH = 23;
    /**
     * WebSocket握手的协议前缀
     */
    private static final String WEBSOCKET_PREFIX = "GET /ws";

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (isRpc(in)) {
            socketAdd(ctx);
        } else {
            //不是rpc就直接走HTTP
            String protocol = getBufStart(in);
            //判断是否是websocket
            if (protocol.startsWith(WEBSOCKET_PREFIX)) {
                websocketAdd(ctx);
            } else {
                //普通http消息
                httpAdd(ctx);
            }
        }
        //判断完毕后删除这个handle 后面的流量直接走对应的处理器
        ctx.pipeline().remove(this.getClass());
    }

    private boolean isRpc(ByteBuf in) {
        int length = in.readableBytes();
        if (length > Header.SIZE) {
            length = Header.SIZE;
        }
        // 标记读位置
        in.markReaderIndex();
        short i = in.readShort();
        in.resetReaderIndex();
        if (i == GlobalConfig.SIGNATURE) {
            return true;
        } else {
            return false;
        }
    }


    private String getBufStart(ByteBuf in) {
        int length = in.readableBytes();
        if (length > MAX_LENGTH) {
            length = MAX_LENGTH;
        }
        // 标记读位置
        in.markReaderIndex();
        byte[] content = new byte[length];
        in.readBytes(content);
        in.resetReaderIndex();
        return new String(content);
    }


    public static void websocketAdd(ChannelHandlerContext ctx) {
        ctx.pipeline().addLast("ssl", SSLHandlerProvider.getOptionalSslHandler());
        // HttpServerCodec：将请求和应答消息解码为HTTP消息
        ctx.pipeline().addLast("http-codec", new HttpServerCodec());
        // HttpObjectAggregator：将HTTP消息的多个部分合成一条完整的HTTP消息
        ctx.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65535));
        // ChunkedWriteHandler：向客户端发送HTML5文件,文件过大会将内存撑爆
        ctx.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
        ctx.pipeline().addLast("WebSocketAggregator", new WebSocketFrameAggregator(65535));
        //用于处理websocket, /ws为访问websocket时的uri
        ctx.pipeline().addLast("ProtocolHandler", new WebSocketServerProtocolHandler("/ws", true));
        ctx.pipeline().addLast("deCode", new IdsWsDecoder());
        ctx.pipeline().addLast("enCode", new IdsWsEncoder());
        ctx.pipeline().addLast("handler", new RpcChannelHandler());
    }

    public static void httpAdd(ChannelHandlerContext ctx) {
        ctx.pipeline().addLast("ssl", SSLHandlerProvider.getOptionalSslHandler());
        ctx.pipeline().addLast("http-decoder", new IdsHttpDecoder());
        ctx.pipeline().addLast("http-encoder", new HttpResponseEncoder());
        ctx.pipeline().addLast("http-aggregator", new HttpObjectAggregator(1024 * 1024 * 1024));
        ctx.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
        ctx.pipeline().addLast("handler", new HttpChanelHandler());
    }

    public static void socketAdd(ChannelHandlerContext ctx) {
        ctx.pipeline().addLast("decoder", new IdsRpcDecoder());
        ctx.pipeline().addLast("encoder", new IdsRpcEncoder());
        ctx.pipeline().addLast("handler", new RpcChannelHandler());
    }
}
