package com.sencorsta.utils.object;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.sencorsta.ids.core.application.master.response.PingMasterResponse;
import com.sencorsta.ids.core.entity.IdsResponse;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * json 封装类
 *
 * @author ICe
 */
public class Jsons {
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

}
