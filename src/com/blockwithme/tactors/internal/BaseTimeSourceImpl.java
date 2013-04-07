/*
 * Copyright (C) 2013 Sebastien Diot.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blockwithme.tactors.internal;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;

import com.blockwithme.tactors.TimeSource;

/**
 * Abstract implementation for a TimeSource.
 *
 * This class must be thread-safe.
 *
 * It only tries to return meaningful values for the real time.
 * It hides the fluctuations due to time-servers, DST, ...
 *
 * Since those checks could be expensive, up to inclusive asking
 * a time-server directly, it might make more sense to have a background
 * thread counting the time, and just set an atomic long, every N milliseconds.
 *
 * On the client, we should base our value on the offset of the local clock
 * to the server time.
 *
 * On the server, we probably need some admin work to either get a good time
 * sync to happen automatically, or not use the local clock at all, and have
 * out own time daemon of sorts.
 *
 * Maybe we should only update the real time when the logical time is updated?
 *
 * @author monster
 */
public abstract class BaseTimeSourceImpl implements TimeSource {

    /** The GMT time zone. Should make use immune to DST ... */
    private static final TimeZone GMT_TIME_ZONE = TimeZone.getTimeZone("GMT");

    /** System time in nanoseconds, at JVM start. */
    private static final AtomicLong NANO_TIME_OFFSET = new AtomicLong(
            System.nanoTime());

    /** Time in nanoseconds, at last call. */
    private static final AtomicLong LAST_NANO_TIME = new AtomicLong(
            NANO_TIME_OFFSET.get());

    /** GMT Time in milliseconds, at last call. */
    private static final AtomicLong LAST_GMT_TIME = new AtomicLong(Calendar
            .getInstance(GMT_TIME_ZONE).getTimeInMillis());

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TimeSource#realTime()
     */
    @Override
    public long realTime() {
        while (true) {
            // TODO Make sure to catch large jumps, and adjust appropriately.
            final long last = LAST_GMT_TIME.get();
            final long now = Calendar.getInstance(GMT_TIME_ZONE)
                    .getTimeInMillis();
            if (now >= last) {
                if (LAST_GMT_TIME.compareAndSet(last, now)) {
                    return now;
                }
            } else {
                // Ouch! Time went backward!
                return last;
            }
        }
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TimeSource#nanoTime()
     */
    @Override
    public long nanoTime() {
        while (true) {
            // TODO Make sure to catch large jumps, and adjust appropriately.
            final long last = LAST_NANO_TIME.get();
            final long now = System.nanoTime();
            if (now >= last) {
                if (LAST_NANO_TIME.compareAndSet(last, now)) {
                    return now - NANO_TIME_OFFSET.get();
                }
            } else {
                // Ouch! System.nanoTime() went backward!
                return last - NANO_TIME_OFFSET.get();
            }
        }
    }
}
