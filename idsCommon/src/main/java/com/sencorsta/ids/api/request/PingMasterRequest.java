package com.sencorsta.ids.api.request;

import io.netty.channel.Channel;
import lombok.Data;

/**
 * @author daibin
 */
@Data
public class PingMasterRequest {
    Channel channel;
    int freeMemory;
}
