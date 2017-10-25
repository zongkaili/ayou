/**
 * Copyright © 2015成都理想境界科技有限公司. All rights reserved.
 *
 * @version
 */

package com.yixun.sdk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.yixun.sdk.R;
import com.yixun.sdk.media.videoplayer.ISARUserAction;
import com.yixun.sdk.media.videoplayer.ISARVideoPlayer;
import com.yixun.sdk.media.videoplayer.ISARVideoPlayerStandard;
import com.yixun.sdk.util.ISARConstants;
import com.yixun.sdk.util.Logger;

import java.io.File;

/**
 * ar video full screen player.
 * If video is played by full screen, app will use this class.
 *
 * @author zongkaili
 */
public class ISARVideoPlayerActivity extends AppCompatActivity {
    private String mVideoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transparent);

        Intent intent = getIntent();
        if (null != intent.getStringExtra("ideal_video_path")) {
            String md5 = intent.getStringExtra("ideal_video_path");
            if (md5.startsWith("file:") || md5.startsWith("http")) {
                mVideoPath = md5;
            } else {
                String suffix = "." + md5.substring(md5.indexOf("_") + 1, md5.length());
                mVideoPath = ISARConstants.UNITY_RESOURCES_DIRETCTORY + File.separator + md5 + suffix;
            }
            Logger.LOGD("VideoPlayer mVideoPath " + mVideoPath + " , md5 : " + md5);
        }

        ISARVideoPlayerStandard.startFullscreen(this,
                ISARVideoPlayerStandard.class,
                mVideoPath,
                "");

        ISARVideoPlayerStandard.setJcUserAction(new ISARUserAction() {
            @Override
            public void onEvent(int type, String url, int screen, Object... objects) {
                if (type == ISARUserAction.ON_BACKPRESS_ENTIRELY)
                    finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (ISARVideoPlayer.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ISARVideoPlayer.releaseAllVideos();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
