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

import org.agilewiki.pactor.Mailbox;
import org.agilewiki.pamailbox.PAMailbox;

/**
 * Base interface for all temporal mailboxes.
 *
 * The main reason for Mailboxes to be TimeSource, is that the logical time
 * must not change while the Mailbox is processing a request/response.
 *
 * TODO Now that all Mailbox are weakly referenced, we could add lifecycles to them.
 *
 * TODO Should a Mailbox be an actor too?
 *
 * @author monster
 */
public interface TMailbox extends PAMailbox, TimeSource {

    /** @see org.agilewiki.pactor.Mailbox#getMailboxFactory() */
    @Override
    TMailboxFactory getMailboxFactory();

    /** @see org.agilewiki.pamailbox.PAMailbox#createPort(Mailbox, int) */
    @Override
    TMailbox createPort(final Mailbox _source, int size);

    /**
     * Returns the Mailbox ID. It will be within [Integer.MIN_VALUE,Integer.MAX_VALUE], but not 0.
     *
     * This is a transient ID generated at runtime. It is guaranteed to be unique within this TSystem instance.
     */
    long id();

    /**
     * Returns the next Actor ID.
     *
     * This method is not thread-safe, and must only be called from within the Mailbox.
     */
    long nextActorID(TActor actor);

    /** Returns the actor with the given ID, if any. */
    TActor findActor(long id);

    /**
     * Returns the frozen (adjusted) real time.
     * It should be in the GMT time zone.
     *
     * Frozen means that while processing a message, the time should not change.
     * This method is not thread-safe, and must only be called from within the Mailbox.
     */
    @Override
    long realTime();

    /**
     * Returns the frozen logical application time.
     *
     * Frozen means that while processing a message, the time should not change.
     * This method is not thread-safe, and must only be called from within the Mailbox.
     */
    @Override
    long logicalTime();
}
