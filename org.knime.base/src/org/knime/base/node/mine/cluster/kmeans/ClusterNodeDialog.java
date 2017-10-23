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
package org.knime.base.node.mine.cluster.kmeans;

import org.knime.core.data.DoubleValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;


/**
 * Dialog for
 * {@link ClusterNodeModel} - allows
 * to adjust number of clusters and other properties.
 *
 * @author Michael Berthold, University of Konstanz
 */
public class ClusterNodeDialog extends DefaultNodeSettingsPane {

    /**
     * Constructor - set name of k-means cluster node. Also initialize special
     * property panel holding the variables that can be adjusted by the user.
     */
    @SuppressWarnings("unchecked")
    ClusterNodeDialog() {
        super();
        SettingsModelIntegerBounded smib = new SettingsModelIntegerBounded(
                ClusterNodeModel.CFG_NR_OF_CLUSTERS,
                ClusterNodeModel.INITIAL_NR_CLUSTERS,
                1, Integer.MAX_VALUE);
        DialogComponentNumber nrOfClusters = new DialogComponentNumber(
            smib, "number of clusters: ", 1, createFlowVariableModel(smib));
        DialogComponentNumber maxNrOfIterations = new DialogComponentNumber(
                new SettingsModelIntegerBounded(
                        ClusterNodeModel.CFG_MAX_ITERATIONS,
                        ClusterNodeModel.INITIAL_MAX_ITERATIONS,
                        1, Integer.MAX_VALUE),
                        "max. number of iterations: ", 10);
        DialogComponentColumnFilter columnFilter = new DialogComponentColumnFilter(
                new SettingsModelFilterString(ClusterNodeModel.CFG_COLUMNS),
                0, true, DoubleValue.class);
        DialogComponentBoolean enableHilite = new DialogComponentBoolean(
            new SettingsModelBoolean(ClusterNodeModel.CFG_ENABLE_HILITE, false),
            "Enable Hilite Mapping");

        addDialogComponent(nrOfClusters);
        addDialogComponent(maxNrOfIterations);
        addDialogComponent(columnFilter);
        addDialogComponent(enableHilite);
        setDefaultTabTitle("K-Means Properties");
    }
}
