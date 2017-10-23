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
import org.knime.core.node.BufferedDataTable;

/**
 * Output of a sequence of rows. See description of super class when the output
 * needs to be filled.
 *
 * @since 2.6
 * @author Bernd Wiswedel, KNIME.com, Zurich, Switzerland
 */
public abstract class RowOutput extends PortOutput {

    /**
     * Adds a new row to the output. The method will block if previously added
     * rows are still being processed by downstream nodes.
     *
     * @param row Row to add.
     * @throws InterruptedException If canceled.
     * @throws OutputClosedException If no consumer is to consume the generated output.
     */
    public abstract void push(final DataRow row) throws InterruptedException;

    /** Fully sets the table and closes the output. Only valid to call if no other rows were added previously through
     * {@link #push(DataRow)}.
     * @param table The non-null table to set.
     * @throws InterruptedException If canceled while offering data to downstream nodes.
     * @throws IllegalStateException If rows were added previously.
     * @since 2.12
     */
    public void setFully(final BufferedDataTable table) throws InterruptedException {
        for (DataRow r : table) {
            push(r);
        }
        close();
    }

    /**
     * To be called by the client to signal the end of the data stream. No more
     * rows will be added.
     * @throws InterruptedException If canceled.
     */
    public abstract void close() throws InterruptedException;

    /** Thrown by {@link RowOutput#push(DataRow)} in case the output is closed. For instance, when all consuming
     * nodes have consumed enough input (e.g. a row filter filtering the first x rows) or there are no consumers at all.
     * @since 3.1
     */
    @SuppressWarnings("serial")
    public static class OutputClosedException extends RuntimeException {


    }

}
