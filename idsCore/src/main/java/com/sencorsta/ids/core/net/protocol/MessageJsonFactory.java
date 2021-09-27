package com.sencorsta.ids.core.net.protocol;

import com.sencorsta.ids.core.constant.ProtocolTypeConstant;
import com.sencorsta.ids.core.constant.SerializeTypeConstant;

/**
 * 定义一些常用的方法
 *
 * @author ICe
 */
public class MessageJsonFactory {
    public static RpcMessage newResMessage(short type) {
        short typeRpcRes;
        if (type == ProtocolTypeConstant.TYPE_REQ) {
            typeRpcRes = ProtocolTypeConstant.TYPE_RES;
        } else if (type == ProtocolTypeConstant.TYPE_PROXY_REQ) {
            typeRpcRes = ProtocolTypeConstant.TYPE_PROXY_RES;
        } else {
            typeRpcRes = ProtocolTypeConstant.TYPE_RPC_RES;
        }
        RpcMessage message = new RpcMessage(typeRpcRes);
        message.setSerializeType(SerializeTypeConstant.TYPE_JSON);
        return message;
    }

    public static RpcMessage newPushMessage() {
        RpcMessage message = new RpcMessage(ProtocolTypeConstant.TYPE_PUSH);
        message.setSerializeType(SerializeTypeConstant.TYPE_JSON);
        return message;
    }
}
