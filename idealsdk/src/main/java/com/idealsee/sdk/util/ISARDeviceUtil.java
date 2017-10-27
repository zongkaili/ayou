/**
 * 项目名称: IDSeeAR 
 * 类名称:  DeviceUtil 
 * 类描述: 
 * 创建人: Ly
 * 创建时间: 2013-1-25 下午2:04:30  
 * 修改人:
 * 修改时间: 
 * 备注: 
 * @version 
 * 
 */

package com.idealsee.sdk.util;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Get device info.
 * @author Ly
 * 
 */
public class ISARDeviceUtil {
    /**
     * 唯一的设备ID: GSM手机的 IMEI 和 CDMA手机的 MEID.
     * 
     * @return device id
     */
    public static String getDeviceId(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        try {
            return tm.getDeviceId();
        }catch (Exception e){
            e.printStackTrace();
            Logger.LOGE("please open the READ_PHONE_STATE permission in SettingS");
        }
        return null;
    }

    /**
     * 获取手机号码.
     * 
     * @return local number
     */
    public static String getLocalNumber(Context context) {
        TelephonyManager tManager = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        String number = tManager.getLine1Number();
        return number;
    }

    /**
     * 获取androidId.
     * 
     * @return android id
     */
    public static String getAndroidId(Context context) {
        String id = android.provider.Settings.Secure.getString(context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        return id;
    }
}
