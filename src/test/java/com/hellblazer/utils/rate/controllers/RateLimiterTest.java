/** (C) Copyright 2015 Chiral Behaviors LLC, All Rights Reserved
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
package com.hellblazer.utils.rate.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 
 * @author hhildebrand
 * 
 */
public class RateLimiterTest {

    @Test
    public void testLimiter() throws InterruptedException {
        RateLimiter limiter = new RateLimiter(10.0, 10, 0);
        assertTrue(limiter.accept(0));
        assertEquals(9, limiter.getCurrentTokens());
        for (int i = 0; i < 9; i++) {
            assertTrue(limiter.accept(0));
        }
        assertFalse(limiter.accept(0));
        // wait for a token to regenerate 
        assertTrue(limiter.accept(100));
        // clear any remaining tokens 
        for (int i = 0; i < limiter.getCurrentTokens(); i++) {
            assertTrue(limiter.accept(100));
        }
        assertEquals(0, limiter.getCurrentTokens());

        limiter.setTargetRate(2);
        // Only one token should regenerate
        assertTrue(limiter.accept(1000));
        assertFalse(limiter.accept(1000));
    }
}