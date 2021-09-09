package com.sencorsta.ids.master;

import cn.hutool.core.util.StrUtil;
import com.sencorsta.ids.core.config.GlobalConfig;

/**
 * @author daibin
 */
public class IdsMasterMain {
    public static void main(String[] args) {
        GlobalConfig.instance().reload();
        String property = System.getProperty("server.type");
        IdsMaster.init().start(StrUtil.isNotBlank(property)?property:"app");
    }
}
