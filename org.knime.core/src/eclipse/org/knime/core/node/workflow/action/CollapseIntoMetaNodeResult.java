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
 *   Jun 21, 2015 (wiswedel): created
 */
package org.knime.core.node.workflow.action;

import org.knime.core.node.util.CheckUtils;
import org.knime.core.node.workflow.NodeID;
import org.knime.core.node.workflow.WorkflowLock;
import org.knime.core.node.workflow.WorkflowManager;
import org.knime.core.node.workflow.WorkflowPersistor;

/**
 * Represents the result of an collapse into metanode. It represent the result itself (the outcoming workflow manager)
 * and an undo persitor.
 *
 * @noreference This class is not intended to be referenced by clients.
 * @author Bernd Wiswedel, KNIME.com, Zurich, Switzerland
 * @since 3.2
 */
public final class CollapseIntoMetaNodeResult {

    private final WorkflowManager m_hostWFM;

    private final NodeID m_collapsedMetanodeID;

    private final WorkflowPersistor m_undoCopyPersistor;

    /** Used by workflow manager to fill content.
     * @param hostWFM
     * @param collapsedMetanodeID
     * @param undoCopyPersistor
     */
    public CollapseIntoMetaNodeResult(final WorkflowManager hostWFM, final NodeID collapsedMetanodeID,
        final WorkflowPersistor undoCopyPersistor) {
        m_hostWFM = CheckUtils.checkArgumentNotNull(hostWFM);
        m_collapsedMetanodeID = CheckUtils.checkArgumentNotNull(collapsedMetanodeID);
        m_undoCopyPersistor = CheckUtils.checkArgumentNotNull(undoCopyPersistor);
    }


    public boolean canUndo() {
        return m_hostWFM.canRemoveNode(m_collapsedMetanodeID);
    }

    public void undo() {
        WorkflowManager hostWFM = m_hostWFM;
        try (WorkflowLock l = m_hostWFM.lock()) {
            m_hostWFM.removeNode(m_collapsedMetanodeID);
            hostWFM.paste(m_undoCopyPersistor);
        }
    }

    /** @return the collapsedMetanodeID */
    public NodeID getCollapsedMetanodeID() {
        return m_collapsedMetanodeID;
    }
}
