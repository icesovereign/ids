package com.sencorsta.ids.core.net.protocol;

import com.sencorsta.ids.core.config.GlobalConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;

import java.util.Arrays;

/**
 * 包体构建类
 *
 * @author ICe
 */
@Data
public final class Body {

    public ByteBuf content;

    public Body(int size) {
        content = Unpooled.buffer(size);
    }

    @Override
    public String toString() {
        return Arrays.toString(content.array());
    }

    /**
     * 写入一个字符串，长度限制为5000
     */
    public void writeString(String utf) {
        if (utf == null) {
            content.writeShort((short) 0);
            return;
        } else if (utf.length() > Short.MAX_VALUE) {
            utf = utf.substring(0, Short.MAX_VALUE);
        }
        byte[] bytes = utf.getBytes(GlobalConfig.UTF_8);
        content.writeShort((short) bytes.length);
        content.writeBytes(bytes);
    }

    public String readString() {
        String tempString = null;
        short length = content.readShort();
        if (length > 0) {
            byte[] byteArr = new byte[length];
            content.readBytes(byteArr);
            tempString = new String(byteArr, GlobalConfig.UTF_8);
        }
        return tempString;
    }

}
