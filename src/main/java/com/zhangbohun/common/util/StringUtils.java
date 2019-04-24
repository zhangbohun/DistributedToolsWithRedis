package com.zhangbohun.common.util;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhangbohun
 * Create Date 2019/04/16 14:56
 * Modify Date 2019/04/17 16:59
 */
public class StringUtils {

    public static String toString(Object obj) {
        return (obj == null) ? "" : obj.toString();
    }

    private static final Pattern NumberPattern = Pattern.compile("^([-]?[0-9]+([\\.]{0,1}[0-9]+){0,1})$");

    public static boolean isNumber(String s) {
        return !DataUtils.isEmpty(s) && NumberPattern.matcher(s).matches();
    }

    public static boolean isNotNumber(String s) {
        return !isNumber(s);
    }

    public static Boolean isMatch(String str, String partten) {
        if (str == null) {
            return false;
        }
        Pattern p = Pattern.compile(partten);
        Matcher matcher = p.matcher(str);
        if (matcher.matches()) {
            return true;
        }
        return false;
    }

    public static Boolean isNotMatch(String str, String partten) {

        return !isMatch(str, partten);
    }

    public static String getNewUUIDString() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String trim(String s) {
        s = toString(s);
        //replaceAll参数是正则,replace不是
        //replace,replaceAll不改变原字符串
        s = s.replaceAll("^\\s*", "");
        s = s.replaceAll("\\s*$", "");
        return s;
    }
}
