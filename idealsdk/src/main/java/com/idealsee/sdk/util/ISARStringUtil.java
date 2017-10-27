/**
 * 项目名称: IdealseeAR
 * 类名称:  StringUtil 
 * 类描述: 
 * 创建人: Ly
 * 创建时间: 2013-5-9 上午9:05:02 
 * 修改人: 
 * 修改时间: 
 * 备注: 
 * @version 
 * 
 */

package com.idealsee.sdk.util;

import android.text.TextUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * String util class.
 * @author Ly
 */
public class ISARStringUtil {

    private ISARStringUtil() {
        // 静态的构造函数, 防止工具类被实例化
    }

    /**
     * 将时间转换为分钟:秒.
     * 
     * @param time time.
     * @return string of time.
     */
    public static String millisecondsToString(long time) {
        SimpleDateFormat format = new SimpleDateFormat("mm:ss", Locale.CHINA);
        Date date = new Date(time);
        return format.format(date);
    }

    /**
     * 当前时间转为年-月-日_时钟-分钟-秒.
     * @return string of time.
     */
    public static String currentTimeToString() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSS", Locale.getDefault());
        return formatter.format(new Date());
    }

    /**
     * 邮箱验证.
     * 
     * @param str target mail will be checked.
     * @return true邮箱格式合法false邮箱格式不合法.
     */
    public static boolean checkEmail(String str) {
        if (str != null && str.length() >= 1) {
            String[] msgStrings = str.split(",");
            for (int i = 0; i < msgStrings.length; i++) {
                // String regMsgString = "^[A-Za-z0-9][\\w\\-\\.]{0,63}@([\\w\\-]+\\.)+[\\w]{2,3}$";
                String regMsgString = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
                if (!msgStrings[i].matches(regMsgString)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取字符串的MD5值.
     * @param str string will be calculated.
     * @return md5 of string.
     */
    public static final String getMD5(String str) {
        String md5 = null;
        try {
            MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(str.getBytes());
            md5 = getHashString(mDigest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md5;
    }

    private static String getHashString(MessageDigest digest) {
        StringBuilder builder = new StringBuilder();
        for (byte b : digest.digest()) {
            builder.append(Integer.toHexString((b >> 4) & 0xf));
            builder.append(Integer.toHexString(b & 0xf));
        }
        return builder.toString();
    }

    /**
     *
     * 替换字符串的换行符号"\n"，使用空格代替.
     * @param str 源字符串
     * @return 替换后的图片
     */
    public static final String replaceWithSpace(String str) {
        if (!TextUtils.isEmpty(str)) {
            return str.replace("\n", " ");
        }
        return str;
    }

    /**
     * 判断当前version是更新后的version.
     * @param curVersion
     * @param oldVersion
     * @return 2 is updated， 0 is not
     */
    public static final int getVersionUpdated(String curVersion, String oldVersion) {
        int result = 0;
        //Logger.LOGD("getVersion curVersion=" + curVersion + ",oldVersion=" + oldVersion);
        String[] curVerArray = curVersion.split("[.]");
        String[] oldVerArray = oldVersion.split("[.]");
        int curSize = curVerArray.length;
        int oldSize = oldVerArray.length;
        int size = curSize > oldSize ? oldSize : curSize;
        // System.out.println("getVersion size=" + oldSize + ",cu=" + curSize);
        for (int i = 0; i < size; i++) {
            // System.out.println(i + " curVerArray[i]=" + Integer.valueOf(curVerArray[i]) + ",IoldVerArray[i]=" + Integer.valueOf(oldVerArray[i]));
            if (Integer.valueOf(curVerArray[i]) > Integer.valueOf(oldVerArray[i])) {
                // System.out.println(i + "=1");
                result = 1;
                break;
            }
            if (i == size - 1) {
                if (curSize > oldSize && Integer.valueOf(curVerArray[i]) >= Integer.valueOf(oldVerArray[i])) {
                    result = 1;
                }
            }
        }
        return result;
    }
}
