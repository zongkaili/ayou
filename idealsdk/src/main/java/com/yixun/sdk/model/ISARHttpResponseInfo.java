package com.yixun.sdk.model;

import com.yixun.sdk.server.ISARHttpRequest;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by hongen on 16-12-12.
 */

public class ISARHttpResponseInfo implements Serializable {
    private int mHttpRespCode;
    private int mFlowRespCode;
    private String mResponseStr;
    private Map<String, List<String>> mHeaderMap;
    private ISARHttpRequest mRequest;

    public ISARHttpResponseInfo() {
    }

    public ISARHttpResponseInfo(ISARHttpResponseInfo responseInfo) {
        this.mHttpRespCode = responseInfo.getHttpRespCode();
        this.mFlowRespCode = responseInfo.getHttpRespCode();
        this.mResponseStr = responseInfo.getRespStr();
        this.mRequest = responseInfo.getRequest();
        this.mHeaderMap = responseInfo.getHeaderMap();
    }

    public int getHttpRespCode() {
        return mHttpRespCode;
    }

    public void setHttpRespCode(int httpRespCode) {
        this.mHttpRespCode = httpRespCode;
    }

    public int getFlowRespCode() {
        return mFlowRespCode;
    }

    public void setFlowRespCode(int flowRespCode) {
        this.mFlowRespCode = flowRespCode;
    }

    public String getRespStr() {
        return mResponseStr;
    }

    public void setRespStr(String responseStr) {
        this.mResponseStr = responseStr;
    }

    public ISARHttpRequest getRequest() {
        return mRequest;
    }

    public void setRequest(ISARHttpRequest request) {
        this.mRequest = request;
    }

    public Map<String, List<String>> getHeaderMap() {
        return this.mHeaderMap;
    }

    public void setHeaderMap(Map<String, List<String>> headerMap) {
        this.mHeaderMap = headerMap;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("HttpRespCode = " + mHttpRespCode + ";");
        builder.append("FlowRespCode = " + mFlowRespCode + ";");
        builder.append("ResponseStr = " + mResponseStr + ";");
        builder.append("Request Url = " + mRequest.getUrl() + ";");
        return builder.toString();
    }
}
