package com.yixun.sdk.util;

import android.app.Activity;
import android.content.res.Configuration;

import com.yixun.sdk.widget.ISARUnityPlayer;
import com.unity3d.player.UnityPlayer;

/**
 * Created by isee on 17-3-13.
 * 需要在UIThread进行API调用
 */

public class ISARUnityTool {
    private static ISARUnityPlayer mUnityPlayer;
    private static ISARUnityTool mUnityTool;

    private ISARUnityTool() {}

    public static synchronized ISARUnityTool getInstance() {
        if (null == mUnityTool) {
            mUnityTool = new ISARUnityTool();
        }
        return mUnityTool;
    }

    public void initUnityPlayer(Activity activity) {
        if (null == mUnityPlayer) {
            mUnityPlayer = new ISARUnityPlayer(activity);
        }
    }

    public UnityPlayer getUnityPlayer() {
        return mUnityPlayer;
    }

    public void resumeUnityPlayer() {
        if (null != mUnityPlayer) {
            mUnityPlayer.resume();
        }
    }

    public void pauseUnityPlayer() {
        if (null != mUnityPlayer) {
            mUnityPlayer.pause();
        }
    }

    public void destroyUnityPlayer() {
        if (null != mUnityPlayer) {
            mUnityPlayer.quit();
            mUnityPlayer = null;
        }
    }

    public void configurationChanged(Configuration newConfig) {
        if (null != mUnityPlayer) {
            mUnityPlayer.configurationChanged(newConfig);
        }
    }

    public void windowFocusChanged(boolean hasFocus) {
        if (null != mUnityPlayer) {
            mUnityPlayer.windowFocusChanged(hasFocus);
        }
    }

    public void requestFocus() {
        if (null != mUnityPlayer) {
            mUnityPlayer.requestFocus();
        }
    }
}
