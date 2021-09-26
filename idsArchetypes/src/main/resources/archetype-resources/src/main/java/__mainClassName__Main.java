package ${package};

import com.sencorsta.ids.core.config.GlobalConfig;

/**
 * @author ICe
 */
public class ${mainClassName}Main {
    public static void main(String[] args) {
        GlobalConfig.instance().reload();
        ${mainClassName}.init().start(System.getProperty("server.type"));
    }
}
