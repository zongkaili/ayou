package com.yixun.sdk.model;

import com.yixun.sdk.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ISARAppInitInfo {
    private static final String TAG = "AppInitInfo";
    public static final String REDPACKET_UNITY_URL = "hb_unity_url";
    public static final String WEATHER_UNITY_URL = "weather_unity_url";
    public static final String ACCEPT_WEATHERS = "accept_weathers";
    public static final String FORCE_UPDATE = "force_update";
    public static final String VERSION = "version";
    public static final String DOWNLOAD_URL = "download_url";
    public static final String UPDATE_CONTENT = "update_content";
    // 移动活动封面
    public static final String MOBILE_ACTIVITY_IMG = "mobile_activity_img";
    // 移动活动h5页面
    public static final String MOBILE_ACTIVITY_URL = "mobile_activity_url";
    // 活动更新时间，比较新说明有更新，需要客户端显示
    public static final String ACTIVITY_UPDATE_DATE = "activity_update_date";
    // 活动主题列表，扫描和分享的时候要回调服务器
    public static final String ACTIVITY_RESOURCE_IDS = "activity_resource_ids";

    public static final String CDN_URL = "cdn_url";
    public static final String CDN_RESOURCE = "cdn_resource";
    public static final String CDN_PIC = "cdn_pic";
    public static final String CDN_PIC_FULL = "cdn_pic_full";
    public static final String CDN_RESOURCE_CROP = "cdn_resource_crop";

    public String mRedpacketUrl;
    public String mWeatherUrl;
    public List<String> mAcceptWeathers;
    public String mForceUpdate;
    public String mVersoin;
    public String mApkUrl;
    public String mUpdateContent;
    public String mActivityImg;
    public String mActivityUrl;
    public String mActivityUpdateTime;
    public List<String> mActivityResourceIds;
    public String mCDNResource;
    public String mCDNPic;
    public String mCDNPicFull;
    public String mCDNResCrop;

    private boolean mHasGotData = false;

    public ISARAppInitInfo() {
        mRedpacketUrl = "";
        mWeatherUrl = "";
        mAcceptWeathers = new ArrayList<String>();
        mForceUpdate = "0";
        mVersoin = "";
        mApkUrl = "";
        mUpdateContent = "";
        mActivityImg = "";
        mActivityUrl = "";
        mActivityResourceIds = new ArrayList<String>();
        mCDNResource = "";
        mCDNPic = "";
        mCDNPicFull = "";
        mHasGotData = false;
    }

    public ISARAppInitInfo(JSONObject json) {
        this();

        try {
            if (json.has(REDPACKET_UNITY_URL)) {
                mRedpacketUrl = json.getString(REDPACKET_UNITY_URL);
            }

            if (json.has(WEATHER_UNITY_URL)) {
                mWeatherUrl = json.getString(WEATHER_UNITY_URL);
            }

            if (json.has(ACCEPT_WEATHERS)) {
                JSONArray array = json.getJSONArray(ACCEPT_WEATHERS);
                for (int i = 0; i < array.length(); i++) {
                    mAcceptWeathers.add(array.getString(i));
                }
            }
            if (json.has(FORCE_UPDATE)) {
                mForceUpdate = json.getString(FORCE_UPDATE);
            }
            if (json.has(VERSION)) {
                mVersoin = json.getString(VERSION);
            }
            if (json.has(DOWNLOAD_URL)) {
                mApkUrl = json.getString(DOWNLOAD_URL);
            }
            if (json.has(UPDATE_CONTENT)) {
                mUpdateContent = json.getString(UPDATE_CONTENT);
            }
            if (json.has(MOBILE_ACTIVITY_IMG)) {
                mActivityImg = json.getString(MOBILE_ACTIVITY_IMG);
            }
            if (json.has(MOBILE_ACTIVITY_URL)) {
                mActivityUrl = json.getString(MOBILE_ACTIVITY_URL);
            }
            if (json.has(ACTIVITY_UPDATE_DATE)) {
                mActivityUpdateTime = json.getString(ACTIVITY_UPDATE_DATE);
            }
            if (json.has(ACTIVITY_RESOURCE_IDS)) {
                JSONArray stringArray = null;
                try {
                    stringArray = json.getJSONArray(ACTIVITY_RESOURCE_IDS);
                } catch (JSONException e) {
                    Logger.LOGW(TAG + "json h5 share title error");
                }
                if (stringArray == null) {
                    this.mActivityResourceIds = new ArrayList<String>();
                    String title = json.getString(ACTIVITY_RESOURCE_IDS);
                    this.mActivityResourceIds.add(title);
                } else {
                    int size = stringArray.length();
                    this.mActivityResourceIds = new ArrayList<String>(size);
                    for (int i = 0; i < size; i++) {
                        String title = stringArray.getString(i);
                        mActivityResourceIds.add(title);
                    }
                }
            }

            if (json.has(CDN_URL)) {
                JSONObject cdnJson = json.getJSONObject(CDN_URL);
                if (cdnJson.has(CDN_RESOURCE)) {
                    mCDNResource = cdnJson.getString(CDN_RESOURCE);
                }
                if (cdnJson.has(CDN_PIC)) {
                    mCDNPic = cdnJson.getString(CDN_PIC);
                }
                if (cdnJson.has(CDN_PIC_FULL)) {
                    mCDNPicFull = cdnJson.getString(CDN_PIC_FULL);
                }
                if (cdnJson.has(CDN_RESOURCE_CROP)) {
                    mCDNResCrop = cdnJson.getString(CDN_RESOURCE_CROP);
                }
            }
            mHasGotData = true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(REDPACKET_UNITY_URL + "=" + mRedpacketUrl + ";");
        builder.append(WEATHER_UNITY_URL + "=" + mWeatherUrl + ";");
        builder.append(ACCEPT_WEATHERS + "=" + mAcceptWeathers.size() + ";");
        builder.append(FORCE_UPDATE + "=" + mForceUpdate + ";");
        builder.append(VERSION + "=" + mVersoin + ";");
        builder.append(DOWNLOAD_URL + "=" + mApkUrl + ";");
        builder.append(UPDATE_CONTENT + "=" + mUpdateContent + ";");
        builder.append(MOBILE_ACTIVITY_IMG + "=" + mActivityImg + ";");
        builder.append(MOBILE_ACTIVITY_URL + "=" + mActivityUrl + ";");
        builder.append(ACTIVITY_UPDATE_DATE + "=" + mActivityUpdateTime + ";");
        int size = mActivityResourceIds.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                builder.append(ACTIVITY_RESOURCE_IDS + "[" + i + "]=" + mActivityResourceIds.get(i) + ";");
            }
        }
        return builder.toString();
    }

    public boolean isHasGotData() {
        return this.mHasGotData;
    }

    public void setHasGotData(boolean hasGotData) {
        this.mHasGotData = hasGotData;
    }
}
