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

import javax.annotation.ParametersAreNonnullByDefault;

import com.blockwithme.tactors.TimeSource;

/**
 * Default implementation for a TimeSource.
 *
 * This class must be thread-safe.
 *
 * @author monster
 */
@ParametersAreNonnullByDefault
public class DefaultTimeSourceImpl implements TimeSource {

    /**
     *
     */
    public DefaultTimeSourceImpl() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TimeSource#realTime()
     */
    @Override
    public long realTime() {
        // TODO Make sure the time never goes backward!
        // TODO Make sure to catch large jumps, and adjust appropriately.
        return System.currentTimeMillis();
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TimeSource#logicalTime()
     */
    @Override
    public long logicalTime() {
        // TODO Implement logical time.
        return realTime();
    }
}
