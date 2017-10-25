package com.yixun.sdk.demo;

import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Toast;

import com.yixun.sdk.activity.ISARSDKActivity;
import com.yixun.sdk.demo.list.MainFragment;
import com.yixun.sdk.demo.list.RandomListFragment;
import com.yixun.sdk.model.ISARRandomInfo;
import com.yixun.sdk.util.ISARTipsUtil;
import com.yixun.sdk.util.Logger;
import com.unity3d.player.UnityPlayer;

import java.io.File;
import java.util.List;

public class SDKDemoActivity extends ISARSDKActivity {
    private static final String TAG = SDKDemoActivity.class.getSimpleName();
    private WakeLock mWakeLock;
    private RandomListFragment mListFragment;
    private MainFragment mMainFragment;
    private Context mContext;
    private double mCurrentLon = 104.07687;
    private double mCurrentLat = 30.546157;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.LOGD(TAG + " onCreate set listener");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "Unity-WakeLock");

        mListFragment = new RandomListFragment();
        mMainFragment = new MainFragment();
        mContext = getApplicationContext();
        setSearchArea(Double.toString(mCurrentLon), Double.toString(mCurrentLat));
        //离线识别时开启
//        enableOffLineRecognition(true);
//        String resourcePath = getExternalFilesDir(null) + File.separator + "Resources";
//        setOfflinePath(resourcePath);
    }

    @Override
    public void onIdealAddCustomView(final UnityPlayer unityView) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewGroup unityParentView = (ViewGroup) (unityView.getParent());
                View myView = getLayoutInflater().inflate(R.layout.activity_idealsee_ar, null);
                unityParentView.addView(myView, new LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT));

                setSearchArea(String.valueOf(mCurrentLon), String.valueOf(mCurrentLat));

                setPlayARMatchedSound(true);
                enableLocalRecognition(true);
                doMainPage();
            }
        });
    }

    @Override
    public void onIdealThemeStatus(int status) {
        switch (status) {
            case ISAR_CAMERA_INIT:
                Logger.LOGD(TAG + " idealThemeStatus camera opened");
                break;
            case ISAR_AR_DESTROYED:
                Logger.LOGD(TAG + " idealThemeStatus ar destroyed");
                break;
            case ISAR_AR_FROM_LIST:
                Logger.LOGD(TAG + " idealThemeStatus came from list");
                break;
            case ISAR_AR_INIT:
                Logger.LOGD(TAG + " idealThemeStatus start load animation");
                break;
            case ISAR_AR_INIT_LOAD_DATA:
                Logger.LOGD(TAG + " idealThemeStatus start init load data");
                break;
            case ISAR_AR_INIT_LOAD_DATA_DONE:
                Logger.LOGD(TAG + " idealThemeStatus end init load data");
                break;
            case ISAR_AR_START_AR:
                Logger.LOGD(TAG + " idealThemeStatus start ar theme");
                break;
        }

    }


    @Override
    public void onIdealARPageCount(final int pageCount) {
        Logger.LOGD(TAG + "onIdealARPageCount=" + pageCount);
        if (null != mMainFragment) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMainFragment.setPageCount(pageCount);
                }
            });
        }
    }

    @Override
    public void onIdealARShowPageIndex(final int pageIndex) {
        Logger.LOGD(TAG + "onIdealARShowPageIndex=" + pageIndex);
        if (null != mMainFragment) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMainFragment.showPageIndex(pageIndex);
                }
            });
        }
    }

    @Override
    public void onIdealGetAnimation(boolean isSuccess, ISARRandomInfo randomInfo, String animation) {
        if (!isSuccess) {
            Logger.LOGE(TAG + " onIdealGetAnimation error.");
            doStopARTheme();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ISARTipsUtil.makeToast(getApplicationContext(), "ar is wrong, scan other ar.", Toast.LENGTH_SHORT);
                }
            });
        }
        Logger.LOGD(TAG + " onIdealGetAnimation");
    }

    @Override
    public void onIdealKeyAuthFailed(final int errorCode) {
        Logger.LOGD(TAG + " onIdealseeKeyAuthFailed--");
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (errorCode == 0) {
                    ISARTipsUtil.makeToast(mContext, "授权成功", Toast.LENGTH_SHORT);
                } else {
                    ISARTipsUtil.makeToast(mContext, "ideal key authenticate failed " + errorCode, Toast.LENGTH_SHORT);
                }
            }
        });
    }

    public String getAppName() {
        return "IdealDemo";
    }

    @Override
    public void onIdealARDestroyOk(int i) {

    }


    @Override
    public void onIdealLoadTimeOut(int status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopScanARTheme();
                mMainFragment.showDefaultMode();
                mMainFragment.addRecognizeTimeLine();
                ISARTipsUtil.makeToast(mContext, "download error", Toast.LENGTH_SHORT);
            }
        });
    }

    @Override
    public boolean onIdealARTouchAction(String jsonString) {
        if (mMainFragment.isVisible()) {
            mMainFragment.doARTouchEvent(jsonString);
            return false;
        }
        return false;
    }

    protected void onResume() {
        super.onResume();
        requireScreenOn();
    }

    @Override
    protected void pauseUnityPlayer() {
        super.pauseUnityPlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.LOGD(TAG + " onDestory");
        releaseScreenOn();
    }

    /**
     * 防止屏幕休眠 acquire wakelock
     */
    private synchronized void requireScreenOn() {
        if (null != mWakeLock && !mWakeLock.isHeld()) {
            Logger.LOGD(TAG + "requireWakeLock");
            mWakeLock.acquire();
        }
    }

    /**
     * 关闭防止屏幕休眠 release wakelock
     */
    private synchronized void releaseScreenOn() {
        if (null != mWakeLock && mWakeLock.isHeld()) {
            Logger.LOGD(TAG + " releaseWakeLock");
            mWakeLock.release();
        }
    }

    public void doStartScanARTheme() {
        Logger.LOGD(TAG + " doStartScanARTheme");
        startScanARTheme();
    }

    public void doStopARTheme() {
        Logger.LOGD(TAG + " doStopARTheme");
        stopScanARTheme();
    }

    public void doStartScanARFromRandom(final ISARRandomInfo randomInof) {
        Logger.LOGD(TAG + " doStartScanARFromRandom");
        doMainPage();
        if (mMainFragment != null) {
            mMainFragment.showARMode();
        }
        stopScanARTheme();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                startARThemeFromRandom(randomInof);
            }
        }, 1000);
    }

    public List<ISARRandomInfo> doGetstartGetRandomList(int startPos, int count, String language, boolean useCache) {
        return startGetRandomInfoList(startPos, count, language, useCache);
    }

    public void doRegiester() {
        registerSDK();
    }

    public void doCapture() {
        startCaptureAR();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                String path = getARCapturePath();
                ISARTipsUtil.makeToast(mContext, path, Toast.LENGTH_SHORT);
            }
        }, 1000);
    }

    public void showThemeImagePath() {
        final String path = getThemeImagePath();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                ISARTipsUtil.makeToast(mContext, path, Toast.LENGTH_SHORT);
            }
        }, 1000);
    }

    public void showUpdateImagePath() {
        final String path = getArCameraImagePath();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                ISARTipsUtil.makeToast(mContext, path, Toast.LENGTH_SHORT);
            }
        }, 1000);
    }

    public void changeScanEffect(boolean enable) {
        enableScanEffect(enable);
    }

    public void changeScanSound(boolean enable) {
        setPlayARMatchedSound(enable);
    }

    public void changeScanARMatch(boolean enable) {
        enableARMatch(enable);
    }

    public void doCameraFoucus() {
        cleanCameraDataAndFocus();
    }

    public void changeCameraFlash(boolean isOpen) {
        if (isOpen) {
            doOpenFlash();
        } else {
            doCloseFlash();
        }
    }

    public void changeCameraState(boolean isOpen) {
        if (isOpen) {
            startCamera();
        } else {
            stopCamera();
        }
    }

    public void changeCameraFace(boolean isBack) {
        switchCamera(isBack);
    }


    /**
     * set support dragging of model by finger.
     *
     * @param support true support, false not
     */
    public void dosetARObjectSupportMove(boolean support) {
        setARObjectSupportMove(support);
    }

    /**
     * set support scaling of theme by finger.
     *
     * @param support true support, false not
     */
    public void dosetARObjectSupportScale(boolean support) {
        setARObjectSupportScale(support);
    }

    /**
     * set support rotation of theme by finger.
     *
     * @param support true support, false not
     */
    public void dosetARObjectSupportRotate(boolean support) {
        setARObjectSupportRotate(support);
    }

    /**
     * reset position of theme to center.
     */
    public void doresetARObjectPose() {
        resetARObjectPose();
    }

    /**
     * set support gyroscope.
     *
     * @param support true support, false not
     */
    public void dosetARObjectSupportFreeMode(boolean support) {
        setARObjectSupportFreeMode(support);
    }

    @Override
    public void onIdealImageMatched(int result) {
        Logger.LOGD(TAG + " onIdealImageMatched " + result);
        mEndRecognizeTime = System.currentTimeMillis();
        mRecognizeTime = mEndRecognizeTime - mStartRecognizeTime;
        if (mMainFragment.isVisible()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMainFragment.addRecognizeTimeLine();
                }
            });
        }

        if (result == IMAGE_MATCHED_CANCELED) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doStopARTheme();
                    if (mMainFragment != null) {
                        mMainFragment.showDefaultMode();
                    }
                    Toast.makeText(getApplicationContext(), "scan failed.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (result == IMAGE_MATCHED_FAILED || result == IMAGE_MATCHED_TIMEOUT) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!mMainFragment.getScanningState()) {
                doStopARTheme();
                return;
            }

            doStartScanARTheme();
        }
    }

    @Override
    public void onIdealUnityPlayerPaused(int i) {

    }

    public void doDiscoverPage() {
        Logger.LOGD(TAG + "doDiscoverPage");
        doStopARTheme();
        FragmentTransaction fragTrans = getFragmentManager().beginTransaction();
        if (mListFragment.isAdded()) {
            fragTrans.show(mListFragment);
            fragTrans.hide(mMainFragment);
        } else {
            fragTrans.add(R.id.fl_home_tip_page, mListFragment);
        }
        fragTrans.commitAllowingStateLoss();
    }

    public void doMainPage() {
        FragmentTransaction fragTrans = getFragmentManager().beginTransaction();
        if (mMainFragment.isAdded()) {
            fragTrans.show(mMainFragment);
            fragTrans.hide(mListFragment);
        } else {
            fragTrans.add(R.id.fl_home_tip_page, mMainFragment);
        }
        fragTrans.commitAllowingStateLoss();
    }
}
