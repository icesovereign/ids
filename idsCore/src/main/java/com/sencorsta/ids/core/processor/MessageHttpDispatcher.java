package com.sencorsta.ids.core.processor;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sencorsta.ids.core.config.ConfigGroup;
import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.ids.core.entity.*;
import com.sencorsta.ids.core.net.handle.ContentTypes;
import com.sencorsta.ids.core.net.handle.HttpContextHelper;
import com.sencorsta.ids.core.net.protocol.HttpMessage;
import com.sencorsta.utils.object.Jsons;
import com.sencorsta.utils.string.StringUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.rtsp.RtspHeaderValues;
import io.netty.handler.stream.ChunkedFile;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * 消息分发器
 *
 * @author ICe
 */
@Slf4j
@AllArgsConstructor
public class MessageHttpDispatcher implements Runnable {
    private final HttpMessage message;

    @Override
    public void run() {
        log.info("开始处理HTTP消息: {}", message.toStringPlus());
        if (GlobalConfig.IS_DEBUG) {
            long sTime = System.currentTimeMillis();
            String method = message.getMethod();
            ScheduledFuture<?> schedule = MessageProcessor.MONITOR.schedule(() -> {
                log.warn("消息处理超时:{}", method);
            }, 10, TimeUnit.SECONDS);
            execute(message);
            schedule.cancel(true);
            if (System.currentTimeMillis() - sTime > 100) {
                log.warn("消息处理时间过高{} -> {}", method, System.currentTimeMillis() - sTime);
            }
        } else {
            execute(message);
        }
    }

    private void execute(HttpMessage message) {
        HttpMethod method = message.getRequest().method();
        switch (method.name()) {
            // 服务器之间的请求 理论上必须能找到处理器 找不到就返回错误
            case "POST":
            case "GET":
                handleHttp(message);
                break;
            // 推送一般直接触发对应的逻辑就行了
            case "OPTIONS":
                handleHttpOptions(message);
                break;
            default:
                log.warn("不支持协议类型：{}", method);
                break;
        }
        message.getChannel().close();
    }

    private void handleHttpOptions(HttpMessage message) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
        response.headers().set("Access-Control-Allow-Origin", "*");
        response.headers().set("Access-Control-Allow-Credentials", "true");
        response.headers().set("Access-Control-Allow-Methods", "*");
        response.headers().set("Access-Control-Allow-Headers", "Content-Type,Access-Token");
        response.headers().set("Access-Control-Expose-Headers", "*");
        Channel ctx = message.getChannel();
        ctx.write(response);
        ctx.flush();
        ctx.close();
    }

    private void handleHttp(HttpMessage message) {
        //先尝试本地是否有此方法
        MethodProxy methodProxy = MessageProcessor.getMETHOD_MAP().get(message.getMethod());
        if (methodProxy != null) {
            handleHttpReq(message);
        } else {
            //没有处理器尝试检查本地文件
            if (GlobalConfig.instance().getBool("httpServer.openStaticWeb", ConfigGroup.server.getName(), false)) {
                handleStaticFile(message);
            } else {
                Channel ctx = message.getChannel();
                FullHttpRequest request = message.getRequest();
                InetSocketAddress inetSocket = (InetSocketAddress) ctx.remoteAddress();
                String clientIP = inetSocket.getAddress().getHostAddress();
                log.warn("没有找到处理句柄:{}  -> {} IP:{}", request.method(), request.uri(), clientIP);
            }
        }
    }

    private void handleStaticFile(HttpMessage message) {
        try {
            FullHttpRequest request = message.getRequest();
            ChannelHandlerContext ctx = message.getChannelHandlerContext();
            String path = message.getMethod();
            URI uri = new URI(request.uri());
            log.trace("没有handler 尝试找本地文件 url：{}", uri);
            // 根据路径地址构建文件
            String basePath = System.getProperty("user.dir") + GlobalConfig.instance().getStr("httpServer.pathStaticWeb", ConfigGroup.server.getName(), "/web");
            String pathFile = basePath + URLDecoder.decode(uri.getPath(), "UTF-8");
            // 构建404页面
            String path404 = basePath + "/404.html";
            File html = new File(pathFile);

            // 状态为1xx的话，继续请求
            if (HttpUtil.is100ContinueExpected(request)) {
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
                ctx.writeAndFlush(response);
            }
            //如果是目录就加默认index.html
            if (html.isDirectory()) {
                html = new File(pathFile + "/index.html");
            }
            // 当文件不存在的时候，将资源指向NOT_FOUND
            boolean isNotFound = false;
            if (!html.exists()) {
                html = new File(path404);
                if (!html.exists()) {
                    InetSocketAddress inSocket = (InetSocketAddress) ctx.channel().remoteAddress();
                    String clientIp = inSocket.getAddress().getHostAddress();
                    log.warn("没有找到页面:{} : {} IP:{}", path404, request.uri(), clientIp);
                    ctx.close();
                    return;
                } else {
                    isNotFound = true;
                }
            }

            RandomAccessFile file = new RandomAccessFile(html, "r");
            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.OK);

            // 文件没有发现设置状态为404
            if (isNotFound) {
                response.setStatus(HttpResponseStatus.NOT_FOUND);
            }
            log.trace("找到本地文件 url:{}", uri);
            // 设置文件格式内容
            if (path.endsWith(".html")) {
                response.headers().set("Content-Type", "text/html; charset=UTF-8");
            } else if (path.endsWith(".js")) {
                response.headers().set("Content-Type", "application/x-javascript");
            } else if (path.endsWith(".css")) {
                response.headers().set("Content-Type", "text/css; charset=UTF-8");
            }
            boolean keepAlive = HttpUtil.isKeepAlive(request);
            if (keepAlive) {
                response.headers().set("Content-Length", file.length());
                response.headers().set("Connection", "keep-alive");
            }
            ctx.write(response);
            ChannelFuture sendFileFuture;
            //通过Netty的ChunkedFile对象直接将文件写入到发送缓冲区中
            sendFileFuture = ctx.write(new ChunkedFile(file, 0, file.length(), 8192), ctx.newProgressivePromise());
            //为sendFileFuture添加监听器，如果发送完成打印发送完成的日志
            sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
                @Override
                public void operationProgressed(ChannelProgressiveFuture future, long progress, long total)
                        throws Exception {
                    if (total < 0) {
                        log.trace(uri + " 传输中: " + progress);
                    } else {
                        log.trace(uri + " 传输中: " + progress + "/" + total);
                    }
                }

                @Override
                public void operationComplete(ChannelProgressiveFuture future)
                        throws Exception {
                    log.debug(uri + " 传输完成！");
                    file.close();
                }
            });
            //如果使用chunked编码，最后需要发送一个编码结束的空消息体，将LastHttpContent.EMPTY_LAST_CONTENT发送到缓冲区中，
            //来标示所有的消息体已经发送完成，同时调用flush方法将发送缓冲区中的消息刷新到SocketChannel中发送
            ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            //如果是非keepAlive的，最后一包消息发送完成后，服务端要主动断开连接
            if (!keepAlive) {
                lastContentFuture.addListener(ChannelFutureListener.CLOSE);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void handleHttpReq(HttpMessage message) {
        MethodProxy methodProxy = MessageProcessor.getMETHOD_MAP().get(message.getMethod());
        if (ObjectUtil.isNotNull(methodProxy)) {
            try {
                Object result = invoke(message, methodProxy);
                String contentType = ContentTypes.JSON;
                if (result instanceof HttpFileResponse) {
                    HttpFileResponse<?> response = ((HttpFileResponse<?>) result);
                    contentType = response.getContentType();
                    String filename = response.getFileName();
                    String filePath = response.getFilePath();
                    responseFile(message, filePath, contentType, filename);
                } else {
                    byte[] bytes = Jsons.mapper.writeValueAsBytes(result);
                    response(message, bytes, contentType);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } else {
            log.debug("没有找到对应的处理方法:{}", message.getMethod());
        }

    }

    private void responseFile(HttpMessage message, String filePath, String contentType, String fileName) throws Exception {
        FullHttpRequest req = message.getRequest();
        Channel ctx = message.getChannel();
        //文件下传
        File file = new File(filePath);
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        long fileLength = randomAccessFile.length();
        log.debug("file:{}", file.getAbsolutePath());
        log.debug("fileLength:{}", fileLength);

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        response.headers().set("Content-Length", fileLength);

        if (contentType.equals(ContentTypes.Auto)) {
            MimetypesFileTypeMap mimetypesTypeMap = new MimetypesFileTypeMap();
            response.headers().set(CONTENT_TYPE, mimetypesTypeMap.getContentType(file.getPath()));
        } else {
            response.headers().set("Content-Type", contentType);
            String userAgent = req.headers().get("User-Agent");
            String encoderFilename;
            if (userAgent.toUpperCase().contains("MSIE") || userAgent.contains("Trident/7.0")) {
                encoderFilename = URLEncoder.encode(fileName, "UTF-8");
            } else if (userAgent.toUpperCase().contains("MOZILLA") || userAgent.toUpperCase().contains("CHROME")) {
                encoderFilename = new String(fileName.getBytes(), StandardCharsets.ISO_8859_1);
            } else {
                encoderFilename = URLEncoder.encode(fileName, "UTF-8");
            }
            log.debug("encoderFilename:{}", encoderFilename);
            response.headers().set("content-disposition", "attachment;filename=" + encoderFilename);
        }

        response.headers().set("Access-Control-Allow-Origin", "*");
        response.headers().set("Charset", "UTF-8");
        if (HttpUtil.isKeepAlive(req)) {
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.write(response);
        ChannelFuture sendFileFuture;
        //通过Netty的ChunkedFile对象直接将文件写入到发送缓冲区中
        sendFileFuture = ctx.write(new ChunkedFile(randomAccessFile, 0, fileLength, 8192), ctx.newProgressivePromise());
        //为sendFileFuture添加监听器，如果发送完成打印发送完成的日志
        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                if (total < 0) {
                    log.trace(fileName + " 传输中: " + progress);
                } else {
                    log.trace(fileName + " 传输中: " + progress + "/" + total);
                }
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future) {
                log.debug(fileName + " 传输完成！");
            }
        });
        //如果使用chunked编码，最后需要发送一个编码结束的空消息体，将LastHttpContent.EMPTY_LAST_CONTENT发送到缓冲区中，
        //来标示所有的消息体已经发送完成，同时调用flush方法将发送缓冲区中的消息刷新到SocketChannel中发送
        ChannelFuture lastContentFuture = ctx.
                writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        //如果是非keepAlive的，最后一包消息发送完成后，服务端要主动断开连接
        if (!HttpUtil.isKeepAlive(req)) {
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void response(HttpMessage message, byte[] bytes, String contentType) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(bytes));
        if (StringUtil.isEmpty(contentType)) {
            response.headers().set("Content-Type", ContentTypes.TEXT_PLAIN);
        } else {
            response.headers().set("Content-Type", contentType);
        }
        response.headers().set("Content-Length", response.content().readableBytes());
        response.headers().set("Access-Control-Allow-Origin", "*");
        if (HttpUtil.isKeepAlive(message.getRequest())) {
            response.headers().set("Connection", RtspHeaderValues.KEEP_ALIVE);
        }
        message.getChannel().write(response);
        message.getChannel().flush();
    }

    private Object invoke(HttpMessage message, MethodProxy methodProxy) throws Exception {
        final Method method = methodProxy.getMethod();
        Class<?> valueType = methodProxy.getValueType();

        Object object;
        if (valueType.isArray()) {
            object = message.getRequest().content().array();
        } else if (valueType == String.class) {
            object = new String(message.getRequest().content().array());
        } else {
            ObjectNode data = HttpContextHelper.parseParam(message.getRequest());
            object = Jsons.getMapper().convertValue(data, valueType);
        }
        if (object == null) {
            object = new Object();
        }
        IdsRequest<?> idsRequest = new IdsRequest<>(object);
        idsRequest.setChannel(message.getChannel());
        idsRequest.setUserId(message.getUserId());
        try {
            return method.invoke(methodProxy.getObj(), idsRequest);
        } catch (Exception exception) {
            if (exception instanceof InvocationTargetException) {
                Throwable targetException = ((InvocationTargetException) exception).getTargetException();
                if (targetException instanceof ErrorCode) {
                    return new IdsResponse<>(null, (ErrorCode) targetException);
                } else {
                    throw exception;
                }
            } else {
                throw exception;
            }
        }
    }
}
