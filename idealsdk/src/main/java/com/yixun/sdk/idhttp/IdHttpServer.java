package com.yixun.sdk.idhttp;

import android.net.http.HttpResponseCache;
import android.text.TextUtils;

import com.yixun.sdk.util.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.CacheResponse;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class IdHttpServer {
    private static final String TAG = IdHttpServer.class.getSimpleName();
    private static final int CONN_TIME_OUT = 15000;//连接超时为15秒
    private static final int READ_TIME_OUT = 15000;//读取超时为15秒
    private static final int MAX_STALE_TIME = 3600;//1小时
    private static final long CACHE_SIZE = 10485760; //10M
    private static final long RECORD_SIZE = 1048576; //1M
    private static final int DEFAULT_RESPONSE_CODE = -1;
    private static final String CHARSET = "UTF-8";
    private static final String BOUNDARY = "-----boundary";//固定格式
    private static final String LINE_END = "\r\n";
    private static final String TWO_HYPHENS = "--";
    private static final String RECORD_FILE_SUFFIX = ".dp";

    IdHttpServer(File cacheDir) {
        setCachePath(cacheDir);
    }

    IdHttpResponseInfo doFileGet(IdFileGetRequest request) throws IOException {
        if (null == request || null == request.getUrl()) {
            throw new NullPointerException("IdHttpRequest is null");
        }

        Logger.LOGD(TAG + request.toString());

        IdHttpResponseInfo responseInfo = new IdHttpResponseInfo();
        responseInfo.setRequest(request);

        HttpURLConnection conn = initGetConnection(request);
        String filePath = request.getTargetPath();
        File positionFile = new File(filePath + RECORD_FILE_SUFFIX);
        long position = getRecordPosition(positionFile);
        conn.setRequestProperty("RANGE", "bytes=" + position + "-");
        int code;
        try {
            if (!request.isUseCache()) {
                conn.connect();
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            return onErrorConnect(responseInfo, e, request);
        } catch (ConnectException e) {
            e.printStackTrace();
            return onErrorConnect(responseInfo, e, request);
        }

        code = conn.getResponseCode();
        String result;
        if (HttpURLConnection.HTTP_OK == code || HttpURLConnection.HTTP_PARTIAL == code || request.isUseCache()) {
            code = HttpURLConnection.HTTP_OK;
            result = getFileDownload(conn, request, position);
        } else {
            result = getErrorString(conn);
        }
        responseInfo.setHttpRespCode(code);
        responseInfo.setRespString(result);
        conn.disconnect();
        return responseInfo;
    }

    IdHttpResponseInfo doFilePost(IdFilePostRequest request) throws IOException {
        if (null == request || null == request.getUrl()) {
            throw new NullPointerException("IdHttpRequest is null");
        }

        Logger.LOGD(TAG + request.toString());

        IdHttpResponseInfo responseInfo = new IdHttpResponseInfo();
        responseInfo.setRequest(request);

        HttpURLConnection conn = initPostConnection(request);
        int code;
        try {
            conn.connect();
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            return onErrorConnect(responseInfo, e, request);
        } catch (ConnectException e) {
            e.printStackTrace();
            return onErrorConnect(responseInfo, e, request);
        }

        DataOutputStream ds = new DataOutputStream(conn.getOutputStream());
        writeStringParams(ds, request.getBodyParams());
        writeFileParams(ds, request.getFileParams(), request.getListener());
        writeEnd(ds);
        ds.flush();
        ds.close();

        code = conn.getResponseCode();
        String result;
        if (HttpURLConnection.HTTP_OK == code) {
            result = getConnString(conn);
        } else {
            result = getErrorString(conn);
        }
        responseInfo.setHttpRespCode(code);
        responseInfo.setRespString(result);
        conn.disconnect();
        return responseInfo;
    }

    IdHttpResponseInfo doJsonGet(IdRequest request) throws IOException {
        if (null == request || null == request.getUrl()) {
            throw new NullPointerException("IdHttpRequest is null");
        }

        Logger.LOGD(TAG + request.toString());

        IdHttpResponseInfo responseInfo = new IdHttpResponseInfo();
        responseInfo.setRequest(request);

        HttpURLConnection conn = initGetConnection(request);
        int code;
        try {
            if (!request.isUseCache()) {
                conn.connect();
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            return onErrorConnect(responseInfo, e, request);
        } catch (ConnectException e) {
            e.printStackTrace();
            return onErrorConnect(responseInfo, e, request);
        }

        code = conn.getResponseCode();
        String result;
        if (HttpURLConnection.HTTP_OK == code || request.isUseCache()) {
            code = HttpURLConnection.HTTP_OK;
            result = getConnString(conn);
        } else {
            result = getErrorString(conn);
        }
        responseInfo.setHttpRespCode(code);
        responseInfo.setRespString(result);
        conn.disconnect();
        return responseInfo;
    }

    String getRecordFileSuffix() {
        return RECORD_FILE_SUFFIX;
    }

    private HttpURLConnection initGetConnection(IdRequest request) throws IOException {
        request = initHttpUrlParams(request);
        URL url = new URL(convertChineseString(request.getUrl()));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(request.getMethod());
        conn.setDoInput(true);
        conn.setUseCaches(request.isUseCache());
        conn.setConnectTimeout(CONN_TIME_OUT);
        conn.setReadTimeout(READ_TIME_OUT);
        conn.setInstanceFollowRedirects(request.isRedirect());
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Charset", "UTF-8");

        initConnectionHeader(conn, request);
        initConnectionCache(conn, request);
        return conn;
    }

    private HttpURLConnection initPostConnection(IdRequest request) throws IOException {
        request = initHttpUrlParams(request);
        URL url = new URL(convertChineseString(request.getUrl()));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(request.getMethod());
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(true);
        conn.setReadTimeout(READ_TIME_OUT);
        conn.setConnectTimeout(CONN_TIME_OUT);
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Charset", "UTF-8");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

        initConnectionHeader(conn, request);
        return conn;
    }

    private IdRequest initHttpUrlParams(IdRequest request) {
        Map<String, String> urlParams = request.getUrlParams();
        if (null == urlParams) {
            return request;
        }

        int count = 0;
        StringBuilder strBuilder = new StringBuilder(request.getUrl());
        for (String key : urlParams.keySet()) {
            String value = urlParams.get(key);
            if (TextUtils.isEmpty(value)) {
                continue;
            }
            if (count == 0) {
                strBuilder.append("?").append(key).append("=").append(value);
            } else {
                strBuilder.append("&").append(key).append("=").append(value);
            }
            count++;
        }
        request.setUrl(strBuilder.toString());
        return request;
    }

    private void initConnectionCache(HttpURLConnection conn, IdRequest request) throws IOException {
        if (request.isUseCache()) {
            conn.addRequestProperty("Cache-Control", "only-if-cached");
            conn.addRequestProperty("Cache-Control", "max-stale=" + MAX_STALE_TIME);

            HttpResponseCache cacheResp;
            CacheResponse cache;
            cacheResp = HttpResponseCache.getInstalled();
            if (null != cacheResp) {
                URI uri = null;
                try {
                    uri = new URI(request.getUrl());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                cache = cacheResp.get(uri, "GET", conn.getRequestProperties());
                if (null == cache) {
                    request.setUseCache(false);
                    conn.setRequestProperty("Cache-control", "no-cached");
                    conn.setRequestProperty("Cache-control", "max-age=0");
                }
            } else {
                conn.addRequestProperty("Cache-Control", "no-cached");
                conn.addRequestProperty("Cache-control", "max-age=0");
            }
        }
    }

    private String getFileDownload(HttpURLConnection conn, IdFileGetRequest request, long position) {
        IdProgressListener listener = request.getListener();
        String filePath = request.getTargetPath();
        File positionFile = new File(filePath + RECORD_FILE_SUFFIX);
        try {
            InputStream is = conn.getInputStream();
            long fileLength = Long.valueOf(conn.getHeaderField("Content-Length"));
            if (request.getMaxFileLength() < fileLength) {
                listener.onSpaceLimited();
                return TAG + " getFileDownload : space limited";
            }
            File file = new File(request.getTargetPath());
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(position);
            writeRecordPosition(positionFile, position);
            long currentLength = position;
            long recordLength = 0;
            int len;
            byte[] buffer = new byte[1024];
            while ((len = is.read(buffer)) != -1) {
                raf.write(buffer, 0, len);
                currentLength += len;
                recordLength += len;
                if (recordLength > RECORD_SIZE) {
                    writeRecordPosition(positionFile, currentLength);
                    recordLength = 0;
                }
                if (listener != null) {
                    listener.onProgress((int) ((currentLength * 100) / (fileLength + position)));
                }
            }
            if (listener != null) {
                listener.onComplete();
            }
            writeRecordPosition(positionFile, fileLength + position - 1);
            raf.close();
            is.close();
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            return TAG + " getFileDownload :" + e.getMessage() + "\n error url:" + conn.getURL();
        }
    }

    private long getRecordPosition(File recordFile) {
        if (!recordFile.exists()) {
            return 0;
        }
        String positionString = "0";
        try {
            BufferedReader in = new BufferedReader(new FileReader(recordFile));
            positionString = in.readLine();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Long.parseLong(positionString);
    }

    private void writeRecordPosition(File recordFile, long position) {
//        try {
//            Files.write(Long.toString(position), recordFile, Charset.forName(CHARSET));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void writeStringParams(DataOutputStream ds, Map<String, String> bodyParams) {
        if (bodyParams == null) {
            return;
        }
        for (String key : bodyParams.keySet()) {
            String value = bodyParams.get(key);
            if (TextUtils.isEmpty(value)) {
                continue;
            }
            try {
                ds.writeBytes(TWO_HYPHENS + BOUNDARY + LINE_END);
                ds.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + LINE_END);
                ds.writeBytes("Content-Type: text/plain; charset= " + CHARSET + LINE_END);
                ds.writeBytes("Content-Transfer-Encoding: 8bit" + LINE_END);
                ds.writeBytes(LINE_END);
                ds.writeBytes(convertChineseString(value));
                ds.writeBytes(LINE_END);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeFileParams(DataOutputStream ds, Map<String, String> fileParams, IdProgressListener listener) {
        if (fileParams == null) {
            return;
        }
        for (String key : fileParams.keySet()) {
            String value = fileParams.get(key);
            if (TextUtils.isEmpty(value)) {
                continue;
            }
            try {
                ds.writeBytes(TWO_HYPHENS + BOUNDARY + LINE_END);
                ds.writeBytes("Content-Disposition: form-data; name=\"" + key +
                        "\"; filename=\"" + encode(value) + "\"" + LINE_END);
                ds.writeBytes("Content-Type: application/octet-stream; charset=" + CHARSET + LINE_END);
                ds.writeBytes(LINE_END);

                File postFile = new File(value);
                long fileLength = postFile.length();
                long currentLength = 0;
                FileInputStream in = new FileInputStream(postFile);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    ds.write(buffer, 0, len);
                    currentLength += len;
                    if (listener != null) {
                        listener.onProgress((int) ((currentLength * 100) / fileLength));
                    }
                }
                ds.writeBytes(LINE_END);
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
                if (listener != null) {
                    listener.onFailed(TAG + " writeFileParams :" + e.getMessage());
                }
            }
        }
        if (listener != null) {
            listener.onComplete();
        }
    }

    private void writeEnd(DataOutputStream ds) {
        try {
            ds.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + LINE_END);
            ds.writeBytes(LINE_END);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private IdHttpResponseInfo onErrorConnect(IdHttpResponseInfo responseInfo, Exception e,
                                              IdRequest request) {
        responseInfo.setHttpRespCode(DEFAULT_RESPONSE_CODE);
        responseInfo.setRespString(TAG + " onErrorConnect : " + e.toString() + "\n error url:" + request.getUrl());
        return responseInfo;
    }

    private void initConnectionHeader(HttpURLConnection conn, IdRequest request) {
        Map<String, String> headerParams = request.getHeaders();
        if (headerParams == null) {
            return;
        }
        for (String key : headerParams.keySet()) {
            String value = headerParams.get(key);
            if (TextUtils.isEmpty(value)) {
                continue;
            }
            conn.setRequestProperty(key, value);
        }
    }

    private String getConnString(HttpURLConnection conn) {
        InputStream is;
        try {
            is = conn.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return TAG + " getConnString :" + e.getMessage() + "\n error url:" + conn.getURL();
        }
        if (null == is) {
            return "";
        }
        String readString = getStreamString(is);
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return readString;
    }

    private String getErrorString(HttpURLConnection conn) {
        InputStream is = conn.getErrorStream();
        if (null == is) {
            return "";
        }
        String readString = getStreamString(is);
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return TAG + " getErrorSting : " + readString + "\n error url:" + conn.getURL();
    }

    private String getStreamString(InputStream is) {
        int read;
        StringBuilder strBuilder = new StringBuilder();
        try {
            while ((read = is.read()) != -1) {
                strBuilder.append((char) read);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return TAG + " getStreamString :" + e.getMessage();
        }
        return strBuilder.toString();
    }


    private void setCachePath(File cacheDir) {
        if (!cacheDir.isDirectory()) {
            return;
        }
        try {
            HttpResponseCache.install(cacheDir, CACHE_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String convertChineseString(String originString) {
        Pattern p = Pattern.compile("[\\u4e00-\\u9fa5]");
        Matcher m = p.matcher(originString);
        while (m.find()) {
            originString = originString.replace(m.group(), encode(m.group()));
        }
        return originString;
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return value;
        }
    }
}
