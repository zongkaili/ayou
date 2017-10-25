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

import org.m4m.AudioFormat;
import org.m4m.IProgressListener;
import org.m4m.StreamingParameters;
import org.m4m.domain.graphics.IEglUtil;

import java.io.IOException;

public interface IAndroidMediaObjectFactory {

    VideoEncoder createVideoEncoder();

    AudioEncoder createAudioEncoder(String mimeType);

    Render createSink(String fileName, IProgressListener progressListener, ProgressTracker progressTracker) throws IOException;

    Render createSink(StreamingParameters StreamingParams, IProgressListener progressListener, ProgressTracker progressTracker);

    ICaptureSource createCaptureSource();

    MediaFormat createVideoFormat(String mimeType, int width, int height);

    MediaFormat createAudioFormat(String mimeType, int channelCount, int sampleRate);

    IMicrophoneSource createMicrophoneSource();

    IAudioContentRecognition createAudioContentRecognition();

    IEglContext getCurrentEglContext();

    IEglUtil getEglUtil();

    IFrameBuffer createFrameBuffer();
}
