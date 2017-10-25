package com.yixun.ar.unity;

/**
 *
 */

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.opengl.GLES20;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import com.yixun.sdk.util.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static com.yixun.ar.unity.ISARAlphaVideoPlayerHelper.MEDIA_STATE.UNLOAD;

/**
 * This class is for unity alpha videoplayer.
 */
public class ISARAlphaVideoPlayerHelper {
    private static final String TAG = "AlphaVideoPlayerHelper";

    // private MediaPlayer mMediaPlayer = null;
    private int mCurrentBufferingPercentage = 0;
    private MEDIA_STATE mCurrentState = MEDIA_STATE.NOT_READY;
    private ReentrantLock mMediaPlayerLock = null;
    private Condition mRGBCondition = null;
    private Condition mAlphaCondition = null;
    private String mCachedFilePathRGB;
    private String mCachedFilePathAlpha;
    private int mSdkVersion = 0;

    // unity will call back
    private Activity mParentActivity;
    private int mTextureRGB_Y, mTextureRGB_UV, mTextureAlpha_Y, mTextureAlpha_UV;
    private int mDataLengthY, mDataLengthUV;
    // store yuv data for y and uv
    private ByteBuffer mBufferRGB_Y, mBufferRGB_UV, mBufferAlpha_Y, mBufferAlpha_UV;
    private byte[] mDataRGB_Y, mDataRGB_UV, mDataAlpha_Y, mDataAlpha_UV; // update to unity
    private byte[] mTmpDataRGB_Y, mTmpDataRGB_UV, mTmpDataAlpha_Y, mTmpDataAlpha_UV; // save temp data
    private byte[] mDataRGB, mDataAlpha;

    private MEDIA_STATE mStateRGB = MEDIA_STATE.NOT_READY;
    private MEDIA_STATE mStateAlpha = MEDIA_STATE.NOT_READY;

    private boolean mIsRepeat = false; // loop
    // buffer needed per millisecond
    private int mVideoWidth = -1;
    private int mVideoHeight = -1;
    private int mPlayerDelayPerFrame = 30; // milliseconds

    private RGBPlayerThread mPlayerRGB;
    private AlphaPlayerThread mPlayerAlpha;
    private Thread mPlayerSyncThread;
    private boolean mPlayerDataReady = false;

    private String OUTPUT_DIR = "/sdcard/22/";

    public static enum MEDIA_STATE {
        NOT_READY(0), ERROR(1), READY(2), PLAYING(3), PAUSED(4), STOPPED(5), REACHED_END(6), BUFFERING(-2), IDLE(-3), UNLOAD(-10);
//        REACHED_END(0), PAUSED(1), STOPPED(2), PLAYING(3), READY(4), NOT_READY(5), ERROR(6), LOADING(7);

        private int mType;

        private MEDIA_STATE(int type) {
            this.mType = type;
        }

        public int getNumericType() {
            return this.mType;
        }
    }

    /**
     * Default Constructor. initialize parameters.
     */
    public ISARAlphaVideoPlayerHelper() {
        Logger.LOGD(TAG + " VideoPlayerHelper");
        // this.mMediaPlayer = null;
        this.mPlayerRGB = null;
        this.mCurrentBufferingPercentage = 0;

        this.mCurrentState = MEDIA_STATE.NOT_READY;
        this.mMediaPlayerLock = null;
        this.mParentActivity = null;
        this.mRGBCondition = null;
        this.mAlphaCondition = null;
        this.mSdkVersion = Build.VERSION.SDK_INT;
        Logger.LOGD(TAG + " VideoPlayer mSDKVersion=" + mSdkVersion);
    }

    /**
     * Initializes the VideoPlayerHelper object.
     */
    public int InitAlphaVideoPlayer() {
        Logger.LOGD(TAG + " init");
        mMediaPlayerLock = new ReentrantLock();
        mRGBCondition = mMediaPlayerLock.newCondition();
        mAlphaCondition = mMediaPlayerLock.newCondition();
        return 1;
    }

    /**
     * Deinitializes the VideoPlayerHelper object.
     */
    public boolean DeinitAlphaVideoPlayer() {
        Logger.LOGD(TAG + " deinit");
        unload();
        return true;
    }

    public boolean UnloadAlphaVideoPlayer() {
        unload();
        return true;
    }

    /**
     * Unloads the currently loaded movie After this is called a new load() has
     * to be invoked.
     */
    public boolean unload() {
        Logger.LOGD(TAG + " unload MediaPlayer " + mPlayerRGB);
        mMediaPlayerLock.lock();
        mCurrentState = UNLOAD;
        if (mPlayerSyncThread != null) {
            try {
                mPlayerSyncThread.interrupt();
                mPlayerSyncThread = null;
            } catch (Exception e) {
                mMediaPlayerLock.unlock();
                Logger.LOGE(TAG + " Could not release");
                e.printStackTrace();
            }
        }
        if (mPlayerRGB != null) {
            try {
                // mPlayerRGB.interrupt();
                mRGBCondition.signal();
                mPlayerRGB = null;
            } catch (Exception e) {
                mMediaPlayerLock.unlock();
                Logger.LOGE(TAG + " Could not release");
                e.printStackTrace();
            }
        }
        if (mPlayerAlpha != null) {
            try {
                // mPlayerAlpha.interrupt();
                mAlphaCondition.signal();
                mPlayerAlpha = null;
            } catch (Exception e) {
                mMediaPlayerLock.unlock();
                Logger.LOGE(TAG + " Could not release");
                e.printStackTrace();
            }
        }

        mIsRepeat = false;
        mTmpDataRGB_Y = null;
        mTmpDataRGB_UV = null;
        mTmpDataAlpha_Y = null;
        mTmpDataAlpha_UV = null;
        mDataRGB_Y = null;
        mDataRGB_UV = null;
        mDataAlpha_Y = null;
        mDataAlpha_UV = null;
        mDataRGB = null;
        mDataAlpha = null;
        mAlphaCondition = null;
        mRGBCondition = null;
        mMediaPlayerLock.unlock();

        mCurrentState = UNLOAD;
        return true;
    }

    /**
     * Loads a movie from a file in the assets folder
     */
    public boolean LoadAlphaVideoWithURL(String videoPath, String alphaVideoPath, int isRepeat) {
        Logger.LOGD(TAG + " load:" + "videoPath=" + videoPath + ",alphaVideoPath=" + alphaVideoPath + ",repeat=" + isRepeat);
        // Looper.prepare();
//        mVideoWidth = 640;
//        mVideoHeight = 360;
        mIsRepeat = (isRepeat == 1) ? true : false;
        // If the client requests that we should be able to play ON_TEXTURE,
        boolean result = false;
        mMediaPlayerLock.lock();

        // If the media has already been loaded then exit.
        // The client must first call unload() before calling load again:
        if ((mCurrentState == MEDIA_STATE.READY) && (mPlayerRGB != null)) {
            Logger.LOGD(TAG + " Already loaded");
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                // Logger.LOGD(TAG + " GL2JNILib init mediatexture=" + mMediaTextureID);

                try {
                    // filename = "http://devimages.apple.com/samplecode/adDemo/ad.m3u8";
                    mCurrentState = MEDIA_STATE.NOT_READY;
//                        String localCanPlayName = getLocalCanPlayPath(videoPath);
//                        String localCanPlayAlpha = getLocalCanPlayPath(alphaVideoPath);
                    String localCanPlayName = videoPath;
                    String localCanPlayAlpha = alphaVideoPath;

                    // if alpha video, should check local alpha video exist.
                    if (null != localCanPlayName && null != localCanPlayAlpha) {
                        mCachedFilePathRGB = localCanPlayName;
                        mCachedFilePathAlpha = localCanPlayAlpha;
                        initPlayer();
                        // mHandler.sendEmptyMessage(MSG_ON_PLAYER_PREPARE);
                        // play video
                        mPlayerSyncThread = new Thread() {
                            @Override
                            public void run() {
                                while (!Thread.interrupted()) {
                                    try {
                                        Thread.sleep(mPlayerDelayPerFrame);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    mMediaPlayerLock.lock();
                                    if (mCurrentState == MEDIA_STATE.PLAYING) {
                                        if (!mPlayerRGB.getLocked() || !mPlayerAlpha.getLocked()) {
                                            mMediaPlayerLock.unlock();
                                            //Logger.LOGD(TAG + " UpdateAlphaVideoDataT-----return------- " + mPlayerRGB.getLocked() + "," + mPlayerAlpha.getLocked());
                                            continue;
                                        }

                                        long rgbSampleTime = mPlayerRGB.getSampleTime();
                                        long alphaSampleTime = mPlayerAlpha.getSampleTime();

                                        //Logger.LOGD(TAG + " UpdateAlphaVideoDataT---------- " + mPlayerRGB.isReachEnd() + "," + mPlayerAlpha.isReachEnd());
                                        if (mPlayerRGB.isReachEnd() && !mPlayerAlpha.isReachEnd()) {
                                            mAlphaCondition.signal();
                                            mMediaPlayerLock.unlock();
                                            continue;
                                        } else if (mPlayerAlpha.isReachEnd() && !mPlayerRGB.isReachEnd()) {
                                            mRGBCondition.signal();
                                            mMediaPlayerLock.unlock();
                                            continue;
                                        }
                                        // Logger.LOGD(TAG + " UpdateAlphaVideoDataT---------- " + mOldRGBSampleTime + "," + rgbSampleTime + "," + alphaSampleTime);
                                        if (rgbSampleTime == 0 || alphaSampleTime == 0) {
                                            Logger.LOGD(TAG + " UpdateAlphaVideoDataT-----notify0----- ");
                                            if (rgbSampleTime == 0) {
                                                mRGBCondition.signal();
                                            }
                                            if (alphaSampleTime == 0) {
                                                mAlphaCondition.signal();
                                            }
                                            mOldRGBSampleTime = rgbSampleTime;
                                            mOldAlphaSampleTime = alphaSampleTime;
                                            mMediaPlayerLock.unlock();
                                            continue;
                                        }

                                        if (rgbSampleTime == alphaSampleTime) {
                                            copyTextureData();
                                            mRGBCondition.signal();
                                            mAlphaCondition.signal();
                                            // 保证unity获取到playing状态时，texture有数据。
                                            mPlayerDataReady = true;
                                            //Logger.LOGD(TAG + " UpdateAlphaVideoDataT-----notify----- ");
                                        } else if (rgbSampleTime > alphaSampleTime) {
                                            //Logger.LOGD(TAG + " UpdateAlphaVideoDataT-----notify2----- ");
                                            mAlphaCondition.signal();
                                        } else {
                                            //Logger.LOGD(TAG + " UpdateAlphaVideoDataT-----notify3----- ");
                                            mRGBCondition.signal();
                                        }
                                        mOldRGBSampleTime = rgbSampleTime;
                                        mOldAlphaSampleTime = alphaSampleTime;
                                    }
                                    mMediaPlayerLock.unlock();
                                }
                            }
                        };
                        mPlayerSyncThread.start();

                    }
                } catch (Exception e) {
                    Logger.LOGE(TAG + " Error while creating the MediaPlayer: " + e.toString());
                    mCurrentState = MEDIA_STATE.ERROR;
                    mMediaPlayerLock.unlock();
                    return false;
                }
            }
            result = true;
        }

        mMediaPlayerLock.unlock();
        Logger.LOGD(TAG + "  load end result=" + result);
        return true;
    }

    /**
     * get media player status.
     *
     * @return MEDIASTATE
     */
    public int GetAlphaVideoPlayStatus() {
        Logger.LOGD(TAG + " getStatus this.mCurrentState.type=" + this.mCurrentState.mType);
        /*if (mPlayerDataReady) {
            return this.mCurrentState.mType;
        } else {
            return this.mCurrentState.NOT_READY.mType;
        }*/
        return this.mCurrentState.mType;
    }

    public int GetExternalTextureRGB(int ptr) {
        return 0;
    }

    public int GetExternalTextureAlpha(int ptr) {
        return 0;
    }

    /**
     * get video width.
     *
     * @return width of video.
     */
    public int getVideoWidth() {
        Logger.LOGD(TAG + " getVideoWidth");
        if ((this.mCurrentState == MEDIA_STATE.NOT_READY) || (this.mCurrentState == MEDIA_STATE.ERROR)) {
            return -1;
        }
        return mVideoWidth;
    }

    /**
     * get video height.
     *
     * @return height of video.
     */
    public int getVideoHeight() {
        Logger.LOGD(TAG + " getVideoHeight");
        if ((this.mCurrentState == MEDIA_STATE.NOT_READY) || (this.mCurrentState == MEDIA_STATE.ERROR)) {
            return -1;
        }
        return mVideoHeight;
    }

    /**
     * start play video.
     */
    public void PlayAlphaVideo() {
        Logger.LOGD(TAG + " play ");

        if ((this.mCurrentState == MEDIA_STATE.NOT_READY) || (this.mCurrentState == MEDIA_STATE.ERROR)) {
            Logger.LOGE(TAG + " Cannot play this video if it is not ready");
            return;
        }

        this.mMediaPlayerLock.lock();

        if (this.mCurrentState == MEDIA_STATE.REACHED_END) {
            try {
                this.mPlayerRGB.seekTo(0);
                //this.mPlayerAlpha.seekTo(0);
            } catch (Exception e) {
                this.mMediaPlayerLock.unlock();
                Logger.LOGE(TAG + " Could not seek to position");
            }
        }

        try {
            // this.mMediaPlayer.start();
            this.mCurrentState = MEDIA_STATE.PLAYING;
            mPlayerRGB.setPlayWhenReady(true);
            mPlayerAlpha.setPlayWhenReady(true);
        } catch (Exception e) {
            this.mMediaPlayerLock.unlock();
            Logger.LOGE(TAG + " Could not start playback");
        }
        this.mMediaPlayerLock.unlock();
    }

    /**
     * pause media player.
     *
     * @return true paused.
     */
    public void PauseAlphaVideo() {
        Logger.LOGD(TAG + " pause");

        if ((this.mCurrentState == MEDIA_STATE.NOT_READY) || (this.mCurrentState == MEDIA_STATE.ERROR)) {
            return;
        }

        this.mMediaPlayerLock.lock();
        if (this.mPlayerRGB != null) {
            this.mCurrentState = MEDIA_STATE.PAUSED;
            mPlayerRGB.setPlayWhenReady(false);
            mPlayerAlpha.setPlayWhenReady(false);
        }
        this.mMediaPlayerLock.unlock();
    }

    /**
     * restart play alpha video from beginning of video.
     */
    public void RewindAlphaVideo() {
        PauseAlphaVideo();
        // 需要延迟一段时间执行后面的逻辑
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if ((this.mCurrentState == MEDIA_STATE.NOT_READY) || (this.mCurrentState == MEDIA_STATE.ERROR)) {
            Logger.LOGE(TAG + " Cannot play this video if it is not ready");
            return;
        }

        this.mMediaPlayerLock.lock();

        try {
            this.mPlayerRGB.seekTo(0);
            this.mPlayerAlpha.seekTo(0);
        } catch (Exception e) {
            this.mMediaPlayerLock.unlock();
            Logger.LOGE(TAG + " Could not seek to position");
        }

        try {
            this.mCurrentState = MEDIA_STATE.PLAYING;
            mPlayerRGB.setPlayWhenReady(true);
            mPlayerAlpha.setPlayWhenReady(true);
        } catch (Exception e) {
            this.mMediaPlayerLock.unlock();
            Logger.LOGE(TAG + " Could not start playback");
        }
        this.mMediaPlayerLock.unlock();
    }

    /**
     * stop media player.
     *
     * @return true stopped.
     */
    public boolean stop() {
        Logger.LOGD(TAG + " stop");

        if ((this.mCurrentState == MEDIA_STATE.NOT_READY) || (this.mCurrentState == MEDIA_STATE.ERROR)) {
            return false;
        }

        boolean result = false;
        this.mMediaPlayerLock.lock();
        if (this.mPlayerRGB != null) {
            this.mCurrentState = MEDIA_STATE.STOPPED;
            this.mPlayerRGB.setPlayWhenReady(false);
            this.mPlayerAlpha.setPlayWhenReady(false);
            result = true;
        }
        this.mMediaPlayerLock.unlock();
        return result;
    }

    private long mOldRGBSampleTime = 0;
    private long mOldAlphaSampleTime = 0;
    /*public int UpdateAlphaVideoData() {
        mMediaPlayerLock.lock();

        if (mDestTextureIDRGB != -1) {
            // Only request an update if currently playing
            if (mCurrentState == MEDIA_STATE.PLAYING) {
//                Logger.LOGD(TAG + " UpdateAlphaVideoDataT----------start ");
                count++;
                if (count % 500 != 0) {
                    mMediaPlayerLock.unlock();
                    return this.mCurrentState.mType;
                }
                if (!mPlayerRGB.getLocked() || !mPlayerAlpha.getLocked()) {
                    Logger.LOGD(TAG + " UpdateAlphaVideoDataT-----return------- " + mPlayerRGB.getLocked() + "," + mPlayerAlpha.getLocked());
                    mMediaPlayerLock.unlock();
                    return this.mCurrentState.mType;
                }
                Logger.LOGD(TAG + " UpdateAlphaVideoDataT---------- " + mOldSampleTime + "," + mPlayerRGB.getSampleTime() + "," + mPlayerAlpha.getSampleTime());
                if (mPlayerRGB.getSampleTime() == 0 || mPlayerAlpha.getSampleTime() == 0) {
                    Logger.LOGD(TAG + " UpdateAlphaVideoDataT-----notify0----- ");
                    mMediaPlayerLock.unlock();
                    return this.mCurrentState.mType;
                }

                if (mPlayerRGB.getSampleTime() == mPlayerAlpha.getSampleTime()) {
                    Bitmap bitmap = mPlayerRGB.getBitmap();
//                    Bitmap bitmap = BitmapUtil.decodeFile("/sdcard/11/frame_00001.jpg");
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mDestTextureIDRGB);
                    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
                    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
                    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

                    Bitmap bitmap2 = mPlayerAlpha.getBitmap();
//                    Bitmap bitmap2 = BitmapUtil.decodeFile("/sdcard/11/frame2_00001.jpg");
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mDestTextureIDAlpha);
                    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap2, 0);
                    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
                    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
                    Logger.LOGD(TAG + " UpdateAlphaVideoDataT-----notify----- ");
                    mRGBCondition.signal();
                    mAlphaCondition.signal();
                } else if (mPlayerRGB.getSampleTime() > mPlayerAlpha.getSampleTime()) {
                    Logger.LOGD(TAG + " UpdateAlphaVideoDataT-----notify2----- ");
                    mAlphaCondition.signal();
                } else {
                    Logger.LOGD(TAG + " UpdateAlphaVideoDataT-----notify3----- ");
                    mRGBCondition.signal();
                }
            }
        }
        mMediaPlayerLock.unlock();
        Logger.LOGD(TAG + " UpdateAlphaVideoDataT----------end ");
        return this.mCurrentState.mType;
    }*/


    public int UpdateAlphaVideoData() {
        mMediaPlayerLock.lock();

        if (mTextureRGB_Y != -1) {
            // Only request an update if currently playing
            if (this.mCurrentState == MEDIA_STATE.PLAYING && mPlayerDataReady) {
                updateTextureRGB();
                updateTextureAlpha();
            }
        }
        mMediaPlayerLock.unlock();

        //Logger.LOGD(TAG + " UpdateAlphaVideoDataTEnd----------end " + mPlayerDataReady + "," + this.mCurrentState.mType);
        return mPlayerDataReady ? this.mCurrentState.mType : MEDIA_STATE.NOT_READY.mType;
    }

    /**
     * set volume of media player.
     *
     * @param value the volume.
     * @return true ok.
     */
    public boolean setVolume(float value) {
        // Log.d(TAG , "setVolume");

        if ((this.mCurrentState == MEDIA_STATE.NOT_READY) || (this.mCurrentState == MEDIA_STATE.ERROR)) {
            return false;
        }

        boolean result = false;
        this.mMediaPlayerLock.lock();
        if (this.mPlayerRGB != null) {
            result = true;
        }
        this.mMediaPlayerLock.unlock();

        return result;
    }

    /**
     * get buffering percent.
     *
     * @return percent of buffering.
     */
    public int getCurrentBufferingPercentage() {
        // Log.d(TAG , "getCurrentBufferingPercentage");
        return this.mCurrentBufferingPercentage;
    }

    PlayerStateListener rgbListener = new PlayerStateListener() {
        @Override
        public void onVideoSizeChanged(int width, int height) {
            Logger.LOGD(TAG + " rgbListener onVideoSizeChanged width=" + width + ",height=" + height);
            calcVideoYUVLength(width, height);

            mBufferRGB_Y = ByteBuffer.allocateDirect(mDataLengthY).order(ByteOrder.nativeOrder());
            mBufferRGB_UV = ByteBuffer.allocateDirect(mDataLengthUV).order(ByteOrder.nativeOrder());
            mDataRGB_Y = new byte[mDataLengthY];
            mDataRGB_UV = new byte[mDataLengthUV];
            mTmpDataRGB_Y = new byte[mDataLengthY];
            mTmpDataRGB_UV = new byte[mDataLengthUV];
            mDataRGB = new byte[mDataLengthUV + mDataLengthY];
            /*mStateRGB = MEDIA_STATE.READY;
            if (mStateAlpha == MEDIA_STATE.READY) {
                mCurrentState = MEDIA_STATE.READY;
            }*/
        }

        @Override
        public void onStateChanged(boolean playWhenReady, MEDIA_STATE playbackState) {
            Logger.LOGD(TAG + " rgbListener onStateChanged playWhenReady=" + playWhenReady + ",playbackState=" + playbackState + ",mCurrentState=" + mCurrentState);
            mStateRGB = playbackState;
            if (mStateRGB == MEDIA_STATE.IDLE && mStateAlpha == MEDIA_STATE.IDLE) {
                mCurrentState = MEDIA_STATE.PLAYING;
            }
            switch (playbackState) {

            }
        }
    };

    PlayerStateListener alphaListener = new PlayerStateListener() {
        @Override
        public void onVideoSizeChanged(int width, int height) {
            Logger.LOGD(TAG + " alphaListener onVideoSizeChanged width=" + width + ",height=" + height);
            calcVideoYUVLength(width, height);

            mBufferAlpha_Y = ByteBuffer.allocateDirect(mDataLengthY).order(ByteOrder.nativeOrder());
            mBufferAlpha_UV = ByteBuffer.allocateDirect(mDataLengthUV).order(ByteOrder.nativeOrder());
            mDataAlpha_Y = new byte[mDataLengthY];
            mDataAlpha_UV = new byte[mDataLengthUV];
            mTmpDataAlpha_Y = new byte[mDataLengthY];
            mTmpDataAlpha_UV = new byte[mDataLengthUV];
            mDataAlpha = new byte[mDataLengthUV + mDataLengthY];
            /*if (mStateRGB == MEDIA_STATE.READY) {
                mCurrentState = MEDIA_STATE.READY;
            }*/
        }

        @Override
        public void onStateChanged(boolean playWhenReady, MEDIA_STATE playbackState) {
            Logger.LOGD(TAG + " alphaListener onStateChanged playWhenReady=" + playWhenReady + ",playbackState=" + playbackState + ",mCurrentState=" + mCurrentState);
            mStateAlpha = playbackState;
            if (mStateRGB == MEDIA_STATE.IDLE && mStateAlpha == MEDIA_STATE.IDLE) {
                mCurrentState = MEDIA_STATE.PLAYING;
            }
            switch (playbackState) {

            }
        }
    };

    /**
     * get surface texture matrix.
     */
    /*public void getSurfaceTextureTransformMatrix(float[] mtx) {
        Logger.LOGD(TAG + " getSurfaceTextureTransformMatrix");
        mSurfaceTextureLock.lock();
        if (mSurfaceTextureRGB != null) {
            mSurfaceTextureRGB.getTransformMatrix(mtx);
        }
        mSurfaceTextureLock.unlock();
    }*/
    public void setAlphaVideoTextureID(int rgbTextureID_y, int rgbTextureID_uv, int alphaTextureID_y, int alphaTextureID_uv) {
        Logger.LOGD(TAG + " setAlphaVideoTextureID");
        this.mTextureRGB_Y = rgbTextureID_y;
        this.mTextureRGB_UV = rgbTextureID_uv;
        this.mTextureAlpha_Y = alphaTextureID_y;
        this.mTextureAlpha_UV = alphaTextureID_uv;
    }

    /**
     * unity call back.
     * set parent activity.
     *
     * @param newActivity activity.
     */
    public void setActivity(Activity newActivity) {
        // Log.d(TAG , "setActivity");
        this.mParentActivity = newActivity;
    }

    private class AlphaPlayerThread extends Thread {
        private MediaExtractor extractor;
        private MediaCodec decoder;
        private Surface surface;
        private String filePath;
        private PlayerStateListener playerStateListener;
        private boolean playWhenReady = true;
        private boolean isInputEOS = false;
        private boolean isOutputEOS = false;
        private boolean mAutoLoopLoop = false;
        private long mSampleTime;
        private Bitmap mBitmap;
        private boolean mLocked = true;
        private final int decodeColorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
        private boolean mHasGotFrameRate = false;
        private long mPreInfoPresentationTime = 0;

        public AlphaPlayerThread(Surface surface, String filePath) {
            this.surface = surface;
            this.filePath = filePath;
        }

        public void setPlayerListener(PlayerStateListener playerListener) {
            this.playerStateListener = playerListener;
        }

        public void setLoop(boolean loop) {
            this.mAutoLoopLoop = loop;
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            Logger.LOGD(TAG + " AlphaPlayerThread alpha thread decoder start run");
            extractor = new MediaExtractor();
            try {
                extractor.setDataSource(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                Logger.LOGD(TAG + " mime:" + mime);
                if (mime.startsWith("video/")) {
                    extractor.selectTrack(i);
                    //Logger.LOGD(TAG + " AlphaPlayerThread containsKey=" + format.containsKey(MediaFormat.KEY_FRAME_RATE));
                    //testFormat(format);
                    //Logger.LOGD(TAG + " frameRate=" + format.getInteger(MediaFormat.KEY_FRAME_RATE));
                    mHasGotFrameRate = calculateDelayPerFrame(format);
                    try {
                        decoder = MediaCodec.createDecoderByType(mime);
                        //showSupportedColorFormat(decoder.getCodecInfo().getCapabilitiesForType(mime));
                        if (isColorFormatSupported(decodeColorFormat, decoder.getCodecInfo().getCapabilitiesForType(mime))) {
                            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, decodeColorFormat);
                            Logger.LOGD(TAG + " AlphaPlayerThread decodeColorFormat support2:" + decodeColorFormat);
                        } else {
                            Logger.LOGD(TAG + " AlphaPlayerThread decodeColorFormat not support2:" + decodeColorFormat);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    decoder.configure(format, surface, null, 0);
                    break;
                }
                /**
                 * TODO
                 * Audio part will be used by AudioTrack.
                 */
            }

            if (decoder == null) {
                Log.e(TAG, "Can't find video info!");
                return;
            }

            try {
                decoder.start();
            } catch (Exception e) {
                e.printStackTrace();
            }

            ByteBuffer[] inputBuffers = decoder.getInputBuffers();
            ByteBuffer[] outputBuffers = decoder.getOutputBuffers();
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

            boolean isFirstTime = true;
            int alphaCount = 0;
            while (MEDIA_STATE.UNLOAD != mCurrentState) {
                mMediaPlayerLock.lock();
                if (!playWhenReady) {
                    try {
                        Logger.LOGD(TAG + " AlphaPlayerThread rgb thread paused");
                        playerStateListener.onStateChanged(this.playWhenReady, MEDIA_STATE.PAUSED);
                        if (null != mAlphaCondition) {
                            mAlphaCondition.await();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mMediaPlayerLock.unlock();
                if (!isInputEOS) {
                    int inIndex = decoder.dequeueInputBuffer(10000);
                    //Log.d(TAG, "AlphaPlayerThread inIndex=" + inIndex + ",extractor.getSampleTime()="  + extractor.getSampleTime());
                    if (inIndex >= 0) {
                        ByteBuffer buffer = inputBuffers[inIndex];
                        int sampleSize = extractor.readSampleData(buffer, 0);
                        if (sampleSize < 0) {
                            // We shouldn't stop the playback at this point, just pass the EOS
                            // flag to decoder, we will get it again from the
                            // dequeueOutputBuffer
                            Logger.LOGD(TAG + " AlphaPlayerThread InputBuffer BUFFER_FLAG_END_OF_STREAM");
                            decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            isInputEOS = true;
                        } else {
                            decoder.queueInputBuffer(inIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                            extractor.advance();
                        }
                    }
                }

                int outIndex = decoder.dequeueOutputBuffer(info, 10000);
                switch (outIndex) {
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        Logger.LOGD(TAG + " AlphaPlayerThread INFO_OUTPUT_BUFFERS_CHANGED");
                        outputBuffers = decoder.getOutputBuffers();
                        break;
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        Logger.LOGD(TAG + " AlphaPlayerThread output format " + decoder.getOutputFormat());
                        playerStateListener.onVideoSizeChanged(
                                decoder.getOutputFormat().getInteger(MediaFormat.KEY_WIDTH),
                                decoder.getOutputFormat().getInteger(MediaFormat.KEY_HEIGHT));
                        break;
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        Logger.LOGD(TAG + " AlphaPlayerThread dequeueOutputBuffer timed out!");
                        break;
                    default:
                        if (isFirstTime) {
                            playerStateListener.onStateChanged(this.playWhenReady, MEDIA_STATE.IDLE);
                            isFirstTime = false;
                        }
                        ByteBuffer buffer = outputBuffers[outIndex];
                        // Log.v(TAG, "AlphaPlayerThread We can't use this buffer but render it due to the API limit, " + buffer);

                        // We use a very simple clock to keep the video FPS, or the video
                        // playback will be too fast

                        boolean doLoop = false;
                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            if (mAutoLoopLoop) {
                                doLoop = true;
                            } else {
                                playerStateListener.onStateChanged(this.playWhenReady, MEDIA_STATE.REACHED_END);
                                // 如果不自动循环播放，则标记play false，下一次循环时候进入wait
                                playWhenReady = false;
                                doLoop = true;
                            }
                            isOutputEOS = true;
                        }
                        // alphaCount++;
                        // String fileName = OUTPUT_DIR + String.format("frame2_%05d.jpg", alphaCount);
                        if (info.size > 0) {
                            mMediaPlayerLock.lock();
                            if (mCurrentState != UNLOAD) {
                                Logger.LOGD(TAG + " AlphaPlayerThread outIndex : " + outIndex + "  mCurrentState : " + mCurrentState);

//                                buffer.get(mDataAlpha);
                                buffer.get(mTmpDataAlpha_Y, 0, mDataLengthY);
                                buffer.get(mTmpDataAlpha_UV, 0, mDataLengthUV);
                                buffer.clear();
                                // mBitmap = byte2image(mDataAlpha, fileName);
                                //dumpFile("/sdcard/11/alpha.yuv", chunk);
//                                System.arraycopy(mDataAlpha, 0, mTmpDataAlpha_Y, 0, mDataLengthY);
//                                System.arraycopy(mDataAlpha, mDataLengthY, mTmpDataAlpha_UV, 0, mDataLengthUV);
                            }
                            mMediaPlayerLock.unlock();

                        }

                        /*Image image = decoder.getOutputImage(outIndex);
                        //System.out.println("image format: " + image.getFormat());

                        if (outputImageFileType != -1) {
                            switch (outputImageFileType) {
                                case FILE_TypeI420:
                                    //fileName = OUTPUT_DIR + String.format("frame_%05d_I420_%dx%d.yuv", count, mVideoWidth, mVideoHeight);
                                    //dumpFile(fileName, getDataFromImage(image, COLOR_FormatI420));
                                    break;
                                case FILE_TypeNV21:
                                    //fileName = OUTPUT_DIR + String.format("frame_%05d_NV21_%dx%d.yuv", count, mVideoWidth, mVideoHeight);
                                    //dumpFile(fileName, getDataFromImage(image, COLOR_FormatNV21));
                                    break;
                                case FILE_TypeJPEG:
                                    compressToJpeg(fileName, image);
                                    break;
                            }
                        }
                        image.close();
                        mBitmap = BitmapUtil.decodeFile(fileName);*/
                        // 使用presentationTimeUs, getSampleTime不准确，还未找到原因
                        mSampleTime = (extractor.getSampleTime() != -1) ? info.presentationTimeUs : 0;
                        // 部分手机无法解析出frame rate，所以需要计算两帧之间的显示时间来确定frame rate。
                        if (!mHasGotFrameRate && (mSampleTime > 0)) {
                            //Logger.LOGD(TAG + " AlphaPlayerThread mPlayerDelayPerFrame=" + mPlayerDelayPerFrame + ",mSampleTime=" + mSampleTime + ",mPreInfoPresentationTime=" + mPreInfoPresentationTime);
                            if (mPreInfoPresentationTime > 0) {
                                long frameTime = mSampleTime - mPreInfoPresentationTime;
                                mPlayerDelayPerFrame = (int) (frameTime / 1000);
                                mHasGotFrameRate = true;
                                Logger.LOGD(TAG + " AlphaPlayerThread mPlayerDelayPerFrame=" + mPlayerDelayPerFrame);
                            }
                            mPreInfoPresentationTime = mSampleTime;
                        }
                        mMediaPlayerLock.lock();
                        if (playWhenReady) {
                            try {
                                mLocked = true;
                                if (null != mAlphaCondition) {
                                    mAlphaCondition.await();
                                }
                                mLocked = false;
                                // Logger.LOGD(TAG + " AlphaPlayerThread alpha thread not sync play");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        mMediaPlayerLock.unlock();
                        decoder.releaseOutputBuffer(outIndex, false);
                        if (doLoop) {
                            extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                            isInputEOS = false;
                            isOutputEOS = false;
                            decoder.flush();
                        }
                        break;
                }
            }
            Logger.LOGD(TAG + " AlphaPlayerThread alpha thread decoder stop");
            decoder.stop();
            decoder.release();
            extractor.release();
            Logger.LOGD(TAG + " AlphaPlayerThread alpha thread decoder stop end");
        }

        public void seekTo(long time) {
            extractor.seekTo(time, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            decoder.flush();
        }

        public void setPlayWhenReady(boolean playWhenReady) {
            mMediaPlayerLock.lock();
            this.playWhenReady = playWhenReady;
            mAlphaCondition.signal();
            mMediaPlayerLock.unlock();
        }

        public long getSampleTime() {
            return this.mSampleTime;
        }

        public Bitmap getBitmap() {
            return this.mBitmap;
        }

        public boolean getLocked() {
            return this.mLocked;
        }

        public boolean isReachEnd() {
            return this.isOutputEOS;
        }
    }

    private class RGBPlayerThread extends Thread {
        private MediaExtractor extractor;
        private MediaCodec decoder;
        private Surface surface;
        private String filePath;
        private PlayerStateListener playerStateListener;
        private boolean playWhenReady = true;
        private boolean isInputEOS = false;
        private boolean isOutputEOS = false;
        private boolean mAutoLoopLoop = false;
        private long mSampleTime;
        private Bitmap mBitmap;
        private boolean mLocked = true;
        private final int decodeColorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;

        public RGBPlayerThread(Surface surface, String filePath) {
            this.surface = surface;
            this.filePath = filePath;
        }

        public void setPlayerListener(PlayerStateListener playerListener) {
            this.playerStateListener = playerListener;
        }

        public void setLoop(boolean loop) {
            this.mAutoLoopLoop = loop;
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            extractor = new MediaExtractor();
            try {
                extractor.setDataSource(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                Logger.LOGD(TAG + " mime:" + mime);
                if (mime.startsWith("video/")) {
                    extractor.selectTrack(i);
                    //Logger.LOGD(TAG + " RGBPlayerThread containsKey=" + format.containsKey(MediaFormat.KEY_FRAME_RATE));
                    //testFormat(format);
                    //calculateDelayPerFrame(format);
                    try {
                        decoder = MediaCodec.createDecoderByType(mime);
                        //showSupportedColorFormat(decoder.getCodecInfo().getCapabilitiesForType(mime));
                        if (isColorFormatSupported(decodeColorFormat, decoder.getCodecInfo().getCapabilitiesForType(mime))) {
                            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, decodeColorFormat);
                            Logger.LOGD(TAG + " RGBPlayerThread decodeColorFormat support:" + decodeColorFormat);
                        } else {
                            Logger.LOGW(TAG + " RGBPlayerThread decodeColorFormat not support:" + decodeColorFormat);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    decoder.configure(format, surface, null, 0);

                    break;
                }
                /**
                 * TODO
                 * Audio part will be used by AudioTrack.
                 */
            }

            if (decoder == null) {
                Log.e(TAG, "Can't find video info!");
                return;
            }

            decoder.start();

            ByteBuffer[] inputBuffers = decoder.getInputBuffers();
            ByteBuffer[] outputBuffers = decoder.getOutputBuffers();
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

            boolean isFirstTime = true;
            int rgbCount = 0;
            while (MEDIA_STATE.UNLOAD != mCurrentState) {
                mMediaPlayerLock.lock();
                if (!playWhenReady) {
                    try {
                        Logger.LOGD(TAG + " RGBPlayerThread rgb thread paused");
                        playerStateListener.onStateChanged(this.playWhenReady, MEDIA_STATE.PAUSED);
                        if (null != mRGBCondition) {
                            mRGBCondition.await();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mMediaPlayerLock.unlock();
                if (!isInputEOS) {
                    int inIndex = decoder.dequeueInputBuffer(10000);
                    // Log.d(TAG, "RGBPlayerThread inIndex=" + inIndex);
                    if (inIndex >= 0) {
                        ByteBuffer buffer = inputBuffers[inIndex];
                        int sampleSize = extractor.readSampleData(buffer, 0);
                        if (sampleSize < 0) {
                            // We shouldn't stop the playback at this point, just pass the EOS
                            // flag to decoder, we will get it again from the
                            // dequeueOutputBuffer
                            Logger.LOGD(TAG + " RGBPlayerThread InputBuffer BUFFER_FLAG_END_OF_STREAM");
                            decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            isInputEOS = true;
                        } else {
                            decoder.queueInputBuffer(inIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                            extractor.advance();
                        }
                    }
                }

                int outIndex = decoder.dequeueOutputBuffer(info, 10000);
                switch (outIndex) {
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        Logger.LOGD(TAG + " RGBPlayerThread INFO_OUTPUT_BUFFERS_CHANGED");
                        outputBuffers = decoder.getOutputBuffers();
                        break;
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        Logger.LOGI(TAG + " RGBPlayerThread output format " + decoder.getOutputFormat());
                        playerStateListener.onVideoSizeChanged(
                                decoder.getOutputFormat().getInteger(MediaFormat.KEY_WIDTH),
                                decoder.getOutputFormat().getInteger(MediaFormat.KEY_HEIGHT));
                        break;
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        Logger.LOGD(TAG + " RGBPlayerThread dequeueOutputBuffer timed out!");
                        break;
                    default:
                        if (isFirstTime) {
                            playerStateListener.onStateChanged(this.playWhenReady, MEDIA_STATE.IDLE);
                            isFirstTime = false;
                        }
                        ByteBuffer buffer = outputBuffers[outIndex];
                        // Log.v(TAG, "RGBPlayerThread We can't use this buffer but render it due to the API limit, " + buffer);

                        boolean doLoop = false;
                        // dequeueInputBuffer达到BUFFER_FLAG_END_OF_STREAM，但是还需要多次dequeueOutputBuffer才能释放完成buffer.
                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            if (mAutoLoopLoop) {
                                doLoop = true;
                            } else {
                                playerStateListener.onStateChanged(this.playWhenReady, MEDIA_STATE.REACHED_END);
                                // 如果不自动循环播放，则标记play false，下一次循环时候进入wait
                                playWhenReady = false;
                                doLoop = true;
                            }
                            isOutputEOS = true;
                        }
                        /*final byte[] chunk = new byte[info.size];
                        buffer.get(chunk);*/
                        // Logger.LOGD(TAG + " RGBPlayerThread info.size=" + info.size + "," + (mDataLengthUV + mDataLengthY));
                        //rgbCount++;
                        //String fileName = OUTPUT_DIR + String.format("frame_%05d.jpg", rgbCount);
                        if (info.size > 0) {
                            mMediaPlayerLock.lock();
                            if (mCurrentState != UNLOAD) {
//                                buffer.get(mDataRGB);
                                buffer.get(mTmpDataRGB_Y, 0, mDataLengthY);
                                buffer.get(mTmpDataRGB_UV, 0, mDataLengthUV);
                                buffer.clear();
                                Logger.LOGD(TAG + " RGBPlayerThread outIndex : " + outIndex + "  mCurrentState : " + mCurrentState);
                                // mBitmap = byte2image(mDataRGB, fileName);
                                //dumpFile("/sdcard/11/rgb.yuv", chunk);
//                                System.arraycopy(mDataRGB, 0, mTmpDataRGB_Y, 0, mDataLengthY);
//                                System.arraycopy(mDataRGB, mDataLengthY, mTmpDataRGB_UV, 0, mDataLengthUV);
                            }
                            mMediaPlayerLock.unlock();
                        }

                        // 使用presentationTimeUs, getSampleTime不准确，还未找到原因
                        mSampleTime = (extractor.getSampleTime() != -1) ? info.presentationTimeUs : 0;
                        mMediaPlayerLock.lock();
                        if (playWhenReady) {
                            try {
                                mLocked = true;
                                if (null != mRGBCondition) {
                                    mRGBCondition.await();
                                }
                                mLocked = false;
                                // Logger.LOGD(TAG + " RGBPlayerThread rgb thread not sync play");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        mMediaPlayerLock.unlock();
                        decoder.releaseOutputBuffer(outIndex, false);
                        if (doLoop) {
                            extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                            isInputEOS = false;
                            isOutputEOS = false;
                            decoder.flush();
                        }
                        break;
                }
            }

            decoder.stop();
            decoder.release();
            extractor.release();
        }

        public void seekTo(long time) {
            extractor.seekTo(time, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            decoder.flush();
        }

        public void setPlayWhenReady(boolean playWhenReady) {
            mMediaPlayerLock.lock();
            this.playWhenReady = playWhenReady;
            mRGBCondition.signal();
            mMediaPlayerLock.unlock();
        }

        public long getSampleTime() {
            return this.mSampleTime;
        }

        public Bitmap getBitmap() {
            return this.mBitmap;
        }

        public boolean getLocked() {
            return this.mLocked;
        }

        public boolean isReachEnd() {
            return this.isOutputEOS;
        }
    }

    interface PlayerStateListener {
        public void onVideoSizeChanged(int width, int height);

        public void onStateChanged(boolean playWhenReady, MEDIA_STATE playbackState);
    }

    private void initPlayer() {
        mPlayerRGB = new RGBPlayerThread(null, mCachedFilePathRGB);
        mPlayerRGB.setPlayerListener(rgbListener);
        mPlayerRGB.setLoop(mIsRepeat);
        mPlayerAlpha = new AlphaPlayerThread(null, mCachedFilePathAlpha);
        mPlayerAlpha.setPlayerListener(alphaListener);
        mPlayerAlpha.setLoop(mIsRepeat);
        mPlayerRGB.start();
        mPlayerAlpha.start();
    }

    private void copyTextureData() {
        if (mCurrentState != UNLOAD) {
            Logger.LOGD(TAG + " copyTextureData-----mCurrentState : " + mCurrentState);
//            System.arraycopy(mTmpDataAlpha_Y, 0, mDataAlpha_Y, 0, mDataLengthY);
//            System.arraycopy(mTmpDataAlpha_UV, 0, mDataAlpha_UV, 0, mDataLengthUV);
            mBufferAlpha_Y.clear();
            mBufferAlpha_Y.put(mTmpDataAlpha_Y).position(0);
            mBufferAlpha_UV.clear();
            mBufferAlpha_UV.put(mTmpDataAlpha_UV).position(0);

//            System.arraycopy(mTmpDataRGB_Y, 0, mDataRGB_Y, 0, mDataLengthY);
//            System.arraycopy(mTmpDataRGB_UV, 0, mDataRGB_UV, 0, mDataLengthUV);
            mBufferRGB_Y.clear();
            mBufferRGB_Y.put(mTmpDataRGB_Y).position(0);
            mBufferRGB_UV.clear();
            mBufferRGB_UV.put(mTmpDataRGB_UV).position(0);
        }
    }

    private void updateTextureRGB() {
        if (mCurrentState != UNLOAD) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureRGB_Y);
            GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, (int) mVideoWidth, (int) mVideoHeight, 0,
                    GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, mBufferRGB_Y);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureRGB_UV);
            GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA, (int) mVideoWidth / 2,
                    (int) mVideoHeight / 2, 0, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, mBufferRGB_UV);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }
    }

    private void updateTextureAlpha() {
        if (mCurrentState != UNLOAD) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureAlpha_Y);
            GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, (int) mVideoWidth, (int) mVideoHeight, 0,
                    GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, mBufferAlpha_Y);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureAlpha_UV);
            GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA, (int) mVideoWidth / 2,
                    (int) mVideoHeight / 2, 0, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, mBufferAlpha_UV);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }
    }


    private static boolean isImageFormatSupported(Image image) {
        int format = image.getFormat();
        switch (format) {
            case ImageFormat.YUV_420_888:
            case ImageFormat.NV21:
            case ImageFormat.YV12:
                return true;
        }
        return false;
    }


    private void dumpFile(String fileName, byte[] data) {
        FileOutputStream outStream;
        try {
            outStream = new FileOutputStream(fileName);
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to create output file " + fileName, ioe);
        }
        try {
            outStream.write(data);
            outStream.close();
        } catch (IOException ioe) {
            throw new RuntimeException("failed writing data to file " + fileName, ioe);
        }
    }

    public int[] yCbCr2Rgb(byte[] yuv, int width, int height) {
        int frameSize = width * height;
        int[] rgba = new int[frameSize];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int y = (0xff & ((int) yuv[i * width + j]));
                int u = (0xff & ((int) yuv[frameSize + (i >> 1) * width
                        + (j & ~1) + 0]));
                int v = (0xff & ((int) yuv[frameSize + (i >> 1) * width
                        + (j & ~1) + 1]));
                y = y < 16 ? 16 : y;
                int r = Math.round(1.166f * (y - 16) + 1.596f * (v - 128));
                int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128)
                        - 0.391f * (u - 128));
                int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));
                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
            }
        }
        return rgba;
    }

    private Bitmap byte2image(byte[] src, String fileName) {
        int[] color = yCbCr2Rgb(src, mVideoWidth, mVideoHeight);
        try {
            Bitmap bmp = Bitmap.createBitmap(color, mVideoWidth, mVideoHeight, Bitmap.Config.ARGB_8888);
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
            try {
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bmp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean calculateDelayPerFrame(MediaFormat format) {
        Logger.LOGD(TAG + " frameRate=" + format.toString());
        if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
            int frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
            mPlayerDelayPerFrame = 1000 / frameRate;
            return true;
        }
        return false;
    }

    private void showSupportedColorFormat(MediaCodecInfo.CodecCapabilities caps) {
        for (int c : caps.colorFormats) {
            Logger.LOGD(TAG + " showSupportedColorFormat:" + c);
        }
    }

    private boolean isColorFormatSupported(int colorFormat, MediaCodecInfo.CodecCapabilities caps) {
        for (int c : caps.colorFormats) {
            if (colorFormat == c) {
                return true;
            }
        }
        return false;
    }

    // 防止用户编辑平台使用两个size不同的视频文件作为透明视频使用.
    // 取最小size，防止copy data越界.
    private void calcVideoYUVLength(int width, int height) {
        if (mVideoWidth > 0) {
            mVideoWidth = (width > mVideoWidth) ? mVideoWidth : width;
            mVideoHeight = (height > mVideoHeight) ? mVideoHeight : height;
        } else {
            mVideoWidth = width;
            mVideoHeight = height;
        }
        mDataLengthY = mVideoWidth * mVideoHeight;
        mDataLengthUV = mDataLengthY / 2;
        Logger.LOGD(TAG + " calcVideoYUVLength width=" + mVideoWidth + ", height=" + mVideoHeight);
    }
}