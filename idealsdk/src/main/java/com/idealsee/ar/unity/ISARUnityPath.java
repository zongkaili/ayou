package com.idealsee.ar.unity;

import com.idealsee.sdk.util.ISARConstants;
import com.idealsee.sdk.util.ISARStringUtil;

import java.io.File;

/**
 * Created by yaolei on 17-9-22.
 */

public class ISARUnityPath {

    public static String getLocalVideoPath(String fileName) {
        String name;
        String md5 = ISARStringUtil.getMD5(fileName);
        if (fileName.endsWith(".m3u8")) {
            name = md5 + ".m3u8";
        } else {
            name = md5 + ".mp4";
        }
        return ISARConstants.APP_VIDEO_DIRECTORY + File.separator + name;
    }
}
