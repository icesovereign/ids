package com.sencorsta.ids.core.application.master.request;

import io.netty.channel.Channel;
import lombok.Data;

/**
 * @author ICe
 */
@Data
public class PingMasterRequest {
    int freeMemory;
}
