package com.sencorsta.ids.core.entity;

import cn.hutool.core.io.unit.DataSizeUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.net.protocol.RpcMessage;
import com.sencorsta.ids.core.processor.MessageProcessor;
import com.sencorsta.utils.string.StringUtil;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ICe
 */
@Data
@Slf4j
public class Server {

    String type;

    String sid;

    String host;

    String publicHost;

    int port;

    long freeMemory;

    long maxMemory;

    @JsonIgnore
    boolean clearFlag;

    @JsonIgnore
    private Channel channel;

    public void bind(Channel channel) {
        this.channel = channel;
        channel.attr(GlobalConfig.SERVER_KEY).set(this);
    }

    public Channel channel() {
        return this.channel;
    }

    public void push(RpcMessage message) {
        log.trace("开始推送:{}[{}:{}]", sid, host, port);
        MessageProcessor.push(message);
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append(sid).append(" --> ");

        if (channel == null) {
            buff.append(" #未连接#");
        } else {
            buff.append(" *已连接*");
        }
        buff.append(" 地址:").append(host).append("(").append(publicHost).append(")").append(":").append(port);
        buff.append(" 内存占用:").append(DataSizeUtil.format(maxMemory - freeMemory)).append("/").append(DataSizeUtil.format(maxMemory));
        return buff.toString();
    }

    @JsonIgnore
    public String getInfo() {
        return sid + " ping : " + host + ":" + port + " 内存占用:" + DataSizeUtil.format(maxMemory - freeMemory) + "/" + DataSizeUtil.format(maxMemory);
    }
}
