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
 *   Apr 13, 2006 (wiswedel): created
 */
package org.knime.base.node.mine.scorer.entrop;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnSelectionComboxBox;


/**
 * This class represents the dialog for the entropy scorer node.
 *
 * @author Bernd Wiswedel, University of Konstanz
 */
public class EntropyNodeDialogPane extends NodeDialogPane {
    private final ColumnSelectionComboxBox m_comboReference;

    private final ColumnSelectionComboxBox m_comboCluster;

    private final SettingsModelBoolean m_flowVarModel = new SettingsModelBoolean("generate flow variables", false);
    private final SettingsModelString m_useNamePrefixModel = createFlowPrefixModel(m_flowVarModel);
    private final DialogComponentBoolean m_flowVarComponent = new DialogComponentBoolean(m_flowVarModel, "Output scores as flow variables");
    private final DialogComponentString m_useNamePrefixComponent = new DialogComponentString(m_useNamePrefixModel, "Prefix of flow variables");

    private boolean m_flowVariableOption;
    
    /**
     * Creates a new dialog for the entropy scorer.
     */
    public EntropyNodeDialogPane() {
        this(false);
    }

    /**
     * The dialog for the entropy scorer.
     * @param flowVar whether the flow variable dialog options should be displayed or not
     * @since 3.2 Added support for flow variable output, by using new flag
     */
    @SuppressWarnings("unchecked")
    public EntropyNodeDialogPane(final boolean flowVar) {
        m_flowVariableOption = flowVar;
        m_comboReference = new ColumnSelectionComboxBox((Border)null,
                DataValue.class);
        m_comboCluster = new ColumnSelectionComboxBox((Border)null,
                DataValue.class);
        int h = m_comboCluster.getPreferredSize().height;
        m_comboCluster.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        m_comboReference.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        JPanel layout = new JPanel(new GridLayout(0, 2, 15, 15));
        JPanel clusterFlower = new JPanel(new FlowLayout());
        clusterFlower.add(m_comboCluster);
        JPanel referenceFlower = new JPanel(new FlowLayout());
        referenceFlower.add(m_comboReference);
        JPanel referenceFlowerLbl = new JPanel(new FlowLayout());
        referenceFlowerLbl.add(new JLabel("Reference Column: ",
                SwingConstants.RIGHT));
        JPanel clusterFlowerLbl = new JPanel(new FlowLayout());
        clusterFlowerLbl.add(new JLabel("Clustering Column: ",
                SwingConstants.RIGHT));
        layout.add(referenceFlowerLbl);
        layout.add(referenceFlower);
        layout.add(clusterFlowerLbl);
        layout.add(clusterFlower);

        if(m_flowVariableOption) {
            layout.add(m_flowVarComponent.getComponentPanel());
            layout.add(m_useNamePrefixComponent.getComponentPanel());
        }
        addTab("Default", layout);
    }


    /**
     * @param useNamePrefixModel TODO
     * @return A new {@link SettingsModelString} for the flow variable prefix
     * @since 3.2 Creates the settings model for the flow variable prefix
     */
    public static SettingsModelString createFlowPrefixModel(final SettingsModelBoolean useNamePrefixModel) {
        final SettingsModelString result = new SettingsModelString("name prefix for flowvars", "");
        useNamePrefixModel.addChangeListener(new ChangeListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void stateChanged(final ChangeEvent e) {
                result.setEnabled(useNamePrefixModel.getBooleanValue());
            }
        });
        result.setEnabled(useNamePrefixModel.getBooleanValue());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings,
            final DataTableSpec[] specs) throws NotConfigurableException {
        // determine the default values
        DataTableSpec reference = specs[EntropyNodeModel.INPORT_REFERENCE];
        DataTableSpec clustering = specs[EntropyNodeModel.INPORT_CLUSTERING];
        String defaultClustering = suggestColumn(clustering, false);
        // in case that someone connected the same spec to both inports,
        // use the last but one string column as reference. Otherwise, take
        // the last column
        String defaultReference = suggestColumn(reference,
                reference == clustering);

        String clusterSelected = settings.getString(
                EntropyNodeModel.CFG_CLUSTERING_COLUMN, defaultClustering);
        String referenceSelected = settings.getString(
                EntropyNodeModel.CFG_REFERENCE_COLUMN, defaultReference);
        m_comboCluster.update(clustering, clusterSelected);
        m_comboReference.update(reference, referenceSelected);
        if (m_flowVariableOption) {
            m_flowVarComponent.loadSettingsFrom(settings, specs);
            m_useNamePrefixComponent.loadSettingsFrom(settings, specs);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
        String reference = m_comboReference.getSelectedColumn();
        String clustering = m_comboCluster.getSelectedColumn();
        settings.addString(EntropyNodeModel.CFG_CLUSTERING_COLUMN, clustering);
        settings.addString(EntropyNodeModel.CFG_REFERENCE_COLUMN, reference);
        if (m_flowVariableOption) {
            m_flowVarModel.saveSettingsTo(settings);
            m_useNamePrefixModel.saveSettingsTo(settings);
        }
    }

    private static String suggestColumn(final DataTableSpec spec,
            final boolean takeLastButOne) {
        boolean takeNext = !takeLastButOne;
        int colCount = spec.getNumColumns();
        // traverse backwards
        for (int i = colCount - 1; i >= 0; i--) {
            DataColumnSpec colSpec = spec.getColumnSpec(i);
            if (colSpec.getType().isCompatible(StringValue.class)) {
                if (takeNext) {
                    return colSpec.getName();
                } else {
                    takeNext = true; // last one was rejected, take previous
                }
            }
        }
        // fallback - simply take the last column
        return colCount > 0 ? spec.getColumnSpec(colCount - 1).getName() : null;
    }
}
