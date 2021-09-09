package com.sencorsta.utils.string;

import ch.qos.logback.core.pattern.color.ANSIConstants;
import cn.hutool.system.SystemUtil;
import com.sencorsta.ids.core.application.Application;

import java.net.URL;

/**
 * @author daibin
 */
public class ColorString {
    static boolean isJar = true;

    static {
        URL path = ColorString.class.getResource("ColorString.class");
        if (path.toString().startsWith("jar:")) {
            isJar = true;
        } else {
            isJar = false;
        }
    }


    public static String getColorStr(String context, String code) {
        if (!detectIfAnsiCapable()) {
            return context;
        }
        String log = ANSIConstants.ESC_START + code + ANSIConstants.ESC_END + context + ANSIConstants.ESC_START + ANSIConstants.ESC_END;
        return log;
    }

    public static String getColor256Str(String context, String code) {
        if (!detectIfAnsiCapable()) {
            return context;
        }
        return getColorStr(context, "38;5;" + code);
    }

    public static String getColor256Head(String code) {
        if (!detectIfAnsiCapable()) {
            return "";
        }
        String log = ANSIConstants.ESC_START + "38;5;" + code + ANSIConstants.ESC_END;
        return log;
    }

    public static String getColorTable() {
        String context = "";
        for (int i = 0; i < 16; i++) {
            String aa = "";
            for (int j = 0; j < 16; j++) {
                int num = (i * 16 + j);
                String temp = "38;5;" + num;
                String colorStr = ColorString.getColorStr(num + "", temp);
                aa += colorStr + " ";
            }
            context += aa + "\n";
        }
        return context;
    }


    private static boolean detectIfAnsiCapable() {
        try {
            // ide启动也展示彩色
            if (!isJar) {
                return true;
            }
            if ((System.console() == null)) {
                return false;
            }
            return !(SystemUtil.getOsInfo().isWindows());
        } catch (Throwable ex) {
            return false;
        }
    }
}
