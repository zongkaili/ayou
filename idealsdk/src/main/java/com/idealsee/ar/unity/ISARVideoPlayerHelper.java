package com.idealsee.ar.unity;

/**
 *
 */

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.view.Surface;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecTrackRenderer.DecoderInitializationException;
import com.google.android.exoplayer.MediaCodecUtil.DecoderQueryException;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.util.Util;
import com.idealsee.sdk.media.player.DemoPlayer;
import com.idealsee.sdk.media.player.HlsRendererBuilder;
import com.idealsee.sdk.util.Logger;
import com.idealsee.sdk.media.ISARDownloadThread;
import com.idealsee.sdk.media.ISARDownloadThread.DownloadUpdateListener;
import com.idealsee.sdk.media.ISARGL2JNILib;
import com.idealsee.sdk.media.player.ExtractorRendererBuilder;
import com.idealsee.sdk.util.ISARFilesUtil;

import java.io.File;
import java.util.concurrent.locks.ReentrantLock;


/**
 * This class is for unity videoplayer.
 */
public class ISARVideoPlayerHelper {
    private static final String TAG = "VideoPlayerHelper";

    private static final float DEFAULT_POSITION = -1.0f;
    private static final int MSG_ON_PLAYER_PREPARE = 1;
    private static final int MSG_ON_PLAYER_PLAY = 2;
    private static final int MSG_ON_PLAYER_ERROR_PLAY = 3;
    private static final int MSG_ON_PLAYER_PLAY_REPEAT = 4;

    // private MediaPlayer mMediaPlayer = null;
    private MEDIA_TYPE mVideoType = MEDIA_TYPE.UNKNOWN;
    private SurfaceTexture mSurfaceTexture = null;
    private int mCurrentBufferingPercentage = 0;
    private MEDIA_STATE mCurrentState = MEDIA_STATE.NOT_READY;
    private boolean mShouldPlayImmediately = false;
    private float mSeekPosition = DEFAULT_POSITION;
    private ReentrantLock mMediaPlayerLock = null;
    private ReentrantLock mSurfaceTextureLock = null;
    private String mCachedFilePath;
    private int mSdkVersion = 0;

    private Activity mParentActivity;
    private int mMediaTextureID;
    private int mDestTextureID;
    private int mFBO;
    float[] mMtx = new float[16];

    private boolean mIsCachedStop = false;
    private boolean mIsCachedReady = false;
    private boolean mIsCachedEnd = false;
    private boolean mIsRepeat = false; // loop
    private boolean mIsNeedStop = false; // deinit before
    private boolean mIsNeedPause = false; // application pause before play

    // buffer needed per millisecond
    private int mVideoWidth = -1;
    private int mVideoHeight = -1;
    ISARDownloadThread mThread = null;

    private DemoPlayer mPlayer;
    boolean playerNeedsPrepare = true;
    private Surface mSurface;
    private String mFilePath = "";

    public static enum MEDIA_TYPE {
        ON_TEXTURE(0), FULLSCREEN(1), ON_TEXTURE_FULLSCREEN(2), UNKNOWN(3);

        private int mType;

        private MEDIA_TYPE(int type) {
            this.mType = type;
        }

        public int getNumericType() {
            return this.mType;
        }
    }

    public static enum MEDIA_STATE {
        REACHED_END(0), PAUSED(1), STOPPED(2), PLAYING(3), READY(4), NOT_READY(5), ERROR(6), LOADING(7);

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
    public ISARVideoPlayerHelper() {
        Logger.LOGD(TAG + " VideoPlayerHelper");
        // this.mMediaPlayer = null;
        this.mPlayer = null;
        this.mVideoType = MEDIA_TYPE.UNKNOWN;
        this.mSurfaceTexture = null;
        this.mCurrentBufferingPercentage = 0;

        this.mCurrentState = MEDIA_STATE.NOT_READY;
        this.mShouldPlayImmediately = false;
        this.mSeekPosition = DEFAULT_POSITION;
        this.mMediaPlayerLock = null;
        this.mSurfaceTextureLock = null;
        this.mParentActivity = null;
        this.mSdkVersion = Build.VERSION.SDK_INT;
        Logger.LOGD(TAG + " VideoPlayer mSDKVersion=" + mSdkVersion);
    }

    /**
     * Initializes the VideoPlayerHelper object.
     */
    public boolean init() {
        Logger.LOGD(TAG + " init");
        mMediaPlayerLock = new ReentrantLock();
        mSurfaceTextureLock = new ReentrantLock();
        return true;
    }

    /**
     * Deinitializes the VideoPlayerHelper object.
     */
    public boolean deinit() {
        Logger.LOGD(TAG + " deinit");
        mMediaPlayerLock.lock();
        mIsNeedStop = true;
        mMediaPlayerLock.unlock();
        unload();

        mSurfaceTextureLock.lock();
        mSurfaceTexture = null;
        mSurfaceTextureLock.unlock();

        return true;
    }

    /**
     * Unloads the currently loaded movie After this is called a new load() has
     * to be invoked.
     */
    public boolean unload() {
        Logger.LOGD(TAG + " unload MediaPlayer " + mPlayer);
        mMediaPlayerLock.lock();
        mCurrentState = MEDIA_STATE.NOT_READY;
        if (mPlayer != null) {
            try {
                mPlayer.release();
                mPlayer = null;
            } catch (Exception e) {
                mMediaPlayerLock.unlock();
                Logger.LOGE(TAG + " Could not release");
                e.printStackTrace();
            }
        }

        mMediaPlayerLock.unlock();
        //  Logger.LOGD(TAG + " unload MediaPlayer mThread=" + (mThread != null));
        if (mThread != null) {
            // Logger.LOGD(TAG + " stop thread");
            mThread.stopThread();
            mThread = null;
        }
        Logger.LOGD(TAG + " unload MediaPlayer mThread end=" + (mThread != null));

        mCurrentState = MEDIA_STATE.NOT_READY;
        mVideoType = MEDIA_TYPE.UNKNOWN;
        mIsRepeat = false;
        return true;
    }

    /**
     * Loads a movie from a file in the assets folder
     */
    public boolean load(String filename, int type, boolean playOnTextureImmediately, float seekPosition, boolean isRepeat) {
        Logger.LOGD(TAG + " load:" + filename + ",type=" + type + ",playOnTextureImmediately=" + playOnTextureImmediately + ",seekPosition=" + seekPosition + ",repeat=" + isRepeat);
        // Looper.prepare();
        mIsRepeat = isRepeat;
        mIsNeedStop = false;
        mIsNeedPause = false;
        MEDIA_TYPE requestedType = MEDIA_TYPE.values()[type];
        // If the client requests that we should be able to play ON_TEXTURE,
        // then we need to create a MediaPlayer:
        boolean canBeOnTexture = false;
        boolean canBeFullscreen = false;

        boolean result = false;
        mMediaPlayerLock.lock();
        mSurfaceTextureLock.lock();

        // If the media has already been loaded then exit.
        // The client must first call unload() before calling load again:
        if ((mCurrentState == MEDIA_STATE.READY) && (mPlayer != null)) {
            Logger.LOGD(TAG + " Already loaded");
        } else {
            if (((requestedType == MEDIA_TYPE.ON_TEXTURE) || (requestedType == MEDIA_TYPE.ON_TEXTURE_FULLSCREEN))
                    && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)) {
                this.mMediaTextureID = ISARGL2JNILib.initMediaTexture();
                // Logger.LOGD(TAG + " GL2JNILib init mediatexture=" + mMediaTextureID);
                if (!setupSurfaceTexture(this.mMediaTextureID)) {
                    Logger.LOGW(TAG + " Can't load file to ON_TEXTURE because the Surface Texture is not ready");
                } else {

                    try {
                        mSurface = new Surface(mSurfaceTexture);

                        // filename = "http://devimages.apple.com/samplecode/adDemo/ad.m3u8";
                        mCurrentState = MEDIA_STATE.NOT_READY;
                        canBeOnTexture = true;
                        mShouldPlayImmediately = playOnTextureImmediately;
                        String localCanPlayName = getLocalCanPlayPath(filename);
                        if (null != localCanPlayName) {
                            mCachedFilePath = localCanPlayName;
                            mHandler.sendEmptyMessage(MSG_ON_PLAYER_PREPARE);
                        } else {
                            String tarPath = ISARUnityPath.getLocalVideoPath(filename);
                            // URL url = new URL(filename);
                            mThread = new ISARDownloadThread(mParentActivity, filename, tarPath, new DownloadUpdateListener() {

                                @Override
                                public void onDownloadUpdate(int cachedSize) {
                                    mMediaPlayerLock.lock();
                                    Logger.LOGD(TAG + " onDownloadUpdate cachedSize=" + cachedSize + ",mIsCachedStop=" + mIsCachedStop + ",mCurrentState=" + mCurrentState);
                                    if (mIsCachedStop && mCurrentState == MEDIA_STATE.LOADING && !mIsNeedStop) {
                                        mIsCachedStop = false;
                                        try {
                                            mHandler.sendEmptyMessage(MSG_ON_PLAYER_ERROR_PLAY);
                                        } catch (Exception e) {
                                            mMediaPlayerLock.unlock();
                                            Logger.LOGE(TAG + " Could not start playback");
                                        }
                                    }

                                    mMediaPlayerLock.unlock();
                                }

                                @Override
                                public void onDownloadRady(int totalSize, int cachedSize, String cachedPath) {
                                    Logger.LOGD(TAG + " onDownloadRady mCachedFilePath=" + cachedPath);
                                    if (mIsNeedStop) {
                                        Logger.LOGW(TAG + " onDownloadRady need stop");
                                        return;
                                    }
                                    mIsCachedReady = true;
                                    mCachedFilePath = cachedPath;
                                    // set the cached file path for full screen, will calculate width and height
                                    ISARFilesUtil.setVideoFilePath(mCachedFilePath);
                                    try {
                                        mHandler.sendEmptyMessage(MSG_ON_PLAYER_PREPARE);
                                    } catch (IllegalArgumentException e) {
                                        e.printStackTrace();
                                    } catch (SecurityException e) {
                                        e.printStackTrace();
                                    } catch (IllegalStateException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onDownloadEnd(String cachedPath) {
                                    Logger.LOGD(TAG + " onDownloadEnd");
                                    mIsCachedEnd = true;
                                }

                                @Override
                                public void onDownloadError() {
                                    mCurrentState = MEDIA_STATE.ERROR;
                                }
                            });
                            mThread.start();
                        }
                    } catch (Exception e) {
                        Logger.LOGE(TAG + " Error while creating the MediaPlayer: " + e.toString());
                        mCurrentState = MEDIA_STATE.ERROR;
                        mMediaPlayerLock.unlock();
                        mSurfaceTextureLock.unlock();
                        return false;
                    }
                }
            } else {
                // 不支持sdk小于14的手机
                return false;
            }

            if ((requestedType == MEDIA_TYPE.FULLSCREEN) || (requestedType == MEDIA_TYPE.ON_TEXTURE_FULLSCREEN)) {
                canBeFullscreen = true;
            }
            // We store the parameters for further use
            mSeekPosition = seekPosition;

            if (canBeFullscreen && canBeOnTexture) {
                mVideoType = MEDIA_TYPE.ON_TEXTURE_FULLSCREEN;
            } else if (canBeFullscreen) {
                mVideoType = MEDIA_TYPE.FULLSCREEN;
                mCurrentState = MEDIA_STATE.READY;
            } else if (canBeOnTexture) {
                // If it is pure fullscreen then we're ready otherwise we let the MediaPlayer load first
                mVideoType = MEDIA_TYPE.ON_TEXTURE;
            } else {
                mVideoType = MEDIA_TYPE.UNKNOWN;
            }
            result = true;
        }

        mSurfaceTextureLock.unlock();
        mMediaPlayerLock.unlock();
        Logger.LOGD(TAG + "  load end result=" + result);
        return true;
    }

    /**
     * check if can play on texture.
     *
     * @return true can play on texture.
     */
    public boolean isPlayableOnTexture() {
        // Log.d(TAG , "isPlayableOnTexture");
        return (Build.VERSION.SDK_INT >= 14)
                && ((this.mVideoType == MEDIA_TYPE.ON_TEXTURE) || (this.mVideoType == MEDIA_TYPE.ON_TEXTURE_FULLSCREEN));
    }

    /**
     * check if can play full screen.
     *
     * @return true can play full screen.
     */
    public boolean isPlayableFullscreen() {
        // Log.d(TAG , "isPlayableFullscreen");
        return (this.mVideoType == MEDIA_TYPE.FULLSCREEN) || (this.mVideoType == MEDIA_TYPE.ON_TEXTURE_FULLSCREEN);
    }

    /**
     * get media player status.
     *
     * @return MEDIASTATE
     */
    public int getStatus() {
        // Log.d(TAG , "getStatus this.mCurrentState.type=" + this.mCurrentState.type);
        return this.mCurrentState.mType;
    }

    /**
     * get video width.
     *
     * @return width of video.
     */
    public int getVideoWidth() {
        Logger.LOGD(TAG + " getVideoWidth");
        if (!isPlayableOnTexture()) {
            return -1;
        }
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
        if (!isPlayableOnTexture()) {
            return -1;
        }
        if ((this.mCurrentState == MEDIA_STATE.NOT_READY) || (this.mCurrentState == MEDIA_STATE.ERROR)) {
            return -1;
        }
        return mVideoHeight;
    }

    /**
     * get video length.
     *
     * @return length of video.
     */
    public int getLength() {
        // Log.d(TAG , "getLength");
        if (!isPlayableOnTexture()) {
            return -1;
        }

        if ((this.mCurrentState == MEDIA_STATE.NOT_READY) || (this.mCurrentState == MEDIA_STATE.ERROR)) {
            return -1;
        }

        int result = -1;
        this.mMediaPlayerLock.lock();
        if (this.mPlayer != null) {
            result = (int) this.mPlayer.getDuration() / 1000;
        }
        this.mMediaPlayerLock.unlock();

        return result;
    }

    /**
     * start play video.
     *
     * @param fullScreen   true of false.
     * @param seekPosition position of video.
     * @return true play started.
     */
    public boolean play(boolean fullScreen, float seekPosition) {
        Logger.LOGD(TAG + " play fullScreen=" + fullScreen + ", seekPosition=" + seekPosition);
        if (fullScreen) {
            if (!isPlayableFullscreen()) {
                Logger.LOGE(TAG + " Cannot play this video fullscreen, it was not requested on load");
                return false;
            }
            return true;
        }

        if (!isPlayableOnTexture()) {
            Logger.LOGE(TAG + " Cannot play this video on texture, it was either not requested on load or is not supported on this plattform");
            return false;
        }

        if ((this.mCurrentState == MEDIA_STATE.NOT_READY) || (this.mCurrentState == MEDIA_STATE.ERROR)) {
            Logger.LOGE(TAG + " Cannot play this video if it is not ready");
            return false;
        }

        this.mMediaPlayerLock.lock();
        mIsNeedPause = false;
        if (seekPosition != -1.0F) {
            try {
                this.mPlayer.seekTo((int) seekPosition * 1000);
            } catch (Exception e) {
                this.mMediaPlayerLock.unlock();
                Logger.LOGE(TAG + " Could not seek to position");
            }
        } else if (this.mCurrentState == MEDIA_STATE.REACHED_END) {
            try {
                this.mPlayer.seekTo(0);
            } catch (Exception e) {
                this.mMediaPlayerLock.unlock();
                Logger.LOGE(TAG + " Could not seek to position");
            }
        }

        try {
            // this.mMediaPlayer.start();
            this.mCurrentState = MEDIA_STATE.PLAYING;
            mPlayer.setPlayWhenReady(true);
        } catch (Exception e) {
            this.mMediaPlayerLock.unlock();
            Logger.LOGE(TAG + " Could not start playback");
        }
        this.mMediaPlayerLock.unlock();
        return true;
    }

    /**
     * pause media player.
     *
     * @return true paused.
     */
    public boolean pause() {
        Logger.LOGD(TAG + " pause");
        if (!isPlayableOnTexture()) {
            return false;
        }

        if ((this.mCurrentState == MEDIA_STATE.NOT_READY) || (this.mCurrentState == MEDIA_STATE.ERROR)) {
            return false;
        }

        boolean result = false;
        this.mMediaPlayerLock.lock();
        if (this.mPlayer != null) {
            /*if (this.mMediaPlayer.isPlaying()) {
                try {
                    this.mMediaPlayer.pause();
                } catch (Exception e) {
                    this.mMediaPlayerLock.unlock();
                    Logger.LOGE(TAG + " Could not pause playback");
                }
            }*/
            this.mCurrentState = MEDIA_STATE.PAUSED;
            mPlayer.setPlayWhenReady(false);
            result = true;
        }
        this.mMediaPlayerLock.unlock();
        // Log.d(TAG , "pause end mCurrentState=" + mCurrentState);
        return result;
    }

    /**
     * stop media player.
     *
     * @return true stopped.
     */
    public boolean stop() {
        Logger.LOGD(TAG + " stop");
        if (!isPlayableOnTexture()) {
            return false;
        }

        if ((this.mCurrentState == MEDIA_STATE.NOT_READY) || (this.mCurrentState == MEDIA_STATE.ERROR)) {
            return false;
        }

        boolean result = false;
        this.mMediaPlayerLock.lock();
        if (this.mPlayer != null) {
            this.mCurrentState = MEDIA_STATE.STOPPED;
            this.mPlayer.setPlayWhenReady(false);
            result = true;
        }
        this.mMediaPlayerLock.unlock();
        return result;
    }

    /**
     * called by unity.
     * if mediaplayer is loading(not playing state), mediaplayer will not stop is unity paused.
     *
     * @return true ok.
     */
    public boolean videoPauseApplication(boolean cannotPlayer) {
        Logger.LOGD(TAG + " videoPauseApplication");
        if (!isPlayableOnTexture()) {
            return false;
        }

        boolean result = false;
        this.mMediaPlayerLock.lock();
        mIsNeedPause = cannotPlayer;
        result = true;
        this.mMediaPlayerLock.unlock();
        Logger.LOGD(TAG + " videoPauseApplication mIsNeedPause=" + mIsNeedPause);
        return result;
    }

    /**
     * Tells the VideoPlayerHelper to update the data from the video feed.
     *
     * @return MEDIASTATE.
     */
    public int updateVideoData() {
        // Logger.LOGD(TAG + " updateVideoData ");
        if (!isPlayableOnTexture()) {
            // DebugLog.LOGD("Cannot update the data of this video since it is not on texture");
            return -1;
        }

        // int result = MEDIA_STATE.NOT_READY.type;

        mSurfaceTextureLock.lock();
        if (mSurfaceTexture != null) {
            // Only request an update if currently playing
            if (mCurrentState == MEDIA_STATE.PLAYING) {
                mSurfaceTexture.updateTexImage();
                mSurfaceTexture.getTransformMatrix(mMtx);
                // GL2JNILib.copyTexture(this.mMediaTextureID, this.mDestTextureID, this.mFBO, mMtx, this.mMediaPlayer.getVideoWidth(), this.mMediaPlayer.getVideoHeight());
                ISARGL2JNILib.copyTexture(this.mMediaTextureID, this.mDestTextureID, this.mFBO, mMtx, mVideoWidth, mVideoHeight);
                // Logger.LOGD(TAG + " updateVideoData ");
            }
            // result = this.mCurrentState.type;
        }
        mSurfaceTextureLock.unlock();

        return this.mCurrentState.mType;
    }

    /**
     * seek to position.
     *
     * @param position position of video.
     * @return true seek.
     */
    public boolean seekTo(float position) {
        // Log.d(TAG , "seekTo");
        if (!isPlayableOnTexture()) {
            return false;
        }

        if ((this.mCurrentState == MEDIA_STATE.NOT_READY) || (this.mCurrentState == MEDIA_STATE.ERROR)) {
            return false;
        }

        boolean result = false;
        this.mMediaPlayerLock.lock();
        if (this.mPlayer != null) {
            try {
                this.mPlayer.seekTo((int) position * 1000);
            } catch (Exception e) {
                this.mMediaPlayerLock.unlock();
                Logger.LOGE(TAG + " Could not seek to position");
            }
            result = true;
        }
        this.mMediaPlayerLock.unlock();

        return result;
    }

    /**
     * get current video position.
     *
     * @return position of video.
     */
    public float getCurrentPosition() {
        // Log.d(TAG , "getCurrentPosition");
        if (!isPlayableOnTexture()) {
            return -1.0F;
        }

        if ((this.mCurrentState == MEDIA_STATE.NOT_READY) || (this.mCurrentState == MEDIA_STATE.ERROR)
                || (this.mCurrentState == MEDIA_STATE.LOADING)) {
            return -1.0F;
        }

        float result = -1.0F;
        this.mMediaPlayerLock.lock();
        if (this.mPlayer != null)
            result = this.mPlayer.getCurrentPosition() / 1000.0F;
        mSeekPosition = result;
        this.mMediaPlayerLock.unlock();

        return result;
    }

    /**
     * set volume of media player.
     *
     * @param value the volume.
     * @return true ok.
     */
    public boolean setVolume(float value) {
        // Log.d(TAG , "setVolume");
        if (!isPlayableOnTexture()) {
            return false;
        }

        if ((this.mCurrentState == MEDIA_STATE.NOT_READY) || (this.mCurrentState == MEDIA_STATE.ERROR)) {
            return false;
        }

        boolean result = false;
        this.mMediaPlayerLock.lock();
        if (this.mPlayer != null) {
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

    /**
     * Used to set up the surface texture.
     *
     * @param nativeTextureID native texture.
     * @return true OK.
     */
    private boolean setupSurfaceTexture(int nativeTextureID) {
        // Log.d(TAG , "setupSufaceTexture");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // We create a surface texture where the video can be played
            // We have to give it a texture id of an already created (in native) OpenGL texture
            mSurfaceTextureLock.lock();
            mSurfaceTexture = new SurfaceTexture(nativeTextureID);
            mSurfaceTextureLock.unlock();

            return true;
        } else {
            return false;
        }
    }

/*    public boolean onError(MediaPlayer mp, int what, int extra) {
        Logger.LOGE(TAG + " Error while opening the file. Unloading the media player what=" + what + ",extra=" + extra);
        mMediaPlayerLock.lock();
        Logger.LOGE(TAG + " Error but station is " + getCurrentPosition() + ",mIsCachedEnd=" + mIsCachedEnd);

        if (!mIsCachedStop && mIsCachedReady && !mIsNeedStop) {
            this.mCurrentState = MEDIA_STATE.LOADING;
            mIsCachedStop = true;
            mMediaPlayer.reset();
            // this.mCurrentState = MEDIA_STATE.LOADING;
            if (mIsCachedEnd) {
                try {
                    mIsCachedStop = false;
                    mMediaPlayer.setDataSource(mCachedFilePath);
                    mMediaPlayer.prepareAsync();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            this.mCurrentState = MEDIA_STATE.ERROR;
        }
        mMediaPlayerLock.unlock();
        return true;
    }

    public void onCompletion(MediaPlayer mp) {
        this.mCurrentState = MEDIA_STATE.REACHED_END;
        if (mIsRepeat) {
            play(false, DEFAULT_POSITION);
        }
    }

    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Logger.LOGD(TAG + " onBufferingUpdate percent=" + percent);
        this.mMediaPlayerLock.lock();
        if (this.mMediaPlayer != null) {
            if (mp == this.mMediaPlayer) {
                this.mCurrentBufferingPercentage = percent;
            }
        }
        this.mMediaPlayerLock.unlock();
    }*/

    DemoPlayer.Listener demoListener = new DemoPlayer.Listener() {

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            Logger.LOGD(TAG + " onVideoSizeChanged width=" + width + ",height=" + height);
            mVideoWidth = width;
            mVideoHeight = height;
            mCurrentState = MEDIA_STATE.READY;
            ISARFilesUtil.setVIDEO_WIDTH(width);
            ISARFilesUtil.setVIDEO_HEIGHT(height);
        }

        @Override
        public void onStateChanged(boolean playWhenReady, int playbackState) {
            Logger.LOGD(TAG + " onStateChanged playWhenReady=" + playWhenReady + ",playbackState=" + playbackState + ",mCurrentState=" + mCurrentState);
            String text = "";
            switch (playbackState) {
                case ExoPlayer.STATE_BUFFERING:
                    text += "buffering";
                    mCurrentState = MEDIA_STATE.LOADING;
                    break;
                case ExoPlayer.STATE_ENDED:
                    text += "ended";
                    mCurrentState = MEDIA_STATE.REACHED_END;
                    if (mIsRepeat) {
                        mHandler.sendEmptyMessage(MSG_ON_PLAYER_PLAY_REPEAT);
                    }
                    break;
                case ExoPlayer.STATE_IDLE:
                    text += "idle";
                    mCurrentState = MEDIA_STATE.LOADING;
                    break;
                case ExoPlayer.STATE_PREPARING:
                    text += "preparing";
                    break;
                case ExoPlayer.STATE_READY:
                    text += "ready";
                    if (playWhenReady) {
                        mCurrentState = MEDIA_STATE.PLAYING;
                    } else {
                        if (mCurrentState != MEDIA_STATE.PAUSED && mShouldPlayImmediately && !mIsNeedPause) {
                            mHandler.sendEmptyMessage(MSG_ON_PLAYER_PLAY);
                        }
                    }
                    break;
                default:
                    text += "unknown";
                    break;
            }
            Logger.LOGD(TAG + " onStateChanged text=" + text);
        }

        @Override
        public void onError(Exception e) {
            Logger.LOGD(TAG + " onError --" + getCurrentPosition());
            String errorString = null;
            if (e instanceof UnsupportedDrmException) {
                // Special case DRM failures.
                UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
                errorString = "drm unkown";
            } else if (e instanceof ExoPlaybackException
                    && e.getCause() instanceof DecoderInitializationException) {
                // Special case for decoder initialization failures.
                DecoderInitializationException decoderInitializationException =
                        (DecoderInitializationException) e.getCause();
                if (decoderInitializationException.decoderName == null) {
                    if (decoderInitializationException.getCause() instanceof DecoderQueryException) {
                        errorString = "Unable to query device decoders";
                    } else if (decoderInitializationException.secureDecoderRequired) {
                        errorString = "This device does not provide a secure decoder for";
                    } else {
                        errorString = "no decodeer";
                    }
                } else {
                    errorString = "error_instantiating_decoder";
                }
            } else {
                Logger.LOGD(TAG + " onError;" + e.getCause().getMessage());
            }
            if (errorString != null) {
                Logger.LOGD(TAG + " onError " + errorString);
            }
//            mHandler.sendEmptyMessageDelayed(3, 20000);
            mMediaPlayerLock.lock();
            if (!mIsCachedStop && mIsCachedReady && !mIsNeedStop) {
                playerNeedsPrepare = true;
                mCurrentState = MEDIA_STATE.LOADING;
                mIsCachedStop = true;
//                this.mCurrentState = MEDIA_STATE.LOADING;
                if (mIsCachedEnd) {
                    mIsCachedStop = false;
                    mHandler.sendEmptyMessage(MSG_ON_PLAYER_ERROR_PLAY);
                }
            } else {
                mCurrentState = MEDIA_STATE.ERROR;
            }
            mMediaPlayerLock.unlock();
        }
    };

    /*@Override
    public void onPrepared(MediaPlayer mp) {
        Logger.LOGD(TAG + "  onPrepared:");
        mCurrentState = MEDIA_STATE.READY;
        // int duration = mMediaPlayer.getDuration();
        // mBufferPerMiSecond = 2 * mVideoTotalSize / duration;
        // Logger.LOGD(TAG + " onPrepared mVBufferPerMiSecond=" + mBufferPerMiSecond
        // +",mVideoTotalSize=" + mVideoTotalSize + ",duration=" + duration);
        // If requested an immediate play
        if (mShouldPlayImmediately) {
            play(false, mSeekPosition);
        }
        mVideoWidth = this.mMediaPlayer.getVideoWidth();
        mVideoHeight = this.mMediaPlayer.getVideoHeight();
        // mSeekPosition = 0;
    }*/

    /**
     * get surface texture matrix.
     *
     * @param mtx matrix of surface texture.
     */
    public void getSurfaceTextureTransformMatrix(float[] mtx) {
        Logger.LOGD(TAG + " getSurfaceTextureTransformMatrix");
        mSurfaceTextureLock.lock();
        if (mSurfaceTexture != null) {
            mSurfaceTexture.getTransformMatrix(mtx);
        }
        mSurfaceTextureLock.unlock();
    }

    /**
     * set video texture.
     *
     * @param textureID texture id of video surface.
     * @return true ok.
     */
    public boolean setVideoTextureID(int textureID) {
        Logger.LOGD(TAG + " setVideoTextureID");
        if ((!isPlayableOnTexture()) || (this.mPlayer == null)) {
            Logger.LOGE(TAG + " Cannot set the video texture ID if it is not playable on texture");
            return false;
        }

        this.mDestTextureID = textureID;
        if ((mVideoWidth > 0) && (mVideoHeight > 0)) {
            mFBO = ISARGL2JNILib.initFBO(mDestTextureID, mVideoWidth, mVideoHeight);
            return true;
        }
        return false;
    }

    /**
     * set parent activity.
     *
     * @param newActivity activity.
     */
    public void setActivity(Activity newActivity) {
        // Log.d(TAG , "setActivity");
        this.mParentActivity = newActivity;
        mHandler = new Handler(mParentActivity.getMainLooper()) {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case MSG_ON_PLAYER_PREPARE:
                        preparePlayer(false);
                        break;
                    case MSG_ON_PLAYER_PLAY:
                        if (null != mPlayer) {
                            mPlayer.setPlayWhenReady(true);
                        }
                        break;
                    case MSG_ON_PLAYER_ERROR_PLAY:
                        Logger.LOGD(TAG + "mHanlder 3--------");
                        if (playerNeedsPrepare) {
                            mPlayer.prepare();
                            playerNeedsPrepare = false;
                        }
                        play(false, mSeekPosition);
                        break;
                    case MSG_ON_PLAYER_PLAY_REPEAT:
                        play(false, 0);
                        break;

                    default:
                        break;
                }
            }
        };
    }

    /**
     * get local path of video URL.
     *
     * @param fileName URL.
     * @return path of local name.
     */
    public String getLocalCanPlayPath(String fileName) {
        if (!fileName.startsWith("http")) {
            return fileName;
        } else {
            // String localName = fileName.substring(fileName.lastIndexOf("/") + 1);
            String filePath = ISARUnityPath.getLocalVideoPath(fileName);
            File file = new File(filePath);
            if (file.exists()) {
                return filePath;
            }
        }
        return null;
    }

    private void preparePlayer(boolean playWhenReady) {
        if (mPlayer == null) {
            mPlayer = new DemoPlayer(getRendererBuilder());
            mPlayer.addListener(demoListener);
            playerNeedsPrepare = true;
        }
        if (playerNeedsPrepare) {
            mPlayer.prepare();
            playerNeedsPrepare = false;
        }
        mPlayer.setSurface(mSurface);
        mPlayer.setPlayWhenReady(playWhenReady);
    }

    private DemoPlayer.RendererBuilder getRendererBuilder() {
        String userAgent = Util.getUserAgent(mParentActivity, "ExoPlayerDemo");
        Logger.LOGD(TAG + " getRendererBuilder userAgent=" + userAgent + ",mCachedFilePath=" + mCachedFilePath);
        if (mCachedFilePath.endsWith(".m3u8")) {
//            return new HlsRendererBuilder(IdseeARApplication.getInstance(), userAgent, "/sdcard/11/ad.m3u8");
            return new HlsRendererBuilder(mParentActivity, userAgent, mCachedFilePath);
        } else {
            return new ExtractorRendererBuilder(mParentActivity, userAgent, Uri.parse(mCachedFilePath));
        }
    }

    Handler mHandler;
}
