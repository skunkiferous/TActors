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

import org.agilewiki.jactor.api.RequestBase;

import com.blockwithme.tactors.TMailbox;
import com.blockwithme.tactors.TRequest;
import com.blockwithme.time.Time;
import com.blockwithme.time.Timeline;

/**
 * Abstract TRequest base class.
 *
 * @author monster
 */
public abstract class TRequestBase<RESPONSE_TYPE> extends
        RequestBase<RESPONSE_TYPE> implements TRequest<RESPONSE_TYPE> {

    /** The creation time. */
    private final Time creationTime;

    /**
     * @param _targetMailbox The target Mailbox
     * @param theCreationTime The creation time, coming from the *source* Mailbox
     */
    protected TRequestBase(final TMailbox _targetMailbox,
            final Time theCreationTime) {
        super(_targetMailbox);
        creationTime = theCreationTime;
    }

    /**
     * @param _targetMailbox The target Mailbox
     * @param timeline The timeline, coming from the *source* Mailbox
     */
    protected TRequestBase(final TMailbox _targetMailbox,
            final Timeline timeline) {
        super(_targetMailbox);
        creationTime = (timeline == null) ? null : timeline.lastTick();
    }

    /** @see org.agilewiki.jactor.Request#getMailbox() */
    @Override
    public TMailbox getMailbox() {
        return (TMailbox) super.getMailbox();
    }

    @Override
    public Time creationTime() {
        return creationTime;
    }
}
