/**
 * 
 */
package com.idealsee.sdk.media;

import java.io.IOException;

import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;

import com.idealsee.sdk.util.Logger;

/**
 * @author hongen
 *
 */
public class ISARAudioRecorder implements OnInfoListener, OnErrorListener{

    private static final String TAG = "IdealAudioRecorder";
    
    private MediaRecorder mAudioRecorder;
    
    public ISARAudioRecorder() {
//        mAudioRecorder = new MediaRecorder();
    }
    
    /**
     * start audio recording
     */
    public void startAudioRecorder(String targetPath) {
        stopAudioRecoder();
//        if (null != mAudioRecorder) {
        mAudioRecorder = new MediaRecorder();
        mAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mAudioRecorder.setOutputFile(targetPath);
        /*try {
            mAudioRecorder.prepare();
            mAudioRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        // Handle IOException
        try {
            mAudioRecorder.prepare();
        } catch (IOException exception) {
            mAudioRecorder.reset();
            mAudioRecorder.release();
            mAudioRecorder = null;
            return;
        }
        // Handle RuntimeException if the recording couldn't start
        try {
            mAudioRecorder.start();
        } catch (RuntimeException exception) {

            mAudioRecorder.reset();
            mAudioRecorder.release();
            mAudioRecorder = null;
            return;
        }
//        }
    }
    
    /**
     * stop audio recording
     */
    public void stopAudioRecoder() {
        if (null != mAudioRecorder) {
            try {
                mAudioRecorder.setOnErrorListener(null);
                mAudioRecorder.setPreviewDisplay(null);
                mAudioRecorder.stop();
                mAudioRecorder.release();
//                mAudioRecorder.reset();
                mAudioRecorder = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Release audio recording.
     * if do not use audio recorder any more, need call this method.
     */
    /*public void releaseAudioRecorder() {
        if (null != mAudioRecorder) {
            try {
                mAudioRecorder.setOnErrorListener(null);
                mAudioRecorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        Logger.LOGD(TAG + " error " + what);
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        Logger.LOGD(TAG + " info " + what);
        
    }
}
