package com.sencorsta.ids.api.request;

import com.sencorsta.ids.core.entity.Server;
import io.netty.channel.Channel;
import lombok.Data;

/**
 * @author daibin
 */
@Data
public class JoinMasterRequest {
    Channel channel;
    Server server;
}
