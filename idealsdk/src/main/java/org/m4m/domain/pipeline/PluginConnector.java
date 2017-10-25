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
import org.m4m.domain.CommandHandlerFactory;
import org.m4m.domain.Encoder;
import org.m4m.domain.ICaptureSource;
import org.m4m.domain.ICommandHandler;
import org.m4m.domain.ICommandProcessor;
import org.m4m.domain.IEglContext;
import org.m4m.domain.IHandlerCreator;
import org.m4m.domain.IMicrophoneSource;
import org.m4m.domain.IPluginOutput;
import org.m4m.domain.ISurface;
import org.m4m.domain.ISurfaceListener;
import org.m4m.domain.MediaCodecPlugin;
import org.m4m.domain.OutputInputPair;
import org.m4m.domain.Pair;
import org.m4m.domain.PassThroughPlugin;
import org.m4m.domain.Plugin;
import org.m4m.domain.Render;

class PluginConnector implements IConnector {
    private final ICommandProcessor commandProcessor;

    public PluginConnector(ICommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    private void configureCommandProcessorPushSurfaceDecoderEncoder(final IPluginOutput decoder, final MediaCodecPlugin encoder) {
        CommandHandlerFactory factory = new CommandHandlerFactory();
        factory.register(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new PushSurfaceCommandHandler(decoder, encoder);
            }
        });
        factory.register(new Pair<Command, Integer>(Command.EndOfFile, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new DrainCommandHandler(encoder);
            }

        });
        factory.register(new Pair<Command, Integer>(Command.OutputFormatChanged, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new SkipOutputFormatChangeCommandHandler(encoder);
            }

        });
        commandProcessor.add(new OutputInputPair(decoder, encoder, factory));
    }

    public void connect(final ICaptureSource source, final Encoder encoder) {
        configureCommandProcessorPushSurfaceDecoderEncoder(source, encoder);

        source.addSetSurfaceListener(new ISurfaceListener() {
            @Override
            public void onSurfaceAvailable(IEglContext eglContext) {
                encoder.configure();
                ISurface surface = encoder.getSurface();
                source.setOutputSurface(surface);
                encoder.start();
            }
        });
    }

    public void connect(final IPluginOutput plugin, final Render render) {
        CommandHandlerFactory factory = new CommandHandlerFactory();
        if (plugin instanceof Encoder || plugin instanceof PassThroughPlugin) {
            factory.register(new Pair<Command, Integer>(Command.OutputFormatChanged, 0), new Pair<Command, Integer>(Command.NeedInputFormat, 0), new IHandlerCreator() {
                @Override
                public ICommandHandler create() {
                    return new EncoderMediaFormatChangedCommandHandler((Plugin) plugin, render);
                }

            });
        }
        factory.register(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new PullDataCommandHandler(plugin, render);
            }

        });
        factory.register(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedInputFormat, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new PullDataCommandHandler(plugin, render);
            }

        });
        factory.register(new Pair<Command, Integer>(Command.EndOfFile, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new DrainRenderCommandHandler(render);
            }

        });
        factory.register(new Pair<Command, Integer>(Command.EndOfFile, 0), new Pair<Command, Integer>(Command.NeedInputFormat, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new DrainRenderCommandHandler(render);
            }

        });
        commandProcessor.add(new OutputInputPair(plugin, render, factory));
        render.configure();
    }

    public void connect(final IMicrophoneSource source, final AudioEncoder encoder) {
        CommandHandlerFactory factory = new CommandHandlerFactory();
        factory.register(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new MicrophoneSourcePullFrameCommandHandler(source, encoder);
            }
        });
        factory.register(new Pair<Command, Integer>(Command.OutputFormatChanged, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new SkipOutputFormatChangeCommandHandler(encoder);
            }

        });
        factory.register(new Pair<Command, Integer>(Command.EndOfFile, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new DrainCommandHandler(encoder);
            }
        });
        commandProcessor.add(new OutputInputPair(source, encoder, factory));


        encoder.configure();
        encoder.start();
    }

}
