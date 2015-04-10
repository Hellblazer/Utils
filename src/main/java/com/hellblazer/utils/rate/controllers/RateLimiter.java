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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hellblazer.utils.rate.Predicate;

/**
 * 
 * Input rate policing based on a token bucket.
 * 
 * @author hhildebrand
 * 
 */
public class RateLimiter implements Predicate {
    private static final Logger log = LoggerFactory.getLogger(RateLimiter.class);

    private double              currentTokens;
    private long                last;
    private int                 maxTokens;
    private final long          minimumRegenerationTime;
    private double              regenerationTime;

    /**
     * @param targetRate
     *            - the target rate limit for accepting new input
     * @param tokenLimit
     *            - the limit to the number of tokens in the bucket
     * @param minRegenerationTime
     *            - the minimum delay time, in Ms, to regenerate tokens
     */
    public RateLimiter(double targetRate, int tokenLimit,
                       int minRegenerationTime) {
        assert targetRate > 0;
        assert minRegenerationTime >= 0;
        assert tokenLimit > 0;
        minimumRegenerationTime = minRegenerationTime;
        regenerationTime = 1.0 / targetRate * 1.0e3;
        if (regenerationTime < 1) {
            regenerationTime = 1;
        }
        maxTokens = tokenLimit;
        currentTokens = tokenLimit * 1.0;
        last = -1;
    }

    /* (non-Javadoc)
     * @see com.salesforce.ouroboros.util.rate.Predicate#accept(int)
     */
    @Override
    public synchronized boolean accept(int cost, long currentTime) {
        long delay = last == -1L ? 0 : currentTime - last;

        if (delay >= minimumRegenerationTime) {
            // Regenerate tokens
            double numTokens = delay / regenerationTime;
            currentTokens += numTokens;
            if (currentTokens > maxTokens) {
                currentTokens = maxTokens;
            }
            last = currentTime;
        }

        if (currentTokens >= cost) {
            currentTokens -= cost;
            return true;
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see com.salesforce.ouroboros.util.rate.Predicate#accept()
     */
    @Override
    public boolean accept(long currentTime) {
        return accept(1, currentTime);
    }

    /**
     * @return the size of the token bucket
     */
    public int getCurrentTokens() {
        return (int) currentTokens;
    }

    /**
     * @return the current depth of the bucket
     */
    public int getMaxTokens() {
        return maxTokens;
    }

    public double getRegenerationTime() {
        return regenerationTime;
    }

    /**
     * Set the depth of the token bucket
     * 
     * @param depth
     */
    public synchronized void setMaxTokens(int depth) {
        maxTokens = depth;
    }

    @Override
    public synchronized void setTargetRate(double targetRate) {
        regenerationTime = 1.0 / targetRate * 1.0e3;
        if (regenerationTime < 1) {
            regenerationTime = 1;
        }
        if (log.isInfoEnabled()) {
            log.info(String.format("New regeneration time set to %s ms",
                                   regenerationTime));
        }
    }
}