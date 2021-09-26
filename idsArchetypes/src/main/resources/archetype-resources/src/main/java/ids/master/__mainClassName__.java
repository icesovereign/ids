package ${groupId}.ids.master;

import com.sencorsta.ids.core.application.Application;


/**
 * @author ICe
 */
public class ${mainClassName} extends Application {
    public static ${mainClassName} init() {
        instance = new ${mainClassName}();
        return (${mainClassName}) instance;
    }
}
