package com.sencorsta.ids.core.constant;

import com.sencorsta.ids.core.entity.ErrorCode;

/**
 * @author ICe
 */
public class ErrorCodeConstant {
    public static final ErrorCode SUCCESS = new ErrorCode(0, "SUCCESS");
    public static final ErrorCode NOT_FIND = new ErrorCode(400, "NOT_FIND");
    public static final ErrorCode SYSTEM_ERROR = new ErrorCode(500, "SYSTEM_ERROR");

    public static final ErrorCode MASTER_SID_FULL = new ErrorCode(10000, "MASTER_SID_FULL");
    public static final ErrorCode MASTER_CAN_NOT_FIND_SERVER = new ErrorCode(10001, "MASTER_CAN_NOT_FIND_SERVER");
}
