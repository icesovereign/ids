package com.sencorsta.ids.core.entity;

import com.sencorsta.ids.core.net.protocol.RpcMessage;
import com.sencorsta.ids.core.processor.MessageProcessor;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 客户端对象
 *
 * @author ICe
 */
@Data
@Slf4j
public class Client {

    private String userId;
    private Channel channel;
    private long callCount;
    private Date LastCallTime;
    private boolean cleanIfOffline = true;
    private Set<String> Subscribes = new HashSet<>();
    private Set<String> serverTypes = new HashSet<>();

    public void sendMsg(RpcMessage msg) {
        msg.setChannel(channel);
        MessageProcessor.push(msg);
    }

    public void subscribe(String subscribeId, String type) {
        Subscribes.add(type + "." + subscribeId);
        serverTypes.add(type);
    }

    public void unSubscribe(String subscribeId, String type) {
        Subscribes.remove(type + "." + subscribeId);
        serverTypes.remove(type);
    }
}
