package com.idealsee.recorder;

import android.content.Context;

import com.idealsee.sdk.util.Logger;

import org.m4m.AudioFormat;
import org.m4m.GLCapture;
import org.m4m.IProgressListener;
import org.m4m.VideoFormat;
import org.m4m.android.AndroidMediaObjectFactory;
import org.m4m.android.AudioFormatAndroid;
import org.m4m.android.VideoFormatAndroid;

import java.io.IOException;

/**
 * 录制视频音频参数设置。
 */
public class VideoCapture
{
    private static final String TAG = "VideoCapture";

    private static final String Codec = "video/avc";
    private static int IFrameInterval = 1;

    private static final Object syncObject = new Object();
    private static volatile VideoCapture videoCapture;

    private static VideoFormat videoFormat;
    private static int videoWidth;
    private static int videoHeight;
    private GLCapture capturer;

    private boolean isConfigured;
    private boolean isStarted;
    private long framesCaptured;
    private Context context;
    private IProgressListener progressListener;

    public VideoCapture(Context context, IProgressListener progressListener)
    {
        this.context = context;
        this.progressListener = progressListener;
    }
    
    public static void init(int width, int height, int frameRate, int bitRate)
    {
        videoWidth = width;
        videoHeight = height;
        
        videoFormat = new VideoFormatAndroid(Codec, videoWidth, videoHeight);
        videoFormat.setVideoFrameRate(frameRate);
        videoFormat.setVideoBitRateInKBytes(bitRate);
        videoFormat.setVideoIFrameInterval(IFrameInterval);
    }

    public void start(String videoPath) throws IOException
    {
//        if (isStarted())
//            throw new IllegalStateException(TAG + " already started!");
        if (isStarted()) {
            Logger.LOGW(TAG + " already started");
            return;
        }

        capturer = new GLCapture(new AndroidMediaObjectFactory(context), progressListener);
        capturer.setTargetFile(videoPath);
        capturer.setTargetVideoFormat(videoFormat);

        AudioFormat audioFormat = new AudioFormatAndroid("audio/mp4a-latm", 44100, 2);
        capturer.setTargetAudioFormat(audioFormat);
        capturer.start();

        isStarted = true;
        isConfigured = false;
        framesCaptured = 0;
    }
    
    public void stop()
    {
//        if (!isStarted())
//            throw new IllegalStateException(TAG + " not started or already stopped!");
        if (!isStarted()) {
            Logger.LOGW(TAG + " not started or already stopped");
            return;
        }

        try {
            capturer.stop();
            isStarted = false;
        } catch (Exception ex) {
            Logger.LOGE(TAG + "--- Exception: GLCapture can't stop");
        }

        capturer = null;
        isConfigured = false;
    }

    private void configure()
    {
        if (isConfigured())
            return;

        try {
            capturer.setSurfaceSize(videoWidth, videoHeight);
            isConfigured = true;
        } catch (Exception ex) {
        }
    }

    public void beginCaptureFrame()
    {
        Logger.LOGW(TAG + " VideoCapture begineCaptureFrame " + isStarted());
        if (!isStarted())
            return;

        configure();
        if (!isConfigured())
            return;

        capturer.beginCaptureFrame();
    }

    public void endCaptureFrame()
    {
        if (!isStarted() || !isConfigured())
            return;

        capturer.endCaptureFrame();
        framesCaptured++;
    }

    public boolean isStarted()
    {
        return isStarted;
    }

    public boolean isConfigured()
    {
        return isConfigured;
    }

}
