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

import org.m4m.AudioFormat;
import org.m4m.domain.AudioEncoder;
import org.m4m.domain.Encoder;
import org.m4m.domain.ICaptureSource;
import org.m4m.domain.ICommandProcessor;
import org.m4m.domain.IInputRaw;
import org.m4m.domain.IMicrophoneSource;
import org.m4m.domain.IOutputRaw;
import org.m4m.domain.IPluginOutput;
import org.m4m.domain.IsConnectable;
import org.m4m.domain.PassThroughPlugin;
import org.m4m.domain.Render;
import org.m4m.domain.VideoEncoder;

import java.util.Collection;
import java.util.LinkedList;

import static org.m4m.domain.pipeline.ManyToOneConnectable.ManyToOneConnections;
import static org.m4m.domain.pipeline.OneToOneConnectable.OneToOneConnection;

public class ConnectorFactory {
    private final ICommandProcessor commandProcessor;
    private final AudioFormat audioMediaFormat;

    public ConnectorFactory(ICommandProcessor commandProcessor, AudioFormat audioMediaFormat) {
        this.commandProcessor = commandProcessor;
        this.audioMediaFormat = audioMediaFormat;
    }

    public void connect(IOutputRaw source, IInputRaw transform) {

        if (source instanceof ICaptureSource && transform instanceof Encoder) {
            new PluginConnector(commandProcessor).connect((ICaptureSource) source, (Encoder) transform);
            return;
        }

        if (source instanceof IMicrophoneSource && transform instanceof AudioEncoder) {
            new PluginConnector(commandProcessor).connect((IMicrophoneSource) source, (AudioEncoder) transform);
            return;
        }

        if (source instanceof IPluginOutput && transform instanceof Render) {
            new PluginConnector(commandProcessor).connect((IPluginOutput) source, (Render) transform);
            return;
        }

        throw new RuntimeException("No connection between " + source.getClass().toString() + " and " + transform.getClass().toString());
    }

    public Collection<IsConnectable> createConnectionRules() {
        Collection<IsConnectable> collection = new LinkedList<IsConnectable>();
        collection.add(OneToOneConnection(ICaptureSource.class, VideoEncoder.class));
        collection.add(OneToOneConnection(VideoEncoder.class, Render.class));
        collection.add(OneToOneConnection(IMicrophoneSource.class, AudioEncoder.class));

        //collection.add(OneToOneConnection(MultipleMediaSource.class, Render.class));

        collection.add(ManyToOneConnections(
            new ManyTypes(
                PassThroughPlugin.class,
                Encoder.class,
                AudioEncoder.class),
            Render.class));

        return collection;
    }
}
