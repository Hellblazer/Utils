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
package com.hellblazer.utils;

/**
 * @author hhildebrand
 *
 */

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A simple coordinator for a group of asynchronous systems that need to
 * coordinated. The rendezvous is composed of a required number of participants
 * and an action to run when the required number of participants have met. The
 * rendezvous may optionally be scheduled for cancellation, providing a timeout
 * action to take if the rendezvous is cancelled due to timeout. The rendezvous
 * instance maintains this scheduled cancellation, clearing it if the rendezvous
 * is met or cancelled through other means.
 * 
 * @author hhildebrand
 * 
 */
public class Rendezvous {
    private final Runnable     action;
    private final Runnable     cancellationAction;
    private boolean            cancelled = false;
    private int                count;
    private final Object       mutex     = new Object();
    private final int          parties;
    private ScheduledFuture<?> scheduled;

    public Rendezvous(int parties, Runnable action,
                      final Runnable cancellationAction) {
        this.parties = parties;
        this.action = action;
        this.cancellationAction = cancellationAction;
        count = parties;
    }

    /**
     * Cancel the rendezvous. If the rendezvous has not been previously
     * cancelled, then run the cancellation action.
     */
    public void cancel() {
        synchronized (mutex) {
            if (count != 0 && !cancelled) {
                cancelled = true;
                if (scheduled != null) {
                    scheduled.cancel(true);
                }
                scheduled = null;
                if (cancellationAction != null) {
                    cancellationAction.run();
                }
            }
        }
    }

    /**
     * @return the number of parties that have made it past the rendezvous point
     */
    public int getCount() {
        synchronized (mutex) {
            return count;
        }
    }

    /**
     * @return the number of parties required to complete the rendezvous
     */
    public int getParties() {
        return parties;
    }

    /**
     * @return whether the rendezvous has been cancelled.
     */
    public boolean isCancelled() {
        synchronized (mutex) {
            return cancelled;
        }
    }

    /**
     * @return true if the number of parties in the rendezvous have met
     */
    public boolean isMet() {
        synchronized (mutex) {
            return count == 0;
        }
    }

    /**
     * Meet at the rendezvous point. If the number of parties has been met, then
     * run the rendezvous action.
     * 
     * @throws IllegalStateException
     *             - if the number of parties has already been met
     * @throws BrokenBarrierException
     *             - if the rendezvous has been cancelled.
     */
    public void meet() throws BrokenBarrierException {
        boolean run = false;
        synchronized (mutex) {
            if (count == 0) {
                throw new IllegalStateException("All parties have rendezvoused");
            }
            if (cancelled) {
                throw new BrokenBarrierException();
            }
            if (--count == 0) {
                if (scheduled != null) {
                    scheduled.cancel(true);
                }
                scheduled = null;
                run = true;
            }
        }
        if (run) {
            if (action != null) {
                action.run();
            }
        }
    }

    /**
     * Schedule a cancellation of the rendezvous. The scehduled cancellation is
     * tracked and maintained by the receiver.
     * 
     * @param timeout
     * @param unit
     * @param timer
     *            - the timer to schedule the cancellation
     */
    public void scheduleCancellation(long timeout, TimeUnit unit,
                                     ScheduledExecutorService timer) {
        synchronized (mutex) {
            if (scheduled != null) {
                throw new IllegalStateException(
                                                "Cancellation has already been scheduled");
            }
            if (cancelled) {
                throw new IllegalStateException(
                                                "Rendezvous has already been cancelled");
            }
            if (count == 0) {
                return;
            }
            scheduled = timer.schedule(new Runnable() {
                @Override
                public void run() {
                    cancel();
                }
            }, timeout, unit);
        }
    }
}
