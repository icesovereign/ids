package com.sencorsta.ids;

import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.constant.ProtocolTypeConstant;
import com.sencorsta.ids.core.constant.SerializeTypeConstant;
import com.sencorsta.ids.core.net.handle.RpcClientChannelHandler;
import com.sencorsta.ids.core.net.innerClient.RpcClientBootstrap;
import com.sencorsta.ids.core.net.innerClient.RpcCodecFactory;
import com.sencorsta.ids.core.net.protocol.RpcMessage;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Client {
    public static void main(String[] args) throws InterruptedException {
        GlobalConfig.instance();
        GlobalConfig.isDebug = false;
        RpcClientBootstrap bootstrap = new RpcClientBootstrap("test", new RpcCodecFactory(new RpcClientChannelHandler()));
        Channel connect = bootstrap.connect("127.0.0.1", 10002);
        int total = 1;
        int count = 0;
        while (count < total) {
            RpcMessage message = new RpcMessage(ProtocolTypeConstant.TYPE_RPC_REQ);
            message.method = "/master/helloWorld";
            message.serializeType = SerializeTypeConstant.TYPE_STRING;
            message.data = "hello world".getBytes();
            connect.writeAndFlush(message);
            count++;
            //Thread.sleep(1);
        }
    }
}
