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
 *   Jun 1, 2009 (wiswedel): created
 */
package org.knime.core.node.workflow;

import java.awt.BorderLayout;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.knime.core.node.util.FlowVariableTableCellRenderer;
import org.knime.core.node.workflow.FlowVariable.Type;
import org.knime.core.util.Pair;

/**
 * View that displays a given {@link FlowObjectStack} in a table.
 * This view is shown as a separate tab in each node's outport views.
 * @author Bernd Wiswedel, University of Konstanz
 */
final class FlowObjectStackView extends JPanel {

    private static final String[] COLUMN_NAMES = new String[]{"Index", "Owner ID", "Name", "Value"};

    /** Table displaying name, value and owner of a {@link FlowVariable}. */
    private final JTable m_table;

    /** Creates new empty view. */
    public FlowObjectStackView() {
        super(new BorderLayout());
        m_table = new JTable(new DefaultTableModel() {
            /** {@inheritDoc} */
            @Override
            public Class<?> getColumnClass(final int columnIndex) {
                switch (columnIndex) {
                case 0: return Integer.class;
                case 1: return NodeID.class;
                case 2: return FlowVariable.class;
                default: return super.getColumnClass(columnIndex);
                }
            }
            /** {@inheritDoc} */
            @Override
            public boolean isCellEditable(final int row, final int column) {
                return false;
            }
        });
        m_table.setAutoCreateRowSorter(true);
        m_table.setDefaultRenderer(FlowVariable.class, new FlowVariableTableCellRenderer());
        add(new JScrollPane(m_table), BorderLayout.CENTER);
    }

    /** Updates the view to display the given stack.
     * @param stack Whose values are to be displayed. */
    public void update(final FlowObjectStack stack) {
        final FontMetrics fontMetrics = m_table.getFontMetrics(m_table.getFont());
        List<Object[]> values;
        // relative width of each column - make important columns wider
        int[] colRelativeWidth = new int[4];
        for (int i = 0; i < colRelativeWidth.length; i++) {
            colRelativeWidth[i] = SwingUtilities.computeStringWidth(fontMetrics, COLUMN_NAMES[i]);
        }
        if (stack != null) {
            values = new ArrayList<Object[]>();
            int loopCount = 0;
            int counter = 0;
            Set<Pair<String, Type>> duplicateElementsSet = new HashSet<Pair<String, Type>>();
            for (FlowObject s : stack) {
                Object[] obj = new Object[4];
                obj[0] = Integer.valueOf(counter);
                obj[1] = s.getOwner();
                if (s instanceof FlowVariable) {
                    FlowVariable v = (FlowVariable)s;
                    final Pair<String, Type> key = new Pair<String, Type>(v.getName(), v.getType());
                    if (!duplicateElementsSet.add(key)) {
                        continue;
                    }
                    obj[2] = s;
                    Object o;
                    switch (v.getType()) {
                    case DOUBLE:
                        o = Double.valueOf(v.getDoubleValue());
                        break;
                    case INTEGER:
                        o = Integer.valueOf(v.getIntValue());
                        break;
                    case STRING:
                        o = v.getStringValue();
                        break;
                    case CREDENTIALS:
                        o = v.getCredentialsValue();
                        break;
                    default:
                        o = "Unknown Type: " + v.getType();
                    }
                    obj[3] = o;
                } else if (s instanceof FlowLoopContext) {
                    duplicateElementsSet.clear();
                    if (!((FlowLoopContext)s).isInactiveScope()) {
                        obj[2] = "Loop (" + (loopCount++) + ")";
                        obj[3] = null;
                    } else {
                        obj[2] = "Inactive Loop Mark";
                        obj[3] = null;
                    }
                } else if (s instanceof InnerFlowLoopContext) {
                    obj[2] = "Loop-Execute";
                    obj[3] = null;
                } else if (s instanceof FlowScopeContext) {
                    obj[2] = s.toString()  + (((FlowScopeContext)s).isInactiveScope() ? " (inactive)" : "");
                    obj[3] = null;
                } else {
                    obj[2] = "unknown:" + s.toString();
                    obj[3] = null;
                }
                values.add(obj);
                for (int i = 0; i < colRelativeWidth.length; i++) {
                    String representation;
                    if (obj[i] instanceof FlowVariable) {
                        representation = "ICON" + ((FlowVariable)obj[i]).getName();
                    } else {
                        representation = Objects.toString(obj[i], "");
                    }
                    colRelativeWidth[i] = Math.max(colRelativeWidth[i],
                        SwingUtilities.computeStringWidth(fontMetrics, representation));
                }
            }
        } else {
            values = Collections.emptyList();
        }
        final DefaultTableModel model = (DefaultTableModel)m_table.getModel();
        model.setDataVector(values.toArray(new Object[values.size()][]), COLUMN_NAMES);
        final TableColumnModel columnModel = m_table.getColumnModel();
        for (int i = 0; i < colRelativeWidth.length; i++) {
            columnModel.getColumn(i).setPreferredWidth(colRelativeWidth[i]);
        }
        m_table.setCellSelectionEnabled(true);
    }

}
