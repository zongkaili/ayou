package com.idealsee.sdk.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zongkaili on 17-6-5.
 */

public class ISARDatFileUtil {
    private static final String TAG = ISARDatFileUtil.class.getSimpleName();
    private static final String SUFFIX_DAT = ".dat";
    private static final String SUFFIX_TXT = ".txt";
    private static ISARDatFileUtil mFileUtil;
    private boolean mHasSetDatPath = false;
    private String[] dataFilesPath;

    public static ISARDatFileUtil getInstance() {
        if (null == mFileUtil)
            mFileUtil = new ISARDatFileUtil();
        return mFileUtil;
    }

    public String[] getDataFilesPath() {
        return dataFilesPath;
    }

    public boolean isHasSetDatPath() {
        return mHasSetDatPath;
    }

    public void setDatFilePath(String rootPath) {
        if (mHasSetDatPath)
            return;

        File file = new File(rootPath);
        if (!file.exists() || !file.isDirectory()) {
            return;
        }

        List<String> filePathList = new ArrayList<String>();
        for (File subFile : file.listFiles()) {
            String datPath = getDatFilePath(subFile);
            filePathList.add(datPath);
        }
        int datSize = filePathList.size();
        dataFilesPath = new String[datSize];
        Collections.sort(filePathList, new FileNameCompare());
        for (int i = 0; i < datSize; i++) {
            dataFilesPath[i] = filePathList.get(i);
            Logger.LOGD(TAG + " dataFilesPath[i]=" + dataFilesPath[i]);
        }

        ISARManager.setupLocalRecognitionDats(dataFilesPath);
        mHasSetDatPath = true;
    }

    private String getDatFilePath(File parent) {
        if (!parent.isDirectory()) {
            return "";
        }

        boolean hasTxt = false;
        boolean hasDat = false;
        String result = "";

        for (File file : parent.listFiles()) {
            if (file.getAbsolutePath().endsWith(SUFFIX_DAT)) {
                hasDat = true;
                result = file.getAbsolutePath();
            }

            if (file.getAbsolutePath().endsWith(SUFFIX_TXT)) {
                hasTxt = true;
            }

            if (hasDat && hasTxt) {
                String themeName = parent.getName();
                Logger.LOGD("load theme success:" + themeName);
                break;
            }
        }
        return result;
    }

    private class FileNameCompare implements Comparator<String> {

        @Override
        public int compare(String lhs, String rhs) {
            if (lhs.compareTo(rhs) > 0) {
                return -1;
            } else {
                return 1;
            }
        }

    }

}
