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
 *   Jun 5, 2008 (wiswedel): created
 */
package org.knime.core.data.container;

import java.io.Closeable;

import org.knime.core.data.RowIterator;

/**
 * A {@link RowIterator row iterator} that can be closed in order to save
 * resources. Iterator of this class are returned by tables created with a
 * {@link DataContainer} or {@link org.knime.core.node.BufferedDataContainer},
 * which typically read from file. If the iterator is not pushed to the end of
 * the table, the input stream is not closed, which can cause system failures.
 * This iterator allows the user to close the stream early on (before reaching
 * the end of the table in which case the stream is closed anyway).
 *
 * @author Bernd Wiswedel, University of Konstanz
 */
public abstract class CloseableRowIterator extends RowIterator implements Closeable {

    /** Closes this iterator. Subsequent calls of {@link RowIterator#hasNext()}
     * will return <code>false</code>. This method does not need to be called
     * if the iterator was pushed to the end (stream will be closed
     * automatically). It's meant to be used in cases where the iterator might
     * not advance to the end of the table.
     *
     * <p>This method does nothing if the table is already closed (multiple
     * invocations are ignored). */
    @Override
    public abstract void close();
}
