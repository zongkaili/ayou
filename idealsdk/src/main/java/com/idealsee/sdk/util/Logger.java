package com.idealsee.sdk.util;

import android.util.Log;

public class Logger {
    public static final int LOG_LEVEL_DEVELOP = 0;
    public static final int LOG_LEVEL_TEST = 1;
    public static final int LOG_LEVEL_RELEASE = 2;

    private static int logLevel = LOG_LEVEL_RELEASE;
    private static int logUrlLevel = LOG_LEVEL_RELEASE;
    private static String tag = "Idealsee";

    private Logger() {

    }

    /**
     * log error.
     *
     * @param nMessage message
     */
    public static final void LOGE(String nMessage) {
        if (logLevel < LOG_LEVEL_RELEASE) {
            StringBuffer bf = new StringBuffer("[" + ISARConstants.APP_PACKAGE_NAME + "]===>>");
            bf.append(nMessage);
            Log.e(tag, bf.toString());
        }
    }

    /**
     * log warning.
     *
     * @param nMessage message
     */
    public static final void LOGW(String nMessage) {
        if (logLevel < LOG_LEVEL_RELEASE) {
            Log.d(tag, " IdealseeApplication.getInstance().getPackageName()=" + ISARConstants.APP_PACKAGE_NAME);
            StringBuffer bf = new StringBuffer("[" + ISARConstants.APP_PACKAGE_NAME + "]===>>");
            bf.append(nMessage);
            Log.w(tag, bf.toString());
        }
    }

    /**
     * log debug.
     *
     * @param nMessage message
     */
    public static final void LOGD(String nMessage) {
        if (logLevel < LOG_LEVEL_RELEASE) {
            StringBuffer bf = new StringBuffer("[" + ISARConstants.APP_PACKAGE_NAME + "]===>>");
            bf.append(nMessage);
            Log.d(tag, bf.toString());
        }
    }

    /**
     * log info.
     *
     * @param nMessage message
     */
    public static final void LOGI(String nMessage) {
        if (logLevel < LOG_LEVEL_RELEASE) {
            StringBuffer bf = new StringBuffer("[" + ISARConstants.APP_PACKAGE_NAME + "]===>>");
            bf.append(nMessage);
            Log.i(tag, bf.toString());
        }
    }

    /**
     * get URL level.
     *
     * @return level
     */
    public static int getLogLevel() {
        return logUrlLevel;
    }

    /**
     * 返回log级别
     *
     * @return
     */
    public static int getLogLevelTest() {
        return logLevel;
    }

    public static void enableLogTest(boolean enable) {
        if (enable) {
            logLevel = LOG_LEVEL_TEST;
        } else {
            logLevel = LOG_LEVEL_RELEASE;
        }
    }
}
