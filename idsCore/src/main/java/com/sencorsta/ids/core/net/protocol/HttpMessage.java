package com.sencorsta.ids.core.net.protocol;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.constant.SerializeTypeConstant;
import com.sencorsta.utils.object.Jsons;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * @author ICe
 * @description: RPC消息 只有服务器内部之间才能使用RpcMessage
 * @date 2019/6/12 17:19
 */
@Data
@Slf4j
public class HttpMessage {
    /**
     * 调用方法
     */
    private String method;
    private String token;
    private String userId;
    private Long msgId;
    private Integer errCode;
    private FullHttpRequest request;
    private ChannelHandlerContext channelHandlerContext;
    private Channel channel;

    public HttpMessage() {
        msgId = 0L;
        errCode = 0;
        token = "";
        userId = "";
        method = "";
    }

    /**
     * 是否已关闭
     */
    public boolean isChannelClosed() {
        return channel == null || !channel.isActive();
    }

    public String toStringPlus() {
        return getMethod();
    }

    @Override
    public String toString() {
        return getMethod();
    }

    public <T> T getAttr(AttributeKey<T> att) {
        return channel.attr(att).get();
    }

    public <T> void setAttr(AttributeKey<T> att, T value) {
        channel.attr(att).set(value);
    }

    public String getType() {
        String[] split = method.split("/");
        return split[1];
    }
}
