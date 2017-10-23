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
 * 2006-06-08 (tm): reviewed
 */
package org.knime.core.node.tableview;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.plaf.LabelUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.renderer.MultiLineBasicLabelUI;
import org.knime.core.node.tableview.TableSortOrder.TableSortKey;
import org.knime.core.node.util.ViewUtils;


/**
 * Renderer to be used to display the column header of a table. It will show
 * an icon on the left and the name of the column on the right. The icon is
 * given by the type's <code>getIcon()</code> method. If the column is sorted,
 * the icon will be compound icon consisting of the type icon and and
 * arrow icon indicating the sort order.
 *
 * @see org.knime.core.data.DataType#getIcon()
 * @author Bernd Wiswedel, University of Konstanz
 */
public class ColumnHeaderRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = -2356486759304444805L;

    private boolean m_showIcon = true;
    private boolean m_wrapHeader = false;

    private static final Icon ICON_PRIM_DES = ViewUtils.loadIcon(
            ColumnHeaderRenderer.class,
            "icon/table_sort_descending_primary.png");

    private static final Icon ICON_PRIM_ASC = ViewUtils.loadIcon(
            ColumnHeaderRenderer.class,
            "icon/table_sort_ascending_primary.png");

    private static final Icon ICON_SEC_DES = ViewUtils.loadIcon(
            ColumnHeaderRenderer.class,
            "icon/table_sort_descending_secondary.png");

    private static final Icon ICON_SEC_ASC = ViewUtils.loadIcon(
            ColumnHeaderRenderer.class,
            "icon/table_sort_ascending_secondary.png");

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUI() {
        super.updateUI();
        MultiLineBasicLabelUI ui2 = new MultiLineBasicLabelUI();
        ui2.setWrapLongLines(isWrapHeader());
        setUI(ui2);
    }

    /** Whether column names are wrapped, default is false.
     * @param value to set
     * @since 2.8
     */
    public void setWrapHeader(final boolean value) {
        if (value != m_wrapHeader) {
            m_wrapHeader = value;
            updateUI();
        }
    }

    /** see {@link #setWrapHeader(boolean)}.
     * @return the wrapHeader
     * @since 2.8
     */
    public boolean isWrapHeader() {
        return m_wrapHeader;
    }

    /**
     * @return the showIcon
     */
    public boolean isShowIcon() {
        return m_showIcon;
    }

    /**
     * @param showIcon the showIcon to set
     */
    public void setShowIcon(final boolean showIcon) {
        m_showIcon = showIcon;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getTableCellRendererComponent(final JTable table,
            final Object value, final boolean isSelected,
            final boolean hasFocus, final int row, final int column) {
        // set look and feel of a header
        if (table != null) {
            JTableHeader header = table.getTableHeader();
            if (header != null) {
                setForeground(header.getForeground());
                setBackground(header.getBackground());
                setFont(header.getFont());
            }
            if (table.isColumnSelected(column)) {
                Color bg = table.getSelectionBackground();
                setBackground(bg);
                setOpaque(true);
            } else {
                setOpaque(false);
            }
        }
        setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        Icon typeIcon = null;
        Icon sortIcon = null;
        Object newValue = value;
        if (value instanceof DataColumnSpec) {
            DataColumnSpec colSpec = (DataColumnSpec)value;
            newValue = colSpec.getName();
            if (isShowIcon()) {
                typeIcon = colSpec.getType().getIcon();
            }
        }
        sortIcon = getSortIcon(table, column);
        if (typeIcon == null && sortIcon == null) {
            setIcon(null);
        } else {
            setIcon(new CompoundIcon(typeIcon, sortIcon));
        }
        setToolTipText(newValue != null ? newValue.toString() : null);
        setValue(newValue);
        return this;
    }

    /** See {@link MultiLineBasicLabelUI#getPreferredTextWidth(javax.swing.JComponent)}.
     * @return This size or -1 if unknown.
     */
    int getPreferredTextWidth() {
        LabelUI ui2 = getUI();
        if (ui2 instanceof MultiLineBasicLabelUI) {
            return ((MultiLineBasicLabelUI)ui2).getPreferredTextWidth(this);
        }
        return -1;
    }

    /**
     * @param table
     * @param column
     * @param sortIcon
     * @return */
    private Icon getSortIcon(final JTable table, final int column) {
        if (table == null) {
            return null;
        }
        TableModel model = table.getModel();
        int colIndexInModel = -1;
        TableSortOrder sortOrder = null;
        if (model instanceof TableContentModel) {
            TableContentModel cntModel = (TableContentModel)model;
            sortOrder = cntModel.getTableSortOrder();
            colIndexInModel = table.convertColumnIndexToModel(column);
        } else if (model instanceof TableRowHeaderModel) {
            TableRowHeaderModel rowHeaderModel = (TableRowHeaderModel)model;
            TableContentInterface cntIface = rowHeaderModel.getTableContent();
            if (cntIface instanceof TableContentModel) {
                TableContentModel cntModel = (TableContentModel)cntIface;
                sortOrder = cntModel.getTableSortOrder();
                colIndexInModel = -1;
            }
        }
        TableSortKey sortKey;
        if (sortOrder == null) {
            sortKey = TableSortKey.NONE;
        } else {
            sortKey = sortOrder.getSortKeyForColumn(colIndexInModel);
        }
        switch (sortKey) {
        case PRIMARY_ASCENDING:
            return ICON_PRIM_ASC;
        case SECONDARY_ASCENDING:
            return ICON_SEC_ASC;
        case PRIMARY_DESCENDING:
            return ICON_PRIM_DES;
        case SECONDARY_DESCENDING:
            return ICON_SEC_DES;
        default:
            return null;
        }
    }

    /** Merges two icons (type icon & sort icon). */
    private static final class CompoundIcon implements Icon {

        private final Icon m_leftIcon;
        private final Icon m_rightIcon;

        /**
         * @param leftIcon
         * @param rightIcon */
        private CompoundIcon(final Icon leftIcon, final Icon rightIcon) {
            m_leftIcon = leftIcon;
            m_rightIcon = rightIcon;
        }

        /** {@inheritDoc} */
        @Override
        public void paintIcon(final Component c, final Graphics g,
                final int x, final int y) {
            if (m_leftIcon == null && m_rightIcon == null) {
                // nothing to paint
            } else if (m_leftIcon == null) {
                m_rightIcon.paintIcon(c, g, x, y);
            } else if (m_rightIcon == null) {
                m_leftIcon.paintIcon(c, g, x, y);
            } else {
                int leftHeight = m_leftIcon.getIconHeight();
                int rightHeight = m_rightIcon.getIconHeight();
                int maxHeight = Math.max(leftHeight, rightHeight);
                int leftWidth = m_leftIcon.getIconWidth();
                if (leftHeight == maxHeight) {
                    m_leftIcon.paintIcon(c, g, x, y);
                } else {
                    m_leftIcon.paintIcon(c, g, x,
                            y + (maxHeight - leftHeight) / 2);
                }
                if (rightHeight == maxHeight) {
                    m_rightIcon.paintIcon(c, g, x + leftWidth + 2, y);
                } else {
                    m_rightIcon.paintIcon(c, g, x + leftWidth + 2,
                            y + (maxHeight - rightHeight) / 2);
                }
            }
        }

        /** {@inheritDoc} */
        @Override
        public int getIconWidth() {
            if (m_leftIcon == null && m_rightIcon == null) {
                return 0;
            } else if (m_leftIcon == null) {
                return m_rightIcon.getIconWidth();
            } else if (m_rightIcon == null) {
                return m_leftIcon.getIconWidth();
            } else {
                return m_leftIcon.getIconWidth()
                + m_rightIcon.getIconWidth() + 2;
            }
        }

        /** {@inheritDoc} */
        @Override
        public int getIconHeight() {
            if (m_leftIcon == null && m_rightIcon == null) {
                return 0;
            } else if (m_leftIcon == null) {
                return m_rightIcon.getIconHeight();
            } else if (m_rightIcon == null) {
                return m_leftIcon.getIconHeight();
            } else {
                return Math.max(m_leftIcon.getIconHeight(),
                        m_rightIcon.getIconHeight());
            }
        }
    }

}
