/*
 * ------------------------------------------------------------------------
 *
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
 *   Jun 5, 2009 (wiswedel): created
 */
package org.knime.core.node.exec;

import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.workflow.NodeContainer;
import org.knime.core.node.workflow.NodeExecutionJob;
import org.knime.core.node.workflow.WorkflowPersistor.LoadResult;
import org.knime.core.node.workflow.WorkflowPersistor.LoadResultEntry.LoadResultEntryType;
import org.knime.core.node.workflow.execresult.NodeContainerExecutionResult;
import org.knime.core.node.workflow.execresult.NodeContainerExecutionStatus;

/**
 * Execution job that applies a given execution result to a node container.
 *
 * @author Bernd Wiswedel, KNIME.com AG, Zurich, Switzerland
 */
final class CopyContentIntoTempFlowNodeExecutionJob extends NodeExecutionJob {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(CopyContentIntoTempFlowNodeExecutionJobManager.class);

    private final NodeContainerExecutionResult m_ncResult;

    /**
     * Delegates to super constructor.
     *
     * @param nc Forwarded
     * @param data Forwarded.
     * @param ncResult Result to be applied during execution.
     */
    CopyContentIntoTempFlowNodeExecutionJob(final NodeContainer nc, final PortObject[] data,
        final NodeContainerExecutionResult ncResult) {
        super(nc, data);
        m_ncResult = ncResult;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean cancel() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isReConnecting() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    protected NodeContainerExecutionStatus mainExecute() {
        LoadResult lR = new LoadResult("load data into temp flow");
        getNodeContainer().loadExecutionResult(m_ncResult, new ExecutionMonitor(), lR);
        if (lR.hasErrors()) {
            LOGGER.error("Errors loading temporary data into workflow (to be submitted to cluster):\n"
                + lR.getFilteredError("", LoadResultEntryType.Warning));
        }
        return m_ncResult;
    }

}
