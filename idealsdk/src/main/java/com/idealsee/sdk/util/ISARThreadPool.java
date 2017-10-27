package com.idealsee.sdk.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public enum ISARThreadPool {
    INSTANCE;

    private ThreadPoolExecutor mThreadPool;

    ISARThreadPool() {
        int core = 5;
        int maxSize = 10;
        int keepAliveTime = 20;
        mThreadPool = new ThreadPoolExecutor(core, maxSize, keepAliveTime, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(10),
                new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    public void execute(Runnable runnable) {
        mThreadPool.execute(runnable);
    }

    public void remove(Runnable runnable) {
        mThreadPool.remove(runnable);
    }

    public void shutDown() {
        mThreadPool.shutdown();
    }
}
