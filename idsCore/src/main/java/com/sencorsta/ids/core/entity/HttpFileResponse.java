package com.sencorsta.ids.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 响应基础类
 *
 * @author ICe
 */
@Data
@NoArgsConstructor
public class HttpFileResponse<T> extends IdsResponse<T> {
    @JsonIgnore
    private String contentType;
    @JsonIgnore
    private String fileName;
    @JsonIgnore
    private String filePath;

    public HttpFileResponse(T data) {
        super(data);
    }

    public HttpFileResponse(T data, ErrorCode errorCode) {
        super(data, errorCode);
    }
}
