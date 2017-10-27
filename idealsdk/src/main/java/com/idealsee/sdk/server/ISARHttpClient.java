package com.idealsee.sdk.server;

import android.content.Context;
import android.net.http.HttpResponseCache;
import android.text.TextUtils;

import com.idealsee.sdk.game.GamePhoneBindResponse;
import com.idealsee.sdk.game.GameTicketInfo;
import com.idealsee.sdk.game.GameTimesInfo;
import com.idealsee.sdk.game.ResouceChangeInfo;
import com.idealsee.sdk.model.ISARAnimDownloadRespInfo;
import com.idealsee.sdk.model.ISARAppInitInfo;
import com.idealsee.sdk.model.ISARHttpResponseInfo;
import com.idealsee.sdk.model.ISARImageMatchedRespInfo;
import com.idealsee.sdk.model.ISARPersonalInfo;
import com.idealsee.sdk.util.ISARBitmapLoader;
import com.idealsee.sdk.util.ISARNetUtil;
import com.idealsee.sdk.util.ISARThreadPool;
import com.idealsee.sdk.util.Logger;
import com.idealsee.ar.unity.ISARUnityMessageManager;
import com.idealsee.sdk.game.GameInitInfo;
import com.idealsee.sdk.game.GamePhoneBindInfo;
import com.idealsee.sdk.game.GameStatusInfo;
import com.idealsee.sdk.model.ISARImageSearchResult;
import com.idealsee.sdk.model.ISARRandomInfo;
import com.idealsee.sdk.util.ISARConstants;
import com.idealsee.sdk.util.ISARFilesUtil;
import com.idealsee.sdk.util.ISARStringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by hongen on 16-12-2.
 * <p>
 * 用于对外提供HTTP访问接口，并处理HTTP返回值。
 */

public class ISARHttpClient {
    private static final String TAG = ISARHttpClient.class.getSimpleName();
    private static ISARHttpClient sIdHttpClient = null;
    private ISARHttpServer mIdHttpServer;
    private ISARHttpRequestQueue mHttpRequestQueue;
    private ISARListener mARListener = null;
    private long mAppStartedTime = 0;
    private Context mContext;

    // 记录最后一次的图片匹配的ISARHttpRequest
    private ISARHttpRequest mLastHttpRequest = null;
    // 记录最后一次成功匹配的ImageSearchResult;
    private ISARImageSearchResult mLastSearchResult = null;

    public interface ISARListener {
        int START_UPLOAD_TO_SERVER = 1;
        int STOP_UPLOAD_TO_SERVER = 2;
        int APP_FLOW_FAILED = -1;
        int APP_FLOW_OK = 0;
        int APP_FLOW_CANCELED = 1;

        void onIdARImageMatch(ISARImageMatchedRespInfo responseInfo);

        void onIdMatchStatusUpdate(int status);

        void onIdDownloadAnimation(ISARAnimDownloadRespInfo animDownloadRespInfo);
    }


    private ISARHttpClient() {
        mIdHttpServer = new ISARHttpServer();
        mHttpRequestQueue = new ISARHttpRequestQueue();
    }

    public synchronized static ISARHttpClient getInstance() {
        if (null == sIdHttpClient) {
            sIdHttpClient = new ISARHttpClient();
        }
        return sIdHttpClient;
    }

    /**
     * 设置cache路径和大小.
     *
     * @param context
     */
    public void setCachePath(Context context) {
        try {
            mContext = context;
            File httpCacheDir = new File(context.getExternalCacheDir(), "http");
            long httpCacheSize = 10 * 1024 * 1024; // 10M
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
            Logger.LOGI(TAG + " setCachePath :" + httpCacheDir.getAbsolutePath());
        } catch (IOException e) {
            Logger.LOGW(TAG + " setCachePath failed.");
            e.printStackTrace();
        }
    }

    /**
     * 设置cache写入文件系统.
     */
    public void flushCache() {
        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (null != cache) {
            cache.flush();
        }
    }

    public void setARListener(ISARListener listener) {
        this.mARListener = listener;
    }

    /**
     * 与服务器进行图片匹配.
     *
     * @return
     */
    public int doSingleImageSearch(final ISARHttpRequest httpRequest) {
        // httpRequest.setRequestListener(mImageMatchHttpReqListener);
        synchronized (this) {
            mHttpRequestQueue.queue(httpRequest);
            mARListener.onIdMatchStatusUpdate(ISARListener.START_UPLOAD_TO_SERVER);
            Logger.LOGD(TAG + " doSingleImageSearch count=" + mHttpRequestQueue.size());
        }
        ISARThreadPool.INSTANCE.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (httpRequest.isLocalRecognize()) {
                        httpRequest.setUseCache(true);
                    }
                    ISARHttpResponseInfo responseInfo = mIdHttpServer.doPost(httpRequest);
                    Logger.LOGD(TAG + responseInfo.toString());
                    if (httpRequest.isLocalRecognize()) {
                        httpRequest.setUseCache(true);
                        File file = new File(ISARConstants.APP_CACHE_DIRECTORY + File.separator + httpRequest.getThemePicMd5() + "_sis.txt");
                        responseInfo.setRespStr(getAnimationCache(file));
                    }
                    if (ISARHttpRequestQueue.ISARCommand.START_AR_SEARCH == responseInfo.getRequest().getARCommand()) {
                        ISARImageMatchedRespInfo matchedRespInfo = new ISARImageMatchedRespInfo(responseInfo);
                        handleImageMatchResult(matchedRespInfo);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    ISARImageMatchedRespInfo matchedRespInfo = new ISARImageMatchedRespInfo();
                    matchedRespInfo.setFlowRespCode(ISARListener.APP_FLOW_FAILED);
                    mARListener.onIdARImageMatch(matchedRespInfo);
                }
            }
        });
        return 0;
    }

    /**
     * 与服务器进行图片匹配,发现页面点击主题.
     * 不需要实际与服务器匹配，只需要传入md5.
     *
     * @return
     */
    public int doDiscoverSingleImageSearch(final ISARHttpRequest httpRequest, final String themeMd5) {
        // httpRequest.setRequestListener(mImageMatchHttpReqListener);
        synchronized (this) {
            int count = 0;
            for (ISARHttpRequest request : mHttpRequestQueue) {
                if (ISARHttpRequestQueue.ISARCommand.START_AR_SEARCH == request.getARCommand()) {
                    request.setCanceled(true);
                    boolean result = mHttpRequestQueue.remove(request);
                    count += result ? 1 : 0;
                }
            }
            Logger.LOGD(TAG + " doDiscoverSingleImageSearch count=" + count);
            mHttpRequestQueue.queue(httpRequest);
            mLastHttpRequest = httpRequest;
            mLastSearchResult = new ISARImageSearchResult();
            mLastSearchResult.md5 = themeMd5;
            mARListener.onIdMatchStatusUpdate(ISARListener.START_UPLOAD_TO_SERVER);
        }
        return 0;
    }

    /**
     * 停止服务器进行图片匹配.
     *
     * @return
     */
    public int doStopSingleImageSearch() {
        synchronized (this) {
            // httpRequest.setCanceled(true);
            int count = 0;
            for (ISARHttpRequest request : mHttpRequestQueue) {
                if (ISARHttpRequestQueue.ISARCommand.START_AR_SEARCH == request.getARCommand()) {
                    request.setCanceled(true);
                    boolean result = mHttpRequestQueue.remove(request);
                    count += result ? 1 : 0;
                }
            }
            mARListener.onIdMatchStatusUpdate(ISARListener.STOP_UPLOAD_TO_SERVER);
            Logger.LOGD(TAG + " doStopSingleImageSearch count=" + count);
        }
        return 0;
    }

    /**
     * 生成unity所需要格式.
     *
     * @param themePicMd5
     * @param raww
     * @param rawh
     * @return
     */
    public String generateLoadPath(String themePicMd5, int raww, int rawh) {
        String tmp = raww + "&" + rawh + "&" + themePicMd5;
        return tmp;
    }

    /**
     * 下载animation json, send it to unity.
     */
    public void downloadAnimation() {
        final String themeMd5 = mLastSearchResult.getMd5();
        Logger.LOGD(TAG + " downloadAnimation targetName:" + themeMd5);
        if (TextUtils.isEmpty(themeMd5)) {
            Logger.LOGW(TAG + " downloadAnimation themeMd5 is empty.");
            downloadAnimationError(ISARListener.APP_FLOW_FAILED);
            return;
        }
        ISARThreadPool.INSTANCE.execute(new Runnable() {
            @Override
            public void run() {
                Logger.LOGD(TAG + " downloadAnimation start load anim");
                try {
                    String animationString = downloadAnimationString(themeMd5);
                    if (!TextUtils.isEmpty(animationString) &&
                            animationString.startsWith(HttpURLConnection.HTTP_OK + ":")) {
                        loadAnimationString(animationString);
                    } else {
                        checkAnimationError();
                    }
                } catch (IOException e) {
                    Logger.LOGW(TAG + " downloadAnimation IOException.");
                    e.printStackTrace();
                    downloadAnimationError(ISARListener.APP_FLOW_FAILED);
                } catch (JSONException e) {
                    e.printStackTrace();
                    downloadAnimationError(ISARListener.APP_FLOW_FAILED);
                }
            }
        });
    }

    private String downloadAnimationString(String themeMd5) throws IOException {
        String animationString;
        ISARHttpRequest lastHttpRequest = mLastHttpRequest;
        ISARAnimDownloadRespInfo downloadRespInfo;
        File file = new File(ISARConstants.APP_CACHE_DIRECTORY + File.separator + themeMd5 + "_anim.txt");
        String animationUrl = ISARHttpServerURL.getAnimationUrl() + "/" + themeMd5;
        ISARHttpRequest animationRequest = new ISARHttpRequest(animationUrl);
        animationRequest.setSharedPreferences(lastHttpRequest.getSharedPreferences());
        animationRequest.setHeaderParams(lastHttpRequest.getHeaderParams());
        if (lastHttpRequest.isLocalRecognize() && !ISARNetUtil.isNetworkConnected(mContext)) {
            animationRequest.setUseCache(true);
            animationString = getAnimationCache(file);
        } else {
            downloadRespInfo = mIdHttpServer.downloadAnimationInfo(animationRequest, themeMd5);
            // if not modified, should read cache data.
            if (HttpURLConnection.HTTP_NOT_MODIFIED == downloadRespInfo.getHttpRespCode()) {
                animationRequest.setUseCache(true);
                animationString = getAnimationCache(file);
            } else {
                animationString = downloadRespInfo.getRespStr();
                updateAnimationCache(file, animationString);
            }
        }
        return animationString;
    }

    private void loadAnimationString(String animationString) throws JSONException {
        animationString = animationString.substring(animationString.indexOf(":") + 1);
        ISARRandomInfo info = new ISARRandomInfo(new JSONObject(animationString));
        String imageUrl = ISARNetUtil.getUrlFromMD5(info.getThemePicMd5(), 400);
        ISARBitmapLoader.getInstance().loadBitmapByUrlOnHttp(mContext, imageUrl);
        String imagePath = ISARConstants.APP_CACHE_DIRECTORY + File.separator + ISARStringUtil.getMD5(imageUrl);
        String sharePicPath = "";
        //share picture path
        if (!TextUtils.isEmpty(info.getSharePicMd5())) {
            String shareUrl = ISARNetUtil.getUrlFromMD5(info.getSharePicMd5(), 400);
            ISARBitmapLoader.getInstance().loadBitmapByUrlOnHttp(mContext, shareUrl);
            sharePicPath = ISARConstants.APP_CACHE_DIRECTORY + File.separator + ISARStringUtil.getMD5(shareUrl);
        }

        if (info.mFloatScreenUrl != null) {
            for (String imageMd5 : info.mFloatScreenUrl) {
                if (!TextUtils.isEmpty(imageMd5)) {
                    String floatIvUrl = ISARNetUtil.getUrlFromMD5(imageMd5);
                    ISARBitmapLoader.getInstance().loadBitmapByUrlOnHttp(mContext, floatIvUrl);
                }
            }
        }

        ISARHttpRequest lastHttpRequest = mLastHttpRequest;
        boolean isCanceled;
        synchronized (this) {
            isCanceled = !mHttpRequestQueue.contains(lastHttpRequest);
            mHttpRequestQueue.remove(lastHttpRequest);
        }
        if (isCanceled) {
            downloadAnimationError(ISARListener.APP_FLOW_CANCELED);
            return;
        }

        info.themePicPath = imagePath;
        info.sharePicPath = sharePicPath;
        Logger.LOGD(TAG + " downloadAnimationInfo result=true onIdealGetAnimation info=" + info.toString());
        ISARAnimDownloadRespInfo downloadRespInfo = new ISARAnimDownloadRespInfo();
        downloadRespInfo.setFlowRespCode(ISARListener.APP_FLOW_OK);
        downloadRespInfo.setRandomInfo(info);
        mARListener.onIdDownloadAnimation(downloadRespInfo);
        ISARUnityMessageManager.loadThemeData(animationString);
    }

    private void checkAnimationError() {
        ISARHttpRequest lastHttpRequest = mLastHttpRequest;
        boolean isCanceled;
        synchronized (this) {
            isCanceled = !mHttpRequestQueue.contains(lastHttpRequest);
            mHttpRequestQueue.remove(lastHttpRequest);
        }
        if (isCanceled) {
            downloadAnimationError(ISARListener.APP_FLOW_CANCELED);
        } else {
            downloadAnimationError(ISARListener.APP_FLOW_FAILED);
        }
    }

    private void downloadAnimationError(int errorName) {
        ISARAnimDownloadRespInfo downloadRespInfo = new ISARAnimDownloadRespInfo();
        downloadRespInfo.setFlowRespCode(errorName);
        mARListener.onIdDownloadAnimation(downloadRespInfo);
    }

    private void handleImageMatchResult(ISARImageMatchedRespInfo responseInfo) {
        boolean notCanceled;
        ISARHttpRequest request = responseInfo.getRequest();
        synchronized (this) {
            notCanceled = mHttpRequestQueue.contains(request);
            // mHttpRequestQueue.remove(request);
            Logger.LOGI(TAG + " handleImageMatchResult notCanceled=" + notCanceled);

        }
        try {
            if (!notCanceled) {
                responseInfo.setFlowRespCode(ISARListener.APP_FLOW_CANCELED);
                mARListener.onIdARImageMatch(responseInfo);
                Logger.LOGW(TAG + " handleImageMatchResult canceled.");
                return;
            }
            if (HttpURLConnection.HTTP_OK == responseInfo.getHttpRespCode()) {
                ISARImageSearchResult imageSearchResult = new ISARImageSearchResult(new JSONObject(responseInfo.getRespStr()));
                mLastSearchResult = imageSearchResult;
                mLastHttpRequest = request;
                responseInfo.setImageSearchResult(imageSearchResult);
                responseInfo.setFlowRespCode(ISARListener.APP_FLOW_OK);
                File file = new File(ISARConstants.APP_CACHE_DIRECTORY + File.separator + imageSearchResult.getMd5() + "_sis.txt");
                updateAnimationCache(file, responseInfo.getRespStr());
                mARListener.onIdARImageMatch(responseInfo);
                downloadThemeTemplate(imageSearchResult, request);
            } else {
                responseInfo.setFlowRespCode(ISARListener.APP_FLOW_FAILED);
                mARListener.onIdARImageMatch(responseInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            responseInfo.setFlowRespCode(ISARListener.APP_FLOW_FAILED);
            mARListener.onIdARImageMatch(responseInfo);
        }
    }

    private void downloadThemeTemplate(ISARImageSearchResult imageSearchResult, ISARHttpRequest request) {
        if (!imageSearchResult.isAr) {
            return;
        }
        String themeMd5 = imageSearchResult.getMd5();
        String tmp = generateLoadPath(themeMd5, imageSearchResult.width, imageSearchResult.height);
        //初始化Theme
        ISARUnityMessageManager.startThemeFromSearch(tmp);
        String name = ISARFilesUtil.getFileNameFromPath(imageSearchResult.getTemplateZipSrc(), false);
        String zipPath = ISARConstants.APP_TRAC_DIRETCORY + File.separator + name + ".zip";
        String datPath = ISARConstants.APP_TRAC_DIRETCORY + File.separator + themeMd5 + ".dat";
        File zipFile = new File(zipPath);
        File datFile = new File(datPath);
        if (datFile.exists() && !request.getCanceled()) {
            try {
                //需要等待场景初始化完成
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ISARUnityMessageManager.loadTemplate(datPath);
            //开启模板叠加
            ISARUnityMessageManager.startAR();
            return;
        }

        if (zipFile.exists()) {
            zipFile.delete();
        }

        ISARHttpRequest dRequest = new ISARHttpRequest(imageSearchResult.getTemplateZipSrc());
        dRequest.setTargetPath(zipPath);
        try {
            mIdHttpServer.downloadFile(dRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (zipFile.exists() && zipFile.length() < 5000) {
            zipFile.delete();
        }

        if (!zipFile.exists()) {
            ISARUnityMessageManager.stopARTheme(0);
            return;
        }

        try {
            ISARFilesUtil.UnZipFolder(zipPath, zipFile.getParent());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (datFile.exists() && !request.getCanceled()) {
            ISARUnityMessageManager.loadTemplate(datPath);
            //开启模板叠加
            ISARUnityMessageManager.startAR();
        } else {
            ISARUnityMessageManager.stopARTheme(0);
        }
    }

    private void updateAnimationCache(File file, String json) {
        if (!TextUtils.isEmpty(json)) {
            // 存文件
            if (file.exists()) {
                file.delete();
            }
            FileWriter writer = null;
            try {
                writer = new FileWriter(file, true); // 若文件不存在, 会立即生成文件
                writer.write(json);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Logger.LOGD(TAG + " updateAnimationCache save file " + file.getAbsolutePath());
        }
    }

    private String getAnimationCache(File file) {
        String result = "";
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] buf = new byte[8 * 1024];
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            int len = 0;
            while ((len = fis.read(buf)) != -1) {
                stream.write(buf, 0, len);
            }
            fis.close();
            result = stream.toString();
            Logger.LOGD(TAG + " history getAnimationCache:" + result);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 下载文件。
     *
     * @param request
     * @return
     * @throws IOException
     */
    public int downloadFile(ISARHttpRequest request) throws IOException {
        return mIdHttpServer.downloadFile(request);
    }

    /**
     * 下载视频文件，针对VideoPlayerHelper做定制。
     *
     * @param request
     * @return
     * @throws IOException
     */
    public int downloadFileForVideoPlayer(ISARHttpRequest request) throws IOException {
        mIdHttpServer.downloadFileForVideoPlayer(request);
        return 0;
    }

    /**
     * 下载.m3u8文件，用于视频下载的定制化。
     *
     * @param request
     * @return
     * @throws IOException
     */
    public List<String> downloadFileForm3u8(ISARHttpRequest request) throws IOException {
        return mIdHttpServer.downloadFileForm3u8(request);
    }

    /**
     * 获取视频文件的文件地址。
     *
     * @param request
     * @return
     * @throws IOException
     */
    public ISARHttpResponseInfo getThemeVideoUrl(ISARHttpRequest request) throws IOException {
        return mIdHttpServer.doGet(request);
    }

    /**
     * 获取HTTP 302跳转的目标地址。
     *
     * @param request
     * @return
     * @throws IOException
     */
    public String getRedirectUrl(ISARHttpRequest request) throws IOException {
        String url = request.getUrl();
        ISARHttpResponseInfo responseInfo = mIdHttpServer.getRedirectUrl(request);
        Map<String, List<String>> header = responseInfo.getHeaderMap();
        if (null == header) {
            return url;
        }
        for (Map.Entry<String, List<String>> entry : header.entrySet()) {
            Logger.LOGD(TAG + " getRedirectUrl Key : " + entry.getKey() +
                    " ,Value : " + entry.getValue());
            if ("Location".equals(entry.getKey())) {
                Logger.LOGI(TAG + " getRedirectUrl found location.");
                url = entry.getValue().get(0);
            }
        }
        return url;
        /*for (Map.Entry<String, List<String>> entry : header.entrySet()) {
            Logger.LOGD(TAG + " downloadAnimationInfo Key : " + entry.getKey() +
                    " ,Value : " + entry.getValue());
        }*/
    }

    /**
     * 与服务器进行KEY匹配.只有当服务器有正确返回时，才取服务器数据，否则返回成功.
     *
     * @param request
     * @return
     * @throws IOException
     */
    public int checkAppKeyStatus(ISARHttpRequest request) throws IOException {
        int status = -1;
        ISARHttpResponseInfo responseInfo = mIdHttpServer.doPost(request);
        if (HttpURLConnection.HTTP_OK == responseInfo.getHttpRespCode()) {
            JSONObject obj = null;
            try {
                obj = new JSONObject(responseInfo.getRespStr());
                status = obj.getInt("code");
                Logger.LOGD(TAG + " checkAppKeyStatus status=" + status);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (responseInfo.getHttpRespCode() == 203) {
            JSONObject obj = null;
            try {
                obj = new JSONObject(responseInfo.getRespStr());
                status = obj.getInt("code");
                Logger.LOGD(TAG + " checkAppKeyStatus status=" + status);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return status;
    }

    /**
     * 与服务器进行KEY匹配.只有当服务器有正确返回时，才取服务器数据，否则返回成功.
     *
     * @param request
     * @return
     * @throws IOException
     */
    public ISARAppInitInfo doAppInit(ISARHttpRequest request) throws IOException {
        ISARHttpResponseInfo responseInfo = mIdHttpServer.doGet(request);
        if (HttpURLConnection.HTTP_OK == responseInfo.getHttpRespCode()) {
            JSONObject obj = null;
            try {
                obj = new JSONObject(responseInfo.getRespStr());
                return new ISARAppInitInfo(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 得到相似主题的H5链接.
     *
     * @param request
     * @return
     * @throws IOException
     */
    public String getSimilarTheme(ISARHttpRequest request) throws IOException {
        ISARHttpResponseInfo responseInfo = mIdHttpServer.doGet(request);
        String similarAR = responseInfo.getRespStr();
        Logger.LOGD(TAG + " getSimilarTheme:" + similarAR);
        return similarAR;
    }

    /**
     * 得到发现列表的RandomInfo列表.
     *
     * @param request
     * @return
     * @throws IOException
     */
    public List<ISARRandomInfo> getRandomInfoList(ISARHttpRequest request) throws IOException {
        if (null == request) {
            return null;
        }
        List<ISARRandomInfo> randomList = null;
        File file = new File(ISARConstants.APP_LAST_RANDOM_CACHE);
        if (request.isUseCache()) {
            String json = getAnimationCache(file);
            if (!TextUtils.isEmpty(json)) {
                try {
                    JSONArray retArray = new JSONArray(json);
                    int size = retArray.length();
                    randomList = new ArrayList<ISARRandomInfo>(size);
                    for (int i = 0; i < size; i++) {
                        Logger.LOGD(TAG + " getRandomInfoList history json:" + retArray.getJSONObject(i));
                        randomList.add(new ISARRandomInfo(retArray.getJSONObject(i)));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            ISARHttpResponseInfo responseInfo = mIdHttpServer.doGet(request);
            if (HttpURLConnection.HTTP_OK == responseInfo.getHttpRespCode()) {
                try {
                    String json = responseInfo.getRespStr();
                    updateAnimationCache(file, json);
                    JSONArray retArray = new JSONArray(json);
                    int size = retArray.length();
                    randomList = new ArrayList<ISARRandomInfo>(size);
                    for (int i = 0; i < size; i++) {
                        Logger.LOGD(TAG + " getRandomInfoList json:" + retArray.getJSONObject(i));
                        randomList.add(new ISARRandomInfo(retArray.getJSONObject(i)));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return randomList;
    }

    /**
     * 得到个人主页列表.
     *
     * @param request
     * @return
     * @throws IOException
     */
    public List<Object> getPersonalInfoList(ISARHttpRequest request) throws IOException {
        if (null == request) {
            return null;
        }
        List<Object> list = null;
        File file = new File(ISARConstants.APP_CACHE_DIRECTORY + File.separator + request.getBodyParams().get("editor_id"));
        if (request.isUseCache()) {
            String json = getAnimationCache(file);
            try {
                JSONObject obj = new JSONObject(json);
                JSONArray retArray = obj.getJSONArray("resource_list");
                int size = retArray.length();
                List<ISARRandomInfo> randomList = new ArrayList<ISARRandomInfo>(size);
                for (int i = 0; i < size; i++) {
                    randomList.add(new ISARRandomInfo(retArray.getJSONObject(i)));
                }
                // get personal info
                ISARPersonalInfo personInfo = new ISARPersonalInfo(obj);
                list = new ArrayList<Object>(2);
                list.add(personInfo);
                list.add(randomList);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            ISARHttpResponseInfo responseInfo = mIdHttpServer.doPost(request);
            if (HttpURLConnection.HTTP_OK == responseInfo.getHttpRespCode()) {
                try {
                    String json = responseInfo.getRespStr();
                    JSONObject obj = new JSONObject(json);
                    JSONArray retArray = obj.getJSONArray("resource_list");
                    int size = retArray.length();
                    List<ISARRandomInfo> randomList = new ArrayList<ISARRandomInfo>(size);
                    for (int i = 0; i < size; i++) {
                        randomList.add(new ISARRandomInfo(retArray.getJSONObject(i)));
                    }
                    // get personal info
                    ISARPersonalInfo personInfo = new ISARPersonalInfo(obj);
                    list = new ArrayList<Object>(2);
                    list.add(personInfo);
                    list.add(randomList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    //***********game start************************

    public GameInitInfo getGameInitInfo(ISARHttpRequest request) throws IOException {
        if (null == request) {
            return null;
        }
        ISARHttpResponseInfo responseInfo = mIdHttpServer.doGet(request);
        if (HttpURLConnection.HTTP_OK == responseInfo.getHttpRespCode()) {
            try {
                String json = responseInfo.getRespStr();
                return new GameInitInfo(new JSONObject(json));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public GameTicketInfo getGameTicketInfo(ISARHttpRequest request) throws IOException {
        if (null == request) {
            return null;
        }
        ISARHttpResponseInfo responseInfo = mIdHttpServer.doGet(request);
        if (HttpURLConnection.HTTP_OK == responseInfo.getHttpRespCode()) {
            try {
                String json = responseInfo.getRespStr();
                return new GameTicketInfo(new JSONObject(json));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public GameStatusInfo getGameStatus(ISARHttpRequest request) throws IOException {
        if (null == request) {
            return null;
        }
        ISARHttpResponseInfo responseInfo = mIdHttpServer.doGet(request);
        if (HttpURLConnection.HTTP_OK == responseInfo.getHttpRespCode()) {
            try {
                String json = responseInfo.getRespStr();
                return new GameStatusInfo(new JSONObject(json));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new GameStatusInfo(new JSONObject());
    }

    public GameTimesInfo getGameTimes(ISARHttpRequest request) throws IOException {
        if (null == request) {
            return null;
        }
        ISARHttpResponseInfo responseInfo = mIdHttpServer.doGet(request);
        if (HttpURLConnection.HTTP_OK == responseInfo.getHttpRespCode()) {
            try {
                String json = responseInfo.getRespStr();
                return new GameTimesInfo(new JSONObject(json));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new GameTimesInfo(new JSONObject());
    }

    public GamePhoneBindInfo getGamePhoneBindInfo(ISARHttpRequest request) throws IOException {
        if (null == request) {
            return null;
        }
        ISARHttpResponseInfo responseInfo = mIdHttpServer.doGet(request);
        if (HttpURLConnection.HTTP_OK == responseInfo.getHttpRespCode()) {
            try {
                String json = responseInfo.getRespStr();
                return new GamePhoneBindInfo(new JSONObject(json));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new GamePhoneBindInfo(new JSONObject());
    }

    public GamePhoneBindResponse postGamePhoneBind(ISARHttpRequest request) throws IOException {
        if (null == request) {
            return null;
        }
        ISARHttpResponseInfo responseInfo = mIdHttpServer.doPost(request);
        if (HttpURLConnection.HTTP_OK == responseInfo.getHttpRespCode()) {
            try {
                String json = responseInfo.getRespStr();
                return new GamePhoneBindResponse(new JSONObject(json));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new GamePhoneBindResponse(new JSONObject());
    }

    public int postGameInfo(ISARHttpRequest request) throws IOException {
        int code = 0;
        if (null == request) {
            return code;
        }
        ISARHttpResponseInfo responseInfo = mIdHttpServer.doPost(request);
        if (HttpURLConnection.HTTP_OK == responseInfo.getHttpRespCode()) {
            return responseInfo.getHttpRespCode();
        }
        return code;
    }

    public ResouceChangeInfo postResouceInfo(ISARHttpRequest request) throws IOException {
        if (null == request) {
            return null;
        }
        ISARHttpResponseInfo responseInfo = mIdHttpServer.doPostJson(request);
        if (HttpURLConnection.HTTP_OK == responseInfo.getHttpRespCode()) {
            try {
                String json = responseInfo.getRespStr();
                JSONObject jsonObject = new JSONObject(json);
                if (jsonObject.has("status")) {
                    if (jsonObject.getString("status").equals("suc"))
                        return new ResouceChangeInfo(new JSONObject(json));
                    else
                        return null;
                }
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public int postUploadUserLog(ISARHttpRequest request) throws IOException {
        int code = 0;
        if (null == request) {
            return code;
        }
        ISARHttpResponseInfo responseInfo = mIdHttpServer.doPost(request);
        if (HttpURLConnection.HTTP_OK == responseInfo.getHttpRespCode()) {
            try {
                String json = responseInfo.getRespStr();
                JSONObject jsonObject = new JSONObject(json);
                if (jsonObject.has("status")) {
                    if (jsonObject.getString("status").equals("suc"))
                        return HttpURLConnection.HTTP_OK;
                    else
                        return code;
                }
                return code;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return code;
    }
}
