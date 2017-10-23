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
 * History
 *   28.10.2010 (gabriel): created
 */
package org.knime.base.node.image;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.port.image.ImagePortObjectSpec;

/**
 * Node model allows translating an generic image port object into a table with
 * one cell.
 *
 * @author Thomas Gabriel, KNIME.com, Zurich, Switzerland
 */
public class ImageToTableNodeModel extends NodeModel {

    private final SettingsModelString m_rowKeyModel =
        ImageToTableNodeDialog.createStringModel();

    private final SettingsModelString m_columnNameModel =
            ImageToTableNodeDialog.createColumnNameModel();

    /**
     * New node model with on image port input and a data table output.
     */
    public ImageToTableNodeModel() {
        super(new PortType[]{ImagePortObject.TYPE},
                new PortType[] {BufferedDataTable.TYPE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        ImagePortObjectSpec inspec = (ImagePortObjectSpec) inSpecs[0];
        DataTableSpec outspec = createResultSpec(inspec, m_columnNameModel.getStringValue());
        return new DataTableSpec[] {outspec};
    }

    private static DataTableSpec createResultSpec(final ImagePortObjectSpec inspec, final String colName) {
        return new DataTableSpec(new DataColumnSpecCreator(colName, inspec.getDataType()).createSpec());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inObjects,
            final ExecutionContext exec) throws Exception {
        ImagePortObject ipo = (ImagePortObject) inObjects[0];
        DataTableSpec outspec = createResultSpec(ipo.getSpec(), m_columnNameModel.getStringValue());
        BufferedDataContainer buf = exec.createDataContainer(outspec);
        RowKey rowKey;
        String rowKeyValue = m_rowKeyModel.getStringValue();
        if (rowKeyValue == null || rowKeyValue.trim().isEmpty()) {
            rowKey = ImageToTableNodeDialog.DEFAULT_ROWKEY;
        } else {
            rowKey = new RowKey(rowKeyValue);
        }
        buf.addRowToTable(new DefaultRow(rowKey, ipo.toDataCell()));
        buf.close();
        buf.getTable();
        return new PortObject[] {buf.getTable()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // empty
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // empty
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_rowKeyModel.saveSettingsTo(settings);
        m_columnNameModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_rowKeyModel.validateSettings(settings);
        if (settings.containsKey(m_columnNameModel.getKey())) {
          //introduced in KNIME 2.10
            final String colName =
                    ((SettingsModelString)m_columnNameModel.createCloneWithValidatedValue(settings)).getStringValue();
            if (colName == null || colName.trim().isEmpty()) {
                throw new InvalidSettingsException("Please specify a column name.");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_rowKeyModel.loadSettingsFrom(settings);
        if (settings.containsKey(m_columnNameModel.getKey())) {
            //introduced in KNIME 2.10
            m_columnNameModel.loadSettingsFrom(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // empty
    }

}
