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
 *   Jun 12, 2012 (wiswedel): created
 */
package org.knime.core.node.streamable;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.node.BufferedDataTable;

/** Default implementation of a {@link RowInput}. It reads data
 * from a {@link DataTable}.
 *
 * @author Bernd Wiswedel, KNIME.com, Zurich, Switzerland
 * @since 2.6
 */
public class DataTableRowInput extends RowInput {

    private final DataTableSpec m_tableSpec;
    private final RowIterator m_iterator;
    private final long m_rowCount;

    /** Initialize with table.
     * @param table The table to read from. */
    public DataTableRowInput(final DataTable table) {
        m_tableSpec = table.getDataTableSpec();
        m_iterator = table.iterator();
        if (table instanceof BufferedDataTable) {
            m_rowCount = ((BufferedDataTable)table).size();
        } else {
            m_rowCount = -1;
        }
    }

    /** {@inheritDoc} */
    @Override
    public DataTableSpec getDataTableSpec() {
        return m_tableSpec;
    }

    /** {@inheritDoc} */
    @Override
    public DataRow poll() throws InterruptedException {
        if (m_iterator.hasNext()) {
            return m_iterator.next();
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        if (m_iterator instanceof CloseableRowIterator) {
            ((CloseableRowIterator)m_iterator).close();
        }
    }

    /**
     * Returns the row count if the table passed during construction was a {@link BufferedDataTable}. Otherwise -1 is
     * returned.
     *
     * @return the number of rows in the table - or -1 if the underlying table is not a buffered data table.
     * @since 2.12
     */
    public long getRowCount() {
        return m_rowCount;
    }

}
