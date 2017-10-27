package com.idealsee.ar.unity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.idealsee.ar.widget.ISARNewDialog;
import com.yixun.sdk.R;
import com.idealsee.sdk.activity.ISARPicturePagerActivity;
import com.idealsee.sdk.activity.ISARVideoPlayerActivity;
import com.idealsee.sdk.server.ISARHttpClient;
import com.idealsee.sdk.server.ISARHttpRequest;
import com.idealsee.sdk.server.ISARHttpServerURL;
import com.idealsee.sdk.util.ISARNetUtil;
import com.idealsee.sdk.util.ISARThreadPool;
import com.idealsee.sdk.util.ISARTipsUtil;
import com.idealsee.sdk.util.Logger;
import com.unity3d.player.UnityPlayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yaolei on 17-9-21.
 */

public class ISARTouchEvent {
    private static final String ALIAS_SUBMIT_SUCCESS = "submitOk";//此别名“submitOk”已与前端约定好，不可随意更改
    private static final int TOUCH_ACTION_TYPE_PHONE = 0;
    private static final int TOUCH_ACTION_TYPE_URL = 1;
    private static final int TOUCH_ACTION_TYPE_GPS = 2;
    private static final int TOUCH_ACTION_TYPE_CONTENT = 3;
    private static final int TOUCH_ACTION_TYPE_MUSIC = 6;
    private static final int TOUCH_ACTION_TYPE_IMAGE = 7;
    private static final int TOUCH_ACTION_TYPE_DOWNLOAD_APP = 8;
    private static final int TOUCH_ACTION_TYPE_TRIGGER = 11;
    private static final int TOUCH_ACTION_TYPE_VIDEO = 14;
    private static final int TOUCH_ACTION_TYPE_JUMP_PAGE = 15;
    private static final int TOUCH_ACTION_TYPE_CHANGE_MODEL_TEXTURE = 17;
    private static final int TOUCH_ACTION_TYPE_PLAY_MODEL_ANIMATION = 18;
    private static final int TOUCH_ACTION_SIMILAR_AR = 19;
    private static final int TOUCH_ACTION_NEARBY = 21;
    private static final int TOUCH_ACTION_DRAW_LIST = 22;
    private static final int TOUCH_ACTION_SHARE_WEIXIN = 24;
    private static final int TOUCH_ACTION_SHARE_PYQ = 25;
    private static final int TOUCH_ACTION_SHARE_WB = 26;

    private Activity mActivity;
    private UnityPlayer mUnityPlayer;
    private String mMD5, mSimilarMD5;

    public ISARTouchEvent(Activity mActivity, UnityPlayer unityPlayer) {
        this.mActivity = mActivity;
        this.mUnityPlayer = unityPlayer;
    }

    public void setThemeMD5(String md5, String similarTargetName) {
        this.mMD5 = md5;
        this.mSimilarMD5 = similarTargetName;
    }

    public void onWidgetTouch(String jsonStr) {
        onTouchEvent(jsonStr, false, "");
    }

    public void onFloatButtonTouch(String jsonStr, String floatBtnId) {
        onTouchEvent(jsonStr, true, floatBtnId);
    }

    /**
     * 用户点击视频元件后，会执行此方法。
     *
     * @param filePath
     */
    public void doTouchVideoEvent(String filePath) {
        if(TextUtils.isEmpty(filePath)){
            ISARTipsUtil.showShortToast(mActivity,"file path is empty !");
            return;
        }
        String localPath = ISARUnityPath.getLocalVideoPath(filePath);
        File localFile = new File(localPath);
        if (localFile.exists()) {
            filePath = "file://"+localPath;
        }
        Intent intent = new Intent(mActivity, ISARVideoPlayerActivity.class);
        intent.putExtra("ideal_video_path", filePath);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.startActivity(intent);
    }

    private void onTouchEvent(String jsonStr, boolean isFloatButton, String floatBtnId) {
        int type = getJsonInt(jsonStr, "type");
        if (type == -1) {
            return;
        }
        JSONObject json = null;
        try {
            json = new JSONObject(jsonStr);
            switch (type) {
                case TOUCH_ACTION_TYPE_PHONE:
                    final String number = json.getString("number");
                    doTouchPhoneEvent(number);
                    break;
                case TOUCH_ACTION_TYPE_URL:
                    final String url = json.getString("url");
                    doTouchWebsiteEvent(url);
                    break;
                case TOUCH_ACTION_TYPE_GPS:
                    final String target = json.getString("target");
                    final double lat = json.getDouble("lat");
                    final double lng = json.getDouble("lng");
                    doTouchLocationEvent(mActivity, target, lat, lng);
                    break;
                case TOUCH_ACTION_TYPE_CONTENT:
                    final String content = json.getString("content");
                    doTouchContentEvent(content);
                    break;
                case TOUCH_ACTION_TYPE_MUSIC:
                    final String mp3 = json.getString("url");
                    doTouchAudioEvent(mp3);
                    break;
                case TOUCH_ACTION_TYPE_IMAGE:
                    if (!json.has("md5"))
                        break;
                    String image = json.getString("md5");
                    doTouchImageEvent(image);
                    break;
                case TOUCH_ACTION_TYPE_DOWNLOAD_APP:
                    final String androidUrl = json.getString("android_url");
                    doTouchDownloadEvent(androidUrl);
                    break;
                case TOUCH_ACTION_TYPE_VIDEO:
                    String videoPath = "";
                    if (json.has("video_key")) {
                        videoPath = json.getString("video_key");
                    } else if (json.has("url")) {
                        videoPath = json.getString("url");
                        videoPath = ISARNetUtil.getResourceUrlFromMD5(videoPath);
                    }
                    doTouchVideoEvent(videoPath);
                    break;
                case TOUCH_ACTION_TYPE_TRIGGER://trigger
                    if (isFloatButton) {
                        ISARUnityMessageManager.playModelByFloatButton(floatBtnId);
                    } else {
                        ISARUnityMessageManager.triggerAction(jsonStr);
                    }
                    break;
                case TOUCH_ACTION_TYPE_JUMP_PAGE://pager skip
                    ISARUnityMessageManager.jumpToPage(jsonStr);
                    break;
                case TOUCH_ACTION_TYPE_CHANGE_MODEL_TEXTURE: //model change
                    if (isFloatButton) {
                        ISARUnityMessageManager.playModelByFloatButton(floatBtnId);
                    } else {
                        ISARUnityMessageManager.changeModelTexture(jsonStr);
                    }
                    break;
                case TOUCH_ACTION_TYPE_PLAY_MODEL_ANIMATION: //model animation
                    if (isFloatButton) {
                        ISARUnityMessageManager.playModelByFloatButton(floatBtnId);
                    } else {
                        ISARUnityMessageManager.playModelAnimation(jsonStr);
                    }
                    break;
                case TOUCH_ACTION_SIMILAR_AR: // 显示相似主题
                    doTouchSimilarAREvent();
                    break;
                case TOUCH_ACTION_NEARBY://附近搜索 搜索关键字search_nearby
                                    /*
                                     * TODO 需要支持定位，SDK暂无此需求
                                     */
                    break;
                case TOUCH_ACTION_DRAW_LIST:// 表单 类型22 url
                    String formUrl = "";
                    if (json.has("url")) {
                        formUrl = json.getString("url");
                    }
                    if (TextUtils.isEmpty(formUrl)) {
                        break;
                    }
                    doTouchFormEvent(formUrl);
                    break;
                case TOUCH_ACTION_SHARE_WEIXIN://分享到微信朋友
                case TOUCH_ACTION_SHARE_PYQ://分享到微信朋友圈
                case TOUCH_ACTION_SHARE_WB://分享到微博
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 用户点击电话元件后，会执行此方法。
     *
     * @param number
     */
    private void doTouchPhoneEvent(String number) {
        Uri uri = Uri.parse("tel:" + number);
        Intent intent = new Intent(Intent.ACTION_DIAL, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.startActivity(intent);
    }

    /**
     * 点击事件: 定位.
     *
     * @param target target.
     * @param lat    lat.
     * @param lng    lng.
     * @context context.
     */
    private void doTouchLocationEvent(Context context, String target, double lat, double lng) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://ditu.google.cn/maps?hl=zh&mrt=loc&q=" + lat + "," + lng));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        if (isIntentAvailable(context, intent)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            context.startActivity(intent);
        } else {
            Intent it = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://ditu.google.cn/maps?hl=zh&mrt=loc&q=" + lat + "," + lng));
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            context.startActivity(it);
        }
    }

    /**
     * 用户点击内容元件后，会执行此方法。
     *
     * @param content
     */
    private void doTouchContentEvent(String content) {
        ViewGroup parentView = (ViewGroup) (mUnityPlayer.getParent());
        View layoutView = LayoutInflater.from(mActivity).inflate(R.layout.inc_home_content, null);
        Drawable drawable = mActivity.getResources().getDrawable(R.color.eighty_per_black);
        WebView mContentWv;
        final PopupWindow mContentWindow;
        TextView closeView = (TextView) layoutView.findViewById(R.id.tv_content_close);

        mContentWv = (WebView) layoutView.findViewById(R.id.wv_home_content_text);
        // 默认编码为GBK
        mContentWv.getSettings().setDefaultTextEncodingName("UTF-8");
        mContentWv.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);// 使图片自适应大小, 不再有滚动条
        mContentWv.loadDataWithBaseURL(null, content, "text/html", "UTF-8", null);
        // mContentWv.getSettings().setJavaScriptEnabled(true);
        // mContentWv.addJavascriptInterface(new JavascriptInterface(getActivity()), "imagelistner");

        mContentWindow = new PopupWindow(layoutView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        mContentWindow.setBackgroundDrawable(drawable);
        mContentWindow.setFocusable(true);
        mContentWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0);
        mContentWindow.update();
        closeView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mContentWindow.dismiss();
            }
        });
    }

    /**
     * 用户点击表单元件后，会执行此方法。
     *
     * @param formUrl
     */
    private void doTouchFormEvent(final String formUrl) {
        View view = View.inflate(mActivity, R.layout.inc_home_content_form, null);
        ImageView closeIv = (ImageView) view.findViewById(R.id.iv_home_content_close);
        WebView contentWv = (WebView) view.findViewById(R.id.wv_home_content_text);
        ISARNewDialog dialog = new ISARNewDialog(mActivity, 0, 0, view, R.style.style_alert_dialog);
        dialog.setWebView(contentWv);
        dialog.setCloseView(closeIv);
        DisplayMetrics dm = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        showFormDialog(mActivity, dialog, formUrl, screenWidth * 5 / 6, screenHeight * 4 / 5);
    }

    /**
     * dialog which shows form
     *
     * @param context
     * @param dialog
     * @param url
     * @param dialogWidth
     * @param dialogHeight
     */
    private void showFormDialog(final Context context, final ISARNewDialog dialog, final String url
            , int dialogWidth, int dialogHeight) {
        if (null == dialog) {
            Logger.LOGW("TouchActionUtil dialog is null.");
            return;
        }
        ImageView closeView = (ImageView) dialog.getCloseView();
        final WebView contentWv = dialog.getWebView();
        // 默认编码为GBK
        contentWv.getSettings().setDefaultTextEncodingName("UTF-8");
        contentWv.getSettings().setJavaScriptEnabled(true);
        //设置缓存模式
        if (ISARNetUtil.isNetworkConnected(context)) {
            //根据cache-control决定是否从网络上取数据:网页数据有更新(缓存过时)才从网络加载(如果网页的cache-control为no-cache，则无论如何都会从网络加载，没网就会出现错误页面)
            contentWv.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            //只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据：只要有缓存就用缓存，即使网页数据更新(缓存过时)了，没有缓存才从网络加载
            contentWv.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        // 开启 DOM storage API 功能
        contentWv.getSettings().setDomStorageEnabled(true);
        // 开启 Application Caches 功能
        contentWv.getSettings().setAppCacheEnabled(true);
        contentWv.loadUrl(url);

        contentWv.setWebChromeClient(new WebChromeClient());
        contentWv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                contentWv.setVisibility(View.VISIBLE);
            }
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                contentWv.setVisibility(View.INVISIBLE);
            }
        });
        dialog.show();
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = dialogWidth;
        params.height = dialogHeight;
        dialog.getWindow().setAttributes(params);

        closeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        contentWv.addJavascriptInterface(new newJavascriptInterface() {
            @JavascriptInterface
            @Override
            public void submitSuccess(String jsonStr) {
                super.submitSuccess(jsonStr);
                ISARTipsUtil.showShortToast(context, R.string.tip_submit_success);
                if (dialog.isShowing()) {
                    new Handler(context.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    });
                }
            }

            @JavascriptInterface
            @Override
            public boolean submitClick() {
                //供前端查看网络状态
                return ISARNetUtil.isNetworkConnected(context);
            }

        }, ALIAS_SUBMIT_SUCCESS);
    }

    /**
     * 用户点击音频元件后，会执行此方法。
     *
     * @param url
     */
    private void doTouchAudioEvent(String url) {
    }

    /**
     * 用户点击图片元件后，会执行此方法。
     *
     * @param url
     */
    private void doTouchImageEvent(String url) {
        Intent it = new Intent(mActivity, ISARPicturePagerActivity.class);
        it.putExtra("pic_json", url);
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.startActivity(it);
    }

    /**
     * 用户点击下载元件后，会执行此方法。
     *
     * @param url
     */
    private void doTouchDownloadEvent(String url) {
        doLoadWeb(url);
    }

    /**
     * 用户点击网页元件后，会执行此方法。
     *
     * @param url
     */
    private void doTouchWebsiteEvent(String url) {
        doLoadWeb(url);
    }

    /**
     * 用户点击相似主题元件后，会执行此方法。
     */
    private void doTouchSimilarAREvent() {
        ISARThreadPool.INSTANCE.execute(new Runnable() {
            @Override
            public void run() {
                ISARHttpRequest request = new ISARHttpRequest(ISARHttpServerURL.getSimilarThemeUrl());
                Map<String, String> urlParams = new HashMap<>(2);
                urlParams.put("md5", mMD5);
                urlParams.put("similar_md5", mSimilarMD5);
                request.setUrlParams(urlParams);
                String similarAR = null;
                try {
                    similarAR = ISARHttpClient.getInstance().getSimilarTheme(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                doLoadWeb(similarAR);
            }
        });
    }

    private void doLoadWeb(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            // 通过Uri获得编辑框里的//地址，加上http://是为了用户输入时可以不要输入
            url = "http://" + url;
        }
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 建立Intent对象，传入uri
        mActivity.startActivity(intent);
    }

    private boolean isIntentAvailable(Context context, Intent intent) {
        List<ResolveInfo> activities = context.getPackageManager().queryIntentActivities(intent,
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        return activities.size() != 0;
    }

    private class newJavascriptInterface {
        @JavascriptInterface
        public void submitSuccess(String jsonStr) {
        }

        @JavascriptInterface
        public boolean submitClick() {
            return true;
        }
    }

    private int getJsonInt(String jsonStr, String tag) {
        int result = -1;
        try {
            JSONObject json = new JSONObject(jsonStr);
            if (json.has(tag)) {
                result = json.getInt(tag);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
