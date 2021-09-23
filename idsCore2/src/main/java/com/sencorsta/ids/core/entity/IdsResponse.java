package com.sencorsta.ids.core.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 响应基础类
 *
 * @author ICe
 */
@Data
@NoArgsConstructor
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

    public IdsResponse(T data,ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg();
        this.data = data;
    }

//    public static IdsResponse<Object> error(ErrorCode errorCode) {
//        IdsResponse<Object> idsResponse = new IdsResponse<>();
//        idsResponse.code = errorCode.getCode();
//        idsResponse.message = errorCode.getMsg();
//        return idsResponse;
//    }
//
//    public static IdsResponse<Object> success(T data) {
//        IdsResponse<Object> idsResponse = new IdsResponse<>();
//        idsResponse.code = errorCode.getCode();
//        idsResponse.message = errorCode.getMsg();
//        return idsResponse;
//    }

}
