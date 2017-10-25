package com.yixun.sdk.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.yixun.ar.unity.ISARCamera;
import com.yixun.ar.unity.ISARIconHelper;
import com.yixun.ar.unity.ISARTouchEvent;
import com.yixun.ar.unity.ISARUnityMessageManager;
import com.yixun.sdk.R;
import com.yixun.ar.unity.ISARMusicPlayer;
import com.yixun.sdk.model.ISARAnimDownloadRespInfo;
import com.yixun.sdk.model.ISARAppInitInfo;
import com.yixun.sdk.model.ISARImageMatchedRespInfo;
import com.yixun.sdk.model.ISARImageSearchResult;
import com.yixun.sdk.model.ISARRandomInfo;
import com.yixun.sdk.server.ISARHttpClient;
import com.yixun.sdk.server.ISARHttpRequest;
import com.yixun.sdk.server.ISARHttpRequestQueue;
import com.yixun.sdk.server.ISARHttpServerURL;
import com.yixun.sdk.util.ISARBitmapUtil;
import com.yixun.sdk.util.ISARConstants;
import com.yixun.sdk.util.ISARDatFileUtil;
import com.yixun.sdk.util.ISARFilesUtil;
import com.yixun.sdk.util.ISARFloatButtonUtil;
import com.yixun.sdk.util.ISARManager;
import com.yixun.sdk.util.ISARNativeTrackUtil;
import com.yixun.sdk.util.ISARThreadPool;
import com.yixun.sdk.util.ISARUnityTool;
import com.yixun.sdk.util.Logger;
import com.unity3d.player.UnityPlayer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.yixun.sdk.util.ISARConstants.SDK_VERSION;

/**
 * Unity交互类，用于接收和处理Unity方法回调：如AR启动，AR状态，AR事件等。
 * Unity回调方法大都是是以ISAR***命名，用于区别非Unity方法。
 * 接收到Unity回调后，如果此方法需要进行UI适配，则通过onIdeal***方法通知实现类。
 */
public abstract class ISARSDKActivity extends ISARBaseActivity {
    private static final String TAG = ISARSDKActivity.class.getSimpleName();
    private static final String U3D_CAPTURE_NAME = "capture.jpg";

    // don't change the name of this variable; referenced from native code
    protected UnityPlayer mUnityPlayer;
    protected boolean mIsScanStarted = false; // 判断unity是否已经进入扫描状态，防止unity还未进入扫描状态，app已经停止unity扫描
    protected int[] mScreenPicSize = new int[]{100, 100}; // unity screen picture width, height
    protected long mAppStartedTime;
    protected List<String> mServerUrl = new ArrayList<>(3);
    protected String mThemePicMd5 = "0"; // ar theme themePicMd5
    protected String mBackgroundImgPath, mShareBackgroundImgPath;

    protected static final int AR_DESTROY_DEFAULT = 0;
    //    protected static final int AR_DESTROY_BACK_HOME = 1;
//    protected static final int AR_DESTROY_RESCAN = 2;

    protected static final int ISAR_CAMERA_INIT = -1;
    protected static final int ISAR_AR_DESTROYED = 0;
    protected static final int ISAR_AR_FROM_LIST = 1;
    protected static final int ISAR_AR_INIT = 2;
    protected static final int ISAR_AR_INIT_LOAD_DATA = 3;
    protected static final int ISAR_AR_INIT_LOAD_DATA_DONE = 4;
    protected static final int ISAR_AR_START_AR = 5;
    protected static final int IMAGE_MATCHED_FAILED = -1;
    protected static final int IMAGE_MATCHED_SUCCESS = 0;
    protected static final int IMAGE_MATCHED_CANCELED = 1;
    protected static final int IMAGE_MATCHED_TIMEOUT = 2;
    //    protected static final int IMAGE_MATCHED_OUT_AREA = 3;

    private static final int SCAN_MODE_FROM_CAMERA = 1;
    //    private static final int SCAN_MODE_FROM_ALBUM = 2;
//    private static final int SCAN_MODE_FROM_RANDOM = 3;

    private boolean mIsPlayMatchedSound;
    private boolean mIsOfflineRecognized = false; // 离线识别默认关闭
    private String mOffLineResourcePath;
    private boolean mIsLocalRecognized = false;//云识别本地化默认关闭
    private boolean mHasLocalRecognized = false; // 是否已经使用本地识别到主题
    private boolean mIsNeedRetry = true; // sdk自动重试
    private boolean mIsFirstTime = true;
    private boolean mIsUnityInited = false;
    // if recording state, need pause unity after video recorder stopped.
    private boolean mIsRecordingStopCalled = false;
    private boolean mIsRecordCompleted = true;
    protected boolean mIsNeedCombineRecordingFile = false; // 暂停的时候，如果需要合并视频，则不释放屏幕锁

    private int mKeyStatus = 0;
    private int mScanMode = SCAN_MODE_FROM_CAMERA;
    private int mSoundRecognizedId; // play when ar is recognized
    private long mPauseUnityDelayedTime = 0;
    private String mQiNiuUrl;
    private String mHuanShiUrl;
    private String mArImagePath;
    private String mFileCachePath;
    private String mLongitude;
    private String mLatitude;
    private SoundPool mSoundPool;
    private ISARMusicPlayer mISARMusicPlayer;
    private ISARMusicPlayer mDefaultEffectPlayer;
    private ISARFloatButtonUtil mFloatBtnUtil;
    private ISARTouchEvent mTouchEvent;

    protected long mStartRecognizeTime;
    protected long mEndRecognizeTime;
    protected long mRecognizeTime;

    private Handler mPauseUnityHandler = new Handler();
    private Runnable mPauseUnityRunnable = new Runnable() {
        @Override
        public void run() {
            pauseUnityPlayer();
        }
    };

    private ISARHttpClient.ISARListener mIdARListener = new ISARHttpClient.ISARListener() {
        @Override
        public void onIdARImageMatch(ISARImageMatchedRespInfo responseInfo) {
            ISARImageSearchResult imageSearchResult = responseInfo.getImageSearchResult();
            int httpRespCode = responseInfo.getHttpRespCode();
            int flowRespCode = responseInfo.getFlowRespCode();
            Logger.LOGD(TAG + " onIdARImageMatch flowRespCode=" + flowRespCode + ",httpRespCode=" + httpRespCode);
            if (IMAGE_MATCHED_FAILED == flowRespCode) {
                if (HttpURLConnection.HTTP_OK == httpRespCode || HttpURLConnection.HTTP_INTERNAL_ERROR == httpRespCode) {
                    onIdealImageMatched(IMAGE_MATCHED_FAILED);
                } else {
                    onIdealImageMatched(IMAGE_MATCHED_TIMEOUT);
                }
            }
            if (null != imageSearchResult && (IMAGE_MATCHED_SUCCESS == flowRespCode)) {
                onIdealImageMatched(IMAGE_MATCHED_SUCCESS);
                Logger.LOGD(TAG + " onIdARImageMatch flowCode=" + responseInfo.getFlowRespCode() + "," + imageSearchResult.datSrc);
                if (null != imageSearchResult.md5) {
                    Logger.LOGD(TAG + " doStartLoadTemplate play sound " + mIsPlayMatchedSound);
                    if (mIsPlayMatchedSound) {
                        mSoundPool.play(mSoundRecognizedId, 1, 1, 0, 0, 1);
                    }
                    mThemePicMd5 = imageSearchResult.md5;
                    mTouchEvent.setThemeMD5(mThemePicMd5, imageSearchResult.similarMd5);
                }
            } else
                Logger.LOGD(TAG + " onIdARImageMatch " + responseInfo.getHttpRespCode());
        }

        @Override
        public void onIdMatchStatusUpdate(int status) {
            Logger.LOGD(TAG + " onIdMatchStatusUpdate " + status);
//            if (status == ISARHttpClient.ISARListener.STOP_UPLOAD_TO_SERVER)
//                onIdealImageMatched(IMAGE_MATCHED_CANCELED);
        }

        @Override
        public void onIdDownloadAnimation(ISARAnimDownloadRespInfo downloadRespInfo) {
            int httpRespCode = downloadRespInfo.getHttpRespCode();
            int flowRespCode = downloadRespInfo.getFlowRespCode();
            Logger.LOGD(TAG + " onIdDownloadAnimation httpRespCode=" + httpRespCode + ",flowRespCode=" + flowRespCode);
            if (IMAGE_MATCHED_SUCCESS == flowRespCode) {
                mBackgroundImgPath = downloadRespInfo.getRandomInfo().themePicPath;
                mShareBackgroundImgPath = downloadRespInfo.getRandomInfo().sharePicPath;
                onIdealGetAnimation(true, downloadRespInfo.getRandomInfo(), null);
            } else if (IMAGE_MATCHED_CANCELED == flowRespCode) {
                /**
                 * TODO
                 * animation获取过程取消后，不通知app状态，后续可调整该流程。
                 */
            } else {
                onIdealGetAnimation(false, null, downloadRespInfo.getRespStr());
            }
        }
    };


    // Setup activity layout
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().takeSurface(null);
        // This makes xperia play happy
        getWindow().setFormat(PixelFormat.RGBX_8888);

        ISARUnityTool.getInstance().initUnityPlayer(this);
        mUnityPlayer = ISARUnityTool.getInstance().getUnityPlayer();
        setContentView(mUnityPlayer);
        mUnityPlayer.requestFocus();
        mTouchEvent = new ISARTouchEvent(ISARSDKActivity.this, mUnityPlayer);
        mFloatBtnUtil = new ISARFloatButtonUtil(this, new ISARFloatButtonUtil.OnFloatButtonTouchListener() {
            @Override
            public void doFloatBtnTouchEvent(final String jsonStr, final String btnId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!onIdealARTouchAction(jsonStr)) {
                            mTouchEvent.onFloatButtonTouch(jsonStr, btnId);
                        }
                    }
                });
            }
        });

        ISARHttpClient.getInstance().setARListener(mIdARListener);
//        ViewGroup vi = (ViewGroup) findViewById(android.R.id.content);
//        vi.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        checkAppKeyStatus();
        initSoundEffect();
        initRootDirectory();

        mServerUrl.add(ISARConstants.BASE_URL_CTCC);
        mServerUrl.add(ISARConstants.BASE_URL_CMCC);
        mServerUrl.add(ISARConstants.BASE_URL_CUCC);
        mAppStartedTime = System.currentTimeMillis();
        // open close unity log
        mHuanShiUrl = ISARHttpServerURL.getHttpUrl();
        mQiNiuUrl = ISARHttpServerURL.getHttpQiNiuUrl();
        Logger.LOGI(TAG + " onCreate2.");
    }

    // Resume Unity
    @Override
    protected void onResume() {
        super.onResume();
        resumeUnitPlayer();
        ISARFilesUtil.createRestoreFile();
    }

    // Pause Unity
    @Override
    protected void onPause() {
        super.onPause();
        Logger.LOGI(TAG + " onPause " + mPauseUnityDelayedTime);
        // workaround. mi4第一次安装进入幻视后无法录制。原因是unity还未初始化完成，启动splash导致unity pause.
        if (mPauseUnityDelayedTime > 0) {
            mPauseUnityHandler.postDelayed(mPauseUnityRunnable, mPauseUnityDelayedTime);
        } else {
            pauseUnityPlayer();
        }
    }

    // Quit Unity
    @Override
    protected void onDestroy() {
        ISARUnityTool.getInstance().destroyUnityPlayer();
        mIsUnityInited = false;
        destroySoundEffect();
        ISARCamera.getInstance().arcameraDeinitCamera();
        super.onDestroy();
    }

    // This ensures the layout will be correct.
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ISARUnityTool.getInstance().configurationChanged(newConfig);
    }

    // Notify Unity of the focus change.
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        ISARUnityTool.getInstance().windowFocusChanged(hasFocus);
    }

    private void callStopScan(int destroyType) {
        mThemePicMd5 = "0";
        ISARUnityMessageManager.stopARTheme(destroyType);
        ISARHttpClient.getInstance().doStopSingleImageSearch();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // need remove all views when stop ar.
                mFloatBtnUtil.clearFloatButtons();
            }
        });

        mIsScanStarted = false;
    }

    private void callStartOfflineScan() {
        mIsScanStarted = true;
        mStartRecognizeTime = System.currentTimeMillis();
        Log.d("Test", "isoffline = " + mIsOfflineRecognized + ";" + "resourcePath =" + mOffLineResourcePath);
        ISARUnityMessageManager.startSearch();
        mArImagePath = ISARCamera.getInstance().startCapturing();
        ISARThreadPool.INSTANCE.execute(new Runnable() {
            @Override
            public void run() {
                do {
                    //全离线识别为了提高效率，在设置模式时预定义了识别路径
                    String filePath = ISARCamera.getInstance().localRecognition2();
                    if (!TextUtils.isEmpty(filePath)) {
                        mThemePicMd5 = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.indexOf(".dat"));
                        loadLocalDatFile(filePath);
                        return;
                    }
                } while (checkOfflineRetry());
            }
        });
    }

    private boolean checkOfflineRetry() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mIsNeedRetry && mIsScanStarted && mIsUnityInited;
    }

    private void callStartOnlineScan() {
        mStartRecognizeTime = System.currentTimeMillis();
        ISARUnityMessageManager.startSearch();
        mArImagePath = ISARCamera.getInstance().startCapturing();
        ISARThreadPool.INSTANCE.execute(new Runnable() {
            @Override
            public void run() {
                String filePath = "";
                if (mIsLocalRecognized) {
                    filePath = localRecognized();
                }
                if (!TextUtils.isEmpty(filePath)) {
                    mHasLocalRecognized = true;
                    mThemePicMd5 = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.indexOf(".dat"));
                    startMatchToServer(ISARHttpServerURL.getSingleSearchUrl());
                } else {
                    mHasLocalRecognized = false;
                    startMatchToServer(ISARHttpServerURL.getSingleSearchUrl());
                }
            }
        });
    }

    private String localRecognized() {
        String result = "";
        File file = new File(ISARConstants.APP_TRAC_DIRETCORY);
        if (!file.exists()) {
            return result;
        }
        File[] subFile = file.listFiles();
        int subLength = subFile.length;
        if (subLength == 0) {
            return result;
        }
        List<String> filePathList = new ArrayList<>();
        for (File aSubFile : subFile) {
            if (aSubFile.getAbsolutePath().endsWith(".dat")) {
                filePathList.add(aSubFile.getAbsolutePath());
            }
        }
        int datSize = filePathList.size();
        String[] dataFilePath = new String[datSize];
        for (int i = 0; i < datSize; i++) {
            dataFilePath[i] = filePathList.get(i);
        }
        int status = ISARCamera.getInstance().localRecognition(dataFilePath);
        if (status >= 0) {
            result = dataFilePath[status];
        }
        return result;
    }

    private void initUnityPlayer() {
        View rootView = findViewById(android.R.id.content);
        final UnityPlayer unityView = findUnityView(rootView);

        // if unity view has been found, add some android view/widget on top
        if (unityView != null) {
            int version = android.os.Build.VERSION.SDK_INT;
            Logger.LOGD(TAG + " sdk version:" + version);
            String scale = "1";
            String data = scale + "&" + version;
            ISARUnityMessageManager.initISARData(data);

            // close wakelock of unity
            ISARUnityMessageManager.setScreenSleep(false);
            ISARUnityMessageManager.setCameraFocus();

            ISARUnityMessageManager.setAddExternalSearchDir(ISARConstants.APP_TRAC_DIRETCORY);
            // make mask
            if (mKeyStatus != 0) {
                ISARUnityMessageManager.setISARCameraQuality();
            }
            ISARUnityMessageManager.setSaveDataPath(mFileCachePath);
            ISARUnityMessageManager.setARObjectSupportFreeMode(true);
            ISARUnityMessageManager.setARObjectSupportMove(true);
            ISARUnityMessageManager.setARObjectSupportScale(true);
            ISARUnityMessageManager.setARObjectSupportRotate(false);
        }
    }

    private UnityPlayer findUnityView(View view) {
        if (view instanceof UnityPlayer) {
            return (UnityPlayer) view;
        }

        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); ++i) {
                UnityPlayer foundView = findUnityView(vg.getChildAt(i));
                if (foundView != null) {
                    return foundView;
                }
            }
        }
        return null;
    }

    /**
     * 上传图片到服务器匹配.
     *
     * @param serverUrl 服务器地址
     */
    private void startMatchToServer(final String serverUrl) {
        Logger.LOGI(TAG + " startMatchToServer gray ok=" + mArImagePath + ",serverUrl=" + serverUrl);
        synchronized (this) {
            File sf = new File(mArImagePath);
            if (mArImagePath.endsWith(".gif") || !sf.exists() || (sf.length() == 0)) {
                Logger.LOGW(TAG + " startLoadTemplate file is not correct");
                onIdealImageMatched(IMAGE_MATCHED_CANCELED);
                return;
            }
            Map<String, String> headerParam = getHttpHeaderPrams();

            Map<String, String> fileParams = new HashMap<>();
            fileParams.put("pic", mArImagePath);

            Logger.LOGD(TAG + " startMatchToServer url=" + serverUrl);
            ISARHttpRequest request = new ISARHttpRequest(serverUrl, ISARHttpRequestQueue.ISARCommand.START_AR_SEARCH);
            request.setHeaderParams(headerParam);
            if (!TextUtils.isEmpty(mLatitude) && !TextUtils.isEmpty(mLongitude)) {
                Map<String, String> bodyParam = new HashMap<>();
                bodyParam.put("lon", mLongitude);
                bodyParam.put("lat", mLatitude);
                request.setBodyParams(bodyParam);
            }
            request.setFileParams(fileParams);
            SharedPreferences sp = getSharedPreferences(ISARConstants.APP_ETAG_PREFERENCE, Context.MODE_PRIVATE);
            request.setSharedPreferences(sp);
            if (mIsLocalRecognized && mHasLocalRecognized) {
                request.setLocalRecognize(true);
                request.setThemePicMd5(mThemePicMd5);
            }
            ISARHttpClient.getInstance().doSingleImageSearch(request);
        }
    }

    private String generateLoadPath(String md5, int raww, int rawh) {
        String tmp = raww + "&" + rawh + "&" + md5;
        mThemePicMd5 = md5;
        return tmp;
    }

    private void loadLocalDatFile(String datPath) {
        if (mIsPlayMatchedSound) {
            mSoundPool.play(mSoundRecognizedId, 1, 1, 0, 0, 1);
        }
        ISARNativeTrackUtil.nAugmentedLoadFile(datPath);
        int[] screenSize = ISARNativeTrackUtil.getScreenSize();
        final String tmp = generateLoadPath(mThemePicMd5, screenSize[0], screenSize[1]);
        Logger.LOGD(TAG + " loadLocalDatFile tmp=" + tmp);
        ISARUnityMessageManager.startThemeFromSearch(tmp);
        Logger.LOGD(TAG + " loadLocalDatFile datPath," + datPath);
        File datFile = new File(datPath);
        // if datFile file is less than 5k, it is bad file, delete it.
        if (datFile.exists() && datFile.length() > 5000) {
            Logger.LOGD(TAG + " loadLocalDatFile datFile.getParent()=" + datFile.getParent());
            ISARUnityMessageManager.loadTemplate(datPath);
            //开启模板叠加
            ISARUnityMessageManager.startAR();
            onIdealImageMatched(IMAGE_MATCHED_SUCCESS);
        } else {
            ISARUnityMessageManager.stopARTheme(AR_DESTROY_DEFAULT);
            onIdealImageMatched(IMAGE_MATCHED_FAILED);
        }
    }

    // 添加view.
    private void addMyIseeView() {
        // else search
        View rootView = this.findViewById(android.R.id.content);
        final UnityPlayer unityView = findUnityView(rootView);

        // if Unity view has been found, add some android view/widget on top
        if (unityView != null) {
            // init unity player
            initUnityPlayer();
            // mIdealCallbackListener.onIdealAddCustomView(unityView);
            onIdealAddCustomView(unityView);
            // addViewToUntiy(R.id.fl_home_tip_page, unityView);
        }
    }

    private Map<String, String> getHttpHeaderPrams() {
        Map<String, String> headerParam = new HashMap<>();
        headerParam.put("imei", ISARConstants.APP_IMEI);
        headerParam.put("User-Agent", ISARConstants.USER_AGENT);
        headerParam.put("version", ISARConstants.APP_VERSION_NAME);
        headerParam.put("appKey", ISARConstants.APP_KEY);
        headerParam.put("appid", ISARConstants.APP_ID);
        Logger.LOGD(TAG + " getHttpHeaderPrams() headerParam : " + headerParam);
        return headerParam;
    }

    private void initSoundEffect() {
        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 5);
        mSoundRecognizedId = mSoundPool.load(this, R.raw.ar_success, 1);
        mISARMusicPlayer = new ISARMusicPlayer(null);
        mDefaultEffectPlayer = new ISARMusicPlayer(mContext, R.raw.sys_audio);
    }

    private void initRootDirectory() {
        mFileCachePath = mContext.getExternalFilesDir(null).getAbsolutePath();
        ISARConstants.APP_PARENT_PATH = mFileCachePath;
        ISARConstants.updateConstants();

        ISARFilesUtil.mkdirs(ISARConstants.APP_ROOT_DIRETCORY);
        ISARFilesUtil.mkdirs(ISARConstants.APP_CACHE_DIRECTORY);
        ISARFilesUtil.mkdirs(ISARConstants.APP_TRAC_DIRETCORY);
        ISARFilesUtil.mkdirs(ISARConstants.APP_LOG_DIRETCORY);
        ISARFilesUtil.mkdirs(ISARConstants.APP_CRASH_DIRETCORY);
        ISARFilesUtil.mkdirs(ISARConstants.APP_IMAGE_DIRECTORY);
        ISARFilesUtil.mkdirs(ISARConstants.APP_VIDEO_DIRECTORY);
        ISARFilesUtil.mkdirs(ISARConstants.ISARSDK_RECORD_AR_DIRECTORY);
        ISARFilesUtil.mkdirs(ISARConstants.APP_SCAN_ERROR_LOG_DIRETCORY);
    }

    private void destroySoundEffect() {
        if (null != mSoundPool) {
            mSoundPool.release();
        }
        if (null != mISARMusicPlayer) {
            mISARMusicPlayer.DeinitMusic();
        }
        if (null != mDefaultEffectPlayer) {
            mDefaultEffectPlayer.DeinitMusic();
        }
    }

    private void checkAppKeyStatus() {
        ISARThreadPool.INSTANCE.execute(new Runnable() {
            @Override
            public void run() {
                Logger.LOGD(TAG + " checkAppKeyStatus: mAppKey=" + mAppKey + ",mPackageName=" + mPackageName);
                ISARHttpRequest request = new ISARHttpRequest(ISARHttpServerURL.getCheckAppKeyUrl());
                request.setHeaderParams(getHttpHeaderPrams());
                Map<String, String> bodyParams = new HashMap<>(2);
                bodyParams.put("app_key", mAppKey);
                bodyParams.put("package", mPackageName);
                request.setBodyParams(bodyParams);
                try {
                    mKeyStatus = ISARHttpClient.getInstance().checkAppKeyStatus(request);
                    if (mKeyStatus == 0) {
                        ISARHttpRequest initRequest = new ISARHttpRequest(ISARHttpServerURL.getAppInitUrl());
                        Map<String, String> headParams = new HashMap<>(2);
                        headParams.put("imei", ISARConstants.APP_IMEI);
                        String userAgent = "ISARSDK_yixun" + " " + SDK_VERSION + " ("
                                + Build.MODEL + "; Android " + Build.VERSION.RELEASE + "; " + Locale.getDefault().toString() + ")";
                        headParams.put("User-Agent", userAgent);
                        initRequest.setHeaderParams(headParams);
                        ISARAppInitInfo initInfo = ISARHttpClient.getInstance().doAppInit(initRequest);
                        if (initInfo != null) {
                            if (!TextUtils.isEmpty(initInfo.mCDNPic)) {
                                ISARHttpServerURL.CDN_PIC = initInfo.mCDNPic;
                            }
                            if (!TextUtils.isEmpty(initInfo.mCDNPicFull)) {
                                ISARHttpServerURL.CDN_PIC_FULL = initInfo.mCDNPicFull;
                            }
                            if (!TextUtils.isEmpty(initInfo.mCDNResource)) {
                                ISARHttpServerURL.CDN_RESOURCE = initInfo.mCDNResource;
                            }
                            if (!TextUtils.isEmpty(initInfo.mCDNResCrop)) {
                                ISARHttpServerURL.CDN_RES_CROP = initInfo.mCDNResCrop;
                            }
                        }
                        String unityString = ISARHttpServerURL.CDN_PIC + "[&]" + ISARHttpServerURL.CDN_PIC_FULL
                                + "[&]" + ISARHttpServerURL.CDN_RESOURCE + "[&]" + ISARHttpServerURL.getHttpUrl();
                        ISARUnityMessageManager.setResourcesServerUrl(unityString);
                    }
                    onIdealKeyAuthFailed(mKeyStatus);
                } catch (IOException e) {
                    e.printStackTrace();
                    onIdealKeyAuthFailed(-1);
                }
                Logger.LOGI(TAG + " checkAppKeyStatus: " + mKeyStatus);
            }
        });
    }
    //--------------------------------Unity call back-------------------------------------

    /**
     * Unity call back, unity 摄像头宽高.
     */
    public void ISARScreenWH(int w, int h) {
        Logger.LOGI(TAG + " ISARScreenWH w:" + w + ",h:" + h);
        mScreenPicSize[0] = w;
        mScreenPicSize[1] = h;
    }

    /**
     * Unity call back, 叠加状态改变.
     *
     * @param status status 1叠加，0未叠加
     */
    public void ISARARStatus(int status) {
        Logger.LOGD(TAG + " ISARARStatus status=" + status);
    }

    /**
     * Unity call back, unity status changed.
     *
     * @param status status
     */
    public void ISARThemeStatus(int status) {
        Logger.LOGD(TAG + " ISARThemeStatus status=" + status);
        switch (status) {
            case ISAR_CAMERA_INIT:
                mIsUnityInited = true;
                // camera opened, unity will call back.
                addMyIseeView();
                break;
            case ISAR_AR_DESTROYED:
                // ar destroyed.
                break;
            case ISAR_AR_FROM_LIST:
                // click from adapter
                break;
            case ISAR_AR_INIT:
                // scan theme ok, start load animation
                // mIsNeedCheckLight = true;
                break;
            case ISAR_AR_INIT_LOAD_DATA:
                //离线识别模式时　关闭在线下载动画数据
                if (mIsOfflineRecognized) {
                    String resourcePath = mOffLineResourcePath + File.separator + mThemePicMd5;
                    ISARManager.loadLocalRecognitionResource(resourcePath);
                } else {
                    ISARHttpClient.getInstance().downloadAnimation();
                }
                // scan theme ok, start load animation
                break;
            case ISAR_AR_INIT_LOAD_DATA_DONE:
                // template load done, it will be shown.
                break;
            case ISAR_AR_START_AR:// theme load ok
                // save download template log
                break;
            default:
                break;
        }
        // mIdealCallbackListener.onIdealThemeStatus(status);
        onIdealThemeStatus(status);
    }

    /**
     * unity回调, data为需要检索的图片数据.
     *
     * @param filePath file path
     * @param width    width
     * @param height   height
     */
    public void ISARCameraImageBuffer(final String filePath, final int width, final int height) {
        Logger.LOGD(TAG + " ISARCameraImageBuffer filePath=" + filePath + ",width=" + width + ",height=" + height);
    }

    /**
     * unity回调，返回加载进度.
     *
     * @param progress progress
     */
    public void ISARShowLoadProgress(float progress) {
        Logger.LOGD(TAG + " ISARShowLoadProgress progress=" + progress);
        // mIdealCallbackListener.onIdealARDownloadProgress(progress);
    }

    /**
     * Unity call back. page cout of current page.
     *
     * @param pageCount total page of ar
     */
    public void ISARShowPageView(final int pageCount) {
        Logger.LOGD(TAG + " ISARShowPageView pageCount=" + pageCount);
        onIdealARPageCount(pageCount);
    }

    /**
     * Unity call back when page changed.
     *
     * @param pageIndex current page
     */
    public void ISARThemeUpdatePageTo(final int pageIndex) {
        onIdealARShowPageIndex(pageIndex);
    }

    /**
     * Unity call back when video full screen or half screen.
     *
     * @param status 0 resume to out of full screen, 1 paused to full screen
     */
    public synchronized void ISARUnityPaused(int status) {
        Logger.LOGD(TAG + " ISARUnityPaused status=" + status);
        if (0 == status && mIsFirstTime) {
            mIsFirstTime = false;
            ISARUnityMessageManager.setCameraFocus();
        }
        onIdealUnityPlayerPaused(status);
    }

    /**
     * Unity call back, make background image
     *
     * @return byte[] byte array
     */
    public byte[] IARPoiu7283hd39442hdh() {
        Logger.LOGD(TAG + " IARPoiu7283hd39442hdh");
        final String bit = "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";
        Bitmap bitmap = ISARBitmapUtil.convertStringToBitmap(bit);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 100, bos);
        byte[] arr = bos.toByteArray();
        bitmap.recycle();
        return arr;
    }

    /**
     * unity video play call back. for fullscreen.
     *
     * @param fileName file name
     */
    public void PlayFullScreenMovie(String fileName) {
        mTouchEvent.doTouchVideoEvent(fileName);
    }

    /**
     * Unity call back, touch event.
     *
     * @param jsonStr json string
     */
    public void ISARTouchAction(final String jsonStr) {
        Logger.LOGD(TAG + " ISARTouchAction");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!onIdealARTouchAction(jsonStr)) {
                    mTouchEvent.onWidgetTouch(jsonStr);
                }
            }
        });
    }

    /**
     * Unity call back, UI buttons
     *
     * @param buttonsData json string
     */
    public void ISARUIButtonsAppear(final String buttonsData) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mFloatBtnUtil.refreshFloatButtons(buttonsData, mUnityPlayer);
            }
        });
    }

    /**
     * Unity callback, play audio effect.
     *
     * @param type     0:system audio; 1:custom audio
     * @param filePath file path
     */
    public synchronized void PlaySoundEffect(int type, String filePath) {
        Logger.LOGD(TAG + " PlaySoundEffect:type=" + type + ",filePath=" + filePath);
        if (type == 0) {
            mDefaultEffectPlayer.PlayMusic();
        } else if ((type == 1) && (filePath != null) && (!filePath.startsWith("http"))) {
            mISARMusicPlayer.startPlayer(filePath);
        }
    }

    /**
     * Unity call back, ar matched offline.
     *
     * @param name themePicMd5 name
     */
    public void ISARImageTargetChanged(String name, float width, float height) {
    }

    /**
     * Unity callback draw custom icon.
     *
     * @param width        width
     * @param height       height
     * @param hasIcon      has icon
     * @param type         type
     * @param text         text
     * @param cornerRadius corner radius
     * @param color        color
     * @param textColor    text color
     * @param fontSize     font size
     * @return image byte array
     */
    public byte[] ISARCustomControlImage(int width, int height, boolean hasIcon, int type, String text,
                                         float cornerRadius, float[] color, float[] textColor, int fontSize) {
        return ISARIconHelper.ISARCustomControlImage(ISARSDKActivity.this, width, height, hasIcon, type, text,
                cornerRadius, color, textColor, fontSize);
    }

    /**
     * Unity callback draw music icon.
     *
     * @param width        width
     * @param height       height
     * @param cornerRadius corner radius
     * @param color        color
     * @return image byte array
     */
    public byte[] ISARCustomMusicImage(int width, int height, float cornerRadius, float[] color) {
        return ISARIconHelper.ISARCustomMusicImage(ISARSDKActivity.this, width, height, cornerRadius, color);
    }

    /**
     * unity call back to draw shape.
     *
     * @param width  width
     * @param height height
     * @param radius radius
     * @param colorr r
     * @param colorg g
     * @param colorb b
     * @return image byte array
     */
    public byte[] ISARDrawShapeControl(int width, int height, int radius, float colorr, float colorg, float colorb) {
        return ISARIconHelper.ISARDrawShapeControl(width, height, radius, colorr, colorg, colorb);
    }

    /**
     * unity call back to draw text.
     *
     * @param str      string
     * @param fontSize font size
     * @param width    width
     * @param height   height
     * @param colorr   r
     * @param colorg   g
     * @param colorb   b
     * @return image byte array
     */
    public byte[] ISARDrawTextControl(String str, int fontSize, int width, int height, float colorr, float colorg,
                                      float colorb) {
        return ISARIconHelper.ISARDrawTextControl(str, fontSize, width, height, colorr, colorg, colorb);
    }

    /**
     * draw image text with gradient color.
     *
     * @param width      width of widget
     * @param height     height of widget
     * @param colorStart start color
     * @param colorEnd   end color
     * @param text       text
     * @return byte of image
     */
    public byte[] ISARDrawNoWidgets(int width, int height, float[] colorStart, float[] colorEnd, String text) {
        return ISARIconHelper.ISARDrawNoWidgets(width, height, colorStart, colorEnd, text);
    }

    /**
     * Unity call back.
     * 主题销毁后会回调此方法.
     *
     * @param destroyType 在调用destroy时候传入的值.
     */
    public void ISARDestroyThemeOk(int destroyType) {
        Logger.LOGI(TAG + " ISARDestroyThemeOk");
        onIdealARDestroyOk(destroyType);
    }

    /**
     * unity call back.
     *
     * @param status 0: timeout; 1: download error
     */
    public void ISARLoadTimeOut(int status) {
        Logger.LOGD(TAG + " ISARLoadTimeOut ");
        onIdealLoadTimeOut(status);
    }

    public boolean ISARIsVideoAllowFullScreen() {
        Logger.LOGD(TAG + " ISARIsVideoAllowFullScreen");
        /**
         * Unity与Android交互复杂，并且与Android其他元件处理录制中断流程不一致，
         * 故直接跳过Unity检查，App在调用全屏播放的时候做录制中断处理。
         */
        return true;
    }

    /**
     * unity callback.
     * record started.
     */
    public void ISARVideoRecordStarted() {
        Logger.LOGD(TAG + " ISARVideoRecordStarted ");
        if (mIsRecordingStopCalled) {
            Logger.LOGW(TAG + " ISARVideoRecordStarted-------------stop called before started");
            mIsNeedCombineRecordingFile = false;
            stopRecordVideo();
            return;
        }
        mIsNeedCombineRecordingFile = true;
        //onIdealVideoRecordStarted();
    }

    /**
     * unity callback.
     * record stoped.
     */
    public synchronized void ISARVideoRecordComplete() {
        Logger.LOGD(TAG + " ISARVideoRecordComplete");
        mIsRecordCompleted = true;

        final File pFile = new File(ISARConstants.ISARSDK_RECORD_AR_DIRECTORY);
        final String vPath = ISARFilesUtil.getRecordedFilePath(pFile);
        Logger.LOGD(TAG + " ISARVideoRecordComplete vPath=" + vPath);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String nFilePath = vPath;
        File file = new File(vPath);
        if (file.exists()) {
            String fileName = vPath.substring(vPath.lastIndexOf("/") + 1, vPath.indexOf(".mp4"));
            Logger.LOGD(TAG + " ISARVideoRecordComplete fileName=" + fileName);
            // need app dispatch video to ar or weather folder.
            String newFilePath = ISARConstants.ISARSDK_RECORD_AR_DIRECTORY + File.separator + fileName + "md5-" + mThemePicMd5 + ".mp4";
            File fileNew = new File(newFilePath);
            file.renameTo(fileNew);
            nFilePath = newFilePath;
        }
        Logger.LOGD(TAG + " ISARVideoRecordComplete vPath=" + vPath + ", newFile:" + nFilePath);
        mIsNeedCombineRecordingFile = false;
        //onIdealVideoRecordComplete(nFilePath);
    }


    //------------------------------SDK API----------------------------------------

    /**
     * 打开或关闭离线识别
     *
     * @param enable true打开， false关闭
     */
    protected void enableOffLineRecognition(boolean enable) {
        mIsOfflineRecognized = enable;
    }

    /**
     * 设置离线识别资源路径
     *
     * @param resourcePath
     */
    protected void setOfflinePath(String resourcePath) {
        if (!mIsOfflineRecognized) {
            return;
        }

        if (TextUtils.isEmpty(resourcePath)) {
            return;
        }
        mOffLineResourcePath = resourcePath;
        ISARDatFileUtil.getInstance().setDatFilePath(resourcePath);
    }

    /**
     * 打开或关闭云识别本地化。
     *
     * @param enable true打开， false关闭
     */
    protected void enableLocalRecognition(boolean enable) {
        mIsLocalRecognized = enable;
    }

    /**
     * 获取AR缓存数据大小，单位MB
     *
     * @return M
     */
    protected float sizeOfISARCache() {
        float size = ISARFilesUtil.getISARCacheSize();
        return size;
    }

    /**
     * 清楚AR缓存数据.
     *
     * @return true success
     */
    protected boolean clearDataCache() {
        boolean result = ISARFilesUtil.clearISARCache();
        return result;
    }

    /**
     * 设置缓存数据的父目录路径.
     * e.g. "/sdcard/11"
     *
     * @return true success
     */
    protected boolean setCacheDataPath(String cachePath) {
        boolean needRename = mFileCachePath.equals(cachePath) ? false : true;
        Logger.LOGD(TAG + " setCacheDataPath:" + cachePath);
        if (needRename) {
            mFileCachePath = cachePath;
            ISARConstants.APP_PARENT_PATH = mFileCachePath;
            ISARConstants.updateConstants();

            ISARFilesUtil.mkdirs(ISARConstants.APP_ROOT_DIRETCORY);
            ISARFilesUtil.mkdirs(ISARConstants.APP_CACHE_DIRECTORY);
            ISARFilesUtil.mkdirs(ISARConstants.APP_TRAC_DIRETCORY);
            ISARFilesUtil.mkdirs(ISARConstants.APP_LOG_DIRETCORY);
            ISARFilesUtil.mkdirs(ISARConstants.APP_CRASH_DIRETCORY);
            ISARFilesUtil.mkdirs(ISARConstants.APP_IMAGE_DIRECTORY);
            ISARFilesUtil.mkdirs(ISARConstants.APP_VIDEO_DIRECTORY);
            ISARFilesUtil.mkdirs(ISARConstants.ISARSDK_RECORD_AR_DIRECTORY);
            ISARFilesUtil.mkdirs(ISARConstants.APP_SCAN_ERROR_LOG_DIRETCORY);
        }
        return true;
    }

    /**
     * 注册ISAR SDK
     */
    protected void registerSDK() {
        checkAppKeyStatus();
    }

    /**
     * 开始扫描AR主题.
     */
    protected void startScanARTheme() {
        Logger.LOGD(TAG + " startScanARTheme " + mKeyStatus);
        if (mKeyStatus != 0) {
            return;
        }
        mScanMode = SCAN_MODE_FROM_CAMERA;
        if (mIsOfflineRecognized) {
            callStartOfflineScan();
        } else {
            callStartOnlineScan();
        }
    }

    /**
     * 停止扫描AR主题.
     */
    protected void stopScanARTheme() {
        Logger.LOGD(TAG + " stopScanARTheme");
        callStopScan(0);
    }

    /**
     * 开启、关闭扫描效果
     */
    protected void enableScanEffect(boolean isOpen) {
        ISARUnityMessageManager.enableScanEffect(isOpen);
    }

    /**
     * set play sound when ar matched.
     *
     * @param play true play, false not
     */
    protected void setPlayARMatchedSound(boolean play) {
        mIsPlayMatchedSound = play;
    }

    /**
     * 开启、关闭AR叠加
     */
    protected void enableARMatch(boolean isMatch) {
        if (isMatch) {
            ISARUnityMessageManager.startAR();
        } else {
            ISARUnityMessageManager.stopAR();
        }
    }

    /**
     * 重置摄像头数据.
     */
    protected void cleanCameraDataAndFocus() {
        ISARUnityMessageManager.setCameraFocus();
    }

    /**
     * 开打闪光灯.
     */
    protected void doOpenFlash() {
        synchronized (this) {
            ISARUnityMessageManager.openFlashTorch();
        }
    }

    /**
     * 关闭闪光灯.
     */
    protected void doCloseFlash() {
        synchronized (this) {
            ISARUnityMessageManager.closeFlashTorch();
        }
    }

    /**
     * 打开摄像头
     */
    protected void startCamera() {
        ISARCamera.getInstance().arcameraStart();
    }

    /**
     * 关闭摄像头
     */
    protected void stopCamera() {
        ISARCamera.getInstance().arcameraStop();
    }

    /**
     * 切换相机摄像头
     */
    protected void switchCamera(boolean isBack) {
        ISARUnityMessageManager.switchCamera(isBack);
    }

    /**
     * Unity AR截屏
     */
    protected void startCaptureAR() {
        ISARUnityMessageManager.makeScreenCapture();
    }

    /**
     * 获取AR截屏图片路径
     */
    protected String getARCapturePath() {
        String path = ISARConstants.APP_PARENT_PATH + File.separator + U3D_CAPTURE_NAME;
        File file = new File(path);
        if (file.exists()) {
            return path;
        } else {
            return "";
        }
    }

    /**
     * 获取主题图片缓存路径
     */
    protected String getThemeImagePath() {
        return mBackgroundImgPath;
    }

    /**
     * 获取上传服务器识别的图片路径.
     */
    protected String getArCameraImagePath() {
        return mArImagePath;
    }


    public boolean getIsOfflineRecognized() {
        return mIsOfflineRecognized;
    }

    public boolean getHasLocalRecognized() {
        return mHasLocalRecognized;
    }

    public long getRecognizeTime() {
        return mRecognizeTime;
    }

    /**
     * set support dragging of model by finger.
     *
     * @param support true support, false not
     */
    protected void setARObjectSupportMove(boolean support) {
        ISARUnityMessageManager.setARObjectSupportMove(support);
    }

    /**
     * set support scaling of theme by finger.
     *
     * @param support true support, false not
     */
    protected void setARObjectSupportScale(boolean support) {
        ISARUnityMessageManager.setARObjectSupportScale(support);
    }

    /**
     * set support rotation of theme by finger.
     *
     * @param support true support, false not
     */
    protected void setARObjectSupportRotate(boolean support) {
        ISARUnityMessageManager.setARObjectSupportRotate(support);
    }

    /**
     * reset position of theme to center.
     */
    protected void resetARObjectPose() {
        ISARUnityMessageManager.resetARObjectPosition();
    }

    /**
     * set support gyroscope.
     *
     * @param support true support, false not
     */
    protected void setARObjectSupportFreeMode(boolean support) {
        Logger.LOGD(" ARThemeIsSupportFreeMode ...");
        ISARUnityMessageManager.setARObjectSupportFreeMode(support);
    }


    /**
     * 设置使用SDK的经纬度
     *
     * @param longitude
     * @param latitude
     */
    protected void setSearchArea(String longitude, String latitude) {
        mLongitude = longitude;
        mLatitude = latitude;
    }

    /**
     * start get random list from server.
     *
     * @param startPos start position
     * @param count    list count
     * @param language language of list item
     * @return list of random
     */
    protected List<ISARRandomInfo> startGetRandomInfoList(int startPos, int count, String language, boolean useCache) {
        if (mKeyStatus != 0) {
            return null;
        }
        List<ISARRandomInfo> list = null;
        ISARHttpRequest request = new ISARHttpRequest(ISARHttpServerURL.getRandomInfoListUrl());
        request.setHeaderParams(getHttpHeaderPrams());
        Map<String, String> urlParams = new HashMap<>(4);
        urlParams.put("imei", ISARConstants.APP_IMEI);
        urlParams.put("pos", String.valueOf(startPos));
        urlParams.put("count", String.valueOf(count));
        urlParams.put("language", language);
        request.setUrlParams(urlParams);

        request.setUseCache(useCache);
        try {
            list = ISARHttpClient.getInstance().getRandomInfoList(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 延迟unity pause.
     *
     * @param millisecond
     */
    protected void setPauseUnityDelayed(long millisecond) {
        mPauseUnityDelayedTime = millisecond;
    }

    /**
     * 从已有的ISARRandomInfo加载AR效果.
     *
     * @param info info
     */
    protected void startARThemeFromRandom(final ISARRandomInfo info) {
        if (mKeyStatus != 0) {
            return;
        }
        Map<String, String> headerParam = getHttpHeaderPrams();

        ISARHttpRequest request = new ISARHttpRequest(ISARHttpServerURL.getSingleSearchUrl(), ISARHttpRequestQueue.ISARCommand.START_AR_SEARCH);
        request.setHeaderParams(headerParam);
        SharedPreferences sp = getSharedPreferences(ISARConstants.APP_ETAG_PREFERENCE, Context.MODE_PRIVATE);
        request.setSharedPreferences(sp);
        ISARHttpClient.getInstance().doDiscoverSingleImageSearch(request, info.getThemePicMd5());

        ISARUnityMessageManager.setISARThemeStatus(1);

        String rotateVector = "-" + info.mXRotate + "," + "-" + info.mYRotate + "," + "-" + info.mZRotate;// 旋转角度的x,y,z值
        Logger.LOGD(TAG + " doScanModeForActivityResult rotateVector=" + rotateVector + "  info.mShowResoursePic: "
                + info.mShowResoursePic);
        ISARUnityMessageManager.setCameraRotate(rotateVector);

        ISARUnityMessageManager.setThemeImageHide(String.valueOf(1 - info.mShowResoursePic));

        final String tmp = generateLoadPath(info.getThemePicMd5(), info.getWidth(), info.getHeight());
        Logger.LOGD(TAG + " doScanModeForActivityResult tmp=" + tmp);
        ISARUnityMessageManager.startThemeFromOther(tmp);
    }

    /**
     * 将文件路径的图片传到服务器匹配，并显示AR效果.
     *
     * @param filePath file path
     */
    /*protected void startARThemeFromFilePath(final String filePath) {
        mScanMode = SCAN_MODE_FROM_ALBUM;
        mISARThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                final String dst = ISARConstants.APP_CACHE_DIRECTORY + File.separator + "tmp_upload.jpg";
                File file = new File(filePath);
                ISARNativePicUtil.rgb2gray(mContext, file.getAbsolutePath(), dst);
                String url = mHttpApi.getSingleSearchUrl();
                ISARSearchImageInfo searchInfo = new ISARSearchImageInfo();
                searchInfo.iamgePath = dst;
                int result = doStartLoadTemplate(searchInfo, url);
                onIdealImageMatched(result);
                switch (result) {
                    case IMAGE_MATCHED_CANCELED:
                        break;
                    case IMAGE_MATCHED_FAILED:
                        Logger.LOGD(TAG + " onScanFromAlbum search failed ");
                        break;
                    case IMAGE_MATCHED_SUCCESS:
                        break;
                    default:
                        break;
                }
            }
        });
    }*/

    /**
     * start get random list from server by editor id.
     *
     * @param startPos start position
     * @param count    list count
     * @param editorId editor id
     * @param language language of list item
     * @return list of editor info. If not null, first object is PersonalInfo, second is RandomInfo list.
     */
    protected List<Object> startGetEditorInfoList(int startPos, int count, String editorId, String language, boolean useCache) {
        if (mKeyStatus != 0) {
            return null;
        }
        List<Object> list = null;
        ISARHttpRequest request = new ISARHttpRequest(ISARHttpServerURL.getPersonalInfoListUrl());
        request.setHeaderParams(getHttpHeaderPrams());
        Map<String, String> bodyParams = new HashMap<>(5);
        bodyParams.put("imei", ISARConstants.APP_IMEI);
        bodyParams.put("pos", String.valueOf(startPos));
        bodyParams.put("count", String.valueOf(count));
        bodyParams.put("editor_id", editorId);
        bodyParams.put("language", language);
        request.setBodyParams(bodyParams);
        request.setUseCache(useCache);
        try {
            list = ISARHttpClient.getInstance().getPersonalInfoList(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 开始录像
     */
    protected synchronized void startRecordVideo() {
        Logger.LOGD(TAG + " start record-------- start ");
        ISARUnityMessageManager.startScreenRecording();
        mIsRecordingStopCalled = false;
        mIsRecordCompleted = false;
    }

    /**
     * 停止录像
     */
    protected synchronized void stopRecordVideo() {
        Logger.LOGD(TAG + " stop record");
        if (mIsRecordCompleted) {
            Logger.LOGW(TAG + " doStopRecordVideo stopped already.");
        }
        ISARUnityMessageManager.stopScreenRecording();
        mIsRecordingStopCalled = true;
    }

    /**
     * 暂停 unity player.
     */
    protected void pauseUnityPlayer() {
        if (null != mUnityPlayer) {
            Logger.LOGW(TAG + " pauseUnityPlayer ----------");
            ISARUnityTool.getInstance().pauseUnityPlayer();
        }
    }

    /**
     * 恢复 unity player.
     */
    protected void resumeUnitPlayer() {
        if (null != mUnityPlayer) {
            Logger.LOGW(TAG + " resumeUnitPlayer ----------");
            ISARUnityTool.getInstance().resumeUnityPlayer();
        }
    }

    /**
     * 打开或关闭log
     * 默认关闭
     *
     * @param enable true,打开，false 关闭
     */
    protected void enableLogTest(boolean enable) {
        Logger.enableLogTest(enable);
        // open close unity log
        /*if (enable) {
            UnityPlayer.UnitySendMessage(UNITY_MESSAGE_TAG, "EnableDebuger", "true");
        } else {
            UnityPlayer.UnitySendMessage(UNITY_MESSAGE_TAG, "EnableDebuger", "false");
        }*/
    }

    //--------------------------SDK call back---------------------------------------------------

    /**
     * if idealsee key authenticate failed, this will call back to client.
     */
    public abstract void onIdealKeyAuthFailed(int errorCode);

    /**
     * add customer view to ui, make it beautiful.
     *
     * @param unityView the unity view, add customer view to parent of unityView
     */
    public abstract void onIdealAddCustomView(final UnityPlayer unityView);

    /**
     * AR theme status changed.
     *
     * @param status status
     */
    public abstract void onIdealThemeStatus(int status);

    /**
     * ar image matched callback.
     *
     * @param result result according to canceled, failed, success.
     */
    public abstract void onIdealImageMatched(int result);

    /**
     * unity player resumed or paused.
     *
     * @param status 0 resumed, 1 paused
     */
    public abstract void onIdealUnityPlayerPaused(int status);

    /**
     * total page count.
     *
     * @param pageCount total count
     */
    public abstract void onIdealARPageCount(int pageCount);

    /**
     * current page index.
     *
     * @param pageIndex current index
     */
    public abstract void onIdealARShowPageIndex(int pageIndex);

    /**
     * get animation call back.
     *
     * @param isSuccess      true is success, false is not
     * @param ISARRandomInfo the model of ar theme, if result is false, randomInfo will be null
     * @param animation      it is for app. If result is false, animation will not be null
     */
    public abstract void onIdealGetAnimation(boolean isSuccess, ISARRandomInfo ISARRandomInfo, String animation);

    /**
     * 主题销毁成功后回调.
     *
     * @param destroyType 客户端在调用stopScanARTheme(int type)时传入的type.
     */
    public abstract void onIdealARDestroyOk(int destroyType);

    /**
     * Unity下载数据超时或下载失败.
     *
     * @param status 0: timeout; 1: download error
     */
    public abstract void onIdealLoadTimeOut(int status);

    /**
     * AR touch action
     *
     * @param jsonString
     */
    public abstract boolean onIdealARTouchAction(String jsonString);
}
