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
 *   03.05.2012 (kilian): created
 */
package org.knime.base.node.preproc.rounddouble;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.base.node.preproc.rounddouble.RoundDoubleConfigKeys.RoundOutputType;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;
import org.knime.core.node.util.filter.NameFilterConfiguration.FilterResult;

/**
 * The node model of the round double node. Rounding double values in specified
 * columns to the specified precision and appending new values in additional
 * columns if specified (otherwise double values are replaced by rounded
 * values).
 *
 * @author Kilian Thiel, KNIME.com, Berlin, Germany
 */
class RoundDoubleNodeModel extends SimpleStreamableFunctionNodeModel {

    /**
     * Default precision to round to.
     */
    public static final int DEF_PRECISION = 3;

    /**
     * Minimum precision to round to.
     */
    public static final int MIN_PRECISION = 0;

    /**
     * Maximum precision to round to.
     */
    public static final int MAX_PRECISION = Integer.MAX_VALUE;

    /**
     * Default append column setting.
     */
    public static final boolean DEF_APPEND_COLUMNS = true;

    /**
     * Default column suffix.
     */
    public static final String DEF_COLUMN_SUFFIX = " (rounded)";

    /**
     * Default rounding mode.
     */
    public static final String DEF_ROUNDING_MODE =
        RoundingMode.HALF_UP.name();

    /**
     * Default number mode.
     */
    public static final String DEF_NUMBER_MODE =
        NumberMode.DECIMAL_PLACES.description();

    /**
     * Possible rounding modes.
     */
    public static final String[] ROUNDING_MODES = new String[] {"UP", "DOWN",
        "CEILING", "FLOOR", "HALF_UP", "HALF_DOWN", "HALF_EVEN"};

    /**
     * Possible number modes.
     */
    public static final String[] NUMBER_MODES = NumberMode.getDescriptions();

    /**
     * The default value of the "output as string" setting.
     */
    public static final boolean DEF_OUTPUT_AS_STRING = false;

    /** Default output type (double type, rounded). */
    public static final String DEF_OUTPUT_TYPE = RoundDoubleConfigKeys.RoundOutputType.Double.getLabel();


    private SettingsModelColumnFilter2 m_filterDoubleColModel =
        RoundDoubleNodeDialog.getFilterDoubleColModel();

    private SettingsModelIntegerBounded m_numberPrecisionModel =
        RoundDoubleNodeDialog.getNumberPrecisionModel();

    private SettingsModelBoolean m_appendColumnsModel =
        RoundDoubleNodeDialog.getAppendColumnModel();

    private SettingsModelString m_columnSuffixModel =
        RoundDoubleNodeDialog.getColumnSuffixModel();

    private SettingsModelString m_roundingModeModel =
        RoundDoubleNodeDialog.getRoundingModelStringModel();

    private SettingsModelString m_outputTypeModel = RoundDoubleNodeDialog.getOutputTypeModel();


    private SettingsModelString m_numberModeModel =
        RoundDoubleNodeDialog.getNumberModeStringModel();

    /**
     * Creates new instance of <code>RoundDoubleNodeModel</code>.
     */
    RoundDoubleNodeModel() {
        m_appendColumnsModel.addChangeListener(new AppendColumnChangeListener());
    }

    /** {@inheritDoc} */
    @Override
    public ColumnRearranger createColumnRearranger(final DataTableSpec dataSpec) throws InvalidSettingsException {
        //
        /// SPEC CHECKS
        //
        FilterResult filteredCols = m_filterDoubleColModel.applyTo(dataSpec);
        // check for at least one double column in input data table spec
        if (filteredCols.getIncludes().length == 0) {
            throw new InvalidSettingsException("There are no columns containing double values in the input table!");
        }
        // check if all included columns are available in the spec
        String[] unknownCols = filteredCols.getRemovedFromIncludes();
        if (unknownCols.length == 1) {
            setWarningMessage("Column \"" + unknownCols[0] + "\" is not available.");
        } else if (unknownCols.length > 1) {
            setWarningMessage("" + unknownCols.length + " selected columns are not available anymore.");
        }

        //
        /// CREATE COLUMN REARRANGER
        //
        // parameters
        int precision = m_numberPrecisionModel.getIntValue();
        boolean append = m_appendColumnsModel.getBooleanValue();
        RoundingMode roundingMode = RoundingMode.valueOf(
                m_roundingModeModel.getStringValue());
        NumberMode numberMode = NumberMode.valueByDescription(
                m_numberModeModel.getStringValue());
        final RoundOutputType outputType = RoundOutputType.valueByTextLabel(m_outputTypeModel.getStringValue());
        String colSuffix = m_columnSuffixModel.getStringValue();

        // get array of indices of included columns
        int[] includedColIndices = getIncludedColIndices(dataSpec, filteredCols.getIncludes());

        ColumnRearranger cR = new ColumnRearranger(dataSpec);
        // create spec of new output columns
        DataColumnSpec[] newColsSpecs = getNewColSpecs(append, colSuffix,
                outputType, filteredCols.getIncludes(), dataSpec);

        // Pass all necessary parameters to the cell factory, which rounds
        // the values and creates new cells to replace or append.
        RoundDoubleCellFactory cellFac = new RoundDoubleCellFactory(precision,
                numberMode, roundingMode, outputType, includedColIndices,
                newColsSpecs);

        // replace or append columns
        if (append) {
            cR.append(cellFac);
        } else {
            cR.replace(cellFac, includedColIndices);
        }

        return cR;
    }

    private static final DataColumnSpec[] getNewColSpecs(final boolean append,
            final String colSuffix, final RoundOutputType outputType,
            final String[] colNamesToRound, final DataTableSpec origInSpec) {
        DataColumnSpec[] appColumnSpecs =
            new DataColumnSpec[colNamesToRound.length];
        int i = 0;

        // walk through column names to round to create the new column specs
        for (String colName : colNamesToRound) {
            String newColName = colName;
            // if columns are appended, append suffix to column name
            if (append) {
                newColName += colSuffix;
                newColName = DataTableSpec.getUniqueColumnName(origInSpec,
                        newColName);
            }

            // create a DoubleCell spec or a StringCell spec
            DataColumnSpec newCol = new DataColumnSpecCreator(newColName, outputType.getDataCellType()).createSpec();

            // collect column specs
            appColumnSpecs[i] = newCol;
            i++;
        }
        return appColumnSpecs;
    }

    /**
     * Creates and returns an array containing the indices of the included
     * columns in the input data table spec.
     *
     * @param dataSpec The input data table spec.
     * @param includedColNames sorted list of column names to include (sorted by position in table)
     * @return An array containing the indicies of the included columns.
     */
    private int[] getIncludedColIndices(final DataTableSpec dataSpec, final String[] includedColNames) {
        int[] includedColIndices = new int[includedColNames.length];
        int noCols = dataSpec.getNumColumns();
        List<String> asList = Arrays.asList(includedColNames);
        int j = 0;
        for (int i = 0; i < noCols; i++) {
            String currColName = dataSpec.getColumnSpec(i).getName();
            if (asList.contains(currColName)) {
                includedColIndices[j] = i;
                j++;
            }
        }
        return includedColIndices;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_filterDoubleColModel.saveSettingsTo(settings);
        m_numberPrecisionModel.saveSettingsTo(settings);
        m_appendColumnsModel.saveSettingsTo(settings);
        m_columnSuffixModel.saveSettingsTo(settings);
        m_roundingModeModel.saveSettingsTo(settings);
        m_outputTypeModel.saveSettingsTo(settings);
        m_numberModeModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_filterDoubleColModel.validateSettings(settings);
        m_numberPrecisionModel.validateSettings(settings);
        m_appendColumnsModel.validateSettings(settings);
        m_columnSuffixModel.validateSettings(settings);
        m_roundingModeModel.validateSettings(settings);
        try {
            // added in 2.8
            m_outputTypeModel.validateSettings(settings);
        } catch (InvalidSettingsException ise) {
            RoundDoubleNodeDialog.getOutputAsStringModel().validateSettings(settings);
        }
        m_numberModeModel.validateSettings(settings);

        // additional sanity checks
        StringBuffer errMsgBuffer = new StringBuffer();
        boolean err = false;

        // precision number has to be between 0 and inf
        int precision = ((SettingsModelIntegerBounded)m_numberPrecisionModel
                .createCloneWithValidatedValue(settings)).getIntValue();
        if (precision < MIN_PRECISION || precision > MAX_PRECISION) {
            errMsgBuffer.append("Rounding precision has to be between "
                    + MIN_PRECISION + " and " + MAX_PRECISION + "\n");
            err = true;
        }
        // if rounded values have to be appended, check for valid column suffix
        boolean append = ((SettingsModelBoolean)m_appendColumnsModel
                .createCloneWithValidatedValue(settings)).getBooleanValue();
        if (append) {
            String suffix = ((SettingsModelString)m_columnSuffixModel
                    .createCloneWithValidatedValue(settings)).getStringValue();
            if (suffix.length() <= 0) {
                errMsgBuffer.append("Column suffix may not be empty if append "
                        + "columns is set!\n");
                err = true;
            }
        }
        // rounding mode string needs to be a valid round mode
        String roundingModeString = ((SettingsModelString)m_roundingModeModel
                .createCloneWithValidatedValue(settings)).getStringValue();
        try {
            RoundingMode.valueOf(roundingModeString);
        } catch (Exception e) {
            errMsgBuffer.append("Specified round mode is not valid!\n");
            err = true;
        }
        // number mode string needs to be a valid number mode
        String numberModeString = ((SettingsModelString)m_numberModeModel
                .createCloneWithValidatedValue(settings)).getStringValue();
        try {
            NumberMode.valueByDescription(numberModeString);
        } catch (Exception e) {
            errMsgBuffer.append("Specified number mode is not valid!\n");
            err = true;

        // throw exception when at least one settings is invalid
        } finally {
            if (err) {
                throw new InvalidSettingsException(errMsgBuffer.toString());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_filterDoubleColModel.loadSettingsFrom(settings);
        m_numberPrecisionModel.loadSettingsFrom(settings);
        m_appendColumnsModel.loadSettingsFrom(settings);
        m_columnSuffixModel.loadSettingsFrom(settings);
        m_roundingModeModel.loadSettingsFrom(settings);
        try {
            m_outputTypeModel.loadSettingsFrom(settings);
        } catch (InvalidSettingsException ise) {
            SettingsModelBoolean outputAsStringModelDeprecated = RoundDoubleNodeDialog.getOutputAsStringModel();
            outputAsStringModelDeprecated.loadSettingsFrom(settings);
            RoundOutputType mappedType;
            if (outputAsStringModelDeprecated.getBooleanValue()) {
                mappedType = RoundOutputType.StringStandard;
            } else {
                mappedType = RoundOutputType.Double;
            }
            m_outputTypeModel.setStringValue(mappedType.getLabel());
        }
        m_numberModeModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // Nothing to reset ...
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
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
    throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }

    private class AppendColumnChangeListener implements ChangeListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            m_columnSuffixModel.setEnabled(
                    m_appendColumnsModel.getBooleanValue());
        }
    }
}
