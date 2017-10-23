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
 *   Dec 13, 2016 (oole): created
 */
package org.knime.core.node.port.database.aggregation.function.parameter;

import java.util.List;

import javax.swing.JPanel;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.database.StatementManipulator;
import org.knime.core.node.port.database.aggregation.DBAggregationFunction;

/**
 * Abstract class that allows the user to enter a number parameter and select a function
 *
 * @author Ole Ostergaard, KNIME.com
 * @since 3.4
 */
public abstract class AbstractNumberSelectFunctionDBAggregationFunction implements DBAggregationFunction {


    private NumberParameterSettingsPanel m_numberParameterPanel;
    private NumberParameterSettings m_numberParameterSettings;
    private String m_numberParameterLabel;

    private SelectFunctionSettingsPanel m_selectFunctionPanel;
    private SelectFunctionSettings m_selectFunctionSettings;
    private String m_selectFunctionLabel;
    private List<String> m_parameters;


    /**
     * @param numberParameterLabel The label for the number parameter field
     * @param numberParameterMin The lower bound for the number parameter field
     * @param numberParameterMax The upper bound for the number parameter field
     * @param numberParameterDefault The default number parameter
     * @param selectFuntionLabel The parameter selection label
     * @param selectFunctionDefault The parameter selection default
     * @param parameters The parameters that can be selected
     */
    protected AbstractNumberSelectFunctionDBAggregationFunction(final String numberParameterLabel,final double numberParameterMin,
        final double numberParameterMax, final double numberParameterDefault,
        final String selectFuntionLabel, final String selectFunctionDefault, final List<String> parameters) {

        m_numberParameterLabel = numberParameterLabel;
        m_numberParameterSettings = new NumberParameterSettings(numberParameterMin,
            numberParameterMax, numberParameterDefault);

        m_selectFunctionLabel = selectFuntionLabel;
        m_selectFunctionSettings = new SelectFunctionSettings(selectFunctionDefault);
        m_parameters = parameters;
    }

    @Override
    public String getSQLFragment (final StatementManipulator manipulator, final String tableName,
        final String columnName) {
        return getLabel() + "(" + manipulator.quoteIdentifier(tableName) + "." + manipulator.quoteIdentifier(columnName)
              + ", "  + m_numberParameterSettings.getParameter() +
              ", '" + m_selectFunctionSettings.getParameter() + "')";
    }

    @Override
    public String getSQLFragment4SubQuery(final StatementManipulator manipulator, final String tableName, final String subQuery) {
        return getLabel() + "((" + subQuery + "), " + + m_numberParameterSettings.getParameter() +
              ", '" + m_selectFunctionSettings.getParameter() + "')";
    }

     /**
        * {@inheritDoc}
        */
       @Override
       public boolean hasOptionalSettings() {
           return true;
       }


       /**
      * {@inheritDoc}
      */
     @Override
     public JPanel getSettingsPanel() {
         JPanel panel = new JPanel();
         if (m_numberParameterPanel == null) {
             m_numberParameterPanel = new NumberParameterSettingsPanel(m_numberParameterSettings, m_numberParameterLabel);
         }
         if (m_selectFunctionPanel == null) {
             m_selectFunctionPanel = new SelectFunctionSettingsPanel(m_selectFunctionSettings, m_selectFunctionLabel, m_parameters);
         }
         panel.add(m_numberParameterPanel);
         panel.add(m_selectFunctionPanel);
         return panel;
     }


    /**
     * {@inheritDoc}
     */
    @Override
    public void loadValidatedSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_numberParameterSettings.loadSettingsFrom(settings);
        m_selectFunctionSettings.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadSettingsFrom(final NodeSettingsRO settings, final DataTableSpec spec)
            throws NotConfigurableException {
        getSettingsPanel();
        m_numberParameterPanel.loadSettingsFrom(settings, spec);
        m_selectFunctionPanel.loadSettingsFrom(settings, spec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveSettingsTo(final NodeSettingsWO settings) {
        m_numberParameterSettings.saveSettingsTo(settings);
        m_selectFunctionSettings.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_numberParameterSettings.validateSettings(settings);
        m_selectFunctionSettings.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate() throws InvalidSettingsException {
        //nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final DataTableSpec spec) throws InvalidSettingsException {
        //nothing to do
    }

    /**
     * Returns the number parameter from the numberParameterSettings
     * @return the number parameter from the numberParameterSettings
     */
    public Double getNumberParameter() {
        return m_numberParameterSettings.getParameter();
    }

    /**
     * Returns the selected function from the selectFunctionSettings
     * @return the selected function from the selectFunctionSettings
     */
    public String getSelectedFunction() {
        return m_selectFunctionSettings.getParameter();
    }
}
