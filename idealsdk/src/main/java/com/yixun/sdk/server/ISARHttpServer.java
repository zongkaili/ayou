package com.yixun.sdk.server;

import android.content.SharedPreferences;
import android.net.http.HttpResponseCache;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.yixun.sdk.model.ISARAnimDownloadRespInfo;
import com.yixun.sdk.model.ISARDownloadInfo;
import com.yixun.sdk.model.ISARHttpResponseInfo;
import com.yixun.sdk.util.ISARStringUtil;
import com.yixun.sdk.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CacheResponse;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by hongen on 16-12-2.
 * <p>
 * 分析文件上传的数据格式，然后根据格式构造相应的发送给服务器的字符串。
 * 格式如下：这里的boundary是我自己构造的字符串，可以是其他任何的字符串
 * --boundary （\r\n）
 * Content-Disposition: form-data; name="text" （\r\n）
 * （\r\n）
 * content for text, this is string params （\r\n）
 * --boundary （\r\n）
 * Content-Disposition: form-data; name="img"; filename="t.txt" （\r\n）
 * Content-Type: application/octet-stream （\r\n）
 * （\r\n）
 * content for img, this is file params （\r\n）
 * --boundary-- （\r\n）
 * （\r\n）
 * <p>
 * 上面的（\r\n）表示各个数据必须以（\r\n）结尾
 */

public class ISARHttpServer {
    private static final String TAG = "idsdk/ISARHttpServer";
    private static final String BOUNDARY = "-----boundary";
    private static final int CONN_TIME_OUT = 15000; //连接超时为15秒
    private static final int READ_TIME_OUT = 15000; //读取超时为15秒
    private static final int MAX_STATLE_TIME = 60 * 60; // 缓存过期时间为1小时
    private static final String LINE_END = "\r\n";
    private static final String TWO_HYPHENS = "--";
    private static final String CHARSET = "UTF-8";


    /**
     * post http to server. response string will be notified by ISARHttpRequestListener.
     *
     * @param request
     * @return IdHttpResponseInfo responseInfo
     * @throws IOException
     */
    public ISARHttpResponseInfo doPost(ISARHttpRequest request) throws IOException {
        if (null == request || null == request.getUrl()) {
            throw new NullPointerException("ISARHttpRequest is null.");
        }
        ISARHttpRequestListener requestListener = request.getHttpRequestListener();
        if (null == requestListener) {
            Logger.LOGW(TAG + " doPost request listener is null.");
        }
        Logger.LOGD(TAG + " doPost request url:" + request.getUrl());
        ISARHttpResponseInfo respInfo = new ISARHttpResponseInfo();
        respInfo.setRequest(request);
        int code = 1;
        if (request.isUseCache()) {
            Logger.LOGI(TAG + " doPost using cache, do nothing.");
            code = HttpURLConnection.HTTP_OK;
            respInfo.setHttpRespCode(code);
            respInfo.setRespStr("");
            if (null != requestListener) {
                request.getHttpRequestListener().onIdPostDone(respInfo);
            }
            return respInfo;
        }

        HttpURLConnection conn = initPostConnection(request.getUrl());
        initConnectionHeader(conn, request);
        try {
            conn.connect();
        } catch (SocketTimeoutException e) {
            Logger.LOGE(TAG + " doPost connect error.");
            e.printStackTrace();
            respInfo.setHttpRespCode(code);
            respInfo.setRespStr(getConnErrorMsg(conn));
            if (null != requestListener) {
                requestListener.onIdPostDone(respInfo);
            }
            return respInfo;
        } catch (ConnectException e) {
            Logger.LOGE(TAG + " doPost connect error.");
            e.printStackTrace();
            respInfo.setHttpRespCode(code);
            respInfo.setRespStr(getConnErrorMsg(conn));
            if (null != requestListener) {
                requestListener.onIdPostDone(respInfo);
            }
            return respInfo;
        }
        Logger.LOGD(TAG + " doPost connect ok.");
        // 设置DataOutputStream, 写入string和file数据
        DataOutputStream ds = new DataOutputStream(conn.getOutputStream());
        writeStringParams(ds, request.getBodyParams());
        writeFileParams(ds, request.getFileParams());
        paramsEnd(ds);
        // close streams
        ds.flush();
        // 关闭DataOutputStream
        ds.close();

        // 得到响应码
        code = conn.getResponseCode();
        String result;
        try {
            if (HttpURLConnection.HTTP_OK == code) {
                result = getConnOkMsg(conn);
            } else {
                result = getConnErrorMsg(conn);
            }
        } finally {
            conn.disconnect();
        }

        Logger.LOGD(TAG + " upload done 上传结果 code:" + code + ",result:" + result);
        respInfo.setHttpRespCode(code);
        respInfo.setRespStr(result);
        if (null != requestListener) {
            request.getHttpRequestListener().onIdPostDone(respInfo);
        }

        return respInfo;
    }

    /**
     * post http to server. response string will be notified by ISARHttpRequestListener.
     *
     * @param request
     * @return IdHttpResponseInfo responseInfo
     * @throws IOException
     */
    public ISARHttpResponseInfo doPostJson(ISARHttpRequest request) throws IOException {
        if (null == request || null == request.getUrl()) {
            throw new NullPointerException("ISARHttpRequest is null.");
        }
        ISARHttpRequestListener requestListener = request.getHttpRequestListener();
        if (null == requestListener) {
            Logger.LOGW(TAG + " doPost request listener is null.");
        }
        Logger.LOGD(TAG + " doPost request url:" + request.getUrl());
        ISARHttpResponseInfo respInfo = new ISARHttpResponseInfo();
        respInfo.setRequest(request);
        int code = 1;
        if (request.isUseCache()) {
            Logger.LOGI(TAG + " doPost using cache, do nothing.");
            code = HttpURLConnection.HTTP_OK;
            respInfo.setHttpRespCode(code);
            respInfo.setRespStr("");
            if (null != requestListener) {
                request.getHttpRequestListener().onIdPostDone(respInfo);
            }
            return respInfo;
        }

        HttpURLConnection conn = initPostJsonConnection(request.getUrl());
        initConnectionHeader(conn, request);
        try {
            conn.connect();
        } catch (SocketTimeoutException e) {
            Logger.LOGE(TAG + " doPost connect error.");
            e.printStackTrace();
            respInfo.setHttpRespCode(code);
            respInfo.setRespStr(getConnErrorMsg(conn));
            if (null != requestListener) {
                requestListener.onIdPostDone(respInfo);
            }
            return respInfo;
        } catch (ConnectException e) {
            Logger.LOGE(TAG + " doPost connect error.");
            e.printStackTrace();
            respInfo.setHttpRespCode(code);
            respInfo.setRespStr(getConnErrorMsg(conn));
            if (null != requestListener) {
                requestListener.onIdPostDone(respInfo);
            }
            return respInfo;
        }
        Logger.LOGD(TAG + " doPost connect ok.");
        // 设置DataOutputStream, 写入string和file数据
        DataOutputStream ds = new DataOutputStream(conn.getOutputStream());
        writeStringJsonParams(ds, request.getBodyParams());
        writeFileParams(ds, request.getFileParams());
//        paramsEnd(ds);
        // close streams
        ds.flush();
        // 关闭DataOutputStream
        ds.close();

        // 得到响应码
        code = conn.getResponseCode();
        String result;
        try {
            if (HttpURLConnection.HTTP_OK == code) {
                result = getConnOkMsg(conn);
            } else {
                result = getConnErrorMsg(conn);
            }
        } finally {
            conn.disconnect();
        }

        Logger.LOGD(TAG + " upload done 上传结果 code:" + code + ",result:" + result);
        respInfo.setHttpRespCode(code);
        respInfo.setRespStr(result);
        if (null != requestListener) {
            request.getHttpRequestListener().onIdPostDone(respInfo);
        }

        return respInfo;
    }

    /**
     * Do get method, connection input stream will be read to response string will be in IdHttpResponseInfo,
     * and call back with ISARHttpRequest if request is not null.
     * 1. using cache for get method, return cache data if has cached; or return http data if has not cached.
     * 2. using http for get method, return http data.
     *
     * @param request ISARHttpRequest instance
     * @return IdHttpResponseInfo
     * @throws IOException
     */
    public ISARHttpResponseInfo doGet(ISARHttpRequest request) throws IOException {
        if (null == request || null == request.getUrl()) {
            throw new NullPointerException("ISARHttpRequest is null.");
        }
        ISARHttpRequestListener requestListener = request.getHttpRequestListener();
        if (null == requestListener) {
            Logger.LOGW(TAG + " doGet request listener is null.");
        }
        ISARHttpResponseInfo respInfo = new ISARHttpResponseInfo();
        respInfo.setRequest(request);

        int code = 1;
        HttpURLConnection conn = initGetConnection(request);
        Logger.LOGD(TAG + " doGet request url:" + request.getUrl());

        initConnectionHeader(conn, request);
        // 判断是否存在cache
        initGetCacheConnection(conn, request);
        try {
            if (!request.isUseCache()) {
                conn.connect();
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            respInfo.setHttpRespCode(code);
            respInfo.setRespStr(getConnErrorMsg(conn));
            return respInfo;
        } catch (ConnectException e) {
            Logger.LOGE(TAG + " doGet connect error.");
            e.printStackTrace();
            respInfo.setHttpRespCode(code);
            respInfo.setRespStr(getConnErrorMsg(conn));
            return respInfo;
        }

        // 得到响应码
        code = conn.getResponseCode();
        String result;
        Map<String, List<String>> headerMap = null;
        try {
            Logger.LOGD(TAG + " doGet code:" + code + ",request.isUseCache():" + request.isUseCache());
            if (HttpURLConnection.HTTP_OK == code || request.isUseCache()) {
                code = HttpURLConnection.HTTP_OK;
                result = getConnOkMsg(conn);
                // headerMap = conn.getHeaderFields();
            } else {
                result = getConnErrorMsg(conn);
            }
        } finally {
            conn.disconnect();
        }

        Logger.LOGD(TAG + " doGet code:" + code + ",result:" + result);
        //respInfo.setHeaderMap(headerMap);
        respInfo.setHttpRespCode(code);
        respInfo.setRespStr(result);
        return respInfo;
    }

    /**
     * 获取302跳转链接，不读取connection input stream.
     *
     * @param request
     * @return
     * @throws IOException
     */
    public ISARHttpResponseInfo getRedirectUrl(ISARHttpRequest request) throws IOException {
        if (null == request || null == request.getUrl()) {
            throw new NullPointerException("ISARHttpRequest is null.");
        }
        ISARHttpRequestListener requestListener = request.getHttpRequestListener();
        if (null == requestListener) {
            Logger.LOGW(TAG + " getRedirectUrl request listener is null.");
        }
        ISARHttpResponseInfo respInfo = new ISARHttpResponseInfo();
        respInfo.setRequest(request);

        int code = 1;
        HttpURLConnection conn = initGetConnection(request);
        Logger.LOGD(TAG + " getRedirectUrl request url:" + request.getUrl());

        initConnectionHeader(conn, request);
        // 判断是否存在cache
        initGetCacheConnection(conn, request);
        try {
            if (!request.isUseCache()) {
                conn.connect();
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            respInfo.setHttpRespCode(code);
            respInfo.setRespStr(getConnErrorMsg(conn));
            return respInfo;
        } catch (ConnectException e) {
            Logger.LOGE(TAG + " getRedirectUrl connect error.");
            e.printStackTrace();
            respInfo.setHttpRespCode(code);
            respInfo.setRespStr(getConnErrorMsg(conn));
            return respInfo;
        }

        // 得到响应码
        code = conn.getResponseCode();
        String result;
        Map<String, List<String>> headerMap = null;
        try {
            Logger.LOGD(TAG + " getRedirectUrl code:" + code + ",request.isUseCache():" + request.isUseCache());
            if (HttpURLConnection.HTTP_OK == code ||
                    HttpURLConnection.HTTP_MOVED_TEMP == code ||
                    request.isUseCache()) {
                code = HttpURLConnection.HTTP_OK;
                headerMap = conn.getHeaderFields();
                result = "";
            } else {
                result = getConnErrorMsg(conn);
            }
        } finally {
            conn.disconnect();
        }

        Logger.LOGD(TAG + " getRedirectUrl code:" + code + ",result:" + result);
        respInfo.setHeaderMap(headerMap);
        respInfo.setHttpRespCode(code);
        respInfo.setRespStr(result);
        return respInfo;
    }

    private void writeStringParams(DataOutputStream ds, Map<String, String> bodyParams) {
        if (bodyParams != null) {
            // 首先组拼文本类型的参数
            for (Map.Entry<String, String> entry : bodyParams.entrySet()) {
                Logger.LOGD(TAG + " bodyParams.key=" + entry.getKey() + ",value=" + entry.getValue());
                try {
                    ds.writeBytes(TWO_HYPHENS + BOUNDARY + LINE_END);
                    ds.writeBytes("Content-Disposition: form-data; name=\""
                            + entry.getKey() + "\"" + LINE_END);
                    ds.writeBytes("Content-Type: text/plain; charset=" + CHARSET + LINE_END);
                    ds.writeBytes("Content-Transfer-Encoding: 8bit" + LINE_END);
                    ds.writeBytes(LINE_END);
                    ds.writeBytes(encode(entry.getValue()));
                    ds.writeBytes(LINE_END);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Logger.LOGD(TAG + " write string ok");
    }

    private void writeStringJsonParams(DataOutputStream ds, Map<String, String> bodyParams) {
        if (bodyParams != null) {
            // 首先组拼文本类型的参数
            JSONObject jsonObject = new JSONObject();
            for (Map.Entry<String, String> entry : bodyParams.entrySet()) {
                Logger.LOGD(TAG + " bodyParams.key=" + entry.getKey() + ",value=" + entry.getValue());
                try {
                    jsonObject.put(entry.getKey(), entry.getValue());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            try {
                ds.writeBytes(jsonObject.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        Logger.LOGD(TAG + " write string ok");
    }

    // 文件数据
    private void writeFileParams(DataOutputStream ds, Map<String, String> fileParams) {
        if (fileParams != null) {
            for (Map.Entry<String, String> entry : fileParams.entrySet()) {
                Logger.LOGD(TAG + " bodyParams.key=" + entry.getKey() + ",value=" + entry.getValue());
                try {
                    ds.writeBytes(TWO_HYPHENS + BOUNDARY + LINE_END);

                    ds.writeBytes("Content-Disposition: form-data; "
                            + "name=\"" + entry.getKey() + "\";filename=\"" + encode(entry.getValue()) + "\"" + LINE_END);
                    ds.writeBytes("Content-Type: application/octet-stream; charset="
                            + CHARSET + LINE_END);
                    ds.writeBytes(LINE_END);

                    /* 取得文件的FileInputStream */
                    FileInputStream fStream = new FileInputStream(entry.getValue());
                    /* 设置每次写入1024bytes */
                    int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];
                    int length = -1;
                    /* 从文件读取数据至缓冲区 */
                    while ((length = fStream.read(buffer)) != -1) {
                    /* 将资料写入DataOutputStream中 */
                        ds.write(buffer, 0, length);
                    }
                    ds.writeBytes(LINE_END);
                    fStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @NonNull
    private String getConnOkMsg(HttpURLConnection conn) throws IOException {
        InputStream is = null;
        is = conn.getInputStream();
        int ch;
        StringBuffer b = new StringBuffer();
        if (null == is) {
            Logger.LOGW(TAG + " getConnOkMsg is=" + is);
            return b.toString();
        }
        while ((ch = is.read()) != -1) {
            b.append((char) ch);
        }
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b.toString();
    }

    @NonNull
    private String getConnErrorMsg(HttpURLConnection conn) throws IOException {
        /* 取得Response内容 */
        Logger.LOGD(TAG + " getConnErrorMsg conn=" + conn);
        InputStream is = conn.getErrorStream();
        if (is == null) {
            is = conn.getInputStream();
        }
        int ch;
        StringBuffer b = new StringBuffer();
        if (null == is) {
            Logger.LOGW(TAG + " getConnErrorMsg is=" + is);
            return b.toString();
        }
        while ((ch = is.read()) != -1) {
            b.append((char) ch);
        }
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b.toString();
    }

    // Post connection的一些必须设置.
    private HttpURLConnection initPostConnection(String strUrl) {
        URL url = null;
        try {
            url = new URL(strUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setConnectTimeout(CONN_TIME_OUT);
        conn.setReadTimeout(READ_TIME_OUT);
        try {
            conn.setRequestMethod("POST");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Charset", "UTF-8");
        return conn;
    }

    // Post connection的一些必须设置.
    private HttpURLConnection initPostJsonConnection(String strUrl) {
        URL url = null;
        try {
            url = new URL(strUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setConnectTimeout(CONN_TIME_OUT);
        conn.setReadTimeout(READ_TIME_OUT);
        try {
            conn.setRequestMethod("POST");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Connection", "Keep-Alive");
        return conn;
    }

    // Get connection的一些必须设置.
    private HttpURLConnection initGetConnection(ISARHttpRequest request) {
        // init get http url params;
        initHttpGetUrlParams(request);
        URL url = null;
        try {
            url = new URL(request.getUrl());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        conn.setDoInput(true);
        conn.setUseCaches(true);
        conn.setConnectTimeout(CONN_TIME_OUT);
        conn.setReadTimeout(READ_TIME_OUT);
        try {
            conn.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        conn.setInstanceFollowRedirects(request.isRedirect());
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Charset", "UTF-8");
        // conn.setRequestProperty("Range", "bytes=" + startPos + "-"+ endPos);//设置获取实体数据的范围
        return conn;
    }

    // According to google. GET method will be cached if cache sets.
    private void initGetCacheConnection(HttpURLConnection conn, ISARHttpRequest request) throws IOException {
        boolean useCache = request.isUseCache();

        if (useCache) {
            Logger.LOGD(TAG + " initCacheConnection using cache.");
            conn.addRequestProperty("Cache-Control", "only-if-cached");
            conn.addRequestProperty("Cache-Control", "max-stale=" + MAX_STATLE_TIME);

            HttpResponseCache cacheResp = null;
            CacheResponse cache = null;
            cacheResp = HttpResponseCache.getInstalled();
            if (null != cacheResp) {
                URI uri = null;
                try {
                    uri = new URI(request.getUrl());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

                cache = cacheResp.get(uri, "GET", conn.getRequestProperties());
                Logger.LOGD(TAG + " initCacheConnection cache=" + (cache != null));
                if (null == cache) {
                    request.setUseCache(false);
                    conn.setRequestProperty("Cache-Control", "no-cached");
                    conn.setRequestProperty("Cache-Control", "max-age=0");
                }
            }
        } else {
            conn.addRequestProperty("Cache-Control", "no-cached");
            conn.addRequestProperty("Cache-Control", "max-age=0");
            Logger.LOGD("CacheTest not cache uri=" + request.getUrl());
            /*Map<String, List<String>> map = conn.getHeaderFields();
            Logger.LOGD(TAG + " downloadAnimationInfo 显示响应Header信息...");
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                Logger.LOGD(TAG + " CacheTest not cache Key : " + entry.getKey() +
                        " ,Value : " + entry.getValue());
            }*/
            //conn.addRequestProperty("Cache-Control", "no-cached");
            //conn.addRequestProperty("Cache-Control", "max-age=0");
        }
    }

    private void initConnectionHeader(HttpURLConnection connection, ISARHttpRequest request) {
        Map<String, String> headerParams = request.getHeaderParams();
        if (headerParams != null) {
            for (String key : headerParams.keySet()) {
                Logger.LOGD(TAG + " headerParams.key=" + key + ",value=" + headerParams.get(key));
                connection.setRequestProperty(key, headerParams.get(key));
            }
        }
    }

    private void initHttpGetUrlParams(ISARHttpRequest request) {
        Map<String, String> urlParams = request.getUrlParams();
        if (urlParams != null) {
            int count = 0;
            StringBuilder stringBuilder = new StringBuilder(request.getUrl());
            for (String key : urlParams.keySet()) {
                if (0 == count) {
                    stringBuilder.append("?" + key + "=" + urlParams.get(key));
                } else {
                    try {
                        stringBuilder.append("&" + key + "=" + encode(urlParams.get(key)));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                count++;
            }
            request.setUrl(stringBuilder.toString());
        }
    }

    //添加结尾数据
    private void paramsEnd(DataOutputStream ds) {
        try {
            ds.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + LINE_END);
            ds.writeBytes(LINE_END);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 对包含中文的字符串进行转码，此为UTF-8。服务器那边要进行一次解码
    private String encode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }

    /**
     * 下载文件.
     *
     * @param request
     * @return
     * @throws IOException
     */
    public int downloadFile(ISARHttpRequest request) throws IOException {
        if (null == request || null == request.getUrl()) {
            throw new NullPointerException("ISARHttpRequest is null.");
        }
        int code = 1;
        ISARHttpRequestListener requestListener = request.getHttpRequestListener();
        if (null == requestListener) {
            Logger.LOGW(TAG + " downloadFile request listener is null.");
        }

        HttpURLConnection conn = initGetConnection(request);
        initConnectionHeader(conn, request);
        // 判断是否存在cache
        initGetCacheConnection(conn, request);
        try {
            if (!request.isUseCache()) {
                conn.connect();
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            return code;
        }

        // 得到响应码
        code = conn.getResponseCode();
        try {
            if (HttpURLConnection.HTTP_OK == code) {
                writeDataToFile(conn, request);
            } else {
                String err = getConnErrorMsg(conn);
                Logger.LOGD(TAG + " downloadFile code:" + code + ",err:" + err);
            }
        } finally {
            conn.disconnect();
        }

        return code;
    }

    /**
     * 获取Animation字符串.
     * 此方法暂时不需要传回网络访问状态，如果需要返回网络状态以及content，
     * 则需要实现listener回调。e.g.doPost
     *
     * @param request
     * @return
     */
    public ISARAnimDownloadRespInfo downloadAnimationInfo(ISARHttpRequest request, String themeMd5) throws IOException {
        if (null == request || null == request.getUrl()) {
            throw new NullPointerException("ISARHttpRequest is null.");
        }
        ISARAnimDownloadRespInfo downloadRespInfo = new ISARAnimDownloadRespInfo();
        ISARHttpRequestListener requestListener = request.getHttpRequestListener();
        if (null == requestListener) {
            Logger.LOGW(TAG + " downloadAnimationInfo request listener is null.");
        }

        int code = 1;
        HttpURLConnection conn = initGetConnection(request);
        initConnectionHeader(conn, request);
        // 判断是否存在cache
        initGetCacheConnection(conn, request);
        SharedPreferences sp = request.getSharedPreferences();
        if (null != sp) {
            // using eTag to get animation.
            String eTag = sp.getString(themeMd5, "");
            if (!TextUtils.isEmpty(eTag) && !request.isUseCache()) {
                Logger.LOGW(TAG + " downloadAnimationInfo set eTag=" + eTag);
                conn.setRequestProperty("If-None-Match", eTag);
            }
        }
        Logger.LOGD(TAG + " downloadAnimation url:" + request.getUrl());

        try {
            if (!request.isUseCache()) {
                conn.connect();
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            downloadRespInfo.setHttpRespCode(code);
            downloadRespInfo.setRespStr(getConnErrorMsg(conn));
            return downloadRespInfo;
        } catch (ConnectException e) {
            Logger.LOGE(TAG + " doGet connect error.");
            e.printStackTrace();
            downloadRespInfo.setHttpRespCode(code);
            downloadRespInfo.setRespStr(getConnErrorMsg(conn));
            return downloadRespInfo;
        }

        if (!request.isUseCache()) {
            try {
                // 得到响应码
                code = conn.getResponseCode();
            } catch (IOException e) {
                e.printStackTrace();
                Logger.LOGW(TAG + " downloadAnimationInfo getResponseCode IOException.");
                downloadRespInfo.setHttpRespCode(code);
                downloadRespInfo.setRespStr(getConnErrorMsg(conn));
                return downloadRespInfo;
            }
        }

        if (null != sp) {
            String eTag = conn.getHeaderField("Etag");
            Logger.LOGD(TAG + " downloadAnimationInfo eTag=" + eTag);
            // 如果没有eTag，重置eTag.保证下次获取数据的时候会从服务器重新下载.
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(themeMd5, eTag);
            editor.commit();
        }

        String result;
        try {
            Logger.LOGD(TAG + " downloadAnimationInfo code:" + code + ",request.isUseCache():" + request.isUseCache());
            if (HttpURLConnection.HTTP_OK == code || request.isUseCache()) {
                code = HttpURLConnection.HTTP_OK;
                result = getConnOkMsg(conn);
            } else {
                result = getConnErrorMsg(conn);
            }
        } finally {
            conn.disconnect();
        }
        Logger.LOGD(TAG + " downloadAnimationInfo code:" + code + ",result:" + result);
        downloadRespInfo.setHttpRespCode(code);
        downloadRespInfo.setRespStr(code + ":" + result);

        return downloadRespInfo;
    }

    private void writeDataToFile(HttpURLConnection conn, ISARHttpRequest request) throws IOException {
        //获取连接的输入流，这个输入流就是图片的输入流
        InputStream is = conn.getInputStream();
        //构建一个file对象用于存储图片
        String filePath = request.getTargetPath();
        File file = new File(filePath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int len = 0;
        byte[] buffer = new byte[1024];
        try {
            //将输入流写入到我们定义好的文件中
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //将缓冲刷入文件
            fos.flush();
            fos.close();
            Logger.LOGD(TAG + " writeDataToFile done." + filePath);
        }
    }

    /**
     * 下载视文件，用于视频下载的定制化。
     *
     * @param request
     * @return
     * @throws IOException
     */
    public int downloadFileForVideoPlayer(ISARHttpRequest request) throws IOException {
        if (null == request || null == request.getUrl()) {
            throw new NullPointerException("ISARHttpRequest is null.");
        }
        int code = 1;
        ISARHttpRequestListener requestListener = request.getHttpRequestListener();
        if (null == requestListener) {
            Logger.LOGW(TAG + " downloadFileForVideoPlayer request listener is null.");
        }

        HttpURLConnection conn = initGetConnection(request);
        initConnectionHeader(conn, request);
        // 判断是否存在cache
        initGetCacheConnection(conn, request);
        try {
            if (!request.isUseCache()) {
                conn.connect();
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            return code;
        }

        // 得到响应码
        code = conn.getResponseCode();
        try {
            if (HttpURLConnection.HTTP_OK == code) {
                writeDataToFileForVideoPlayer(conn, request);
            } else {
                String err = getConnErrorMsg(conn);
                Logger.LOGD(TAG + " downloadFileForVideoPlayer code:" + code + ",err:" + err);
            }
        } finally {
            conn.disconnect();
        }

        return code;
    }

    private void writeDataToFileForVideoPlayer(HttpURLConnection conn, ISARHttpRequest request) throws IOException {
        // 获取listener
        ISARRequestListener<ISARDownloadInfo> arRequestListener = request.getARReqListener();
        //获取连接的输入流，这个输入流就是图片的输入流
        InputStream is = conn.getInputStream();
        //构建一个file对象用于存储图片
        String filePath = request.getTargetPath();
        String tmpFilePath;
        if (request.getUrl().endsWith(".m3u8")) {
            String temp = filePath.substring(0, filePath.lastIndexOf("."));
            String format = filePath.substring(filePath.lastIndexOf("."));
            tmpFilePath = temp + System.currentTimeMillis() + "_tmp" + format;
        } else {
            tmpFilePath = filePath + System.currentTimeMillis() + "_tmp";
        }
        File file = new File(tmpFilePath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ISARDownloadInfo ISARDownloadInfo = new ISARDownloadInfo();
        ISARDownloadInfo.setTotalSize(conn.getContentLength());
        int curSize = 0;
        int len = 0;
        byte[] buffer = new byte[1024];
        try {
            //将输入流写入到我们定义好的文件中
            while (!request.getCanceled() && (len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                curSize += len;
                ISARDownloadInfo.setCurrentSize(curSize);
                ISARDownloadInfo.setSizeReadOnce(len);
                Thread.sleep(5);
                arRequestListener.onIdARHttpGetProgress(ISARDownloadInfo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //将缓冲刷入文件
            fos.flush();
            fos.close();
            if (request.getCanceled() && file.exists()) {
                file.delete();
            } else {
                File tarFile = new File(filePath);
                if (!tarFile.exists()) {
                    file.renameTo(tarFile);
                }
            }
            arRequestListener.onIdARHttpGetDone(ISARDownloadInfo);
            Logger.LOGD(TAG + " writeDataToFileForVideoPlayer done." + filePath);
        }
    }

    /**
     * 下载.m3u8文件，用于视频下载的定制化。
     *
     * @param request
     * @return
     * @throws IOException
     */
    public List<String> downloadFileForm3u8(ISARHttpRequest request) throws IOException {
        if (null == request || null == request.getUrl()) {
            throw new NullPointerException("ISARHttpRequest is null.");
        }
        List<String> resultList = new ArrayList<>();
        ISARHttpRequestListener requestListener = request.getHttpRequestListener();
        if (null == requestListener) {
            Logger.LOGW(TAG + " downloadFileForm3u8 request listener is null.");
        }

        HttpURLConnection conn = initGetConnection(request);
        initConnectionHeader(conn, request);
        // 判断是否存在cache
        initGetCacheConnection(conn, request);
        try {
            if (!request.isUseCache()) {
                conn.connect();
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            return resultList;
        }

        // 得到响应码
        int code = conn.getResponseCode();
        try {
            if (HttpURLConnection.HTTP_OK == code) {
                resultList = writeDataToFileForm3u8(conn, request);
            } else {
                String err = getConnErrorMsg(conn);
                Logger.LOGD(TAG + " downloadFileForm3u8 code:" + code + ",err:" + err);
            }
        } finally {
            conn.disconnect();
        }

        return resultList;
    }

    private List<String> writeDataToFileForm3u8(HttpURLConnection conn, ISARHttpRequest request) throws IOException {
        //获取连接的输入流，这个输入流就是图片的输入流
        InputStream is = conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        //构建一个file对象用于存储图片
        String filePath = request.getTargetPath();
        String tmpFilePath;
        if (request.getUrl().endsWith(".m3u8")) {
            String temp = filePath.substring(0, filePath.lastIndexOf("."));
            String format = filePath.substring(filePath.lastIndexOf("."));
            tmpFilePath = temp + System.currentTimeMillis() + "_tmp" + format;
        } else {
            tmpFilePath = filePath + System.currentTimeMillis() + "_tmp";
        }
        File file = new File(tmpFilePath);
        if (file.exists()) {
            file.delete();
        }
        List<String> resultList = new ArrayList<>();
        String fileMd5 = ISARStringUtil.getMD5(request.getUrl());
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int len = 0;
        String line = "";
        byte[] buffer = new byte[1024];
        try {
            //将输入流写入到我们定义好的文件中
            while ((line = reader.readLine()) != null) {
                fos.write(buffer, 0, len);
                Logger.LOGD(TAG + " writeDataToFileForm3u8=[" + line + "]");
                if (line.startsWith("#")) {
                    line = line + "\r\n";
                    fos.write(line.getBytes(), 0, line.getBytes().length);
                } else {
                    // parse video list local path
                    if (line.length() > 0) {
                        String videoName = getLocalM3U8VideoFile(line);
                        resultList.add(videoName);
                        String s = fileMd5 + videoName;
                        //replace 这行的内容
                        line = s + "\r\n";
                        fos.write(line.getBytes(), 0, line.getBytes().length);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //将缓冲刷入文件
            fos.flush();
            fos.close();
            if (request.getCanceled() && file.exists()) {
                file.delete();
            } else {
                File tarFile = new File(filePath);
                if (!tarFile.exists()) {
                    file.renameTo(tarFile);
                }
            }
            Logger.LOGD(TAG + " writeDataToFileForm3u8 done." + filePath);
        }
        return resultList;
    }

    private String getLocalM3U8VideoFile(String line) {
        if (line.startsWith("http://")) {
            int las = line.lastIndexOf("/");
            String result = line.substring(las + 1);
            Logger.LOGD(TAG + " getLocalM3U8VideoFile las=" + las + ", name=" + result);
            return result;
        } else {
            return line;
        }
    }

    private void testResponseHeaders(HttpURLConnection conn) {
        // test header of response.
        /*Map<String, List<String>> map2 = conn.getRequestProperties();
        Logger.LOGD(TAG + " downloadAnimationInfo 显示响应request信息...");
        for (Map.Entry<String, List<String>> entry : map2.entrySet()) {
            Logger.LOGD(TAG + " downloadAnimationInfo Key : " + entry.getKey() +
                    " ,Value : " + entry.getValue());
        }*/
        Map<String, List<String>> map = conn.getHeaderFields();
        Logger.LOGD(TAG + " downloadAnimationInfo 显示响应Header信息...");
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            Logger.LOGD(TAG + " downloadAnimationInfo Key : " + entry.getKey() +
                    " ,Value : " + entry.getValue());
        }

    }

    private void testCache(HttpURLConnection conn, ISARHttpRequest request) throws IOException {
        Logger.LOGD(TAG + " initCacheConnection using cache.");
        conn.addRequestProperty("Cache-Control", "only-if-cached");
        conn.addRequestProperty("Cache-Control", "max-stale=" + 60 * 60);

        HttpResponseCache cacheResp = null;
        CacheResponse cache = null;
        cacheResp = HttpResponseCache.getInstalled();
        if (null != cacheResp) {
            URI uri = null;
            try {
                uri = new URI(request.getUrl());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            cache = cacheResp.get(uri, "POST", conn.getRequestProperties());
            Logger.LOGD(TAG + " initCacheConnection cache=" + (cache != null));
            /*if (null == cache) {
                request.setUseCache(false);
                conn.setRequestProperty("Cache-Control", "no-cached");
                conn.setRequestProperty("Cache-Control", "max-age=0");
            }*/
        }
    }

}
