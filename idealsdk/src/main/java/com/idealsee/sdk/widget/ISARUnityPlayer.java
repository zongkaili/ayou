package com.idealsee.sdk.widget;

import android.content.ContextWrapper;

import com.idealsee.sdk.util.Logger;
import com.unity3d.player.UnityPlayer;

/**
 * Created by isee on 17-3-15.
 */

public class ISARUnityPlayer extends UnityPlayer {

    public ISARUnityPlayer(ContextWrapper var1) {
        super(var1);
    }

    @Override
    protected void kill() {
//        super.kill();
        Logger.LOGD("ISARUnityPlayer not kill");
        currentActivity = null;
    }
}
