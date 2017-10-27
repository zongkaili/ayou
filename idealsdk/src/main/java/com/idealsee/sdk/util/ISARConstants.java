/**
 * 项目名称: IDSeeAR
 * 类名称:  Constant
 * 类描述:
 * 创建人: Ly
 * 创建时间: 2013-1-24 下午12:29:33
 * 修改人:
 * 修改时间:
 * 备注:
 *
 * @version
 */

package com.idealsee.sdk.util;

import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.util.Locale;

/**
 * Constants class.
 *
 * @author Ly
 */
public class ISARConstants {

    // combine md5 for resources if release version.
    public static final String BASE_URL_FOR_UNITY_RELEASE = "http://yxfile.idealsee.com";
    public static final String BASE_URL_INNER = "http://oltest.yixun.idealsee.com"; // 外网测试
    //    public static final String BASE_URL_INNER = "http://10.0.1.33"; // 内网测试
    public static final String BASE_URL_RELEASE = "http://yixun.arhieason.com"; // release
    public static final String BASE_URL_CTCC = "http://118.194.63.239";
    public static final String BASE_URL_CMCC = "http://119.97.156.131";
    public static final String BASE_URL_CUCC = "http://182.118.12.243";
    public static final String QINIU_URL_INNER = "http://o6zq3p22t.bkt.clouddn.com"; // 外网测试
    //    public static final String QINIU_URL_INNER = "http://7xtnpe.com2.z0.glb.qiniucdn.com"; // 内网测试
    public static final String QINIU_URL_RELEASE = "http://7xsom5.com2.z0.glb.qiniucdn.com"; // release
    public static int APP_VERSION_CODE = 0;
    public static String APP_PARENT_PATH = "";
    public static String APP_NAME = "";
    public static String APP_KEY = "";
    public static String APP_ID = "";
    public static String APP_IMEI = "";
    public static String APP_VERSION_NAME = "";
    public static String APP_PACKAGE_NAME = "";
    public static final String APP_FROM = "idealsee";

    public static final int CONNECTION_CANCEL = -1;
    public static final int CONNECTION_EXCEPTION = 1001;
    public static final int SOCKTET_TIME_OUT_EXCEPTION = 1002;
    public static final int FILE_NOT_FOUND_EXCEPTION = 1003;
    public static final int HTTP_IO_EXCEPTION = 1004;
    public static final int JSON_EXCEPTION = 1005;
    public static final int HTTP_PARSE_EXCEPTION = 1006;
    public static final int HTTP_CLIENT_PROTOCOL_EXCEPTION = 1007;
    /**
     * sp文件的名字.
     */
    public static final String APP_SHARED_PREFERENCE = "idealsee_ar_sp";

    /**
     * Unity downloaded在sdcard上的根目录.
     */
    public static String UNITY_RESOURCES_DIRETCTORY = APP_PARENT_PATH + File.separator + "Resources";

    /**
     * 程序在sdcard上的根目录.
     */
    public static String APP_ROOT_DIRETCORY = APP_PARENT_PATH + File.separator + "isarsdk";

    /**
     * log的存放路径.
     */
    public static String APP_LOG_DIRETCORY = APP_ROOT_DIRETCORY + File.separator + "log";

    /**
     * scan error log 的存放路径.
     */
    public static String APP_SCAN_ERROR_LOG_DIRETCORY = APP_ROOT_DIRETCORY + File.separator + "error";

    /**
     * crash的存放路径.
     */
    public static String APP_CRASH_DIRETCORY = APP_LOG_DIRETCORY + File.separator + "crash";

    /**
     * 图片缓存的存放路径.
     */
    public static String APP_CACHE_DIRECTORY = APP_ROOT_DIRETCORY + File.separator + "cache";

    /**
     * bitmap存放路径,不要清除此文件夹.
     */
    public static String APP_IMAGE_DIRECTORY = APP_ROOT_DIRETCORY + File.separator + "image";

    /**
     * tracker_data的存放路径.
     */
    public static String APP_TRAC_DIRETCORY = APP_ROOT_DIRETCORY + File.separator + "tracker";

    /**
     * 视频缓存的存放路径.
     */
    public static String APP_VIDEO_DIRECTORY = APP_ROOT_DIRETCORY + File.separator + "video";

    /**
     * Unity屏幕录像需要的文件路径
     */
    public static String ISARSDK_RECORD_AR_DIRECTORY = APP_ROOT_DIRETCORY + File.separator + "recorder";

    public static String APP_BEHAVIOR_LOG_FILE = APP_LOG_DIRETCORY + File.separator + "user_behavior.txt";

    public static String USER_AGENT = "";

    /**
     * 动画信息etag.
     */
    public static final String APP_ETAG_PREFERENCE = "idealsee_ar_etag";
    public static final String APP_LOGIN_NAME_PREFERENCE = "login_name";
    public static final String APP_LOGIN_ICON_PREFERENCE = "login_icon";
    public static final String APP_LOGIN_RESULT_PREFERENCE = "login_result";
    // ar音效
    public static final String APP_STATE_USE_AR_AUDIO_EFFECT = "app_state_use_ar_audio_effect";

    public static String APP_LAST_RANDOM_CACHE = APP_CACHE_DIRECTORY + File.separator + "last_random.txt";
    public static String APP_LAST_FAVORITE_CACHE = APP_CACHE_DIRECTORY + File.separator + "last_favorite.txt";
    public static String APP_LAST_SEARCH_CACHE = APP_CACHE_DIRECTORY + File.separator + "last_search.txt";
    public static String APP_LAST_TIP_IMAGE_CACHE = APP_CACHE_DIRECTORY + File.separator + "last_tip_image.txt";

    public static String SDK_VERSION = "3.30";

    public static void updateConstants() {
        USER_AGENT = "ISARSDK" + " " + SDK_VERSION + " ("
                + Build.MODEL + "; Android " + Build.VERSION.RELEASE + "; " + Locale.getDefault().toString() + ")";
        APP_ROOT_DIRETCORY = APP_PARENT_PATH + File.separator + "isarsdk";
        APP_LOG_DIRETCORY = APP_ROOT_DIRETCORY + File.separator + "log";
        APP_SCAN_ERROR_LOG_DIRETCORY = APP_ROOT_DIRETCORY + File.separator + "error";
        APP_CRASH_DIRETCORY = APP_LOG_DIRETCORY + File.separator + "crash";
        APP_CACHE_DIRECTORY = APP_ROOT_DIRETCORY + File.separator + "cache";
        APP_IMAGE_DIRECTORY = APP_ROOT_DIRETCORY + File.separator + "image";
        APP_TRAC_DIRETCORY = APP_ROOT_DIRETCORY + File.separator + "tracker";
        APP_VIDEO_DIRECTORY = APP_ROOT_DIRETCORY + File.separator + "video";
        ISARSDK_RECORD_AR_DIRECTORY = APP_ROOT_DIRETCORY + File.separator + "recorder";

        APP_BEHAVIOR_LOG_FILE = APP_ROOT_DIRETCORY + File.separator + "log" + File.separator + "user_behavior.txt";
        APP_LAST_RANDOM_CACHE = APP_CACHE_DIRECTORY + File.separator + "last_random.txt";
        APP_LAST_FAVORITE_CACHE = APP_CACHE_DIRECTORY + File.separator + "last_favorite.txt";
        APP_LAST_SEARCH_CACHE = APP_CACHE_DIRECTORY + File.separator + "last_search.txt";
        APP_LAST_TIP_IMAGE_CACHE = APP_CACHE_DIRECTORY + File.separator + "last_tip_image.txt";
    }

    public static final String GAME_SIGN_KEY = "YXBwIGtleSBmb3IgeWl4dW4uYXJoaWVhc29uLmNvbSAmJiBjb21tZXJjaWFsIGJhbmsgb2YgbHV6aG91LCBjcmVhdGVkIGJ5IHpoYW5nbWluQGlkZWFsc2VlLmNuLCAyMDE3MDUwNg==";

    //todo 临时增加
    /**
     * 保存屏幕录像到相册目标路径
     */
    /**
     * APP DCIM folder.
     */
    public static String APP_DCIM_DIRECTORY = Environment.getExternalStorageDirectory().getPath() + File.separator
            + "DCIM/arhieason";
    public static String APP_SCREEN_VIDEO_DIRECTORY = APP_DCIM_DIRECTORY + File.separator + "videos";
}
