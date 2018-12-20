package com.example.ori.jnidemo.utils;

public class StringUtil {

    private StringUtil() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static Boolean isNotEmpty(String str) {
        return str != null && !"".equals(str.trim());
    }

    public static Boolean isEmpty(String str){
        return str == null || "".equals(str.trim());
    }
}
