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
 *   20.09.2007 (Fabian Dill): created
 */
package org.knime.core.node.workflow;

import java.util.EventObject;

/**
 *
 * @author Fabian Dill, University of Konstanz
 */
public class NodeStateEvent extends EventObject {

    private final InternalNodeContainerState m_internalNCState;

    /**
     * A new event from node container with the given id.
     *
     * The internally kept (and deprecated) {@link InternalNodeContainerState} state will be set to <code>null</code>!
     *
     * @param nodeID the node container the state has changed for (not null)
     * @since 3.5
     */
    public NodeStateEvent(final NodeID nodeID) {
        this(nodeID, null);
    }

    /** A new event from the current node container ID and state.
     * @param nc A node container to derive the state from (not null).
     */
    public NodeStateEvent(final NodeContainer nc) {
        this(nc.getID(), nc.getInternalState());
    }

    /**
     * @param src id of the node
     * @param newState the new state.
     */
    NodeStateEvent(final NodeID src, final InternalNodeContainerState newState) {
        super(src);
        m_internalNCState = newState;
    }

    /**
     *
     * @return the new state of the node
     * @deprecated Don't get the state from the event but receive it from the node itself
     */
    @Deprecated
    public NodeContainer.State getState() {
        return m_internalNCState.mapToOldStyleState();
    }

    /** @return the internalNCState */
    InternalNodeContainerState getInternalNCState() {
        return m_internalNCState;
    }

    /** {@inheritDoc} */
    @Override
    public NodeID getSource() {
        return (NodeID)super.getSource();
    }
}
