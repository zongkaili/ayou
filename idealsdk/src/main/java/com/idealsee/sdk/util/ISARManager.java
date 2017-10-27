package com.idealsee.sdk.util;

import com.idealsee.ar.unity.ISARUnityMessageManager;

import java.io.File;

public class ISARManager {
    private static final String TAG = ISARManager.class.getSimpleName();

    /**
     * 设置识别文件(.dat文件)路径数组
     *
     * @param pathArray
     */
    public static void setupLocalRecognitionDats(String[] pathArray) {
        Logger.LOGD(TAG + " setupLocalRecognitionDats pathArray = " + pathArray);
        ISARNativeTrackUtil.setDatPath(pathArray);
    }

    /**
     * 读取该文件夹下txt动画数据，并加载
     *
     * @param path
     */
    public static void loadLocalRecognitionResource(String path) {
        File file = new File(path);
        if (!file.exists() || !ISARSdCardUtil.checkSdCardExist()) {
            return;
        }
        String txtPath = "";
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().endsWith("android.txt")) {
                    txtPath = files[i].getAbsolutePath();
                }
            }
        }
        File txtFile = new File(txtPath);
        if (!txtFile.exists()) {
            return;
        }
        String animationData = ISARFilesUtil.getTxtFileContent(txtFile);
        Logger.LOGD(TAG + " loadLocalRecognitionResource animationData : " + animationData);
        ISARUnityMessageManager.setThemeResourceFolder(path);
        ISARUnityMessageManager.loadThemeData(animationData);
    }
}
