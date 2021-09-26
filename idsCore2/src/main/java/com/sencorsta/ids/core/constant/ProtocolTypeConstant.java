package com.sencorsta.ids.core.constant;

/**
 * @author ICe
 */
public class ProtocolTypeConstant {
    public static final short TYPE_RPC_REQ = 1;
    public static final short TYPE_RPC_RES = 2;

    public static final short TYPE_REQ = 3;
    public static final short TYPE_RES = 4;

    public static final short TYPE_HEAT = 5;

    public static final short TYPE_PUSH = 6;
    public static final short TYPE_RPC_PUSH = 7;


    public static String getProtocolStrByType(int type) {
        switch (type) {
            case TYPE_RPC_REQ:
                return "RPC_REQ";
            case TYPE_RPC_RES:
                return "RPC_RES";
            case TYPE_REQ:
                return "REQ";
            case TYPE_RES:
                return "RES";
            case TYPE_HEAT:
                return "HEAT";
            case TYPE_PUSH:
                return "PUSH";
            case TYPE_RPC_PUSH:
                return "RPC_PUSH";
            default:
                return "UNKNOWN";
        }
    }
}
