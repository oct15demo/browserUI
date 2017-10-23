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
 *   08.01.2009 (ohl): created
 */
package org.knime.core.node;

import org.knime.core.node.port.PortObjectSpec;

/**
 * Object passed to {@link Node#configure(PortObjectSpec[], NodeConfigureHelper)}
 * in order to modify the output specs in case the node is wrapped and its
 * output is modified.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @author ohl, University of Konstanz
 */
public interface NodeConfigureHelper {

    /**
     * Called immediately before NodeModel's configure is called. (Used to apply variables to the settings.)
     * @throws InvalidSettingsException ...
     */
    public void preConfigure() throws InvalidSettingsException;

    /**
     * Modifies the output table specs calculated by the
     * {@link NodeModel#configure(PortObjectSpec[])} method.
     *
     * @param inSpecs port object specs from predecessor node(s)
     * @param nodeModelOutSpecs the output specs created by the underlying node
     * @return the output specs actually delivered at the node's output ports
     * @throws InvalidSettingsException if the node can't be executed.
     */
    public PortObjectSpec[] postConfigure(final PortObjectSpec[] inSpecs,
            PortObjectSpec[] nodeModelOutSpecs) throws InvalidSettingsException;

}
