/*
 * Copyright 2014-2016 Media for Mobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.m4m.domain;

import android.media.MediaCodec;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.m4m.AudioFormat;
import org.m4m.IProgressListener;
import org.m4m.VideoFormat;

import com.yixun.sdk.util.Logger;

public class MuxRender extends Render {

    private static final String TAG = "MuxRender";
    private final IMediaMuxer notReadyMuxer;
    private IMediaMuxer muxer;
    private final IProgressListener progressListener;
    private final ProgressTracker progressTracker;
    private int connectedPluginsCount = 0;
    private int tracksCount = 0;
    private int drainCount = 0;
    private int videoTrackId = -1;
    private int audioTrackId = -1;
    private ArrayList<IPluginOutput> releasersList = new ArrayList();
    private FrameBuffer frameBuffer = new FrameBuffer(0);
    private boolean zeroFramesReceived = true;

    public MuxRender(IMediaMuxer muxer, IProgressListener progressListener, ProgressTracker progressTracker) {
        super();
        this.notReadyMuxer = muxer;
        this.progressListener = progressListener;
        this.progressTracker = progressTracker;
    }

    @Override
    protected void initInputCommandQueue() {
    }

    @Override
    public void push(Frame frame) {
        // Logger.getLogger("AMP").info("Render frame presentationTimeUs = " +
        // frame.getSampleTime());

        if (zeroFramesReceived == true) {
            zeroFramesReceived = false;
        }

        if (frameBuffer.areAllTracksConfigured()) {
            writeBufferedFrames();
            writeSampleData(frame);
            feedMeIfNotDraining();
        } else {
            frameBuffer.push(frame);
            getInputCommandQueue().queue(Command.NeedInputFormat, 0);
        }
    }

    public void pushWithReleaser(Frame frame, IPluginOutput releaser) {
        // Logger.getLogger("AMP").info("Render frame presentationTimeUs = " +
        // frame.getSampleTime());

        if (zeroFramesReceived == true) {
            zeroFramesReceived = false;
        }

        if (frameBuffer.areAllTracksConfigured()) {
            writeBufferedFrames();
            writeSampleData(frame);
            releaser.releaseOutputBuffer(frame.getBufferIndex());
            feedMeIfNotDraining();
        } else {
            frameBuffer.push(frame);
            releasersList.add(releaser);
            getInputCommandQueue().queue(Command.NeedInputFormat, 0);
        }
    }

    private void writeBufferedFrames() {
        while (frameBuffer.canPull()) {
            Frame bufferedFrame = frameBuffer.pull();
            writeSampleData(bufferedFrame);
            releasersList.get(0).releaseOutputBuffer(bufferedFrame.getBufferIndex());
            releasersList.remove(0);
        }
    }

    private void writeSampleData(Frame frame) {
        IMediaCodec.BufferInfo bufferInfo = new IMediaCodec.BufferInfo();
        bufferInfo.flags = frame.getFlags();
        bufferInfo.presentationTimeUs = frame.getSampleTime();
        bufferInfo.size = frame.getLength();

        // Logger.LOGD(TAG + " writeSampleData frame.length=" + frame.getLength() + ", track=" + frame.getTrackId() + ",flag=" + bufferInfo.flags);

        muxer.writeSampleData(frame.getTrackId(), frame.getByteBuffer(), bufferInfo);

        progressTracker.track(frame.getSampleTime());
        progressListener.onMediaProgress(progressTracker.getProgress());
    }

    @Override
    public void drain(int bufferIndex) {
        Logger.LOGD(TAG + " drain bufferIndex=" + bufferIndex + ",drainCount=" + drainCount);
        drainCount++;
        if (drainCount > 1) {
            Logger.LOGW(TAG + " drainCount=" + drainCount);
            return;
        }
//        if (drainCount == connectedPluginsCount) {


            if (onStopListener != null) {
                onStopListener.onStop();
            }
            Logger.LOGD(TAG + " drain write end start");
            writeEnd();
            Logger.LOGD(TAG + " drain write end end");
            int count = 0;
            // delay 50ms, make audio write to file.(for no audio permission, last audio frame will not be written to file)
            while(count > 5) {
                try {
                    Thread.sleep(10);
                    count++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            closeRender();
            progressListener.onMediaStop();
            getInputCommandQueue().clear();
            setState(PluginState.Drained);
//        }
        // TODO: no unit tests yet, please help
        if (frameBuffer.areAllTracksConfigured()) {
            feedMeIfNotDraining();
        } else {
            getInputCommandQueue().queue(Command.NeedInputFormat, 0);
        }
        Logger.LOGD(TAG + " drain bufferIndex=" + bufferIndex + ",drainCount=" + drainCount + " ,end---");
    }

    @Override
    public void configure() {
        Logger.LOGD(TAG + " configure connectedPluginsCount=" + connectedPluginsCount);
        connectedPluginsCount++;
        getInputCommandQueue().queue(Command.NeedInputFormat, 0);
        frameBuffer.addTrack();
    }

    @Override
    public void setMediaFormat(MediaFormat mediaFormat) {
        int trackIndex = notReadyMuxer.addTrack(mediaFormat);
        if (mediaFormat instanceof VideoFormat)
            videoTrackId = trackIndex;
        if (mediaFormat instanceof AudioFormat)
            audioTrackId = trackIndex;

        frameBuffer.configure(tracksCount);
        tracksCount++;
    }

    @Override
    public int getTrackIdByMediaFormat(MediaFormat mediaFormat) {
        if (mediaFormat instanceof VideoFormat) {
            if (videoTrackId == -1)
                throw new IllegalStateException("Video track not initialised");
            return videoTrackId;
        } else if (mediaFormat instanceof AudioFormat) {
            if (audioTrackId == -1)
                throw new IllegalStateException("Audio track not initialised");
            return audioTrackId;
        }

        return -1;
    }

    @Override
    public void start() {
        Logger.LOGD(TAG + " start connectedPluginsCount=" + connectedPluginsCount + ", tracksCount=" + tracksCount);
        if (connectedPluginsCount == tracksCount) {

            notReadyMuxer.start();
            muxer = notReadyMuxer;

            for (int track = 0; track < tracksCount; track++) {
                feedMeIfNotDraining();
            }
            progressListener.onMediaDone();
        }
    }

    @Override
    public boolean canConnectFirst(IOutputRaw connector) {
        return true;
    }

    @Override
    public void fillCommandQueues() {
    }

    public void close() throws IOException {
        closeRender();
    }

    private void closeRender() {
        Logger.LOGD(TAG + " closeRender");
        if (muxer != null) {
            try {
                muxer.stop();
                muxer.release();
                muxer = null;
            } catch (Exception e) {
                if (zeroFramesReceived == false) {

                    throw new RuntimeException("Failed to close the render.", e);
                }
            }
        }
    }

    // workaround. 快速点击，会导致muxer没有写入任何数据，stop时报错。
    private void writeEnd() {
        int bufferSize = 1024 * 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
        Frame frame = new Frame(buffer, bufferSize, 0, 0, 0, 1);
        IMediaCodec.BufferInfo bufferInfo = new IMediaCodec.BufferInfo();
        bufferInfo.flags = frame.getFlags();
        bufferInfo.presentationTimeUs = frame.getSampleTime();
        bufferInfo.size = frame.getLength();
        muxer.writeSampleData(frame.getTrackId(), frame.getByteBuffer(), bufferInfo);

        frame.setFlags(MediaCodec.BUFFER_FLAG_KEY_FRAME);
        bufferInfo.flags = frame.getFlags();
        bufferInfo.presentationTimeUs = frame.getSampleTime();
        bufferInfo.size = frame.getLength();
        muxer.writeSampleData(frame.getTrackId(), frame.getByteBuffer(), bufferInfo);

        frame.setFlags(MediaCodec.BUFFER_FLAG_END_OF_STREAM);
        bufferInfo.flags = frame.getFlags();
        bufferInfo.presentationTimeUs = frame.getSampleTime();
        bufferInfo.size = frame.getLength();
        muxer.writeSampleData(frame.getTrackId(), frame.getByteBuffer(), bufferInfo);

        // write audio
        Frame frame2 = new Frame(buffer, bufferSize, 0, 0, 0, 0);
        bufferInfo.flags = frame2.getFlags();
        bufferInfo.presentationTimeUs = frame2.getSampleTime();
        bufferInfo.size = frame2.getLength();
        muxer.writeSampleData(frame2.getTrackId(), frame2.getByteBuffer(), bufferInfo);

    }
}
