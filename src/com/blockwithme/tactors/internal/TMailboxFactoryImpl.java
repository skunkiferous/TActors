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
import org.threeten.bp.ZonedDateTime;

import com.blockwithme.tactors.TActor;
import com.blockwithme.tactors.TMailbox;
import com.blockwithme.tactors.TMailboxFactory;
import com.blockwithme.tactors.TimeSource;
import com.blockwithme.util.LongObjectCache;
import com.google.common.base.Preconditions;

/**
 * TMailboxFactoryImpl implements the TMailboxFactory interface.
 *
 * @author monster
 */
public class TMailboxFactoryImpl<M extends TMailbox> extends
        DefaultMailboxFactoryImpl<M> implements TMailboxFactory {

    /** The globally unique ID for this TMailboxFactory. */
    private final long id;

    /** The TimeSource to use. */
    private final TimeSource timeSource;

    /** The Mailbox ID counter. */
    private final AtomicLong nextID = new AtomicLong();

    /** All the actors. */
    private final LongObjectCache<TActor<?>> actors;

    /** Constructor */
    public TMailboxFactoryImpl(final long theID,
            final TimeSource theTimeSource,
            final LongObjectCache<TActor<?>> theCache) {
        id = theID;
        timeSource = Preconditions.checkNotNull(theTimeSource, "theTimeSource");
        actors = Preconditions.checkNotNull(theCache, "theCache");
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TimeSource#currentTimeNanos(boolean)
     */
    @Override
    public final long currentTimeNanos(final boolean utc) {
        return timeSource.currentTimeNanos(utc);
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TimeSource#now(boolean)
     */
    @Override
    public final ZonedDateTime now(final boolean utc) {
        return timeSource.now(utc);
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TimeSource#logicalTime()
     */
    @Override
    public final long logicalTime() {
        return timeSource.logicalTime();
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TMailboxFactory#id()
     */
    @Override
    public final long id() {
        return id;
    }

    @Override
    public long nextActorID(final TActor<?> actor, final boolean pin) {
        if (actor.id() != 0) {
            throw new IllegalStateException("Actor already registered! "
                    + actor);
        }
        final long result = nextID.incrementAndGet();
        if (result == 0) {
            // This is NEVER going to happen!
            throw new InternalError("Maximum valid Actor ID exceeded!");
        }
        actors.cacheObject(result, actor.name(), actor, pin);
        return result;
    }

    @Override
    public TActor<?> findActor(final long actorID) {
        return actors.findObject(actorID);
    }

    @Override
    public TActor<?> findActor(final String name) {
        return actors.findObject(name);
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
                this, messageQueue, _log, _initialBufferSize);
    }
}
