package com.yixun.sdk.demo.utils;

/**
 * Created by yaolei on 17-8-11.
 */

public class BtnUtils {
    private static final int SEGMENT_MS = 500;
    private static long lastClickTime;

    public static boolean isClickValid() {
        if (System.currentTimeMillis() - lastClickTime > SEGMENT_MS) {
            lastClickTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }
}
