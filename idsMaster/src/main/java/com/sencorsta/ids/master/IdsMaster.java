package com.sencorsta.ids.master;

import com.sencorsta.ids.core.application.Application;


/**
 * @author daibin
 */
public class IdsMaster extends Application {
    public static IdsMaster init() {
        instance = new IdsMaster();
        return (IdsMaster) instance;
    }

//	// 服务器列表
//	public static final ConcurrentHashMap<String, List<Server>> totalServers = new ConcurrentHashMap<String, List<Server>>();


}
