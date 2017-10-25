/**
 * 项目名称: IDSeeAR
 * 类名称:  NetUtil
 * 类描述:
 * 创建人: Ly
 * 创建时间: 2013-2-6 上午11:25:51
 * 修改人:
 * 修改时间:
 * 备注:
 *
 * @version
 */

package com.yixun.sdk.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.yixun.sdk.server.ISARHttpServerURL;

/**
 * net work utility.
 *
 * @author idealsee
 */
public class ISARNetUtil {
    public static final String TAG = "===NetUtil===";
    public static final int NET_TYPE_WIFI = 0; // 网络连接类型:wifi
    public static final int NET_TYPE_MOBILE = 10; // 网络连接类型:2g/3g
    public static final int NET_TYPE_MOBILE_GPRS = 11; // 移动2g
    public static final int NET_TYPE_MOBILE_CDMA = 12; // 联通2g
    public static final int NET_TYPE_MOBILE_EDGE = 13; // 电信2g

    private static final String TEST_URL = ISARConstants.QINIU_URL_INNER;
    // app下载文件使用yxfile，此路径已经做了qiniu跳转.
    private static final String RELEASE_URL = ISARConstants.BASE_URL_FOR_UNITY_RELEASE;
    private static final String MEDIA_PREFIX = "/media01/";
    private static final String MEDIA_SUFFIX = "-full.mp4";
    private static String URL = RELEASE_URL;

//    static {
//        if (Logger.getLogLevel() == Logger.LOG_LEVEL_RELEASE) {
//            URL = RELEASE_URL;
//        } else {
//            URL = INNER_URL;
//        }
//        Logger.LOGI("QINIU:" + URL);
//    }

    private ISARNetUtil() {

    }

    /**
     * 判断网络是否可用.
     *
     * @return true or false
     */
    public static boolean isNetworkConnected(Context context) {
        try {
            // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                // 获取网络连接管理的对象
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null) {
                    // 返回当前网络是否可用
                    return info.isConnected();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    /**
     * 获取网络类型.
     *
     * @param context context
     * @return net work type
     */
    public static int getNetworkType(Context context) {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isAvailable()) {
                    int type = info.getType();
                    if (type == ConnectivityManager.TYPE_MOBILE) {
                        int subtype = info.getSubtype();
                        if (subtype == TelephonyManager.NETWORK_TYPE_GPRS) {
                            return NET_TYPE_MOBILE_GPRS;
                        } else if (subtype == TelephonyManager.NETWORK_TYPE_CDMA) {
                            return NET_TYPE_MOBILE_CDMA;
                        } else if (subtype == TelephonyManager.NETWORK_TYPE_EDGE) {
                            return NET_TYPE_MOBILE_EDGE;
                        } else if (subtype == TelephonyManager.NETWORK_TYPE_UNKNOWN) {
                            return NET_TYPE_MOBILE;
                        }
                    } else if (type == ConnectivityManager.TYPE_WIFI) {
                        return NET_TYPE_WIFI;
                    } else {
                        return -1;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return -1;
    }

    /**
     * check if WIFI connected.
     *
     * @return true or false
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWiFiNetworkInfo != null) {
            // WifiManager manager = (WifiManager)
            // IdealseeApplication.mContext.getSystemService(Context.WIFI_SERVICE);
            // WifiInfo wifiInfo = manager.getConnectionInfo();
            return mWiFiNetworkInfo.isConnected();
        }
        return false;
    }

    /**
     * using md5 generate image real path.
     *
     * @param md5 return the origin file url
     */
    public static String getUrlFromMD5(String md5) {
        // https://yixunfiles-ali.yixun.arhieason.com/MD5.FORMAT?x-oss-process=image/format,png
        // https://7xofx2.com2.z0.glb.qiniucdn.com/MD5.FORMAT?imageView2/0/format/png
        String formatString = md5.substring(md5.indexOf("_") + 1);
        String baseUrl = ISARHttpServerURL.CDN_PIC_FULL;
        String[] stringArray = baseUrl.split("\\?");
        String domainString = stringArray[0].replace("MD5", md5).replace("FORMAT", formatString);
        String argsString = stringArray[1];
        return domainString + "?" + argsString;
    }

    /**
     * @param md5   md5
     * @param width width
     * @return image url
     */
    public static String getUrlFromMD5(String md5, int width) {
        // https://yixunfiles-ali.yixun.arhieason.com/MD5.FORMAT?x-oss-process=image/resize,w_SIZE,limit_0,format,png
        // https://7xofx2.com2.z0.glb.qiniucdn.com/MD5.FORMAT?imageView2/0/w/SIZE/h/0/format/png
        String formatString = md5.substring(md5.indexOf("_") + 1);
        String baseUrl = ISARHttpServerURL.CDN_PIC;
        String[] stringArray = baseUrl.split("\\?");
        String domainString = stringArray[0].replace("MD5", md5).replace("FORMAT", formatString);
        String argsString = stringArray[1].replace("SIZE", String.valueOf(width));
        return domainString + "?" + argsString;
    }

    public static String getUrlFromMD5(String md5, int x, int y, int width, int height) {
        // https://yixunfiles-ali.yixun.arhieason.com/MD5.FORMAT?x-oss-process=image/crop,x_IX,y_IY,w_IW,h_IH,format,png
        String formatString = md5.substring(md5.indexOf("_") + 1);
        String baseUrl = ISARHttpServerURL.CDN_RES_CROP;
        String[] stringArray = baseUrl.split("\\?");
        String domainString = stringArray[0].replace("MD5", md5).replace("FORMAT", formatString);
        String argsString = stringArray[1].replace("IX", String.valueOf(x)).replace("IY", String.valueOf(y))
                .replace("IW", String.valueOf(width)).replace("IH", String.valueOf(height));
        return domainString + "?" + argsString;
    }

    public static String getResourceUrlFromMD5(String md5) {
        // https://yixunfiles-ali.yixun.arhieason.com/MD5.FORMAT
        // https://7xofx2.com2.z0.glb.qiniucdn.com/MD5.FORMAT
        String formatString = md5.substring(md5.indexOf("_") + 1);
        String baseUrl = ISARHttpServerURL.CDN_RESOURCE.replace("MD5", md5).replace("FORMAT", formatString);
        return baseUrl;
    }
}
