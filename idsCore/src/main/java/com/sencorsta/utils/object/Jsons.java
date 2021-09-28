package com.sencorsta.utils.object;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sencorsta.ids.core.application.master.response.PingMasterResponse;
import com.sencorsta.ids.core.entity.IdsResponse;
import lombok.Getter;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * json 封装类
 *
 * @author ICe
 */
public class Jsons {
    @Getter
    public static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:SS"));
        mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.setVisibility(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
    }

    public static <T> T toBean(byte[] data, Class<T> valueType) {
        if (ObjectUtil.isEmpty(data)) {
            return null;
        }
        try {
            return Jsons.mapper.readValue(data, valueType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T toBean(byte[] data, TypeReference<T> valueType) {
        if (ObjectUtil.isEmpty(data)) {
            return null;
        }
        try {
            return Jsons.mapper.readValue(data, valueType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ObjectNode createObjectNode() {
        return mapper.createObjectNode();
    }

    public static ArrayNode createArrayNode() {
        return mapper.createArrayNode();
    }

    public static void main(String[] args) throws Exception {
        String jsonString="{\"channel\":null,\"server\":{\"type\":\"idsGateway\",\"sid\":null,\"host\":\"0.0.0.0\",\"publicHost\":\"10.198.50.16\",\"port\":10001,\"freeMemory\":385336040,\"maxMemory\":510656512}}";

        JsonNode jsonNode = mapper.readTree(jsonString);

        ObjectNode objectNode = jsonNode.deepCopy();
        System.out.println(objectNode.get("server"));
    }

}
