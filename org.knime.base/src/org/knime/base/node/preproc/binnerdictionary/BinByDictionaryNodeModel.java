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
 *
 */
package org.knime.base.node.preproc.binnerdictionary;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValueComparator;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortObjectInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.StreamableOperator;

/** Model to node.
 * @author Bernd Wiswedel, KNIME.com, Zurich, Switzerland
 */
final class BinByDictionaryNodeModel extends NodeModel {

    private BinByDictionaryConfiguration m_configuration;

    /** 2 ins, 1 out. */
    public BinByDictionaryNodeModel() {
        super(2, 1);
    }

    /** {@inheritDoc} */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        ColumnRearranger rearranger;
        try {
            rearranger = createColumnRearranger(inSpecs, null, null);
        } catch (CanceledExecutionException e) {
            throw new RuntimeException("Illegal table iteration in configure");
        }
        DataTableSpec outSpec = rearranger.createSpec();
        return new DataTableSpec[] {outSpec};
    }

    /** {@inheritDoc} */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        DataTableSpec[] specs = new DataTableSpec[2];
        specs[0] = inData[0].getDataTableSpec();
        specs[1] = inData[1].getDataTableSpec();
        ColumnRearranger rearranger = createColumnRearranger(
                specs, inData[1], exec.createSubProgress(0.0));
        BufferedDataTable table = exec.createColumnRearrangeTable(
                inData[0], rearranger, exec);
        return new BufferedDataTable[] {table};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamableOperator createStreamableOperator(final PartitionInfo partitionInfo, final PortObjectSpec[] inSpecs)
        throws InvalidSettingsException {
        return new StreamableOperator() {

            @Override
            public void runFinal(final PortInput[] inputs, final PortOutput[] outputs, final ExecutionContext exec) throws Exception {
                DataTableSpec[] specs = new DataTableSpec[2];
                specs[0] = (DataTableSpec) inSpecs[0];
                specs[1] = (DataTableSpec) inSpecs[1];
                BufferedDataTable dict = (BufferedDataTable) ((PortObjectInput) inputs[1]).getPortObject();
                ColumnRearranger core = createColumnRearranger(specs, dict, exec);
                core.createStreamableFunction(0, 0).runFinal(inputs, outputs, exec);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputPortRole[] getInputPortRoles() {
        return new InputPortRole[]{InputPortRole.DISTRIBUTED_STREAMABLE, InputPortRole.NONDISTRIBUTED_NONSTREAMABLE};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputPortRole[] getOutputPortRoles() {
        return new OutputPortRole[]{OutputPortRole.DISTRIBUTED};
    }

    private ColumnRearranger createColumnRearranger(final DataTableSpec[] ins,
            final BufferedDataTable port1Table, final ExecutionMonitor exec)
        throws InvalidSettingsException, CanceledExecutionException {
        final BinByDictionaryConfiguration c = m_configuration;
        if (c == null) {
            throw new InvalidSettingsException("No configuration set");
        }

        String lowerColPort1 = c.getLowerBoundColumnPort1();
        String upperColPort1 = c.getUpperBoundColumnPort1();
        String labelCol = c.getLabelColumnPort1();
        String valueColumnPort0 = c.getValueColumnPort0();
        DataType valueType = null;

        final int valueColIndexPort0 = ins[0].findColumnIndex(valueColumnPort0);
        if (valueColIndexPort0 < 0) {
            throw new InvalidSettingsException(
                    "No such column in 1st input: " + valueColumnPort0);
        } else {
            valueType = ins[0].getColumnSpec(valueColIndexPort0).getType();
        }

        final boolean isLowerBoundInclusive = c.isLowerBoundInclusive();
        final boolean isUpperBoundInclusive = c.isUpperBoundInclusive();
        final int lowerBoundColIndex;
        final DataValueComparator lowerBoundComparator;
        if (lowerColPort1 == null) { // no lower bound specified
            lowerBoundComparator = null;
            lowerBoundColIndex = -1;
        } else {
            lowerBoundColIndex = ins[1].findColumnIndex(lowerColPort1);
            if (lowerBoundColIndex < 0) {
                throw new InvalidSettingsException(
                        "No such column in 2nd input: " + lowerColPort1);
            }
            DataType type = ins[1].getColumnSpec(lowerBoundColIndex).getType();
            if (valueType.equals(type)) {
                lowerBoundComparator = valueType.getComparator();
            } else {
                setWarningMessage("The types of the comparison and value "
                        + "columns are not equal, comparison might be done "
                        + "based on lexicographical string representation!");
                lowerBoundComparator = DataType.getCommonSuperType(
                        valueType, type).getComparator();
            }
        }
        final int upperBoundColIndex;
        final DataValueComparator upperBoundComparator;
        if (upperColPort1 == null) { // no upper bound specified
            upperBoundColIndex = -1;
            upperBoundComparator = null;
        } else {
            upperBoundColIndex = ins[1].findColumnIndex(upperColPort1);
            if (upperBoundColIndex < 0) {
                throw new InvalidSettingsException(
                        "No such column in 2nd input: " + upperColPort1);
            }
            DataType type = ins[1].getColumnSpec(upperBoundColIndex).getType();
            if (valueType.equals(type)) {
                upperBoundComparator = valueType.getComparator();
            } else {
                setWarningMessage("The types of the comparison and value "
                        + "columns are not equal, comparison might be done "
                        + "based on lexicographical string representation!");
                upperBoundComparator = DataType.getCommonSuperType(
                        valueType, type).getComparator();
            }
        }

        final int labelColIndex = ins[1].findColumnIndex(labelCol);
        if (labelColIndex < 0) {
            throw new InvalidSettingsException(
                    "No such column in 2nd input: " + labelCol);
        }
        DataColumnSpecCreator labelColSpecCreator =
            new DataColumnSpecCreator(ins[1].getColumnSpec(labelColIndex));
        labelColSpecCreator.removeAllHandlers();
        String name = DataTableSpec.getUniqueColumnName(ins[0], labelCol);
        labelColSpecCreator.setName(name);
        final DataColumnSpec labelColSpec = labelColSpecCreator.createSpec();

        final BinByDictionaryRuleSet ruleSet = new BinByDictionaryRuleSet(
            lowerBoundComparator, isLowerBoundInclusive,
            upperBoundComparator, isUpperBoundInclusive, c.isUseBinarySearch());
        if (port1Table != null) { // in execute
            long rowCount = port1Table.size();
            long current = 1;
            for (DataRow r : port1Table) {
                DataCell lower = lowerBoundColIndex < 0 ? null
                        : r.getCell(lowerBoundColIndex);
                DataCell upper = upperBoundColIndex < 0 ? null
                        : r.getCell(upperBoundColIndex);
                DataCell label = r.getCell(labelColIndex);
                ruleSet.addRule(lower, upper, label);
                exec.setProgress(/*no prog */0.0,
                        "Indexing rule table " + (current++) + "/" + rowCount
                        + " (\"" + r.getKey() + "\")");
                exec.checkCanceled();
            }
        }
        ruleSet.close();
        SingleCellFactory fac = new SingleCellFactory(ruleSet.getSize() > 100, labelColSpec) {

            @Override
            public DataCell getCell(final DataRow row) {
                DataCell value = row.getCell(valueColIndexPort0);
                if (value.isMissing()) {
                    return DataType.getMissingCell();
                }
                DataCell result = ruleSet.search(value);
                if (result != null) {
                    return result;
                }
                if (c.isFailIfNoRuleMatches()) {
                    throw new RuntimeException("No rule matched for row \""
                            + row.getKey() + "\", value: \"" + value + "\"");
                }
                return DataType.getMissingCell();
            }
        };
        ColumnRearranger rearranger = new ColumnRearranger(ins[0]);
        rearranger.append(fac);
        return rearranger;
    }

    /** {@inheritDoc} */
    @Override
    protected void reset() {
        // no internals
    }

    /** {@inheritDoc} */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        BinByDictionaryConfiguration c = new BinByDictionaryConfiguration();
        c.loadSettingsModel(settings);
    }

    /** {@inheritDoc} */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        BinByDictionaryConfiguration c = new BinByDictionaryConfiguration();
        c.loadSettingsModel(settings);
        m_configuration = c;
    }

    /** {@inheritDoc} */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        if (m_configuration != null) {
            m_configuration.saveSettingsTo(settings);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // no internals
    }

    /** {@inheritDoc} */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // no internals
    }

}
