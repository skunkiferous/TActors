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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.agilewiki.pactor.ExceptionHandler;
import org.agilewiki.pactor.Request;
import org.agilewiki.pactor.RequestBase;
import org.agilewiki.pactor.ResponseProcessor;
import org.agilewiki.pactor.UnboundRequest;
import org.agilewiki.pamailbox.EventResponseProcessor;
import org.agilewiki.pautil.ResponseCounter;

import com.blockwithme.tactors.TActor;
import com.blockwithme.tactors.TMailbox;
import com.google.common.base.Preconditions;

/**
 * Helper class, allowing TActors to manage their listeners.
 *
 * @author monster
 */
public class TActorListenerSupportImpl implements TActorListenerSupport {

    /** WeakReference with topic, so we can do quick remove on GC. */
    private static final class WeakReferenceWithTopic extends
            WeakReference<TActor<?>> {

        public final Object topic;

        /**
         * @param referent
         */
        public WeakReferenceWithTopic(final Object theTopic,
                final TActor<?> obj,
                final ReferenceQueue<? super TActor<?>> queue) {
            super(obj, queue);
            topic = theTopic;
        }
    }

    /** The listeners map. */
    private Map<Object, List<Object>> listeners;

    /** The optional reference queue, for weak listeners. */
    private ReferenceQueue<Object> refQueue;

    /** The actor Mailbox. */
    private final TMailbox mailbox;

    /** Creates a TActorListenerSupport for an actor with the given Mailbox- */
    public TActorListenerSupportImpl(final TMailbox theMailbox) {
        mailbox = Preconditions.checkNotNull(theMailbox);
    }

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
    @Override
    public final void register(final Object topic, final TActor<?> listener,
            final boolean weakRef) {
        Preconditions.checkNotNull(topic, "topic cannot be null");
        Preconditions.checkNotNull(listener, "listener cannot be null");
        if (listeners == null) {
            listeners = new HashMap<Object, List<Object>>();
        }
        List<Object> list = listeners.get(topic);
        if (list == null) {
            list = new ArrayList<Object>();
            listeners.put(topic, list);
        }
        for (final Object obj : list) {
            if (obj == listener) {
                throw new IllegalStateException("listener " + listener
                        + " already registered for topic " + topic);
            } else if (obj instanceof WeakReferenceWithTopic) {
                final WeakReferenceWithTopic ref = (WeakReferenceWithTopic) obj;
                if (ref.get() == listener) {
                    throw new IllegalStateException("listener " + listener
                            + " already registered for topic " + topic);
                }
            }
        }
        if (weakRef) {
            if (refQueue == null) {
                refQueue = new ReferenceQueue<Object>();
            }
            list.add(new WeakReferenceWithTopic(topic, listener, refQueue));
        } else {
            list.add(listener);
        }
    }

    /**
     * Unregisters a listener actor.
     *
     * The registry is NOT thread-safe, and so this can only be called from
     * within the actor mailbox context.
     *
     * @return true on success.
     */
    @Override
    public final boolean unregister(final Object topic, final TActor<?> listener) {
        Preconditions.checkNotNull(topic, "topic cannot be null");
        Preconditions.checkNotNull(listener, "listener cannot be null");
        if (listeners != null) {
            final List<Object> list = listeners.get(topic);
            if (list != null) {
                final int size = list.size();
                for (int i = 0; i < size; i++) {
                    final Object obj = list.get(i);
                    if (obj == listener) {
                        if (list.size() == 1) {
                            listeners.remove(topic);
                        } else {
                            list.remove(i);
                        }
                        return true;
                    } else if (obj instanceof WeakReferenceWithTopic) {
                        final WeakReferenceWithTopic ref = (WeakReferenceWithTopic) obj;
                        if (ref.get() == listener) {
                            if (list.size() == 1) {
                                listeners.remove(topic);
                            } else {
                                list.remove(i);
                            }
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Process the queue of weak references to listeners.
     *
     * The registry is NOT thread-safe, and so this can only be called from
     * within the actor mailbox context.
     *
     * The queue is processed automatically, when listenersFor() finds a GCed listener.
     */
    @Override
    public final void processWeakListenerQueue() {
        if (refQueue != null) {
            WeakReferenceWithTopic ref;
            while ((ref = (WeakReferenceWithTopic) refQueue.poll()) != null) {
                final List<Object> list = listeners.get(ref.topic);
                if (list != null) {
                    for (final Object obj : list) {
                        if (obj == ref) {
                            if (list.size() == 1) {
                                listeners.remove(ref.topic);
                            } else {
                                list.remove(ref);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns all the Listeners to a topic.
     *
     * The registry is NOT thread-safe, and so this can only be called from
     * within the actor mailbox context.
     */
    @Override
    public final List<TActor<?>> listenersFor(final Object topic) {
        Preconditions.checkNotNull(topic, "topic cannot be null");
        if (listeners != null) {
            final List<Object> list = listeners.get(topic);
            if (list != null) {
                final List<TActor<?>> result = new ArrayList<>(list.size());
                boolean processQueue = false;
                for (final Object obj : list) {
                    if (obj instanceof WeakReferenceWithTopic) {
                        final WeakReferenceWithTopic ref = (WeakReferenceWithTopic) obj;
                        final TActor<?> actor = ref.get();
                        if (actor == null) {
                            processQueue = true;
                        } else {
                            result.add(actor);
                        }
                    } else {
                        final TActor<?> actor = (TActor<?>) obj;
                        result.add(actor);
                    }
                }
                if (processQueue) {
                    processWeakListenerQueue();
                }
                return result;
            }
        }
        return Collections.emptyList();
    }

    /** Creates and returns a new Request to perform a registration. */
    @Override
    public final Request<Void> registerRequest(final Object topic,
            final TActor<?> listener, final boolean weakRef) {
        return new RequestBase<Void>(mailbox) {
            @Override
            public void processRequest(final ResponseProcessor<Void> _rp)
                    throws Exception {
                register(topic, listener, weakRef);
                _rp.processResponse(null);
            }
        };
    }

    /** Creates and returns a new Request to perform an un-registration. */
    @Override
    public final Request<Void> unregisterRequest(final Object topic,
            final TActor<?> listener) {
        return new RequestBase<Void>(mailbox) {
            @Override
            public void processRequest(final ResponseProcessor<Void> _rp)
                    throws Exception {
                unregister(topic, listener);
                _rp.processResponse(null);
            }
        };
    }

    /**
     * Inform the listeners of a topic about an event (Request).
     *
     * rp can be null, if not response is desired.
     *
     * The registry is NOT thread-safe, and so this can only be called from
     * within the actor mailbox context.
     */
    @Override
    public final <TARGET_ACTOR_TYPE extends TActor<?>> void informListeners(
            final Object topic,
            final UnboundRequest<Void, TARGET_ACTOR_TYPE> event,
            final ResponseProcessor<Void> rp) throws Exception {
        final List<TActor<?>> actors = listenersFor(topic);
        if ((rp == null) || (rp == EventResponseProcessor.SINGLETON)) {
            for (final TActor<?> actor : actors) {
                @SuppressWarnings("unchecked")
                final TARGET_ACTOR_TYPE listener = (TARGET_ACTOR_TYPE) actor;
                event.signal(listener);
            }
        } else {
            final ResponseCounter<Void> rc = new ResponseCounter<Void>(
                    actors.size(), null, rp);
            mailbox.setExceptionHandler(new ExceptionHandler() {
                @Override
                public void processException(final Throwable throwable)
                        throws Exception {
                    rc.decrementCount();
                }
            });
            for (final TActor<?> actor : actors) {
                @SuppressWarnings("unchecked")
                final TARGET_ACTOR_TYPE listener = (TARGET_ACTOR_TYPE) actor;
                event.send(mailbox, listener, rc);
            }
        }
    }

    /**
     * Creates a new Request, to inform the listeners of a topic about an event (Request).
     */
    @Override
    public final <TARGET_ACTOR_TYPE extends TActor<?>> Request<Void> informListenersRequest(
            final Object topic,
            final UnboundRequest<Void, TARGET_ACTOR_TYPE> event) {
        return new RequestBase<Void>(mailbox) {
            @Override
            public void processRequest(final ResponseProcessor<Void> _rp)
                    throws Exception {
                informListeners(topic, event, _rp);
            }
        };
    }
}
