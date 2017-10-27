package com.idealsee.sdk.model;

/**
 * Created by hongen on 16-12-12.
 */

public class ISARImageMatchedRespInfo extends ISARHttpResponseInfo {

    public ISARImageMatchedRespInfo() {}

    public ISARImageMatchedRespInfo(ISARHttpResponseInfo responseInfo) {
        super(responseInfo);
    }

    private ISARImageSearchResult mImageSearchResult;

    public ISARImageSearchResult getImageSearchResult() {
        return mImageSearchResult;
    }

    public void setImageSearchResult(ISARImageSearchResult imageSearchResult) {
        this.mImageSearchResult = imageSearchResult;
    }
}
