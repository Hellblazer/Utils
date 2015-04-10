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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hellblazer.utils.rate.Controller;
import com.hellblazer.utils.rate.Predicate;

/**
 * 
 * An implementation of Controller that uses a direct adjustment of queue
 * thresholds based on the error in the target percentile (e.g. 90%, etc).
 * 
 * @author hhildebrand
 * 
 */
public class RateController implements Controller {
    private static Logger       log                    = LoggerFactory.getLogger(RateController.class);

    private volatile double     additiveIncrease       = 0.5;
    private volatile double     highWaterMark          = 1.2D;
    private volatile long       lastSampled            = 0;
    private final ReentrantLock lock                   = new ReentrantLock();
    private volatile double     lowWaterMark           = 0.9D;
    private volatile double     maximum                = 5000.0;
    private volatile double     minimum                = 0.05;
    private volatile double     multiplicativeDecrease = 2;
    private final Predicate     predicate;
    private final AtomicInteger sampleCount            = new AtomicInteger();
    private final int           sampleFrequency;
    private volatile long       sampleRate             = 1000L;
    private volatile double     smoothConstant         = 0.7;
    private volatile double     target;
    private final SampleWindow  window;
    private final double        targetPercentile;

    public RateController(Predicate predicate) {
        this(predicate, 1000, 1, 0.9);
    }

    public RateController(Predicate predicate, double minimumRate,
                          double maximumRate, int windowSize,
                          int sampleFrequency, double targetPercentile) {
        this(predicate, windowSize, sampleFrequency, targetPercentile);
        minimum = minimumRate;
        maximum = maximumRate;
    }

    public RateController(Predicate predicate, int windowSize,
                          int sampleFrequency, double targetPercentile) {
        window = new SampleWindow(windowSize);
        this.predicate = predicate;
        this.sampleFrequency = sampleFrequency;
        this.targetPercentile = targetPercentile;
    }

    @Override
    public boolean accept(int cost, long currentTime) {
        return predicate.accept(cost, currentTime);
    }

    @Override
    public boolean accept(long currentTime) {
        return predicate.accept(currentTime);
    }

    public double getAdditiveIncrease() {
        return additiveIncrease;
    }

    public double getHighWaterMark() {
        return highWaterMark;
    }

    public double getLowWaterMark() {
        return lowWaterMark;
    }

    public double getMaximum() {
        return maximum;
    }

    @Override
    public double getMedianResponseTime() {
        return window.getMedian();
    }

    public double getMinimum() {
        return minimum;
    }

    public double getMultiplicativeDecrease() {
        return multiplicativeDecrease;
    }

    @Override
    public double getResponseTime() {
        return window.getPercentile(0.9);
    }

    public long getSampleRate() {
        return sampleRate;
    }

    public double getSmoothConstant() {
        return smoothConstant;
    }

    @Override
    public double getTarget() {
        return target;
    }

    @Override
    public int getWindow() {
        return window.getWindow();
    }

    /* (non-Javadoc)
     * @see com.salesforce.ouroboros.util.rate.Controller#reset()
     */
    @Override
    public void reset() {
        window.reset();
    }

    @Override
    public void sample(double sample, long currentTime) {
        // Only sample every N batches
        if (sampleCount.incrementAndGet() % sampleFrequency != 0) {
            return;
        }
        ReentrantLock myLock = lock;
        if (!myLock.tryLock()) {
            // Skip sample if locked
            return;
        }
        try {

            if (currentTime - lastSampled < sampleRate) {
                return;
            }
            window.sample(sample);
            lastSampled = currentTime;
            double data = window.getPercentile(targetPercentile);

            if (data < lowWaterMark * target) {
                increaseRate();
            } else if (data > highWaterMark * target) {
                decreaseRate();
            }
        } finally {
            myLock.unlock();
        }
    }

    public void setAdditiveIncrease(double additiveIncrease) {
        this.additiveIncrease = additiveIncrease;
    }

    public void setHighWaterMark(double highWaterMark) {
        this.highWaterMark = highWaterMark;
    }

    public void setLowWaterMark(double lowWaterMark) {
        this.lowWaterMark = lowWaterMark;
    }

    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }

    public void setMinimum(double minimum) {
        this.minimum = minimum;
    }

    public void setMultiplicativeDecrease(double multiplicativeDecrease) {
        this.multiplicativeDecrease = multiplicativeDecrease;
    }

    public void setSampleRate(long sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void setSmoothConstant(double smoothConstant) {
        this.smoothConstant = smoothConstant;
    }

    @Override
    public void setTarget(double targetRate) {
        target = targetRate;
        predicate.setTargetRate(targetRate);
    }

    protected void decreaseRate() {
        if (target > minimum) {
            target /= multiplicativeDecrease;
            if (target < minimum) {
                target = minimum;
            }
            predicate.setTargetRate(target);
            if (log.isInfoEnabled()) {
                log.info(String.format("Target rate decreased to %s", target));
            }
        } else {
            if (log.isInfoEnabled()) {
                log.info(String.format("Target rate already at minimum"));
            }
        }
    }

    protected void increaseRate() {
        if (target < maximum) {
            target += additiveIncrease;
            if (target > maximum) {
                target = maximum;
            }
            predicate.setTargetRate(target);
            if (log.isInfoEnabled()) {
                log.info(String.format("Target rate increased to %s", target));
            }
        } else {
            if (log.isInfoEnabled()) {
                log.info(String.format("Target rate already at maximum"));
            }
        }
    }
}