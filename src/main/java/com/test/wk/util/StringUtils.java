package com.test.wk.util;

public class StringUtils {

    public static boolean isEmpty(String msg) {
        if (msg == null || "".equalsIgnoreCase(msg)){
            return true;
        }
        return false;
    }
}
