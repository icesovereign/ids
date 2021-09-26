package ${package}.${typeLow};

import com.sencorsta.ids.core.application.Application;


/**
 * @author ICe
 */
public class ${typeFirstUp} extends Application {
    public static ${typeFirstUp} init() {
        instance = new ${typeFirstUp}();
        return (${typeFirstUp}) instance;
    }
}
