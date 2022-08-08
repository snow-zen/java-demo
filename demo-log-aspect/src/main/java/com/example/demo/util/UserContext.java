package com.example.demo.util;

/**
 * 用户上下文信息
 *
 * @author snow-zen
 */
public final class UserContext {

    private final static ThreadLocal<String> LOCAL_USER_INFO = new ThreadLocal<>();

    private UserContext() {
        throw new UnsupportedOperationException();
    }

    public static void setLocalUserInfo(String userInfo) {
        LOCAL_USER_INFO.set(userInfo);
    }

    public static String getLocalUserInfo() {
        return LOCAL_USER_INFO.get();
    }
}
