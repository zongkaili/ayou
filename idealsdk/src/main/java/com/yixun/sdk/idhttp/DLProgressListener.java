package com.yixun.sdk.idhttp;

/**
 * Created by yaolei on 2017/3/22.
 */

public interface DLProgressListener {
    void onSpaceLimited();

    void onStart();

    void onProgress(int per, String pos);

    void onComplete();

    void onCancel();

    void onFailed(String failedString);
}
