package com.sencorsta.ids.core.application.master.request;

import com.sencorsta.ids.core.entity.Server;
import lombok.Data;

import java.util.Map;

/**
 * @author ICe
 */
@Data
public class TotalServerPush {
    Map<String, Map<String, Server>> totalServers;
}
