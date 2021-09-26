package ${package}.${typeLow};

import com.sencorsta.ids.core.config.GlobalConfig;

/**
 * @author ICe
 */
public class ${typeFirstUp}Main {
    public static void main(String[] args) {
        GlobalConfig.instance().reload();
        ${typeFirstUp}.init().start(System.getProperty("server.type"));
    }
}
