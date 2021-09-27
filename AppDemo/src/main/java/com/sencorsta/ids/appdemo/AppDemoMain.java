package com.sencorsta.ids.appdemo;

import com.sencorsta.ids.core.config.GlobalConfig;

/**
 * @author ICe
 */
public class AppDemoMain {
    public static void main(String[] args) {
        GlobalConfig.instance().reload();
        AppDemo.init().start(System.getProperty("server.type"));
    }
}
