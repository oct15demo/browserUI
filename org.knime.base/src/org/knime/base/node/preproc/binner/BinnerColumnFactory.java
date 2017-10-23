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
 * History
 *   02.08.2005 (gabriel): created
 */
package org.knime.base.node.preproc.binner;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnDomain;
import org.knime.core.data.DataColumnDomainCreator;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.NodeSettingsWO;

/**
 * Factory to generate binned string cells from a selected column which can be
 * either replaced or appended.
 *
 * @author Thomas Gabriel, University of Konstanz
 * @deprecated use {@link org.knime.base.node.preproc.pmml.binner.BinnerColumnFactory} instead
 */
@Deprecated
final class BinnerColumnFactory implements CellFactory {

    private final int m_columnIdx;

    private final Bin[] m_bins;

    private final DataColumnSpec m_columnSpec;

    private final boolean m_append;

    /**
     * A binned column created by name and a number of bins. The new, binned
     * column is either append or replaced in the current table.
     *
     * @param columnIdx the column index to bin
     * @param name the new binned column name
     * @param bins a set of bins
     * @param append append or replace column
     */
    BinnerColumnFactory(final int columnIdx, final Bin[] bins,
            final String name, final boolean append) {
        m_columnIdx = columnIdx;
        m_bins = bins;
        // ensures that all bin names are available as possible values
        // in the column spec, the buffereddatatable will iterate all values
        // (and hence determine the possible values) but some of the names
        // might not be used in the table (no value for this bin)
        StringCell[] binNames = new StringCell[bins.length];
        for (int i = 0; i < binNames.length; i++) {
            binNames[i] = new StringCell(bins[i].getBinName());
        }
        DataColumnDomain dom =
            new DataColumnDomainCreator(binNames).createDomain();
        DataColumnSpecCreator specCreator =
            new DataColumnSpecCreator(name, StringCell.TYPE);
        specCreator.setDomain(dom);
        m_columnSpec = specCreator.createSpec();
        m_append = append;
    }

    /**
     * @return the column index to bin
     */
    int getColumnIndex() {
        return m_columnIdx;
    }

    /**
     * @return if this bin is appended to the table
     */
    boolean isAppendedColumn() {
        return m_append;
    }

    /**
     * @return the column name to append
     */
    DataColumnSpec getColumnSpec() {
        return m_columnSpec;
    }

    /**
     * @return number of bins
     */
    int getNrBins() {
        return m_bins.length;
    }

    /**
     * Return <code>Bin</code> for index.
     *
     * @param index for this index
     * @return the assigned bin
     */
    Bin getBin(final int index) {
        return m_bins[index];
    }

    /**
     * Apply a value to this bining trying to cover it at all available
     * <code>Bin</code>s.
     *
     * @param cell the value to cover
     * @return the bin's name as DataCell which cover's this value
     */
    DataCell apply(final DataCell cell) {
        if (cell.isMissing()) {
            return DataType.getMissingCell();
        }
        for (int i = 0; i < m_bins.length; i++) {
            if (m_bins[i].covers(cell)) {
                return new StringCell(m_bins[i].getBinName());
            }
        }
        return DataType.getMissingCell();
    }

    /**
     * General bin.
     */
    public interface Bin {

        /**
         * @return this bin's name
         */
        String getBinName();

        /**
         * @param value the double value
         * @return if covered by this interval
         */
        boolean covers(DataCell value);

        /**
         * Save this bin.
         *
         * @param set to this settings
         */
        void saveToSettings(NodeSettingsWO set);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell[] getCells(final DataRow row) {
        DataCell cell = row.getCell(m_columnIdx);
        return new DataCell[]{apply(cell)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataColumnSpec[] getColumnSpecs() {
        return new DataColumnSpec[]{m_columnSpec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProgress(final int curRowNr, final int rowCount,
            final RowKey lastKey, final ExecutionMonitor exec) {
        exec.setProgress(1.0 * curRowNr / rowCount, "Binning row: "
                + lastKey.getString());
    }
}
