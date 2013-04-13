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
package com.blockwithme.tactors;

import org.threeten.bp.ZonedDateTime;

/**
 * Some entity that has it's own notion of time.
 *
 * TODO Most likely, we would want some kind of listener mechanism,
 * to possible time updates. Something like the java.util.Timer Class.
 *
 * @author monster
 */
public interface TimeSource {
    /**
     * Returns the local/UTC time, in nano-seconds.
     * It will never go backward, but might not be updated outside of a
     * logical time update.
     */
    long currentTimeNanos(boolean utc);

    /**
     * Returns the local/UTC time, in nano-seconds, as an ZonedDateTime.
     * It will never go backward, but might not be updated outside of a
     * logical time update.
     */
    ZonedDateTime now(boolean utc);

    /**
     * Returns the logical application time.
     * It should can go backward, if the application logic allows it, and the
     * frequency of updates depends on the application, and might even vary in
     * some cases. This method will throw an exception if accessed before the
     * application time was set.
     */
    long logicalTime();
}
