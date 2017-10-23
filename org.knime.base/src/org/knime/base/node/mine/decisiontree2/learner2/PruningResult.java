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
 *   12.03.2007 (sieb): created
 */
package org.knime.base.node.mine.decisiontree2.learner2;

import org.knime.base.node.mine.decisiontree2.model.DecisionTreeNode;

/**
 * A pruning result is the possibly new node and a quality value (e.g.
 * description length, estimated error) of this node.
 *
 * @author Christoph Sieb, University of Konstanz
 * 
 * @since 2.6
 */
public class PruningResult {

    private double m_qualityValue;

    private DecisionTreeNode m_node;

    /**
     * Creates a pruning result from a node and its quality value (e.g.
     * description length, estimated error).
     *
     * @param qualityValue the quality value (e.g. description length, estimated
     *            error) of the node
     *
     * @param node the node of the pruning result
     */
    public PruningResult(final double qualityValue,
            final DecisionTreeNode node) {
        m_qualityValue = qualityValue;
        m_node = node;
    }

    /**
     * Returns the quality value for this node.
     *
     * @return the quality value length for this node
     */
    public double getQualityValue() {
        return m_qualityValue;
    }

    /**
     * Returns the decision tree of this pruning result.
     *
     * @return the decision tree of this pruning result
     */
    public DecisionTreeNode getNode() {
        return m_node;
    }
}
