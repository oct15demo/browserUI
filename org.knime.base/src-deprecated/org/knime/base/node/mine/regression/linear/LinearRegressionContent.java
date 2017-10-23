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
 * History
 *   Feb 23, 2006 (wiswedel): created
 */
package org.knime.base.node.mine.regression.linear;

import java.util.Arrays;
import java.util.List;

import org.knime.base.data.filter.column.FilterColumnTable;
import org.knime.base.node.mine.regression.PMMLRegressionTranslator;
import org.knime.base.node.mine.regression.PMMLRegressionTranslator.NumericPredictor;
import org.knime.base.node.mine.regression.PMMLRegressionTranslator.RegressionTable;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.pmml.PMMLPortObject;
import org.knime.core.node.port.pmml.PMMLPortObjectSpec;
import org.knime.core.node.port.pmml.PMMLPortObjectSpecCreator;


/**
 * Utility class that carries out the loading and saving of linear regression
 * models. It is used by the learner node model and the predictor node model.
 *
 * @author Bernd Wiswedel, University of Konstanz
 */
@Deprecated
public final class LinearRegressionContent {
    private static final String MODEL_NAME = "KNIME Linear Regression";
    private static final String ALGORITHM_NAME = "LinearRegression";
    private static final String CFG_OFFSET = "offset";
    private static final String CFG_MULTIPLIER = "multipliers";
    private static final String CFG_MEANS = "means";


    /** Offset value. */
    private double m_offset;

    /** Multipliers for regression evaluation. */
    private double[] m_multipliers;
    /** Mean values of all included columns used for visualization. (The view
     * shows the 2D-regression line on one input variable. We use the mean
     * values of the remaining variables to determine the two points that define
     * the regression line.) */
    private double[] m_means;

    private DataTableSpec m_spec;

    /** Public no arg constructor as required by super class. */
    public LinearRegressionContent() {
        // see comment
    }

    /**
     * Create new object with the given parameters.
     *
     * @param spec The table spec of the variables
     * @param offset The fixed (constant) offset
     * @param multipliers multiplier values
     * @param means means of all variables (used for 2D plot approximation)
     */
    public LinearRegressionContent(
            final DataTableSpec spec, final double offset,
            final double[] multipliers, final double[] means) {
        if (multipliers == null || means == null) {
            throw new NullPointerException();
        }
        int expectedLength = spec.getNumColumns() - 1;
        if (expectedLength != means.length) {
            throw new IllegalArgumentException(
                    "Confusing array length: " + means.length + ", expected "
                    + expectedLength);
        }
        if (expectedLength != multipliers.length) {
            throw new IllegalArgumentException(
                    "Confusing array length: " + multipliers.length
                    + ", expected " + expectedLength);
        }
        m_offset = offset;
        m_multipliers = multipliers;
        m_means = means;
        m_spec = spec;
    }

    /**
     * Creates a new PMML regression port object from this linear regression
     * model.
     * @param inPMMLPort the incoming PMMLPort object (can be null)
     * @param dts the full data table spec with which the regression
     *      model was created.
     * @param learningSpec a data table spec containing only learning columns
     * @return a port object
     * @throws InvalidSettingsException if the settings are invalid
     */
    public PMMLPortObject createPortObject(final PMMLPortObject inPMMLPort,
            final DataTableSpec dts, final DataTableSpec learningSpec)
        throws InvalidSettingsException {
        PMMLPortObjectSpec inPMMLSpec = null;
        if (inPMMLPort != null) {
            inPMMLSpec = inPMMLPort.getSpec();
        }
        PMMLPortObjectSpec spec = createPortObjectSpec(inPMMLSpec, dts,
                learningSpec);
        PMMLPortObject outPMMLPort = new PMMLPortObject(spec,
                inPMMLPort);

        NumericPredictor[] nps = new NumericPredictor[m_multipliers.length];
        for (int i = 0; i < nps.length; i++) {
            nps[i] = new NumericPredictor(
                    m_spec.getColumnSpec(i).getName(), 1, m_multipliers[i]);
        }
        RegressionTable regressionTable = new RegressionTable(m_offset, nps);

        /* To maintain compatibility with the previous SAX-based implementation.
         * */
        String targetField = "Response";
        List<String> targetFields = spec.getTargetFields();
        if (!targetFields.isEmpty()) {
            targetField = targetFields.get(0);
        }

        PMMLRegressionTranslator trans = new PMMLRegressionTranslator(
                MODEL_NAME, ALGORITHM_NAME, regressionTable, targetField);
        outPMMLPort.addModelTranslater(trans);

        return outPMMLPort;
    }

    /**
     * Creates a PMML port object spec based on all columns in the given data
     * table spec. <b>The target column must be the last column in the table
     * spec!</b>
     *
     * @param pmmlSpec the optional {@link PMMLPortObjectSpec} which can be null
     * @param tableSpec the full data table spec with which the regression
     *      model was created.
     * @param learningSpec a data table spec containing only columns used for
     *      learning the model
     * @return a PMML port object spec
     * @throws InvalidSettingsException if PMML incompatible type was found
     */
    public static PMMLPortObjectSpec createPortObjectSpec(
            final PMMLPortObjectSpec pmmlSpec, final DataTableSpec tableSpec,
            final DataTableSpec learningSpec)
            throws InvalidSettingsException {
        PMMLPortObjectSpecCreator c = new PMMLPortObjectSpecCreator(pmmlSpec,
                tableSpec);
        int numColumns = learningSpec.getNumColumns();
        String[] learningCols = new String[numColumns - 1];
        for (int i = 0; i < learningCols.length; i++) {
            learningCols[i] = learningSpec.getColumnSpec(i).getName();
        }
        c.setLearningCols(FilterColumnTable.createFilterTableSpec(learningSpec,
                learningCols));
        c.setTargetCols(Arrays.asList(learningSpec.getColumnSpec(
                numColumns - 1)));
        return c.createSpec();
    }

    /**
     * Get the name of the response column, i.e. the prediction column.
     *
     * @return the name of the response column
     */
    public String getTargetColumnName() {
        return m_spec.getColumnSpec(m_spec.getNumColumns() - 1).getName();
    }

    /** @return the offset */
    public double getOffset() {
        return m_offset;
    }

    /** @return the multipliers */
    public double[] getMultipliers() {
        return m_multipliers;
    }

    /**
     * Does a prediction when the given variable has the value v and all other
     * variables have their mean value. Used to determine the line in a 2D plot.
     *
     * @param variable the variable currently shown on x
     * @param v its value
     * @return the value of the linear regression line
     */
    public double getApproximationFor(final String variable, final double v) {
        double sum = m_offset;
        boolean isFound = false;
        // only iterate to last but one element (last is response column)
        for (int i = 0; i < m_spec.getNumColumns() - 1; i++) {
            DataColumnSpec col = m_spec.getColumnSpec(i);
            double val;
            if (col.getName().equals(variable)) {
                isFound = true;
                val = v;
            } else {
                val = m_means[i];
            }
            sum += m_multipliers[i] * val;
        }
        if (!isFound) {
            throw new IllegalArgumentException("No such column: " + variable);
        }
        return sum;
    }

    /**
     * @return the spec
     */
    public DataTableSpec getSpec() {
        return m_spec;
    }

    /**
     * Predicts the target value for the given row.
     *
     * @param row a data row to predict
     * @return the predicted value in a data cell
     */
    public DataCell predict(final DataRow row) {
        double sum = m_offset;
        for (int i = 0; i < row.getNumCells(); i++) {
            DataCell c = row.getCell(i);
            if (c.isMissing()) {
                return DataType.getMissingCell();
            }
            double d = ((DoubleCell)c).getDoubleValue();
            sum += m_multipliers[i] * d;
        }
        return new DoubleCell(sum);
    }

    /**
     * Saves the regression model into the model content object.
     *
     * @param par a model content object where the settings are saved to
     */
    public void save(final ModelContentWO par) {
        par.addDouble(CFG_OFFSET, m_offset);
        par.addDoubleArray(CFG_MULTIPLIER, m_multipliers);
        par.addDoubleArray(CFG_MEANS, m_means);
    }

    /**
     * Loads a linear regression model from the given model content object.
     *
     * @param par a model content object
     * @param spec the port object spec
     * @throws InvalidSettingsException if the model to load in invalid
     */
    protected void load(final ModelContentRO par, final PortObjectSpec spec)
        throws InvalidSettingsException {
        m_offset = par.getDouble(CFG_OFFSET);
        m_multipliers = par.getDoubleArray(CFG_MULTIPLIER);
        m_means = par.getDoubleArray(CFG_MEANS);
        m_spec = (DataTableSpec)spec;
        // exclude last element (response column)
        int expLength = m_spec.getNumColumns() - 1;
        if (m_means.length != expLength) {
            throw new InvalidSettingsException("Unexpected array length: "
                    + m_means.length + ", expected " + expLength);
        }
        if (m_multipliers.length != expLength) {
            throw new InvalidSettingsException("Unexpected array length: "
                    + m_multipliers.length + ", expected " + expLength);
        }
    }

    /**
     * Creates a new linear regression model that is read from the given model
     * content object.
     *
     * @param par a model content object
     * @param spec the spec for the model
     * @return a linear regression model
     * @throws InvalidSettingsException if the model to load in invalid
     */
    public static LinearRegressionContent instantiateAndLoad(
            final ModelContentRO par, final PortObjectSpec spec)
        throws InvalidSettingsException {
        LinearRegressionContent result = new LinearRegressionContent();
        result.load(par, spec);
        return result;
    }
}
