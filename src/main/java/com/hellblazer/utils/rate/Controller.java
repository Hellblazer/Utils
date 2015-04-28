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

package com.hellblazer.utils.rate;

/**
 * A controller that dynamically controls the input rate in order to meet a
 * target throughput.
 * 
 * @author hhildebrand
 * 
 */
public interface Controller {

    /**
     * @param currentTime
     * @return true if a new element can be accepted, using the supplied cost
     */
    public abstract boolean accept(int cost, long currentTime);

    /**
     * @param currentTime
     * @return true if a new element can be accepted, using the default cost
     */
    public abstract boolean accept(long currentTime);

    /**
     * Answer the target response time
     * 
     * @return
     */
    public double getTarget();

    /**
     * notify the controller of a new sample
     * 
     * @param responseTime
     *            - the new sample
     * @param currentTime
     */
    public void sample(double responseTime, long currentTime);

    /**
     * @return the median of the sampled response time over the history of the
     *         controller
     */
    double getMedianResponseTime();

    /**
     * @return the 90th percentile of the sampled response time over the history
     *         of the controller
     */
    double getResponseTime();

    /**
     * Answer the size of the sample window
     * 
     * @return the size of the sample window
     */
    int getWindow();

    /**
     * Reset the stae of the controller
     */
    void reset();

    /**
     * Set the target rate
     * 
     * @param target
     */
    void setTarget(double target);

}