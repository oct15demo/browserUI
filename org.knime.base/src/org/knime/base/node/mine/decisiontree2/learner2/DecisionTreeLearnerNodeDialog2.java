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
 *   25.10.2006 (sieb): created
 */
package org.knime.base.node.mine.decisiontree2.learner2;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.base.node.mine.decisiontree2.PMMLMissingValueStrategy;
import org.knime.base.node.mine.decisiontree2.PMMLNoTrueChildStrategy;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.NominalValue;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;
import org.knime.core.node.util.CombinedColumnFilter;
import org.knime.core.node.util.DataValueColumnFilter;

/**
 * Dialog for a decision tree learner node.
 *
 * @author Christoph Sieb, University of Konstanz
 * @since 2.6
 */
public class DecisionTreeLearnerNodeDialog2 extends DefaultNodeSettingsPane {

    /**
     * Constructor: create NodeDialog with one column selectors and two other
     * properties.
     */
    @SuppressWarnings("unchecked")
    public DecisionTreeLearnerNodeDialog2() {
        createNewGroup("General");
        // class column selection
        DialogComponentColumnNameSelection classCol = new DialogComponentColumnNameSelection(
            createSettingsClassColumn(), "Class column", DecisionTreeLearnerNodeModel2.DATA_INPORT, NominalValue.class);
        this.addDialogComponent(classCol);

        // quality measure
        String[] qualityMethods =
                {DecisionTreeLearnerNodeModel2.SPLIT_QUALITY_GAIN_RATIO,
                        DecisionTreeLearnerNodeModel2.SPLIT_QUALITY_GINI};
        this.addDialogComponent(new DialogComponentStringSelection(
                createSettingsQualityMeasure(),
                        "Quality measure", qualityMethods));

        // pruning method
        String[] methods =
                {DecisionTreeLearnerNodeModel2.PRUNING_NO,
                 DecisionTreeLearnerNodeModel2.PRUNING_MDL};
                 // DecisionTreeLearnerNodeModel.PRUNING_ESTIMATED_ERROR};
        this.addDialogComponent(new DialogComponentStringSelection(
                createSettingsPruningMethod(), "Pruning method", methods));

        this.addDialogComponent(new DialogComponentBoolean(
            createSettingsReducedErrorPruning(), "Reduced Error Pruning"));


        // confidence value threshold for c4.5 pruning
//        this.addDialogComponent(new DialogComponentNumber(
//              createSettingsConfidenceValue(),
//              "Confidence threshold (estimated error)", 0.01, 7));

        // min number records for a node also used for determine whether a partition is useful
        // both are closely related
        this.addDialogComponent(new DialogComponentNumber(
            createSettingsMinNumRecords(), "Min number records per node", 1));

        // number records to store for the view
        this.addDialogComponent(new DialogComponentNumber(
            createSettingsNumberRecordsForView(),
                   "Number records to store for view", 100));

        // split point set to average value or to upper value of lower partition
        this.addDialogComponent(new DialogComponentBoolean(
                createSettingsSplitPoint(), "Average split point"));

        // number processors to use
        this.addDialogComponent(new DialogComponentNumber(
                createSettingsNumProcessors(), "Number threads", 1, 5));

        // skip columns with many nominal values
        this.addDialogComponent(new DialogComponentBoolean(
                createSettingsSkipNominalColumnsWithoutDomain(),
                "Skip nominal columns without domain information"));

        createNewGroup("Root split");
        // check box to specify use of first split column
        DialogComponentBoolean useFirstSplitCol =
            new DialogComponentBoolean(createSettingsUseFirstSplitColumn(), "Force root split column");
        this.addDialogComponent(useFirstSplitCol);

        ColumnFilter classNameFilter = new ColumnFilter() {

            @Override
            public boolean includeColumn(final DataColumnSpec colSpec) {
                return !colSpec.getName().equals(((SettingsModelString)classCol.getModel()).getStringValue());
            }

            @Override
            public String allFilteredMsg() {
                return "Filtered all columns";
            }

        };

        ColumnFilter combinedFilter = new CombinedColumnFilter(new DataValueColumnFilter(NominalValue.class, DoubleValue.class), classNameFilter);

        DialogComponentColumnNameSelection firstSplitCol = new DialogComponentColumnNameSelection(createSettingsFirstSplitColumn(
            (SettingsModelBoolean)useFirstSplitCol.getModel()),
            "Root split column", 0, combinedFilter);
        this.addDialogComponent(firstSplitCol);

        // change possible values for firstCol depending on classCol (the same column can't be class and first split column)
        classCol.getModel().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(final ChangeEvent e) {
                try {
                    firstSplitCol.setColumnFilter(combinedFilter);
                } catch (NotConfigurableException e1) {
                    // This is only possible if there is only one ordinary column in the spec
                }
            }
        });

        createNewGroup("Binary nominal splits");
        // binary nominal split mode
        SettingsModelBoolean binarySplitMdl =
            createSettingsBinaryNominalSplit();
        DialogComponentBoolean binarySplit = new DialogComponentBoolean(
                binarySplitMdl, "Binary nominal splits");
        this.addDialogComponent(binarySplit);

        // max number nominal values for complete subset calculation for binary
        // nominal splits
        final DialogComponentNumber maxNominalBinary
                = new DialogComponentNumber(
                createSettingsBinaryMaxNominalValues(), "Max #nominal",
                1, 5);
        this.addDialogComponent(maxNominalBinary);

        final DialogComponentBoolean filterNominalValuesFromParent =
            new DialogComponentBoolean(
                    createSettingsFilterNominalValuesFromParent(binarySplitMdl),
                    "Filter invalid attribute values in child nodes");
        addDialogComponent(filterNominalValuesFromParent);

        /* Enable the max nominal binary settings if binary split is
         * enabled. */
        binarySplit.getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                boolean selected = ((SettingsModelBoolean)e.getSource())
                        .getBooleanValue();
                maxNominalBinary.getModel().setEnabled(selected);
            }
        });

        createNewTab("PMMLSettings");
        //
        createNewGroup("No true child strategy");
        String[] methodsnTC =
                {PMMLNoTrueChildStrategy.RETURN_LAST_PREDICTION.toString(),
                 PMMLNoTrueChildStrategy.RETURN_NULL_PREDICTION.toString()};
        this.addDialogComponent(new DialogComponentButtonGroup(
                createSettingsnoTrueChildMethod(), true,
                null , methodsnTC));
        closeCurrentGroup();
        createNewGroup("Missing Value Strategy");
        String[] methodsMV =
            {
            PMMLMissingValueStrategy.LAST_PREDICTION.toString(),
//                PMMLMissingValueStrategy.NULL_PREDICTION.toString(),// not supported in predictor
//                PMMLMissingValueStrategy.DEFAULT_CHILD.toString(),  // bug 4780 not supported by learner
//                PMMLMissingValueStrategy.WEIGHTED_CONFIDENCE.toString(), // not supported in predictor
//                PMMLMissingValueStrategy.AGGREGATE_NODES.toString(), // not supported in predictor
                PMMLMissingValueStrategy.NONE.toString()
                };
        this.addDialogComponent(new DialogComponentButtonGroup(createSettingsmissValueStrategyMethod(), true,
                                                               null, methodsMV));
        closeCurrentGroup();

    }

    /**
     * @return class column selection
     */
    static SettingsModelString createSettingsClassColumn() {
        return new SettingsModelString(
                    DecisionTreeLearnerNodeModel2.KEY_CLASSIFYCOLUMN, null);
    }

    /**
     * @return quality measure
     */
    static SettingsModelString createSettingsQualityMeasure() {
        return new SettingsModelString(
                DecisionTreeLearnerNodeModel2.KEY_SPLIT_QUALITY_MEASURE,
                DecisionTreeLearnerNodeModel2.DEFAULT_SPLIT_QUALITY_MEASURE);
    }

    /**
     * @return noTrueChild method
     */
    static SettingsModelString createSettingsnoTrueChildMethod() {
        return new SettingsModelString(
                    DecisionTreeLearnerNodeModel2.KEY_NOTRUECHILD,
                    PMMLNoTrueChildStrategy.RETURN_NULL_PREDICTION.toString());
    }

    /**
     * @return missingValueStrategy method
     */
    static SettingsModelString createSettingsmissValueStrategyMethod() {
        return new SettingsModelString(
                    DecisionTreeLearnerNodeModel2.KEY_MISSINGSTRATEGY,
                    PMMLMissingValueStrategy.LAST_PREDICTION.toString());
    }

    /**
     * @return pruning method
     */
    static SettingsModelString createSettingsPruningMethod() {
        return new SettingsModelString(
                    DecisionTreeLearnerNodeModel2.KEY_PRUNING_METHOD,
                    DecisionTreeLearnerNodeModel2.DEFAULT_PRUNING_METHOD);
    }

    /**
     * Create a new settings model boolean to switch on/off the reduced error pruning option, default is true.
     * @return a new settings model for reduced error pruning
     * @since 2.8
     */
    static SettingsModelBoolean createSettingsReducedErrorPruning() {
        return new SettingsModelBoolean(
                   DecisionTreeLearnerNodeModel2.KEY_REDUCED_ERROR_PRUNING,
                   DecisionTreeLearnerNodeModel2.DEFAULT_REDUCED_ERROR_PRUNING);
    }

    /**
     * @return confidence value threshold for c4.5 pruning
     */
    static SettingsModelDoubleBounded createSettingsConfidenceValue() {
        return new SettingsModelDoubleBounded(
          DecisionTreeLearnerNodeModel2.KEY_PRUNING_CONFIDENCE_THRESHOLD,
          DecisionTreeLearnerNodeModel2.DEFAULT_PRUNING_CONFIDENCE_THRESHOLD,
          0.0, 1.0);
    }

    /**
     * @return minimum number of objects per node
     */
    static SettingsModelIntegerBounded createSettingsMinNumRecords() {
        // min number records for a node also used for determine whether a
        // partition is useful both are closely related
        return new SettingsModelIntegerBounded(
                DecisionTreeLearnerNodeModel2.KEY_MIN_NUMBER_RECORDS_PER_NODE,
                DecisionTreeLearnerNodeModel2.DEFAULT_MIN_NUM_RECORDS_PER_NODE,
                1, Integer.MAX_VALUE);
    }

    /**
     * @return number records to store for the view
     */
    static SettingsModelIntegerBounded createSettingsNumberRecordsForView() {
        return new SettingsModelIntegerBounded(
               DecisionTreeLearnerNodeModel2.KEY_NUMBER_VIEW_RECORDS,
               DecisionTreeLearnerNodeModel2.DEFAULT_NUMBER_RECORDS_FOR_VIEW,
               0, Integer.MAX_VALUE);
    }

    /**
     * @return split point set to average value or to upper value of lower
     *         partition
     */
    static SettingsModelBoolean createSettingsSplitPoint() {
        return new SettingsModelBoolean(
                    DecisionTreeLearnerNodeModel2.KEY_SPLIT_AVERAGE,
                    DecisionTreeLearnerNodeModel2.DEFAULT_SPLIT_AVERAGE);
    }

    /**
     * @return binary nominal split mode
     */
    static SettingsModelBoolean createSettingsBinaryNominalSplit() {
        return new SettingsModelBoolean(
            DecisionTreeLearnerNodeModel2.KEY_BINARY_NOMINAL_SPLIT_MODE,
            DecisionTreeLearnerNodeModel2.DEFAULT_BINARY_NOMINAL_SPLIT_MODE);
    }

    /**
     * @return binary nominal split mode
     */
    static SettingsModelBoolean
            createSettingsSkipNominalColumnsWithoutDomain() {
        SettingsModelBoolean setting = new SettingsModelBoolean(
                DecisionTreeLearnerNodeModel2.KEY_SKIP_COLUMNS,
                DecisionTreeLearnerNodeModel2.DEFAULT_BINARY_NOMINAL_SPLIT_MODE);
        setting.setBooleanValue(true);
        return setting;
    }

    /**
     * @return max number nominal values for complete subset calculation for
     *         binary nominal splits
     */
    static SettingsModelIntegerBounded createSettingsBinaryMaxNominalValues() {
        SettingsModelIntegerBounded model = new SettingsModelIntegerBounded(
                DecisionTreeLearnerNodeModel2.KEY_BINARY_MAX_NUM_NOMINAL_VALUES,
                DecisionTreeLearnerNodeModel2
                    .DEFAULT_MAX_BIN_NOMINAL_SPLIT_COMPUTATION,
                1, Integer.MAX_VALUE);
        model.setEnabled(DecisionTreeLearnerNodeModel2
                .DEFAULT_BINARY_NOMINAL_SPLIT_MODE);
        return model;
    }

    /**
     * @param skipNominalColumnsWithoutDomainModel model to listen to for
     * enablement (only enable if binary nominal splits)
     * @return model representing {@link
     * DecisionTreeLearnerNodeModel2#KEY_FILTER_NOMINAL_VALUES_FROM_PARENT}
     */
    static SettingsModelBoolean createSettingsFilterNominalValuesFromParent(
            final SettingsModelBoolean skipNominalColumnsWithoutDomainModel) {
        final SettingsModelBoolean model = new SettingsModelBoolean(
            DecisionTreeLearnerNodeModel2.KEY_FILTER_NOMINAL_VALUES_FROM_PARENT,
            false);
        skipNominalColumnsWithoutDomainModel.addChangeListener(
                new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                model.setEnabled(
                        skipNominalColumnsWithoutDomainModel.getBooleanValue());
            }
        });
        model.setEnabled(skipNominalColumnsWithoutDomainModel.getBooleanValue());
        return model;
    }

    /**
     * @return number processors to use
     */
    static SettingsModelIntegerBounded createSettingsNumProcessors() {
        return new SettingsModelIntegerBounded(DecisionTreeLearnerNodeModel2.KEY_NUM_PROCESSORS,
            DecisionTreeLearnerNodeModel2.DEFAULT_NUM_PROCESSORS, 1, Integer.MAX_VALUE);
    }

    /**
     * @return name of column to perform first split on
     */
    static SettingsModelString createSettingsFirstSplitColumn(final SettingsModelBoolean useFirstSplitCol) {
        SettingsModelString firstSplitCol = new SettingsModelString(DecisionTreeLearnerNodeModel2.KEY_FIRST_SPLIT_COL, null);
        useFirstSplitCol.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(final ChangeEvent e) {
                firstSplitCol.setEnabled(((SettingsModelBoolean)e.getSource()).getBooleanValue());
            }

        });
        firstSplitCol.setEnabled(useFirstSplitCol.getBooleanValue());
        return firstSplitCol;
    }

    /**
     *
     */
    static SettingsModelBoolean createSettingsUseFirstSplitColumn() {
        return new SettingsModelBoolean(DecisionTreeLearnerNodeModel2.KEY_USE_FIRST_SPLIT_COL, false);
    }
}
