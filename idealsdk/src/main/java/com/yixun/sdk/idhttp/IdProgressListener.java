package com.yixun.sdk.idhttp;

/**
 * Created by yaolei on 2017/3/22.
 */

public interface IdProgressListener {
    void onSpaceLimited();

    void onProgress(int per);

    void onComplete();

    void onCancel();

    void onFailed(String failedString);
}
