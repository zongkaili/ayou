package com.idealsee.sdk.util;

public class ISARNativeTrackUtil {
    static {
        System.loadLibrary("ISARNatvieModel");
    }

    public static native void initNative(int cameraWidth, int cameraHeight);
    public static native void deInitNative();
    /**
     * initialize augment.
     * 
     */
    public static native int nAugmentedInit(float fx, float fy, float cx, float cy);

    /**
     * run using y buffer
     */
    public static native int nAugmentedRun(byte[] yBUffer);
    
    public static native void nAugmentedGetPoseForUnity(int status);
    
    /**
     * load template
     */
    public static native int nAugmentedLoadFile(String filePath);
    
    /**
     * save y picture
     * @param data
     * @param filePath
     * @return
     */
    public static native int saveYPicture(byte[] data, String filePath);

    /**
     * 本地识别
     * @param data
     * @param pathArray
     * @return
     */
    public static native int localRecognition(byte[] data, String[] pathArray);

    /**
     * 本地识别　针对直接读取dat数据匹配
     * @param data
     * @return
     */
    public static native String localRecognition2(byte[] data);

    public static native int[] getScreenSize();

    public static native void setDatPath(String[] parthArray);
}
