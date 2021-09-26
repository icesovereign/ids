package com.sencorsta.ids.core.constant;

/**
 * @author ICe
 * @description: 序列化类型
 * @date 2019/6/12 17:27
 */
public class SerializeTypeConstant {
    public static final short TYPE_JSON = 1;
    public static final short TYPE_PROTOBUF = 2;
    public static final short TYPE_BYTEARR = 3;
    public static final short TYPE_STRING = 4;

    public static String getSerializeStrByType(int type) {
        switch (type) {
            case TYPE_JSON:
                return "JSON";
            case TYPE_PROTOBUF:
                return "PROTOBUF";
            case TYPE_BYTEARR:
                return "BYTE";
            case TYPE_STRING:
                return "STRING";
            default:
                return "UNKNOWN";
        }
    }
}
