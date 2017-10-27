package com.idealsee.sdk.media;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.idealsee.sdk.model.ISARDownloadInfo;
import com.idealsee.sdk.model.ISARHttpResponseInfo;
import com.idealsee.sdk.server.ISARHttpClient;
import com.idealsee.sdk.server.ISARHttpRequest;
import com.idealsee.sdk.util.Logger;
import com.idealsee.sdk.server.ISARHttpServerURL;
import com.idealsee.sdk.server.ISARRequestListener;
import com.idealsee.sdk.util.ISARStringUtil;

public class ISARDownloadThread extends Thread {
    private static final String TAG = "DownloadThread";
    private static final int READY_BUFF = 500 * 1024;// 500-6;1000-16
    private static final int CACHED_BUFF = 100 * 1024;
    private DownloadUpdateListener mDownloadListener;

    private int mCachedSize;
    private int mLastCachedSize;
    // if download file is ready
    private boolean mIsReady = false;
    // if need stop download thread.
    private boolean mIsNeedStop = false;
    private boolean mIsHls = false;
    private boolean mVideoFloat = false;
    private boolean mIsLastVideo = false;
    
    private String mFileUrl;
//    private String mCachedTmpPath;
    private String mTargetPath;
    private String mFileMD5;
    private ISARHttpRequest mHttpRequest;

    public interface DownloadUpdateListener {
        public void onDownloadRady(int totalSize, int cachedSize, String cachedPath);
        public void onDownloadUpdate(int cachedSize);
        public void onDownloadEnd(String cachedPath);
        public void onDownloadError();
    }

    private ISARRequestListener<ISARDownloadInfo> mRequestListener = new ISARRequestListener<ISARDownloadInfo>() {
        @Override
        public void onIdARHttpGetDone(ISARDownloadInfo downloadInfo) {
            Logger.LOGD(TAG + " onIdARHttpGetDone");
            // for file length < CACHED_BUFF
            if (!mIsReady) {
                if (mIsHls && mIsLastVideo && !mIsNeedStop) {
                    Logger.LOGD(TAG + " onIdARHttpGetDone onDownloadReady mIsReady=" + mIsReady);
                    mIsReady = true;
                    mDownloadListener.onDownloadRady(downloadInfo.getTotalSize(), downloadInfo.getCurrentSize(), mFileUrl);
                } else {
                    if (!mIsNeedStop) {
                        Logger.LOGD(TAG + " onIdARHttpGetDone onDownloadReady mIsReady=" + mIsReady);
                        mIsReady = true;
                        mDownloadListener.onDownloadRady(downloadInfo.getTotalSize(), downloadInfo.getCurrentSize(), mFileUrl);
                    }
                }
            } else {
                if (mIsHls && mIsLastVideo && !mIsNeedStop) {
                    mDownloadListener.onDownloadEnd(mTargetPath);
                } else {
                    if (!mIsNeedStop) {
                        mDownloadListener.onDownloadEnd(mTargetPath);
                    }
                }
            }
        }

        @Override
        public void onIdARHttpGetProgress(ISARDownloadInfo downloadInfo) {
            Logger.LOGD(TAG + " onIdARHttpGetProgress:" + downloadInfo.getCurrentSize());
            if (!mIsReady && !mIsNeedStop) {
                mCachedSize += downloadInfo.getSizeReadOnce();
                if ((mCachedSize - mLastCachedSize) > READY_BUFF && !mIsNeedStop) {
                    Logger.LOGD(TAG + " onIdARHttpGetProgress onDownloadReady mIsReady=" + mIsReady);
                    mIsReady = true;
                    mLastCachedSize = mCachedSize;
                    mDownloadListener.onDownloadRady(downloadInfo.getTotalSize(), mCachedSize, mFileUrl);
                }
            }
        }
    };

    /**
     * Constructor.
     * @param targetPath target path
     * @param listener listener
     */
    public ISARDownloadThread(Context context, String fileUrl, String targetPath, DownloadUpdateListener listener) {
        Logger.LOGD(TAG + " DownloadThread fileUrl=" + fileUrl);
        mFileUrl = fileUrl;
        mTargetPath = targetPath;
        mFileMD5 = ISARStringUtil.getMD5(fileUrl);
        mDownloadListener = listener;
        if (fileUrl.endsWith(".m3u8")) {
            mIsHls = true;
            /*String temp = targetPath.substring(0, targetPath.lastIndexOf("."));
            String format = targetPath.substring(targetPath.lastIndexOf("."));
            mCachedTmpPath = temp + System.currentTimeMillis() + "_tmp" +  format;*/
        } else {
//            mCachedTmpPath = targetPath + System.currentTimeMillis() + "_tmp";
        }
        /*if (!mIsHls && !fileUrl.endsWith(".mp4")) {
            String videoUrl = NetUtil.doGetThemeVideoUrl(fileUrl);
            if (fileUrl.startsWith("http")
                    && (fileUrl.contains("youku") || fileUrl.contains("tc.qq.com") || fileUrl.contains("tudou"))) {
                if (videoUrl != null) {
                    Logger.LOGD(TAG + " load video url=" + videoUrl);
                    fileUrl = getRealVideoPath(videoUrl);
                } else {
                    Logger.LOGE(TAG + " load video url fail=" + videoUrl);
                }
            }
        }
        try {
            mUrl = new URL(fileUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void run() {
        try {
            mHttpRequest = new ISARHttpRequest();
            if (!mIsHls && !mFileUrl.endsWith(".mp4")) {
                Logger.LOGD(TAG + " run mVideoFloat=" + mVideoFloat);
                Logger.LOGD(TAG + " run mVideoFloat=" + mVideoFloat);
                    /*
                     * TODO v4后，float button video url changed
                     * if (mFileUrl.startsWith("http")
                            && (mFileUrl.contains("youku") || mFileUrl.contains("tc.qq.com") || mFileUrl
                                    .contains("tudou"))) {
                        mFileUrl = getRealVideoPath(mFileUrl);
                    }*/
                mHttpRequest.setUrl(ISARHttpServerURL.getThemeVideoUrl());
                Map<String, String> urlParams = new HashMap<>(1);
                urlParams.put("key", mFileUrl);
                mHttpRequest.setUrlParams(urlParams);
                mHttpRequest.setUseCache(true);
                // String videoUrl = NetUtil.doGetThemeVideoUrl(mFileUrl);
                ISARHttpResponseInfo responseInfo = ISARHttpClient.getInstance().getThemeVideoUrl(mHttpRequest);
                String videoUrl = responseInfo.getRespStr();
                if (mFileUrl.startsWith("http")
                        && (mFileUrl.contains("youku") || mFileUrl.contains("tc.qq.com") || mFileUrl
                        .contains("tudou"))) {
                    if (videoUrl != null) {
                        Logger.LOGD(TAG + " load video url=" + videoUrl);
                        // mFileUrl = getRealVideoPath(videoUrl);
                        mHttpRequest.setUrl(videoUrl);
                        mHttpRequest.setUrlParams(null);
                        mHttpRequest.setUseCache(false);
                        mHttpRequest.setRedirect(false);
                        mFileUrl = ISARHttpClient.getInstance().getRedirectUrl(mHttpRequest);
                        Logger.LOGD(TAG + " load video mFileUrl=" + mFileUrl);
                    } else {
                        Logger.LOGE(TAG + " load video url fail=" + videoUrl);
                        mDownloadListener.onDownloadError();
                        return;
                    }
                }
            }

            mHttpRequest.setUrlParams(null);
            if (mIsHls) {
                // 1.parse m3u8 video list
                // 2.save m3u8 file width local video list
                // 3.download video list as formated name
                mHttpRequest.setUrl(mFileUrl);
                mHttpRequest.setUseCache(false);
                mHttpRequest.setARReqListener(mRequestListener);
                mHttpRequest.setTargetPath(mTargetPath);
                mLastCachedSize = 0;
                mIsLastVideo = false;
                List<String> videoList = ISARHttpClient.getInstance().downloadFileForm3u8(mHttpRequest);

                String prefixUrl = getPrefixUrl(mFileUrl);
                String videoUrl = prefixUrl;
                int listSize = videoList.size();
                for (int i=0; i < listSize; i++) {
                    if (mIsNeedStop) {
                        break;
                    }
                    String vName = videoList.get(i);
                    if (i == (listSize - 1)) {
                        mIsLastVideo = true;
                    }
                    //downloadVideoFile(videoUrl + vName, vName, isLastVideo);
                    mHttpRequest.setUrl(videoUrl + vName);
                    int result = ISARHttpClient.getInstance().downloadFileForVideoPlayer(mHttpRequest);
                    if (result != HttpURLConnection.HTTP_OK) {
                        mDownloadListener.onDownloadError();
                    }
                    Logger.LOGD(TAG + " run videoList=" + vName);
                }
            } else {
                mHttpRequest.setUrl(mFileUrl);
                mHttpRequest.setUseCache(false);
                mHttpRequest.setARReqListener(mRequestListener);
                mHttpRequest.setTargetPath(mTargetPath);
                mLastCachedSize = 0;
                int result = ISARHttpClient.getInstance().downloadFileForVideoPlayer(mHttpRequest);
                if (result != HttpURLConnection.HTTP_OK) {
                    mDownloadListener.onDownloadError();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            mDownloadListener.onDownloadError();
        }
    }

    /**
     * stop thread.
     */
    public synchronized void stopThread() {
        Logger.LOGD(TAG + " stop thread");
        this.mIsNeedStop = true;
    }

    /**
     * copy file.
     * @param srcPath source path
     * @param tarPath target path
     */
    public synchronized void copyFile(String srcPath, String tarPath) {
        try {
            int byteRead = 0;
            String mid = tarPath + System.currentTimeMillis() + "_mid";
            File srcFile = new File(srcPath);
            if (srcFile.exists()) {
                FileInputStream fin = new FileInputStream(srcFile);
                FileOutputStream fout = new FileOutputStream(mid);

                byte[] buffer = new byte[1024];
                while ((byteRead = fin.read(buffer)) != -1) {
                    fout.write(buffer, 0, byteRead);
                }
                fout.flush();
                fout.close();
                fin.close();

                // rename to target file
                File tarFile = new File(tarPath);
                File midFile = new File(mid);
                if (!tarFile.exists()) {
                    midFile.renameTo(tarFile);
                }

            }
        } catch (Exception e) {
            Logger.LOGE(TAG + "e:" + e);
            e.printStackTrace();
        }
    }

    public void write(String filePath, String content) {
        BufferedWriter bw = null;

        try {
            // 根据文件路径创建缓冲输出流 
            bw = new BufferedWriter(new FileWriter(filePath));
            // 将内容写入文件中 
            bw.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭流 
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    bw = null;
                }
            }
        }
    }
    
    public void setVideoFloat(boolean videoFloat) {
        Logger.LOGD(TAG + " setVideoFloat videoFloat=" + videoFloat);
        this.mVideoFloat = videoFloat;
    }

    private String getPrefixUrl(String line) {
        if (line.startsWith("http://")) {
            int las = line.lastIndexOf("/");
            String result = line.substring(0, las + 1);
            Logger.LOGD(TAG + " getPrefixUrl las=" + las + ", name=" + result);
            return result;
        } else {
            return line;
        }
    }

}
