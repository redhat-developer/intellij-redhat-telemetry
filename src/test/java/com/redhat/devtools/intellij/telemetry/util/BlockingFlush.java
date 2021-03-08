/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Segment, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.redhat.devtools.intellij.telemetry.util;

import com.segment.analytics.Analytics;
import com.segment.analytics.Callback;
import com.segment.analytics.MessageTransformer;
import com.segment.analytics.Plugin;
import com.segment.analytics.messages.Message;
import com.segment.analytics.messages.MessageBuilder;

import java.util.concurrent.Phaser;

/**
 * The {@link Analytics} class doesn't come with a blocking {@link Analytics#flush()} implementation
 * out of the box. It's trivial to build one using a {@link Phaser} that monitors requests and is
 * able to block until they're uploaded.
 *
 * <pre><code>
 * BlockingFlush blockingFlush = BlockingFlush.create();
 * Analytics analytics = Analytics.builder(writeKey)
 *      .plugin(blockingFlush.plugin())
 *      .build();
 *
 * // Do some work.
 *
 * analytics.flush(); // Trigger a flush.
 * blockingFlush.block(); // Block until the flush completes.
 * analytics.shutdown(); // Shut down after the flush is complete.
 * </code></pre>
 */
public class BlockingFlush {

    public static BlockingFlush create() {
        return new BlockingFlush();
    }

    BlockingFlush() {
        this.phaser = new Phaser(1);
    }

    final Phaser phaser;

    public Plugin plugin() {
        return new Plugin() {
            @Override
            public void configure(Analytics.Builder builder) {
                builder.messageTransformer(
                        new MessageTransformer() {
                            @Override
                            public boolean transform(MessageBuilder builder) {
                                phaser.register();
                                return true;
                            }
                        });

                builder.callback(
                        new Callback() {
                            @Override
                            public void success(Message message) {
                                phaser.arrive();
                            }

                            @Override
                            public void failure(Message message, Throwable throwable) {
                                phaser.arrive();
                            }
                        });
            }
        };
    }

    public void block() {
        phaser.arriveAndAwaitAdvance();
    }
}