package com.sencorsta.ids.core.application.master.response;

import com.sencorsta.ids.core.entity.Server;
import lombok.Data;

import java.util.Map;

/**
 * @author ICe
 */
@Data
public class GetTotalServerResponse {
    Map<String, Map<String, Server>> totalServers;
}
