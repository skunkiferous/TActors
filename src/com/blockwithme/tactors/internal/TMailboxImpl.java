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

import java.util.concurrent.atomic.AtomicReference;

import org.agilewiki.pactor.Mailbox;
import org.agilewiki.pamailbox.MailboxImpl;
import org.agilewiki.pamailbox.Message;
import org.agilewiki.pamailbox.MessageQueue;
import org.slf4j.Logger;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.blockwithme.tactors.MBOwner;
import com.blockwithme.tactors.TActor;
import com.blockwithme.tactors.TMailbox;
import com.blockwithme.tactors.TMailboxFactory;
import com.blockwithme.util.CurrentTimeNanos;
import com.blockwithme.util.NanoClock;

/**
 * TMailboxImpl implements the TMailbox interface.
 *
 * @author monster
 */
public class TMailboxImpl extends MailboxImpl implements TMailbox {

    /** System Default ZoneId */
    private static final ZoneId LOCAL = ZoneId.systemDefault();

    /** The cached local time in nanos. */
    private long localTime;

    /** The cached Instant. */
    private Instant instant;

    /** The cached local time in nanos, as a ZonedDateTime. */
    private ZonedDateTime localNow;

    /** The cached UTC time in nanos, as a ZonedDateTime. */
    private ZonedDateTime utcNow;

    /** The cached logical time. */
    private long logicalTime;

    /** Was the time cached? */
    private boolean timeCached;

    /** The owner. */
    private final AtomicReference<MBOwner<?>> owner = new AtomicReference<>();

    /**
     * @param _mayBlock
     * @param _onIdle
     * @param _messageProcessor
     * @param factory
     * @param messageQueue
     * @param _log
     * @param _initialBufferSize
     */
    public TMailboxImpl(final boolean _mayBlock, final Runnable _onIdle,
            final Runnable _messageProcessor,
            final TMailboxFactoryImpl<?> factory,
            final MessageQueue messageQueue, final Logger _log,
            final int _initialBufferSize) {
        super(_mayBlock, _onIdle, _messageProcessor, factory, messageQueue,
                _log, _initialBufferSize);
    }

    @Override
    public final TMailboxFactory getMailboxFactory() {
        return (TMailboxFactory) super.getMailboxFactory();
    }

    @Override
    public final TMailboxImpl createPort(final Mailbox _source, final int size) {
        return (TMailboxImpl) super.createPort(_source, size);
    }

    /** Called after running processXXXMessage(Message). */
    @Override
    protected void afterProcessMessage(final boolean request,
            final Message message) {
        timeCached = false;
    }

    /** Caches the time */
    private void cacheTime() {
        if (!timeCached) {
            timeCached = true;
            final TMailboxFactory fac = getMailboxFactory();
            localTime = fac.currentTimeNanos(false);
            logicalTime = fac.logicalTime();
            localNow = utcNow = null;
            instant = null;
        }
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TimeSource#realTime()
     */
    @Override
    public final long currentTimeNanos(final boolean utc) {
        cacheTime();
        return utc ? (localTime + CurrentTimeNanos.getLocalToUTCOffsetNS())
                : localTime;
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TimeSource#now(boolean)
     */
    @Override
    public final ZonedDateTime now(final boolean utc) {
        // both instant, utcNow and localNow are lazy-created, but based on frozen time.
        if (instant == null) {
            instant = NanoClock.instant(localTime);
        }
        if (utc) {
            if (utcNow == null) {
                utcNow = instant.atZone(ZoneOffset.UTC);
            }
            return utcNow;
        }
        if (localNow == null) {
            localNow = instant.atZone(LOCAL);
        }
        return utcNow;
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TimeSource#logicalTime()
     */
    @Override
    public final long logicalTime() {
        cacheTime();
        return logicalTime;
    }

    @Override
    public final MBOwner<?> owner() {
        return owner.get();
    }

    /**
     * Generates the next Actor ID, register that actor under that ID,
     * and returns the ID. If the actor has a non-null name, it will be
     * registered too.
     */
    @Override
    public final long nextActorID(final TActor<?> actor, final boolean pin) {
        if (actor instanceof MBOwner<?>) {
            // owner will only be set for the first registered actor.
            owner.compareAndSet(null, (MBOwner<?>) actor);
        } else if (owner.get() == null) {
            throw new IllegalStateException(
                    "First registered actor is not a MBOwner");
        }
        final long result = getMailboxFactory().nextActorID(actor, pin);
        return result;
    }
}
