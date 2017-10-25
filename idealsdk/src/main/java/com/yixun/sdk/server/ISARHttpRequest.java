package com.yixun.sdk.server;

import android.content.SharedPreferences;

import java.util.Map;

/**
 * Created by hongen on 16-12-6.
 */

public class ISARHttpRequest {
    private String mUrl;
    private String mTargetPath;
    private String themePicMd5; // 主题md5，用于离线识别时使用
    private boolean mCanceled = false;
    private boolean mUseCache = false;
    private boolean mIsRedirect = true; // 302 redirect, default is true.
    private boolean isLocalRecognize = false; // 是否为离线识别，默认为false.
    private int mRetryCount = 0;
    private int mSequence = 0;
    private ISARHttpRequestListener mHttpReqListener = null; // for http server listener.
    private ISARRequestListener mARReqListener = null; // for client http listener.
    private ISARHttpRequestQueue.ISARCommand mARCommand;
    // preferences for eTag in animation api.
    private SharedPreferences mSharedPreferences;
    private Object mTag;
    private Map<String, String> mHeaderParams = null;
    private Map<String, String> mBodyParams = null;
    private Map<String, String> mFileParams = null;
    private Map<String, String> mUrlParams = null;

    public ISARHttpRequest() {
        this(null);
    }

    public ISARHttpRequest(String strUrl) {
        this(strUrl, null);
    }

    public ISARHttpRequest(String strUrl, ISARHttpRequestQueue.ISARCommand command) {
        this.mARCommand = command;
        this.mUrl = strUrl;
    }

    public boolean getCanceled() {
        return this.mCanceled;
    }

    public void setCanceled(boolean canceled) {
        this.mCanceled = canceled;
    }

    /**
     * Returns this request's tag
     *
     * @return
     */
    public Object getTag() {
        return this.mTag;
    }

    /***
     * Set a tag on this request. Can be used to cancel all requests with this tag.
     *
     * @param tag
     * @return This Request object
     */
    public ISARHttpRequest setTag(Object tag) {
        this.mTag = tag;
        return this;
    }

    public String getUrl() {
        return this.mUrl;
    }

    public void setUrl(String strUrl) {
        this.mUrl = strUrl;
    }

    public Map<String, String> getHeaderParams() {
        return this.mHeaderParams;
    }

    public void setHeaderParams(Map<String, String> headerParams) {
        this.mHeaderParams = headerParams;
    }

    public Map<String, String> getBodyParams() {
        return this.mBodyParams;
    }

    public void setBodyParams(Map<String, String> bodyParams) {
        this.mBodyParams = bodyParams;
    }

    public Map<String, String> getFileParams() {
        return this.mFileParams;
    }

    public void setFileParams(Map<String, String> fileParams) {
        this.mFileParams = fileParams;
    }

    public Map<String, String> getUrlParams() {
        return this.mUrlParams;
    }

    public void setUrlParams(Map<String, String> urlparams) {
        this.mUrlParams = urlparams;
    }

    public void setHttpRequestListener(ISARHttpRequestListener listener) {
        this.mHttpReqListener = listener;
    }

    public ISARHttpRequestListener getHttpRequestListener() {
        return this.mHttpReqListener;
    }

    public void setARReqListener(ISARRequestListener listener) {
        this.mARReqListener = listener;
    }

    public ISARRequestListener getARReqListener() {
        return this.mARReqListener;
    }

    public ISARHttpRequestQueue.ISARCommand getARCommand() {
        return this.mARCommand;
    }

    public void setARCommand(ISARHttpRequestQueue.ISARCommand command) {
        this.mARCommand = command;
    }

    public int getSequence() {
        return this.mSequence;
    }

    public void setSequence(int sequence) {
        this.mSequence = sequence;
    }

    public String getTargetPath() {
        return this.mTargetPath;
    }

    public void setTargetPath(String targetPath) {
        this.mTargetPath = targetPath;
    }

    public boolean isUseCache() {
        return mUseCache;
    }

    public void setUseCache(boolean useCache) {
        this.mUseCache = useCache;
    }

    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public void setSharedPreferences(SharedPreferences sharedPreferences) {
        this.mSharedPreferences = sharedPreferences;
    }

    public boolean isRedirect() {
        return this.mIsRedirect;
    }

    public void setRedirect(boolean isRedirect) {
        this.mIsRedirect = isRedirect;
    }

    public String getThemePicMd5() {
        return themePicMd5;
    }

    public void setThemePicMd5(String themePicMd5) {
        this.themePicMd5 = themePicMd5;
    }

    public boolean isLocalRecognize() {
        return isLocalRecognize;
    }

    public void setLocalRecognize(boolean localRecognize) {
        isLocalRecognize = localRecognize;
    }
}
