/** (C) Copyright 2010 Hal Hildebrand, All Rights Reserved
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
package com.hellblazer.utils.fd.impl;

import com.hellblazer.utils.fd.FailureDetector;
import com.hellblazer.utils.fd.FailureDetectorFactory;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public class AdaptiveFailureDetectorFactory implements FailureDetectorFactory {
    private double convictionThreshold;
    private int    windowSize;
    private long   expectedSampleInterval;
    private int    initialSamples;
    private double minimumInterval;
    private double scale;

    public AdaptiveFailureDetectorFactory() {
    }

    /**
     * 
     * @param convictionThreshold
     *            - the level of certainty that must be met before conviction.
     *            This value must be <= 1.0
     * @param windowSize
     *            - the number of samples in the window
     * @param scale
     *            - a scale factor to accomidate the real world
     * @param expectedSampleInterval
     *            - the expected sample interval, used to prime the detector
     * @param initialSamples
     *            - the number of initial samples to prime the detector
     * @param minimumInterval
     *            - the minimum inter arival interval
     */
    public AdaptiveFailureDetectorFactory(double convictionThreshold,
                                          int windowSize, double scale,
                                          long expectedSampleInterval,
                                          int initialSamples,
                                          double minimumInterval) {
        if (convictionThreshold > 1.0) {
            throw new IllegalArgumentException(
                                               String.format("Conviction threshold %s must be <= 1.0",
                                                             convictionThreshold));
        }
        this.convictionThreshold = convictionThreshold;
        this.windowSize = windowSize;
        this.expectedSampleInterval = expectedSampleInterval;
        this.initialSamples = initialSamples;
        this.minimumInterval = minimumInterval;
        this.scale = scale;
    }

    @Override
    public FailureDetector create() {
        return new AdaptiveFailureDetector(convictionThreshold, windowSize,
                                           scale, expectedSampleInterval,
                                           initialSamples, minimumInterval);
    }

    /**
     * @return the convictionThreshold
     */
    public double getConvictionThreshold() {
        return convictionThreshold;
    }

    /**
     * @return the expectedSampleInterval
     */
    public long getExpectedSampleInterval() {
        return expectedSampleInterval;
    }

    /**
     * @return the initialSamples
     */
    public int getInitialSamples() {
        return initialSamples;
    }

    /**
     * @return the minimumInterval
     */
    public double getMinimumInterval() {
        return minimumInterval;
    }

    /**
     * @return the scale
     */
    public double getScale() {
        return scale;
    }

    /**
     * @return the windowSize
     */
    public int getWindowSize() {
        return windowSize;
    }

    /**
     * @param convictionThreshold
     *            the convictionThreshold to set
     */
    public void setConvictionThreshold(double convictionThreshold) {
        this.convictionThreshold = convictionThreshold;
    }

    /**
     * @param expectedSampleInterval
     *            the expectedSampleInterval to set
     */
    public void setExpectedSampleInterval(long expectedSampleInterval) {
        this.expectedSampleInterval = expectedSampleInterval;
    }

    /**
     * @param initialSamples
     *            the initialSamples to set
     */
    public void setInitialSamples(int initialSamples) {
        this.initialSamples = initialSamples;
    }

    /**
     * @param minimumInterval
     *            the minimumInterval to set
     */
    public void setMinimumInterval(double minimumInterval) {
        this.minimumInterval = minimumInterval;
    }

    /**
     * @param scale
     *            the scale to set
     */
    public void setScale(double scale) {
        this.scale = scale;
    }

    /**
     * @param windowSize
     *            the windowSize to set
     */
    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

}
