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

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

/**
 * 
 * @author hhildebrand
 * 
 */
public class SampleWindowTest {
    @Test
    public void testNonVarying() {
        int windowSize = 1000;
        SampleWindow window = new SampleWindow(windowSize);
        for (int i = 0; i < 5 * windowSize; i++) {
            window.sample(1);
        }
        assertEquals(1, (int) window.getMedian());
        assertEquals(1, (int) window.getPercentile(0.9));
    }

    @Test
    public void testRandom() {
        Random r = new Random(666);
        int windowSize = 1000;
        ArrayDeque<Integer> deque = new ArrayDeque<Integer>();
        SampleWindow window = new SampleWindow(windowSize);
        for (int i = 0; i < 5 * windowSize; i++) {
            int data = r.nextInt();
            window.sample(data);
            deque.add(data);
            if (deque.size() > windowSize) {
                deque.removeFirst();
            }
        }
        int[] reference = new int[deque.size()];
        int index = 0;
        for (int i : deque) {
            reference[index++] = i;
        }
        Arrays.sort(reference);
        assertEquals(reference[windowSize / 2], (int) window.getMedian());
    }
}