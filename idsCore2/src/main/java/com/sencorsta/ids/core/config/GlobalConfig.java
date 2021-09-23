package com.sencorsta.ids.core.config;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import cn.hutool.setting.GroupedMap;
import cn.hutool.setting.Setting;
import com.sencorsta.ids.core.entity.Server;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局配置
 *
 * @author ICe
 */
public class GlobalConfig extends Setting {
    private static Logger log = LoggerFactory.getLogger(GlobalConfig.class);
    private static final String BASE_CONFIG_NAME = "ids-server.conf";
    public static Charset UTF_8 = StandardCharsets.UTF_8;
    public static Short SIGNATURE = 7777;
    public static boolean IS_DEBUG = false;
    public static AttributeKey<Server> SERVER_KEY = AttributeKey.valueOf("SERVER_KEY");

    private static class SingletonHolder {
        private static final GlobalConfig INSTANCE = new GlobalConfig();
    }

    public static GlobalConfig instance() {
        return SingletonHolder.INSTANCE;
    }

    private Setting baseConfig;
    private final Map<String, Setting> includeConfig = new ConcurrentHashMap<>();

    private boolean isInit = false;

    private GlobalConfig() {
        super();
        init();
    }

    private synchronized void init() {
        if (isInit) {
            return;
        }
        baseConfig = new Setting(loadResource(BASE_CONFIG_NAME), charset, false);
        Boolean isAutoLoad = baseConfig.getBool("autoLoad", ConfigGroup.core.getName(), false);
        baseConfig.autoLoad(isAutoLoad, (callBack) -> onConfModify(baseConfig, callBack));
        this.addSetting(baseConfig);

        Setting include = baseConfig.getSetting(ConfigGroup.include.getName());
        include.forEach((key, value) -> {
            Setting temp = new Setting(loadResource(value), charset, false);
            temp.autoLoad(isAutoLoad, (callBack) -> onConfModify(temp, callBack));
            this.addSetting(temp);
            includeConfig.put(key, temp);
        });
        IS_DEBUG = getBool("server.debug", ConfigGroup.core.getName(), false);
        System.setProperty("server.type", getStr("server.type", ConfigGroup.core.getName(), "default"));
        System.setProperty("log.home", getStr("log.home", ConfigGroup.core.getName(), "./log"));
        System.setProperty("log.level", getStr("log.level", ConfigGroup.core.getName(), "info"));
        reloadLogger();
        isInit = true;
    }

    private void reloadLogger() {
        try {
            final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            ContextInitializer ci = new ContextInitializer(loggerContext);
            URL url = ci.findURLOfDefaultConfigurationFile(true);
            loggerContext.reset();
            ci.configureByResource(url);
        } catch (JoranException e) {
            e.printStackTrace();
        }
    }

    private URL loadResource(String name) {
        final URL resource = this.getClass().getClassLoader().getResource(name);
        if (resource == null) {
            throw new RuntimeException("配置缺失：" + name);
        }
        return resource;
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
     * @param config   变动的配置
     * @param callBack
     * @return 总配置
     */
    private Setting onConfModify(Setting config, Boolean callBack) {
        log.debug("重新加载配置1{}, callBack:{}", config, callBack);
        return this.addSetting(config);
    }

    public void reload() {
        init();
    }
}
