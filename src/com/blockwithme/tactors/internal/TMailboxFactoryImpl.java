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

import java.util.concurrent.atomic.AtomicLong;

import org.agilewiki.pamailbox.DefaultMailboxFactoryImpl;
import org.agilewiki.pamailbox.MessageQueue;
import org.slf4j.Logger;

import com.blockwithme.tactors.TActor;
import com.blockwithme.tactors.TMailbox;
import com.blockwithme.tactors.TMailboxFactory;
import com.blockwithme.tactors.TimeSource;
import com.blockwithme.util.Provider;

/**
 * TMailboxFactoryImpl implements the TMailboxFactory interface.
 *
 * @author monster
 */
public class TMailboxFactoryImpl<M extends TMailbox> extends
        DefaultMailboxFactoryImpl<M> implements TMailboxFactory {

    /** First invalid ID. */
    private static final long INVALID_ID = 0x100000000L;

    /** Mask used to extract the Mailbox ID from an actor ID. */
    private static final long MB_ID_MASK = INVALID_ID - 1L;

    /** The globally unique ID for this TMailboxFactory. */
    private final long id;

    /** The TimeSource to use. */
    private final TimeSource timeSource;

    /** The Mailbox ID counter. */
    private final AtomicLong nextID = new AtomicLong();

    /** The factory. */
    private final Provider<LongObjectCache<TActor>> cacheProvider;

    /** All the Mailboxes. */
    private final LongObjectCache<TMailbox> mailboxes;

    /** Returns a new actor cache. */
    protected LongObjectCache<TActor> newActorCache() {
        return cacheProvider.get();
    }

    /** Constructor */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TMailboxFactoryImpl(final long theID,
            final TimeSource theTimeSource,
            final Provider<LongObjectCache<?>> theCacheProvider) {
        id = theID;
        timeSource = theTimeSource;
        cacheProvider = (Provider) theCacheProvider;
        mailboxes = (LongObjectCache<TMailbox>) theCacheProvider.get();
    }

    /** Returns the next Mailbox ID. */
    public final long nextMailboxID(final TMailbox mailbox) {
        final long result = nextID.incrementAndGet();
        if (result >= INVALID_ID) {
            throw new InternalError("Maximum valid Email ID exceeded!");
        }
        mailboxes.cacheObject(result, mailbox);
        return result;
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TimeSource#realTime()
     */
    @Override
    public final long realTime() {
        return timeSource.realTime();
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TimeSource#logicalTime()
     */
    @Override
    public final long logicalTime() {
        return timeSource.logicalTime();
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TimeSource#nanoTime()
     */
    @Override
    public final long nanoTime() {
        return timeSource.nanoTime();
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TMailboxFactory#id()
     */
    @Override
    public final long id() {
        return id;
    }

    /** Returns the mailbox with the given ID (Mailbox ID or Actor ID), if any. */
    @Override
    public final TMailbox findMailbox(final long mailboxID) {
        return mailboxes.findObject(mailboxID & MB_ID_MASK);
    }

    /** Returns the actor with the given ID, if any. */
    @Override
    public final TActor findActor(final long actorID) {
        final TMailbox mb = findMailbox(actorID);
        return (mb == null) ? null : mb.findActor(actorID);
    }

    /**
     * Actually instantiate the Mailbox.
     * Can be overridden, to create application-specific Mailbox instances.
     * newActorCache() will provide the actor cache.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected M createMailbox(final boolean _mayBlock, final Runnable _onIdle,
            final Runnable _messageProcessor, final MessageQueue messageQueue,
            final Logger _log, final int _initialBufferSize) {
        return (M) new TMailboxImpl(_mayBlock, _onIdle, _messageProcessor,
                this, messageQueue, _log, _initialBufferSize, newActorCache());
    }
}
