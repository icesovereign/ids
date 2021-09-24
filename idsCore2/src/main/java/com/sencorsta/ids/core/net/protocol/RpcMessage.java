package com.sencorsta.ids.core.net.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.constant.SerializeTypeConstant;
import com.sencorsta.utils.object.Jsons;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * @author ICe
 * @description: RPC消息
 * @date 2019/6/12 17:19
 */
@Data
@Slf4j
public class RpcMessage extends BaseMessage {
    /**
     * 序列化类型
     */
    private short serializeType;
    /**
     * 调用方法
     */
    private String method;
    private String token;
    private Long msgId;
    private Integer errCode;
    /**
     * 数据
     */
    private byte[] data;

    private Channel channel;

    public RpcMessage(short typeProtocol) {
        this();
        header.type = typeProtocol;
    }

    public RpcMessage() {
        data = new byte[0];
        msgId = 0L;
        errCode = 0;
        token = "";
        method = "";
    }

    public void setJsonData(Object o) {
        try {
            data = Jsons.mapper.writeValueAsBytes(o);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 是否已关闭
     */
    public boolean isChannelClosed() {
        return channel == null || !channel.isActive();
    }

    @Override
    public void encodeBody() {
        body = new Body(10);
        body.content.writeShort(serializeType);
        body.writeString(method);
        body.writeString(token);
        body.content.writeLong(msgId);
        body.content.writeInt(errCode);
        body.content.writeBytes(data);
        header.length = body.content.readableBytes();
    }

    @Override
    public void decodeBody() {
        serializeType = body.content.readShort();
        method = body.readString();
        token = body.readString();
        msgId = body.content.readLong();
        errCode = body.content.readInt();
        data = new byte[body.content.readableBytes()];
        body.content.readBytes(data);
    }

    public String toStringPlus() {
        switch (serializeType) {
            case SerializeTypeConstant.TYPE_JSON:
            case SerializeTypeConstant.TYPE_STRING:
                return "header:[" + header + "]" + "  body:[" + "errCode:" + errCode + " serializeType:" + serializeType + " method:" + method + " data:" + new String(data, GlobalConfig.UTF_8) + "]";
            case SerializeTypeConstant.TYPE_BYTEARR:
            case SerializeTypeConstant.TYPE_PROTOBUF:
                return "header:[" + header + "]" + "  body:[" + "errCode:" + errCode + " serializeType:" + serializeType + " method:" + method + " data:" + Arrays.toString(data) + "]";
            default:
                return "header:[" + header + "]" + "  body:[" + "errCode:" + errCode + " serializeType:" + serializeType + " method:" + method + " data:" + "🐷未知协议🐷" + "]";
        }
    }

    @Override
    public String toString() {
        switch (serializeType) {
            case SerializeTypeConstant.TYPE_JSON:
                return "{[" + header + "]" + "[TYPE_JSON]" + method + "}";
            case SerializeTypeConstant.TYPE_STRING:
                return "{[" + header + "]" + "[TYPE_STRING]" + method + "}";
            case SerializeTypeConstant.TYPE_BYTEARR:
                return "{[" + header + "]" + "[TYPE_BYTEARR]" + method + "}";
            case SerializeTypeConstant.TYPE_PROTOBUF:
                return "{[" + header + "]" + "[TYPE_PROTOBUF]" + method + "}";
            default:
                break;
        }
        return "{[" + header + "]" + "[未知协议🐷]" + method + "}";
    }

    public <T> T getAttr(AttributeKey<T> att) {
        return channel.attr(att).get();
    }

    public <T> void setAttr(AttributeKey<T> att, T value) {
        channel.attr(att).set(value);
    }


}
