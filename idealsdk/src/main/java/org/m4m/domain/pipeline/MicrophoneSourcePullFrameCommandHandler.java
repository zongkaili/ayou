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

package org.m4m.domain.pipeline;

import org.m4m.domain.AudioEncoder;
import org.m4m.domain.Command;
import org.m4m.domain.Frame;
import org.m4m.domain.ICommandHandler;
import org.m4m.domain.IMicrophoneSource;

import com.idealsee.sdk.util.Logger;

class MicrophoneSourcePullFrameCommandHandler implements ICommandHandler {
	private static final String TAG = "MicrophoneSourcePullFrameCommandHandler";
    private IMicrophoneSource source;
    private AudioEncoder encoder;

    public MicrophoneSourcePullFrameCommandHandler(IMicrophoneSource source, AudioEncoder encoder) {
        super();
        this.source = source;
        this.encoder = encoder;
    }

    @Override
    public void handle() {
        Logger.LOGD("7+");
        Frame encoderFrame = encoder.findFreeFrame();
        if (encoderFrame == null) {
            handleNoFreeInputBuffer();
            Logger.LOGD("7++");
            return;
        }

        Logger.LOGD(TAG + " handle queue nextPair");
        // Workaround. For timely handling of ready video frames we have to switch on next pair.
        source.getOutputCommandQueue().queue(Command.NextPair, encoder.getTrackId());

        source.pull(encoderFrame);
        encoder.push(encoderFrame);

        if (!encoderFrame.equals(Frame.EOF())) {
            Logger.LOGD(TAG + " hanlde");
            encoder.checkIfOutputQueueHasData();
        }
        Logger.LOGD("7-");
    }

    private void handleNoFreeInputBuffer() {
        source.getOutputCommandQueue().queue(Command.HasData, encoder.getTrackId());
        encoder.skipProcessing();
        encoder.getInputCommandQueue().queue(Command.NeedData, encoder.getTrackId());
    }
}
