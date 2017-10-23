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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.knime.core.data.property.ColorAttr;
import org.knime.core.data.renderer.DataValueRenderer;
import org.knime.core.node.tableview.TableSortOrder.TableSortKey;


/**
 * Row Header for a table view on a {@link org.knime.core.data.DataTable}.
 * It displays a {@link org.knime.core.data.DataRow}'s key (type
 * {@link org.knime.core.data.DataCell})using the
 * <code>toString()</code> method. Thus, this table has exactly one column.
 * The model for this kind of view is a
 * {@link org.knime.core.node.tableview.TableRowHeaderModel}.
 * <br>
 * This class is used in conjunction with a {@link TableContentView} as
 * the row header view in a scroll pane (realized by {@link TableView}).
 *
 * @see org.knime.core.node.tableview.TableContentView
 * @author Bernd Wiswedel, University of Konstanz
 */
public final class TableRowHeaderView extends JTable {
    private static final long serialVersionUID = 4115412802300446736L;
    private Color m_headerBackground;
    private boolean m_isShowColorInfo;

    /**
     * Instantiates new view based on a <code>TableRowHeaderModel</code>.
     *
     * @param dm the model to be displayed
     */
    private TableRowHeaderView(final TableRowHeaderModel dm) {
        super(dm);
        getTableHeader().setReorderingAllowed(false);
        setShowColorInfo(true);
        new RowHeaderHeightMouseListener(this);
    } // TableRowHeaderView

    /**
     * Makes sure to register a property handler and sets the correct
     * cell renderer.
     * {@inheritDoc}
     */
    @Override
    public void addColumn(final TableColumn aColumn) {
        super.addColumn(aColumn);
        aColumn.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("width")) {
                    int newWidth = (Integer)evt.getNewValue();
                    // bug fix #293: on execute & open view the row header
                    // column is collapsed to a minimum (hardcoded to 15 in
                    // TableColumn.java). We set a slightly smaller value here
                    // and catch requests to set it to this minimum.
                    if (newWidth == aColumn.getMinWidth()) {
                        return;
                    }
                    Dimension newSize = new Dimension(newWidth, 0);
                    setPreferredScrollableViewportSize(newSize);
                }
            }
        });
        // set renderer for table (similar to column header renderer)
        // do not call the method setRenderer because it might be overridden
        DataCellHeaderRenderer renderer = DataCellHeaderRenderer.newInstance();
        renderer.showIcon(m_isShowColorInfo);
        aColumn.setCellRenderer(renderer);
        assert (getColumnCount() == 1);
    }

    /**
     * Sets a new model for this view The argument
     * must be instance of {@link TableRowHeaderModel}.
     *
     * @param tableModel the new model
     * @throws NullPointerException if argument is <code>null</code>.
     * @throws ClassCastException if <code>dataModel</code> is not of type
     *         {@link TableRowHeaderModel}.
     * @see javax.swing.JTable#setModel(javax.swing.table.TableModel)
     */
    @Override
    public void setModel(final TableModel tableModel) {
        if (tableModel == null) {
            throw new NullPointerException("Model must not be null.");
        }
        if (!(tableModel instanceof TableRowHeaderModel)) {
            throw new ClassCastException("Not a TableRowHeaderModel");
        }
        super.setModel(tableModel);
    }

    /** {@inheritDoc} */
    @Override
    public TableRowHeaderModel getModel() {
        return (TableRowHeaderModel)super.getModel();
    }

    /**
     * Resets all individual row heights as the super implementation
     * deletes the private SizeSequence field.
     * {@inheritDoc}
     */
    @Override
    public void tableChanged(final TableModelEvent e) {
        // for all the cases in the following if-statement the super
        // implementation will delete the private field "rowModel", causing
        // the individual row heights to be forgotten
        if (e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW
                || e.getLastRow() == Integer.MAX_VALUE) {
            // row height is less than 1 when view has not been opened yet
            int oldRowHeight = getRowHeight();
            if (oldRowHeight >= 1) {
                // this will reset all individual row heights, all rows
                // will have the same height.
                setRowHeight(oldRowHeight);
            }
        }
        super.tableChanged(e);
    }

    /**
     * Delegating method to model. It sets the column header name which is,
     * by default, "Key".
     *
     * @param newName the new name of the column or <code>null</code> to leave
     * empty
     */
    public void setColumnName(final String newName) {
        getModel().setColumnName(newName);
    }

    /**
     * Overridden in order to set the correct selection color (depending on
     * hilite status).
     * {@inheritDoc}
     */
    @Override
    public Component prepareRenderer(
        final TableCellRenderer renderer, final int row, final int column) {
        if (renderer instanceof DataCellHeaderRenderer) {
            final DataCellHeaderRenderer r = (DataCellHeaderRenderer)renderer;
            final TableRowHeaderModel model = getModel();

            // set proper selection color
            boolean isHiLit = model.isHiLit(row);
            Color selColor = (isHiLit
                    ? ColorAttr.SELECTED_HILITE : ColorAttr.SELECTED);
            setSelectionBackground(selColor);
            // this might be ignored anyway if the row is selected ...
            if (isHiLit) {
                r.setBackground(ColorAttr.HILITE);
            } else {
                r.setBackground(m_headerBackground);
            }
            ColorAttr colorAttr = model.getColorAttr(row);
            r.setColor(colorAttr.getColor());
        }
        return super.prepareRenderer(renderer, row, column);
    } // prepareRenderer(TableCellRenderer, int, int)


    private float[] m_tempHSBColor;

    /**
     * Overridden to avoid event storm. The super implementation will invoke
     * a repaint if the color has changed. Since that happens frequently (and
     * also within the repaint) this causes an infinite loop.
     * {@inheritDoc}
     */
    @Override
    public void setSelectionBackground(final Color back) {
        if (back == null) {
            throw new NullPointerException("Color must not be null!");
        }
        Color fore;
        if (m_tempHSBColor == null) {
            m_tempHSBColor = new float[3];
        }
        float[] hsb = m_tempHSBColor;
        Color.RGBtoHSB(back.getRed(), back.getGreen(), back.getBlue(),
                       hsb);
        if (hsb[2] > 0.5f) {
            fore = Color.BLACK;
        } else {
            fore = Color.WHITE;
        }
        super.selectionForeground = fore;
        super.selectionBackground = back;
    }

    /**
     * Overridden so that we can attach a mouse listener to it and set
     * the proper renderer. The mouse listener is used to display a popup menu.
     * {@inheritDoc}
     */
    @Override
    public void setTableHeader(final JTableHeader newTableHeader) {
        if (newTableHeader != null) {
            ColumnHeaderRenderer renderer = new ColumnHeaderRenderer();
            renderer.setHorizontalAlignment(SwingConstants.CENTER);
            newTableHeader.setDefaultRenderer(renderer);
            renderer.setShowIcon(false);
            newTableHeader.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    onMouseClickInHeader(e);
                }

            });
        }
        super.setTableHeader(newTableHeader);
    }

    /** {@inheritDoc} */
    @Override
    public void setFont(final Font font) {
        super.setFont(font);
        JTableHeader th = getTableHeader();
        if (th != null) {
            th.setFont(font);
        }
    }

    private void onMouseClickInHeader(final MouseEvent e) {
        TableRowHeaderModel rowHeaderModel = getModel();
        TableContentInterface cntIface = rowHeaderModel.getTableContent();
        boolean isSortingAllowed = false;
        TableContentModel cntModel = null;
        if (cntIface instanceof TableContentModel) {
            cntModel = (TableContentModel)cntIface;
            isSortingAllowed = cntModel.isSortingAllowed();
        }
        if (e.isControlDown() && isSortingAllowed) {
            getModel().requestSort(this.getRootPane());
        } else if (SwingUtilities.isLeftMouseButton(e) && isSortingAllowed) { // left click in header.
            TableSortOrder sortOrder = null;
            sortOrder = cntModel.getTableSortOrder(); // can't be null here
            TableSortKey sortKey;
            if (sortOrder == null) {
                sortKey = TableSortKey.NONE;
            } else {
                sortKey = sortOrder.getSortKeyForColumn(TableSortOrder.COLIDX_ROWKEY);
            }
            final JPopupMenu popup = TableContentView.createSortPopupMenu(
                cntModel, this, TableSortOrder.COLIDX_ROWKEY, sortKey);
            popup.show(getTableHeader(), e.getX(), e.getY());
        }

    }
    /**
     * Shall row header encode the color information in an icon?
     *
     * @param isShowColor <code>true</code> for show icon (and thus the color),
     *        <code>false</code> ignore colors
     */
    public void setShowColorInfo(final boolean isShowColor) {
        boolean oldValue = isShowColorInfo();
        TableCellRenderer renderer =
            getColumnModel().getColumn(0).getCellRenderer();
        if (renderer instanceof DataCellHeaderRenderer) {
            ((DataCellHeaderRenderer)renderer).showIcon(isShowColor);
        }
        boolean newValue = isShowColorInfo();
        m_isShowColorInfo = newValue;
        if (oldValue != newValue) {
            repaint();
        }
    } // setShowColorInfo(boolean)

    /**
     * Is the color info shown?
     *
     * @return <code>true</code> if icon with the color is present,
     *      <code>false</code> otherwise
     */
    public boolean isShowColorInfo() {
        TableCellRenderer renderer = getColumnModel().getColumn(0).getCellRenderer();
        if (renderer instanceof DataCellHeaderRenderer) {
            return ((DataCellHeaderRenderer)renderer).isShowIcon();
        }
        return false;
    } // isShowColorInfo()

    /** Iterates all rows, determines best width and sets it. This method is expensive (as it iterates the rows).
     * @since 2.8
     */
    public void sizeWidthToFit() {
        int bestFit = -1;
        int rowCount = getRowCount();
        for (int i = 0; i < rowCount; i++) {
            TableCellRenderer cellRenderer = getCellRenderer(i, 0);
            Component rendererComponent = prepareRenderer(cellRenderer, i, 0);
            bestFit = Math.max(bestFit, rendererComponent.getPreferredSize().width);
        }
        if (bestFit > 0) {
            // no idea why there are ~2 pixel missing. String is clipped if omitted.
            TableColumn column = getColumnModel().getColumn(0);
            column.setPreferredWidth(bestFit + 2);
            column.setWidth(bestFit + 2);
        }
    }

    /**
     * Overridden in order to fire an event that an individual row has
     * changed.
     *
     * {@inheritDoc}
     */
    @Override
    public void setRowHeight(final int row, final int myRowHeight) {
        super.setRowHeight(row, myRowHeight);
        firePropertyChange("individualrowheight", -1, row);
    }

    /**
     * Overridden in order to fire an event. The super class will also fire
     * a property change event with property name "rowHeight". This event,
     * however, is not propagated when the row height doesn't change. The
     * super class will therefore not handle the case where there is at least
     * one individual row height and the majority row height (as set by
     * setRowHeight) stays the same. It will swallow the event.
     *
     * {@inheritDoc}
     */
    @Override
    public void setRowHeight(final int myRowHeight) {
        super.setRowHeight(myRowHeight);
        // values must differ
        firePropertyChange("allrowheight", -1, myRowHeight);
    }

    /**
     * Changes look and feel here (by calling {@link JTable#updateUI()})
     * and also in the renderer.
     * {@inheritDoc}
     */
    @Override
    public void updateUI() {
        super.updateUI();
        m_headerBackground = UIManager.getColor("TableHeader.background");
        setBackground(m_headerBackground);
        Border b = UIManager.getBorder("TableHeader.border");
        TableCellRenderer renderer =
            getColumnModel().getColumn(0).getCellRenderer();
        if (renderer instanceof DataCellHeaderRenderer) {
            ((DataCellHeaderRenderer)renderer).setBorder(b);
        }
    } // updateUI()

    /**
     * Set a new renderer for our column.
     *
     * @param renderer the new renderer
     */
    public void setRenderer(final DataValueRenderer renderer) {
        getColumnModel().getColumn(0).setCellRenderer(renderer);
    }

    /**
     * Factory method that creates a row header view for a given table.
     * The argument's model must be an instance of
     * {@link TableContentInterface} in order to connect the returned
     * view to argument view. This method will also bind both selection
     * models together (in particular, the returned view will use the
     * argument's view) and also both tables row height.
     *
     * @param table the table to which to connect to
     * @return a new header view connected to <code>table</code>
     * @throws NullPointerException if the argument is <code>null</code>.
     * @throws IllegalArgumentException if the table's model is not instance
     *          of {@link TableContentInterface}.
     */
    public static TableRowHeaderView createHeaderView(final JTable table) {
        TableModel model = table.getModel();
        final TableRowHeaderModel myModel = new TableRowHeaderModel(model);
        final TableRowHeaderView headerView = new TableRowHeaderView(myModel);
        // concatenate the selection models
        headerView.setSelectionModel(table.getSelectionModel());
        // make sure row heights are equal
        headerView.setRowHeight(table.getRowHeight());

        // add listener to the content view (the argument) which is informed
        // when the views model changes
        PropertyChangeListener propListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                if (propName.equals("model")) {
                    TableModel m = table.getModel();
                    myModel.setTableContent(m);
                } else if (propName.equals("requestRowHeight")) {
                    // this event comes in when the table is
                    // build up from scratch
                    headerView.setRowHeight((Integer)evt.getNewValue());
                } else if (propName.equals(TableContentModel.PROPERTY_DATA)) {
                    headerView.getTableHeader().repaint(); // sort icon
                } else if (propName.equals("font")) {
                    headerView.setFont((Font)evt.getNewValue());
                }
            }
        };
        table.addPropertyChangeListener(propListener);

        // add a listener to the row header view, which makes sure that
        // any row height change is propagated to the content view
        PropertyChangeListener prop2Listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent e) {
                if (e.getPropertyName().equals("allrowheight")) {
                    table.setRowHeight((Integer)e.getNewValue());
                } else if (e.getPropertyName().equals("rowSelectionAllowed")) {
                    table.setRowSelectionAllowed((Boolean)e.getNewValue());
                } else if (e.getPropertyName().equals(
                        "individualrowheight")) {
                    int row = (Integer)e.getNewValue();
                    int newHeight = headerView.getRowHeight(row);
                    table.setRowHeight(row, newHeight);
                }
            }
        };
        headerView.addPropertyChangeListener(prop2Listener);
        if (table instanceof TableContentView) {
            int bestRowHeight =
                ((TableContentView)table).fitCellSizeToRenderer();
            headerView.setRowHeight(bestRowHeight);
        }
        return headerView;
    }
}
