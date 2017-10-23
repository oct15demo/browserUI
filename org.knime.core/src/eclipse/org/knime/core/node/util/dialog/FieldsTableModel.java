/*
 * ------------------------------------------------------------------------
 *
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
 *   21 Oct 2016 (albrecht): created
 */
package org.knime.core.node.util.dialog;

import java.util.HashMap;
import java.util.Map;

import org.knime.core.node.util.DefaultConfigTableModel;

/**
 * Extends the table model by validation methods.
 * <p>This class might change and is not meant as public API.
 *
 * @author Christian Albrecht, KNIME.com GmbH, Konstanz Germany
 * @since 3.3
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noreference This class is not intended to be referenced by clients.
 */
@SuppressWarnings("serial")
public abstract class FieldsTableModel extends DefaultConfigTableModel {

    /**
     * The columns of the output table.
     */
    public enum Column {
        /** The index of the column with the fields type. */
        FIELD_TYPE,
        /** The index of the replace existing column. */
        REPLACE_EXISTING,
        /** The index of the input column / flow variable column. */
        COLUMN,
        /** The index of the column with the data type. */
        DATA_TYPE,
        /** The index of the column if a list cell should be created. */
        IS_COLLECTION,
        /** The index of the column with default value for the column / flow variable. */
        DEFAULT_VALUE,
        /** The index of the target (e.g. Java) type column */
        TARGET_TYPE,
        /** The index of the target (e.g. Java) field name column. */
        TARGET_FIELD;
    }

    private Map<Column, Integer> m_columns;
    private Map<Integer, Column> m_columnsReverse;

    /**
     * Create a model with the given column names.
     * @param columns the column names.
     */
    public FieldsTableModel(final String[] columns) {
        super(columns);
    }

    /**
     * Fill the mapping of columns to there actual position on the table.
     * @param columns the columns map to fill.
     */
    protected void setColumnsMap(final Map<Column, Integer> columns) {
        m_columns = columns;

        m_columnsReverse = new HashMap<>();
        for (Column column : m_columns.keySet()) {
            m_columnsReverse.put(m_columns.get(column), column);
        }
    }

    /**
     * Returns an attribute value for the cell at <code>row</code>
     * and <code>column</code>.
     *
     * @param   row             the row whose value is to be queried
     * @param   column          the column whose value is to be queried
     * @return                  the value Object at the specified cell
     * @exception  ArrayIndexOutOfBoundsException  if an invalid row or
     *               column was given
     */
    public Object getValueAt(final int row, final Column column) {
        return getValueAt(row, m_columns.get(column));
    }

    /**
     * Sets the object value for the cell at <code>column</code> and
     * <code>row</code>.  <code>aValue</code> is the new value.  This method
     * will generate a <code>tableChanged</code> notification.
     *
     * @param   aValue          the new value; this can be null
     * @param   row             the row whose value is to be changed
     * @param   column          the column whose value is to be changed
     * @exception  ArrayIndexOutOfBoundsException  if an invalid row or
     *               column was given
     */
    public void setValueAt(final Object aValue, final int row, final Column column) {
        setValueAt(aValue, row, m_columns.get(column));
    }

    /**
     * Get the index of the column.
     * @param column the column
     * @return the index of the column of -1 if the column is not found.
     */
    public int getIndex(final Column column) {
        Integer index = m_columns.get(column);
        return null != index ? index : -1;
    }

    /**
     * Test whether the value of the cell defined by row and column is valid.
     * @param row the row index of the cell
     * @param column the column of the cell
     * @return true when cell value is valid
     */
    public abstract boolean isValidValue(int row, Column column);

    /**
     * Gives the error message when isValidValue(row, column) returns false.
     * @param row the row index of the cell
     * @param column the column index of the cell
     * @return the error message or null when cell value is valid.
     */
    public abstract String getErrorMessage(int row, Column column);

    /**
     * Test whether the value of the cell defined by row and column is valid.
     * @param row the row index of the cell
     * @param column the column of the cell
     * @return true when cell value is valid
     */
    boolean isValidValue(final int row, final int column) {
        return isValidValue(row, m_columnsReverse.get(column));
    }

    /**
     * Gives the error message when isValidValue(row, column) returns false.
     * @param row the row index of the cell
     * @param column the column index of the cell
     * @return the error message or null when cell value is valid.
     */
    public String getErrorMessage(final int row, final int column) {
        return getErrorMessage(row, m_columnsReverse.get(column));
    }

    /**
     * Checks whether the cell values in the given row are valid.
     * @param row the row to check
     * @return true when all tested values are valid
     */
    public boolean validateValues(final int row) {
        for (Column c : m_columns.keySet()) {
            if (!isValidValue(row, c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether the all cell values are valid.
     * @return true when all values are valid
     */
    public boolean validateValues() {
        for (int r = 0; r < getRowCount(); r++) {
            if (!validateValues(r)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether to given value is unique in the given column.
     * Optionally provide a type column to ensure uniqueness in the column per type.
     *
     * @param value the value to check
     * @param row the row
     * @param column the column
     * @param typeColumn the type column, can be null
     * @return true when value is unique with special constraints.
     */
    public boolean isUnique(final Object value, final int row, final Column column, final Column typeColumn) {
        boolean isUnique = true;
        Object typeValue = null;
        if (typeColumn != null) {
            typeValue = getValueAt(row, typeColumn);
        }
        for (int i = 0; i < getRowCount(); i++) {
            if (i == row) {
                continue;
            }
            Object value2 = getValueAt(i, column);
            if (value.equals(value2)) {
                if (typeValue != null) {
                    // if optional type value is different value is still assumed unique
                    Object typeValue2 = getValueAt(i, typeColumn);
                    if (!typeValue.equals(typeValue2)) {
                        continue;
                    }
                }
                isUnique = false;
                break;
            }
        }
        return isUnique;
    }

}
