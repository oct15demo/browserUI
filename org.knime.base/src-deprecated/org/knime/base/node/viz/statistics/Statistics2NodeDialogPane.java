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
 */
package org.knime.base.node.viz.statistics;

import org.knime.base.node.viz.statistics2.Statistics3NodeDialogPane;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

/**
 * Node dialog for the Statistics node.
 *
 * @author Thomas Gabriel, University of Konstanz
 * @deprecated Use the {@link Statistics3NodeDialogPane} instead.
 */
@Deprecated
public class Statistics2NodeDialogPane extends DefaultNodeSettingsPane {

	private final SettingsModelFilterString m_filterModel;

    /** Default constructor. */
    Statistics2NodeDialogPane() {
        addDialogComponent(new DialogComponentBoolean(
                createMedianModel(),
                "Calculate median values (computationally expensive)"));
        createNewGroup("Nominal values");
        m_filterModel = createNominalFilterModel();
        addDialogComponent(new DialogComponentColumnFilter(
                m_filterModel, 0, false));
        DialogComponentNumber numNomValueComp =
            new DialogComponentNumber(createNominalValuesModel(),
             "Max no. of most frequent and infrequent values (in view): ", 5);
        numNomValueComp.setToolTipText(
             "Max no. of most frequent and infrequent "
                + "values per column displayed in the node view.");
        addDialogComponent(numNomValueComp);
        DialogComponentNumber numNomValueCompOutput =
            new DialogComponentNumber(createNominalValuesModelOutput(),
                "Max no. of possible values per column (in output table): ", 5);
        addDialogComponent(numNomValueCompOutput);
    }

    /**
     * @return create nominal filter model
     */
    static SettingsModelFilterString createNominalFilterModel() {
        return new SettingsModelFilterString("filter_nominal_columns");
    }

    /**
     * @return boolean model to compute median
     */
    static SettingsModelBoolean createMedianModel() {
        return new SettingsModelBoolean("compute_median", false);
    }

    /**
     * @return int model to restrict number of nominal values
     */
    static SettingsModelIntegerBounded createNominalValuesModel() {
        return new SettingsModelIntegerBounded(
                "num_nominal-values", 20, 0, Integer.MAX_VALUE);
    }

    /**
     * @return int model to restrict number of nominal values for the output
     */
    static SettingsModelIntegerBounded createNominalValuesModelOutput() {
        return new SettingsModelIntegerBounded(
                "num_nominal-values_output", 1000, 0, Integer.MAX_VALUE);
    }
}
