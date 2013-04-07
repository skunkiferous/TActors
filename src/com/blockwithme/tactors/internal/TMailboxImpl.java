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

import org.agilewiki.pactor.Mailbox;
import org.agilewiki.pamailbox.MailboxImpl;
import org.agilewiki.pamailbox.Message;
import org.agilewiki.pamailbox.MessageQueue;
import org.slf4j.Logger;

import com.blockwithme.tactors.TActor;
import com.blockwithme.tactors.TMailbox;
import com.blockwithme.tactors.TMailboxFactory;

/**
 * TMailboxImpl implements the TMailbox interface.
 *
 * @author monster
 */
public class TMailboxImpl extends MailboxImpl implements TMailbox {

    /** First invalid ID. */
    private static final long INVALID_ID = 0x100000000L;

    /** The cached real time. */
    private long realTime;

    /** The cached logical time. */
    private long logicalTime;

    /** Was the time cached? */
    private boolean timeCached;

    /** The Actor ID counter. */
    private long nextID;

    /** My Mailbox ID. */
    private final long id;

    /** All the actors. */
    private final LongObjectCache<TActor> actors = new LongObjectCache<TActor>();

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
        id = factory.nextMailboxID(this);
    }

    @Override
    public TMailboxFactory getMailboxFactory() {
        return (TMailboxFactory) super.getMailboxFactory();
    }

    @Override
    public TMailboxImpl createPort(final Mailbox _source, final int size) {
        return (TMailboxImpl) super.createPort(_source, size);
    }

    @Override
    public long nextActorID(final TActor actor) {
        final long next = ++nextID;
        if (next >= INVALID_ID) {
            throw new InternalError("Maximum valid Actor ID exceeded!");
        }
        final long result = (next << 32L) | id;
        actors.cacheObject(result, actor);
        return result;
    }

    @Override
    public TActor findActor(final long actorID) {
        return actors.findObject(actorID);
    }

    @Override
    public long id() {
        return id;
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
            realTime = fac.realTime();
            logicalTime = fac.logicalTime();
        }
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TimeSource#realTime()
     */
    @Override
    public long realTime() {
        cacheTime();
        return realTime;
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TimeSource#logicalTime()
     */
    @Override
    public long logicalTime() {
        cacheTime();
        return logicalTime;
    }
}
