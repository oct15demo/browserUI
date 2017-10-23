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
 *   19.08.2008 (ohl): created
 */
package org.knime.core.data.vector.bitvector;

import java.util.Arrays;

/**
 * Stores Zeros and Ones in a vector, i.e. with fixed positions. The vector has
 * a fixed length. <br>
 * Implementation stores the bits in a collection of longs (64 bit words). Thus
 * it can be used for well populated vectors. Its length is restricted to
 * ({@link Integer#MAX_VALUE} - 1) * 64 (i.e. 137438953344, in which case it
 * uses around 16GigaByte of memory).<br>
 * The implementation is not thread-safe.
 *
 * @author ohl, University of Konstanz
 */
public class DenseBitVector implements BitVector {

    // number of bits used per storage object
    private static final int STORAGE_BITS = 64;

    // number of shifts need to go from bit index to storage index
    private static final int STORAGE_ADDRBITS = 6;

    // bits are stored in these objects
    private final long[] m_storage;

    // the first storage address containing a set bit
    private int m_firstAddr;

    // the last storage address containing a set bit
    private int m_lastAddr;

    /*
     * could be different from the actual storage length if some bits are left
     * unused "at the end".
     */
    private final long m_length;

    /**
     * Creates a new vector of the specified length, with no bits set.
     *
     * @param length the length of the new bit vector.
     */
    public DenseBitVector(final long length) {
        if (length < 0) {
            throw new IllegalArgumentException(
                    "Length of a BitVector can't be negative.");
        }
        if (length >= (long)(Integer.MAX_VALUE - 1) * (long)STORAGE_BITS) {
            // we need MAX_VALUE internally!
            throw new IllegalArgumentException(
                    "Can't create a vector that big!");
        }
        m_length = length;
        assert ((m_length - 1) >> STORAGE_ADDRBITS) + 1 < Integer.MAX_VALUE;

        // shift with sign extension for length zero
        m_storage = new long[(int)(((m_length - 1) >> STORAGE_ADDRBITS) + 1)];
        m_firstAddr = -1;
        m_lastAddr = Integer.MAX_VALUE;

        assert (checkConsistency() == null);
    }

    /**
     * Creates a new instance taking over the initialization of the bits from
     * the passed array. The array must be build like the one returned by the
     * {@link #getAllBits()} method.
     *
     * @param bits the array containing the initial values of the vector
     * @param length the number of bits to use from the array. If the array is
     *            too long (i.e. contains more than length bits) the additional
     *            bits are ignored. If the array is too short, an exception is
     *            thrown.
     * @throws IllegalArgumentException if length is negative or MAX_VALUE, or
     *             if the length of the argument array is less than (length - 1)
     *             &gt;&gt; 6) + 1
     *
     */
    public DenseBitVector(final long[] bits, final long length) {
        if (length < 0) {
            throw new IllegalArgumentException(
                    "Length of a BitVector can't be negative.");
        }
        if (length >= (long)(Integer.MAX_VALUE - 1) * (long)STORAGE_BITS) {
            // we need MAX_VALUE internally!
            throw new IllegalArgumentException(
                    "Can't create a vector that big!");
        }

        long arrayLength = ((length - 1) >> STORAGE_ADDRBITS) + 1;
        assert arrayLength < Integer.MAX_VALUE;

        if (bits.length < arrayLength) {
            throw new IllegalArgumentException(
                    "Bits array is too short (length=" + bits.length
                            + ") to hold " + length + " bits.");
        }

        m_storage = Arrays.copyOf(bits, (int)arrayLength);
        m_length = length;
        // mask off bits beyond m_length
        maskOffBitsAfterEndOfVector();

        m_firstAddr = findFirstBitAddress();
        m_lastAddr = findLastBitAddress();

        assert (checkConsistency() == null);

    }

    /**
     * Initializes the created bit vector from the hex representation in the
     * passed string. Only characters <code>'0' - '9'</code>,
     * <code>'A' - 'F'</code>and <code>'a' - 'f'</code> are allowed. The
     * character at string position <code>(length - 1)</code> represents the
     * bits with index 0 to 3 in the vector. The character at position 0
     * represents the bits with the highest indices. The length of the vector
     * created is the length of the string times 4 (as each character represents
     * four bits).
     *
     * @param hexString containing the hex value to initialize the vector with
     * @throws IllegalArgumentException if <code>hexString</code> contains
     *             characters other then the hex characters (i.e.
     *             <code>0 - 9, A - F, and 'a' - 'f'</code>)
     */
    public DenseBitVector(final String hexString) {
        this(hexString.length() << 2); // four bits for each character

        if (hexString.length() == 0) {
            return;
        }

        int len = hexString.length();
        for (int c = 0; c < len; c++) {
            long cVal = hexString.charAt(len - c - 1);
            if (cVal >= '0' && cVal <= '9') {
                cVal -= '0';
            } else if (cVal >= 'A' && cVal <= 'F') {
                cVal -= 'A' - 10;
            } else if (cVal >= 'a' && cVal <= 'f') {
                cVal -= 'a' - 10;
            } else {
                throw new IllegalArgumentException(
                        "Invalid character in hex number ('"
                                + hexString.charAt(len - c - 1) + "')");
            }
            // cVal must only use the lower four bits
            assert (cVal & 0xFFFFFFFFFFFFFFF0L) == 0L;

            // sixteen characters go into one qword - each provides four bits
            m_storage[c >> 4] |= cVal << ((c % 16) * 4);
        }

        m_firstAddr = findFirstBitAddress();
        m_lastAddr = findLastBitAddress();
        assert checkConsistency() == null;
    }

    /**
     * Creates a new instance as copy of the passed argument.
     *
     * @param clone the vector to copy into the new instance
     */
    public DenseBitVector(final DenseBitVector clone) {
        if (clone == null) {
            throw new NullPointerException(
                    "Can't initialize from a null vector");
        }
        assert clone.checkConsistency() == null;
        m_storage = Arrays.copyOf(clone.m_storage, clone.m_storage.length);
        m_length = clone.m_length;
        m_firstAddr = clone.m_firstAddr;
        m_lastAddr = clone.m_lastAddr;
        assert checkConsistency() == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long length() {
        return m_length;
    }

    /**
     * Sets all bits in the storage array beyond m_length to zero. Normally
     * there should be no need to call this, as we ensure that no index is
     * specified beyond m_length. Only in {@link #invert()} and when
     * initializing from a long array this might be called.
     */
    private void maskOffBitsAfterEndOfVector() {
        if (m_length % STORAGE_BITS != 0) {
            long mask = ~(-1L << m_length);
            m_storage[m_storage.length - 1] &= mask;
        }
    }

    /**
     * Returns the index of the first storage location that contains a one.
     * Doesn't rely on m_firstAddr or m_lastAddr.
     *
     * @return the index of the first storage object that is not zero
     */
    private int findFirstBitAddress() {
        for (int i = 0; i < m_storage.length; i++) {
            if (m_storage[i] != 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index of the first storage location that contains a one.
     * Doesn't rely on m_firstAddr or m_lastAddr.
     *
     * @return the index of the first storage object that is not zero
     */
    private int findLastBitAddress() {
        for (int i = m_storage.length - 1; i >= 0; i--) {
            if (m_storage[i] != 0) {
                return i;
            }
        }
        return Integer.MAX_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(final long bitIdx, final boolean value) {
        if (value) {
            set(bitIdx);
        } else {
            clear(bitIdx);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(final long bitIdx) {
        assert (checkConsistency() == null);
        if (bitIdx >= m_length) {
            throw new ArrayIndexOutOfBoundsException("Index ('" + bitIdx
                    + "') too large for vector of length " + m_length);
        }
        assert bitIdx >> STORAGE_ADDRBITS < Integer.MAX_VALUE;
        int storageAddr = (int)(bitIdx >> STORAGE_ADDRBITS);
        int storageIdx = (int)(bitIdx % STORAGE_BITS);

        m_storage[storageAddr] = m_storage[storageAddr] | (1L << storageIdx);

        if (m_firstAddr < 0 || m_firstAddr > storageAddr) {
            m_firstAddr = storageAddr;
        }
        if (m_lastAddr >= m_storage.length || m_lastAddr < storageAddr) {
            m_lastAddr = storageAddr;
        }
        assert (checkConsistency() == null);
    }

    /**
     * Sets all bits in the specified range to the new value. The bit at index
     * startIdx is included, the endIdx is not included in the change. The
     * endIdx can't be smaller than the startIdx. If the indices are equal, no
     * change is made.
     *
     * @param startIdx the index of the first bit to set to the new value
     * @param endIdx the index of the last bit to set to the new value
     * @param value if set to true the bits are set to one, otherwise to zero
     */
    public void set(final long startIdx, final long endIdx,
            final boolean value) {
        if (value) {
            set(startIdx, endIdx);
        } else {
            clear(startIdx, endIdx);
        }
    }

    /**
     * Sets all bits in the specified range. The bit at index startIdx is
     * included, the endIdx is not included in the change. The endIdx can't be
     * smaller than the startIdx. If the indices are equal, no change is made.
     *
     * @param startIdx the index of the first bit to set to one
     * @param endIdx the index of the last bit to set to one
     */
    public void set(final long startIdx, final long endIdx) {
        assert (checkConsistency() == null);
        if (endIdx < startIdx) {
            throw new IllegalArgumentException("The end index can't be smaller"
                    + " than the start index.");
        }
        if (endIdx > m_length) {
            throw new ArrayIndexOutOfBoundsException("Index ('" + endIdx
                    + "') too large for vector of length " + m_length);
        }
        if (endIdx == startIdx) {
            return;
        }
        assert startIdx >> STORAGE_ADDRBITS < Integer.MAX_VALUE;
        assert endIdx - 1 >> STORAGE_ADDRBITS < Integer.MAX_VALUE;

        int storageStartAddr = (int)(startIdx >> STORAGE_ADDRBITS);
        // endIdx is not supposed to be changed
        int storageEndAddr = (int)(endIdx - 1 >> STORAGE_ADDRBITS);

        long firstMask = -1L << startIdx;
        long lastMask = ~(-1L << endIdx);
        if (endIdx % STORAGE_BITS == 0) {
            lastMask = -1L;
        }
        if (storageStartAddr == storageEndAddr) {
            // range fully lies in one storage object
            m_storage[storageStartAddr] |= firstMask & lastMask;
            if (m_firstAddr < 0 || m_firstAddr > storageStartAddr) {
                m_firstAddr = storageStartAddr;
            }
            if (m_lastAddr >= m_storage.length || m_lastAddr < storageEndAddr) {
                m_lastAddr = storageEndAddr;
            }

        } else {
            int addr = storageStartAddr;
            // apply first mask to first storage address
            m_storage[addr++] |= firstMask;
            // set all addresses in-between to all '1's
            while (addr < storageEndAddr) {
                m_storage[addr++] = -1L;
            }
            // apply last mask to last storage address
            m_storage[addr] |= lastMask;

            if (m_firstAddr < 0 || m_firstAddr > storageStartAddr) {
                m_firstAddr = storageStartAddr;
            }
            if (m_lastAddr >= m_storage.length || m_lastAddr < storageEndAddr) {
                m_lastAddr = storageEndAddr;
            }
        }
        assert (checkConsistency() == null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear(final long bitIdx) {
        assert (checkConsistency() == null);
        if (bitIdx >= m_length) {
            throw new ArrayIndexOutOfBoundsException("Index ('" + bitIdx
                    + "') too large for vector of length " + m_length);
        }

        assert bitIdx >> STORAGE_ADDRBITS < Integer.MAX_VALUE;
        int storageAddr = (int)(bitIdx >> STORAGE_ADDRBITS);
        int storageIdx = (int)(bitIdx % STORAGE_BITS);
        m_storage[storageAddr] = m_storage[storageAddr] & (~(1L << storageIdx));

        if (storageAddr == m_firstAddr && m_storage[storageAddr] == 0) {
            // we just cleared the first bit...
            m_firstAddr = findFirstBitAddress();
        }
        if (storageAddr == m_lastAddr && m_storage[storageAddr] == 0) {
            // we just cleared the last bit...
            m_lastAddr = findLastBitAddress();
        }
        assert (checkConsistency() == null);

    }

    /**
     * Clears all bits in the specified range. The bit at index startIdx is
     * included, the endIdx is not included in the change. The endIdx can't be
     * smaller than the startIdx. If the indices are equal, no change is made.
     *
     * @param startIdx the index of the first bit to set to zero
     * @param endIdx the index of the last bit to set to zero
     */
    public void clear(final long startIdx, final long endIdx) {
        assert (checkConsistency() == null);
        if (endIdx < startIdx) {
            throw new IllegalArgumentException("The end index can't be smaller"
                    + " than the start index.");
        }
        if (endIdx > m_length) {
            throw new ArrayIndexOutOfBoundsException("Endindex ('" + endIdx
                    + "') too large for vector of length " + m_length);
        }
        if (endIdx == startIdx) {
            return;
        }

        assert startIdx >> STORAGE_ADDRBITS < Integer.MAX_VALUE;
        assert endIdx - 1 >> STORAGE_ADDRBITS < Integer.MAX_VALUE;
        int storageStartAddr = (int)(startIdx >> STORAGE_ADDRBITS);
        // last index is not supposed to be changed
        int storageEndAddr = (int)(endIdx - 1 >> STORAGE_ADDRBITS);

        long firstMask = -1L << startIdx;
        long lastMask = ~(-1L << endIdx);
        if (endIdx % STORAGE_BITS == 0) {
            lastMask = -1L;
        }
        if (storageStartAddr == storageEndAddr) {
            // range fully lies in one storage object
            m_storage[storageStartAddr] &= ~(firstMask & lastMask);
        } else {
            int addr = storageStartAddr;
            // apply first mask to first storage address
            m_storage[addr++] &= ~firstMask;
            // set all addresses in-between to all '0's
            while (addr < storageEndAddr) {
                m_storage[addr++] = 0;
            }
            // apply last mask to last storage address
            m_storage[addr] &= ~lastMask;
        }
        if (storageStartAddr <= m_firstAddr && storageEndAddr >= m_firstAddr) {
            m_firstAddr = findFirstBitAddress();
        }
        if (storageStartAddr <= m_lastAddr && storageEndAddr >= m_lastAddr) {
            m_lastAddr = findLastBitAddress();
        }

        assert (checkConsistency() == null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public long cardinality() {
        assert (checkConsistency() == null);
        long result = 0;
        // because we make sure no bits are set beyond the length of the vector
        // we can just count all ones
        if (m_firstAddr == -1) {
            return 0;
        }
        for (int i = m_firstAddr; i <= m_lastAddr; i++) {
            result += Long.bitCount(m_storage[i]);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        assert (checkConsistency() == null);
        return m_firstAddr == -1;
    }

    /**
     * Returns true, if this and the argument vector have at least one bit set
     * at the same position.
     *
     * @param bv the vector to test
     * @return true, if this and the argument vector have at least one bit set
     *         at the same position.
     */
    public boolean intersects(final DenseBitVector bv) {
        assert (checkConsistency() == null);

        if (bv.isEmpty() || isEmpty()) {
            return false;
        }
        int startIdx = Math.max(m_firstAddr, bv.m_firstAddr);
        int endIdx = Math.min(m_lastAddr, bv.m_lastAddr);
        if (startIdx > endIdx) {
            return false;
        }

        for (int i = startIdx; i <= endIdx; i++) {
            if ((m_storage[i] & bv.m_storage[i]) != 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean get(final long bitIdx) {
        assert (checkConsistency() == null);
        if (bitIdx >= m_length) {
            throw new ArrayIndexOutOfBoundsException("Index ('" + bitIdx
                    + "') too large for vector of length " + m_length);
        }
        assert bitIdx >> STORAGE_ADDRBITS < Integer.MAX_VALUE;
        int storageAddr = (int)(bitIdx >> STORAGE_ADDRBITS);
        int storageIdx = (int)(bitIdx % STORAGE_BITS);
        return (m_storage[storageAddr] & (1L << storageIdx)) != 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long nextSetBit(final long startIdx) {
        assert (checkConsistency() == null);
        if (startIdx >= m_length) {
            return -1;
        }
        if (startIdx < 0) {
            throw new ArrayIndexOutOfBoundsException(
                    "Starting index can't be negative.");
        }

        if (m_firstAddr == -1) {
            // there is no bit set in this vector
            return -1;
        }

        assert startIdx >> STORAGE_ADDRBITS < Integer.MAX_VALUE;
        int storageAddr = (int)(startIdx >> STORAGE_ADDRBITS);
        int storageIdx;

        if (storageAddr >= m_firstAddr) {
            storageIdx = (int)(startIdx % STORAGE_BITS);
        } else {
            // lets start with the first used storage object
            storageAddr = m_firstAddr;
            storageIdx = 0;
        }

        // mask off the bits before the startIdx
        long bits = m_storage[storageAddr] & (-1L << storageIdx);

        while (true) {
            if (bits != 0) {
                return ((long)STORAGE_BITS * (long)storageAddr)
                        + Long.numberOfTrailingZeros(bits);
            }
            storageAddr++;
            if (storageAddr > m_lastAddr) {
                break;
            }
            bits = m_storage[storageAddr];
        }
        // no further '1's in this vector
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long nextClearBit(final long startIdx) {
        assert (checkConsistency() == null);
        if (startIdx >= m_length) {
            return -1;
        }
        if (startIdx < 0) {
            throw new ArrayIndexOutOfBoundsException(
                    "Starting index can't be negative.");
        }

        assert startIdx >> STORAGE_ADDRBITS < Integer.MAX_VALUE;
        int storageAddr = (int)(startIdx >> STORAGE_ADDRBITS);
        int storageIdx = (int)(startIdx % STORAGE_BITS);

        if (m_firstAddr == -1 || storageAddr < m_firstAddr
                || storageAddr > m_lastAddr) {
            /*
             * there is no bit set in this vector, or we are outside the range
             * where bits are set
             */
            return startIdx;
        }

        /*
         * As java.lang.Long only counts trailing zeros (and not ones) we invert
         * the bits and look for ones.
         */

        // mask off the bits before the startIdx
        long bits = ~m_storage[storageAddr] & (-1L << storageIdx);

        while (m_lastAddr > storageAddr) {
            if (bits != 0) {
                return ((long)STORAGE_BITS * (long)storageAddr)
                        + Long.numberOfTrailingZeros(bits);
            }
            bits = ~m_storage[++storageAddr];
        }

        // bits contains the last used storage object.
        // (note, if it is fully set 'bits' will be zero and
        // numberOfTrailingZeros returns 64 - which is fine.)
        long result =
                ((long)STORAGE_BITS * (long)storageAddr)
                        + Long.numberOfTrailingZeros(bits);
        if (result >= m_length) {
            return -1;
        } else {
            return result;
        }

    }

    /**
     * Creates and returns a new bit vector that contains a subsequence of this
     * vector, beginning with the bit at index <code>startIdx</code> and with
     * its last bit being this' bit at position <code>endIdx - 1</code>. The
     * length of the result vector is <code>endIdx - startIdx</code>. If
     * <code>startIdx</code> equals <code>endIdx</code> a vector of length
     * zero is returned.
     *
     * @param startIdx the startIdx of the subsequence
     * @param endIdx the first bit in this vector after startIdx that is not
     *            included in the result sequence.
     * @return a new vector of length <code>endIdx - startIdx</code>
     *         containing the subsequence of this vector from
     *         <code>startIdx</code> (included) to <code>endIdx</code> (not
     *         included anymore).
     */
    public DenseBitVector subSequence(final long startIdx, final long endIdx) {

        if (startIdx < 0 || endIdx > m_length || endIdx < startIdx) {
            throw new IllegalArgumentException("Illegal range for subsequense."
                    + "(startIdx=" + startIdx + ", endIdx=" + endIdx
                    + ", length = " + m_length + ")");
        }

        DenseBitVector result = new DenseBitVector(endIdx - startIdx);

        int startAddr = (int)(startIdx >> STORAGE_ADDRBITS);
        int addrCount = (int)((endIdx - startIdx - 1) >> STORAGE_ADDRBITS) + 1;
        if (startIdx == endIdx || startAddr > m_lastAddr
                || (endIdx - 1) >> STORAGE_ADDRBITS < m_firstAddr) {
            // if the range is null, or no bits are in the range
            return result;
        }

        long storageMask = STORAGE_BITS - 1;

        boolean aligned = ((startIdx % STORAGE_BITS) == 0);

        // Process all words but the last word
        for (int i = 0; i < addrCount - 1; i++, startAddr++) {
            if (aligned) {
                result.m_storage[i] = m_storage[startAddr];
            } else {
                result.m_storage[i] =
                        (m_storage[startAddr] >>> startIdx)
                                | (m_storage[startAddr + 1] << -startIdx);

            }
        }
        // Process the last word
        long lastWordMask = -1L >>> -endIdx;
        if (((endIdx - 1) & storageMask) < (startIdx & storageMask)) {
            result.m_storage[addrCount - 1] =
                    (m_storage[startAddr] >>> startIdx)
                            | (m_storage[startAddr + 1] & lastWordMask)
                            << -startIdx;
        } else {
            result.m_storage[addrCount - 1] =
                    ((m_storage[startAddr] & lastWordMask) >>> startIdx);
        }

        // Set wordsInUse correctly
        result.m_firstAddr = result.findFirstBitAddress();
        result.m_lastAddr = result.findLastBitAddress();
        assert result.checkConsistency() == null;

        return result;

    }

    /**
     * Creates and returns a new bit vector whose bits are set at positions
     * where both, this and the argument vector have their bits set. The length
     * of the new vector is the maximum of the length of this and the argument.
     *
     * @param bv the vector to AND this one with
     * @return a new instance containing the result of the AND operation
     */
    public DenseBitVector and(final DenseBitVector bv) {
        assert (checkConsistency() == null);
        DenseBitVector result =
                new DenseBitVector(Math.max(m_length, bv.m_length));

        if (isEmpty() || bv.isEmpty()) {
            assert (result.checkConsistency() == null);
            return result;
        }

        int startAddr = Math.max(m_firstAddr, bv.m_firstAddr);
        int endAddr = Math.min(m_lastAddr, bv.m_lastAddr);
        if (endAddr < startAddr) {
            // no intersection of ones
            assert (result.checkConsistency() == null);
            return result;
        }

        for (int i = startAddr; i <= endAddr; i++) {
            result.m_storage[i] = m_storage[i] & bv.m_storage[i];
        }
        result.m_firstAddr = result.findFirstBitAddress();
        result.m_lastAddr = result.findLastBitAddress();

        assert (result.checkConsistency() == null);
        return result;

    }

    /**
     * Creates and returns a new bit vector whose bits are set at positions
     * where at least one of the vectors (this or the argument vector) have a
     * bit set. The length of the new vector is the maximum of the length of
     * this and the argument.
     *
     * @param bv the vector to OR this one with
     * @return a new instance containing the result of the OR operation
     */
    public DenseBitVector or(final DenseBitVector bv) {
        assert (checkConsistency() == null);
        DenseBitVector result =
                new DenseBitVector(Math.max(m_length, bv.m_length));

        // check if one of them is empty
        if (isEmpty()) {
            if (bv.isEmpty()) {
                return result;
            }
            // just copy bv's array
            System.arraycopy(bv.m_storage, 0, result.m_storage, 0,
                    bv.m_storage.length);
            result.m_firstAddr = bv.m_firstAddr;
            result.m_lastAddr = bv.m_lastAddr;
            assert (result.checkConsistency() == null);
            return result;
        } else {
            if (bv.isEmpty()) {
                // just copy this' array
                System.arraycopy(m_storage, 0, result.m_storage, 0,
                        m_storage.length);
                result.m_firstAddr = m_firstAddr;
                result.m_lastAddr = m_lastAddr;
                assert (result.checkConsistency() == null);
                return result;
            }
        }

        /*
         * TODO: Only the intersection of both used address spaces actually
         * needs to be ORed. The non-intersecting regions (from the one
         * firstAddr to the firstAddr of the other operand and from the one
         * lastAddr to the other lastAddr of the other operand) could be just
         * copied.
         */
        int startAddr = Math.min(m_firstAddr, bv.m_firstAddr);
        int endAddr = Math.max(m_lastAddr, bv.m_lastAddr);

        for (int i = startAddr; i <= endAddr; i++) {
            result.m_storage[i] = (i < m_storage.length ?  m_storage[i] : 0)
                    | (i < bv.m_storage.length ? bv.m_storage[i] : 0);
        }
        result.m_firstAddr = startAddr;
        result.m_lastAddr = endAddr;

        assert (result.checkConsistency() == null);
        return result;

    }

    /**
     * Creates and returns a new bit vector whose bits are set at positions
     * where (exactly) one of the vectors (this or the argument vector) have a
     * bit set. The length of the new vector is the maximum of the length of
     * this and the argument.
     *
     * @param bv the vector to XOR this one with
     * @return a new instance containing the result of the XOR operation
     */
    public DenseBitVector xor(final DenseBitVector bv) {
        assert (checkConsistency() == null);

        DenseBitVector result =
                new DenseBitVector(Math.max(m_length, bv.m_length));

        // check if one of them is empty
        if (isEmpty()) {
            if (bv.isEmpty()) {
                return result;
            }
            // just copy bv's array
            System.arraycopy(bv.m_storage, 0, result.m_storage, 0,
                    bv.m_storage.length);
            result.m_firstAddr = bv.m_firstAddr;
            result.m_lastAddr = bv.m_lastAddr;
            assert (result.checkConsistency() == null);
            return result;
        } else {
            if (bv.isEmpty()) {
                // just copy this' array
                System.arraycopy(m_storage, 0, result.m_storage, 0,
                        m_storage.length);
                result.m_firstAddr = m_firstAddr;
                result.m_lastAddr = m_lastAddr;
                assert (result.checkConsistency() == null);
                return result;
            }
        }

        /*
         * TODO: Only the intersection of both used address spaces actually
         * needs to be XORed. The non-intersecting regions (from the one
         * firstAddr to the firstAddr of the other operand and from the one
         * lastAddr to the other lastAddr of the other operand) could be just
         * copied.
         */
        int startAddr = Math.min(m_firstAddr, bv.m_firstAddr);
        int endAddr = Math.max(m_lastAddr, bv.m_lastAddr);

        for (int i = startAddr; i <= endAddr; i++) {
            result.m_storage[i] = (i < m_storage.length ? m_storage[i] : 0)
                    ^ (i < bv.m_storage.length ? bv.m_storage[i] : 0);
        }
        result.m_firstAddr = result.findFirstBitAddress();
        result.m_lastAddr = result.findLastBitAddress();

        assert (result.checkConsistency() == null);
        return result;
    }

    /**
     * Creates and returns a new bit vector whose bits are inverted compared to
     * this vector. The bits of the result are set at positions where this
     * vector has a cleared bit and vice versa. The result vector has the same
     * length as this vector.
     *
     * @return a new instance containing the inverted bits of this vector
     */
    public DenseBitVector invert() {
        assert (checkConsistency() == null);
        DenseBitVector result = new DenseBitVector(m_length);

        if (isEmpty()) {
            // this might be faster
            if (m_length > 0) {
                Arrays.fill(result.m_storage, -1L);
                result.m_firstAddr = 0;
                result.m_lastAddr = result.m_storage.length - 1;
                // mask off the bits beyond m_length
                result.maskOffBitsAfterEndOfVector();

            }
            assert (result.checkConsistency() == null);
            return result;
        }
        for (int i = 0; i < m_storage.length; i++) {
            result.m_storage[i] = ~m_storage[i];
        }
        result.maskOffBitsAfterEndOfVector();
        result.m_firstAddr = result.findFirstBitAddress();
        result.m_lastAddr = result.findLastBitAddress();
        assert (result.checkConsistency() == null);
        return result;
    }

    /**
     * Creates and returns a new bit vector that contains copies of both (this
     * and the argument vector). The argument vector is appended at the end of
     * this vector, i.e. it's bit with index zero will be stored at index
     * "length-of-this-vector" in the result vector. The length of the result is
     * the length of this plus the length of the argument vector.
     *
     * @param bv the vector to append at the end of this
     * @return a new instance containing both vectors concatenated
     */
    public DenseBitVector concatenate(final DenseBitVector bv) {
        assert (checkConsistency() == null);
        DenseBitVector result = new DenseBitVector(m_length + bv.m_length);

        // first we always copy this' storage - unless its all zeros
        if (!isEmpty()) {
            System.arraycopy(m_storage, 0, result.m_storage, 0,
                    m_storage.length);
            result.m_firstAddr = m_firstAddr;
        }

        if (bv.isEmpty()) {
            result.m_lastAddr = m_lastAddr;
            assert (result.checkConsistency() == null);
            return result;
        }

        if (m_length % STORAGE_BITS == 0) {
            // this' array is aligned - just copy bv's array
            System.arraycopy(bv.m_storage, 0, result.m_storage,
                    m_storage.length, bv.m_storage.length);
            // we know bv is not empty (that's handled above)
            if (isEmpty()) {
                result.m_firstAddr = bv.m_firstAddr + m_storage.length;
            }
            result.m_lastAddr = bv.m_lastAddr + m_storage.length;
            assert (result.checkConsistency() == null);
            return result;
        }

        // cut a piece from bv's storage that fills the "rest" of our storage
        int leftover = (int)(m_length % STORAGE_BITS);

        int resultAddr = m_storage.length - 1;
        int bvAddr = 0;
        while (bvAddr <= bv.m_lastAddr) {
            result.m_storage[resultAddr] |= bv.m_storage[bvAddr] << leftover;
            if (resultAddr < result.m_storage.length - 1) {
                result.m_storage[resultAddr + 1] =
                        bv.m_storage[bvAddr] >>> (STORAGE_BITS - leftover);
            }
            bvAddr++;
            resultAddr++;
        }

        result.m_firstAddr = result.findFirstBitAddress();
        result.m_lastAddr = result.findLastBitAddress();
        assert (result.checkConsistency() == null);

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        assert (checkConsistency() == null);
        long hash = 0;
        if (m_firstAddr > -1) {
            for (int storageAddr = m_firstAddr; storageAddr <= m_lastAddr; storageAddr++) {
                for (int storageIdx = 0; storageIdx < STORAGE_BITS; storageIdx++) {
                    long index = (storageAddr << STORAGE_ADDRBITS) + storageIdx;
                    if (index >= m_length) {
                        break;
                    }
                    if ((m_storage[storageAddr] & (1L << storageIdx)) != 0) {
                        hash = hash * 524287 + (index + 1);
                    }
                }
            }
        }
        return (int) (hash ^ (hash >> 32));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        assert (checkConsistency() == null);
        if (!(obj instanceof DenseBitVector)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        DenseBitVector o = (DenseBitVector)obj;

        assert (o.checkConsistency() == null);
        if (o.m_firstAddr != m_firstAddr || o.m_lastAddr != m_lastAddr) {
            return false;
        }
        if (m_firstAddr == -1) {
            // all empty.
            return true;
        }
        for (int i = m_firstAddr; i <= m_lastAddr; i++) {
            if (o.m_storage[i] != m_storage[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a string containing (comma separated) indices of the bits set in
     * this vector. The number of bit indices added to the string is limited to
     * {@link BitVectorValue#MAX_DISPLAY_BITS}. If the output is truncated, the string ends on &quot;... }&quot;
     *
     * @return a string containing (comma separated) indices of the bits set in
     *         this vector.
     */
    @Override
    public String toString() {
        assert (checkConsistency() == null);
        long ones = cardinality();

        int use = (int)Math.min(ones, BitVectorValue.MAX_DISPLAY_BITS);

        StringBuilder result = new StringBuilder(use * 7);
        result.append("{length=").append(m_length).append(", set bits=");
        for (long i = nextSetBit(0); i > -1; i = nextSetBit(++i)) {
            result.append(i).append(", ");
        }
        if (use < ones) {
            result.append("... ");
        } else if (result.length() > 2) {
            result.delete(result.length() - 2, result.length());
        }
        result.append('}');
        return result.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toHexString() {
        // the number of bits we store in the string
        long max = Math.min(m_length, BitVectorValue.MAX_DISPLAY_BITS);

        // 4 bits are combined to one character
        StringBuilder result = new StringBuilder((int)(max >> 2));

        // the last storage might not be fully used
        int leftOver = (int)(max % STORAGE_BITS);

        // start with the highest bits

        int storageAddr = (int)((max - 1) >> STORAGE_ADDRBITS);
        assert storageAddr <= m_storage.length;

        int nibbleIdx;
        if (leftOver == 0) {
            nibbleIdx = 15;
        } else {
            nibbleIdx = (leftOver - 1) >> 2;
        }
        while (storageAddr >= 0) {

            while (nibbleIdx >= 0) {
                int value = (int)(m_storage[storageAddr] >>> (nibbleIdx << 2));
                value &= 0x0f;

                value += '0';
                if (value > '9') {
                    value += ('A' - ('9' + 1));
                }
                // add character to string
                result.append((char)(value));

                nibbleIdx--;
            }
            // a 64bit word stores 16 nibbles
            nibbleIdx = 15;
            storageAddr--;
        }

        if (max < m_length) {
            result.insert(0, "...");
        }
        return result.toString();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toBinaryString() {
        // the number of bits we store in the string
        int max = (int)Math.min(m_length, BitVectorValue.MAX_DISPLAY_BITS);

        StringBuilder result = new StringBuilder(max);
        if (max == 0) {
            return result.toString();
        }

        // start with the highest bits
        int storageAddr = ((max - 1) >> STORAGE_ADDRBITS);
        int storageIdx = ((max - 1) % STORAGE_BITS);

        while (storageAddr >= 0) {

            while (storageIdx >= 0) {
                if ((m_storage[storageAddr] & (1L << storageIdx)) == 0) {
                    result.append('0');
                } else {
                    result.append('1');
                }
                storageIdx--;
            }
            storageIdx = STORAGE_BITS - 1;
            storageAddr--;
        }

        if (max < m_length) {
            result.append("...");
        }
        return result.toString();

    }

    /**
     * There are certain conditions the implementation depends on. They are all
     * checked in here. Normally the method should return null. If it doesn't
     * something is fishy and a error message is returned. NOTE: This method is
     * not cheap! It should be called in an assert statement only.
     *
     * @return the error message, or null if everything is alright
     */
    private String checkConsistency() {
        /*
         * This code has been commented out for performance reasons.
         * If development starts again on this class, it could be re-activated.
        if (m_firstAddr != findFirstBitAddress()) {
            return "m_firstAddress is not set properly";
        }

        if (m_lastAddr != findLastBitAddress()) {
            return "m_lastAddress is not set properly";
        }

        if (m_length > 0) {
            assert (m_length - 1) >> STORAGE_ADDRBITS < Integer.MAX_VALUE;
            int highestIdx = (int)((m_length - 1) >> STORAGE_ADDRBITS);
            if (m_storage.length <= highestIdx) {
                return "Storage array is too short";
            }
            // storage shouldn't be too long either
            if (m_storage.length > ((m_length - 1) >> STORAGE_ADDRBITS) + 1) {
                return "Storage array is too long";
            }
            // make sure there are no bits set "above" m_length
            assert m_length >> STORAGE_ADDRBITS < Integer.MAX_VALUE;
            int addr = (int)(m_length >> STORAGE_ADDRBITS);
            int idx = (int)(m_length % STORAGE_BITS);
            while (addr < m_storage.length) {
                if ((m_storage[addr] & (1L << idx)) != 0) {
                    return "A bit is set outside the vector's length";
                }
                idx++;
                addr += idx / STORAGE_BITS;
                idx = idx % STORAGE_BITS;
            }
        } else {
            // if length is zero, the storage should be of length zero too
            if (m_storage.length > 0) {
                return "Vector length is zero, but has a storage allocated";
            }

        }
        */
        return null;
    }

    /**
     * Returns a copy of the internal storage of all bits. The vector's bit with
     * index zero is stored in the array at index zero and at the right-most bit
     * (LSB) of the long word. In general bit with index i is stored in the long
     * word at index (i &gt;&gt;&gt; 6) in the array and in this long word at
     * bit position (index) i % 64. The length of the returned array is
     * ((vector_length - 1) &gt;&gt; 6) + 1.
     *
     * @return a copy of the internal representation of the bits in this vector.
     */
    public long[] getAllBits() {
        return m_storage.clone();
    }

    /**
     * Returns a multi-line dump of the internal storage.
     *
     * @return a multi-line dump of the internal storage.
     */
    public String dumpBits() {
        assert (checkConsistency() == null);
        if (m_length == 0) {
            return "<bitvector of length zero>";
        }
        StringBuilder result =
                new StringBuilder(m_storage.length * (STORAGE_BITS + 15));
        for (int i = m_storage.length - 1; i >= 0; i--) {
            result.append("[");
            String s = Long.toBinaryString(m_storage[i]);
            if (s.length() < STORAGE_BITS) {
                char[] z = new char[STORAGE_BITS - s.length()];
                Arrays.fill(z, '0');
                result.append(z);
            }
            result.append(s);
            result.append("] ");

            result.append(((i + 1) * STORAGE_BITS) - 1);
            result.append("-");
            result.append(i * STORAGE_BITS);
            result.append("\n");
        }
        return result.toString();
    }

    /**
     * Computes the cardinality of the intersection with the given bitVector.
     *
     * @see BitVectorUtil#cardinalityOfIntersection(BitVectorValue, BitVectorValue)
     * @param bitVector the other operand for the AND operator
     * @return the cardinality of the intersection
     */
    long cardinalityOfIntersection(final DenseBitVector bitVector) {
        if (isEmpty() || bitVector.isEmpty()) {
            return 0;
        }

        int startAddr = Math.max(m_firstAddr, bitVector.m_firstAddr);
        int endAddr = Math.min(m_lastAddr, bitVector.m_lastAddr);

        long result = 0;
        long[] otherStorage = bitVector.m_storage;
        for (int i = startAddr; i <= endAddr; i++) {
                result += Long.bitCount(m_storage[i] & otherStorage[i]);
        }
        return result;
    }

    /**
     * Computes the cardinality of the complement relative to the given bitVector.
     *
     * @see BitVectorUtil#cardinalityOfRelativeComplement(BitVectorValue, BitVectorValue)
     * @param bitVector the other operand
     * @return the cardinality of the intersection
     */
    long cardinalityOfRelativeComplement(final DenseBitVector bitVector) {
        if (isEmpty()) {
            return 0;
        }
        long result = 0;
        long[] otherStorage = bitVector.m_storage;
        for (int i = m_firstAddr; i <= m_lastAddr; i++) {
            if (i < otherStorage.length) {
                result += Long.bitCount(m_storage[i] & ~otherStorage[i]);
            } else {
                result += Long.bitCount(m_storage[i]);
            }
        }
        return result;
    }
}
