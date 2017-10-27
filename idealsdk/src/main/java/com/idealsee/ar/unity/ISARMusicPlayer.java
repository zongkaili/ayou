/**
 * Copyright © 2015成都理想境界科技有限公司. All rights reserved.
 * 项目名称: Idealsee-AR3
 * 类名称: MusicPlayer
 * 类描述:
 * 创建人: hongen
 * 创建时间: 2015-7-7 下午8:30:39
 * 修改人:
 * 修改时间:
 * 备注:
 *
 * @version
 */

package com.idealsee.ar.unity;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;

import com.idealsee.sdk.util.Logger;

import java.io.IOException;

import static com.unity3d.player.UnityPlayer.UnitySendMessage;

/**
 * Class for unity play music.
 *
 * @author idealsee
 *
 */
public class ISARMusicPlayer {
    private static final String TAG = "[MusicPlayer]";
    private MediaPlayer mMusicPlayer;
    private boolean mIsPlayMusic;
    private boolean mIsPauseMusic;
    private boolean mIsRepeat = false;
    private String mMusicPath;

    /**
     * Constructor.
     */
    public ISARMusicPlayer() {
        Logger.LOGD(TAG + " MusicPlayer");
    }

    /**
     * Constructor.
     *
     * @param musicURL
     *            music url
     */
    public ISARMusicPlayer(String musicURL) {
        Logger.LOGD(TAG + " MusicPlayerInit musicURL=" + musicURL);
        mMusicPath = musicURL;
        mMusicPlayer = new MediaPlayer();
        mMusicPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMusicPlayer.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Logger.LOGD(TAG + " onPrepared : mIsPauseMusic=" + mIsPauseMusic);
                if (mIsPauseMusic) {
                    return;
                }
                mIsPlayMusic = true;
                mp.start();
            }
        });
        mMusicPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Logger.LOGD(TAG + " onCompletion mIsRepeat=" + mIsRepeat);
                if (mIsRepeat) {
                    mp.start();
                }
                mIsPlayMusic = true;
                mIsPauseMusic = true;
            }
        });
        mMusicPlayer.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Logger.LOGD(TAG + " onError");
                mp.reset();
                mIsPlayMusic = false;
                mIsPauseMusic = true;
                return false;
            }
        });
    }

    /**
     * Constructor for app to play default sound effect.
     *
     * @param context
     *            context
     * @param rawId
     *            raw id
     */
    public ISARMusicPlayer(Context context, int rawId) {
        Logger.LOGD(TAG + " MusicPlayerInit rawId=" + rawId);
        mMusicPlayer = MediaPlayer.create(context, rawId);
        mMusicPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMusicPlayer.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Logger.LOGD(TAG + " onPrepared : mIsPauseMusic=" + mIsPauseMusic);
                if (mIsPauseMusic) {
                    return;
                }
                mIsPlayMusic = true;
                mIsPauseMusic = true;
                // mp.start();
            }
        });
        mMusicPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Logger.LOGD(TAG + " onCompletion mIsRepeat=" + mIsRepeat);
                if (mIsRepeat) {
                    mp.start();
                }
                mIsPlayMusic = true;
                mIsPauseMusic = true;
            }
        });
        mMusicPlayer.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Logger.LOGD(TAG + " onError");
                mp.reset();
                mIsPlayMusic = false;
                mIsPauseMusic = true;
                return false;
            }
        });
    }

    /**
     * Unit call back.
     *
     * @param musicURL
     *            music url
     * @return true or false
     */
    public boolean MusicPlayerInit(String musicURL, final boolean autoPlay) {
        Logger.LOGD(TAG + " MusicPlayerInit musicURL=" + musicURL + ",autoPlay=" + autoPlay);
        mMusicPath = musicURL;
        if (null != mMusicPlayer) {
            Logger.LOGD(TAG + " MusicPlayerInit mMusicPlayer not null");
            return true;
        }
        mIsPauseMusic = false;
        mMusicPlayer = new MediaPlayer();
        mMusicPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMusicPlayer.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Logger.LOGD(TAG + " onPrepared : mIsPauseMusic=" + mIsPauseMusic);
                if (mIsPauseMusic) {
                    return;
                }
                mIsPlayMusic = true;
                if (autoPlay) {
                    mp.start();
                } else {
                    mIsPauseMusic = true;
                }
            }
        });
        mMusicPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Logger.LOGD(TAG + " onCompletion mIsRepeat=" + mIsRepeat);
                if (mIsRepeat) {
                    mp.start();
                } else {
                    // 如果不是循环播放，结束时需要告诉Unity, Unity会停止图标旋转.
                    UnitySendMessage("ARTheme(Clone)", "AudioPlayerDidFinish", mMusicPath);
                    mIsPauseMusic = true;
                }
            }
        });
        mMusicPlayer.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Logger.LOGD(TAG + " onError");
                mp.reset();
                mIsPlayMusic = false;
                mIsPauseMusic = true;
                return false;
            }
        });

        return true;
    }

    /**
     * load music.
     *
     * @param repeat
     *            repeat or not
     * @return true or false
     */
    public boolean LoadMusic(boolean repeat) {
        Logger.LOGD(TAG + " LoadMusic repeat=" + repeat);
        try {
            mIsRepeat = repeat;
            mMusicPlayer.reset();
            mMusicPlayer.setDataSource(mMusicPath);
            mMusicPlayer.prepareAsync();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * play music.
     */
    public void PlayMusic() {
        Logger.LOGD(TAG + " PlayMusic");
        if (mIsPlayMusic && mIsPauseMusic && (null != mMusicPlayer)) {
            mIsPauseMusic = false;
            mMusicPlayer.start();
        }
    }

    /**
     * pause music.
     */
    public void PauseMusic() {
        Logger.LOGD(TAG + " PauseMusic mIsPlayMusic=" + mIsPlayMusic + ",mIsPauseMusic=" + mIsPauseMusic);
        if (mIsPlayMusic && !mIsPauseMusic && (null != mMusicPlayer)) {
            mIsPauseMusic = true;
            mMusicPlayer.pause();
        }
    }

    /**
     * stop music.
     */
    public void StopMusic() {
        Logger.LOGD(TAG + " StopMusic");
        if (mIsPlayMusic && !mIsPauseMusic && (null != mMusicPlayer)) {
            mIsPauseMusic = true;
            mMusicPlayer.stop();
        }
    }

    /**
     * resume music.
     */
    public void ResumeMusic() {
        Logger.LOGD(TAG + " ResumeMusic : mIsPlayMusic=" + mIsPlayMusic + ",mIsPauseMusic=" + mIsPauseMusic);
        if (mIsPlayMusic && mIsPauseMusic && (null != mMusicPlayer)) {
            mIsPauseMusic = false;
            mMusicPlayer.start();
        }
    }

    /**
     * release music.
     */
    public void DeinitMusic() {
        Logger.LOGD(TAG + " DeinitMusic");
        if (mIsPlayMusic) {
            mMusicPlayer.reset();
            mMusicPlayer.release();
            mIsPlayMusic = false;
            mIsPauseMusic = true;
            mMusicPlayer = null;
        }
    }

    /**
     * start player.
     *
     * @param url
     *            url to play
     */
    public void startPlayer(String url) {
        try {
            mMusicPath = url;
            mIsPauseMusic = false;
            mMusicPlayer.reset();
            mMusicPlayer.setDataSource(url);
            mMusicPlayer.prepareAsync();
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
}
