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

import org.agilewiki.pactor.api.Mailbox;
import org.agilewiki.pactor.impl.MailboxImpl;
import org.agilewiki.pactor.impl.Message;
import org.agilewiki.pactor.impl.MessageQueue;
import org.slf4j.Logger;

import com.blockwithme.tactors.MBOwner;
import com.blockwithme.tactors.TActor;
import com.blockwithme.tactors.TMailbox;
import com.blockwithme.tactors.TMailboxFactory;

/**
 * TMailboxImpl implements the TMailbox interface.
 *
 * @author monster
 */
public class TMailboxImpl extends MailboxImpl implements TMailbox {

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
