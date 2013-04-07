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

/**
 * A TimeSource that allows updating the logical time.
 *
 * @author monster
 */
public interface UpdatableTimeSource extends TimeSource {
    /**
     * Set the logical time.
     * In a running application, this could cause problems.
     */
    void setLogicalTime(long newTime);

    /**
     * Set the logical time, but only if it has the expected old value.
     * In a running application, this could cause problems.
     * @return true, if the logical time was updated.
     */
    boolean setLogicalTime(long oldTime, long newTime);

    /**
     * Modifies the logical time by the give number of units,
     * and returns the new value.
     */
    long offsetLogicalTime(long delta);
}
