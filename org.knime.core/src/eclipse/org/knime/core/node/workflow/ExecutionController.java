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
 *   Jun 11, 2015 (wiswedel): created
 */
package org.knime.core.node.workflow;


/** Base class that controls execution beyond dedicated nodes.
 * Derived classes can be used to stop execution at a particular type of node, e.g. SubNodes
 * that are to be displayed in the WebPortal. This is essentially a call-back from the workflow
 * manager which is used to internally flag nodes after execution and sub sequentially ask if
 * newly queued nodes have a halted predecessor. Derived classes can then add more functionality
 * to define halting-conditions, extract the halted nodes, restart execution etc.
 * This base class implementation simply assumes no nodes are special.
 *
 * @author Bernd Wiswedel, Michael Berthold, KNIME.com, Zurich, Switzerland
 */
interface ExecutionController {

    /** Singleton instance that is used when not run in a wizard. */
    static final ExecutionController NO_OP = new ExecutionController() {};

    /**
     * Check if execution was halted at this node previously. If so, no successors
     * should be queued.
     *
     * @param source node to be checked.
     * @return derived classes can return true if the execution was stopped at this node.
     */
    default boolean isHalted(final NodeID source) {
        return false;
    }

    /**
     * Call when a certain nodes was executed to determine if it should be added to the
     * list of halted nodes.
     *
     * @param source node to be checked.
     */
    default void checkHaltingCriteria(final NodeID source) {
    }

}
