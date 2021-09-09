package com.sencorsta.ids.core.entity;

import lombok.Data;

/**
 * 响应基础类
 *
 * @author daibin
 */
@Data
public class IdsResponse<T> {
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

    public IdsResponse(T data) {
        this.code = 0;
        this.message = "ok";
        this.data = data;
    }

}
