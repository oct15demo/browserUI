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
 *   12.09.2007 (Fabian Dill): created
 */
package org.knime.core.node.defaultnodesettings;

import org.knime.core.node.InvalidSettingsException;

/**
 * 
 * @author Fabian Dill, University of Konstanz
 */
public class SettingsModelLongBounded extends SettingsModelLong {
    
    private final long m_minValue;
    
    private final long m_maxValue;

    /**
     * @param configName the key for the settings
     * @param defaultValue default value
     * @param minValue lower bound
     * @param maxValue upper bound
     * 
     */
    public SettingsModelLongBounded(final String configName, 
            final long defaultValue, final long minValue, 
            final long maxValue) {
        super(configName, defaultValue);
        m_minValue = minValue;
        m_maxValue = maxValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SettingsModelLongBounded createClone() {
        return new SettingsModelLongBounded(getConfigName(), 
                getLongValue(), m_minValue, m_maxValue);
    }

    
    /**
     * 
     * @return lower bound
     */
    public long getLowerBound() {
        return m_minValue;
    }
    
    /**
     * 
     * @return upper bound
     */
    public long getUpperBound() {
        return m_maxValue;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateValue(final long value)
            throws InvalidSettingsException {
        super.validateValue(value);
        try {
            checkBounds(value);
        } catch (IllegalArgumentException iae) {
            throw new InvalidSettingsException(iae.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLongValue(final long newValue) {
        checkBounds(newValue);
        super.setLongValue(newValue);
    }

    private void checkBounds(final long val) {
        if ((val < m_minValue) || (m_maxValue < val)) {
            throw new IllegalArgumentException("value (=" + val
                    + ") must be within the range [" + m_minValue + "..."
                    + m_maxValue + "].");
        }
    }
    
}
