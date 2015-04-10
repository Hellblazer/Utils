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
 * 
 * The predicate for evaluating the rate of acceptance
 * 
 * @author hhildebrand
 * 
 */
public interface Predicate {

    /**
     * Evaluate the predicate, using the supplied cost
     * 
     * @param cost
     *            - the cost of accepting
     * @param currentTime
     *            - the current time to use for the request
     * @return true if the predicated rate is valid given the cost, false
     *         otherwise
     */
    boolean accept(int cost, long currentTime);

    /**
     * Evaluate the predicate, using the default cost
     * 
     * @param currentTime
     *            - the current time to use for the request
     * 
     * @return true if the predicated rate is valid, false otherwise
     */
    boolean accept(long currentTime);

    /**
     * Set the target rate of the predicate
     * 
     * @param targetRate
     */
    void setTargetRate(double targetRate);

}