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
 *   Apr 16, 2008 (mb): created
 *   Sep 22, 2008 (mb): added loop termination criterion.
 */
package org.knime.core.node.workflow;

/** Interface implemented by {@link org.knime.core.node.NodeModel} classes
 * to define a loop start node. The framework will take care of the details,
 * such as finding the appropriate end node in the workflow (can be accessed
 * after the first loop iteration using the <code>getLoopEndNode()</code>
 * method defined in the abstract <code>NodeModel</code> class) and preparing
 * the flow object stack.
 *
 * <p>In comparison to an ordinary nodes, loop start nodes don't get their
 * <code>reset()</code> method called between loop iterations (although the
 * node is executed) but the output tables are cleared; secondly, if a loop
 * start node defines new data that needs to be kept between loop iterations
 * it must implement the {@link org.knime.core.node.BufferedDataTableHolder}
 * or {@link org.knime.core.node.port.PortObjectHolder} interface and return
 * the important tables in the corresponding get method (it should return
 * null _after_ the last iteration if the tables should not be persisted
 * with saving the workflow).
 *
 * @author M. Berthold, University of Konstanz &amp; Bernd Wiswedel, KNIME.com
 */
public interface LoopStartNode extends ScopeStartNode {
    // marker interface only
}
