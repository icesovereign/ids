package com.sencorsta.ids.master;

import com.sencorsta.ids.core.config.GlobalConfig;

/**
 * @author ICe
 */
public class IdsProxyMain {
    public static void main(String[] args) {
        GlobalConfig.instance().reload();
        IdsProxy.init().start(System.getProperty("server.type"));
    }
}
