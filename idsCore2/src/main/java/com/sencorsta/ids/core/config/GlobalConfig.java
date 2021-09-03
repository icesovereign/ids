package com.sencorsta.ids.core.config;

import cn.hutool.setting.Setting;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局配置
 *
 * @author daibin
 */
@Slf4j
public class GlobalConfig extends Setting {

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

    private GlobalConfig() {
        super();
        init();
    }

    private void init() {
        baseConfig = new Setting(CONF_DIR + BASE_CONFIG_NAME, true);
        Boolean isAutoLoad = baseConfig.getBool("autoLoad", ConfigGroup.core.getName(), false);
        baseConfig.autoLoad(isAutoLoad, (callBack) -> onConfModify(baseConfig));
        this.addSetting(baseConfig);

        Setting include = baseConfig.getSetting(ConfigGroup.include.getName());
        include.forEach((key, value) -> {
            Setting temp = new Setting(ROOT_DIR + value);
            temp.autoLoad(isAutoLoad, (callBack) -> onConfModify(temp));
            this.addSetting(temp);
            includeConfig.put(key, temp);
        });
        isDebug = getBool("server.debug", ConfigGroup.core.getName(), false);
        System.setProperty("server.type",getStr("server.type", ConfigGroup.core.getName(), "default"));
        System.setProperty("server.type",getStr("log.home", ConfigGroup.core.getName(), "./log"));

    }

    /**
     * 配置变动会调用此方法
     *
     * @param config 变动的配置
     * @return 总配置
     */
    private Setting onConfModify(Setting config) {
        return this.addSetting(config);
    }

    public void reload() {
        init();
    }
}
