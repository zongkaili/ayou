package com.yixun.sdk.demo.list;

import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.idealsee.ar.unity.ISARUnityMessageManager;
import com.yixun.sdk.demo.Constants;
import com.yixun.sdk.demo.R;
import com.yixun.sdk.demo.SDKDemoActivity;
import com.yixun.sdk.demo.list.adapter.RecognizeTimeListAdapter;
import com.yixun.sdk.demo.utils.BtnUtils;
import com.yixun.sdk.demo.utils.PreferenceUtil;
import com.yixun.sdk.demo.widget.CharSlideMenu;
import com.yixun.sdk.demo.widget.webview.WebViewActivity;
import com.idealsee.sdk.util.ISARTipsUtil;
import com.idealsee.sdk.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hn This is tips layout for showing how to use it.
 */
public class MainFragment extends Fragment implements OnClickListener {
    private static final String TAG = "[MainFragment]";
    private static final int CHANGE_PAGE_PRE = 1;
    private static final int CHANGE_PAGE_NEXT = 2;
    private static final int MSG_ON_AR_TOUCH_EVENT = 0x001;
    //下面的type值不可随意更改
    protected static final int TOUCH_ACTION_NEARBY = 21;
    protected static final int TOUCH_ACTION_SHARE_WEIXIN = 24;
    protected static final int TOUCH_ACTION_SHARE_PYQ = 25;
    protected static final int TOUCH_ACTION_SHARE_WB = 26;

    private RecyclerView mRecognizeTimeRV;
    private RecognizeTimeListAdapter mRecTimeRVAdapter;
    private List<String> mRecTimeList;

    private TextView mStartScan;
    private TextView mDiscoverTv;
    private TextView mPrePageTv, mNextPageTv;
    private RelativeLayout mToastRl, mScanRl, mCameraRl, mThemeRl;
    private RelativeLayout mSelectedRl;
    private TextView mTestMove, mTestScale, mTestRotate, mTestCenter, mTestGyro;
    private CharSlideMenu mContentPageSlideCsm;
    private View mView;
    private RelativeLayout mFloatButtonsLayout = null;
    private boolean mIsScanning = false;

    private boolean mScanEffect = true;
    private boolean mScanSound = true;
    private boolean mScanMatch = true;

    private boolean mCameraFlash = false;
    private boolean mCameraState = true;
    private boolean mCameraBackFace = true;

    private boolean mIsSupportMove = true;
    private boolean mIsSupportScale = true;
    private boolean mIsSupportRotate = false;
    private boolean mIsSupportGyroscope = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ON_AR_TOUCH_EVENT:
                    String jsonString = (String) msg.obj;
                    doTouchEvent(jsonString, false, "");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Configuration config = getResources().getConfiguration();
        mView = inflater.inflate(R.layout.frag_main, null);
        initWidget(mView);
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void initWidget(View rootView) {
        mStartScan = (TextView) rootView.findViewById(R.id.start_scan);
        mDiscoverTv = (TextView) rootView.findViewById(R.id.tv_discover);
        mPrePageTv = (TextView) rootView.findViewById(R.id.tv_pre);
        mNextPageTv = (TextView) rootView.findViewById(R.id.tv_next);
        mRecognizeTimeRV = (RecyclerView) rootView.findViewById(R.id.list_recognize_time);
        mContentPageSlideCsm = (CharSlideMenu) rootView.findViewById(R.id.csm_ar_page_number);

        mStartScan.setOnClickListener(this);
        mDiscoverTv.setOnClickListener(this);
        mPrePageTv.setOnClickListener(this);
        mNextPageTv.setOnClickListener(this);

        initToastOperator(rootView);
        initScanOperator(rootView);
        initCameraOperator(rootView);
        initObjectOperator(rootView);
        initMenu(rootView);
        initRecognizeTimeList();
    }

    private void initToastOperator(View rootView) {
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.test_register:
                        ((SDKDemoActivity) getActivity()).doRegiester();
                        break;
                    case R.id.test_capture:
                        ((SDKDemoActivity) getActivity()).doCapture();
                        break;
                    case R.id.test_theme_image:
                        ((SDKDemoActivity) getActivity()).showThemeImagePath();
                        break;
                    case R.id.test_update_image:
                        ((SDKDemoActivity) getActivity()).showUpdateImagePath();
                        break;
                    default:
                        break;
                }
            }
        };

        rootView.findViewById(R.id.test_register).setOnClickListener(listener);
        rootView.findViewById(R.id.test_capture).setOnClickListener(listener);
        rootView.findViewById(R.id.test_theme_image).setOnClickListener(listener);
        rootView.findViewById(R.id.test_update_image).setOnClickListener(listener);
    }

    private void initScanOperator(View rootView) {
        final TextView scanEffect = (TextView) rootView.findViewById(R.id.test_effect);
        final TextView scanSound = (TextView) rootView.findViewById(R.id.test_sound);
        final TextView scanMatch = (TextView) rootView.findViewById(R.id.test_ar_match);

        scanEffect.setText(getString(R.string.tv_support_scan_effect, mScanEffect ? "开" : "关"));
        scanSound.setText(getString(R.string.tv_support_scan_sound, mScanSound ? "开" : "关"));
        scanMatch.setText(getString(R.string.tv_support_scan_ar_match, mScanMatch ? "开" : "关"));

        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.test_effect:
                        mScanEffect = !mScanEffect;
                        scanEffect.setText(getString(R.string.tv_support_scan_effect, mScanEffect ? "开" : "关"));
                        ((SDKDemoActivity) getActivity()).changeScanEffect(mScanEffect);
                        break;
                    case R.id.test_sound:
                        mScanSound = !mScanSound;
                        scanSound.setText(getString(R.string.tv_support_scan_sound, mScanSound ? "开" : "关"));
                        ((SDKDemoActivity) getActivity()).changeScanSound(mScanSound);
                        break;
                    case R.id.test_ar_match:
                        mScanMatch = !mScanMatch;
                        scanMatch.setText(getString(R.string.tv_support_scan_ar_match, mScanMatch ? "开" : "关"));
                        ((SDKDemoActivity) getActivity()).changeScanARMatch(mScanMatch);
                        break;
                    default:
                        break;
                }
            }
        };

        scanEffect.setOnClickListener(listener);
        scanSound.setOnClickListener(listener);
        scanMatch.setOnClickListener(listener);
    }

    private void initCameraOperator(View rootView) {
        final TextView flashTv = (TextView) rootView.findViewById(R.id.test_flash);
        flashTv.setText(getString(R.string.tv_support_camera_flash, mCameraFlash ? "开" : "关"));
        final TextView cameraTv = (TextView) rootView.findViewById(R.id.test_camera);
        cameraTv.setText(getString(R.string.tv_support_camera_camera, mCameraState ? "开" : "关"));

        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.test_focus:
                        ((SDKDemoActivity) getActivity()).doCameraFoucus();
                        break;
                    case R.id.test_flash:
                        mCameraFlash = !mCameraFlash;
                        flashTv.setText(getString(R.string.tv_support_camera_flash, mCameraFlash ? "开" : "关"));
                        ((SDKDemoActivity) getActivity()).changeCameraFlash(mCameraFlash);
                        break;
                    case R.id.test_camera:
                        mCameraState = !mCameraState;
                        cameraTv.setText(getString(R.string.tv_support_camera_camera, mCameraState ? "开" : "关"));
                        ((SDKDemoActivity) getActivity()).changeCameraState(mCameraState);
                        break;
                    case R.id.test_switch:
                        ((SDKDemoActivity) getActivity()).doStopARTheme();
                        showDefaultMode();
                        mCameraBackFace = !mCameraBackFace;
                        ((SDKDemoActivity) getActivity()).changeCameraFace(mCameraBackFace);
                        break;
                    default:
                        break;
                }
            }
        };

        rootView.findViewById(R.id.test_focus).setOnClickListener(listener);
        flashTv.setOnClickListener(listener);
        cameraTv.setOnClickListener(listener);
        rootView.findViewById(R.id.test_switch).setOnClickListener(listener);
    }

    private void initObjectOperator(View rootView) {
        mTestMove = (TextView) rootView.findViewById(R.id.test_move);
        mTestScale = (TextView) rootView.findViewById(R.id.test_scale);
        mTestRotate = (TextView) rootView.findViewById(R.id.test_rotate);
        mTestCenter = (TextView) rootView.findViewById(R.id.test_center);
        mTestGyro = (TextView) rootView.findViewById(R.id.test_gyroscope);

        mIsSupportMove = PreferenceUtil.getBoolean(Constants.SP_KEY_IS_SUPPORT_MOVE, true);
        mIsSupportScale = PreferenceUtil.getBoolean(Constants.SP_KEY_IS_SUPPORT_SCALE, true);
        mIsSupportRotate = PreferenceUtil.getBoolean(Constants.SP_KEY_IS_SUPPORT_ROTATE, false);
        mIsSupportGyroscope = PreferenceUtil.getBoolean(Constants.SP_KEY_IS_SUPPORT_FREEMODE, false);

        mTestMove.setText(getString(R.string.tv_support_theme_move, mIsSupportMove ? "开" : "关"));
        mTestScale.setText(getString(R.string.tv_support_theme_scale, mIsSupportScale ? "开" : "关"));
        mTestRotate.setText(getString(R.string.tv_support_theme_rotate, mIsSupportRotate ? "开" : "关"));
        mTestGyro.setText(getString(R.string.tv_support_theme_gyroscope, mIsSupportGyroscope ? "开" : "关"));

        ((SDKDemoActivity) getActivity()).dosetARObjectSupportMove(mIsSupportMove);
        ((SDKDemoActivity) getActivity()).dosetARObjectSupportScale(mIsSupportScale);
        ((SDKDemoActivity) getActivity()).dosetARObjectSupportRotate(mIsSupportRotate);
        ((SDKDemoActivity) getActivity()).dosetARObjectSupportFreeMode(mIsSupportGyroscope);

        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.test_move:
                        mIsSupportMove = !mIsSupportMove;
                        ((SDKDemoActivity) getActivity()).dosetARObjectSupportMove(mIsSupportMove);
                        PreferenceUtil.putBoolean(Constants.SP_KEY_IS_SUPPORT_MOVE, mIsSupportMove);
                        mTestMove.setText(getString(R.string.tv_support_theme_move, mIsSupportMove ? "开" : "关"));
                        break;
                    case R.id.test_scale:
                        mIsSupportScale = !mIsSupportScale;
                        ((SDKDemoActivity) getActivity()).dosetARObjectSupportScale(mIsSupportScale);
                        PreferenceUtil.putBoolean(Constants.SP_KEY_IS_SUPPORT_SCALE, mIsSupportScale);
                        mTestScale.setText(getString(R.string.tv_support_theme_scale, mIsSupportScale ? "开" : "关"));
                        break;
                    case R.id.test_rotate:
                        mIsSupportRotate = !mIsSupportRotate;
                        ((SDKDemoActivity) getActivity()).dosetARObjectSupportRotate(mIsSupportRotate);
                        PreferenceUtil.putBoolean(Constants.SP_KEY_IS_SUPPORT_ROTATE, mIsSupportRotate);
                        mTestRotate.setText(getString(R.string.tv_support_theme_rotate, mIsSupportRotate ? "开" : "关"));
                        break;
                    case R.id.test_center:
                        ((SDKDemoActivity) getActivity()).doresetARObjectPose();
                        Toast.makeText(getActivity(), "support reset theme position", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.test_gyroscope:
                        mIsSupportGyroscope = !mIsSupportGyroscope;
                        ((SDKDemoActivity) getActivity()).dosetARObjectSupportFreeMode(mIsSupportGyroscope);
                        PreferenceUtil.putBoolean(Constants.SP_KEY_IS_SUPPORT_FREEMODE, mIsSupportGyroscope);
                        mTestGyro.setText(getString(R.string.tv_support_theme_gyroscope, mIsSupportGyroscope ? "开" : "关"));
                        break;
                    default:
                        break;
                }
            }
        };

        mTestMove.setOnClickListener(listener);
        mTestScale.setOnClickListener(listener);
        mTestRotate.setOnClickListener(listener);
        mTestCenter.setOnClickListener(listener);
        mTestGyro.setOnClickListener(listener);
    }

    private void initMenu(View rootView) {
        mToastRl = (RelativeLayout) rootView.findViewById(R.id.rl_theme_toast_operator);
        mScanRl = (RelativeLayout) rootView.findViewById(R.id.rl_theme_scan_operator);
        mCameraRl = (RelativeLayout) rootView.findViewById(R.id.rl_theme_camera_operator);
        mThemeRl = (RelativeLayout) rootView.findViewById(R.id.rl_theme_model_operator);

        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedRl != null) {
                    mSelectedRl.setVisibility(View.GONE);
                }
                switch (v.getId()) {
                    case R.id.test_menu_toast:
                        mToastRl.setVisibility(View.VISIBLE);
                        mSelectedRl = mToastRl;
                        break;
                    case R.id.test_menu_scan:
                        mScanRl.setVisibility(View.VISIBLE);
                        mSelectedRl = mScanRl;
                        break;
                    case R.id.test_menu_camera:
                        mCameraRl.setVisibility(View.VISIBLE);
                        mSelectedRl = mCameraRl;
                        break;
                    case R.id.test_menu_theme:
                        mThemeRl.setVisibility(View.VISIBLE);
                        mSelectedRl = mThemeRl;
                        break;
                    default:
                        break;
                }
            }
        };
        rootView.findViewById(R.id.test_menu_toast).setOnClickListener(listener);
        rootView.findViewById(R.id.test_menu_scan).setOnClickListener(listener);
        rootView.findViewById(R.id.test_menu_camera).setOnClickListener(listener);
        rootView.findViewById(R.id.test_menu_theme).setOnClickListener(listener);
    }

    private void initRecognizeTimeList() {
        mRecTimeList = new ArrayList<>();
        mRecTimeRVAdapter = new RecognizeTimeListAdapter(getActivity(), mRecTimeList);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setStackFromEnd(true);
        mRecognizeTimeRV.setLayoutManager(manager);
        mRecognizeTimeRV.setAdapter(mRecTimeRVAdapter);
        mRecTimeRVAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onClick(View v) {
        if (!BtnUtils.isClickValid()) {
            return;
        }
        switch (v.getId()) {
            case R.id.start_scan:
                mIsScanning = !mIsScanning;
                if (mIsScanning) {
                    ((SDKDemoActivity) getActivity()).doStartScanARTheme();
                    showARMode();
                } else {
                    ((SDKDemoActivity) getActivity()).doStopARTheme();
                    showDefaultMode();
                    if (mIsScanning)
                        addRecognizeTimeLine();
                }
                break;
            case R.id.tv_discover:
                ((SDKDemoActivity) getActivity()).doDiscoverPage();
                break;
            case R.id.tv_pre:
                changePage(CHANGE_PAGE_PRE);
                break;
            case R.id.tv_next:
                changePage(CHANGE_PAGE_NEXT);
                break;
            default:
                break;
        }
    }

    public void showARMode() {
        mIsScanning = true;
        mStartScan.setText(R.string.stop_scan);
        mDiscoverTv.setVisibility(View.GONE);
    }

    public void showDefaultMode() {
        resetView();
        mIsScanning = false;
        mStartScan.setText(R.string.start_scan);
        mDiscoverTv.setVisibility(View.VISIBLE);
    }

    public boolean getScanningState() {
        return mIsScanning;
    }

    long lastTime = 0l;
    private void changePage(int type) {
        if(System.currentTimeMillis() - lastTime < 2000) {
            return;
        }
        ISARUnityMessageManager.changePage(type);
        lastTime = System.currentTimeMillis();
        Logger.LOGD(TAG + " changePage : " + type + " time : " + lastTime);
    }

    private void resetView() {
        mContentPageSlideCsm.setVisibility(View.GONE);
        mPrePageTv.setVisibility(View.GONE);
        mNextPageTv.setVisibility(View.GONE);
        if (null != mFloatButtonsLayout) {
            ViewGroup group = (ViewGroup) mView;
            group.removeView(mFloatButtonsLayout);
        }
    }

    public void addRecognizeTimeLine() {
//        Double recognizeTime = ((SDKDemoActivity) getActivity()).getRecognizeTime() / 1000.00;
//        String recogTime = new DecimalFormat("0.00").format(recognizeTime);
//        if (((SDKDemoActivity) getActivity()).getIsOfflineRecognized()) {
//            mRecTimeList.add(getResources().getString(R.string.tv_list_recognize_time_offline, recogTime));
//        } else if (((SDKDemoActivity) getActivity()).getHasLocalRecognized()) {
//            mRecTimeList.add(getResources().getString(R.string.tv_list_recognize_time_local, recogTime));
//        } else {
//            mRecTimeList.add(getResources().getString(R.string.tv_list_recognize_time_online, recogTime));
//        }
//        mRecTimeRVAdapter.notifyItemInserted(mRecTimeRVAdapter.getItemCount() - 1);
//        Logger.LOGD(TAG + " COUNT : " + mRecTimeRVAdapter.getItemCount());
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mRecognizeTimeRV.smoothScrollToPosition(mRecTimeRVAdapter.getItemCount() - 1);
//            }
//        }, 500);
    }

    public void setPageCount(int count) {
        Logger.LOGD(TAG + " setPagesCount " + count);
        if (count > 1) {
            mContentPageSlideCsm.setMaxNumber(count);
            mContentPageSlideCsm.setVisibility(View.VISIBLE);
            mPrePageTv.setVisibility(View.VISIBLE);
            mNextPageTv.setVisibility(View.VISIBLE);
        }
    }

    public void showPageIndex(int index) {
        Logger.LOGD(TAG + " showPageIndex " + index);
        mContentPageSlideCsm.setChoose(index);
        mContentPageSlideCsm.invalidate();
    }

    public void doARTouchEvent(String jsonString) {
        mHandler.obtainMessage(MSG_ON_AR_TOUCH_EVENT, jsonString).sendToTarget();
    }

    private void doTouchEvent(final String jsonStr, final boolean floatButton, final String floatBtnId) {
        Logger.LOGI(TAG + " touchEvent->jsonStr:" + jsonStr);
        try {
            JSONObject json = new JSONObject(jsonStr);
            if (null == json)
                return;
            if (json.has("type")) {
                int type = json.getInt("type");
                if (type == TOUCH_ACTION_SHARE_WEIXIN && json.has("share_way")) {
                    type += json.getInt("share_way");
                }
                switch (type) {
                    case TOUCH_ACTION_NEARBY://附近搜索 搜索关键字search_nearby
                        String searchKeyword = "";
                        if (json.has("search_nearby")) {
                            searchKeyword = json.getString("search_nearby");
                        }
                        if (TextUtils.isEmpty(searchKeyword)) {
                            Logger.LOGD(TAG + " search keyword is null");
                            break;
                        }
                        double currentLat = 30.546157;
                        double currentLng = 104.07687;
                        if (currentLng == 0.0 || currentLat == 0.0)
                            ISARTipsUtil.showShortToast(getActivity(), getResources().getString(R.string.weather_error_position));
                        Logger.LOGD(TAG + " search nearby  current location ： lat=" + currentLat + ", lng=" + currentLng);
                        final String searchUrl = "http://uri.amap.com/search?center="
                                + currentLng + "," + currentLat
                                + "&keyword=" + searchKeyword
                                + "&view=map"
                                + "&src=mypage";
                        Logger.LOGD(TAG + " searchUrl : " + searchUrl);

                        WebViewActivity.loadUrl(getActivity(), searchUrl, "加载中...");
                        break;
                    case TOUCH_ACTION_SHARE_WEIXIN://分享到微信朋友
                        ISARTipsUtil.showShortToast(getActivity(), "分享类型 : 微信朋友, json : " + json.toString());
                        break;
                    case TOUCH_ACTION_SHARE_PYQ://分享到微信朋友圈
                        ISARTipsUtil.showShortToast(getActivity(), "分享类型 : 微信朋友圈, json : " + json.toString());
                        break;
                    case TOUCH_ACTION_SHARE_WB://分享到微博
                        ISARTipsUtil.showShortToast(getActivity(), "分享类型 : 微博, json : " + json.toString());
                        break;
                    default:
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
