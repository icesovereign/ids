package com.sencorsta.utils.string;

import ch.qos.logback.core.pattern.color.ANSIConstants;

/**
 * @author daibin
 */
public class ColorString {
    public static String getColorStr(String context, String code) {
        String log = ANSIConstants.ESC_START + code + ANSIConstants.ESC_END + context + ANSIConstants.ESC_START + ANSIConstants.ESC_END;
        return log;
    }

    public static String getColor256Str(String context, String code) {
        return getColorStr(context, "38;5;" + code);
    }

    public static String getColor256Head(String code) {
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
}