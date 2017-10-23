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
 *   13.08.2008 (ohl): created
 */
package org.knime.core.data.collection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.container.BlobDataCell;
import org.knime.core.data.container.BlobSupportDataRow;
import org.knime.core.data.container.BlobWrapperDataCell;
import org.knime.core.node.BufferedDataTable;

/**
 * A list of {@link DataCell} objects, which special treatment for
 * {@link BlobDataCell}. The implementation will keep blobs in special
 * {@link BlobWrapperDataCell} in order to allow for a possible garbage
 * collection (and deserializing when a blob is accessed.)
 *
 * @author ohl, University of Konstanz
 */
public class BlobSupportDataCellSet
    implements Iterable<DataCell>, Serializable {

    private Set<Wrapper> m_set;

    private boolean m_containsBlobWrapperCells;

    private DataType m_elementType;

    /**
     * Factory method to create a set of data cells based on a collection.
     * <p>
     * If the underlying collection stems from a {@link DataRow} (as read from a
     * any table), consider using {@link #create(DataRow, int[])} in order to
     * minimize cell access.
     *
     * @param coll The underlying collection to take the cells from .
     * @return The newly created set.
     * @throws NullPointerException If the argument is null or contains null
     *             elements.
     */
    public static BlobSupportDataCellSet create(
            final Collection<? extends DataCell> coll) {
        return new BlobSupportDataCellSet(coll);
    }

    /**
     * Create new set containing selected cells from a {@link DataRow}. Using
     * this method will check if the row is returned by a
     * {@link BufferedDataTable} and will handle blobs appropriately.
     *
     * @param row The underlying row
     * @param cols The indices of the cells to store in the set
     * @return A newly created set.
     * @throws NullPointerException If either argument is null.
     * @throws IndexOutOfBoundsException If the indices are invalid.
     */
    public static BlobSupportDataCellSet create(final DataRow row,
            final int[] cols) {
        ArrayList<DataCell> coll = new ArrayList<DataCell>(cols.length);
        for (int i = 0; i < cols.length; i++) {
            DataCell c;
            if (row instanceof BlobSupportDataRow) {
                c = ((BlobSupportDataRow)row).getRawCell(cols[i]);
            } else {
                c = row.getCell(cols[i]);
            }
            coll.add(c);
        }
        return create(coll);
    }

    /**
     * Rather use one of the factory methods to create a new set.
     *
     * @param cells to be stored in the new set.
     *
     */
    BlobSupportDataCellSet(final Collection<? extends DataCell> cells) {
        init(cells);
    }

    /**
     * @param cells
     */
    private void init(final Collection<? extends DataCell> cells) {
        LinkedHashSet<Wrapper> cellSet = new LinkedHashSet<Wrapper>();
        DataType commonType = null;
        for (DataCell c : cells) {
            if (c == null) {
                throw new NullPointerException(
                        "Collection element must not be null");
            }
            DataType cellType;
            Wrapper element;

            if (c instanceof BlobWrapperDataCell) {
                m_containsBlobWrapperCells = true;
                element = new Wrapper(c);
                cellType =
                        DataType.getType(((BlobWrapperDataCell)c)
                                .getBlobClass());
            } else if (c instanceof BlobDataCell) {
                m_containsBlobWrapperCells = true;
                element = new Wrapper(new BlobWrapperDataCell((BlobDataCell)c));
                cellType = c.getType();
            } else {
                element = new Wrapper(c);
                cellType = c.getType();
            }

            cellSet.add(element);

            if (!c.isMissing()) {
                if (commonType == null) {
                    commonType = cellType;
                } else {
                    commonType =
                            DataType.getCommonSuperType(commonType, cellType);
                }
            }
        }
        if (commonType == null) {
            m_elementType = DataType.getMissingCell().getType();
        } else {
            m_elementType = commonType;
        }
        m_set = Collections.unmodifiableSet(cellSet);
    }

    /**
     * Returns true if the set contains the specified cell.
     *
     * @param cell the cell to check for
     * @return true if the set contains the specified cell.
     */
    public boolean contains(final DataCell cell) {
        return m_set.contains(new Wrapper(cell));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<DataCell> iterator() {
        return new WrapperIterator(m_set.iterator());
    }

    /**
     * @return true, if at least one of the stored cells is a blob cell
     */
    public boolean containsBlobWrapperCells() {
        return m_containsBlobWrapperCells;
    }

    /**
     * @return the elements' common super type
     */
    public DataType getElementType() {
        return m_elementType;
    }

    /**
     * Returns the number of currently stored elements. If the set contains more
     * cells than Integer.MAX_VALUE, it returns Integer.MAX_VALUE (see
     * {@link Set#size()}.
     *
     * @return the number of currently stored elements
     */
    public int size() {
        return m_set.size();
    }

    /**
     * Write this object to an output.
     *
     * @param output To write to.
     * @throws IOException If that fails.
     */
    public void serialize(final DataCellDataOutput output) throws IOException {
        output.writeInt(size());
        for (Wrapper w : m_set) {
            DataCell c = w.getCell();
            output.writeDataCell(c);
        }
    }

    /**
     * Static deserializer for a datacell set.
     *
     * @param input To read from.
     * @return A newly created set.
     * @throws IOException If that fails
     * @see DataCellSerializer#deserialize(DataCellDataInput)
     */
    public static BlobSupportDataCellSet deserialize(
            final DataCellDataInput input) throws IOException {
        int size = input.readInt();
        if (size < 0) {
            throw new IOException("Invalid size: " + size);
        }
        ArrayList<DataCell> cells = new ArrayList<DataCell>(size);
        for (int i = 0; i < size; i++) {
            cells.add(input.readDataCell());
        }
        return new BlobSupportDataCellSet(cells);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return m_set.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return m_set.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BlobSupportDataCellSet)) {
            return false;
        }

        BlobSupportDataCellSet s = (BlobSupportDataCellSet)obj;
        return s.getElementType().equals(m_elementType)
                && s.m_set.equals(m_set);
    }
    /*
     * ----------- Serialization methods -------------------------------------
     * Serialization is rarely used on this class as it is only used collection
     * cells (which use a DataCellSerializer). It might be used when a
     * collection cell is part of the column domain information, though
     * (which, again, is a rare case).
     */

    private static final long serialVersionUID = -2654937729187428116L;

    /** Read object method (as described in {@link Serializable} interface). */
    @SuppressWarnings("unchecked")
    private void readObject(final ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        Collection<DataCell> coll =
            (Collection<DataCell>)stream.readObject();
        init(coll);
    }

    /** Write object method (as described in {@link Serializable} interface). */
    private void writeObject(final ObjectOutputStream stream)
        throws IOException {
        Collection<DataCell> coll = new LinkedHashSet<DataCell>();
        for (Wrapper w : m_set) {
            coll.add(w.getCell());
        }
        stream.writeObject(coll);
    }

    /*
     * -----------------------------------------------------------------------
     */
    /**
     * Helper class that wraps all cells stored in the set. To ensure that
     * equals properly works for wrapped blob cells and simple data cells (both
     * ways in all combinations). It determines how deep it has to look inside
     * possibly wrapped elements.
     *
     */
    private static final class Wrapper {

        private DataCell m_cell;

        /**
         *
         */
        private Wrapper(final DataCell c) {
            m_cell = c;
        }

        /**
         * @return the cell
         */
        public DataCell getCell() {
            return m_cell;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof Wrapper)) {
                return false;
            }
            if (this == obj) {
                return true;
            }

            // we need to compare the real data cells in both wrappers
            DataCell thisCell = m_cell;
            DataCell otherCell = ((Wrapper)obj).m_cell;
            if (thisCell == otherCell) {
                return true;
            }
            if (thisCell instanceof BlobWrapperDataCell) {
                thisCell = ((BlobWrapperDataCell)thisCell).getCell();
            }
            if (otherCell instanceof BlobWrapperDataCell) {
                otherCell = ((BlobWrapperDataCell)otherCell).getCell();
            }
            return thisCell.equals(otherCell);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            // BlobWrapper descends
            return m_cell.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return m_cell.toString();
        }
    }

    private static final class WrapperIterator implements BlobSupportDataCellIterator {

        private final Iterator<Wrapper> m_iter;
        private Wrapper m_lastReturned;

        private WrapperIterator(final Iterator<Wrapper> iter) {
            m_iter = iter;
        }

        /** {@inheritDoc} */
        @Override
        public DataCell nextWithBlobSupport() {
            m_lastReturned = m_iter.next();
            return m_lastReturned.getCell();
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return m_iter.hasNext();
        }

        /** {@inheritDoc} */
        @Override
        public DataCell next() {
            DataCell cell = nextWithBlobSupport();
            if (cell instanceof BlobWrapperDataCell) {
                return ((BlobWrapperDataCell)cell).getCell();
            } else {
                return cell;
            }
        }

        /** {@inheritDoc} */
        @Override
        public void replaceLastReturnedWithWrapperCell(final DataCell cell) {
            if (m_lastReturned == null) {
                throw new IllegalStateException("nextWithBlobSupport not called");
            }
            m_lastReturned.m_cell = cell;
            m_lastReturned = null;
        }

        /** {@inheritDoc} */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Modification not allowed");
        }

    }
}
