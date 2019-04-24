package com.zhangbohun.common.util;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhangbohun
 * Create Date 2019/04/16 14:56
 * Modify Date 2019/04/17 16:59
 */
public class DataUtils {
    //相同具体类型对象比较,后续可以补充,不要加Object
    public static boolean equals(String obj1, String obj2) {
        return Objects.equals(obj1, obj2);
    }

    public static boolean equals(Class obj1, Class obj2) {
        return Objects.equals(obj1, obj2);
    }

    public static boolean equals(Long obj1, Long obj2) {
        return Objects.equals(obj1, obj2);
    }

    public static boolean equals(Float obj1, Float obj2) {
        return Objects.equals(obj1, obj2);
    }

    public static boolean equals(Double obj1, Double obj2) {
        return Objects.equals(obj1, obj2);
    }

    public static boolean equals(Integer obj1, Integer obj2) {
        return Objects.equals(obj1, obj2);
    }

    public static boolean equals(Short obj1, Short obj2) {
        return Objects.equals(obj1, obj2);
    }

    public static boolean equals(Byte obj1, Byte obj2) {
        return Objects.equals(obj1, obj2);
    }

    public static boolean equals(Boolean obj1, Boolean obj2) {
        return Objects.equals(obj1, obj2);
    }

    public static boolean isNull(Object o) {
        return o == null;
    }

    public static boolean isNotNull(Object o) {
        return !isNull(o);
    }

    public static boolean isEmpty(String s) {
        return s == null || "".equals(s);
    }

    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

    public static boolean isBlank(String s) {
        return isEmpty(s) || s.matches("\\s*");
    }

    public static boolean isNotBlank(String s) {
        return !isBlank(s);
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.size() == 0;
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static boolean isEmpty(Map<?, ?> m) {
        return m == null || m.size() == 0;
    }

    public static boolean isNotEmpty(Map<?, ?> m) {
        return !isEmpty(m);
    }

    public static boolean isEmpty(Byte[] b) {
        return b == null || b.length == 0;
    }

    public static boolean isNotEmpty(Byte[] b) {
        return !isEmpty(b);
    }
}
