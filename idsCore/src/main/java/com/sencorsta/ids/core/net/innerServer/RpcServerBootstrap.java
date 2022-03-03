package com.sencorsta.ids.core.net.innerServer;

import cn.hutool.system.SystemUtil;
import com.sencorsta.ids.core.config.ConfigGroup;
import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.net.handle.ProtocolChooseHandle;
import com.sencorsta.ids.core.processor.IdsThreadFactory;
import com.sencorsta.utils.string.StringUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.ThreadPerTaskExecutor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 服务端网络服务引导
 *
 * @author ICe
 */
@Slf4j
public class RpcServerBootstrap {

    private final String name;
    private final ChannelInitializer<SocketChannel> factory;

    private ServerBootstrap serverBootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    protected String hostStr = "host";
    protected String hostPublicStr = "host.public";
    protected String minPortStr = "port.min";
    protected String maxPortStr = "port.max";
    protected String portStr = "port";

    public RpcServerBootstrap(String name) {
        this.name = name;
        this.factory = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("protocolChooseHandle", new ProtocolChooseHandle());
            }
        };
    }


    public RpcServerBootstrap(String name, ChannelInitializer<SocketChannel> factory) {
        this.name = name;
        this.factory = factory;
    }

    boolean isEnable = false;

    /**
     * 调用此方法绑定端口侦听服务，添加编解码过滤器
     *
     * @throws Exception
     */
    public void start() throws Exception {
        try {
            serverBootstrap = new ServerBootstrap();

            ThreadPerTaskExecutor bossExecutor = new ThreadPerTaskExecutor(new IdsThreadFactory(name + "NetBoss"));
            ThreadPerTaskExecutor workerExecutor = new ThreadPerTaskExecutor(new IdsThreadFactory(name + "NetWorker"));
            if (SystemUtil.getOsInfo().isLinux() && Epoll.isAvailable()) {
                //第一个参数为0 则代表默认值:核心数*2
                bossGroup = new EpollEventLoopGroup(0, bossExecutor);
                workerGroup = new EpollEventLoopGroup(0, workerExecutor);
                serverBootstrap.channel(EpollServerSocketChannel.class);
            } else {
                bossGroup = new NioEventLoopGroup(0, bossExecutor);
                workerGroup = new NioEventLoopGroup(0, workerExecutor);
                serverBootstrap.channel(NioServerSocketChannel.class);
            }
            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.childHandler(factory);
            serverBootstrap
                    //服务端处理客户端连接请求是顺序处理的，所以同一时间只能处理一个客户端连接，多个客户端来的时候，服务端将不能处理的客户端连接请求放在队列中等待处理，backlog参数指定了队列的大小
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    //这个参数表示允许重复使用本地地址和端口
                    .option(ChannelOption.SO_REUSEADDR, true)
                    //TCP接收缓冲区的大小(不能设置太小 否则有性能问题 设置太大则浪费内存)
                    .option(ChannelOption.SO_RCVBUF, 128 * 1024)
                    //超时时间
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                    //ByteBuf的分配器(重用缓冲区)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    //自适应的接受缓冲区分配器
                    .childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(64, 2048, 65536))
                    //禁止Nagle算法
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    //发送周期性特定报文以维持连接
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            //加载ssl证书 用于https服务
            SSLHandlerProvider.initSSLContext2();

            isEnable = false;
            String ip = GlobalConfig.instance().getStr(hostStr, ConfigGroup.server.getName(), "0.0.0.0");
            int serviceMin = GlobalConfig.instance().getInt(minPortStr, ConfigGroup.server.getName());
            int serviceMax = GlobalConfig.instance().getInt(maxPortStr, ConfigGroup.server.getName());
            for (int j = serviceMin; j <= serviceMax; j++) {
                ChannelFuture f = null;
                try {
                    InetSocketAddress socketAddress = (ip != null && ip.length() > 6) ? new InetSocketAddress(ip, j)
                            : new InetSocketAddress(j);
                    f = serverBootstrap.bind(socketAddress).sync();
                    if (f.isSuccess()) {
                        log.trace(name + "端口可用:{}{}", " -> ", j);
                        String realHost = GlobalConfig.instance().getStr(hostPublicStr, ConfigGroup.server.getName(), "0.0.0.0");
                        if (StringUtil.isEmpty(GlobalConfig.instance().getStr(hostPublicStr, ConfigGroup.server.getName(), null))) {
                            log.info(name + "服务绑定于 -> " + socketAddress);
                        } else {
                            realHost += ":" + j;
                            log.info(name + "服务绑定于 -> " + socketAddress + " (公开地址:" + realHost + ")");
                            log.info(name + "HTTP服务地址 -> https://{}:{}/", socketAddress.getHostString(), j);
                        }
                        GlobalConfig.instance().put(ConfigGroup.server.getName(), portStr, j + "");
                        isEnable = true;
                        break;
                    }
                } catch (Exception e) {
                    log.trace("端口{}已被占用,查找新的[{}-{}]端口中...", j, serviceMin, serviceMax);
                }
            }
            if (!isEnable) {
                throw new Exception(name + "服务器没有找到可用内部端口!,请检查配置是否正确! ->" + ip + "[" + serviceMin + "-" + serviceMax + "]");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public boolean isStarted() {
        return isEnable;
    }


}
