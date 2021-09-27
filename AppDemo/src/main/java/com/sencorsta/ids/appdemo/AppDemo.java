package com.sencorsta.ids.appdemo;

import com.sencorsta.ids.core.application.Application;


/**
 * @author ICe
 */
public class AppDemo extends Application {
    public static AppDemo init() {
        instance = new AppDemo();
        return (AppDemo) instance;
    }
}
