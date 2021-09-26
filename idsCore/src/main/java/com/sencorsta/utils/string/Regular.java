package com.sencorsta.utils.string;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
　　* @description: 手机号码正则验证
　　* @author ICe
　　* @date 2019/6/18 11:40
　　*/
public class Regular {

    public static boolean phone(String phone){
        String regex = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17[013678])|(18[0,5-9]))\\d{8}$";
        if(phone.length() != 11){
            return false;
        }else{
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(phone);
            boolean isMatch = m.matches();
            if(isMatch){
                return true;
            } else {
                return false;
            }
        }
    }


}
