package com.sencorsta.ids.core.net.protocol;

import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * @description: 基本信息
 * @author ICe
 * @date 2019/6/12 17:18
 */
@Data
public abstract class BaseMessage implements Cloneable {
    protected Header header;
    public Body body;

    public BaseMessage() {
        header = new Header();
    }

    public void decode(ByteBuf in) {
        header.signature = in.readShort();
        header.type = in.readShort();
        header.length = in.readInt();
        body = new Body(header.length);
        in.readBytes(body.content);
        decodeBody();
    }


    public void encode(ByteBuf out) {
        encodeBody();
        out.writeShort(header.signature);
        out.writeShort(header.type);
        out.writeInt(header.length);
        out.writeBytes(body.content);
    }

    //编码
    public abstract void encodeBody();

    //解码
    public abstract void decodeBody();

    @Override
    public String toString() {
        return "header:[" + header + "] body:[" + body + "]";
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
