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

import org.agilewiki.jactor.api.Mailbox;
import org.agilewiki.jactor.impl.JAMailbox;

/**
 * Base interface for all temporal mailboxes.
 *
 * The main reason for Mailboxes to be TimeSource, is that the logical time
 * must not change while the Mailbox is processing a request/response.
 * The time is effectively "frozen". Because of this, the TimeSource methods
 * are NOT thread-safe in Mailbox, and can only be called from within the Mailbox.
 *
 * TODO Now that all Mailbox are weakly referenced, we could add lifecycles to them.
 *
 * @author monster
 */
public interface TMailbox extends JAMailbox {

    /** @see org.agilewiki.jactor.api.Mailbox#getMailboxFactory() */
    @Override
    TMailboxFactory getMailboxFactory();

    /** @see org.agilewiki.jactor.impl.JAMailbox#createPort(Mailbox, int) */
    @Override
    TMailbox createPort(final Mailbox _source, int size);

    /**
     * Generates the next Actor ID, register that actor under that ID,
     * and returns the ID. If the actor has a non-null name, it will be
     * registered too. Optionally, the actor can be pinned.
     */
    long nextActorID(TActor actor, boolean pin);

    /**
     * Returns the Mailbox owner. This is the first actor created for this
     * Mailbox. The reference prevents the owner from being CGed, as long as
     * something else references the Mailbox.
     */
    MBOwner owner();
}
