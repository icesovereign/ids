package com.sencorsta.ids.core.constant;

import com.sencorsta.ids.core.entity.ErrorCode;

/**
 * @author daibin
 */
public class ErrorCodeConstant {
    public static final ErrorCode SUCCESS = new ErrorCode(0, "SUCCESS");
    public static final ErrorCode NOT_FIND = new ErrorCode(400, "NOT_FIND");
    public static final ErrorCode SYSTEM_ERROR = new ErrorCode(500, "SYSTEM_ERROR");
}
