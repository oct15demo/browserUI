/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   07.08.2008 (thor): created
 */
package org.knime.base.util.math;

import java.math.BigInteger;

import org.knime.core.util.ThreadPool;

/**
 * This class comes in handy if you want to compute combinations and process
 * them in some way. A combination is a subset of <em>k</em> elements from a
 * set of <em>m</em> elements.
 *
 * The {@link #rank(int[])} and {@link #unrank(long)} methods are based on
 * <em>Gary D. Knott: "A Numbering System for Combinations", CACM, 17(1), 1974,
 * pp. 45-46</em>.
 * The {@link #enumerate(Callback)}, and
 * {@link #enumerate(long, long, Callback)} methods are based on
 * <em>Charles J. Mifsud: "Algorithm 154: Combination in lexicographical order",
 * CACM, 6(3), 1963, p. 103</em>.
 *
 * @author Thorsten Meinl, University of Konstanz
 */
public final class Combinations {
    /**
     * Callback interface used by the various visit methods.
     *
     * @author Thorsten Meinl, University of Konstanz
     */
    public interface Callback {
        /**
         * This method is called with one particular combination of <em>k</em>
         * selected numbers (from 0 to <em>n</em> - 1) in the array
         * <em>selected</em>.
         *
         * @param selected an array with the selected elements
         * @param n the number of total elements
         * @param k the number of selected elements
         * @param index the unique index of the current combination
         * @param max the maximum number of combinations
         * @return <code>true</code> if the enumeration should continue,
         *         <code>false</code> otherwise
         */
        public boolean visit(int[] selected, int n, int k, long index, long max);
    }

    private final int m_n, m_k;

    private final long m_nrOfCombinations;

    /**
     * Create a combination object.
     *
     * @param n the total number of elements
     * @param k the number of selected elements
     */
    public Combinations(final int n, final int k) {
        if (k > n) {
            throw new ArithmeticException("k must not be greater than n");
        }
        m_n = n;
        m_k = k;
        m_nrOfCombinations = getNumberOfCombinations(n, k);
    }

    /**
     * Returns the number of possible combinations for <em>n</em> and
     * <em>k</em> specified in the constructor.
     *
     * @return the number of combinations
     */
    public long getNumberOfCombinations() {
        return m_nrOfCombinations;
    }

    private long vns(final int[] p) {
        long v = 0;
        for (int i = 0; i < m_k; i++) {
            v += getNumberOfCombinations(p[i], i + 1);
        }
        return v;
    }

    private int[] vns_inv(final long k) {
        int[] p = new int[m_k];

        for (int i = p.length - 1; i >= 0; i--) {
            long sum = 0;
            for (int j = i + 1; j < m_k; j++) {
                sum += getNumberOfCombinations(p[j], j + 1);
            }

            long x = k - sum;

            for (int j = m_n - 1; j > i - 1; j--) {
                if (x >= getNumberOfCombinations(j, i + 1)) {
                    p[i] = j;
                    break;
                }
            }
        }

        return p;
    }

    private int[] complement(final int[] p) {
        int[] q = new int[p.length];
        for (int i = 0; i < p.length; i++) {
            q[i] = m_n - 1 - p[m_k - 1 - i];
        }
        return q;
    }

    /**
     * Computes the unique rank of the given combination. The elements inside
     * the array must be sorted ascending.
     *
     * @param p a valid combination
     * @return the unique rank, a number between 0 and
     *         {@link #getNumberOfCombinations()} - 1.
     */
    public long rank(final int[] p) {
        return m_nrOfCombinations - vns(complement(p)) - 1;
    }

    /**
     * Creates the unique combination associated with the given rank number.
     *
     * @param k the rank, a number between 0 and
     *            {@link #getNumberOfCombinations()} - 1.
     * @return a valid combination
     */
    public int[] unrank(final long k) {
        return complement(vns_inv(m_nrOfCombinations - k - 1));
    }

    /**
     * Returns the number of possible combinations when selecting <em>k</em>
     * elements from a set of <em>n</em> elements (without repetition).
     *
     * @param n the size of the set
     * @param k the number of selected elements
     * @return the number of possible combinations
     *
     * @throws ArithmeticException if an overflow occurs during computing. In
     * this case you can use {@link #getNumberOfCombinationsBig(int, int)}
     * instead (which is slower, but works for arbitrary big numbers).
     */
    public static long getNumberOfCombinations(final int n, final int k) {
        if ((k > n) || (k < 0)) {
            return 0;
        }
        if ((k == 0) || (k == n)) {
            return 1;
        }

        long[] t = new long[n + 1];
        t[0] = 1;

        for (int i = 0; i < n; i++) {
            t[i] = 1;
            for (int j = i - 1; j > 0; j--) {
                t[j] = t[j] + t[j - 1];
                if (t[j] < 0) {
                    throw new ArithmeticException(
                            "Overflow, n and k are too large");
                }
            }
            t[0] = 1;
        }

        t[n] = 1;
        for (int j = n - 1; j >= k; j--) {
            t[j] = t[j] + t[j - 1];
        }
        if (t[k] < 0) {
            throw new ArithmeticException("Overflow, n and k are too large");
        }

        return t[k];
    }


    /**
     * Returns the number of possible combinations when selecting <em>k</em>
     * elements from a set of <em>n</em> elements (without repetition).
     *
     * @param n the size of the set
     * @param k the number of selected elements
     * @return the number of possible combinations
     */
    public static BigInteger getNumberOfCombinationsBig(final int n, final int k) {
        if ((k > n) || (k < 0)) {
            return new BigInteger("0");
        }
        if ((k == 0) || (k == n)) {
            return new BigInteger("1");
        }

        final BigInteger one = new BigInteger("1");

        BigInteger[] t = new BigInteger[n + 1];
        t[0] = one;

        for (int i = 0; i < n; i++) {
            t[i] = one;
            for (int j = i - 1; j > 0; j--) {
                t[j] = t[j].add(t[j - 1]);
            }
            t[0] = one;
        }

        t[n] = one;
        for (int j = n - 1; j >= k; j--) {
            t[j] = t[j].add(t[j - 1]);
        }

        return t[k];
    }


    /**
     * Enumerates all combinations and calls the callback for each combination.
     *
     * @param callback the callback class
     */
    public void enumerate(final Callback callback) {
        int[] c = new int[m_k];
        for (int j = 0; j < m_k; j++) {
            c[j] = j;
        }

        if (m_k == m_n) {
            callback.visit(c, m_n, m_k, 0, 1);
            return;
        }

        for (long i = 0; i < m_nrOfCombinations; i++) {
            if (!callback.visit(c, m_n, m_k, i, m_nrOfCombinations)) {
                break;
            }

            if (c[m_k - 1] < m_n - 1) {
                c[m_k - 1]++;
            } else {
                for (int j = m_k - 1; j > 0; j--) {
                    if (c[j - 1] < m_n - m_k + j - 1) {
                        c[j - 1]++;
                        for (int s = j; s < m_k; s++) {
                            c[s] = c[j - 1] + s - (j - 1);
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * Enumerates all combinations from rank <em>from</em> until rank
     * <em>to</em> (inclusive). The callback is called for each combination.
     *
     * @param from the first combination to be enumerated
     * @param to the last combination to be enumerated
     * @param callback the callback
     */
    public void enumerate(final long from, final long to,
            final Callback callback) {
        if ((from > m_nrOfCombinations) || (to > m_nrOfCombinations)) {
            throw new IllegalArgumentException("from and/or to must not be "
                    + "greater than the maximum number of possibilities");
        }

        int[] c = unrank(from);

        for (long i = from; i <= to; i++) {
            if (!callback.visit(c, m_n, m_k, i, m_nrOfCombinations)) {
                break;
            }

            if (c[m_k - 1] < m_n - 1) {
                c[m_k - 1]++;
            } else {
                for (int j = m_k - 1; j > 0; j--) {
                    if (c[j - 1] < m_n - m_k + j - 1) {
                        c[j - 1]++;
                        for (int s = j; s < m_k; s++) {
                            c[s] = c[j - 1] + s - (j - 1);
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * Enumerates all combinations and calls the callback for each combination.
     * The enumeration process is carried out in parallel by several threads.
     *
     * @param threads the number of threads to use
     * @param callback the callback class
     *
     * @throws InterruptedException if this thread was interrupted while waiting
     *             for the enumeration to finish
     */
    public void enumerate(final int threads, final Callback callback)
            throws InterruptedException {
        final long pSize = m_nrOfCombinations / threads;

        ThreadPool pool = new ThreadPool(threads - 1);
        for (int i = 0; i < threads; i++) {
            final int k = i;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    enumerate(k * pSize, (k + 1) * pSize - 1, callback);
                }
            };
            pool.submit(r);
        }

        enumerate((threads - 1) * pSize, m_nrOfCombinations, callback);
        pool.waitForTermination();
    }
}
