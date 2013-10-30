package com.hellblazer.utils.collections;

/*               
 * Copyright (C) 2004-2010 Paolo Boldi, Massimo Santini and Sebastiano Vigna 
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by the Free
 *  Software Foundation; either version 2.1 of the License, or (at your option)
 *  any later version.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hellblazer.utils.MersenneTwister;

// RELEASE-STATUS: DIST

/**
 * Provides an implementation of consistent hashing. Consistent hashing has been
 * introduced in <blockquote>
 * <P>
 * Consistent Hashing and Random Trees: Distributed Caching Protocols for
 * Relieving Hot Spots on the World Wide Web, by David R. Karger, Eric Lehman,
 * Frank T. Leighton, Rina Panigrahy, Matthew S. Levine and Daniel Lewin, Proc.
 * of the twenty-ninth annual ACM symposium on Theory of computing, El Paso,
 * Texas, United States, 1997, pages 654&minus;663. </blockquote> This class
 * provides some extension to the original definition: weighted buckets and
 * skippable buckets. More precisely, keys are distributed on buckets
 * proportionally to the weight of the buckets, and it is possible to specify a
 * {@linkplain ConsistentHashFunction.SkipStrategy skip strategy} for buckets.
 * 
 * <H3>Consistent Hash Function: Properties</H3>
 * 
 * <P>
 * A consistent hash function consists, at any time, of a set of objects, called
 * <em>buckets</em>, each with a specified weight (a positive integer). At the
 * beginning, there are no buckets, but you can
 * {@linkplain #add(Comparable, int) add a new bucket}, or
 * {@linkplain #remove(Comparable) remove an existing bucket}.
 * 
 * <P>
 * The method {@link #hash(long)} can be used to hash a <em>point</em> (a long)
 * to a bucket. More precisely, when applied to a given point, this method will
 * return one of the buckets, and the method will satisfy the following
 * properties:
 * 
 * <OL>
 * <LI>the bucket returned by {@link #hash(long)} is one of the buckets
 * currently present in the consistent hash function;
 * <LI>the fraction of points that are hashed to a specific bucket is
 * approximately proportional to the weight of the bucket; for example, if there
 * are only two buckets <var>A</var> and <var>B</var>, and the weight of
 * <var>A</var> is 2 whereas the weight of <var>B</var> is 3, then about 2/5 of
 * all longs will be hashed to <var>A</var> and about 3/5 will be hashed to
 * <var>B</var>;
 * <LI>every time you add a new bucket, some of the points will of course be
 * hashed to the new bucket, but it is impossible that a point that was hashed
 * to an old bucket will now be hashed to some other old bucket; more formally,
 * consider the following sequence of instructions:
 * 
 * <pre>
 * Object A = chf.hash(x);
 * chf.add(B);
 * Object C = chf.hash(x);
 * </pre>
 * 
 * at the end either <code>A==C</code> (i.e., the hash of x has not changed
 * after adding B), or <code>C==B</code> (i.e., now x is hashed to the new
 * object).
 * </OL>
 * 
 * <P>
 * Otherwise said, if a new bucket is added, then the number of keys that change
 * their assignment is the minimum necessary; more importantly, it is impossible
 * that a key changes its bucket assignment towards a bucket that already
 * existed: when a bucket is added old buckets can only lose keys towards the
 * new bucket.
 * 
 * <P>
 * It is easy to see that the last property stated above can be equivalently
 * stated by saying that every point determines a (total) order among buckets;
 * the {@link #hash(long)} method only returns the first element of this order.
 * It is also possible, using {@link #hash(long, int)} to obtain an
 * <em>array</em> of buckets containing the (first part of the) order. In
 * particular, if the latter method is called with a specified length
 * corresponding to the number of buckets, the whole order will be returned.
 * 
 * <H3>Implementation Details</H3>
 * 
 * <P>
 * With each bucket, we associate a number of points, called replicae, located
 * on the unit circle (the unit circle itself is represented approximately on
 * the whole range of <code>long</code>s). Then, given a point <var>p</var>, one
 * can {@linkplain #hash(long) get the bucket} corresponding to the replica that
 * is closest to <var>p</var>.
 * 
 * <P>
 * The method that {@linkplain #hash(long, int) gets an array} containing the
 * buckets looks at the buckets that are closest to a point, in distance order,
 * without repetitions. In particular, by computing an array as large as the
 * number of buckets, you will obtain a permutation of the buckets themselves.
 * Indeed, another viewpoint on consistent hashing is that it associates a
 * random permutation of the buckets to each point (albeit the interpretation of
 * weights, in that case, becomes a bit twisted).
 * 
 * <P>
 * The number of replicas associated to a bucket is fixed when the bucket is
 * inserted in the map. The actual number depends on the weight and on the
 * constant {@link #replicaePerBucket}.
 * 
 * <P>
 * This class handles overlaps (i.e., conflicts in a replica creation). In that
 * case, a local deterministic ordering is generated using the hash code (and,
 * in case of a tie, the lexicographic order of string representation) of the
 * buckets.
 * 
 * <P>
 * The hashing function is deterministically based on the hash code of the
 * buckets, which should be of good quality. This is essential to ensure
 * deterministic replicability of the results of this function across different
 * instances.
 * 
 */

public final class ConsistentHashFunction<T extends Comparable<? super T>>
        implements Cloneable, Iterable<T> {

    /**
     * Allows to skip suitable items when searching for the closest replica.
     * 
     * <P>
     * Sometimes it is useful to restrict the set of buckets that can be
     * returned without modifying a consistent hash function (if not else,
     * because any change requires removing or adding
     * {@link ConsistentHashFunction#replicaePerBucket} replicae).
     * 
     * <P>
     * To do so, it is possible to
     * {@linkplain ConsistentHashFunction#ConsistentHashFunction(ConsistentHashFunction.SkipStrategy)
     * provide at construction time} a strategy that, at each call to
     * {@link ConsistentHashFunction#hash(long)}, will be used to test whether
     * the bucket of a certain replica can be returned or not. Of course, in the
     * latter case the search will continue with the next replica.
     */

    public static interface SkipStrategy<T> {

        /**
         * Checks whether a bucket can be returned or should be skipped. As
         * mulitple buckets can be returned, the list of buckets preceding the
         * test value are supplied so that skip logic can take into account any
         * interdependencies - such as same rack, same host, etc.
         * 
         * @param previous
         *            the list of buckets preceeding the bucket to test
         * @param bucket
         *            the bucket to test.
         * @return true if the bucket should be skipped.
         */
        public boolean isSkippable(List<T> previous, T bucket);
    }

    private static int                    DEFAULT_REPLICAS = 200;
    private static final Logger           log              = Logger.getLogger(ConsistentHashFunction.class.getCanonicalName());

    /** Each bucket is replicated this number of times. */
    public final int                      replicaePerBucket;
    /** The cached key set of {@link #sizes}. */
    final private Set<T>                  buckets;
    /** Maps points in the unit interval to buckets. */
    final private SortedMap<Long, Object> replicae         = new TreeMap<Long, Object>();
    /** For each bucket, its size. */
    final private Map<T, Integer>         sizes            = new HashMap<T, Integer>();
    /** The optional strategy to skip buckets, or <code>null</code>. */
    final private SkipStrategy<T>         skipStrategy;

    /** Creates a new consistent hash function. */
    public ConsistentHashFunction() {
        this(DEFAULT_REPLICAS);
    }

    public ConsistentHashFunction(int replicas) {
        this(null, replicas);
    }

    public ConsistentHashFunction(SkipStrategy<T> skipStrategy) {
        this(skipStrategy, DEFAULT_REPLICAS);
    }

    /**
     * Creates a new consistent hash function with given skip strategy.
     * 
     * @param skipStrategy
     *            a skip strategy, or <code>null</code>.
     * @param replicas
     *            the number of replicas per bucket
     */
    public ConsistentHashFunction(final SkipStrategy<T> skipStrategy,
                                  int replicas) {
        this.skipStrategy = skipStrategy;
        this.replicaePerBucket = replicas;
        buckets = getSizes().keySet();
    }

    /**
     * Adds a bucket to the map.
     * 
     * @param bucket
     *            the new bucket.
     * @param weight
     *            the weight of the new bucket; buckets with a larger weight are
     *            returned proportionately more often.
     * @return false if the bucket was already present.
     */

    @SuppressWarnings("unchecked")
    public boolean add(final T bucket, final int weight) {

        if (getSizes().containsKey(bucket)) {
            return false;
        }
        getSizes().put(bucket, weight);

        final MersenneTwister twister = new MersenneTwister(bucket.hashCode());

        long point;
        Object o;
        SortedSet<T> conflictSet;

        for (int i = 0; i < weight * replicaePerBucket; i++) {

            point = twister.nextLong();

            if ((o = replicae.get(point)) != null) {
                if (o != bucket) { // o == bucket should happen with very low
                                   // probability.
                    if (o instanceof SortedSet) {
                        if (log.isLoggable(Level.FINEST)) {
                            log.finest(String.format("Adding bucket %s to the conflict set",
                                                     bucket));
                        }
                        ((SortedSet<T>) o).add(bucket);
                    } else {
                        if (log.isLoggable(Level.FINEST)) {
                            log.finest(String.format("Creating conflict set for buckets %s and %s",
                                                     o, bucket));
                        }
                        conflictSet = new TreeSet<T>();
                        conflictSet.add((T) o);
                        conflictSet.add(bucket);
                        replicae.put(point, conflictSet);
                    }
                }
            } else {
                replicae.put(point, bucket);
            }
        }

        return true;
    }

    @Override
    public ConsistentHashFunction<T> clone() {
        ConsistentHashFunction<T> dupe = new ConsistentHashFunction<T>(
                                                                       skipStrategy,
                                                                       replicaePerBucket);
        for (Entry<T, Integer> entry : getSizes().entrySet()) {
            dupe.add(entry.getKey(), entry.getValue());
        }
        return dupe;
    }

    /**
     * Returns the set of buckets of this consistent hash function.
     * 
     * @return the set of buckets.
     */

    public Set<T> getBuckets() {
        return buckets;
    }

    /**
     * @return the replicaePerBucket
     */
    public int getReplicaePerBucket() {
        return replicaePerBucket;
    }

    public Map<T, Integer> getSizes() {
        return sizes;
    }

    /**
     * @return the skipStrategy
     */
    public SkipStrategy<T> getSkipStrategy() {
        return skipStrategy;
    }

    /**
     * Returns the bucket of the replica that is closest to the given point.
     * 
     * @param point
     *            a point on the unit circle.
     * @return the bucket of the closest replica, or <code>null</code> if there
     *         are no buckets or all buckets must be skipped.
     * @see #hash(long, int)
     * @throws NoSuchElementException
     *             if there are no buckets, or if a skip strategy has been
     *             specified and it skipped all existings buckets.
     */

    public T hash(long point) {
        final List<T> result = hash(point, 1);
        if (result.size() == 0) {
            throw new NoSuchElementException();
        } else {
            return result.get(0);
        }
    }

    /**
     * Returns an array of buckets whose replicae are close to the given point.
     * The first element will be the bucket of the replica closest to the point,
     * followed by the bucket of the next closest replica (whose bucket is not
     * the first, of course) and so on.
     * 
     * @param point
     *            a point on the unit circle.
     * @param n
     *            the number of closest buckets to return.
     * @return an array of distinct buckets of the closest replicas; the array
     *         could be shorter than <code>n</code> if there are not enough
     *         buckets and, in case a skip strategy has been specified, it could
     *         be empty even if the bucket set is nonempty.
     */

    public List<T> hash(long point, int n) {
        if (n == 0 || buckets.size() == 0) {
            return Collections.emptyList();
        }

        final ArrayList<T> result = new ArrayList<T>(n);

        for (int pass = 0; pass < 2; pass++) {
            Map<Long, Object> map = pass == 0 ? replicae.tailMap(point)
                                             : replicae.headMap(point);
            for (Entry<Long, Object> entry : map.entrySet()) {
                if (entry.getValue() instanceof SortedSet) {
                    @SuppressWarnings("unchecked")
                    SortedSet<T> value = (SortedSet<T>) entry.getValue();
                    for (T p : value) {
                        if ((skipStrategy == null || !skipStrategy.isSkippable(result,
                                                                               p))
                            && !result.contains(p) && result.add(p) && --n == 0) {
                            return result;
                        }
                    }
                } else {
                    @SuppressWarnings("unchecked")
                    T value = (T) entry.getValue();
                    if ((skipStrategy == null || !skipStrategy.isSkippable(result,
                                                                           value))
                        && !result.contains(value)
                        && result.add(value)
                        && --n == 0) {
                        return result;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns the bucket of the replica that is closest to the given key.
     * 
     * @param key
     *            an object to hash.
     * @return the bucket of the closest replica, or <code>null</code> if there
     *         are no buckets or all buckets must be skipped.
     * @see #hash(Object, int)
     * @throws NoSuchElementException
     *             if there are no buckets, or if a skip strategy has been
     *             specified and it skipped all existings buckets.
     */

    public T hash(final Object key) {
        final List<T> result = hash(key, 1);
        if (result.size() == 0) {
            throw new NoSuchElementException();
        } else {
            return result.get(0);
        }
    }

    /**
     * Returns an array of buckets whose replicae are close to the given object.
     * 
     * @param key
     *            an object ot hash.
     * @param n
     *            the number of close buckets to return.
     * 
     *            <P>
     *            This method just uses <code>hashCode() << 32</code> as point
     *            for {@link #hash(long,int)}
     * @return an array of distinct buckets of the closest replicas; the array
     *         could be shorter than <code>n</code> if there are not enough
     *         buckets and, in case a skip strategy has been specified, it could
     *         be empty even if the bucket set is nonempty.
     * @see #hash(long, int)
     */

    public List<T> hash(final Object key, final int n) {
        return hash((long) key.hashCode() << 32, n);
    }

    @Override
    public Iterator<T> iterator() {
        return buckets.iterator();
    }

    /**
     * Removes a bucket.
     * 
     * @param bucket
     *            the bucket to be removed.
     * @return false if the bucket was not present.
     */

    @SuppressWarnings("unchecked")
    public boolean remove(final T bucket) {

        if (!getSizes().containsKey(bucket)) {
            return false;
        }

        final MersenneTwister twister = new MersenneTwister(bucket.hashCode());
        final int size = getSizes().remove(bucket);

        long point;
        Object o;
        SortedSet<T> conflictSet;

        for (int i = 0; i < size * replicaePerBucket; i++) {
            point = twister.nextLong();

            o = replicae.remove(point);
            if (o instanceof SortedSet) {
                if (log.isLoggable(Level.FINEST)) {
                    log.finest(String.format("Removing %s from conflict set...",
                                             point));
                }
                conflictSet = (SortedSet<T>) o;
                conflictSet.remove(bucket);
                if (conflictSet.size() > 1) {
                    replicae.put(point, conflictSet);
                } else {
                    replicae.put(point, conflictSet.first());
                }
            } else if (o != null && ((T) o).compareTo(bucket) != 0) {
                replicae.put(point, o);
            }
        }

        return true;
    }

    public int size() {
        return buckets.size();
    }

    @Override
    public String toString() {
        return replicae.toString();
    }
}