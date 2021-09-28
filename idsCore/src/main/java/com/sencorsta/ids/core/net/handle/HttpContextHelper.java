package com.sencorsta.ids.core.net.handle;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sencorsta.ids.core.config.GlobalConfig;
import com.sencorsta.utils.object.Jsons;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * HttpContextHelper
 *
 * @author ICe
 */
@Slf4j
public class HttpContextHelper {

    /**
     * 解析请求参数
     *
     * @return 包含所有请求参数的键值对, 如果没有参数, 则返回空Map
     * @throws
     * @throws IOException
     */
    public static ObjectNode parseParamPost(FullHttpRequest req) throws Exception {
        // 是POST请求
        String type = req.headers().get("Content-Type");
        log.trace("Content-Type:{}", type);
        ObjectNode paramMap;
        if (type != null && type.contains("json")) {
            byte[] bytes = new byte[req.content().readableBytes()];
            req.content().getBytes(0, bytes);
            String json = new String(bytes, GlobalConfig.UTF_8);
            paramMap = Jsons.getMapper().readTree(json).deepCopy();
            if (ObjectUtil.isEmpty(paramMap)) {
                paramMap = Jsons.createObjectNode();
            }
        } else if (type != null && type.contains("text/xml")) {
            paramMap = Jsons.createObjectNode();
            paramMap.put("xmlString", req.content().toString(GlobalConfig.UTF_8));
        } else {
            paramMap = Jsons.createObjectNode();
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(req);
            decoder.offer(req);
            List<InterfaceHttpData> paramList = decoder.getBodyHttpDatas();
            for (InterfaceHttpData param : paramList) {
                log.trace("HttpDataType：{}", param.getHttpDataType());
                if (param.getHttpDataType().equals(InterfaceHttpData.HttpDataType.Attribute)) {
                    Attribute data = (Attribute) param;
                    paramMap.put(data.getName(), data.getValue());
                } else if (param.getHttpDataType().equals(InterfaceHttpData.HttpDataType.FileUpload)) {
                    final FileUpload fileUpload = (FileUpload) param;
                    byte[] file = fileUpload.get();
                    ObjectNode fileJson = Jsons.createObjectNode();
                    fileJson.put("name", fileUpload.getFilename());
                    fileJson.put("data", file);
                    paramMap.replace(fileUpload.getName(), fileJson);
                }
            }
        }
        if (req.uri().contains("?")) {
            final ObjectNode tempNode = getQueryStringParam(req);
            paramMap.setAll(tempNode);
        }
        return paramMap;
    }

    public static ObjectNode parseParamGet(FullHttpRequest req) {
        // 是GET请求
        ObjectNode paramMap = Jsons.createObjectNode();
        if (req.uri().contains("?")) {
            final ObjectNode tempNode = getQueryStringParam(req);
            paramMap.setAll(tempNode);
        }
        return paramMap;
    }

    private static ObjectNode getQueryStringParam(FullHttpRequest req) {
        QueryStringDecoder query = new QueryStringDecoder(req.uri());
        final ObjectNode tempNode = Jsons.createObjectNode();
        query.parameters().forEach((key, values) -> {
            if (values.size() == 1) {
                tempNode.put(key, values.get(0));
            } else {
                ArrayNode arrayNode = Jsons.createArrayNode();
                values.forEach(arrayNode::add);
                tempNode.replace(key, arrayNode);
            }
        });
        return tempNode;
    }

    /**
     * 解析请求参数
     *
     * @return 包含所有请求参数的键值对, 如果没有参数, 则返回空Map
     * @throws
     * @throws IOException
     */
    public static ObjectNode parseParam(FullHttpRequest req) throws Exception {
        HttpMethod method = req.method();
        if (HttpMethod.POST == method) {
            return parseParamPost(req);
        } else if (HttpMethod.GET == method) {
            return parseParamGet(req);
        } else if (HttpMethod.OPTIONS == method) {
            return null;
        }
        return null;
    }
}
