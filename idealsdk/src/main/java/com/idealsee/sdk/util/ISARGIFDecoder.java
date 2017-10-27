package com.idealsee.sdk.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

/**
 * Gif decoder.
 *
 */
public class ISARGIFDecoder {

    private int mFrameCount;
    private int mImageWidth;
    private int mImageHeight;
    private int mLoopCount;
    private Frame mCurFrame = null;
    private int mDecoderHandler = 0;

    public static final int DEFAULT_DELAY = 100;

    /**
     * constructor.
     * 
     * @param path
     *            the gif file path
     */
    public ISARGIFDecoder(String path) {
        int[] params = new int[5];
        if (nInitByPath(path, params) == 0) {
            this.mFrameCount = params[0];
            this.mImageWidth = params[1];
            this.mImageHeight = params[2];
            this.mLoopCount = params[3];
            this.mDecoderHandler = params[4];
            mCurFrame = new Frame();
            mCurFrame.mBitmap = Bitmap.createBitmap(mImageWidth, mImageHeight, Config.ARGB_8888);
            mCurFrame.mIndex = 0;
        } else {
            throw new RuntimeException("Gif file decode error");
        }
    }

    /**
     * constructor.
     * 
     * @param buffer
     *            the gif param array
     */
    public ISARGIFDecoder(byte[] buffer) {
        int[] params = new int[5];
        if (nInitByBytes(buffer, params) == 0) {
            this.mFrameCount = params[0];
            this.mImageWidth = params[1];
            this.mImageHeight = params[2];
            this.mLoopCount = params[3];
            this.mDecoderHandler = params[4];
            mCurFrame = new Frame();
            mCurFrame.mBitmap = Bitmap.createBitmap(mImageWidth, mImageHeight, Config.ARGB_8888);
            mCurFrame.mIndex = 0;
        } else {
            throw new RuntimeException("Gif file decode error");
        }
    }

    /**
     * release the resources.
     */
    public void recycle() {
        mCurFrame.mBitmap.recycle();
        if (this.nDestory(this.mDecoderHandler) != 0) {
            throw new RuntimeException("native destory failed");
        }
    }

    /**
     * get total frame count of the gif.
     * @return frame count
     */
    public int getFrameCount() {
        return this.mFrameCount;
    }

    /**
     * get image width.
     * @return image width
     */
    public int getWidth() {
        return this.mImageWidth;
    }

    /**
     * get image height.
     * @return image height
     */
    public int getHeight() {
        return this.mImageHeight;
    }

    /**
     * get loop count.
     * @return loop count
     */
    public int getLoopCount() {
        return this.mLoopCount;
    }

    /**
     * get first frame.
     * @return the first frame.
     */
    public Frame getFirstFrame() {
        return getFrame(0);
    }

    /**
     * get current frame.
     * @return the current frame.
     */
    public Frame getCurrentFrame() {
        return getFrame(mCurFrame.mIndex);
    }

    /**
     * get next frame.
     * @return the next frame.
     */
    public Frame getNextFrame() {
        if ((mLoopCount == 1) && ((mCurFrame.mIndex + 1) == mFrameCount)) {
            return getFrame(mCurFrame.mIndex);
        }
        return getFrame((mCurFrame.mIndex + 1) % mFrameCount);
    }

    /**
     * get frame by index.
     * 
     * @param index index
     * @return frame
     */
    public Frame getFrame(int index) {
        if (index < 0 || index >= this.mFrameCount) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        if (mCurFrame.mBitmap != null) {
            int delay = nGetFrameBitmap(index, mCurFrame.mBitmap, this.mDecoderHandler);
            if (delay > 0) {
                mCurFrame.mDelay = delay;
            } else {
                mCurFrame.mDelay = DEFAULT_DELAY;
            }
            mCurFrame.mIndex = index;
            return mCurFrame;
        } else {
            throw new NullPointerException("Bitmap is null");
        }
    }

    /**
     * One frame of the gif.
     * 
     * @author felix
     * 
     */
    public class Frame {
        public int mIndex = 0;
        public Bitmap mBitmap = null;
        public int mDelay = 0;
    }

    @Override
    protected void finalize() throws Throwable {
        this.recycle();
        super.finalize();
    }

    /**
     * load the native library.
     */
    static {
        System.loadLibrary("ISARNatvieModel");
    }

    /**
     * init the params, get the frame count, width,height.
     * 
     * @param path
     *            gif image file path
     * @param params
     *            returned gif parameters
     * @return error code ,0 if no error
     */
    private static native int nInitByPath(String path, int[] params);

    /**
     * init the params, get the frame count, width,height.
     * 
     * @param buffer buffer
     * @param params params
     * @return success or fail
     */
    private static native int nInitByBytes(byte[] buffer, int[] params);

    /**
     * write image data to bitmap.
     * 
     * @param index
     *            frame index
     * @param bmp
     *            target bitmap
     * @param handler handler
     * @return frame delay time(ms) if <= 0 means failed
     */
    private static native int nGetFrameBitmap(int index, Object bmp, int handler);

    /**
     * destory the native resources.
     * @param handler handler
     * @return success or fail
     */
    private static native int nDestory(int handler);

}
