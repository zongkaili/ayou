package com.idealsee.sdk.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;

public class ISARTipImageInfo implements Serializable {

    /**
     * serial.
     */
    private static final long serialVersionUID = -6945254674826605192L;

    private static final String WIDTH = "width"; // editor id
    private static final String NAME = "name";
    private static final String MEDIA_SRC = "media_src";
    private static final String HEIGHT = "height"; //
    // 活动目标H5, 如果有地址，则表示可以点击进入，如果为空字符串，则表示不可点击
    private static final String APP_ACTIVITY_URL = "app_activity_url";

    private String mWidth;
    private String mHeight;
    private String mName;
    private String mMediaSrc;
    private String mAppActivityUrl = "";
    // private boolean mAppActivity;
    // private boolean mAppActivityActive;
    private Bitmap mBitmap;

    /**
     * Constructor.
     * @param json json string
     */
    public ISARTipImageInfo(JSONObject json) {
        try {
            if (json.has(WIDTH)) {
                mWidth = json.getString(WIDTH);
            }
            if (json.has(HEIGHT)) {
                mHeight = json.getString(HEIGHT);
            }
            if (json.has(NAME)) {
                mName = json.getString(NAME);
            }
            if (json.has(MEDIA_SRC)) {
                mMediaSrc = json.getString(MEDIA_SRC);
            }
            if (json.has(APP_ACTIVITY_URL)) {
                mAppActivityUrl = json.getString(APP_ACTIVITY_URL);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getmWidth() {
        return mWidth;
    }

    public void setmWidth(String mWidth) {
        this.mWidth = mWidth;
    }

    public String getmHeight() {
        return mHeight;
    }

    public void setmHeight(String mHeight) {
        this.mHeight = mHeight;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmMediaSrc() {
        return mMediaSrc;
    }

    public void setmMediaSrc(String mMediaSrc) {
        this.mMediaSrc = mMediaSrc;
    }


    public String getmAppActivityUrl() {
        return mAppActivityUrl;
    }

    public void setmAppActivityUrl(String mAppActivityUrl) {
        this.mAppActivityUrl = mAppActivityUrl;
    }

    public Bitmap getmBitmap() {
        return mBitmap;
    }

    public void setmBitmap(Bitmap mBitmap) {
        this.mBitmap = mBitmap;
    }

    @Override
    public int hashCode() {
        int result = MEDIA_SRC.hashCode();
        result = 29 * result;
        return result;
    }

    @Override
    public boolean equals(Object other) {
        // 先检查是否其自反性，后比较other是否为空。这样效率高
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof ISARTipImageInfo)) {
            return false;
        }

        final ISARTipImageInfo info = (ISARTipImageInfo) other;

        if (!getmMediaSrc().equals(info.getmMediaSrc())) {
            return false;
        }
        return true;
    }
    
    /**
     * format string.
     * @return string
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("mWidth=" + mWidth + ",");
        sb.append("mHeight=" + mHeight + ",");
        sb.append("mName=" + mName + ",");
        sb.append("mMediaSrc=" + mMediaSrc);
        return sb.toString();
    }

}
