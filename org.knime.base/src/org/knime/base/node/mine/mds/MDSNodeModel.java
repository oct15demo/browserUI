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
 *   07.03.2008 (Kilian Thiel): created
 */
package org.knime.base.node.mine.mds;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.base.node.mine.mds.distances.DistanceManagerFactory;
import org.knime.base.node.mine.sota.logic.SotaUtil;
import org.knime.base.node.util.DefaultDataArray;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
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
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class MDSNodeModel extends NodeModel {

    /**
     * The default number of rows to use.
     */
    public static final int DEF_NO_ROWS = 1000;

    /**
     * The default settings of the usage of the number of max rows.
     */
    public static final boolean DEF_USE_MAX_ROWS = true;

    /**
     * The minimum number of rows to use.
     */
    public static final int MIN_NO_ROWS = 1;

    /**
     * The maximum number of rows to use.
     */
    public static final int MAX_NO_ROWS = Integer.MAX_VALUE;

    /**
     * The default value of the learning rate.
     */
    public static final double DEF_LEARNINGRATE = 1.0;

    /**
     * The minimum value of the learning rate.
     */
    public static final double MIN_LEARNINGRATE = 0.0;

    /**
     * The maximum value of the learning rate.
     */
    public static final double MAX_LEARNINGRATE = 1.0;

    /**
     * The default value of the epochs.
     */
    public static final int DEF_EPOCHS = 50;

    /**
     * The minimum value of the epochs.
     */
    public static final int MIN_EPOCHS = 1;

    /**
     * The maximum value of the epochs.
     */
    public static final int MAX_EPOCHS = Integer.MAX_VALUE;

    /**
     * The default value of the output dimension.
     */
    public static final int DEF_OUTPUTDIMS = 2;

    /**
     * The minimum value of the output dimension.
     */
    public static final int MIN_OUTPUTDIMS = 1;

    /**
     * The maximum value of the output dimension.
     */
    public static final int MAX_OUTPUTDIMS = Integer.MAX_VALUE;

    /**
     * The default value of the distance to use.
     */
    public static final String DEF_DISTANCE =
        DistanceManagerFactory.EUCLIDEAN_DIST;



    private SettingsModelIntegerBounded m_rowsModel =
        MDSNodeDialog.getRowsModel();

    private SettingsModelBoolean m_useRowsModel =
        MDSNodeDialog.getUseMaxRowsModel();

    private SettingsModelDoubleBounded m_learnrateModel =
        MDSNodeDialog.getLearnrateModel();

    private SettingsModelIntegerBounded m_epochsModel =
        MDSNodeDialog.getEpochModel();

    private SettingsModelIntegerBounded m_outputDimModel =
        MDSNodeDialog.getOutputDimModel();

    private SettingsModelString m_distModel = MDSNodeDialog.getDistanceModel();

    private SettingsModelFilterString m_colModel =
        MDSNodeDialog.getColumnModel();

    private SettingsModelIntegerBounded m_seedModel =
        MDSNodeDialog.getSeedModel();

    private MDSManager m_manager;

    private List<String> m_includeList;

    private boolean m_fuzzy = false;

    /**
     * Creates a new instance of <code>MDSNodeModel</code>.
     */
    public MDSNodeModel() {
        super(1, 1);
        m_useRowsModel.addChangeListener(new CheckBoxChangeListener());
        checkUncheck();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        assert inSpecs.length == 1;
        m_includeList = m_colModel.getIncludeList();
        List<String> allColumns = new ArrayList<String>();
        StringBuffer buffer = new StringBuffer();

        // check number of selected columns
        int numberCells = 0;
        int fuzzyCells = 0;
        for (int i = 0; i < inSpecs[0].getNumColumns(); i++) {
            allColumns.add(inSpecs[0].getColumnSpec(i).getName());
            if (m_includeList.contains(inSpecs[0].getColumnSpec(i).getName())) {
                DataType type = inSpecs[0].getColumnSpec(i).getType();

                if (SotaUtil.isNumberType(type)) {
                    numberCells++;
                } else if (SotaUtil.isFuzzyIntervalType(type)) {
                    fuzzyCells++;
                }
            }
        }

        // check if selected columns are still in spec
        for (String s : m_includeList) {
            if (!allColumns.contains(s)) {
                buffer.append("Selected column are not in spec !");
            }
        }

        // throw exception if number of selected columns is not valid.
        if (numberCells <= 0 && fuzzyCells <= 0) {
            buffer.append("Number of columns has to be "
                    + "greater than zero !");
        } else if (numberCells > 0 && fuzzyCells > 0) {
            buffer.append("Number cells and fuzzy cells must not be mixed !");
        } else if (fuzzyCells > 0) {
            m_fuzzy = true;
        } else if (numberCells > 0) {
            m_fuzzy = false;
        }

        // if buffer throw exception
        if (buffer.length() > 0) {
            throw new InvalidSettingsException(buffer.toString());
        }

        // create output table spec (input spec with the additional mds columns
        // appended).
        ColumnRearranger rearranger = createColumnRearranger(inSpecs[0],
                new MDSCellFactory(null, m_outputDimModel.getIntValue()));

        return new DataTableSpec[]{rearranger.createSpec()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

        int rowsToUse = m_rowsModel.getIntValue();
        if (m_useRowsModel.getBooleanValue()) {
            rowsToUse = inData[0].getRowCount();
        }

        // Warn if number of rows is greater than chosen number of rows
        if (inData[0].getRowCount() > rowsToUse) {
            setWarningMessage("Maximal number of rows to report is less than number of rows in input data table !");
        }

        final DataTableSpec inSpec = inData[0].getSpec();
        final ColumnRearranger colFilter = new ColumnRearranger(inSpec);
        if (m_includeList != null) {
            colFilter.keepOnly(m_includeList.toArray(new String[m_includeList.size()]));
        }
        BufferedDataTable rowCutDataTable = exec.createColumnRearrangeTable(inData[0], colFilter,
            exec.createSilentSubProgress(0.0));

        // use only specified rows
        DataTable dataContainer = new DefaultDataArray(rowCutDataTable, 1, rowsToUse);

        // save BufferedDataTable with rows to use and ALL columns to generate
        // the output data table out of it.
        rowCutDataTable = exec.createBufferedDataTable(dataContainer, exec);

        // create MDS manager, init and train stuff
        m_manager = new MDSManager(m_outputDimModel.getIntValue(),
                m_distModel.getStringValue(), m_fuzzy, rowCutDataTable, exec);
        m_manager.init(m_seedModel.getIntValue());
        m_manager.train(m_epochsModel.getIntValue(),
                m_learnrateModel.getDoubleValue());

        // create BufferedDataTable out of mapped data.
        ColumnRearranger rearranger = createColumnRearranger(inSpec, new MDSCellFactory(
                        m_manager.getDataPoints(), m_manager.getDimension()));

        return new BufferedDataTable[] {exec.createColumnRearrangeTable(inData[0], rearranger,
                exec.createSubProgress(0.1))};
    }

    /**
     * Creates and returns the column rearranger to append columns.
     * @param originalSpec The spec of the original input data.
     * @param mdsCellFac The cell factory.
     * @return The column rearranger to append mds columns.
     * @since 2.6
     */
    private static ColumnRearranger createColumnRearranger(
            final DataTableSpec originalSpec, final MDSCellFactory mdsCellFac) {
        ColumnRearranger rearranger = new ColumnRearranger(originalSpec);
        rearranger.append(mdsCellFac);
        return rearranger;
    }

    /**
     * Creates the <code>DataTableSpec</code> of the output data table.
     *
     * @param inPsec The <code>DataTableSpec</code> of the input data table.
     * @param dimensions The dimensions of the output data.
     * @return The <code>DataTableSpec</code> of the output data table.
     * @deprecated should not be used anymore, since column rearranger creates
     * the output spec.
     * @since 2.6
     */
    @Deprecated
    static DataTableSpec createDataTableSpec(final DataTableSpec inPsec,
            final int dimensions) {
        return new DataTableSpec(inPsec,
                new DataTableSpec(getColumnSpecs(dimensions)));
    }

    /**
     * The <code>DataColumnSpec</code>s of the mds data (columns).
     *
     * @param dimensions The dimension of the mds data.
     * @return The <code>DataColumnSpec</code>s of the mds data.
     * @deprecated should not be used anymore, since column rearranger creates
     * the output spec.
     */
    @Deprecated
    static DataColumnSpec[] getColumnSpecs(final int dimensions) {
        DataColumnSpec[] specs = new DataColumnSpec[dimensions];
        for (int i = 0; i < dimensions; i++) {
            DataColumnSpecCreator creator =
                new DataColumnSpecCreator("MDS Col " + (i + 1),
                        DoubleCell.TYPE);
            specs[i] = creator.createSpec();

        }
        return specs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_colModel.loadSettingsFrom(settings);
        m_distModel.loadSettingsFrom(settings);
        m_epochsModel.loadSettingsFrom(settings);
        m_learnrateModel.loadSettingsFrom(settings);
        m_outputDimModel.loadSettingsFrom(settings);
        m_rowsModel.loadSettingsFrom(settings);
        m_seedModel.loadSettingsFrom(settings);
        m_useRowsModel.loadSettingsFrom(settings);
        checkUncheck();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        if (m_manager != null) {
            m_manager.reset();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_colModel.saveSettingsTo(settings);
        m_distModel.saveSettingsTo(settings);
        m_epochsModel.saveSettingsTo(settings);
        m_learnrateModel.saveSettingsTo(settings);
        m_outputDimModel.saveSettingsTo(settings);
        m_rowsModel.saveSettingsTo(settings);
        m_seedModel.saveSettingsTo(settings);
        m_useRowsModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_colModel.validateSettings(settings);
        m_distModel.validateSettings(settings);
        m_epochsModel.validateSettings(settings);
        m_learnrateModel.validateSettings(settings);
        m_outputDimModel.validateSettings(settings);
        m_rowsModel.validateSettings(settings);
        m_seedModel.validateSettings(settings);
        m_useRowsModel.validateSettings(settings);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }


    /**
     *
     * @author Kilian Thiel, University of Konstanz
     */
    class CheckBoxChangeListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            checkUncheck();
        }
    }

    private void checkUncheck() {
        if (m_useRowsModel.getBooleanValue()) {
            m_rowsModel.setEnabled(false);
        } else {
            m_rowsModel.setEnabled(true);
        }
    }
}
