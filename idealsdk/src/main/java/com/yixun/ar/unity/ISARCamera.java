package com.yixun.ar.unity;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.yixun.sdk.util.ISARConstants;
import com.yixun.sdk.util.Logger;
import com.yixun.sdk.util.ISARNativeTrackUtil;
import com.unity3d.player.UnityPlayer;

/**
 * CameraInterface is used to handle camera interface. Encapsulate method of Camera, and do some
 * work for business.
 *
 * @author hongen
 */

public class ISARCamera implements PreviewCallback {
    private static final String TAG = "camera_android";
    public static ISARCamera sISARCamera;
    private Camera mCamera;
    private Camera.Parameters mParams;
    private SurfaceTexture mSurfaceTexture;

    private float mCameraDefaultWidth = 640.0f;
    private float mCameraDefaultHeight = 480.0f;
    private int mCameraWidth;
    private int mCameraHeight;
    private static int mDataLengthY; // length of y
    private int mDataLengthUV; // length of uv
    private int mTextureY = 0;
    private int mTextureUV = 0;
    private int mCurCamera = 0; // front or background.

    private boolean mIsCapturing = false;
    //    private boolean mIsStartCalled = false;
    private static boolean mIsPreviewed = false;
    private byte[] mPreviewBuffer = null;
    private static float[] mPosition = new float[3];
    private static float[] mOrientation = new float[4];
    private float[] mProjectionMatrix = new float[16];

    // store yuv data for y and uv
    private static ByteBuffer mBufferY;
    private static ByteBuffer mBufferUV;
    private static byte[] mYData;
    private static byte[] mUVData;
    private static byte[] mTmpYData;
    private static boolean mIsStopedByApp = false;
    private volatile boolean mNeedTracking = false;

    private ISARCamera() {
        Logger.LOGI(TAG + " camera construct");
        if (mTextureY <= 0) {
            int[] tempTextures = new int[2];
            GLES20.glGenTextures(2, tempTextures, 0);
            mTextureY = tempTextures[0];
            mTextureUV = tempTextures[1];
            Logger.LOGD(TAG + " Construct " + mTextureY + "," + mTextureUV);
        }
    }

    /**
     * keep camera instance unique.
     *
     * @return CameraInterface
     */
    public static synchronized ISARCamera getInstance() {
        if (null == sISARCamera) {
            sISARCamera = new ISARCamera();
        }
        Logger.LOGD(TAG + " getInstance");
        return sISARCamera;
    }

    /**
     * open camera
     *
     * @param camera 0 is background
     * @return false camera error
     */
    public synchronized int arcameraInitCamera(int camera) {
        try {
            GLES20.glDeleteTextures(2, new int[]{mTextureY, mTextureUV}, 0);
            int[] tempTextures = new int[2];
            GLES20.glGenTextures(2, tempTextures, 0);
            mTextureY = tempTextures[0];
            mTextureUV = tempTextures[1];
            if (null != mCamera) {
                Logger.LOGW(TAG + " arcameraInitCamera camera has opened-----------: " + camera);
                return 1;
            }
            Logger.LOGD(TAG + " arcameraInitCamera " + mTextureY + "," + mTextureUV);
            mCurCamera = camera;
            mCamera = Camera.open(camera);
            mParams = mCamera.getParameters();
            doInitPreviewParams((int) mCameraDefaultWidth, (int) mCameraDefaultHeight, camera);
            ISARNativeTrackUtil.initNative((int) mCameraDefaultWidth, (int) mCameraDefaultHeight);
            // HsNativeUtil.nAugmentedLoadFile("/sdcard/DCIM/ee.dat");
            Logger.LOGI(TAG + " arcameraInitCamera ok " + camera);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 1;
    }

    // need call this before start preview
    private synchronized boolean doInitPreviewParams(int width, int height, int camera) {
        if (mCamera != null) {
            float previewRate = (width > height) ? (((float) width) / ((float) height))
                    : (((float) height) / ((float) width));
            int previewWidth = (width < height) ? height : width;
            Size previewSize = ISARCameraParamUtil.getInstance().getPropPreviewSize(mParams.getSupportedPreviewSizes(),
                    previewRate, previewWidth);
            mParams.setPreviewSize(previewSize.width, previewSize.height);
            mParams.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            mParams.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            // 前置摄像头不支持focus mode
            if (camera == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            mParams.setPreviewFormat(ImageFormat.NV21);
            mCamera.setParameters(mParams);

            // mCamera.setDisplayOrientation(90);
            mSurfaceTexture = new SurfaceTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
            /*
             * int bufSize = mCameraWidth * mCameraHeight * 3 / 2; mPreviewBuffer = new
             * byte[bufSize]; mCamera.addCallbackBuffer(mPreviewBuffer);
             * mCamera.setPreviewCallbackWithBuffer(this);
             */
            try {
                mCamera.setPreviewTexture(mSurfaceTexture);
                Logger.LOGD(TAG + " preview format:" + mCamera.getParameters().getPictureFormat());
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCameraWidth = mParams.getPreviewSize().width;
            mCameraHeight = mParams.getPreviewSize().height;
            Logger.LOGD(TAG + " camerawidth: " + mCameraWidth + "," + mCameraHeight);
            int dataSize = mCameraWidth * mCameraHeight;
            int bufSize = dataSize * 3 / 2;
            mPreviewBuffer = new byte[bufSize];
            mDataLengthY = dataSize;
            mDataLengthUV = dataSize / 2;
            mBufferY = ByteBuffer.allocateDirect(mDataLengthY).order(ByteOrder.nativeOrder());
            mBufferUV = ByteBuffer.allocateDirect(mDataLengthUV).order(ByteOrder.nativeOrder());
            mYData = new byte[mDataLengthY];
            mTmpYData = new byte[mDataLengthY];
            mUVData = new byte[mDataLengthUV];
        }
        return true;
    }

    /**
     * unity callback to open or close flash.
     *
     * @param on 1 open flash, else close flash
     * @return 1 success
     */
    public synchronized int arcameraSetFlashTorchMode(int on) {
        Logger.LOGD(TAG + " arcameraOpenFlashTorchMode " + on);
        if (mCamera != null) {
            if (1 == on) {
                mParams = mCamera.getParameters();
                mParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(mParams);
                return 1;
            } else {
                mParams = mCamera.getParameters();
                mParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(mParams);
                return 1;
            }
        }
        return -1;
    }

    /**
     * do start preview, and show image to surface
     *
     * @return 1 ok
     */
    public synchronized int arcameraStart() {
        if (mCamera != null) {
            if (mIsPreviewed) {
                Logger.LOGD(TAG + " arcameraStart mIsPrevied------- " + mIsPreviewed);
                return 1;
            }
//            mIsStartCalled = true;
            mCamera.addCallbackBuffer(mPreviewBuffer);
            mCamera.setPreviewCallbackWithBuffer(this);
            try {
                // mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
                mCamera.cancelAutoFocus();
                mParams = mCamera.getParameters();
                Logger.LOGD(TAG + " arcameraStart ok");
                return 1;
            } catch (RuntimeException e1) {
                Logger.LOGE(TAG + " arcameraStart error.");
                e1.printStackTrace();
                int status = retryOpenCamera();
                return status;
            }
        } else {
            Logger.LOGE(TAG + " camera is null");
            return -1;
        }

    }

    public int arcameraStop() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mIsPreviewed = false;
            Logger.LOGI(TAG + " arcameraStop ok");
            return 1;
        }
        return -1;
    }

    /**
     * Deinit camera
     *
     * @return 0 is ok
     */
    public int arcameraDeinitCamera() {
        if (null != mCamera) {
            Logger.LOGI(TAG + " arcameraDeinitCamera start");
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mIsPreviewed = false;
            mCamera.release();
            mCamera = null;
            GLES20.glDeleteTextures(2, new int[]{mTextureY, mTextureUV}, 0);
            mTextureY = 0;
            mTextureUV = 0;
            ISARNativeTrackUtil.deInitNative();
            Logger.LOGI(TAG + " arcameraDeinitCamera ok");
            return 1;
        }
        return -1;
    }

    // call back to control sound
    ShutterCallback mShutterCallback = new ShutterCallback() {

        @Override
        public void onShutter() {
            Logger.LOGD(TAG + " onShutter");
        }
    };

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // Logger.LOGD(TAG + " onPreviewFrame ");
        camera.addCallbackBuffer(mPreviewBuffer);

        synchronized (mBufferY) {
            if (mIsCapturing) {
                Logger.LOGD(TAG + " onPreviewFrame mIsCapturing " + mIsCapturing);
                return;
            }
            System.arraycopy(data, 0, mYData, 0, mDataLengthY);
            System.arraycopy(data, mDataLengthY, mUVData, 0, mDataLengthUV);
            mBufferY.clear();
            mBufferY.put(mYData).position(0);
            mBufferUV.clear();
            mBufferUV.put(mUVData).position(0);
            mIsPreviewed = true;
//            Logger.LOGD(TAG + " onPreviewFrame mIsPreViewed");
        }
    }

    public synchronized int arcameraUpdateCameraTexture_y() {
        // int[] tempTextures = new int[1];
        // GLES20.glGenTextures(1, tempTextures,0);
        // int tempTexture = tempTextures[0];
        // Logger.LOGD(TAG + " arcameraUpdateCameraTexture_y");
        if (!mIsPreviewed || mIsStopedByApp) {
            Logger.LOGW(TAG + " arcameraUpdateCameraTexture_y has not previewed.");
            return 0;
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureY);
        GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
        synchronized (mBufferY) {
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, (int) mCameraWidth, (int) mCameraHeight, 0,
                    GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, mBufferY);
        }
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
//        Logger.LOGD(TAG + " arcameraUpdateCameraTexture_y " + mTextureY + "," + mTextureUV);
        return mTextureY;
    }

    public synchronized int arcameraUpdateCameraTexture_uv() {
        // int[] tempTextures = new int[1];
        // GLES20.glGenTextures(1, tempTextures,0);
        // int tempTexture = tempTextures[0];
        // Logger.LOGD(TAG + " arcameraUpdateCameraTexture_uv");
        if (!mIsPreviewed || mIsStopedByApp) {
            Logger.LOGW(TAG + " arcameraUpdateCameraTexture_uv has not previewed.");
            return 0;
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureUV);
        GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
        synchronized (mBufferY) {
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA, (int) mCameraWidth / 2,
                    (int) mCameraHeight / 2, 0, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, mBufferUV);
        }
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
//        Logger.LOGD(TAG + " arcameraUpdateCameraTexture_uv " + mTextureY + "," + mTextureUV);
        return mTextureUV;
    }

    public float[] getProjectionMatrix(float nearClip, float farClip, int screenOrientation) {
        // Logger.LOGD(TAG + " getProjectionMatrix nearClip=" + nearClip + ",farClip=" + farClip +
        // ",screenOrientation=" + screenOrientation);

//        float cx = mCameraWidth / 2.0f;
//        float cy = mCameraHeight / 2.0f;
//        float cx = 315.29844f;
//        float cy = 254.10528f;
        float cx = (float) mCameraWidth * 0.5f;
        float cy = (float) mCameraHeight * 0.5f;
        float HFOV = 0f;
        float VFOV = 0f;
        if (mCamera != null) {
            HFOV = mCamera.getParameters().getHorizontalViewAngle();
            VFOV = mCamera.getParameters().getVerticalViewAngle();
            Logger.LOGD(TAG + " getProjectionMatrix origin HFOV=" + HFOV + ",VFOV=" + VFOV);

            // 图像叠加不紧密
            if (Math.abs(HFOV - VFOV) > 20) {
                VFOV = Math.min(HFOV, VFOV);
                HFOV = VFOV + 11f;
            }
            // 图像拉伸
            float ratio = VFOV / HFOV;
            if (ratio < 0.7f || ratio > 0.8f) {
                VFOV = HFOV * 0.75f;
            }
//            HFOV = 38.204f;
//            VFOV = 28.9484f;
        }
        Logger.LOGD(TAG + " getProjectionMatrix HFOV=" + HFOV + ",VFOV=" + VFOV);

//        float fx = 649.24193f;
//        float fy = 648.63881f;
//        float fx = (float) Math.abs(mCameraWidth / (2 * Math.tan(HFOV / 180 * Math.PI / 2)));
//        float fy = (float) Math.abs(mCameraHeight / (2 * Math.tan(VFOV / 180 * Math.PI / 2)));
        //fx=532.5694,fy=697.0106,cx=320.0,cy=240.0   HFOV=62.0,VFOV=38.0
        float fx = (float) Math.abs(mCameraWidth / (2 * Math.tan(HFOV / 180 * Math.PI / 2)));
        float fy = (float) Math.abs(mCameraHeight / (2 * Math.tan(VFOV / 180 * Math.PI / 2)));
        Logger.LOGD(TAG + " getProjectionMatrix fx=" + fx + ",fy=" + fy + ",cx=" + cx + ",cy=" + cy);

        if (Camera.CameraInfo.CAMERA_FACING_FRONT == mCurCamera) {
            mProjectionMatrix[5] = -2 * fx / mCameraWidth;
        } else {
            mProjectionMatrix[5] = 2 * fx / mCameraWidth;
        }
        mProjectionMatrix[1] = 0.0f;
        mProjectionMatrix[2] = 0.0f;
        mProjectionMatrix[3] = 0.0f;

        mProjectionMatrix[4] = 0.0f;
        if (Camera.CameraInfo.CAMERA_FACING_FRONT == mCurCamera) {
            mProjectionMatrix[0] = -2 * fy / mCameraHeight;
        } else {
            mProjectionMatrix[0] = 2 * fy / mCameraHeight;
        }
        mProjectionMatrix[6] = 0.0f;
        mProjectionMatrix[7] = 0.0f;

        mProjectionMatrix[9] = 1.0f - 2 * cx / mCameraWidth;
        mProjectionMatrix[8] = 2 * cy / mCameraHeight - 1.0f;
        mProjectionMatrix[10] = -(farClip + nearClip) / (farClip - nearClip);
        mProjectionMatrix[11] = -1.0f;

        mProjectionMatrix[12] = 0.0f;
        mProjectionMatrix[13] = 0.0f;
        mProjectionMatrix[14] = -2.0f * farClip * nearClip / (farClip - nearClip);
        mProjectionMatrix[15] = 0.0f;

        ISARNativeTrackUtil.nAugmentedInit(fx, fy, cx, cy);
        return mProjectionMatrix;
    }

    public int getStatus() {
        // Logger.LOGD(TAG + " getStatus ");
        synchronized (mBufferY) {
//            Logger.LOGD(TAG + " getStatus " + mNeedTracking);
            if (null != mYData && 0 != mYData.length) {
                int status = ISARNativeTrackUtil.nAugmentedRun(mYData);
                if (status == 0) {
                    ISARNativeTrackUtil.nAugmentedGetPoseForUnity(1);
                    return 0;
                } else {
                    // Logger.LOGD(TAG + " getStatus " + status);
                }
            }
        }
        return -1;
    }

    /**
     * called from unity.
     *
     * @return
     */
    public float[] getPosition() {
//         Logger.LOGD(TAG + " getPosition mPosition[0]=" + mPosition[0] + ",mPosition[1]=" + mPosition[1] + ",mPosition[2]=" + mPosition[2]);
        return mPosition;
    }

    /**
     * called from unity.
     *
     * @return
     */
    public float[] getOrientation() {
//         Logger.LOGD(TAG + " getPosition mOrientation[0]=" + mOrientation[0] + ",mOrientation[1]=" + mOrientation[1] + ",mOrientation[2]=" + mOrientation[2] + ",mOrientation[3]="+ mOrientation[3]);
        return mOrientation;
    }

    public boolean loadARTemplate(String filePath) {
        Logger.LOGD(TAG + " loadARTemplate " + filePath);
        // AugmentedReality2D_loadFile
        int status = ISARNativeTrackUtil.nAugmentedLoadFile(filePath);
        Logger.LOGD(TAG + " loadARTemplate " + filePath + ",status=" + status);
        UnityPlayer.UnitySendMessage("ISARCamera", "StartAR", "");
        return true;
    }

    /**
     * update position from native.
     */
    public static void updatePosition(float x, float y, float z) {
        // Logger.LOGD(TAG + " updatePosition x=" + x);
        mPosition[0] = x;
        mPosition[1] = y;
        mPosition[2] = z;
    }

    /**
     * update orientation from native.
     */
    public static void updateOrientation(float f1, float f2, float f3, float f4) {
//         Logger.LOGD(TAG + " updateOrientation f1=" + f1);
        mOrientation[0] = f1;
        mOrientation[1] = f2;
        mOrientation[2] = f3;
        mOrientation[3] = f4;
    }

    public void putYUVData(byte[] buffer, int length) {
        // if (YUVQueue.size() >= 10) {
        // YUVQueue.poll();
        // }
        // YUVQueue.add(buffer);
    }

    public String startCapturing() {
        String result = "";
        mIsCapturing = true;
        // byte[] yData = new byte[mDataLengthY];
        // synchronized (mBufferY) {
        // // Logger.LOGD(TAG + " startCapturing ylengt = " + mBufferY.capacity());
        // ByteBuffer y = mBufferY;
        // y.get(yData);
        // }
        // Logger.LOGD(TAG + " startCapturing yData[10]=" + yData[10]);
        String filePath = ISARConstants.APP_CACHE_DIRECTORY + File.separator + "camera.jpg";
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        if (null != mYData && 0 != mYData.length && mIsPreviewed) {
            System.arraycopy(mYData, 0, mTmpYData, 0, mDataLengthY);
            ISARNativeTrackUtil.saveYPicture(mYData, filePath);
            result = filePath;
        }
        mIsCapturing = false;
        return result;
    }

    public int localRecognition(String[] pathArray) {
        synchronized (this) {
            int status = 0;
            try {
                status = ISARNativeTrackUtil.localRecognition(mYData, pathArray);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return status;
        }
    }

    public String localRecognition2() {
        byte[] data = new byte[mDataLengthY];
        synchronized (this) {
            System.arraycopy(mYData, 0, data, 0, mDataLengthY);
        }
        String result = null;
        try {
            result = ISARNativeTrackUtil.localRecognition2(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void startAR(boolean start) {
        /*if (start) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Logger.LOGD(TAG + " startAR sleep");
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Logger.LOGD(TAG + " startAR sleep end");
                    mNeedTracking = true;
                }
            }).start();
        } else {
            mNeedTracking = false;
        }*/
        Logger.LOGD(TAG + " startAR " + start);
    }

    /**
     * open flash
     */
    public void openFlash() {
        Logger.LOGD(TAG + " openFlash");
        if (null != mCamera) {
            mParams = mCamera.getParameters();
            mParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(mParams);
        }
    }

    /**
     * close flash
     */
    public void closeFlash() {
        Logger.LOGD(TAG + " closeFlash");
        if (null != mCamera) {
            mParams = mCamera.getParameters();
            mParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(mParams);
        }
    }

    public int arcameraSetFocusMode(int focusMode) {
        mCamera.cancelAutoFocus();
        mParams = mCamera.getParameters();
        mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
        mCamera.setParameters(mParams);
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    mParams = mCamera.getParameters();
                    mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    mCamera.setParameters(mParams);
                }
            }
        });
        return 1;
    }

    /**
     * called from app.
     * set camera state.
     *
     * @param stop if true, camera will not be update by unity.
     */
    public void updateAppCamera(boolean stop) {
        mIsStopedByApp = stop;
    }

    // workaround for camera open failed.
    private int retryOpenCamera() {
        arcameraDeinitCamera();
        arcameraInitCamera(mCurCamera);
        if (mCamera != null) {
            if (mIsPreviewed) {
                Logger.LOGD(TAG + " retryOpenCamera arcameraStart mIsPrevied " + mIsPreviewed);
                return 1;
            }
            mCamera.addCallbackBuffer(mPreviewBuffer);
            mCamera.setPreviewCallbackWithBuffer(this);
            try {
                // mCamera.setPreviewDisplay(holder);
                // Logger.LOGD(TAG + " retryOpenCamera arcameraStart start");
                mCamera.startPreview();
                mCamera.cancelAutoFocus();
                mParams = mCamera.getParameters();
                Logger.LOGD(TAG + " retryOpenCamera arcameraStart ok");
                return 1;
            } catch (RuntimeException e1) {
                Logger.LOGE(TAG + " retryOpenCamera arcameraStart error.");
                e1.printStackTrace();
            }

        } else {
            Logger.LOGW(TAG + " retryOpenCamera camera is null");
            return -1;
        }
        return -1;
    }

    public void setDatPath(String[] pathArray) {
        Logger.LOGD(TAG + " setDatPath ");
        ISARNativeTrackUtil.setDatPath(pathArray);
    }
}
