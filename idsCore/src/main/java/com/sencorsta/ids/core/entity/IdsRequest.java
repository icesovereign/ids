package com.sencorsta.ids.core.entity;

import io.netty.channel.Channel;
import lombok.Data;

/**
 * 请求基础类
 *
 * @author ICe
 */
@Data
public class IdsRequest<T> {
    /**
     * 数据
     * 具体类型看消息类型来定 一般使用JSON字符串
     */
    T data;

    /**
     * socket channel
     */
    Channel channel;

    public IdsRequest(T data) {
        this.data = data;
    }
}
