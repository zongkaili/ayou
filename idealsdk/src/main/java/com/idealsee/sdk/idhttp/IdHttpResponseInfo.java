package com.idealsee.sdk.idhttp;

import java.util.List;
import java.util.Map;

/**
 * Created by yaolei on 2017/3/22.
 */

class IdHttpResponseInfo {
    private int httpRespCode;
    private int flowRespCode;
    private String respString;
    private IdRequest request;
    private Map<String, List<String>> headerMap;

    public IdHttpResponseInfo() {
    }

    public IdHttpResponseInfo(IdHttpResponseInfo responseInfo) {
        this.httpRespCode = responseInfo.getHttpRespCode();
        this.flowRespCode = responseInfo.getFlowRespCode();
        this.respString = responseInfo.getRespString();
        this.request = responseInfo.request;
        this.headerMap = responseInfo.getHeaderMap();
    }

    public int getHttpRespCode() {
        return httpRespCode;
    }

    public void setHttpRespCode(int httpRespCode) {
        this.httpRespCode = httpRespCode;
    }

    public int getFlowRespCode() {
        return flowRespCode;
    }

    public void setFlowRespCode(int flowRespCode) {
        this.flowRespCode = flowRespCode;
    }

    public String getRespString() {
        return respString;
    }

    public void setRespString(String respString) {
        this.respString = respString;
    }

    public IdRequest getRequest() {
        return request;
    }

    public void setRequest(IdRequest request) {
        this.request = request;
    }

    public Map<String, List<String>> getHeaderMap() {
        return headerMap;
    }

    public void setHeaderMap(Map<String, List<String>> headerMap) {
        this.headerMap = headerMap;
    }
}
