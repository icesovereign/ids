package com.sencorsta.ids.master;

import com.sencorsta.ids.core.config.GlobalConfig;

/**
 * @author ICe
 */
public class IdsMasterMain {
    public static void main(String[] args) {
        GlobalConfig.instance().reload();
        IdsMaster.init().start(System.getProperty("server.type"));
    }
}
