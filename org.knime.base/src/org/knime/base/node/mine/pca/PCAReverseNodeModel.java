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
 * 
 */
package org.knime.base.node.mine.pca;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.knime.base.data.append.column.AppendedColumnTable;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import Jama.Matrix;

/**
 * Invert PCA transformation to transform data back to original space.
 * 
 * @author Uwe Nagel, University of Konstanz
 */
public class PCAReverseNodeModel extends NodeModel {
    /**
     * config string determining of columns with pca coordinates shall be
     * removed.
     */
    static final String REMOVE_PCACOLS = "removePCACOLS";

    /**
     * create node.
     */
    protected PCAReverseNodeModel() {
        super(new PortType[]{PCAModelPortObject.TYPE, BufferedDataTable.TYPE},
                new PortType[]{BufferedDataTable.TYPE});

    }

    /**
     * Config key, for the number of dimensions the original data is reduced to.
     */
    protected static final String RESULT_DIMENSIONS = "result_dimensions";

    /** Index of input data port. */
    public static final int DATA_INPORT = 1;

    /** Index of model data port. */
    public static final int MODEL_INPORT = 0;

    /** Index of input data port. */
    public static final int DATA_OUTPORT = 0;

    /**
     * config string for columns containing pca coordinates.
     */
    public static final String PCA_COLUMNS = "pcaCols";

    /** pca columns. */
    private final SettingsModelFilterString m_pcaColumns =
        new SettingsModelFilterString(PCA_COLUMNS);

    /** determine if pca cols are to be removed. */
    private final SettingsModelBoolean m_removePCACols =
        new SettingsModelBoolean(REMOVE_PCACOLS, true);

    /** fail on missing data? */
    private final SettingsModelBoolean m_failOnMissingValues =
        new SettingsModelBoolean(PCANodeModel.FAIL_MISSING, false);

    private String[] m_inputColumnNames = {};


    private int[] m_inputColumnIndices;

    /**
     * Performs the PCA.
     * 
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {

        final PCAModelPortObject model =
            (PCAModelPortObject)inData[MODEL_INPORT];
        final Matrix eigenvectors =
            EigenValue.getSortedEigenVectors(model.getEigenVectors(), model
                    .getEigenvalues(), m_inputColumnIndices.length);
        if (m_failOnMissingValues.getBooleanValue()) {
            for (final DataRow row : (DataTable)inData[DATA_INPORT]) {
                for (int i = 0; i < m_inputColumnIndices.length; i++) {
                    if (row.getCell(m_inputColumnIndices[i]).isMissing()) {
                        throw new IllegalArgumentException(
                                "data table contains missing values");
                    }
                }
            }

        }
        final String[] origColumnNames =
            ((PCAModelPortObjectSpec)((PCAModelPortObject)inData[MODEL_INPORT])
                    .getSpec()).getColumnNames();
        final DataColumnSpec[] specs =
            createAddTableSpec(
                    (DataTableSpec)inData[DATA_INPORT].getSpec(),
                    origColumnNames);

        final CellFactory fac = new CellFactory() {

            @Override
            public DataCell[] getCells(final DataRow row) {
                return convertInputRow(eigenvectors, row, model.getCenter(),
                        m_inputColumnIndices, origColumnNames.length);
            }

            @Override
            public DataColumnSpec[] getColumnSpecs() {

                return specs;
            }

            @Override
            public void setProgress(final int curRowNr, final int rowCount,
                    final RowKey lastKey, final ExecutionMonitor texec) {
                texec.setProgress((double)curRowNr / rowCount);

            }

        };
        final ColumnRearranger cr =
            new ColumnRearranger((DataTableSpec)inData[DATA_INPORT]
                                                       .getSpec());
        cr.append(fac);
        if (m_removePCACols.getBooleanValue()) {
            cr.remove(m_inputColumnIndices);
        }
        final BufferedDataTable result =
            exec.createColumnRearrangeTable(
                    (BufferedDataTable)inData[DATA_INPORT], cr, exec);
        final PortObject[] out = {result};
        return out;
    }

    /**
     * reduce a single input row to the principal components.
     * 
     * @param eigenvectors transposed matrix of eigenvectors (eigenvectors in
     *            rows, number of eigenvectors corresponds to dimensions to be
     *            projected to)
     * @param row the row to convert
     * @param means vector with means of columns
     * @param inputColumnIndices indices of input columns
     * @param resultDimensions number of dimensions
     * @return array of data cells to be added to the row
     */
    protected static DataCell[] convertInputRow(final Matrix eigenvectors,
            final DataRow row, final double[] means,
            final int[] inputColumnIndices, final int resultDimensions) {
        // get row of input values
        boolean missingValues = false;
        for (int i = 0; i < inputColumnIndices.length; i++) {
            if (row.getCell(inputColumnIndices[i]).isMissing()) {
                missingValues = true;
                continue;
            }
        }
        // put each cell of a pca row into the row to append
        final DataCell[] cells = new DataCell[resultDimensions];

        if (missingValues) {
            for (int i = 0; i < resultDimensions; i++) {
                cells[i] = DataType.getMissingCell();
            }
        } else {
            final double[] rowVec = new double[resultDimensions];
            try {
                for (int i = 0; i < rowVec.length; i++) {
                    for (int j = 0; j < eigenvectors.getColumnDimension(); j++) {
                        rowVec[i] +=
                            ((DoubleValue)row
                                    .getCell(inputColumnIndices[j]))
                                    .getDoubleValue()
                                    * eigenvectors.get(i, j);
                    }
                    rowVec[i] += means[i];
                }
            } catch (final ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < resultDimensions; i++) {
                cells[i] = new DoubleCell(rowVec[i]);
            }
        }
        return cells;
    }

    /**
     * create part of table spec to be added to the input table.
     * 
     * @param inSpecs input specs (for unique column names)
     * @param colNames names of input columns
     * @return part of table spec to be added to input table
     */
    public DataColumnSpec[] createAddTableSpec(final DataTableSpec inSpecs,
            final String[] colNames) {
        // append pca columns
        final DataColumnSpec[] specs = new DataColumnSpec[colNames.length];

        for (int i = 0; i < colNames.length; i++) {
            final String colName =
                DataTableSpec.getUniqueColumnName(inSpecs, colNames[i]);
            final DataColumnSpecCreator specCreator =
                new DataColumnSpecCreator(colName, DataType
                        .getType(DoubleCell.class));
            specs[i] = specCreator.createSpec();
        }
        return specs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
    throws InvalidSettingsException {
        m_inputColumnNames = new String[m_pcaColumns.getIncludeList().size()];
        int index = 0;
        for (final String input : m_pcaColumns.getIncludeList()) {
            if (((DataTableSpec)inSpecs[DATA_INPORT]).getColumnSpec(input) == null) {
                // seems to be reconnected, activate default config
                m_inputColumnNames = new String[0];
                m_inputColumnIndices = new int[0];
                break;
            }
            m_inputColumnNames[index++] = input;
        }
        // no pca cols selected -> select default ones
        if (m_inputColumnNames.length == 0) {
            final DataTableSpec dts = (DataTableSpec)inSpecs[DATA_INPORT];
            final LinkedList<String> include = new LinkedList<String>();
            final LinkedList<String> exclude = new LinkedList<String>();

            for (int i = 0; i < dts.getNumColumns(); i++) {
                final DataColumnSpec dcs = dts.getColumnSpec(i);

                if (dcs.getName().startsWith(PCANodeModel.PCA_COL_PREFIX)) {
                    include.add(dcs.getName());
                } else {
                    exclude.add(dcs.getName());
                }
            }

            m_pcaColumns.setIncludeList(include);
            m_pcaColumns.setExcludeList(exclude);
            m_inputColumnNames = new String[include.size()];
            index = 0;
            for (final String input : include) {
                m_inputColumnNames[index++] = input;
            }
        }
        // no pca cols found
        if (m_inputColumnNames.length == 0) {
            throw new InvalidSettingsException("no columns for pca chosen");
        }
        m_inputColumnIndices = new int[m_inputColumnNames.length];
        index = 0;
        for (final String colName : m_inputColumnNames) {
            final DataColumnSpec colspec =
                ((DataTableSpec)inSpecs[DATA_INPORT])
                .getColumnSpec(colName);

            if (!colspec.getType().isCompatible(DoubleValue.class)) {
                throw new InvalidSettingsException("column \"" + colName
                        + "\" is not compatible with double");
            }
            m_inputColumnIndices[index++] =
                ((DataTableSpec)inSpecs[DATA_INPORT])
                .findColumnIndex(colName);
        }

        final DataColumnSpec[] specs =
            createAddTableSpec((DataTableSpec)inSpecs[DATA_INPORT],
                    ((PCAModelPortObjectSpec)inSpecs[MODEL_INPORT])
                    .getColumnNames());

        final DataTableSpec dts =
            AppendedColumnTable.getTableSpec(
                    (DataTableSpec)inSpecs[DATA_INPORT], specs);
        if (m_removePCACols.getBooleanValue()) {
            final ColumnRearranger columnRearranger = new ColumnRearranger(dts);
            columnRearranger.remove(m_inputColumnIndices);
            return new DataTableSpec[]{columnRearranger.createSpec()};
        }

        return new DataTableSpec[]{dts};

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
    throws InvalidSettingsException {
        m_removePCACols.loadSettingsFrom(settings);
        m_pcaColumns.loadSettingsFrom(settings);
        m_failOnMissingValues.loadSettingsFrom(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        m_inputColumnNames = new String[]{};
        m_inputColumnIndices = new int[]{};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_removePCACols.saveSettingsTo(settings);
        m_pcaColumns.saveSettingsTo(settings);
        m_failOnMissingValues.saveSettingsTo(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
    throws InvalidSettingsException {
        m_removePCACols.validateSettings(settings);
        m_pcaColumns.validateSettings(settings);
        m_failOnMissingValues.validateSettings(settings);

    }
}
