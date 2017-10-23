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
 * Created on 21.08.2013 by Christian Albrecht, KNIME.com AG, Zurich, Switzerland
 */
package org.knime.core.node.dialog;

import javax.json.JsonException;
import javax.json.JsonValue;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 *
 * @author Christian Albrecht, KNIME.com AG, Zurich, Switzerland
 * @since 2.9
 */
public interface DialogNodeValue {

    /**
     * @param settings
     */
    public abstract void saveToNodeSettings(final NodeSettingsWO settings);

    /**
     * @param settings
     * @throws InvalidSettingsException
     */
    public abstract void loadFromNodeSettings(final NodeSettingsRO settings) throws InvalidSettingsException;

    /**
     * @param settings
     */
    public abstract void loadFromNodeSettingsInDialog(final NodeSettingsRO settings);

    /** Parses the value from command line - default implement will just fail as 'complex' nodes such as the column
     * filter cannot be parameterized via commandline. Simple nodes (integer &amp; string input, ...) will just parse
     * the value.
     * @param fromCmdLine Argument as per commandline.
     * @throws UnsupportedOperationException ... as per above.
     * @since 2.12
     */
    public abstract void loadFromString(final String fromCmdLine) throws UnsupportedOperationException;

    /**
     * Called when parameterized via web service invocation. Each implementation should support reading its value from
     * a JSON value.
     * @param json a JSON value, never <code>null</code>
     * @throws JsonException If parsing fails.
     * @since 2.12
     */
    public abstract void loadFromJson(final JsonValue json) throws JsonException;

    /** Reverse operation to {@link #loadFromJson(JsonValue)}. This can be used to dump the current configuration to a
     * file that can then be modified by the user. It helps the user to understand the structure of the expected
     * {@link JsonValue}.
     * @return Content as {@link JsonValue}, never <code>null</code>
     * @since 2.12
     */
    public JsonValue toJson();

}
