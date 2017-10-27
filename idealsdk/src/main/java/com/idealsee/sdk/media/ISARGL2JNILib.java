package com.idealsee.sdk.media;

public class ISARGL2JNILib {
    static {
        System.loadLibrary("ISARNatvieModel");
    }

    public static native int initMediaTexture();

    public static native void bindMediaTexture(int mediaTextureID);

    public static native int initFBO(int destTextureID, int videoWidth, int videoHeight);

    public static native void copyTexture(int mediaTextureID, int destTextureID, int fbo, float[] textureMat, int videoWidth, int videoHeight);
    
}
