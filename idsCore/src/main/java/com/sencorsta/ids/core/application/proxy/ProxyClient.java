package com.sencorsta.ids.core.application.proxy;

import cn.hutool.core.thread.ThreadUtil;
import com.sencorsta.ids.core.application.Application;
import com.sencorsta.ids.core.entity.Server;
import com.sencorsta.ids.core.net.handle.RpcChannelHandler;
import com.sencorsta.ids.core.net.innerClient.RpcClientBootstrap;
import com.sencorsta.ids.core.net.innerClient.RpcCodecFactory;
import com.sencorsta.ids.core.net.protocol.RpcMessage;
import com.sencorsta.ids.core.processor.MessageProcessor;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全网服务器代理
 *
 * @author ICe
 */
@Slf4j
public class ProxyClient {

    static RpcClientBootstrap bootstrap = new RpcClientBootstrap("proxy", new RpcCodecFactory(new RpcChannelHandler()));

    // 维护链接
    public static synchronized void maintenanceList(Map<String, Map<String, Server>> data) {
        Map<String, Map<String, Server>> totalServers = Application.instance().getTotalServers();
        // 已经有的服务器增加标识
        for (Map<String, Server> map : totalServers.values()) {
            for (Server server : map.values()) {
                server.setClearFlag(true);
            }
        }

        log.trace("************************当前已经连接Master的服务器************************");
        for (String keys : data.keySet()) {
            log.trace("-------------------------------------------------------------");
            log.trace(keys + ":");
            for (Server serverTemp : data.get(keys).values()) {
                ProxyClient.connect(serverTemp);
            }
            log.trace("-------------------------------------------------------------");
        }
        log.trace("**************************************************************************");

        // 清理不存在的服务器
        for (Map<String, Server> map : totalServers.values()) {
            for (Server server : map.values()) {
                if (server.isClearFlag()) {
                    ProxyClient.disconnect(server);
                }
            }
        }

        //打印当前所有激活服务器
        StringBuilder sb = new StringBuilder();
        sb.append("打印当前所有激活服务器:");
        sb.append("\n");
        for (Map<String, Server> map : totalServers.values()) {
            for (Server server : map.values()) {
                if (server != null) {
                    Channel channelTemp = server.channel();
                    if (channelTemp != null && channelTemp.isActive()) {
                        sb.append(server.getSid());
                        sb.append("\n");
                    }
                }
            }
        }
        log.trace(sb.toString());
    }

    public static void connect(Server server) {
        Map<String, Map<String, Server>> totalServers = Application.instance().getTotalServers();
        Map<String, Server> serverList = totalServers.get(server.getType());
        if (serverList == null) {
            serverList = new ConcurrentHashMap<>();
            totalServers.put(server.getType(), serverList);
        }
        Server serverTemp = serverList.get(server.getSid());
        boolean needBind = false;
        if (serverTemp != null) {
            Channel channelTemp = serverTemp.channel();
            if (channelTemp != null && channelTemp.isActive()) {
                log.debug("已存在的服务,且通信不为空,跳过处理:{}", server.getSid());
            } else {
                needBind = true;
                log.debug("已存在的服务,但channel为空,重新绑定:{}", server.getSid());
            }
        } else {
            serverTemp = server;
            needBind = true;
            serverList.put(serverTemp.getSid(), serverTemp);
            log.debug("新的服务,绑定:{}", server.getSid());
        }

        if (needBind) {
            Channel channel = null;
            int count = 1, wait = 3000;
            while (channel == null) {
                //先连接外网IP
                channel = bootstrap.connect(server.getPublicHost(), server.getPort());
                if (channel == null) {
                    //尝试连接本地ip
                    log.debug("外网ip绑定失败，尝试连接本地ip...");
                    channel = bootstrap.connect(server.getHost(), server.getPort());
                    log.debug("绑定本地IP成功:{}", server.getHost());
                } else {
                    log.debug("绑定外网IP成功:{}", server.getPublicHost());
                }
                if (channel == null) {
                    ThreadUtil.sleep((long) wait * count);
                    log.warn(" reconnect {}", server);
                    if (count++ == 3) {
                        log.warn("reconnect {} over times quit!!!", server.getSid());
                        return;
                    }
                }
            }
            serverTemp.bind(channel);
        }

        serverTemp.setClearFlag(false);
        log.trace(serverTemp.toString());
    }

    public static boolean hasConnect(String type) {
        if (Application.instance().totalServers.containsKey(type)) {
            Map<String, Server> serverList = Application.instance().totalServers.get(type);
            if (serverList == null) {
                return false;
            }
            for (Server serverTemp : serverList.values()) {
                if (serverTemp != null) {
                    Channel channelTemp = serverTemp.channel();
                    if (channelTemp != null && channelTemp.isActive()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


//
//
//    public static void sendBySID(RpcMessage message, String type, String SID) {
//        ConcurrentHashMap<String, Server> serverMapTemp = totalServers.get(type);
//        if (serverMapTemp != null && serverMapTemp.size() > 0) {
//            log.debug("sendBySID开始:", type);
//            if (serverMapTemp.containsKey(SID)) {
//                Server bestServer = serverMapTemp.get(SID);
//                if (bestServer != null) {
//                    bestServer.channel().writeAndFlush(message);
//                    //message.channel=bestServer.channel();
//                    //sendMsg(message);
//                    return;
//                }
//            }
//            log.warn("sendBySID:失败 没有找到对应服务器:", SID);
//        } else {
//            log.warn("sendBySID:类型不存在:", type);
//        }
//    }
//
//    public static void broadcast(RpcMessage message, String type, String subscribeId) {
//        ConcurrentHashMap<String, Server> serverMapTemp = totalServers.get(type);
//        if (serverMapTemp != null && serverMapTemp.size() > 0) {
//            log.debug("broadcast开始:", type);
//
//            JSONObject obj = new JSONObject();
//            obj.put("subscribeId", SysConfig.getInstance().get("server.type") + "." + subscribeId);
//            obj.put("method", message.method);
//            obj.put("serializeType", message.serializeType);
//            obj.put("data", message.data);
//
//
//            for (Server server : serverMapTemp.values()) {
//                RpcMessage broadcast = new RpcMessage(TypeProtocol.TYPE_RPC_REQ);
//                broadcast.method = "Broadcast";
//                broadcast.data = obj.toJSONString().getBytes();
//                broadcast.serializeType = TypeSerialize.TYPE_JSON;
//                server.channel().writeAndFlush(broadcast);
////                message.channel=server.channel();
////                sendMsg(message);
//            }
//        } else {
//            log.warn("broadcast:类型不存在:", type);
//        }
//    }
//
//    public static void subscribe(String desType, String srcType, String subscribeId, String userId) {
//        ConcurrentHashMap<String, Server> serverMapTemp = totalServers.get(desType);
//        if (serverMapTemp != null && serverMapTemp.size() > 0) {
//            log.debug("subscribe开始:", desType);
//
//            JSONObject obj = new JSONObject();
//            obj.put("subscribeId", subscribeId);
//            obj.put("userId", userId);
//            obj.put("type", srcType);
//
//
//            for (Server server : serverMapTemp.values()) {
//                RpcMessage message = new RpcMessage(TypeProtocol.TYPE_RPC_REQ);
//                message.method = "Subscribe";
//                message.data = obj.toJSONString().getBytes();
//                message.serializeType = TypeSerialize.TYPE_JSON;
//                server.channel().writeAndFlush(message);
////                message.channel=server.channel();
////                sendMsg(message);
//            }
//        } else {
//            log.warn("subscribe:类型不存在:", desType);
//        }
//    }
//
//    public static void unSubscribe(String desType, String srcType, String subscribeId, String userId) {
//        ConcurrentHashMap<String, Server> serverMapTemp = totalServers.get(desType);
//        if (serverMapTemp != null && serverMapTemp.size() > 0) {
//            log.debug("UnSubscribe开始:", desType);
//
//            JSONObject obj = new JSONObject();
//            obj.put("subscribeId", subscribeId);
//            obj.put("userId", userId);
//            obj.put("type", srcType);
//
//            for (Server server : serverMapTemp.values()) {
//                RpcMessage message = new RpcMessage(TypeProtocol.TYPE_RPC_REQ);
//                message.method = "UnSubscribe";
//                message.data = obj.toJSONString().getBytes();
//                message.serializeType = TypeSerialize.TYPE_JSON;
//                server.channel().writeAndFlush(message);
////                message.channel=server.channel();
////                sendMsg(message);
//            }
//        } else {
//            log.warn("UnSubscribe:类型不存在:", desType);
//        }
//    }
//
//    public static void userLeave(String userId, Set<String> types) {
//        log.debug("userLeave开始:", userId);
//        JSONObject obj = new JSONObject();
//        obj.put("userId", userId);
//        for (String type : types) {
//            ConcurrentHashMap<String, Server> serverMapTemp = totalServers.get(type);
//            if (serverMapTemp != null && serverMapTemp.size() > 0) {
//                log.debug("userLeave开始:", userId);
//                for (Server server : serverMapTemp.values()) {
//                    RpcMessage broadcast = new RpcMessage(TypeProtocol.TYPE_RPC_REQ);
//                    broadcast.method = "UserLeave";
//                    broadcast.data = obj.toJSONString().getBytes();
//                    broadcast.serializeType = TypeSerialize.TYPE_JSON;
//                    server.channel().writeAndFlush(broadcast);
//                }
//            } else {
//                log.warn("userLeave:类型不存在:", type);
//            }
//        }
//    }

    public static void disconnect(Server server) {
        Map<String, Map<String, Server>> totalServers = Application.instance().getTotalServers();
        Map<String, Server> serverList = totalServers.get(server.getType());
        if (serverList.containsKey(server.getSid())) {
            serverList.remove(server.getSid());
            log.debug("清理服务:", server.getSid());
        }
    }

    public static Server getBestServerByType(String type) {
        Map<String, Map<String, Server>> totalServers = Application.instance().getTotalServers();
        Map<String, Server> serverList = totalServers.get(type);
        if (serverList == null) {
            return null;
        }
        Server bestServer = null;
        int bestFreeMemory = 0;
        for (String keyTemp : serverList.keySet()) {
            Server tempServer = serverList.get(keyTemp);
            long freeMemory = tempServer.getFreeMemory();
            if (freeMemory >= bestFreeMemory) {
                bestServer = tempServer;
            }
        }
        return bestServer;
    }


    /**
     * 同步远程调用
     *
     * @param req
     * @param type
     * @return
     */
    public static RpcMessage requestByTypeSync(RpcMessage req, String type) {
        Map<String, Map<String, Server>> totalServers = Application.instance().getTotalServers();
        Map<String, Server> serverMapTemp = totalServers.get(type);
        if (serverMapTemp != null && serverMapTemp.size() > 0) {
            log.debug("requestByType开始:{}", type);
            Server bestServer = getBestServerByType(type);
            if (bestServer != null) {
                req.setChannel(bestServer.channel());
                return MessageProcessor.request(req);
            } else {
                log.warn("requestByType:{} 服务器不存在", type);
            }
        } else {
            log.warn("requestByType:{} 类型不存在:", type);
        }
        return null;
    }

    /**
     * 异步远程调用
     *
     * @param message
     * @param type
     */
    public static void sendByTypeAsync(RpcMessage message, String type) {
        Map<String, Map<String, Server>> totalServers = Application.instance().getTotalServers();
        Map<String, Server> serverMapTemp = totalServers.get(type);
        if (serverMapTemp != null && serverMapTemp.size() > 0) {
            log.debug("sendByType开始:{}", type);
            Server bestServer = getBestServerByType(type);
            if (bestServer != null) {
                bestServer.channel().writeAndFlush(message);
            }
        } else {
            log.warn("sendByType:{} 类型不存在:", type);
        }
    }


//    public static void solo(String name) {
//        SysConfig config = SysConfig.getInstance();
//        Server localServer = new Server();
//        localServer.type = Application.SERVER_TYPE;
//
//        localServer.openHost = config.get("openServer.host", "0.0.0.0");
//        localServer.openPublicHost = config.get("openServer.host.public", localServer.openHost);
//        localServer.openPort = config.getInt("openServer.port", 0);
//        localServer.backHost = config.get("service.host", "0.0.0.0");
//        localServer.backPublicHost = config.get("service.host.public", localServer.backHost);
//        localServer.backPort = config.getInt("service.port", 0);
//        localserver.getSid() = name;
//        ProxyClient.connect(localServer);
//        log.info("没有检测到开启服务治理 自连。。。。");
//    }
}
