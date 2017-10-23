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
 *   06.11.2008 (thiel): created
 */
package org.knime.core.data.vector.bitvector;

import org.knime.core.node.util.CheckUtils;

/**
 * A utility class providing methods to apply set operations like "AND", "OR", and "XOR" on different kind of
 * {@link BitVectorValue}s, such as {@link SparseBitVectorCell}s or {@link DenseBitVectorCell}s in a convenient way.
 *
 * @author Kilian Thiel, University of Konstanz
 * @author Marcel Hanser, University of Konstanz
 */
public final class BitVectorUtil {

    private BitVectorUtil() { /*empty*/
    }

    /**
     * Creates a sparse bit vector cell, in case that one or both given values are sparse bit vector cells (otherwise a
     * dense bit vector cell). The returned cell contains the result of the AND operation on the passed operands. The
     * length of the result vector is the maximum of the lengths of the operands.<br>
     * NOTE: This method performs best if the two arguments are both {@link SparseBitVectorCell}s or
     * {@link DenseBitVectorCell}s. All other implementations need to access the bits through get/set methods which
     * probably performs very poorly.<br>
     *
     * To perform the AND operation the sparse implementation
     * {@link SparseBitVectorCellFactory#and(BitVectorValue, BitVectorValue)}, or the dense implementation
     * {@link DenseBitVectorCellFactory#and(BitVectorValue, BitVectorValue)} is called.
     *
     * @param bv1 the first operand to AND with the other
     * @param bv2 the other operand to AND with the first one
     * @return the result of the AND operation
     */
    public static BitVectorValue and(final BitVectorValue bv1, final BitVectorValue bv2) {
        if (bv1 == null || bv2 == null) {
            throw new NullPointerException("Given BitVectorValues may not be null!");
        }

        int noSparseBVC = sparseBitVectorCellCount(bv1, bv2);
        if (noSparseBVC >= 1) {
            return SparseBitVectorCellFactory.and(bv1, bv2);
        }
        return DenseBitVectorCellFactory.and(bv1, bv2);
    }

    /**
     * Creates a sparse bit vector cell, in case that both given values are sparse bit vector cells (otherwise a dense
     * bit vector cell). The returned cell contains the result of the OR operation on the passed operands. The length of
     * the result vector is the maximum of the lengths of the operands.<br>
     * NOTE: This method performs best if the two arguments are both {@link SparseBitVectorCell}s or
     * {@link DenseBitVectorCell}s. All other implementations need to access the bits through get/set methods which
     * probably performs very poorly.<br>
     *
     * To perform the OR operation the sparse implementation
     * {@link SparseBitVectorCellFactory#or(BitVectorValue, BitVectorValue)}, or the dense implementation
     * {@link DenseBitVectorCellFactory#or(BitVectorValue, BitVectorValue)} is called.
     *
     * @param bv1 the first operand to OR with the other
     * @param bv2 the other operand to OR with the first one
     * @return the result of the OR operation
     */
    public static BitVectorValue or(final BitVectorValue bv1, final BitVectorValue bv2) {
        if (bv1 == null || bv2 == null) {
            throw new NullPointerException("Given BitVectorValues may not be null!");
        }

        int noSparseBVC = sparseBitVectorCellCount(bv1, bv2);
        if (noSparseBVC == 2) {
            return SparseBitVectorCellFactory.or(bv1, bv2);
        }
        return DenseBitVectorCellFactory.or(bv1, bv2);
    }

    /**
     * Creates a sparse bit vector cell, in case that both given values are sparse bit vector cells (otherwise a dense
     * bit vector cell). The returned cell contains the result of the XOR operation on the passed operands. The length
     * of the result vector is the maximum of the lengths of the operands.<br>
     * NOTE: This method performs best if the two arguments are both {@link SparseBitVectorCell}s or
     * {@link DenseBitVectorCell}s. All other implementations need to access the bits through get/set methods which
     * probably performs very poorly.<br>
     *
     * To perform the XOR operation the sparse implementation
     * {@link SparseBitVectorCellFactory#xor(BitVectorValue, BitVectorValue)}, or the dense implementation
     * {@link DenseBitVectorCellFactory#xor(BitVectorValue, BitVectorValue)} is called.
     *
     * @param bv1 the first operand to XOR with the other
     * @param bv2 the other operand to XOR with the first one
     * @return the result of the XOR operation
     */
    public static BitVectorValue xor(final BitVectorValue bv1, final BitVectorValue bv2) {
        if (bv1 == null || bv2 == null) {
            throw new NullPointerException("Given BitVectorValues may not be null!");
        }

        int noSparseBVC = sparseBitVectorCellCount(bv1, bv2);
        if (noSparseBVC == 2) {
            return SparseBitVectorCellFactory.xor(bv1, bv2);
        }
        return DenseBitVectorCellFactory.xor(bv1, bv2);
    }

    /**
     * Computes the cardinality of the bitwise AND of the given vectors. This method is significantly faster than using
     * if both BitVectorValues are from the same type:
     *
     * <pre>
     * long cardOfIntersection = BitVectorUtil.and(bv1, bv2).cardinality();
     * </pre>
     *
     * @param bv1 first vector
     * @param bv2 second vector
     * @return cardinality of the bitwise AND operator of the given bit vectors
     * @throws NullPointerException if any argument is <code>null</code>
     * @since 2.10
     */
    public static long cardinalityOfIntersection(final BitVectorValue bv1, final BitVectorValue bv2) {
        CheckUtils.checkNotNull(bv1, "Given BitVectorValues may not be null!");
        CheckUtils.checkNotNull(bv2, "Given BitVectorValues may not be null!");

        if (bv1 instanceof DenseBitVectorCell && bv2 instanceof DenseBitVectorCell) {
            return ((DenseBitVectorCell)bv1).cardinalityOfIntersection((DenseBitVectorCell)bv2);
        } else if (bv1 instanceof SparseBitVectorCell && bv2 instanceof SparseBitVectorCell) {
            return ((SparseBitVectorCell)bv1).cardinalityOfIntersection((SparseBitVectorCell)bv2);
        }

        // we have to go into the loop
        long bv1Idx = bv1.nextSetBit(0);

        long toReturn = 0;
        while (bv1Idx >= 0 && bv1Idx < bv2.length()) {
            if (bv2.get(bv1Idx)) {
                // both vectors have 1 at the index.
                toReturn++;
            }
            bv1Idx = bv1.nextSetBit(bv1Idx + 1);
        }
        return toReturn;
    }

    /**
     * Computes the cardinality of the relative complement of the given vectors, i.e. the number of ones contained in
     * bv1 but not in bv2.
     *
     * @param bv1 first vector
     * @param bv2 second vector
     * @return cardinality of the relative complement
     * @throws NullPointerException if any argument is <code>null</code>
     * @since 2.10
     */
    public static long cardinalityOfRelativeComplement(final BitVectorValue bv1, final BitVectorValue bv2) {
        CheckUtils.checkNotNull(bv1, "Given BitVectorValues may not be null!");
        CheckUtils.checkNotNull(bv2, "Given BitVectorValues may not be null!");

        if (bv1 instanceof DenseBitVectorCell && bv2 instanceof DenseBitVectorCell) {
            return ((DenseBitVectorCell)bv1).cardinalityOfRelativeComplement((DenseBitVectorCell)bv2);
        } else if (bv1 instanceof SparseBitVectorCell && bv2 instanceof SparseBitVectorCell) {
            return ((SparseBitVectorCell)bv1).cardinalityOfRelativeComplement((SparseBitVectorCell)bv2);
        }

        long bv1Idx = bv1.nextSetBit(0);

        long toReturn = 0;
        while (bv1Idx >= 0) {
            // we looking for bv1 \ bv2, so bv2 should not be set on a valid bv1Idx
            if (bv1Idx >= bv2.length() || !bv2.get(bv1Idx)) {
                toReturn++;
            }
            bv1Idx = bv1.nextSetBit(bv1Idx + 1);
        }
        return toReturn;
    }

    private static int sparseBitVectorCellCount(final BitVectorValue bv1, final BitVectorValue bv2) {
        int count = 0;
        if (bv1 instanceof SparseBitVectorCell) {
            count++;
        }
        if (bv2 instanceof SparseBitVectorCell) {
            count++;
        }
        return count;
    }
}
