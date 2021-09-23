package com.sencorsta.ids.core.net.protocol;

import com.sencorsta.ids.core.constant.ProtocolTypeConstant;
import com.sencorsta.ids.core.constant.SerializeTypeConstant;

/**
 * 定义一些常用的方法
 *
 * @author ICe
 */
public class MessageFactor {
    public static RpcMessage newResMessage() {
        RpcMessage message = new RpcMessage(ProtocolTypeConstant.TYPE_RPC_RES);
        message.setSerializeType(SerializeTypeConstant.TYPE_JSON);
        return message;
    }
}
