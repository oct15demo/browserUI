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
 *   Jul 2, 2014 (wiswedel): created
 */
package org.knime.core.node.dialog;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;

/**
 * Interface to be implemented by {@link org.knime.core.node.NodeDialogPane} when additional runtime parameter
 * need to be shown. For a detailed description see {@link ValueControlledNode}.
 *
 * @author Bernd Wiswedel, KNIME.com, Zurich, Switzerland
 * @since 2.10
 */
public interface ValueControlledDialogPane {

    /** Counter part to {@link ValueControlledNode#saveCurrentValue(org.knime.core.node.NodeSettingsWO)}. It receives
     * the currently used value from the node and shows it as part of a label or updated component in the configuration
     * dialog. Such value controlled parameters are not modified by the node configuration dialog itself (but only
     * from the outer sub node, for instance).
     * @param value To load from, not null.
     * @throws InvalidSettingsException If that fails. Errors are logged but not further handled.
     */
    public void loadCurrentValue(NodeSettingsRO value) throws InvalidSettingsException;

}
