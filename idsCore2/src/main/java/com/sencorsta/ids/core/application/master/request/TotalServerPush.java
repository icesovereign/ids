package com.sencorsta.ids.core.application.master.request;

import com.sencorsta.ids.core.entity.Server;
import io.netty.channel.Channel;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author ICe
 */
@Data
public class TotalServerPush {
    Map<String, List<Server>> totalServers;
}
