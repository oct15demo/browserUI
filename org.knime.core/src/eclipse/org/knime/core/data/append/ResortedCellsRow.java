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
 */
package org.knime.core.data.append;

import java.util.Iterator;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultCellIterator;


/**
 * A row that takes a base row and re-sorts the cells in it according to an
 * <code>int[]</code> parameter passed in the constructor.
 *
 * @author Bernd Wiswedel, University of Konstanz
 * @since 3.1
 */
public class ResortedCellsRow implements DataRow {

    private final DataRow m_row;

    private final int[] m_sort;

    /**
     * Creates new row with <code>row</code> as underlying base row and
     * <code>sort</code> the new sorting scheme. That is the old
     * <code>i</code>-th entry becomes entry number <code>sort[i]</code>.
     *
     * @param row the base row
     * @param sort the re-sorting
     * @throws IllegalArgumentException if the lengths of arrays don't match
     * @throws NullPointerException if either argument is <code>null</code>
     */
    protected ResortedCellsRow(final DataRow row, final int[] sort) {
        if (row.getNumCells() != sort.length) {
            throw new IllegalArgumentException("Length don't match.");
        }
        m_row = row;
        m_sort = sort;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumCells() {
        return m_row.getNumCells();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RowKey getKey() {
        return m_row.getKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell getCell(final int index) {
        return m_row.getCell(m_sort[index]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<DataCell> iterator() {
        return new DefaultCellIterator(this);
    }
}
