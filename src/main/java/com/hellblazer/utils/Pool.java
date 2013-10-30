/** (C) Copyright 2013 Hal Hildebrand, All Rights Reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package com.hellblazer.utils;

import java.util.concurrent.locks.ReentrantLock;

import com.hellblazer.utils.collections.RingBuffer;

/**
 * @author hhildebrand
 *
 */
/**
 * A thread safe pooling implementation. Keeps statistics on what shit be
 * happening.
 * 
 * @author hhildebrand
 * 
 */
public class Pool<T> {
    public interface Clearable {
        void clear();
    }

    public interface Factory<T> {
        T newInstance(Pool<T> pool);
    }

    private final ReentrantLock lock      = new ReentrantLock();
    private int                 created   = 0;
    private int                 discarded = 0;
    private final Factory<T>    factory;
    private final String        name;
    private final RingBuffer<T> pool;
    private int                 pooled    = 0;
    private int                 reused    = 0;

    public Pool(String name, Factory<T> factory, int limit) {
        this.name = name;
        pool = new RingBuffer<T>(limit);
        this.factory = factory;
    }

    public T allocate() {
        final ReentrantLock myLock = lock;
        myLock.lock();
        try {
            T allocated = pool.poll();
            if (allocated == null) {
                created++;
                allocated = factory.newInstance(this);
            } else {
                reused++;
            }
            return allocated;
        } finally {
            myLock.unlock();
        }
    }

    public void free(T free) {
        final ReentrantLock myLock = lock;
        myLock.lock();
        try {
            if (!pool.offer(free)) {
                discarded++;
            } else {
                pooled++;
                if (free instanceof Clearable) {
                    ((Clearable) free).clear();
                }
            }
        } finally {
            myLock.unlock();
        }
    }

    /**
     * @return the created
     */
    public int getCreated() {
        return created;
    }

    /**
     * @return the discarded
     */
    public int getDiscarded() {
        return discarded;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    public int getPooled() {
        return pooled;
    }

    public int size() {
        return pool.size();
    }

    @Override
    public String toString() {
        return String.format("Pool[%s] size: %s reused: %s created: %s pooled: %s discarded: %s",
                             name, size(), reused, created, pooled, discarded);
    }
}