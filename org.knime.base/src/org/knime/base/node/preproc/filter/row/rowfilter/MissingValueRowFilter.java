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
 *   04.02.2008 (ohl): created
 */
package org.knime.base.node.preproc.filter.row.rowfilter;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.collection.CollectionDataValue;

/**
 * Filters rows with a missing value in a certain column.<br>
 * NOTE: Before the filter instance is applied it must be configured to find the
 * column index to the specified column name.
 *
 * @author ohl, University of Konstanz
 */
public class MissingValueRowFilter extends AttrValueRowFilter {
    /**
     * Creates a row filter that includes or excludes (depending on the
     * corresponding argument) rows with a missing value in the specified
     * column.
     *
     *
     * @param colName the column name of the cell to match
     * @param include if true, matching rows are included, if false, they are
     *            excluded.
     *
     */
    public MissingValueRowFilter(final String colName, final boolean include) {
        this(colName, include, false);
    }
    /**
     * Creates a row filter that includes or excludes (depending on the
     * corresponding argument) rows with a missing value in the specified
     * column.
     *
     *
     * @param colName the column name of the cell to match
     * @param include if true, matching rows are included, if false, they are
     *            excluded.
     * @param deepFiltering if true, the filtering is applied to the elements of a collection cell, if false,
     *            the filter is applied to the collection cell as a whole
     * @since 2.10
     *
     */
    public MissingValueRowFilter(final String colName, final boolean include, final boolean deepFiltering) {
        super(colName, include, deepFiltering);
    }

    /**
     * Don't use created filter without loading settings before.
     */
    MissingValueRowFilter() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(final DataRow row, final long rowIndex) throws EndOfTableException, IncludeFromNowOn {
        // if this goes off, configure was probably not called after
        // loading filter's settings
        assert getColIdx() >= 0;

        DataCell theCell = row.getCell(getColIdx());
        boolean match = matches(theCell);
        if (!match && getDeepFiltering() && (theCell instanceof CollectionDataValue)) {
            match = performDeepFiltering((CollectionDataValue) theCell);
        }
        return ((getInclude() && match) || (!getInclude() && !match));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(final DataCell theCell) {
        return theCell.isMissing();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "MissingValueFilter: ColName='" + getColName()
                + (getInclude() ? " includes" : "excludes") + " rows.";
    }

}
