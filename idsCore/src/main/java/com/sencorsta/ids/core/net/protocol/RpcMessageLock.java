package com.sencorsta.ids.core.net.protocol;


import lombok.Data;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author ICe
 */
@Data
public class RpcMessageLock {
    private Lock lock;
    private Condition condition;
    private RpcMessage message;

    public RpcMessageLock(Lock lock, Condition condition) {
        this.lock = lock;
        this.condition = condition;
    }
}
