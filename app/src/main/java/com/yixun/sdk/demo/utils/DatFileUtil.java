package com.yixun.sdk.demo.utils;

import com.idealsee.sdk.util.ISARManager;
import com.idealsee.sdk.util.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zongkaili on 17-6-5.
 */

public class DatFileUtil {
    private static final String TAG = DatFileUtil.class.getSimpleName();
    public static final String SUFFIX_DAT = ".dat";
    public static DatFileUtil mFileUtil;
    public boolean mHasSetDatPath = false;
    private String[] dataFilesPath;

    public static DatFileUtil getInstance() {
        if (null == mFileUtil)
            mFileUtil = new DatFileUtil();
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
            if (subFile[i].getAbsolutePath().endsWith(SUFFIX_DAT)) {
                filePathList.add(subFile[i].getAbsolutePath());
            }
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

}
