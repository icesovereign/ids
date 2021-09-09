package com.sencorsta.ids.core.config;

import cn.hutool.setting.GroupedMap;
import cn.hutool.setting.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局配置
 *
 * @author daibin
 */
public class GlobalConfig extends Setting {
    Logger log = LoggerFactory.getLogger(GlobalConfig.class);
    private static final String BASE_CONFIG_NAME = "idsServer.conf";
    public static Charset UTF_8 = StandardCharsets.UTF_8;
    public static Short SIGNATURE = 7777;
    public static boolean isDebug = false;

    private static class SingletonHolder {
        private static final GlobalConfig INSTANCE = new GlobalConfig();
    }
    public static GlobalConfig instance() {
        return SingletonHolder.INSTANCE;
    }

    private Setting baseConfig;
    private final Map<String, Setting> includeConfig = new ConcurrentHashMap<>();
    public final static String ROOT_DIR = System.getProperty("user.dir") + File.separator;
    public final static String CONF_DIR = ROOT_DIR + "conf" + File.separator;

    private boolean isInit = false;

    private GlobalConfig() {
        super();
        init();
    }

    private void init() {
        if (isInit) {
            return;
        }
        baseConfig = new Setting(CONF_DIR + BASE_CONFIG_NAME, true);
        Boolean isAutoLoad = baseConfig.getBool("autoLoad", ConfigGroup.core.getName(), false);
        baseConfig.autoLoad(isAutoLoad, (callBack) -> onConfModify(baseConfig,callBack));
        this.addSetting(baseConfig);

        Setting include = baseConfig.getSetting(ConfigGroup.include.getName());
        include.forEach((key, value) -> {
            Setting temp = new Setting(ROOT_DIR + value);
            temp.autoLoad(isAutoLoad, (callBack) -> onConfModify(temp,callBack));
            this.addSetting(temp);
            includeConfig.put(key, temp);
        });
        isDebug = getBool("server.debug", ConfigGroup.core.getName(), false);
        System.setProperty("server.type", getStr("server.type", ConfigGroup.core.getName(), "default"));
        System.setProperty("log.home", getStr("log.home", ConfigGroup.core.getName(), "./log"));
        System.setProperty("log.level", getStr("log.level", ConfigGroup.core.getName(), "info"));
        StaticLoggerBinder.reset();
        log = LoggerFactory.getLogger(GlobalConfig.class);
        isInit = true;
    }

    public void printValue() {
        List<String> groups = getGroups();
        GroupedMap groupedMap = getGroupedMap();
        StringBuilder sb = new StringBuilder();
        sb.append("成功加载配置:\n");
        groups.forEach(key -> {
            sb.append("[").append(key).append("]:\n");
            LinkedHashMap<String, String> stringStringLinkedHashMap = groupedMap.get(key);
            stringStringLinkedHashMap.forEach((k, v) -> {
                sb.append(k).append(" = ").append(v).append("\n");
            });
        });
        log.debug(sb.toString());
    }

    /**
     * 配置变动会调用此方法
     *
     * @param config 变动的配置
     * @param callBack
     * @return 总配置
     */
    private Setting onConfModify(Setting config, Boolean callBack) {
        log.debug("重新加载配置1{}, callBack:{}", config,callBack);
        return this.addSetting(config);
    }

    public void reload() {
        init();
    }
}
