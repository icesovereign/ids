package com.sencorsta.ids.core.net.protocol;

import com.sencorsta.ids.core.config.GlobalConfig;
import lombok.Data;

/**
 * 通讯协议包头
 *
 * @author ICe
 */
@Data
public class Header {

    /**
     * 协议特征码 用于快速识别本协议
     */
    public short signature = GlobalConfig.SIGNATURE;
    /**
     * 协议类型
     */
    public short type;

    /**
     * 协议包体字节数
     */
    public int length;

    /**
     * 包头的字节数
     */
    public final static byte SIZE = 8;

    public Header() {
        type = 0;
        length = 0;
    }

    @Override
    public String toString() {
        return "type:" + type + " length:" + length;
    }

}
