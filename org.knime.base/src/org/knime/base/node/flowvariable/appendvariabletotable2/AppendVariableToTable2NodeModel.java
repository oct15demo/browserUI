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
 *   May 1, 2008 (wiswedel): created
 */
package org.knime.base.node.flowvariable.appendvariabletotable2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;
import org.knime.core.node.util.filter.variable.FlowVariableFilterConfiguration;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.util.Pair;

/** NodeModel for the "Variable To TableColumn" node  which adds variables as new columns to the input table.
 *
 * @author Bernd Wiswedel, KNIME.com, Zurich, Switzerland
 * @author Patrick Winter, KNIME.com, Zurich, Switzerland
 *
 * @since 2.9
 */
public class AppendVariableToTable2NodeModel extends SimpleStreamableFunctionNodeModel {

    /**
     * Key for the filter configuration.
     */
    static final String CFG_KEY_FILTER = "variable-filter";

    private FlowVariableFilterConfiguration m_filter;

    /** One input, one output. */
    public AppendVariableToTable2NodeModel() {
        super(new PortType[]{FlowVariablePortObject.TYPE_OPTIONAL, BufferedDataTable.TYPE},
            new PortType[]{BufferedDataTable.TYPE}, 1, 0);
        m_filter = new FlowVariableFilterConfiguration(CFG_KEY_FILTER);
        m_filter.loadDefaults(getAvailableFlowVariables(), false);
    }

    /** {@inheritDoc} */
    @Override
    protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {
        BufferedDataTable t = (BufferedDataTable)inData[1];
        DataTableSpec ts = t.getSpec();
        ColumnRearranger ar = createColumnRearranger(ts);
        BufferedDataTable out = exec.createColumnRearrangeTable(t, ar, exec);
        return new BufferedDataTable[]{out};
    }

    /** {@inheritDoc} */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        ColumnRearranger ar = createColumnRearranger((DataTableSpec)inSpecs[1]);
        return new DataTableSpec[]{ar.createSpec()};
    }

    @Override
    protected ColumnRearranger createColumnRearranger(final DataTableSpec spec) throws InvalidSettingsException {
        ColumnRearranger arranger = new ColumnRearranger(spec);
        Set<String> nameHash = new HashSet<String>();
        for (DataColumnSpec c : spec) {
            nameHash.add(c.getName());
        }
        List<Pair<String, FlowVariable.Type>> vars = getVariablesOfInterest();
        if (vars.isEmpty()) {
            throw new InvalidSettingsException("No variables selected");
        }
        DataColumnSpec[] specs = new DataColumnSpec[vars.size()];
        final DataCell[] values = new DataCell[vars.size()];
        for (int i = 0; i < vars.size(); i++) {
            Pair<String, FlowVariable.Type> c = vars.get(i);
            String name = c.getFirst();
            final DataType type;
            switch (c.getSecond()) {
                case DOUBLE:
                    type = DoubleCell.TYPE;
                    try {
                        double dValue = peekFlowVariableDouble(name);
                        values[i] = new DoubleCell(dValue);
                    } catch (NoSuchElementException e) {
                        throw new InvalidSettingsException("No such flow variable (of type double): " + name);
                    }
                    break;
                case INTEGER:
                    type = IntCell.TYPE;
                    try {
                        int iValue = peekFlowVariableInt(name);
                        values[i] = new IntCell(iValue);
                    } catch (NoSuchElementException e) {
                        throw new InvalidSettingsException("No such flow variable (of type int): " + name);
                    }
                    break;
                case STRING:
                    type = StringCell.TYPE;
                    try {
                        String sValue = peekFlowVariableString(name);
                        sValue = sValue == null ? "" : sValue;
                        values[i] = new StringCell(sValue);
                    } catch (NoSuchElementException e) {
                        throw new InvalidSettingsException("No such flow variable (of type String): " + name);
                    }
                    break;
                default:
                    throw new InvalidSettingsException("Unsupported variable type: " + c.getSecond());
            }
            if (nameHash.contains(name) && !name.toLowerCase().endsWith("(variable)")) {
                name = name.concat(" (variable)");
            }
            String newName = name;
            int uniquifier = 1;
            while (!nameHash.add(newName)) {
                newName = name + " (#" + (uniquifier++) + ")";
            }
            specs[i] = new DataColumnSpecCreator(newName, type).createSpec();
        }
        arranger.append(new AbstractCellFactory(specs) {
            /** {@inheritDoc} */
            @Override
            public DataCell[] getCells(final DataRow row) {
                return values;
            }
        });
        return arranger;
    }

    private List<Pair<String, FlowVariable.Type>> getVariablesOfInterest() {
        List<Pair<String, FlowVariable.Type>> result = new ArrayList<Pair<String, FlowVariable.Type>>();
        if (m_filter != null) {
            String[] names = m_filter.applyTo(getAvailableFlowVariables()).getIncludes();
            Map<String, FlowVariable> vars = getAvailableFlowVariables();
            for (String name : names) {
                result.add(new Pair<String, FlowVariable.Type>(name, vars.get(name).getType()));
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    protected void reset() {
    }

    /** {@inheritDoc} */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        FlowVariableFilterConfiguration conf = new FlowVariableFilterConfiguration(CFG_KEY_FILTER);
        conf.loadConfigurationInModel(settings);
        m_filter = conf;
    }

    /** {@inheritDoc} */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_filter.saveConfiguration(settings);
    }

    /** {@inheritDoc} */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        FlowVariableFilterConfiguration conf = new FlowVariableFilterConfiguration(CFG_KEY_FILTER);
        conf.loadConfigurationInModel(settings);
    }

    /** {@inheritDoc} */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
        CanceledExecutionException {
    }

    /** {@inheritDoc} */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
        CanceledExecutionException {
    }

}
