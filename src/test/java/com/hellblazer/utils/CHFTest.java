/** (C) Copyright 2013 Hal Hildebrand, All Rights Reserved
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Logger;

import org.junit.Test;

import com.hellblazer.utils.collections.ConsistentHashFunction;
import com.hellblazer.utils.collections.ConsistentHashFunction.SkipStrategy;

/**
 * @author hhildebrand
 * 
 */
public class CHFTest {
    private static final Logger log = Logger.getLogger(ConsistentHashFunction.class.getCanonicalName());

    @Test
    public void testAdd() {
        ConsistentHashFunction<String> chf = new ConsistentHashFunction<String>(
                                                                                200);
        final String o0 = "0", o1 = "1", o2 = "2";
        Random r = new Random(1);

        chf.add(o0, 1);
        chf.add(o1, 1);
        chf.add(o2, 2);

        boolean found0 = false, found1 = false, found2 = false;

        for (int i = 0; i < 200; i++) {
            long nextLong = r.nextLong();
            if (chf.hash(nextLong) == o0) {
                found0 = true;
            }
            nextLong = r.nextLong();
            if (chf.hash(nextLong) == o1) {
                found1 = true;
            }
            nextLong = r.nextLong();
            if (chf.hash(nextLong) == o2) {
                found2 = true;
            }
        }

        assertTrue(found0);
        assertTrue(found1);
        assertTrue(found2);
    }

    @Test
    public void testClone() {
        SkipStrategy<String> strategy = new SkipStrategy<String>() {
            @Override
            public boolean isSkippable(List<String> previous, String bucket) {
                return false;
            }
        };
        ConsistentHashFunction<String> chf = new ConsistentHashFunction<String>(
                                                                                strategy,
                                                                                200);
        Random r = new Random(0x1638);
        while (chf.size() < 100) {
            chf.add(Integer.toString(r.nextInt()), Math.max(1, r.nextInt(5)));
        }

        ConsistentHashFunction<String> dupe = chf.clone();
        assertEquals(chf.size(), dupe.size());
        for (Entry<String, Integer> entry : chf.getSizes().entrySet()) {
            assertEquals(chf.getSizes().get(entry.getKey()),
                         dupe.getSizes().get(entry.getKey()));
        }
    }

    @Test
    public void testConsistency() {
        final Random r = new Random(1);
        int nBucket = 1 + r.nextInt(4);
        ArrayList<String> bucket = new ArrayList<String>();
        for (int i = 0; i < nBucket; i++) {
            bucket.add(Integer.toString(i));
        }

        ConsistentHashFunction<String> chf = new ConsistentHashFunction<String>(
                                                                                200);

        for (int i = 0; i < nBucket; i++) {
            chf.add(bucket.get(i), 1);
        }

        for (int t = 0; t < 500; t++) {
            long sample = r.nextLong();
            String a = chf.hash(sample);
            String b = "foo";
            chf.add(b, 1);
            String c = chf.hash(sample);
            assertTrue(c == a || c == b);
            if (c == a) {
                System.out.print("*");
            } else {
                System.out.print("-");
            }
            chf.remove(b);
        }
        System.out.println();
    }

    @Test
    public void testPerf() {
        ConsistentHashFunction<Integer> ring = new ConsistentHashFunction<Integer>();
        Random r = new Random(0x1638);
        while (ring.size() < 100) {
            int bucket = r.nextInt();
            if (bucket > 0) {
                ring.add(bucket, 1);
            }
        }

        long now = System.currentTimeMillis();
        int points = 10000000;
        for (int i = 0; i < points; i++) {
            ring.hash(r.nextLong());
        }
        System.out.println(String.format("Time to hash %s points: %s ms",
                                         points, System.currentTimeMillis()
                                                 - now));
    }

    @Test
    public void testRemove() {
        ConsistentHashFunction<String> chf = new ConsistentHashFunction<String>(
                                                                                200);
        final String o0 = "0", o1 = "1", o2 = "2";
        Random r = new Random(1);

        chf.add(o0, 1);
        chf.add(o1, 1);
        assertFalse(chf.add(o1, 1)); // To increase coverage
        chf.remove(o1);
        for (int i = 0; i < 1000000; i++) {
            assertEquals(o0, chf.hash(r.nextLong()));
        }
        chf.add(o1, 1);
        chf.add(o2, 1);
        chf.remove(o1);
        chf.remove(o2);
        for (int i = 0; i < 1000000; i++) {
            assertEquals(o0, chf.hash(r.nextLong()));
        }
        chf.remove(o2); // To increase coverage

    }

    @Test
    public void testSecondChance() {
        final Random r = new Random(1);
        int nBucket = 1 + r.nextInt(4);
        ConsistentHashFunction<String> chf = new ConsistentHashFunction<String>(
                                                                                200);
        for (int i = 0; i < nBucket; i++) {
            chf.add(Integer.toString(i), 1);
        }

        for (int t = 0; t < 500; t++) {
            long sample = r.nextLong();
            List<String> chances = chf.hash(sample,
                                            Math.min(chf.getBuckets().size(),
                                                     r.nextInt(3) + 2));
            log.finest("Chances for " + sample + " are " + chances + " out of "
                       + chf.getBuckets());
            for (String chance : chances) {
                assertEquals(chf.hash(sample) + " != " + chance,
                             chf.hash(sample), chance);
                chf.remove(chance);
            }
            for (int i = chances.size() - 1; i >= 0; i--) {
                chf.add(chances.get(i), 1);
                // assertEquals( chf.hash( sample ), chances[ i ] );
            }

            assertTrue(sample + ": " + chances + " != "
                               + chf.hash(sample, chances.size()) + " (size="
                               + chf.getBuckets().size() + ")",
                       Arrays.equals(chances.toArray(),
                                     chf.hash(sample, chances.size()).toArray()));
        }
    }

    @Test
    public void testSpecial() {
        ConsistentHashFunction<String> chf = new ConsistentHashFunction<String>(
                                                                                200);
        long sample = -3599839008849623859L;
        chf.add("0", 1);
        chf.add("1", 1);
        chf.add("2", 1);

        List<String> r = chf.hash(sample, 3);
        System.out.println("3: " + r);
        for (String element : r) {
            assertEquals(chf.hash(sample) + " != " + element, chf.hash(sample),
                         element);
            chf.remove(element);
        }
        for (int i = r.size() - 1; i >= 0; i--) {
            chf.add(r.get(i), 1);
        }

        System.out.println(chf.hash(sample, 3));
    }

    @Test
    public void testStress() {
        final Random r = new Random(1);
        int nBucket = 1 + r.nextInt(1000);
        ArrayList<String> bucket = new ArrayList<String>();
        for (int i = 0; i < nBucket; i++) {
            bucket.add(Integer.toString(i));
        }

        ConsistentHashFunction<String> chf = new ConsistentHashFunction<String>(
                                                                                200);

        for (int i = 0; i < nBucket; i++) {
            chf.add(bucket.get(i), 1);
        }

        for (int t = 0; t < r.nextInt(1000); t++) {
            for (int p = 0; p < r.nextInt(100); p++) {
                assertTrue(bucket.contains(chf.hash(r.nextLong())));
            }

            int removals = Math.min(r.nextInt(5), bucket.size() - 1);
            for (int k = 0; k < removals; k++) {
                log.finest(String.format("Removing %d/%d\n", k, removals));
                String x = bucket.remove(r.nextInt(bucket.size()));
                chf.remove(x);
            }
            int additions = r.nextInt(5);
            for (int k = 0; k < additions; k++) {
                log.finest(String.format("Adding %d/%d\n", k, additions));
                String x = Integer.toString(new Object().hashCode());
                bucket.add(x);
                chf.add(x, 1);
            }
            if (bucket.size() == 0) {
                log.finest(String.format("Adding out of emergency"));
                String x = Integer.toString(new Object().hashCode());
                bucket.add(x);
                chf.add(x, 1);
            }
            assertEquals(bucket.size(), chf.getBuckets().size());
        }
    }
}