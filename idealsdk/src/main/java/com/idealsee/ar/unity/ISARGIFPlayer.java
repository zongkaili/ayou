/**
 * Copyright © 2014成都理想境界科技有限公司. All rights reserved.
 * 项目名称: Idealsee-AR2
 * 类名称: gifPlayer
 * 类描述:
 * 创建人: ly
 * 创建时间: 2014年3月25日 上午10:56:14
 * 修改人:
 * 修改时间:
 * 备注:
 *
 * @version
 */

package com.idealsee.ar.unity;

import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.text.TextUtils;

import com.idealsee.sdk.util.ISARGIFDecoder;
import com.idealsee.sdk.util.Logger;

/**
 * Class for unity play gif.
 *
 * @author ly
 */
public class ISARGIFPlayer {
    private static final String TAG = "[gifPlayer]";
    private boolean mIsDecodedGif = false;
    private boolean mIsPlayingGif = false; // 解码gif, 播放gif
    private boolean mIsConsumed = true;
    private int mGifTextureId = 0;
    private int mCurrentId = 0;
    private long mLastGifLoad = 0; // 上一次加载gif的时间
    private int mDefaultDelay = 100;
    private int mLoopCount = 0;
    private String mFilePath = null;
    private ISARGIFDecoder mISARGIFDecoder;
    private ISARGIFDecoder.Frame mCurFrame;

    public ISARGIFPlayer() {
        Logger.LOGD(TAG + " gifPlayer()");
    }

    /**
     * Unity回调,加载指定路径的gif文件.
     *
     * @param path path
     * @return true or false
     */
    public boolean LoadGifDataWithPath(final String path) {
        mFilePath = path;
        Logger.LOGD(TAG + " LoadGifDataWithPath=" + path);
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        if (mISARGIFDecoder != null) {
            mISARGIFDecoder.recycle();
        }
        if (TextUtils.isEmpty(mFilePath))
            return false;

        // 使用jni里面的方法查找文件，经过测试带file前缀会找不到文件
        if (mFilePath.startsWith("file://")) {
            mFilePath = mFilePath.replace("file://", "");
        }
        mISARGIFDecoder = new ISARGIFDecoder(mFilePath);
        mCurFrame = mISARGIFDecoder.getFirstFrame();
        mLoopCount = mISARGIFDecoder.getLoopCount();
        Logger.LOGD(TAG + " LoadGifDataWithPath end=" + mCurFrame.mIndex + ",loop=" + mLoopCount);
        mIsDecodedGif = true;
        return true;

    }

    /**
     * Unity回调,设置gif纹理id.
     *
     * @param id texture id
     * @return true or false
     */
    public boolean SetGifTextureId(int id) {
        Logger.LOGI(TAG + " SetGifTextureId:" + id);
        this.mGifTextureId = id;
        return true;
    }

    /**
     * Unity回调,开始gif动画.
     *
     * @return true or false
     */
    public boolean StartPlay() {
        Logger.LOGI(TAG + " StartPlay, isDecodedGif: " + mIsDecodedGif);
        if (mGifTextureId != -1) {
            mIsPlayingGif = true;
        }
        return true;
    }

    /**
     * Unity回调,停止播放gif.
     *
     * @return true or false
     */
    public synchronized boolean StopPlay() {
        Logger.LOGI(TAG + " StopPlay, isDecodedGif: " + mIsDecodedGif);
        mIsPlayingGif = false;
        mIsDecodedGif = false;
        mIsConsumed = false;
        // mGifDecoder.recycle();
        mISARGIFDecoder = null;
        return true;
    }

    /**
     * Unity回调,重新播放gif, 如果还未decode, 返回false.
     *
     * @return true or false
     */
    public synchronized void RewindGIF() {
        if (!mIsDecodedGif) {
            Logger.LOGW(TAG + " StartReplay gif is not decoded.");
            return;
        }
        mIsPlayingGif = false;
        mIsConsumed = false;
        mCurFrame = mISARGIFDecoder.getFirstFrame();
        mIsPlayingGif = true;
    }

    /**
     * Unity回调.
     *
     * @return texture id
     */

    public synchronized int GetExternalTexture() {
        if (mIsDecodedGif && mIsPlayingGif/* && mGifTextureId != -1*/) {
            synchronized (this) {
                if (mIsConsumed && mLastGifLoad != 0) {
                    mCurFrame = mISARGIFDecoder.getNextFrame();
                    mDefaultDelay = mCurFrame.mDelay;
                }
                long now = System.currentTimeMillis();
                if (now - mLastGifLoad < mDefaultDelay) {
                    mIsConsumed = false;
                    return 0;
                }

                mLastGifLoad = now;
                if (mGifTextureId != -1) {
                    GLES20.glDeleteTextures(1, new int[]{mGifTextureId}, 0);
                }
                int[] tempTextures = new int[1];
                GLES20.glGenTextures(1, tempTextures, 0);
                int tempTexture = tempTextures[0];
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tempTexture);
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mCurFrame.mBitmap, 0);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

                mGifTextureId = tempTexture;
                Logger.LOGD(TAG + " GetExternalTexture, mGifTextureId =" + mGifTextureId);
                mIsConsumed = true;
            }

        }

        return mGifTextureId;
    }

}
