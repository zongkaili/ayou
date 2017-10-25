/**
 * 项目名称: IDSeeAR 
 * 类名称:  SdCardUtil 
 * 类描述: 
 * 创建人: Ly
 * 创建时间: 2013-1-24 上午11:32:42  
 * 修改人:
 * 修改时间: 
 * 备注: 
 * @version 
 * 
 */

package com.yixun.sdk.util;

import android.os.Environment;

/**
 * SD card utility.
 * @author Ly
 * 
 */
public class ISARSdCardUtil {
    private ISARSdCardUtil() {

    }

    /**
     * 检查sdcard是否存在.
     * 
     * @return true exist
     */
    public static boolean checkSdCardExist() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }
}
