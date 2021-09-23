package com.sencorsta.ids.core.application.master.request;

import com.sencorsta.ids.core.entity.Server;
import io.netty.channel.Channel;
import lombok.Data;

/**
 * @author ICe
 */
@Data
public class JoinMasterRequest {
    Channel channel;
    Server server;
}
