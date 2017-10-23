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
 * -------------------------------------------------------------------
 * 
 * History
 *   29.06.2006 (gabriel): created
 */
package org.knime.base.node.mine.bfn.fuzzy;

import org.knime.base.node.mine.bfn.BasisFunctionPredictorRow;
import org.knime.base.node.mine.bfn.Distance;
import org.knime.base.node.mine.bfn.fuzzy.membership.MembershipFunction;
import org.knime.base.node.mine.bfn.fuzzy.norm.Norm;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.RowKey;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.util.MutableDouble;

/**
 * 
 * @author Thomas Gabriel, University of Konstanz
 */
public class FuzzyBasisFunctionPredictorRow extends BasisFunctionPredictorRow {
    
    private final MembershipFunction[] m_mem;

    private final int m_norm;

    /**
     * Everything below this threshold causes an activation, also don't know
     * class probability.
     */
    private static final double MINACT = 0.0;

    /**
     * Creates a new predictor as fuzzy rule.
     * @param key The id for this rule.
     * @param classLabel The class label of this rule.
     * @param mem An array of membership functions each per dimension. 
     * @param norm A fuzzy norm to combine activations via all dimensions.
     */
    protected FuzzyBasisFunctionPredictorRow(final RowKey key,
            final DataCell classLabel, final MembershipFunction[] mem,
            final int norm) {
        super(key, classLabel, MINACT);
        m_norm = norm;
        m_mem = mem;
    }

    /**
     * Creates a new predictor as fuzzy rule.
     * @param pp Content to read rule from.
     * @throws InvalidSettingsException If the content is invalid.
     */
    public FuzzyBasisFunctionPredictorRow(final ModelContentRO pp)
            throws InvalidSettingsException {
        super(pp);
        m_norm = pp.getInt(Norm.NORM_KEY);
        ModelContentRO memParams = pp.getModelContent("membership_functions");
        m_mem = new MembershipFunction[memParams.keySet().size()];
        int i = 0;
        for (String key : memParams.keySet()) {
            m_mem[i] = new MembershipFunction(memParams.getModelContent(key));
            i++;
        }
    }
    
    /**
     * Computes the overlapping of two fuzzy basisfunction based on their core
     * spreads.
     * 
     * @param bf the other fuzzy basis function
     * @param symmetric if the result is proportional to both basis functions,
     *            and thus symmetric, or if it is proportional to the area of
     *            the basis function on which the function is called
     * @return a degree of overlap normalized with the overall volume of both
     *         basis functions
     */
    @Override
    public double overlap(final BasisFunctionPredictorRow bf,
            final boolean symmetric) {
        FuzzyBasisFunctionPredictorRow fbf = (FuzzyBasisFunctionPredictorRow)bf;
        assert (fbf.getNrMemships() == getNrMemships());
        double overlap = 1.0;
        for (int i = 0; i < getNrMemships(); i++) {
            MembershipFunction memA = getMemship(i);
            MembershipFunction memB = fbf.getMemship(i);
            if (memA.isMissingIntern() || memB.isMissingIntern()) {
                continue;
            }
            double overlapping = overlapping(memA.getMinCore(), memA
                    .getMaxCore(), memB.getMinCore(), memB.getMaxCore(),
                    symmetric);
            if (overlapping == 0.0) {
                return 0.0;
            } else {
                overlap *= overlapping;
            }
        }
        return overlap;
    }

    /**
     * Return number of memberships which is equivalent to the number of
     * numeric input dimensions.
     * @return Number of membership functions. 
     */
    public int getNrMemships() {
        return m_mem.length;
    }

    /**
     * Returns the membership for one dimension.
     * @param i Dimension index.
     * @return A fuzzy membership function.
     */
    public MembershipFunction getMemship(final int i) {
        return m_mem[i];
    }
    
    /**
     * @return array of fuzzy membership function
     */
    public MembershipFunction[] getMemships() {
        return m_mem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(final ModelContentWO pp) {
        super.save(pp);
        pp.addInt(Norm.NORM_KEY, m_norm);
        ModelContentWO memParams = pp.addModelContent("membership_functions");
        for (int i = 0; i < m_mem.length; i++) {
            m_mem[i].save(memParams.addModelContent("" + i));
        }
    }

    /**
     * Composes the degree of membership by using the disjunction of the
     * tco-norm operator.
     * 
     * @param row row
     * @param act activation
     * @return the new activation array
     * 
     * @see #computeActivation(DataRow)
     * @see Norm#computeTCoNorm(double,double)
     */
    @Override
    public double compose(final DataRow row, final double act) {
        assert (act >= 0.0 && act <= 1.0) : "act=" + act;
        // compute current activation (maximum) of input row
        return Norm.NORMS[m_norm].computeTCoNorm(
                computeActivation(row), act);
    }

    /**
     * Returns the compute activation of this input vector.
     * 
     * @param row input pattern
     * @return membership degree
     */
    @Override
    public double computeActivation(final DataRow row) {
        assert (m_mem.length == row.getNumCells());
        // sets degree to maximum
        double degree = 1.0;
        // overall cells in the vector
        for (int i = 0; i < m_mem.length; i++) {
            DataCell cell = row.getCell(i);
            if (cell.isMissing()) {
                continue;
            }
            // gets cell at index i
            double value = ((DoubleValue) cell).getDoubleValue();
            // act in current dimension
            double act = m_mem[i].getActivation(value);
            if (i == 0) {
                degree = act;
                continue;
            }
            // calculates the new (minimum) degree using norm index
            degree = Norm.NORMS[m_norm].computeTNorm(degree, act);
            assert (0.0 <= degree && degree <= 1.0);
        }
        // returns membership degree
        return degree;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public double computeDistance(final DataRow row) {
        assert row.getNumCells() == m_mem.length;
        double[] d1 = new double[m_mem.length];
        double[] d2 = new double[m_mem.length];
        for (int i = 0; i < m_mem.length; i++) {
            DataCell cell = row.getCell(i);
            if (cell.isMissing()) {
                d1[i] = Double.NaN;
            } else {
                d1[i] = ((DoubleValue) cell).getDoubleValue();
            }
            if (m_mem[i].isMissingIntern()) {
                d2[i] = Double.NaN;
            } else {
                d2[i] = m_mem[i].getAnchor();
            }
        }
        return Distance.getInstance().compute(d1, d2);
    }
    
    /**
     * Returns the aggregated spread of the core.
     * 
     * @return the overall spread of the core regions
     */
    @Override
    public double computeSpread() {
        double vol = 0.0;
        double dom = 0.0;
        for (int i = 0; i < getNrMemships(); i++) {
            MembershipFunction mem = getMemship(i);
            if (mem.isMissingIntern()) {
                continue;
            }
            double spread = (mem.getMaxCore() - mem.getMinCore());
            if (spread > 0.0) {
                if (vol == 0.0) {
                    vol = spread;
                    dom = (mem.getMax().doubleValue() 
                            - mem.getMin().doubleValue());
                } else {
                    vol *= spread;
                    dom *= (mem.getMax().doubleValue() 
                            - mem.getMin().doubleValue());
                }
            }
        }
        return (vol > 0 ? vol / dom : 0);
    }
    
    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public int getNrUsedFeatures() {
        int used = 0;
        for (MembershipFunction mem : m_mem) {
            if (mem.isMissingIntern() || !mem.isSuppLeftMax() 
                    || !mem.isSuppRightMax()) {
                used++;
            }
        }
        return used; 
    }
    
    /**
     * @return fuzzy norm
     */
    public final int getNorm() {
        return m_norm;
    }
    
    /**
     * @return array of minimum bounds
     */
    public final MutableDouble[] getMins() {
        MutableDouble[] mins = new MutableDouble[m_mem.length];
        for (int i = 0; i < mins.length; i++) {
            mins[i] = m_mem[i].getMin();
        }
        return mins;
    }
    
    /**
     * @return array of maximum bounds
     */
    public final MutableDouble[] getMaxs() {
        MutableDouble[] maxs = new MutableDouble[m_mem.length];
        for (int i = 0; i < maxs.length; i++) {
            maxs[i] = m_mem[i].getMax();
        }
        return maxs;
    }

}
