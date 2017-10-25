package com.yixun.sdk.game;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.yixun.ar.unity.ISARCamera;
import com.yixun.ar.unity.ISARUnityMessageManager;
import com.yixun.sdk.idhttp.DLProgressListener;
import com.yixun.sdk.server.ISARHttpServerURL;
import com.yixun.sdk.util.ISARFilesUtil;
import com.yixun.sdk.util.ISARThreadPool;
import com.yixun.sdk.util.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 用于检查游戏资源信息：添加/更新/删除
 * Created by zongkaili on 17-5-10.
 */

public class GameResourceHelper {
    private static final String TAG = GameResourceHelper.class.getSimpleName();
    private static final int MSG_HANDLE_RESOUCE_INFO = 1001;
    private static final int MSG_DECOMPRESS_FINISH = 1002;
    private static final String ZIP = "Zip";
    private static final String RESOURCES = "Resources";
    private Context mContext;
    private ResouceChangeInfo mResouceChangeInfo;
    private String mOpDatetime = "";
    public String mZipDir, mResourceDir;
    public boolean mHasSetDatPath = false;
    private String[] dataFilePath;
    private DLProgressListener mDlProgressListener;

    public GameResourceHelper(Context context) {
        mContext = context;
        mZipDir = mContext.getExternalFilesDir(null) + File.separator + ZIP;
        mResourceDir = mContext.getExternalFilesDir(null) + File.separator + RESOURCES;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            Log.d(TAG, "handleMessage msg.what=" + msg.what);
            switch (msg.what) {
                case MSG_HANDLE_RESOUCE_INFO:
                    handleResouceInfo();
                    break;
                case MSG_DECOMPRESS_FINISH:
                    //解压zip完成后，读取其中的txt文件，将其中的动画数据传给unity
                    handleAnimationData((String) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 更新离线识别文件
     * 　如果有　则相应执行下载/删除等操作
     *
     * @param dlProgressListener 进度回调　不关心进度可传null
     * @param callBack           post资源信息的回调　只回调成功/失败
     */
    public void updateLocalRecognitionFiles(String opDatetime, DLProgressListener dlProgressListener, final GameHttpClientHelper.PostResouceInfoCallBack callBack) {
        mDlProgressListener = dlProgressListener;
        //检查本地dat文件 返回已有dat文件名字符串
        String md5s = getLocalDatFilesName();
        Logger.LOGD(TAG + " updateLocalRecognitionFiles mResouceChangeInfo md5s : " + md5s);
        Logger.LOGD(TAG + " updateLocalRecognitionFiles mResouceChangeInfo mOpDatetime : " + mOpDatetime);

        //post检查线上资源更改情况
        GameHttpClientHelper.getInstance().doPostCheckResouceInfo(md5s, opDatetime, new GameHttpClientHelper.PostResouceInfoCallBack() {
            @Override
            public void onSuccess(ResouceChangeInfo info) {
                mResouceChangeInfo = info;
                mHandler.sendEmptyMessage(MSG_HANDLE_RESOUCE_INFO);
                callBack.onSuccess(info);
            }

            @Override
            public void onFailed() {
                callBack.onFailed();
            }
        });
    }

    private String getLocalDatFilesName() {
        String md5s = "";
        File file = new File(mContext.getExternalFilesDir(null) + File.separator + "Resources");
        if (!file.exists()) {
            return md5s;
        }
        File[] subFile = file.listFiles();
        int subLength = subFile.length;
        if (subLength == 0) {
            return md5s;
        }
        for (int i = 0; i < subLength; i++) {
            if (subFile[i].getAbsolutePath().endsWith(ARPackageManager.SUFFIX_DAT)) {
                if (i == subLength - 1) {
                    md5s += (subFile[i].getName().substring(0, subFile[i].getName().lastIndexOf(".")));
                } else {
                    md5s += (subFile[i].getName().substring(0, subFile[i].getName().lastIndexOf(".")) + ",");
                }
            }
        }
        return md5s;
    }

    private void handleResouceInfo() {
        Logger.LOGD(TAG + " mResouceChangeInfo.addMd5s : " + mResouceChangeInfo.addMd5s);
        Logger.LOGD(TAG + " mResouceChangeInfo.deleteMd5s : " + mResouceChangeInfo.deleteMd5s);
        Logger.LOGD(TAG + " mResouceChangeInfo.updateMd5s : " + mResouceChangeInfo.updateMd5s);
        //记录当前造操作的时间
        mOpDatetime = mResouceChangeInfo.opDateTime;

        if (mResouceChangeInfo.addMd5s != null && mResouceChangeInfo.addMd5s.size() > 0) {
            //下载增加的资源dat文件
            downloadDatFiles(mResouceChangeInfo.addMd5s);
        }
        if (mResouceChangeInfo.deleteMd5s != null && mResouceChangeInfo.deleteMd5s.size() > 0) {
            //删除相应的识别文件和资源文件
            deleteDatAndResouceFile(mResouceChangeInfo.deleteMd5s, true);
        }
        if (mResouceChangeInfo.updateMd5s != null && mResouceChangeInfo.updateMd5s.size() > 0) {
            //只删除相应的资源文件
            deleteDatAndResouceFile(mResouceChangeInfo.updateMd5s, false);
        }
    }

    private void downloadDatFiles(final List<String> addMd5s) {
        ISARThreadPool.INSTANCE.execute(new Runnable() {
            @Override
            public void run() {
                File datDir = new File(mResourceDir);
                datDir.mkdirs();
                ARPackageManager manager = new ARPackageManager(ISARHttpServerURL.getHttpUrl());
                manager.setResouceDir(datDir);
                manager.downloadPackages(mResouceChangeInfo.datUrl, ARPackageManager.SUFFIX_DAT, addMd5s, mDlProgressListener);

                setDatFilePath();
            }
        });
    }

    public void setDatFilePath() {
        if (mHasSetDatPath) {
            return;
        }
        File file = new File(mContext.getExternalFilesDir(null) + File.separator + "Resources");
        if (!file.exists()) {
            return;
        }

        File[] subFile = file.listFiles();
        int subLength = subFile.length;
        if (subLength == 0) {
            return;
        }

        List<String> filePathList = new ArrayList<String>();
        for (int i = 0; i < subLength; i++) {
            if (subFile[i].getAbsolutePath().endsWith(ARPackageManager.SUFFIX_DAT)) {
                filePathList.add(subFile[i].getAbsolutePath());
            }
        }
        int datSize = filePathList.size();
        dataFilePath = new String[datSize];
        Collections.sort(filePathList, new FileNameCompare());
        for (int i = 0; i < datSize; i++) {
            dataFilePath[i] = filePathList.get(i);
            Logger.LOGD(TAG + " dataFilePath[i]=" + dataFilePath[i]);
        }

        if (!mHasSetDatPath) {
            ISARCamera.getInstance().setDatPath(dataFilePath);
            mHasSetDatPath = true;
        }
    }

    class FileNameCompare implements Comparator<String> {

        @Override
        public int compare(String lhs, String rhs) {
            if (lhs.compareTo(rhs) > 0) {
                return -1;
            } else {
                return 1;
            }
        }

    }

    public String[] getDataFilePath() {
        return dataFilePath;
    }

    public boolean isHasSetDatPath() {
        return mHasSetDatPath;
    }

    private void deleteDatAndResouceFile(List<String> deleteMd5s, boolean isDeleteDat) {
        File resourceDir = new File(mResourceDir);
        if (!resourceDir.exists()) {
            return;
        }
        if (resourceDir.listFiles() == null || resourceDir.listFiles().length <= 0) {
            return;
        }
        String fileName = "";
        for (int i = 0; i < resourceDir.listFiles().length; i++) {
            for (String deleteMd5 : deleteMd5s) {
                if (i >= resourceDir.listFiles().length) {
                    break;
                }
                if (resourceDir.listFiles()[i].isDirectory()) {
                    fileName = resourceDir.listFiles()[i].getName();
                } else if (resourceDir.listFiles()[i].getName().endsWith(ARPackageManager.SUFFIX_DAT)) {
                    fileName = resourceDir.listFiles()[i].getName().substring(0, resourceDir.listFiles()[i].getName().lastIndexOf("."));
                }

                if (fileName.equals(deleteMd5) && (isDeleteDat ? true : resourceDir.listFiles()[i].isDirectory())) {
                    ISARFilesUtil.deleteAllFiles(resourceDir.listFiles()[i]);
                }
            }
        }
        setDatFilePath();
    }

    /**
     * 更新动画数据　下载zip数据文件
     *
     * @param md5                　主题md5
     * @param packageUrl         　下载zip包的ｕrl地址
     * @param dlProgressListener 　下载进度回调 可传null
     * @param decompressListener 　解压回调 可传null
     */
    public void updateAnimationData(final String md5, final String packageUrl,
                                    final DLProgressListener dlProgressListener,
                                    final DecompressListener decompressListener) {
        File resourcesDir = new File(mContext.getExternalFilesDir(null) + File.separator + "Resources" + File.separator + md5);
        Logger.LOGD(TAG + " mThemePicMd5 ==resourcesDir " + resourcesDir.getAbsolutePath());
        Logger.LOGD(TAG + " mThemePicMd5 ==resourcesDir.exists() " + resourcesDir.exists());
        if (resourcesDir.exists()) {
            if (resourcesDir.listFiles().length > 0) {
                handleAnimationData(md5);
                Logger.LOGD(TAG + " mThemePicMd5 ==1 " + md5);
                return;
            } else {
                Logger.LOGD(TAG + " mThemePicMd5 ==2 " + md5);
                resourcesDir.delete();
            }
        }
        Logger.LOGD(TAG + " mThemePicMd5 ==3 " + md5);
        if (TextUtils.isEmpty(packageUrl) || TextUtils.isEmpty(md5) || md5.equals("0")) {
            return;
        }
        ISARThreadPool.INSTANCE.execute(new Runnable() {
            @Override
            public void run() {
                File zipDir = new File(mZipDir);
                File resourcesDir = new File(mResourceDir);
                zipDir.mkdirs();
                resourcesDir.mkdirs();
                ARPackageManager manager = new ARPackageManager(ISARHttpServerURL.getHttpUrl());
                manager.setZipDir(zipDir);
                manager.setResouceDir(resourcesDir);
                //下载zip文件(包括动画数据),如果不关心进度，第2/3个参数可以传null
                manager.downloadPackage(packageUrl, ARPackageManager.SUFFIX_ZIP, md5, dlProgressListener, new DecompressListener() {
                    @Override
                    public void onStart() {
                        if (decompressListener != null) {
                            decompressListener.onStart();
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (decompressListener != null) {
                            decompressListener.onComplete();
                        }
                        // 解压完成后，需要休眠一会，等待文件完全写入文件系统，防止算法读取到错误的文件。
                        Message msg = new Message();
                        msg.what = MSG_DECOMPRESS_FINISH;
                        msg.obj = md5;
                        mHandler.sendMessageDelayed(msg, 2000);
                    }

                    @Override
                    public void onFailed() {
                        if (decompressListener != null) {
                            decompressListener.onFailed();
                        }
                    }
                });
            }
        });
    }

    public void handleAnimationData(String themePicMd5) {
        if (TextUtils.isEmpty(themePicMd5)) {
            return;
        }
        //读取Resources文件夹下的txt文件内容
        File file = new File(mResourceDir + File.separator + themePicMd5 + File.separator + themePicMd5 + ".txt");
        if (!file.exists()) {
            return;
        }
        String animationData = ISARFilesUtil.getTxtFileContent(file);
        Logger.LOGD(TAG + " handleAnimationData animationData : " + animationData);
        ISARUnityMessageManager.loadThemeData(animationData);
    }

    /**
     * 检查unity游戏资源
     *
     * @param unityFileName      文件名
     * @param unity3dUrl         　请求url
     * @param dlProgressListener 进度回调
     */
    public void checkGameResource(final String unityFileName, final String unity3dUrl,
                                  final DLProgressListener dlProgressListener) {
        final File resourcesDir = new File(mContext.getExternalFilesDir(null) + File.separator + "Resources/" + unityFileName);
        if (resourcesDir.exists()) {
            if (resourcesDir.listFiles().length <= 0) {
                resourcesDir.delete();
            } else {
                dlProgressListener.onComplete();
                return;
            }
        }
        ISARThreadPool.INSTANCE.execute(new Runnable() {
            @Override
            public void run() {
                resourcesDir.mkdirs();
                ARPackageManager manager = new ARPackageManager(ISARHttpServerURL.getHttpUrl());
                manager.setResouceDir(resourcesDir);
                List<String> fileList = new ArrayList<String>();
                fileList.add(unityFileName);
                manager.downloadPackages(unity3dUrl, "", fileList, dlProgressListener);
            }
        });
    }
}
