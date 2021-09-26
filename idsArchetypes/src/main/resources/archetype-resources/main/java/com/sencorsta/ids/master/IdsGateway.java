package com.sencorsta.ids.master;

import com.sencorsta.ids.core.application.Application;


/**
 * @author ICe
 */
public class IdsProxy extends Application {
    public static IdsProxy init() {
        instance = new IdsProxy();
        return (IdsProxy) instance;
    }
}
