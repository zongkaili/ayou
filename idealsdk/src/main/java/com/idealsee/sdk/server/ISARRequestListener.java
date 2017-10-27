package com.idealsee.sdk.server;

/**
 * Created by hongen on 17-1-3.
 */

public interface ISARRequestListener<T> {
    void onIdARHttpGetDone(T t);
    void onIdARHttpGetProgress(T t);
}
