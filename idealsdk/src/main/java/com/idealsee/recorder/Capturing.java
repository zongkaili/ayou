/**
 * 
 */
package com.idealsee.recorder;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.idealsee.sdk.util.Logger;
import com.idealsee.sdk.util.ISARConstants;

import org.m4m.IProgressListener;
import org.m4m.android.graphics.FullFrameTexture;

import java.io.File;
import java.io.IOException;

/**
 * AR录制屏幕入口，建立录制功能与Unity的连接。
 * Unity访问此类中的方法，实现录制功能，并将状态通过Activity传给客户端。
 * @author hongen
 * 
 */
public class Capturing {
    private static final String TAG = "Capturing";

    private static FullFrameTexture texture;

    private VideoCapture videoCapture;
    private int mScreenRecordWidth = 720;
    private int mScreenRecordHeight = 0;

    private int videoWidth = 0;
    private int videoHeight = 0;
    private int videoFrameRate = 0;

    private long nextCaptureTime = 0;
    private long startTime = 0;

    private static Capturing instance = null;

    private SharedContext sharedContext = null;
    private EncodeThread encodeThread = null;
    private boolean isRunning = false;
    private boolean isStoppedPre = false;

    private IProgressListener progressListener = new IProgressListener() {
        @Override
        public void onMediaStart() {
            synchronized (videoCapture) {
                startTime = System.nanoTime();
                nextCaptureTime = 0;
                encodeThread.start();
            }
        }

        @Override
        public void onMediaProgress(float progress) {
        }

        @Override
        public void onMediaDone() {
            synchronized (videoCapture) {
                if (isStoppedPre) {
                    Log.w(TAG, "onMediaStart stop called before, need stop manual.");
                    isStoppedPre = false;
                    encodeThread.queryStop();
                    Log.w(TAG, "onMediaStart stop called before, need stop manual end.");
                    return;
                }
                isRunning = true;
            }
        }

        @Override
        public void onMediaPause() {
            
        }

        @Override
        public void onMediaStop() {
            Log.i(TAG, "onMediaStop.");
            isRunning = false;
        }

        @Override
        public void onError(Exception exception) {
        }
    };

    private class EncodeThread extends Thread {
        private static final String TAG = "EncodeThread";

        private SharedContext sharedContext;
        private boolean isStopped = false;
        private int textureID;
        private boolean newFrameIsAvailable = true;

        EncodeThread(SharedContext sharedContext) {
            super();
            this.sharedContext = sharedContext;
        }

        @Override
        public void run() {
            Logger.LOGD(TAG + " run");
            do {
                if (newFrameIsAvailable) {
                    synchronized (videoCapture) {
                        sharedContext.makeCurrent();
                        videoCapture.beginCaptureFrame();
                        GLES20.glViewport(0, 0, videoWidth, videoHeight);
                        texture.draw(textureID);
                        videoCapture.endCaptureFrame();
                        newFrameIsAvailable = false;
                        sharedContext.doneCurrent();
                    }
                }
            } while (!isStopped);
            isStopped = false;
            synchronized (videoCapture) {
                Logger.LOGD(TAG + " stop");
                videoCapture.stop();
            }
        }

        public void queryStop() {
            isStopped = true;
        }
        
        public boolean isStop() {
            return isStopped;
        }

        public void pushFrame(int textureID) {
            this.textureID = textureID;
            newFrameIsAvailable = true;
        }
    }

    public Capturing(Context context, int width, int height) {
        Logger.LOGD(TAG + " Capturing construct width=" + width + ",height=" + height);
        videoCapture = new VideoCapture(context, progressListener);

        if (width > mScreenRecordWidth && width != 0) {
            mScreenRecordHeight = height * mScreenRecordWidth / width;
        } else {
            mScreenRecordWidth = width;
            mScreenRecordHeight = height;
        }
        Logger.LOGD(TAG + " Capturing mScreenRecordWidth " + mScreenRecordWidth + ",mScreenRecordHeight=" + mScreenRecordHeight);

        texture = new FullFrameTexture();
        Logger.LOGD(TAG + " Capturing construct FullFrameTexture ok");
        sharedContext = new SharedContext();
        Logger.LOGD(TAG + " Capturing construct sharedContext ok");
        instance = this;
        Logger.LOGD(TAG + " Capturing construct ok");
    }

    public static Capturing getInstance() {
        return instance;
    }

    public static String getDirectoryDCIM() {
        Logger.LOGD(TAG + " getDirectoryDCIM " + ISARConstants.ISARSDK_RECORD_AR_DIRECTORY);
        return ISARConstants.ISARSDK_RECORD_AR_DIRECTORY + File.separator;
    }

    public synchronized void initCapturing(int width, int height, int frameRate, int bitRate) {
        Logger.LOGD(TAG + " initCapturing: " + width + "x" + height + ", " + frameRate + ", " + bitRate);
        if (isRunning || isStoppedPre) {
            Logger.LOGW(TAG + " initCapturing is recording isRunning=" + isRunning + ",isStoppedPre=" + isStoppedPre);
            return;
        }
        videoFrameRate = frameRate;
        VideoCapture.init(mScreenRecordWidth, mScreenRecordHeight, frameRate, bitRate);
        videoWidth = mScreenRecordWidth;
        videoHeight = mScreenRecordHeight;
        // videoWidth = width;
        // videoHeight = height;

        encodeThread = new EncodeThread(sharedContext);
    }

    public void startCapturing(final String videoPath) {
        String vPath = getDirectoryDCIM() + "v_" + System.currentTimeMillis() + ".mp4";
        if (videoCapture == null) {
            Logger.LOGW(TAG + " startCapturing videoCapture is null");
            return;
        }
        if (isRunning || isStoppedPre) {
            Logger.LOGW(TAG + " startCapturing is isRunning");
            return;
        }

        // (new Thread() {
        // public void run() {
        Logger.LOGD(TAG + " --- startCapturing:" + vPath);
        synchronized (videoCapture) {
            try {
                isStoppedPre = false;
                videoCapture.start(vPath);
            } catch (IOException e) {
                Logger.LOGE(TAG + " --- startCapturing error");
            }
        }
        // }
        // }).start();
    }

    public void captureFrame(int textureID) {
        Logger.LOGD(TAG + " Catpuring captrueFrame " + textureID);
        encodeThread.pushFrame(textureID);
    }

    public void stopCapturing() {
        Logger.LOGD(TAG + " --- stopCapturing");
        synchronized (videoCapture) {
            if (!isRunning) {
                Logger.LOGW(TAG + " stopCapturing has not call back started, need call stop manual.");
                isStoppedPre = true;
                return;
            }
        }
        encodeThread.queryStop();
    }

    public boolean isRunning() {
        return isRunning;
    }
}
