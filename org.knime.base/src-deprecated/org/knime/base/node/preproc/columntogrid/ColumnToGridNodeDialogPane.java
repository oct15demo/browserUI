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
 *   Jul 24, 2010 (wiswedel): created
 */
package org.knime.base.node.preproc.columntogrid;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.NominalValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.ColumnFilterPanel;
import org.knime.core.node.util.ColumnSelectionComboxBox;

/**
 * Dialog to Column-to-Grid node. Shows spinner for grid count column and
 * column selection panel.
 *
 * @author Bernd Wiswedel, KNIME.com, Zurich, Switzerland
 */
final class ColumnToGridNodeDialogPane extends NodeDialogPane {

    private final JSpinner m_gridColSpinner;
    private final ColumnFilterPanel m_colFilterPanel;
    private final JCheckBox m_enableGroupingChecker;
    private final ColumnSelectionComboxBox m_groupColumnCombo;

    /** Sets up dialog. */
    @SuppressWarnings("unchecked")
    public ColumnToGridNodeDialogPane() {
        m_gridColSpinner = new JSpinner(new SpinnerNumberModel(
                4, 1, Integer.MAX_VALUE, 1));
        m_colFilterPanel = new ColumnFilterPanel(false);
        m_groupColumnCombo = new ColumnSelectionComboxBox(
                (Border)null, NominalValue.class);
        m_enableGroupingChecker = new JCheckBox("Enable Grouping");
        m_enableGroupingChecker.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                m_groupColumnCombo.setEnabled(
                        m_enableGroupingChecker.isSelected());
            }
        });
        m_enableGroupingChecker.doClick();
        JPanel panel = new JPanel(new BorderLayout());
        JPanel northPanel = new JPanel(new FlowLayout());
        northPanel.add(new JLabel("Grid Column Count: "));
        northPanel.add(m_gridColSpinner);
        panel.add(northPanel, BorderLayout.NORTH);

        panel.add(m_colFilterPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new FlowLayout());
        southPanel.add(m_enableGroupingChecker);
        southPanel.add(m_groupColumnCombo);
        panel.add(southPanel, BorderLayout.SOUTH);
        addTab("Grid Settigs", panel);
    }

    /** {@inheritDoc} */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
        ColumnToGridConfiguration config = new ColumnToGridConfiguration();
        config.setColCount((Integer)m_gridColSpinner.getValue());
        Set<String> inclSet = m_colFilterPanel.getIncludedColumnSet();
        String[] includes = inclSet.toArray(new String[inclSet.size()]);
        config.setIncludes(includes);
        if (m_enableGroupingChecker.isSelected()) {
            config.setGroupColumn(m_groupColumnCombo.getSelectedColumn());
        }
        config.saveSettingsTo(settings);
    }

    /** {@inheritDoc} */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings,
            final DataTableSpec[] specs) throws NotConfigurableException {
        ColumnToGridConfiguration config = new ColumnToGridConfiguration();
        config.loadSettings(settings, specs[0]);
        m_colFilterPanel.update(specs[0], false, config.getIncludes());
        m_gridColSpinner.setValue(config.getColCount());
        String groupCol = config.getGroupColumn();
        if (m_enableGroupingChecker.isSelected() != (groupCol != null)) {
            m_enableGroupingChecker.doClick();
        }
        try {
            m_groupColumnCombo.update(specs[0], groupCol);
        } catch (NotConfigurableException nce) {
            // ignore
        }
    }

}
