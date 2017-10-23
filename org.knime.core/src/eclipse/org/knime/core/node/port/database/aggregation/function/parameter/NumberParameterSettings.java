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
 */

package org.knime.core.node.port.database.aggregation.function.parameter;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;


/**
 * Class that saves the settings of the {@link SelectFunctionSettingsPanel}.
 *
 * @author Ole Ostergaard
 * @since 3.4
 */
public class NumberParameterSettings {

    private static final String NUMBER_PARAM = "numberParameter";

    private final SettingsModelDoubleBounded m_number;

    /**
     * @param min the parameter's minimum value
     * @param max the parameter's maximum value
     * @param def the default parameter
     */
    public NumberParameterSettings(final double min, final double max, final double def) {
        m_number = new SettingsModelDoubleBounded(NUMBER_PARAM, def, min, max);
    }

    /**
     * @return the boolean model
     */
    SettingsModelDoubleBounded getModel() {
        return m_number;
    }

    /**
     * @param settings the {@link NodeSettingsRO} to read the settings from
     * @throws InvalidSettingsException if the settings are invalid
     */
    public void validateSettings(final NodeSettingsRO settings)
    throws InvalidSettingsException {
        m_number.validateSettings(settings);
    }

    /**
     * @param settings the {@link NodeSettingsRO} to read the settings from
     * @throws InvalidSettingsException if the settings are invalid
     */
    public void loadSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_number.loadSettingsFrom(settings);
    }
    /**
     * @param settings the {@link NodeSettingsWO} to write to
     */
    public void saveSettingsTo(final NodeSettingsWO settings) {
        m_number.saveSettingsTo(settings);
    }

    /**
     * @return the models value
     */
    public Double getParameter() {
        return m_number.getDoubleValue();
    }

    /**
     * @return the model's minimum value
     */
    public Double getMin() {
        return m_number.getLowerBound();
    }

    /**
     * @return the models maximum value
     */
    public Double getMax() {
        return m_number.getUpperBound();
    }

    /**
     * @return a clone of this settings object
     */
    public NumberParameterSettings createClone() {
        return new NumberParameterSettings(getMin(), getMax(), getParameter());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_number == null) ? 0 : m_number.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NumberParameterSettings other = (NumberParameterSettings)obj;
        if (m_number == null) {
            if (other.m_number != null) {
                return false;
            }
        } else if (!m_number.equals(other.m_number)) {
            return false;
        }
        return true;
    }
}
