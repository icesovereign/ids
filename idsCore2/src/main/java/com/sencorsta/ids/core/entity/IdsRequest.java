package com.sencorsta.ids.core.entity;

import lombok.Data;

/**
 * 请求基础类
 *
 * @author daibin
 */
@Data
public class IdsRequest<T> {
    /**
     * 响应码
     */
    int code;
    /**
     * 响应消息
     */
    String message;
    /**
     * 数据
     * 具体类型看消息类型来定 一般使用JSON字符串
     */
    T data;

    public T getDate() {
        return null;
    }
}
