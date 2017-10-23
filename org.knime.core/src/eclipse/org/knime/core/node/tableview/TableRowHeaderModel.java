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
package org.knime.core.node.tableview;

import javax.swing.JComponent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.knime.core.data.DataCell;
import org.knime.core.data.RowKey;
import org.knime.core.data.property.ColorAttr;


/**
 * Model for a Row Header view in a table view that displays a
 * {@link org.knime.core.data.DataTable}. This model has exactly one column
 * that contains the keys (type {@link org.knime.core.data.DataCell}) of
 * the {@link org.knime.core.data.DataRow}s in the
 * underlying {@link org.knime.core.data.DataTable}. The view to this model
 * is a {@link org.knime.core.node.tableview.TableRowHeaderView} which can
 * be located, for instance, in a scroll pane's row header view.
 *
 * <p>An instance of this class always corresponds to an instance of
 * <code>TableContentModel</code>.
 * @see org.knime.core.node.tableview.TableContentModel
 *
 * @author Bernd Wiswedel, University of Konstanz
 */
public class TableRowHeaderModel extends AbstractTableModel {
    private static final long serialVersionUID = -2200601319386867806L;

    /* This model is detached from a TableContentModel (where it nevertheless
     * has a pointer to) because it has a different column count. It was not an
     * option to implement a smart TableRowHeaderView based on a
     * TableContentModel because all get functions in view and model would
     * differ then.
     */

    /** Reference to underlying TableContentModel, never <code>null</code>. */
    private TableContentInterface m_contentInterface;

    /** Listener on {@link #m_contentInterface}. */
    private TableModelListener m_contentListener;

    /**
     * In some, very rare cases we need to set the column name - which,
     * by default is just "Row ID".
     */
    private String m_columnName = "Row ID";


    /**
     * Instantiates a new <code>TableRowHeaderModel</code> based on a
     * {@link TableModel}. In case the argument is instance of
     * {@link TableContentInterface}, this object will delegate
     * to it and retrieve information (row key, hilite status, etc) from it.
     * In case it's an ordinary table model, an adapter is created that always
     * returns default row keys (named "default" and none of the rows will be
     * hilited). This constructor is used in the method
     * {@link TableRowHeaderView#createHeaderView(javax.swing.JTable)} and
     * shouldn't be called from anywhere else.
     *
     * @param content the model for the content to this row header
     * @throws NullPointerException if argument is <code>null</code>
     */
    TableRowHeaderModel(final TableModel content) {
        if (content == null) {
            throw new NullPointerException("Content model must not be null!");
        }
        m_contentListener = new TableModelListener() {
            /**
             * Catches events from content model, and passes it to the
             * listeners (event's source is changed, however). UPDATE events
             * are ignored
             */
            @Override
            public void tableChanged(final TableModelEvent e) {
                final int col = e.getColumn();
                if (col != TableModelEvent.ALL_COLUMNS) {
                    return; // don't care about those events.
                }
                // rows have been inserted (most likely) or deleted
                final int firstRow = e.getFirstRow();
                final int lastRow = e.getLastRow();
                final int type = e.getType();
                final TableModelEvent newEvent = new TableModelEvent(
                    TableRowHeaderModel.this, firstRow, lastRow, col, type);
                fireTableChanged(newEvent);
            } // tableChanged(TableModelEvent)

        };
        setTableContent(content);
    } // TableRowHeaderModel(TableContentModel)

    /**
     * Returns 1. A row header model only has one column ... the key.
     * @return 1
     */
    @Override
    public int getColumnCount() {
        return 1;
    } // getColumnCount()

    /**
     * Returns row count as in {@link TableContentModel}.
     *
     * @return <code>getContentModel().getRowCount()</code>
     * @see TableContentModel#getRowCount()
     */
    @Override
    public int getRowCount() {
        return m_contentInterface.getRowCount();
    } // getRowCount()

    /**
     * Returns the key of the row with index <code>rowIndex</code>.
     *
     * @param rowIndex the row of interest
     * @param columnIndex must be 0
     * @return The key of the {@link org.knime.core.data.DataRow}
     *      (type {@link DataCell})
     * @throws IndexOutOfBoundsException if <code>columnIndex</code> is not 0 or
     *         <code>rowIndex</code> violates its range
     */
    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        boundColumn(columnIndex);
        // will throw IndexOutOfBoundsException if rowIndex is invalid
        return m_contentInterface.getRowKey(rowIndex);
    } // getValueAt(int, int)

    /**
     * Return <code>DataCell.class</code> since the key of a
     * {@link org.knime.core.data.DataRow} is a {@link DataCell}.
     *
     * @param columnIndex must be 0
     * @return <code>DataCell.class</code>
     */
    @Override
    public Class<DataCell> getColumnClass(final int columnIndex) {
        boundColumn(columnIndex);
        return DataCell.class;
    } // getColumnClass(int)

    /**
     * Returns "Key" as default row header name.
     *
     * @param column must be 0
     * @return "Key"
     * @throws IndexOutOfBoundsException if column is not 0
     */
    @Override
    public String getColumnName(final int column) {
        boundColumn(column);
        return m_columnName;
    } // getColumnName(int)

    /**
     * Sets a new name for this column.
     *
     * @param newName the new name or <code>null</code> to have no column name
     */
    public void setColumnName(final String newName) {
        if (newName != null && newName.equals(m_columnName)) {
            return;
        }
        m_columnName = newName;
        fireTableCellUpdated(TableModelEvent.HEADER_ROW, 0);
    }

    /** Request sort according to column header in associated content table.
     * @param modalityComp The visual component to block while the table is
     * sorted. */
    final void requestSort(final JComponent modalityComp) {
        if (m_contentInterface instanceof TableContentModel) {
            ((TableContentModel)m_contentInterface).requestSort(
                    -1, modalityComp);
        }
    }

    /**
     * Set the table content to which this class will listen and whose
     * content is content of this class.
     * <br>
     * Note: If the passed argument is not an instance of
     * {@link TableContentInterface}, this model will be a very dumb model:
     * It will not show any row keys and also no hilighting nor color
     * information.
     *
     * @param content the new model
     * @throws IllegalArgumentException if argument is <code>null</code>
     */
    public void setTableContent(final TableModel content) {
        if (content == null) {
            throw new IllegalArgumentException("Can't set null content.");
        }
        if (content == m_contentInterface) {
            return;
        }
        // previously passed an TableModel that has been wrapped?
        if (m_contentInterface instanceof TableContentWrapper
                && ((TableContentWrapper)m_contentInterface).m_model
                == content) {
            return;
        }

        // set new value
        if (m_contentInterface != null) {
            m_contentInterface.removeTableModelListener(m_contentListener);
        }
        if (content instanceof TableContentInterface) {
            m_contentInterface = (TableContentInterface)content;
        } else {
            m_contentInterface = new TableContentWrapper(content);
        }
        m_contentInterface.addTableModelListener(m_contentListener);
        fireTableDataChanged();
    }

    /** @return the contentInterface that was set via
     * {@link #setTableContent(TableModel)}. */
    public final TableContentInterface getTableContent() {
        return m_contentInterface;
    }

    /** Return <code>false</code> if the underlying table is an instance
     * of {@link TableContentModel} and its row count is not final (indicating
     * that the table has not been traversed to the very end). In all other
     * cases return <code>true</code>.
     * @return Whether there are (not) more rows to see.
     */
    boolean isRowCountFinal() {
        if (!(m_contentInterface instanceof TableContentModel)) {
            return true;
        }
        return ((TableContentModel)m_contentInterface).isRowCountFinal();
    }


    /**
     * Delegating method to {@link TableContentModel}.
     *
     * @param row row index of interest
     * @return Hilite status of <code>row</code>.
     * @see TableContentModel#isHiLit(int)
     */
    public boolean isHiLit(final int row) {
        return m_contentInterface.isHiLit(row);
    } // isHiLit(int)

    /**
     * Delegating method to {@link TableContentModel}.
     *
     * @param row row index of interest
     * @return color information to that row.
     * @see TableContentModel#getColorAttr(int)
     */
    public ColorAttr getColorAttr(final int row) {
        return m_contentInterface.getColorAttr(row);
    } // getColorAttr(int)

    /**
     * Checks if column index is 0. If not, throws an exception.
     *
     * @param columnIndex must be 0
     * @throws IndexOutOfBoundsException if index is not 0
     */
    private void boundColumn(final int columnIndex) {
        if (columnIndex != 0) {
            throw new IndexOutOfBoundsException("Column index for row header"
                + "must be 0, not " + columnIndex);
        }
    } // boundColumn(int)

    /** Utitlity class that implements TableContentInterface but returns
     * default values for calls such as getRowKey and isHilit. Used when
     * the table content model is  not implementing TableContentInterface.
     */
    private static class TableContentWrapper
        implements TableContentInterface {
        private static final RowKey UNKNOWN = new RowKey("unknown");
        private final TableModel m_model;

        /**
         * Creates wrapper based on the model.
         *
         * @param model the model to wrap
         */
        TableContentWrapper(final TableModel model) {
            m_model = model;
        }

        /**
         * Delegates to model.
         *
         * @see TableContentInterface#getRowCount()
         */
        @Override
        public int getRowCount() {
            return m_model.getRowCount();
        }

        /**
         * Returns "unknown".
         *
         * @see TableContentInterface#getRowKey(int)
         */
        @Override
        public RowKey getRowKey(final int row) {
            return UNKNOWN;
        }

        /**
         * Returns a default color.
         *
         * @see TableContentInterface#getColorAttr(int)
         */
        @Override
        public ColorAttr getColorAttr(final int row) {
            return ColorAttr.DEFAULT;
        }

        /**
         * Returns false.
         *
         * @see TableContentInterface#isHiLit(int)
         */
        @Override
        public boolean isHiLit(final int row) {
            return false;
        }

        /**
         * Delegates to model.
         *
         * @see TableContentInterface#addTableModelListener(TableModelListener)
         */
        @Override
        public void addTableModelListener(final TableModelListener l) {
            m_model.addTableModelListener(l);
        }

        /**
         * Delegates to model.
         *
         * @see TableContentInterface#removeTableModelListener(
         * TableModelListener)
         */
        @Override
        public void removeTableModelListener(final TableModelListener l) {
            m_model.removeTableModelListener(l);
        }
    }
}
