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
 * ------------------------------------------------------------------------
 */
package org.knime.base.node.mine.bfn.fuzzy.shrink;

import org.knime.base.node.mine.bfn.fuzzy.membership.MembershipFunction;

/**
 * ShrinkVolumeAnchorBased. calculates the volume loss from the left or right
 * support or core region border to the new value, divided by the anchor to the
 * border spread <code>d1_i*Prod(i!=k)d2_k/Prod(k)d2_k = d1_i/d2_i</code>.
 * 
 * @author Thomas Gabriel, University of Konstanz
 */
public final class VolumeAnchorBasedShrink implements Shrink {
    
    private static final Shrink SHRINK = new VolumeAnchorBasedShrink();

    /**
     * Empty default constructor.
     */
    private VolumeAnchorBasedShrink() {
        // empty
    }

    /**
     * @return a new instance of the ShrinkVolumeRuleBased object
     */
    public static Shrink getInstance() {
        return SHRINK;
    }

    /**
     * @param value current value
     * @param mem holds parameter of fuzzy trapezoid membership function
     * @return loss normalized loss in volume in the left support region
     */
    public double leftSuppLoss(final double value, 
            final MembershipFunction mem) {

        // value on the border
        if (mem.getMinSupport() >= value) {
            return Double.MIN_VALUE;
        }
        // normalized volume loss
        return (value - mem.getMinSupport())
                / (mem.getAnchor() - mem.getMinSupport());
    }

    /**
     * leftCoreLoss(.).
     * 
     * @param value current value
     * @param mem holds parameter of fuzzy trapezoid membership function
     * @return loss normalized loss in volume in the left core region
     */
    public double leftCoreLoss(final double value, 
            final MembershipFunction mem) {

        // value on the border
        if (mem.getMinCore() >= value) {
            return Double.MIN_VALUE;
        }
        // normalized volume loss
        return (value - mem.getMinCore())
                / (mem.getAnchor() - mem.getMinCore());
    }

    /**
     * rightSuppLoss(.).
     * 
     * @param value current value
     * @param mem holds parameter of fuzzy trapezoid membership function
     * @return loss normalized loss in volume in the right support region
     */
    public double rightSuppLoss(final double value, 
            final MembershipFunction mem) {

        // value on the border
        if (mem.getMaxSupport() <= value) {
            return Double.MIN_VALUE;
        }
        // normalized volume loss
        return (mem.getMaxSupport() - value)
                / (mem.getMaxSupport() - mem.getAnchor());
    }

    /**
     * rightCoreLoss(.).
     * 
     * @param value current value
     * @param mem holds parameter of fuzzy trapezoid membership function
     * @return loss normalized loss in volume in the right core region
     */
    public double rightCoreLoss(final double value, 
            final MembershipFunction mem) {

        // value on the border
        if (mem.getMaxCore() <= value) {
            return Double.MIN_VALUE;
        }
        // normalized volume loss
        return (mem.getMaxCore() - value)
                / (mem.getMaxCore() - mem.getAnchor());
    }

    /**
     * Returns the string representation <b>VolumeAnchorBased</b> for this
     * shrink function.
     * 
     * @return a name for this shrink
     */
    @Override
    public final String toString() {
        return "VolumeAnchorBased";
    }
}
