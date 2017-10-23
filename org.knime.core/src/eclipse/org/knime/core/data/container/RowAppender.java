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
 *   Apr 20, 2006 (thor): created
 */
package org.knime.core.data.container;

import org.knime.core.data.DataRow;

/**
 * This is a very simple interface that allows adding <code>DataRow</code>s to 
 * a <code>DataTable</code>, <code>DataContainer</code>, or anything else.
 * 
 * @author Thorsten Meinl, University of Konstanz
 */
public interface RowAppender {
    /**
     * Appends a row to the end of a container. The row must comply with 
     * the settings in the <code>DataTableSpec</code> that has been set when 
     * the container or table has been constructed. 
     * @param row <code>DataRow</code> to be added
     * @throws NullPointerException if the argument is <code>null</code>
     * @throws IllegalStateException If the state forbids to add rows.
     * @throws IllegalArgumentException if the structure of the row forbids to
     *         add it to the table 
     * @throws DataContainerException An IllegalArgumentException may also be
     *         wrapped in a DataContainerException if the writing takes place 
     *         asynchronously. This exception may be caused by the writing of
     *         a row that was added previously (not necessarily the current 
     *         argument). This exception may also indicate the interruption
     *         of a write process. 
     * @throws org.knime.core.util.DuplicateKeyException 
     *         If the row's key has already been added.
     */
    public void addRowToTable(final DataRow row);
}
