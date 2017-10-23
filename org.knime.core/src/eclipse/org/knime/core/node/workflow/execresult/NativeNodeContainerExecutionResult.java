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
 */
package org.knime.core.node.workflow.execresult;

import org.knime.core.node.util.CheckUtils;
import org.knime.core.node.workflow.SingleNodeContainer;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Specialized execution result for {@link SingleNodeContainer}. Offers access
 * to the node's execution result (containing port objects and possibly
 * internals).
 * @author Bernd Wiswedel, University of Konstanz
 * @since 3.1
 */
public class NativeNodeContainerExecutionResult extends NodeContainerExecutionResult {

    private NodeExecutionResult m_nodeExecutionResult;

    /**
     * Create an empty execution result.
     */
    public NativeNodeContainerExecutionResult() {
    }

    /**
     * Copy constructor.
     *
     * @param toCopy Execution result that should be copied.
     * @since 3.5
     */
    public NativeNodeContainerExecutionResult(final NativeNodeContainerExecutionResult toCopy) {
        super(toCopy);
        m_nodeExecutionResult = new NodeExecutionResult(toCopy.m_nodeExecutionResult);
    }

    /** @return The execution result for the node. */
    @JsonProperty
    public NodeExecutionResult getNodeExecutionResult() {
        return m_nodeExecutionResult;
    }

    /**
     * @param nodeExecutionResult the result to set
     * @throws NullPointerException If argument is null.
     */
    @JsonProperty
    public void setNodeExecutionResult(final NodeExecutionResult nodeExecutionResult) {
        m_nodeExecutionResult = CheckUtils.checkArgumentNotNull(nodeExecutionResult);
    }

    /** {@inheritDoc} */
    @Override
    public NodeContainerExecutionStatus getChildStatus(final int idSuffix) {
        throw new IllegalStateException(getClass().getSimpleName() + " has no children");
    }

}
