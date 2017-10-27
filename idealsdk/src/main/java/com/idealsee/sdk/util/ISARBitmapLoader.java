package com.idealsee.sdk.util;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.idealsee.sdk.server.ISARHttpClient;
import com.idealsee.sdk.server.ISARHttpRequest;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by hongen on 16-12-27.
 * 操作本地缓存图片.
 */

public class ISARBitmapLoader {
    private static final String TAG = "BitmapLoader";
    private static final int SOFT_CACHE_CAPACITY = 32;
    private LinkedHashMap<String, SoftReference<Bitmap>> mSoftCache;
    private LruCache<String, Bitmap> mMemoryCache;
    private Lock mMemoryLock = new ReentrantLock();
    private boolean mIsInitialized = false;
    private static ISARBitmapLoader sBitmapLoader;

    private ISARBitmapLoader() {
    }

    public static synchronized ISARBitmapLoader getInstance() {
        if (null == sBitmapLoader) {
            sBitmapLoader = new ISARBitmapLoader();
        }
        return sBitmapLoader;
    }

    /**
     * BitmapLoader should initialized by context.
     * if not initialized, methods will be ignore.
     * @param context
     */
    public void initCache(Context context) {
        mMemoryLock.lock();
        if (mIsInitialized) {
            Logger.LOGW(TAG + " init");
            mMemoryLock.unlock();
            return;
        }
        mSoftCache = new LinkedHashMap<String, SoftReference<Bitmap>>(SOFT_CACHE_CAPACITY) {
            private static final long serialVersionUID = -3583502473393599509L;

            @Override
            public SoftReference<Bitmap> put(String key, SoftReference<Bitmap> value) {
                return super.put(key, value);
            }

            @Override
            protected boolean removeEldestEntry(Entry<String, SoftReference<Bitmap>> eldest) {
                if (size() > SOFT_CACHE_CAPACITY) {
                    Logger.LOGD("Soft Reference limit , purge one");
                    return true;
                }
                return false;
            }
        };

        int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        int cacheSize = 1024 * 1024 * memClass / 4;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                Logger.LOGD("LruCache sizeOf:" + value.getByteCount());
                return value.getByteCount();
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                if (oldValue != null) {
                    if (evicted && mMemoryCache.get(key) != null) {
                        mMemoryLock.lock();
                        Logger.LOGD("don't release");
                        oldValue.recycle();
                        oldValue = null;
                        mMemoryCache.remove(key);
                        mMemoryLock.unlock();
                    }
                }
                Logger.LOGD("remove size:" + size());
                super.entryRemoved(evicted, key, oldValue, newValue);
            }
        };
        mIsInitialized = true;
        mMemoryLock.unlock();
        Logger.LOGI(TAG + " memClass:" + memClass + ", cacheSize:" + cacheSize);
    }

    /**
     * 将bmp放入到cache中，key为path.
     * @param path cache中的key
     * @param bmp cache中的value
     */
    public void putBitmapToCache(String path, Bitmap bmp) {
        mMemoryLock.lock();
        mMemoryCache.put(path, bmp);
        mMemoryLock.unlock();
    }

    /**
     * load bitmap from cache.
     * @param path
     * @return null if not found.
     */
    public synchronized Bitmap loadBitmapFromCache(String path) {
        mMemoryLock.lock();
        Bitmap bitmap = mMemoryCache.get(path);
        mMemoryLock.unlock();
        if (bitmap != null && !bitmap.isRecycled()) {
            Logger.LOGI("load bitmap from cache");
            return bitmap;
        }
        // 如果memoryCache中找不到，到softCache中找
        SoftReference<Bitmap> rf = mSoftCache.get(path);
        if (rf != null) {
            Bitmap bitmap2 = rf.get();
            if (bitmap2 != null) {
                return bitmap2;
            } else {
                mSoftCache.remove(path);
            }
        }
        return null;
    }

    /**
     * load bitmap from storage.
     * @param path path
     * @return null if not found.
     */
    public synchronized Bitmap loadBitmapFromStorage(Context context, String path) {
        File file = new File(path);
        if (file.exists()) {
            Bitmap tmp = ISARBitmapUtil.decodeFile(context, path);
            if (tmp != null) {
                putBitmapToCache(path, tmp);
                return tmp;
            } else {
                // 文件存在, 但是无法生成bitmap，删除损坏文件。
                file.delete();
            }
        }
        return null;
    }

    /**
     * first load from cache, then load from storage.
     * @param url http url
     * @return null if not found
     */
    public synchronized Bitmap loadBitmapByUrlNoHttp(Context context, String url) {
        String filename = ISARStringUtil.getMD5(url);
        String path = ISARConstants.APP_CACHE_DIRECTORY + File.separator + filename;
        Bitmap tmp = loadBitmapFromCache(path);
        if (tmp != null && !tmp.isRecycled()) {
            return tmp;
        }
        tmp = loadBitmapFromStorage(context, path);
        if (tmp != null) {
            return tmp;
        }
        return null;
    }

    /**
     * load bitmap by url, load bitmap from storage first, if it is not exist, load it from http.
     * @param url bitmap http url.
     * @return
     */
    public synchronized Bitmap loadBitmapByUrlOnHttp(Context context, String url) {
        String filename = ISARStringUtil.getMD5(url);
        String path = ISARConstants.APP_CACHE_DIRECTORY + File.separator + filename;
        Bitmap bitmap = loadBitmapByPath(context, path);
        if (null != bitmap) {
            return bitmap;
        }
        // not found from cache and storage, download it.
        ISARHttpRequest request = new ISARHttpRequest(url);
        request.setTargetPath(path);
        try {
            int status = ISARHttpClient.getInstance().downloadFile(request);
            if (HttpURLConnection.HTTP_OK == status) {
                bitmap = loadBitmapByPath(context, path);
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

    /**
     * first load from cache, then load from storage.
     * @param path file path
     * @return null if not found
     */
    public synchronized Bitmap loadBitmapByPath(Context context, String path) {
        Bitmap tmp = loadBitmapFromCache(path);
        if (tmp != null && !tmp.isRecycled()) {
            return tmp;
        }
        tmp = loadBitmapFromStorage(context, path);
        if (tmp != null) {
            return tmp;
        }
        return null;
    }

    public synchronized void release() {

    }

    /**
     * 内存不足时清理内存.
     *
     */
    public synchronized void clear() {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
        }
    }
}
