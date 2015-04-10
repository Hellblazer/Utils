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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;

import com.hellblazer.utils.rate.Predicate;

/**
 * 
 * @author hhildebrand
 * 
 */
public class RateControllerTest {
    @Test
    public void testRateController() {
        Predicate predicate = mock(Predicate.class);
        RateController controller = new RateController(predicate, 0.01, 1, 10,
                                                       1, 0.9);
        controller.setTarget(1);

        controller.setSampleRate(0);

        long sampleTime = 0L;
        int[] responseTimes = new int[] { 10, 10, 10, 10, 10, 10, 1 };
        for (int responseTime : responseTimes) {
            controller.sample(responseTime, sampleTime);
            sampleTime += 10;
        }
        for (int i = 0; i < 25; i++) {
            controller.sample(0, sampleTime);
            sampleTime += 10;
        }

        controller.sample(0, sampleTime);
        sampleTime += 10;
        controller.sample(0, sampleTime);
        sampleTime += 10;
        controller.sample(0, sampleTime);
        sampleTime += 10;
        controller.sample(1, sampleTime);
        sampleTime += 10;
        controller.sample(1, sampleTime);
        sampleTime += 10;
        controller.sample(1, sampleTime);
        sampleTime += 10;
        controller.sample(1, sampleTime);
        sampleTime += 10;
        controller.sample(0, sampleTime);
        sampleTime += 10;
        controller.sample(0, sampleTime);
        sampleTime += 10;
        controller.sample(0, sampleTime);
        sampleTime += 10;
        verify(predicate).setTargetRate(0.5);
        verify(predicate).setTargetRate(0.25);
        verify(predicate).setTargetRate(0.125);
        verify(predicate).setTargetRate(0.0625);
        verify(predicate).setTargetRate(0.03125);
        verify(predicate).setTargetRate(0.015625);
        verify(predicate).setTargetRate(0.01);
        verify(predicate).setTargetRate(0.51);
        verify(predicate, times(2)).setTargetRate(1.0);
        verifyNoMoreInteractions(predicate);
    }
}