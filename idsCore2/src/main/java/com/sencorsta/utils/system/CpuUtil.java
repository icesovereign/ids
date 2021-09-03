package com.sencorsta.utils.system;

import cn.hutool.system.SystemUtil;
import com.sencorsta.utils.string.StringUtil;

import java.io.IOException;
import java.util.Scanner;

/**
 * @description: 功能系统
 * @author ICe
 * @date 2019/6/12 17:21
 *
 */
public class CpuUtil {
    private static String getCpu4Windows() {
        try {
            Process process = Runtime.getRuntime().exec("wmic cpu get name");
            process.getOutputStream().close();
            Scanner sc = new Scanner(process.getInputStream());
            String key = sc.next();
            String cpuName = "";
            while (sc.hasNext()) {
                cpuName += sc.next() + " ";
            }
            return StringUtil.isEmpty(cpuName) ? "unknown" : cpuName.substring(0, cpuName.length() - 1);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static String getCpu4Mac() {
        try {
            Process process = Runtime.getRuntime().exec("sysctl machdep.cpu.brand_string");
            process.getOutputStream().close();
            Scanner sc = new Scanner(process.getInputStream());
            String cpuName = "";
            while (sc.hasNext()) {
                cpuName += sc.next() + " ";
            }
            return StringUtil.isEmpty(cpuName) ? "unknown" : cpuName.substring(0, cpuName.length() - 1);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "unknown";
    }

    private static String getCpu4Linux() {
        try {
            Process process = Runtime.getRuntime().exec("cat /proc/cpuinfo");
            process.getOutputStream().close();
            Scanner sc = new Scanner(process.getInputStream());
            String prefix = "model name\t: ";
            while (sc.hasNextLine()) {
                String next = sc.nextLine();
                if (next.startsWith(prefix)) {
                    return next.replaceAll(prefix, "");
                }
            }
            return "unknown";
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "unknown";
    }

    public static String getCpu() {
        if (SystemUtil.getOsInfo().isWindows()) {
            return getCpu4Windows();
        } else if (SystemUtil.getOsInfo().isMac()) {
            return getCpu4Mac();
        } else if (SystemUtil.getOsInfo().isLinux()) {
            return getCpu4Linux();
        }
        return "unknown";
    }


}
