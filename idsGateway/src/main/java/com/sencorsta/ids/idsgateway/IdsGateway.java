package com.sencorsta.ids.idsgateway;

import com.sencorsta.ids.core.application.Application;


/**
 * @author ICe
 */
public class IdsGateway extends Application {
    public static IdsGateway init() {
        instance = new IdsGateway();
        return (IdsGateway) instance;
    }
}
