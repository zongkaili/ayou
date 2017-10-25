package com.yixun.sdk.demo.utils;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by zongkaili on 17-6-5.
 */

public class AssetsFileUtil {
    /**
     * 将assets下的文件放到sd指定目录下
     *
     * @param context    上下文
     * @param assetsPath assets下的路径
     * @param sdCardPath sd卡的路径
     */
    public static void putAssetsFilesToSDCard(Context context, String assetsPath, String sdCardPath) {
        try {
            String mString[] = context.getAssets().list(assetsPath);
            if (mString.length == 0) { // 说明assetsPath为空,或者assetsPath是一个文件
                InputStream mIs = context.getAssets().open(assetsPath);
                byte[] mByte = new byte[1024];
                int bt = 0;
                File file = new File(sdCardPath + File.separator
                        + assetsPath.substring(assetsPath.lastIndexOf('/')));
                if (!file.exists()) {
                    file.createNewFile();
                } else {
                    return;
                }
                FileOutputStream fos = new FileOutputStream(file);
                while ((bt = mIs.read(mByte)) != -1) {
                    fos.write(mByte, 0, bt);
                }
                fos.flush();
                mIs.close();
                fos.close();
            } else { // 文件夹
                sdCardPath = sdCardPath + File.separator + assetsPath.substring(assetsPath.lastIndexOf('/'));
                File file = new File(sdCardPath);
                if (!file.exists())
                    file.mkdirs();
                for (String stringFile : mString) {
                    putAssetsFilesToSDCard(context, assetsPath + File.separator
                            + stringFile, sdCardPath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
