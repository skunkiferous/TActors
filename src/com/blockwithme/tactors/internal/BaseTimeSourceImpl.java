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

import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.blockwithme.tactors.TimeSource;
import com.blockwithme.time.CurrentTimeNanos;
import com.blockwithme.time.NanoClock;

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

    /** The UTC clock. */
    private static final Clock UTC = NanoClock.systemUTC();

    /** The LOCAL clock. */
    private static final Clock LOCAL = NanoClock.systemDefaultZone();

    /**
     * Returns the local/UTC time, in nano-seconds.
     * It will never go backward, but might not be updated outside of a
     * logical time update.
     */
    @Override
    public final long currentTimeNanos(final boolean utc) {
        return utc ? CurrentTimeNanos.utcTimeNanos() : CurrentTimeNanos
                .localTimeNanos();
    }

    /**
     * Returns the local/UTC time, in nano-seconds, as an ZonedDateTime.
     * It will never go backward, but might not be updated outside of a
     * logical time update.
     */
    @Override
    public final ZonedDateTime now(final boolean utc) {
        return utc ? UTC.instant().atZone(UTC.getZone()) : LOCAL.instant()
                .atZone(LOCAL.getZone());
    }
}
