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
 * ------------------------------------------------------------------------
 */
package org.knime.core.node.defaultnodesettings;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.workflow.FlowVariable;

/**
 * A settingsmodel for double default components.
 *
 * @author ohl, University of Konstanz
 */
public class SettingsModelDouble extends SettingsModelNumber
implements SettingsModelFlowVariableCompatible {

    private double m_value;

    private final String m_configName;

    /**
     * Creates a new object holding a double value.
     *
     * @param configName the identifier the value is stored with in the
     *            {@link org.knime.core.node.NodeSettings} object
     * @param defaultValue the initial value
     */
    public SettingsModelDouble(final String configName,
            final double defaultValue) {
        if ((configName == null) || "".equals(configName)) {
            throw new IllegalArgumentException("The configName must be a "
                    + "non-empty string");
        }
        m_value = defaultValue;
        m_configName = configName;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected SettingsModelDouble createClone() {
        return new SettingsModelDouble(m_configName, m_value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getModelTypeID() {
        return "SMID_double";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getConfigName() {
        return m_configName;
    }

    /**
     * set the value stored to the new value.
     *
     * @param newValue the new value to store.
     */
    public void setDoubleValue(final double newValue) {
        boolean notify = (newValue != m_value);

        m_value = newValue;

        if (notify) {
            notifyChangeListeners();
        }
    }

    /**
     * @return the current value stored.
     */
    public double getDoubleValue() {
        return m_value;
    }

    /**
     * Allows to set a new value by passing a string that will be parsed and, if
     * valid, set as new value.
     *
     * @param newValueStr the new value to be set, as string representation
     */
    @Override
    void setNumberValueStr(final String newValueStr) {
        try {
            double tmp = Double.valueOf(newValueStr);
            setDoubleValue(tmp);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid floating point number"
                    + " format.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    String getNumberValueStr() {
        return Double.toString(getDoubleValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsForDialog(final NodeSettingsRO settings,
            final PortObjectSpec[] specs) throws NotConfigurableException {
        try {
            // use the current value, if no value is stored in the settings
            setDoubleValue(settings.getDouble(m_configName, m_value));
        } catch (IllegalArgumentException e) {
            // if the value is not accepted, keep the old value.
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsForDialog(final NodeSettingsWO settings)
            throws InvalidSettingsException {
        saveSettingsForModel(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettingsForModel(final NodeSettingsRO settings)
            throws InvalidSettingsException {

        validateValue(settings.getDouble(m_configName));
    }

    /**
     * Called during {@link #validateSettingsForModel}, can be overwritten by
     * derived classes.
     *
     * @param value the value to validate
     * @throws InvalidSettingsException if the value is not valid and should be
     *             rejected
     */
    protected void validateValue(final double value)
            throws InvalidSettingsException {
        // derived class needs to check value
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsForModel(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        try {
            // no default value, throw an exception instead
            setDoubleValue(settings.getDouble(m_configName));

        } catch (IllegalArgumentException iae) {
            // value not accepted
            throw new InvalidSettingsException(iae.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsForModel(final NodeSettingsWO settings) {
        settings.addDouble(m_configName, m_value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + " ('" + m_configName + "')";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return m_configName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowVariable.Type getFlowVariableType() {
        return FlowVariable.Type.DOUBLE;
    }
}
