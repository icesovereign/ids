package com.sencorsta.ids.core.net.innerClient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.ThreadPerTaskExecutor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcClientBootstrap {
    // 存放NIO客户端实例对象
    private static Map<String, RpcClientBootstrap> clientMap = new HashMap<String, RpcClientBootstrap>();

    private String serverIp;
    private int serverPort;
    private Bootstrap bootstrap;
    private EventLoopGroup group;

    public RpcClientBootstrap(String name, ChannelInitializer<SocketChannel> factory) {
        group = new NioEventLoopGroup(0, new ThreadPerTaskExecutor(new DefaultThreadFactory(name + "Send")));
        bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.handler(factory);
        bootstrap.group(group);
    }

    public Channel connect(String host, int port) {
        try {
            this.serverIp = host;
            this.serverPort = port;
            ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
            future.awaitUninterruptibly(3, TimeUnit.SECONDS);
            if (!future.isSuccess()) {
                if (future.cause() != null) {
                    log.trace("连接服务器出错:{}", future.cause().getMessage());
                }
                return null;
            }
            return future.channel();
        } catch (Exception e) {
            log.error("连接服务器出错" + "host:" + host + " port:" + port + " -> " + e.getMessage(), e);
            return null;
        }
    }

    public void close(Channel channel) throws InterruptedException {
        log.trace("开始关闭链接");
        channel.closeFuture().sync();
        group.shutdownGracefully();
        clientMap.remove(serverIp + ":" + serverPort);
        log.trace("关闭链接");
    }

}
