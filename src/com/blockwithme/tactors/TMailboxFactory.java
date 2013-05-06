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

import org.agilewiki.jactor.impl.JAMailboxFactory;

import com.blockwithme.time.ClockServiceSource;

/**
 * The TMailboxFactory produces all Mailboxes.
 *
 * Typically, there would be one per JVM, or one per application, in an application server.
 *
 * @author monster
 */
public interface TMailboxFactory extends JAMailboxFactory, ClockServiceSource {

    /** @see org.agilewiki.jactor.api.MailboxFactory#createMailbox() */
    @Override
    TMailbox createMailbox();

    /** @see org.agilewiki.jactor.api.MailboxFactory#createMailbox(boolean) */
    @Override
    TMailbox createMailbox(final boolean mayBlock);

    /** @see org.agilewiki.jactor.api.MailboxFactory#createMailbox(int) */
    @Override
    TMailbox createMailbox(final int initialBufferSize);

    /** @see org.agilewiki.jactor.api.MailboxFactory#createMailbox(boolean, int) */
    @Override
    TMailbox createMailbox(final boolean mayBlock, final int initialBufferSize);

    /** @see org.agilewiki.jactor.api.MailboxFactory#createMailbox(boolean, Runnable) */
    @Override
    TMailbox createMailbox(final boolean mayBlock, final Runnable onIdle);

    /** @see org.agilewiki.jactor.api.MailboxFactory#createMailbox(boolean, int, Runnable) */
    @Override
    TMailbox createMailbox(final boolean mayBlock, final int initialBufferSize,
            final Runnable onIdle);

    /** @see org.agilewiki.jactor.api.MailboxFactory#createThreadBoundMailbox(Runnable) */
    @Override
    TMailbox createThreadBoundMailbox(final Runnable _messageProcessor);

    /**
     * Returns the MailboxFactory ID. It will be within [Long.MIN_VALUE,Long.MAX_VALUE], but not 0.
     *
     * It should be global within each and every JVM used by the application.
     */
    long id();

    /**
     * Generates the next Actor ID, register that actor under that ID,
     * and returns the ID. If the actor has a non-null name, it will be
     * registered too.
     */
    long nextActorID(TActor actor, boolean pin);

    /** Returns the actor with the given ID, if any. */
    TActor findActor(long id);

    /** Returns the actor with the given name, if any. */
    TActor findActor(String name);

}
