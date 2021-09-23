package com.sencorsta.ids.core.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author ICe
 */
@Data
@AllArgsConstructor
public class ErrorCode extends Exception {
    private Integer code;
    private String msg;
}
