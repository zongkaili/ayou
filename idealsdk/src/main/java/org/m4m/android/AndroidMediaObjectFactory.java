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

package org.m4m.android;

import android.content.Context;
import android.media.MediaMuxer;
import android.opengl.EGL14;
import org.m4m.IProgressListener;
import org.m4m.StreamingParameters;
import org.m4m.domain.AudioEncoder;
import org.m4m.domain.IAndroidMediaObjectFactory;
import org.m4m.domain.IAudioContentRecognition;
import org.m4m.domain.ICaptureSource;
import org.m4m.domain.IEglContext;
import org.m4m.domain.IFrameBuffer;
import org.m4m.domain.IMediaFormatWrapper;
import org.m4m.domain.IMicrophoneSource;
import org.m4m.domain.ISurfaceWrapper;
import org.m4m.domain.MediaFormat;
import org.m4m.domain.MuxRender;
import org.m4m.domain.ProgressTracker;
import org.m4m.domain.Render;
import org.m4m.domain.VideoEncoder;

import org.m4m.android.graphics.EglUtil;
import org.m4m.android.graphics.FrameBuffer;
import org.m4m.domain.graphics.IEglUtil;

import java.io.IOException;

public class AndroidMediaObjectFactory implements IAndroidMediaObjectFactory {
    private final Context context;

    MediaCodecEncoderPlugin audioMediaCodec;

    public AndroidMediaObjectFactory(Context context) {
        this.context = context;
    }

    @Override
    public VideoEncoder createVideoEncoder() {
        VideoEncoder videoEncoder = new VideoEncoder(new MediaCodecEncoderPlugin("video/avc", getEglUtil()));
        videoEncoder.setTimeout(getDeviceSpecificTimeout());
        return videoEncoder;
    }

    @Override
    public AudioEncoder createAudioEncoder(String mime) {
        audioMediaCodec = MediaCodecEncoderPlugin.createByCodecName(mime != null ? mime : "audio/mp4a-latm", getEglUtil());
        AudioEncoder audioEncoder = new AudioEncoder(audioMediaCodec);
        audioEncoder.setTimeout(getDeviceSpecificTimeout());
        return audioEncoder;
    }

    @Override
    public Render createSink(String fileName, IProgressListener progressListener, ProgressTracker progressTracker) throws IOException {
        if (fileName != null) {
            return new MuxRender(new MediaMuxerPlugin(fileName, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4), progressListener, progressTracker);
        }
        return null;
    }

    @Override
    public Render createSink(StreamingParameters parameters, IProgressListener progressListener, ProgressTracker progressTracker) {
        // Was disabled publishing
        // return new MuxRender(new MediaStreamerInternal(parameters), progressListener, progressTracker);
        throw new UnsupportedOperationException();
    }

    @Override
    public ICaptureSource createCaptureSource() {
        return new GameCapturerSource();
    }

    @Override
    public MediaFormat createVideoFormat(String mimeType, int width, int height) {
        return new VideoFormatAndroid(mimeType, width, height);
    }

    @Override
    public MediaFormat createAudioFormat(String mimeType, int channelCount, int sampleRate) {
        return new AudioFormatAndroid(mimeType, sampleRate, channelCount);
    }

    @Override
    public IMicrophoneSource createMicrophoneSource() {
        return new MicrophoneSource();
    }

    @Override
    public IAudioContentRecognition createAudioContentRecognition() {
        return new AudioContentRecognition();
    }

    @Override
    public IEglContext getCurrentEglContext() {
        return new EGLContextWrapper(EGL14.eglGetCurrentContext());
    }

    @Override
    public IEglUtil getEglUtil() {
        return EglUtil.getInstance();
    }

    @Override
    public IFrameBuffer createFrameBuffer() {
        return new FrameBuffer(getEglUtil());
    }

    private int getDeviceSpecificTimeout() {
//        if (Build.MANUFACTURER.equals("samsung") && Build.MODEL.equals("SM-N900")) {
//            return 10000;
//        }
        return 10;
    }

    static public class Converter {

        static public ISurfaceWrapper convert(android.view.Surface surface) {
            return new SurfaceWrapper(surface);
        }

        static public IMediaFormatWrapper convert(android.media.MediaFormat mediaFormat) {
            return new MediaFormatWrapper(mediaFormat);
        }
    }
}
