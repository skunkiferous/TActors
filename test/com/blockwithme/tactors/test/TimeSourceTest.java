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
package com.blockwithme.tactors.test;

import junit.framework.TestCase;

import com.blockwithme.tactors.UpdatableTimeSource;
import com.blockwithme.tactors.internal.DefaultTimeSourceImpl;

/**
 * Test code.
 */
public class TimeSourceTest extends TestCase {

    private UpdatableTimeSource timeSource;

    @Override
    protected void setUp() {
        timeSource = new DefaultTimeSourceImpl();
    }

    @Override
    protected void tearDown() throws Exception {
        timeSource = null;
    }

    public void testCurrentTimeNanos() throws Exception {
        // TODO
        throw new IllegalStateException("TODO testCurrentTimeNanos()");
    }

    public void testNow() throws Exception {
        // TODO
        throw new IllegalStateException("TODO testNow()");
    }

    public void testLogicalTime() throws Exception {
        // TODO
        throw new IllegalStateException("TODO testLogicalTime()");
    }
}
