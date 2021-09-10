package com.sencorsta.ids.core.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author daibin
 */
@Data
@AllArgsConstructor
public class ErrorCode {
    private Integer code;
    private String msg;
}
