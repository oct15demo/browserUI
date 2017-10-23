/* Created on Apr 19, 2006 2:00:23 PM by thor
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
 *   Apr 19, 2006 (thor): created
 */
package org.knime.base.node.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.container.DataContainer;
import org.knime.core.data.container.RowAppender;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.NodeModel;
import org.knime.core.util.ThreadPool;

/**
 * This class is an extension of a normal {@link org.knime.core.node.NodeModel}
 * that offers parallel processing of {@link DataTable}s. Therefore the
 * {@link #executeByChunk( BufferedDataTable, BufferedDataTable[],
 * RowAppender[], ExecutionMonitor)} method must be overriden. This method is
 * called with a {@link DataTable} containing only a part of the input rows as
 * often as necessary. A default value for the maximal chunk size (i.e. the
 * number of rows in the chunked data table) is given in the constructor.<br>
 *
 * If the node has more than one input table only the first input table is
 * chunked, the remaining ones are passed to {@link #executeByChunk(
 * BufferedDataTable, BufferedDataTable[], RowAppender[], ExecutionMonitor)}
 * completely.
 *
 * @author Thorsten Meinl, University of Konstanz
 */
public abstract class AbstractParallelNodeModel extends NodeModel {
    private int m_chunkSize;

    /** The execution service that is used. */
    protected final ThreadPool m_workers;

    /**
     * Creates a new AbstractParallelNodeModel.
     *
     * @param nrDataIns The number of {@link DataTable} elements expected as
     *            inputs.
     * @param nrDataOuts The number of {@link DataTable} objects expected at the
     *            output.
     * @param chunkSize the default number of rows in the DataTables that are
     *            passed to {@link #executeByChunk( BufferedDataTable,
     *            BufferedDataTable[], RowAppender[], ExecutionMonitor)}
     * @param workers a thread pool where threads for processing the chunks are
     *            taken from
     */
    public AbstractParallelNodeModel(final int nrDataIns, final int nrDataOuts,
            final int chunkSize, final ThreadPool workers) {
        super(nrDataIns, nrDataOuts);
        m_chunkSize = chunkSize;
        m_workers = workers;
    }

    /**
     * This method is called before the first chunked is processed. The method
     * must return the data table specification(s) for the result table(s)
     * because the {@link RowAppender} passed to {@link #executeByChunk(
     * BufferedDataTable, BufferedDataTable[], RowAppender[], ExecutionMonitor)}
     * must be constructed accordingly.
     *
     * @param data the input data tables
     * @return the table spec(s) of the result table(s) in the right order. The
     *         result and none of the table specs must be null!
     * @throws Exception if something goes wrong during preparation
     */
    protected abstract DataTableSpec[] prepareExecute(final DataTable[] data)
            throws Exception;

    /**
     * {@inheritDoc}
     */
    @Override
    protected final BufferedDataTable[] execute(final BufferedDataTable[] data,
            final ExecutionContext exec) throws Exception {
        final DataTableSpec[] outSpecs = prepareExecute(data);

        final List<Future<BufferedDataContainer[]>> futures = new ArrayList<>();
        final BufferedDataTable[] additionalTables =
                new BufferedDataTable[Math.max(0, data.length - 1)];
        System.arraycopy(data, 1, additionalTables, 0, additionalTables.length);

        // do some consistency checks to bail out as early as possible
        if (outSpecs == null) {
            throw new NullPointerException("Implementation Error: The "
                    + "array of generated output table specs can't be null.");
        }
        if (outSpecs.length != getNrOutPorts()) {
            throw new IllegalStateException("Implementation Error: Number of"
                    + " provided DataTableSpecs doesn't match number of output"
                    + " ports");
        }
        for (DataTableSpec outSpec : outSpecs) {
            if (outSpec == null) {
                throw new IllegalStateException("Implementation Error: The"
                        + " generated output DataTableSpec is null.");
            }
        }

        final double max = data[0].size();

        final Callable<Void> submitter = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                final RowIterator it = data[0].iterator();
                BufferedDataContainer container = null;
                int count = 0, chunks = 0;
                while (true) {
                    if ((count++ % m_chunkSize == 0) || !it.hasNext()) {
                        exec.checkCanceled();

                        if (container != null) {
                            container.close();
                            final BufferedDataContainer temp = container;
                            chunks++;
                            final int temp2 = chunks;
                            futures.add(m_workers
                                    .submit(new Callable<BufferedDataContainer[]>() {
                                        @Override
                                        public BufferedDataContainer[] call()
                                                throws Exception {
                                            ExecutionMonitor subProg =
                                                    exec
                                                            .createSilentSubProgress((m_chunkSize > max) ? 1
                                                                    : m_chunkSize
                                                                            / max);
                                            exec.setMessage("Processing chunk "
                                                    + temp2);
                                            BufferedDataContainer[] result = new BufferedDataContainer[outSpecs.length];
                                            for (int i = 0; i < outSpecs.length; i++) {
                                                result[i] = exec.createDataContainer(outSpecs[i], true, 0);
                                            }

                                            executeByChunk(temp.getTable(),
                                                    additionalTables, result,
                                                    subProg);

                                            for (DataContainer c : result) {
                                                c.close();
                                            }

                                            exec.setProgress(temp2
                                                    * m_chunkSize / max);
                                            return result;
                                        }
                                    }));
                        }
                        if (!it.hasNext()) {
                            break;
                        }

                        container =
                                exec.createDataContainer(data[0]
                                        .getDataTableSpec());
                    }
                    container.addRowToTable(it.next());
                }
                return null;
            }
        };

        try {
            m_workers.runInvisible(submitter);
        } catch (IllegalThreadStateException ex) {
            // this node has not been started by a thread from a thread pool.
            // This is odd, but may happen
            submitter.call();
        }

        final BufferedDataTable[][] tempTables =
                new BufferedDataTable[outSpecs.length][futures.size()];
        int k = 0;
        for (Future<BufferedDataContainer[]> results : futures) {
            try {
                exec.checkCanceled();
            } catch (CanceledExecutionException ex) {
                for (Future<BufferedDataContainer[]> cancel : futures) {
                    cancel.cancel(true);
                }
                throw ex;
            }

            final BufferedDataContainer[] temp = results.get();

            if ((temp == null) || (temp.length != getNrOutPorts())) {
                throw new IllegalStateException("Invalid result. Execution "
                        + " failed, reason: data is null or number "
                        + "of outputs wrong.");
            }

            for (int i = 0; i < temp.length; i++) {
                tempTables[i][k] = temp[i].getTable();
            }
            k++;
        }

        final BufferedDataTable[] resultTables = new BufferedDataTable[outSpecs.length];
        for (int i = 0; i < resultTables.length; i++) {
            resultTables[i] = exec.createConcatenateTable(exec, tempTables[i]);
        }

        return resultTables;
    }

    /**
     * This method is called as often as necessary by multiple threads. The
     * <code>inData</code>-table will contain at most
     * <code>maxChunkSize</code> rows from the the first table in the array
     * passed to {@link #execute(BufferedDataTable[], ExecutionContext)}, the
     * <code>additionalData</code>-tables are passed completely.
     *
     * @param inDataChunk the chunked input data table
     * @param additionalData the complete tables of additional data
     * @param outputTables data containers for the output tables where the
     *            computed rows must be added
     * @param exec an execution monitor which is actually a subprogress monitor
     * @throws Exception if an exception occurs
     */
    protected abstract void executeByChunk(final BufferedDataTable inDataChunk,
            final BufferedDataTable[] additionalData,
            final RowAppender[] outputTables, final ExecutionMonitor exec)
            throws Exception;

    /**
     * Sets the chunk size of the split data tables.
     *
     * @param newValue the new value which is number of rows
     */
    public void setChunkSize(final int newValue) {
        m_chunkSize = newValue;
    }

    /**
     * Returns the current chunk size.
     *
     * @return the chunk size in number of rows
     */
    public int getChunkSize() {
        return m_chunkSize;
    }
}
