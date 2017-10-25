package com.yixun.sdk.idhttp;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

/**
 * Created by yaolei on 2017/3/22.
 */

public class IdHttpClient {
    public static final String SEGMENT = ":";
    private static final String TAG = IdHttpClient.class.getSimpleName();
    private static IdHttpClient sIdHttpClient;
    private IdHttpServer mIdHttpServer;


    private IdHttpClient() {
    }

    public synchronized static IdHttpClient getInstance() {
        if (null == sIdHttpClient) {
            sIdHttpClient = new IdHttpClient();
        }
        return sIdHttpClient;
    }

    public void init(File cacheDir) {
        mIdHttpServer = new IdHttpServer(cacheDir);
    }

    public String doJsonGet(String url) {
        String result;
        IdJsonRequest request = new IdJsonRequest();
        request.setUrl(url);
        request.setMethod("GET");
        try {
            IdHttpResponseInfo info = mIdHttpServer.doJsonGet(request);
            int code = info.getHttpRespCode();
            result = code + SEGMENT + info.getRespString();
        } catch (IOException e) {
            e.printStackTrace();
            result = TAG + " doJsonGet: " + e.getMessage();
        }
        return result;
    }

    public boolean doFileDownload(String url, String targetPath, long maxFileLength, IdProgressListener listener) {
        boolean downloadSuccess = false;
        IdFileGetRequest request = new IdFileGetRequest();
        request.setUrl(url);
        request.setTargetPath(targetPath);
        request.setMaxFileLength(maxFileLength);
        request.setListener(listener);
        request.setMethod("GET");
        try {
            IdHttpResponseInfo info = mIdHttpServer.doFileGet(request);
            String result = info.getHttpRespCode() + SEGMENT + info.getRespString();
            downloadSuccess = true;
            if (!result.startsWith(String.valueOf(HttpURLConnection.HTTP_OK))) {
                downloadSuccess = false;
                listener.onFailed(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
            String result = TAG + " doFileDownload: " + e.getMessage();
            listener.onFailed(result);
        }
        return downloadSuccess;
    }

    public String doFilePost(String url, Map<String, String> headers, Map<String, String> bodyParams,
                             Map<String, String> fileParams, IdProgressListener listener) {
        String result;
        IdFilePostRequest request = new IdFilePostRequest();
        request.setUrl(url);
        request.setHeaders(headers);
        request.setBodyParams(bodyParams);
        request.setFileParams(fileParams);
        request.setListener(listener);
        request.setMethod("POST");
        try {
            IdHttpResponseInfo info = mIdHttpServer.doFilePost(request);
            result = info.getHttpRespCode() + SEGMENT + info.getRespString();
        } catch (IOException e) {
            e.printStackTrace();
            result = TAG + " doFilePost: " + e.getMessage();
        }
        return result;
    }

    public String getRecordFileSuffix() {
        return mIdHttpServer.getRecordFileSuffix();
    }
}
