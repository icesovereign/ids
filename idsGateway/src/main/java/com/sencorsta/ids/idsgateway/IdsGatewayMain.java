package com.sencorsta.ids.idsgateway;

import com.sencorsta.ids.core.config.GlobalConfig;

/**
 * @author ICe
 */
public class IdsGatewayMain {
    public static void main(String[] args) {
        GlobalConfig.instance().reload();
        IdsGateway.init().start(System.getProperty("server.type"));
    }
}
