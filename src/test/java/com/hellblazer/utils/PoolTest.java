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

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.hellblazer.utils.Pool.Factory;

/**
 * @author hhildebrand
 * 
 */
public class PoolTest {
    @Test
    public void testFactory() {
        final AtomicInteger count = new AtomicInteger(0);
        Pool<String> test = new Pool<String>("test-me", new Factory<String>() {
            @Override
            public String newInstance(Pool<String> pool) {
                return String.format("created: ", count.incrementAndGet());
            }
        }, 100);

        for (int i = 0; i < 100; i++) {
            assertEquals(String.format("created: ", i), test.allocate());
        }

        assertEquals(100, test.getCreated());

    }

    @Test
    public void testFree() {
        final AtomicInteger count = new AtomicInteger(0);
        Pool<String> test = new Pool<String>("test-me", new Factory<String>() {
            @Override
            public String newInstance(Pool<String> pool) {
                return String.format("created: ", count.incrementAndGet());
            }
        }, 100);

        for (int i = 0; i < 100; i++) {
            test.free(String.format("freed: ", i));
        }
        assertEquals(0, test.getCreated());
        assertEquals(100, test.getPooled());

        test.free(String.format("freed: ", 100));

        assertEquals(100, test.getPooled());
        assertEquals(100, test.size());
        assertEquals(1, test.getDiscarded());

        for (int i = 0; i < 100; i++) {
            assertEquals(String.format("freed: ", i), test.allocate());
        }

        assertEquals(0, test.size());
        assertEquals(0, test.getCreated());

        assertEquals(String.format("created: ", 0), test.allocate());

        assertEquals(1, test.getCreated());
        assertEquals(0, test.size());
    }
}