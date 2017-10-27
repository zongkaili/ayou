package com.idealsee.sdk.server;

import com.idealsee.sdk.model.ISARHttpResponseInfo;

/**
 * Created by hongen on 16-12-5.
 */

public interface ISARHttpRequestListener {
//    void onIdRequestOk(int status, String response, IdHttpRequest request);
//    void onIdRequestFailed(int status, String response, IdHttpRequest request);
    void onIdPostDone(ISARHttpResponseInfo responseInfo);
    void onIdGetDone(ISARHttpResponseInfo responseInfo);
}
