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
 * Created on Apr 16, 2013 by Berthold
 */
package org.knime.core.node.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.interactive.ViewContent;
import org.knime.core.node.wizard.WizardNode;

/** Container for all of the information transported in between a {@link WizardNode}
 * and an interactive view running in the wizard or WebPortal.
 *
 * @author B. Wiswedel, Th. Gabriel, M. Berthold, C. Albrecht
 * @since 2.9
 */
public interface WebViewContent extends ViewContent {

    /**
     * @param viewContentStream an input stream, that is used to create the instance of a view content.
     * @throws IOException Exception that can occur while creating an instance.
     */
    public abstract void loadFromStream(InputStream viewContentStream) throws IOException;

    /**
     * @return An output stream with the serialized view content.
     * @throws IOException Exception that can occur while serializing object.
     */
    public abstract OutputStream saveToStream() throws IOException;

    /**
     * @param settings
     * @since 2.9
     */
    public abstract void saveToNodeSettings(NodeSettingsWO settings);

    /**
     * @param settings
     * @since 2.9
     */
    public abstract void loadFromNodeSettings(NodeSettingsRO settings) throws InvalidSettingsException;
}
