package com.idealsee.sdk.model;


/**
 * Created by hongen on 16-12-12.
 */

public class ISARAnimDownloadRespInfo extends ISARHttpResponseInfo {

    private ISARRandomInfo mRandomInfo;
    private String mETag;

    public ISARRandomInfo getRandomInfo() {
        return mRandomInfo;
    }

    public void setRandomInfo(ISARRandomInfo mRandomInfo) {
        this.mRandomInfo = mRandomInfo;
    }

    public String geteTag() {
        return mETag;
    }

    public void seteTag(String eTag) {
        this.mETag = eTag;
    }
}
