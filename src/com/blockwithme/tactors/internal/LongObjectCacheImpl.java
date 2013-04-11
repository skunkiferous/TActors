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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.carrotsearch.hppc.LongObjectOpenHashMap;

/**
 * Weak object cache, where each object has a unique, immutable long ID.
 * No hard reference is kept for the object, therefore allowing GC.
 *
 * @author monster
 */
public class LongObjectCacheImpl<E> extends ReferenceQueue<E> implements
        LongObjectCache<E> {

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

    /** Number of reads allowed, before forcing a processQueue(). */
    private static final int READS_BEFORE_PROCESS_QUEUE = 1000;

    /** The map. */
    private final LongObjectOpenHashMap<WeakReferenceWithID<E>> map = new LongObjectOpenHashMap<WeakReferenceWithID<E>>();

    /** Lock for accessing the map, which is not thread-safe. */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /** Reads since last processQueue(). */
    private int readsSinceLastProcessQueue;

    @SuppressWarnings("unchecked")
    private void processQueue() {
        readsSinceLastProcessQueue = 0;
        WeakReferenceWithID<E> ref;
        while ((ref = (WeakReferenceWithID<E>) poll()) != null) {
            map.remove(ref.id);
        }
    }

    /** Returns the object with the ID, if any. */
    @Override
    public E findObject(final long id) {
        final E result;
        boolean processQueue = false;
        lock.readLock().lock();
        try {
            final WeakReferenceWithID<E> ref = map.get(id);
            result = (ref == null) ? null : ref.get();
            // OK. The next line is a write within a readLock(), which is bad,
            // but for our purpose it does not mater.
            if (++readsSinceLastProcessQueue >= READS_BEFORE_PROCESS_QUEUE) {
                processQueue = true;
            }
        } finally {
            lock.readLock().unlock();
        }
        if (processQueue) {
            lock.writeLock().lock();
            try {
                processQueue();
            } finally {
                lock.writeLock().unlock();
            }
        }
        return result;
    }

    /** Adds the Object with the ID. */
    @Override
    public void cacheObject(final long id, final E obj) {
        lock.writeLock().lock();
        try {
            processQueue();
            final WeakReferenceWithID<E> ref = new WeakReferenceWithID<E>(id,
                    obj, this);
            if (!map.putIfAbsent(id, ref)) {
                throw new IllegalStateException("ID " + id + " already in use!");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}
