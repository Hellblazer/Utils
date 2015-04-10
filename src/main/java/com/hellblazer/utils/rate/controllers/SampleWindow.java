/**
 * Copyright (c) 2015 Chiral Behaviors, LLC, all rights reserved.
 * 
 
 * This file is part of Ultrastructure.
 *
 *  Ultrastructure is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  ULtrastructure is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Ultrastructure.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.utils.rate.controllers;

import com.hellblazer.utils.collections.SkipList;
import com.hellblazer.utils.windows.Window;

/**
 * 
 * @author hhildebrand
 * 
 */
public class SampleWindow extends Window {
    private final SkipList sorted = new SkipList();
    private final int      window;

    public SampleWindow(int windowSize) {
        super(windowSize);
        window = windowSize;
    }

    public double getMedian() {
        if (count == 0) {
            throw new IllegalStateException(
                                            "Must have at least one sample to calculate the median");
        }
        return sorted.get(sorted.size() / 2);
    }

    public double getPercentile(double percentile) {
        if (count == 0) {
            throw new IllegalStateException(
                                            "Must have at least one sample to calculate the percentile");
        }
        return sorted.get((int) ((sorted.size() - 1) * percentile));
    }

    public int getWindow() {
        return window;
    }

    /**
     * Reset the state of the receiver
     */
    @Override
    public void reset() {
        super.reset();
        sorted.reset();
    }

    public void sample(double sample) {
        sorted.add(sample);
        if (count == samples.length) {
            sorted.remove(removeFirst());
        }
        addLast(sample);
    }
}