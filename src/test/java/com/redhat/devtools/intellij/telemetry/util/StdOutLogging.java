/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2014 Segment, Inc.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
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
import com.segment.analytics.Log;
import com.segment.analytics.Plugin;
import com.segment.analytics.messages.Message;

/**
 * A {@link Plugin} implementation that redirects client logs to standard output and logs callback
 * events.
 */
public class StdOutLogging implements Plugin {
    @Override
    public void configure(Analytics.Builder builder) {
        builder.log(
                new Log() {
                    @Override
                    public void print(Level level, String format, Object... args) {
                        System.out.println(level + ":\t" + String.format(format, args));
                    }

                    @Override
                    public void print(Level level, Throwable error, String format, Object... args) {
                        System.out.println(level + ":\t" + String.format(format, args));
                        System.out.println(error);
                    }
                });

        builder.callback(
                new Callback() {
                    @Override
                    public void success(Message message) {
                        System.out.println("Uploaded " + message);
                    }

                    @Override
                    public void failure(Message message, Throwable throwable) {
                        System.out.println("Could not upload " + message);
                        System.out.println(throwable);
                    }
                });
    }
}