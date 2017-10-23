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
 *   16.12.2009 (meinl): created
 */
package org.knime.base.node.meta.looper.columnlist2;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterConfiguration;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterPanel;

/**
 * This is the dialog for the column list loop start node where the user can
 * select the column over which the loop should iterate.
 *
 * @author Thorsten Meinl, University of Konstanz
 */
public class ColumnListLoopStartNodeDialog extends NodeDialogPane {

    private final DataColumnSpecFilterPanel m_filterPanel =
            new DataColumnSpecFilterPanel();

    private final JRadioButton m_noColumnsPolicyFailButton =
            new JRadioButton("Fail");

    private final JRadioButton m_noColumnsPolicyOneInterationButton =
            new JRadioButton("Run one iteration");

    /**
     * Creates a new dialog.
     */
    public ColumnListLoopStartNodeDialog() {
        JPanel p = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        p.add(m_filterPanel, c);

        JPanel noColumnsPolicyPanel = new JPanel(new GridLayout(0, 1));
        noColumnsPolicyPanel.setBorder(
                BorderFactory.createTitledBorder("If include column list is empty:"));
        ButtonGroup bg = new ButtonGroup();
        bg.add(m_noColumnsPolicyOneInterationButton);
        //Default is to run one iteration.
        m_noColumnsPolicyOneInterationButton.doClick();
        bg.add(m_noColumnsPolicyFailButton);
        noColumnsPolicyPanel.add(m_noColumnsPolicyOneInterationButton);
        noColumnsPolicyPanel.add(m_noColumnsPolicyFailButton);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 0;
        p.add(noColumnsPolicyPanel, c);

        addTab("Standard settings", p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
        DataColumnSpecFilterConfiguration conf = ColumnListLoopStartNodeModel.createDCSFilterConfiguration();
        m_filterPanel.saveConfiguration(conf);
        conf.saveConfiguration(settings);

        SettingsModelBoolean noColumnsSettings = ColumnListLoopStartNodeModel.createNoColumnsPolicySetings();
        noColumnsSettings.setBooleanValue(m_noColumnsPolicyOneInterationButton.isSelected());
        noColumnsSettings.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings,
            final DataTableSpec[] specs) throws NotConfigurableException {
        DataColumnSpecFilterConfiguration conf = ColumnListLoopStartNodeModel.createDCSFilterConfiguration();
        conf.loadConfigurationInDialog(settings, specs[0]);
        m_filterPanel.loadConfiguration(conf, specs[0]);

        //true means to run one iteration and false that the node should fail
        boolean runOneIter = settings.getBoolean(ColumnListLoopStartNodeModel.CFG_NO_COLUMNS_POLICY, true);
        if(runOneIter){
            m_noColumnsPolicyOneInterationButton.doClick();
        } else {
            m_noColumnsPolicyFailButton.doClick();
        }
    }
}
