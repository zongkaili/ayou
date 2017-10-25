package com.yixun.sdk.model;

import java.io.Serializable;

/**
 * Created by hongen on 17-1-6.
 */

public class ISARDownloadInfo implements Serializable {
    private int mTotalSize;
    private int mCurrentSize;
    private int mSizeReadOnce; // for download call back, save how many data read this time.

    public int getTotalSize() {
        return this.mTotalSize;
    }

    public void setTotalSize(int totalSize) {
        this.mTotalSize = totalSize;
    }

    public int getCurrentSize() {
        return this.mCurrentSize;
    }

    public void setCurrentSize(int currentSize) {
        this.mCurrentSize = currentSize;
    }

    public int getSizeReadOnce() {
        return this.mSizeReadOnce;
    }

    public void setSizeReadOnce(int size) {
        this.mSizeReadOnce = size;
    }
}
