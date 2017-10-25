package com.yixun.sdk.game;

import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import com.yixun.sdk.idhttp.DLProgressListener;
import com.yixun.sdk.idhttp.IdHttpClient;
import com.yixun.sdk.idhttp.IdProgressListener;
import com.yixun.sdk.util.ISARFilesUtil;
import com.yixun.sdk.util.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yaolei on 17-3-24.
 */

public class ARPackageManager {
    public static final String SEGMENT = "_";
    public static final String SUFFIX_ZIP = ".zip";
    public static final String SUFFIX_DAT = ".dat";
    private File mZipDir;//.zip资源包文件
    private File mResourcesDir;//.zip解压后的存储文件和.dat识别文件存放路径
    private String mUrlHeader;

    public ARPackageManager(String urlHeader) {
        this.mUrlHeader = urlHeader;
    }

    public void setZipDir(File zipDir) {
        if (!zipDir.isDirectory()) {
            throw new NullPointerException();
        }
        this.mZipDir = zipDir;
    }

    public void setResouceDir(File resouceDir) {
        if (!resouceDir.isDirectory()) {
            throw new NullPointerException();
        }
        this.mResourcesDir = resouceDir;
    }

    public ARPackageRefreshInfo getRefreshInfo(List<ARPackageInfo> arPackages) {
        List<ARPackageInfo> refreshList = new ArrayList<>();
        List<String> deleteList = new ArrayList<>();
        Map<String, String> localPackages = getLocalPackages();
        for (ARPackageInfo info : arPackages) {
            String currentTime = localPackages.get(info.getId());
            if (TextUtils.isEmpty(currentTime)) {
                refreshList.add(info);
            } else if (info.getTime().equals(currentTime)) {
                // 未完成的下载
                String dpFilePath = mZipDir.getAbsolutePath() + File.separator
                        + info.getId()
                        + SEGMENT
                        + info.getTime()
                        + SUFFIX_ZIP
                        + IdHttpClient.getInstance().getRecordFileSuffix();
                File dpFile = new File(dpFilePath);
                if (dpFile.exists()) {
                    refreshList.add(info);
                }
            } else if (Long.valueOf(info.getTime()) > Long.valueOf(currentTime)) {
                // 需要更新的下载
                refreshList.add(info);
                String deleteZipPath = mZipDir.getAbsolutePath() + File.separator
                        + info.getId() + SEGMENT + currentTime + SUFFIX_ZIP;
                String deleteDpPath = deleteZipPath + IdHttpClient.getInstance().getRecordFileSuffix();
                File dpFile = new File(deleteDpPath);
                if (dpFile.exists()) {
                    deleteList.add(deleteDpPath);
                }
                deleteList.add(deleteZipPath);
            }
        }
        return new ARPackageRefreshInfo(refreshList, deleteList);
    }

    /**
     * 用于下载多个非zip文件（不需要解压）：如dat文件
     *
     * @param suffix
     * @param md5s
     * @param dlProgressListener
     */
    public void downloadPackages(String url, String suffix, final List<String> md5s, final DLProgressListener dlProgressListener) {
        IdProgressListener listener = new IdProgressListener() {
            int loadedPackagePos = 1;

            @Override
            public void onSpaceLimited() {
                if (dlProgressListener != null)
                    dlProgressListener.onSpaceLimited();
            }

            @Override
            public void onProgress(int per) {
                Logger.LOGD(" downloadPackages : onProgress per : " + per);
                if (dlProgressListener != null) {
                    if (md5s.size() > 1)
                        dlProgressListener.onProgress(per, loadedPackagePos + "/" + md5s.size());
                    else
                        dlProgressListener.onProgress(per, "");
                }
            }

            @Override
            public void onComplete() {
                Logger.LOGD(" downloadPackages : onProgress onComplete loadedPackagePos : " + loadedPackagePos);
                loadedPackagePos++;
            }

            @Override
            public void onCancel() {
                if (dlProgressListener != null)
                    dlProgressListener.onCancel();
            }

            @Override
            public void onFailed(String failedString) {
                if (dlProgressListener != null)
                    dlProgressListener.onFailed(failedString);
            }
        };

        File dpFile = null;
        for (int i = 0; i < md5s.size(); i++) {
            if (i == 0) {
                if (dlProgressListener != null)
                    dlProgressListener.onStart();
            }
            IdHttpClient.getInstance().doFileDownload(
                    mUrlHeader + url + md5s.get(i) + suffix,
                    mResourcesDir.getAbsolutePath() + File.separator
                            + md5s.get(i)
                            + suffix,
                    getSDAvailableSize(),
                    listener);
            dpFile = new File(mResourcesDir.getAbsolutePath() + File.separator
                    + md5s.get(i)
                    + suffix + IdHttpClient.getInstance().getRecordFileSuffix());
            if (dpFile.exists()) {
                dpFile.delete();
            }
            if (i == md5s.size() - 1 && dlProgressListener != null) {
                dlProgressListener.onComplete();
            }
        }
    }

    /**
     * 用于下载单个zip文件，下好之后解压
     *
     * @param suffix              　文件名后缀
     * @param md5                 　主题md5:此处用于文件名
     * @param mDlProgressListener 　下载进度
     * @param mDecompressListener 　解压进度
     */
    public void downloadPackage(String url, String suffix, String md5, final DLProgressListener mDlProgressListener
            , DecompressListener mDecompressListener) {
        IdProgressListener listener = new IdProgressListener() {
            @Override
            public void onSpaceLimited() {
                if (mDlProgressListener != null)
                    mDlProgressListener.onSpaceLimited();
            }

            @Override
            public void onProgress(int per) {
                Logger.LOGD(" downloadPackages : onProgress per : " + per);
                if (mDlProgressListener != null)
                    mDlProgressListener.onProgress(per, "");
            }

            @Override
            public void onComplete() {
                if (mDlProgressListener != null)
                    mDlProgressListener.onComplete();
            }

            @Override
            public void onCancel() {
                if (mDlProgressListener != null)
                    mDlProgressListener.onCancel();
            }

            @Override
            public void onFailed(String failedString) {
                if (mDlProgressListener != null)
                    mDlProgressListener.onFailed(failedString);
            }
        };
        String fileName = mZipDir.getAbsolutePath() + File.separator
                + md5
                + suffix;
        if (mDlProgressListener != null)
            mDlProgressListener.onStart();
        IdHttpClient.getInstance().doFileDownload(
                mUrlHeader + url + md5 + suffix,
                fileName,
                getSDAvailableSize(),
                listener);
        if (mDlProgressListener != null)
            mDlProgressListener.onComplete();
        decompressData(fileName, md5, mDecompressListener);
    }

    /**
     * 用于下载多个zip文件，逐个解压
     *
     * @param refreshPackages
     * @param mDlProgressListener
     * @param mDecompressListener
     */
    public void downloadPackages(final List<ARPackageInfo> refreshPackages, final DLProgressListener mDlProgressListener
            , DecompressListener mDecompressListener) {
        IdProgressListener listener = new IdProgressListener() {
            int loadedPackagePos = 1;

            @Override
            public void onSpaceLimited() {
                mDlProgressListener.onSpaceLimited();
            }

            @Override
            public void onProgress(int per) {
                mDlProgressListener.onProgress(per, loadedPackagePos + "/" + refreshPackages.size());
            }

            @Override
            public void onComplete() {
                loadedPackagePos++;
            }

            @Override
            public void onCancel() {
                mDlProgressListener.onCancel();
            }

            @Override
            public void onFailed(String failedString) {
                mDlProgressListener.onFailed(failedString);
            }
        };

        for (int i = 0; i < refreshPackages.size(); i++) {
            if (i == 0) {
                mDlProgressListener.onStart();
            }
            IdHttpClient.getInstance().doFileDownload(
                    mUrlHeader + refreshPackages.get(i).getUrl(),
                    mZipDir.getAbsolutePath() + File.separator
                            + refreshPackages.get(i).getId()
                            + SEGMENT + refreshPackages.get(i).getTime() + SUFFIX_ZIP,
                    getSDAvailableSize(),
                    listener);
            if (i == refreshPackages.size() - 1 && mDlProgressListener != null) {
                mDlProgressListener.onComplete();
                decompressDatas(refreshPackages, mDecompressListener);
            }
        }
    }

    private long getSDAvailableSize() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return blockSize * availableBlocks;
    }

    /**
     * 解压多个数据包
     *
     * @param decompressPackages
     * @param decompressListener
     */
    private void decompressDatas(List<ARPackageInfo> decompressPackages, DecompressListener decompressListener) {
        for (int i = 0; i < decompressPackages.size(); i++) {
            if (i == 0 && decompressListener != null) {
                decompressListener.onStart();
            }
            String fileName = mZipDir.getAbsolutePath() + File.separator
                    + decompressPackages.get(i).getId()
                    + SEGMENT + decompressPackages.get(i).getTime() + SUFFIX_ZIP;
            if (fileName.endsWith(SUFFIX_ZIP)) {
                try {
                    ISARFilesUtil.UnZipFolder(fileName, mResourcesDir.getAbsolutePath());
                    File dpFile = new File(fileName + IdHttpClient.getInstance().getRecordFileSuffix());
                    if (dpFile.exists()) {
                        dpFile.delete();
                    }
                } catch (Exception e) {
                    if (decompressListener != null) {
                        decompressListener.onFailed();
                    }
                    e.printStackTrace();
                }
            }
            if (i == decompressPackages.size() - 1 && decompressListener != null)
                decompressListener.onComplete();
        }
    }

    /**
     * 解压单个数据包
     *
     * @param md5
     * @param decompressListener
     */
    private void decompressData(String fileName, String md5, DecompressListener decompressListener) {
        if (decompressListener != null)
            decompressListener.onStart();
        if (fileName.endsWith(SUFFIX_ZIP)) {
            try {
                File resourceFile = new File(mResourcesDir.getAbsolutePath() + File.separator + md5);
                if (!resourceFile.exists())
                    resourceFile.mkdirs();
                ISARFilesUtil.UnZipFolder(fileName, resourceFile.getAbsolutePath());
                File dpFile = new File(fileName + IdHttpClient.getInstance().getRecordFileSuffix());
                if (dpFile.exists()) {
                    dpFile.delete();
                }
            } catch (Exception e) {
                if (decompressListener != null) {
                    decompressListener.onFailed();
                }
                e.printStackTrace();
            }
        }
        if (decompressListener != null)
            decompressListener.onComplete();
    }

    private Map<String, String> getLocalPackages() {
        Map<String, String> localPackages = new HashMap<>();
        for (File file : mZipDir.listFiles()) {
            String fileName = file.getName();
            if (fileName.endsWith(SUFFIX_ZIP)) {
                String id = fileName.substring(0, fileName.indexOf(SEGMENT));
                String time = fileName.substring(fileName.indexOf(SEGMENT) + 1, fileName.indexOf(SUFFIX_ZIP));
                if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(time)) {
                    localPackages.put(id, time);
                }
            }
        }
        return localPackages;
    }
}
