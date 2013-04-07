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

import com.carrotsearch.hppc.LongObjectOpenHashMap;

/**
 * Weak object cache, where each object has a unique, immutable long ID.
 * No hard reference is kept for the object, therefore allowing GC.
 *
 * TODO Can we make this thread-safe without synchronization?
 *
 * @author monster
 */
public class LongObjectCache<E> {

    /** WeakReference with ID, so we can do quick remove on GC. */
    private static final class WeakReferenceWithID<E> extends WeakReference<E> {

        public final long id;

        /**
         * @param referent
         */
        public WeakReferenceWithID(final long theID, final E obj,
                final ReferenceQueue<? super E> queue) {
            super(obj, queue);
            id = theID;
        }
    }

    /** The reference queue */
    private final ReferenceQueue<E> queue = new ReferenceQueue<E>();

    /** The map. */
    private final LongObjectOpenHashMap<WeakReferenceWithID<E>> map = new LongObjectOpenHashMap<WeakReferenceWithID<E>>();

    @SuppressWarnings("unchecked")
    private void processQueue() {
        WeakReferenceWithID<E> ref;
        while ((ref = (WeakReferenceWithID<E>) queue.poll()) != null) {
            map.remove(ref.id);
        }
    }

    /** Returns the object with the ID, if any. */
    public E findObject(final long id) {
        synchronized (this) {
            processQueue();
            final WeakReferenceWithID<E> ref = map.get(id);
            return (ref == null) ? null : ref.get();
        }
    }

    /** Adds the Object with the ID. */
    public void cacheObject(final long id, final E obj) {
        synchronized (this) {
            final WeakReferenceWithID<E> ref = new WeakReferenceWithID<E>(id,
                    obj, queue);
            if (!map.putIfAbsent(id, ref)) {
                throw new IllegalStateException("ID " + id + " already in use!");
            }
        }
    }
}
