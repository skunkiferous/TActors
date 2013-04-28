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

import java.util.List;

import org.agilewiki.pactor.api.Request;
import org.agilewiki.pactor.api.ResponseProcessor;
import org.agilewiki.pactor.api.UnboundRequest;

import com.blockwithme.tactors.TActor;

/**
 * Helper class, allowing TActors to manage their listeners.
 *
 * @author monster
 */
public interface TActorListenerSupport {

    /**
     * Registers a listener actor.
     *
     * The registry is NOT thread-safe, and so this can only be called from
     * within the actor mailbox context.
     *
     * The topic might be anything, but should be immutable.
     * Remember that the registry is also used by base classes and derived classes.
     * The topic cannot be null. The listener cannot be null.
     * The listener must not already have been registered to this topic.
     */
    void register(final Object topic, final TActor<?> listener,
            final boolean weakRef);

    /**
     * Unregisters a listener actor.
     *
     * The registry is NOT thread-safe, and so this can only be called from
     * within the actor mailbox context.
     *
     * @return true on success.
     */
    boolean unregister(final Object topic, final TActor<?> listener);

    /**
     * Process the queue of weak references to listeners.
     *
     * The registry is NOT thread-safe, and so this can only be called from
     * within the actor mailbox context.
     *
     * The queue is processed automatically, when listenersFor() finds a GCed listener.
     */
    void processWeakListenerQueue();

    /**
     * Returns all the Listeners to a topic.
     *
     * The registry is NOT thread-safe, and so this can only be called from
     * within the actor mailbox context.
     */
    List<TActor<?>> listenersFor(final Object topic);

    /** Creates and returns a new Request to perform a registration. */
    Request<Void> registerRequest(final Object topic, final TActor<?> listener,
            final boolean weakRef);

    /** Creates and returns a new Request to perform an un-registration. */
    Request<Void> unregisterRequest(final Object topic, final TActor<?> listener);

    /**
     * Inform the listeners of a topic about an event (Request).
     *
     * rp can be null, if not response is desired.
     *
     * The registry is NOT thread-safe, and so this can only be called from
     * within the actor mailbox context.
     */
    <TARGET_ACTOR_TYPE extends TActor<?>> void informListeners(
            final Object topic,
            final UnboundRequest<Void, TARGET_ACTOR_TYPE> event,
            final ResponseProcessor<Void> rp) throws Exception;

    /**
     * Creates a new Request, to inform the listeners of a topic about an event (Request).
     */
    <TARGET_ACTOR_TYPE extends TActor<?>> Request<Void> informListenersRequest(
            final Object topic,
            final UnboundRequest<Void, TARGET_ACTOR_TYPE> event);
}
