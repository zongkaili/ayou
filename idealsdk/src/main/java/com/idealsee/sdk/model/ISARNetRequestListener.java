package com.idealsee.sdk.model;

public interface ISARNetRequestListener {
    public int requestOk();
    public int requestError(int errorCode);
}
