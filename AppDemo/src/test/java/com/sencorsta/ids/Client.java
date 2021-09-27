package com.sencorsta.ids;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sencorsta.ids.core.application.master.request.PingMasterRequest;
import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.constant.ProtocolTypeConstant;
import com.sencorsta.ids.core.constant.SerializeTypeConstant;
import com.sencorsta.ids.core.net.handle.RpcClientChannelHandler;
import com.sencorsta.ids.core.net.innerClient.RpcClientBootstrap;
import com.sencorsta.ids.core.net.innerClient.RpcCodecFactory;
import com.sencorsta.ids.core.net.protocol.RpcMessage;
import com.sencorsta.ids.core.processor.MessageProcessor;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Client {

    public static void main(String[] args){
        GlobalConfig.instance();
        GlobalConfig.IS_DEBUG = false;
        RpcClientBootstrap bootstrap = new RpcClientBootstrap("appDemoClientTest", new RpcCodecFactory(new RpcClientChannelHandler()));
        Channel connect = bootstrap.connect("127.0.0.1", 10001);

        int total = 1;
        int count = 0;
        while (count < total) {
            RpcMessage message = new RpcMessage(ProtocolTypeConstant.TYPE_REQ);
            message.setMethod("/appDemo/helloWorld");
            message.setSerializeType(SerializeTypeConstant.TYPE_BYTEARR);
            message.setData("ice".getBytes());
            message.setChannel(connect);

            RpcMessage response = MessageProcessor.request(message);
            if (response != null) {
                if (response.getErrCode() > 0) {
                    log.info("response error:{}", response.getErrCode());
                } else {
                    log.info("response:{}", new String(response.getData()));
                }
            } else {
                log.info("response:{}", "null");
            }
            count++;
        }
    }
}
