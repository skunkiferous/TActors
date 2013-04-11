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

import java.util.concurrent.atomic.AtomicReference;

import com.blockwithme.tactors.UpdatableTimeSource;

/**
 * Default implementation for a UpdatableTimeSource.
 *
 * This class must be thread-safe.
 *
 * @author monster
 */
public class DefaultTimeSourceImpl extends BaseTimeSourceImpl implements
        UpdatableTimeSource {

    /** System time in nanoseconds, at JVM start. */
    private final AtomicReference<Long> logicalTime = new AtomicReference<Long>();

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TimeSource#logicalTime()
     */
    @Override
    public long logicalTime() {
        final Long result = logicalTime.get();
        if (result == null) {
            throw new IllegalStateException("Logical time not set yet!");
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.UpdatableTimeSource#setLogicalTime(long)
     */
    @Override
    public void setLogicalTime(final long newTime) {
        logicalTime.set(newTime);
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.UpdatableTimeSource#setLogicalTime(long, long)
     */
    @Override
    public boolean setLogicalTime(final long oldTime, final long newTime) {
        return logicalTime.compareAndSet(oldTime, newTime);
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.UpdatableTimeSource#offsetLogicalTime(long)
     */
    @Override
    public long offsetLogicalTime(final long delta) {
        while (true) {
            final long oldTime = logicalTime();
            final long newTime = oldTime + delta;
            if (setLogicalTime(oldTime, newTime)) {
                return logicalTime();
            }
        }
    }
}
