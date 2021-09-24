package com.sencorsta.ids.master;

import com.sencorsta.ids.core.application.Application;
import com.sencorsta.ids.core.application.master.request.TotalServerPush;
import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.constant.ProtocolTypeConstant;
import com.sencorsta.ids.core.constant.SerializeTypeConstant;
import com.sencorsta.ids.core.entity.Server;
import com.sencorsta.ids.core.net.protocol.RpcMessage;
import com.sencorsta.utils.string.StringUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author ICe
 */
@Slf4j
public class IdsMaster extends Application {
    public static IdsMaster init() {
        instance = new IdsMaster();
        return (IdsMaster) instance;
    }

    public static void addServer(Server server) {
        String serverType = server.getType();
        Map<String, Server> servers = instance().totalServers.get(serverType);
        if (servers == null) {
            servers = new ConcurrentHashMap<>();
            instance().totalServers.put(serverType, servers);
        }
        servers.put(server.getSid(), server);

        log.debug("master添加服务器成功!{}", server);
        showTotalServers();
        pushTotalServers();
    }

    public static synchronized String getSidByType(Server server) throws Exception {

        String serverType = server.getType();
        Map<String, Server> servers = instance().totalServers.get(serverType);
        if (servers == null) {
            servers = new ConcurrentHashMap<>();
        }

        if (!StringUtil.isEmpty(server.getSid())) {
            //先判断之前的id是否可以继续用
            boolean isUseBefore = false;
            for (Server serverTemp : servers.values()) {
                if (serverTemp.getSid().equals(server.getSid())) {
                    isUseBefore = true;
                    break;
                }
            }
            if (!isUseBefore) {
                return server.getSid();
            }
        }

        int index = 0;
        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            boolean isUse = false;
            for (Server serverTemp : servers.values()) {
                String num = String.format("%03d", i);
                String sidTemp = serverType + "-" + num;
                if (serverTemp.getSid().equals(sidTemp)) {
                    isUse = true;
                    break;
                }
            }
            if (!isUse) {
                index = i;
                break;
            }
        }
        if (index == 0) {
            throw new Exception("服务器列表已满!");
        }
        String num = String.format("%03d", index);
        return serverType + "-" + num;

    }

    @Override
    public void onServiceClose(Channel channel) {
        if (channel.attr(GlobalConfig.SERVER_KEY) != null) {
            final Server server = channel.attr(GlobalConfig.SERVER_KEY).get();
            if (server != null) {
                Map<String, Server> servers = instance().totalServers.get(server.getType());
                if (servers != null) {
                    log.info("{} 退出 master!!!", server);
                    servers.remove(server);
                    showTotalServers();
                    pushTotalServers();
                }
            }
        }
    }

    public static void showTotalServers() {
        StringBuffer status = new StringBuffer();
        status.append("\n");
        status.append("***************************当前已经连接的服务器***************************").append("\n");
        for (String keys : instance().totalServers.keySet()) {
            status.append("-------------------------------------------------------------").append("\n");
            status.append(keys + ":").append("\n");
            Map<String, Server> serversTemp = instance().totalServers.get(keys);
            for (Server serverTemp : serversTemp.values()) {
                status.append(serverTemp.toString()).append("\n");
            }
            status.append("-------------------------------------------------------------").append("\n");
        }
        status.append("**************************************************************************").append("\n");
        log.trace(status.toString());
        //打印当前所有激活服务器
        StringBuffer sb = new StringBuffer();
        sb.append("\n");
        sb.append("打印当前所有激活服务器:").append("\n");
        for (String keys : instance().totalServers.keySet()) {
            Map<String, Server> serversTemp = instance().totalServers.get(keys);
            for (Server server : serversTemp.values()) {
                if (server != null) {
                    Channel channelTemp = server.channel();
                    if (channelTemp != null && channelTemp.isActive()) {
                        sb.append(server.getSid()).append("\n");
                    }
                }
            }
        }
        log.trace(sb.toString());
    }

    public static void pushTotalServers() {
        for (String keys : instance().totalServers.keySet()) {
            Map<String, Server> serversTemp = IdsMaster.instance().totalServers.get(keys);
            for (Server serverTemp : serversTemp.values()) {
                TotalServerPush totalServerPush = new TotalServerPush();
                totalServerPush.setTotalServers(instance().totalServers);
                RpcMessage message = new RpcMessage(ProtocolTypeConstant.TYPE_RPC_PUSH);
                message.setMethod("/masterClient/totalServers");
                message.setSerializeType(SerializeTypeConstant.TYPE_JSON);
                message.setJsonData(totalServerPush);
                serverTemp.push(message);
            }
        }
    }


}
