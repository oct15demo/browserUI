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
 *   Jun 13, 2012 (wiswedel): created
 */
package org.knime.core.node.streamable.simple;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.StreamableFunction;
import org.knime.core.node.streamable.StreamableFunctionProducer;

/**
 * Abstract definition of a node that applies a simple function using a {@link ColumnRearranger}. Each input row is
 * mapped to an output row.
 *
 * @author Bernd Wiswedel, KNIME.com, Zurich, Switzerland
 * @since 2.6
 */
public abstract class SimpleStreamableFunctionNodeModel extends NodeModel implements StreamableFunctionProducer {

    private int m_streamableInPortIdx;
    private int m_streamableOutPortIdx;

    /**
     * Default constructor, defining one data input and one data output port.
     */
    public SimpleStreamableFunctionNodeModel() {
        super(1, 1);
        m_streamableInPortIdx = 0;
        m_streamableOutPortIdx = 0;
    }

    /**
     * Constructor for a node with multiple in or out ports.
     *
     * @param inPortTypes in-port types. The ports at the index <code>streamableInPortIdx</code> MUST be a non-optional {@link BufferedDataTable}!
     * @param outPortTypes out-port types.The ports at the index <code>streamableOutPortIdx</code> MUST be a non-optional {@link BufferedDataTable}!
     * @param streamableInPortIdx the index of the port that is streamable. All the others are assumed as neither streamable nor distributable.
     * @param streamableOutPortIdx the index of the port that is streamable. All the others are assumed as neither streamable nor distributable.
     * @since 3.1
     */
    public SimpleStreamableFunctionNodeModel(final PortType[] inPortTypes,
        final PortType[] outPortTypes, final int streamableInPortIdx, final int streamableOutPortIdx) {
        super(inPortTypes, outPortTypes);
        assert BufferedDataTable.TYPE.isSuperTypeOf(inPortTypes[streamableInPortIdx]);
        assert BufferedDataTable.TYPE.isSuperTypeOf(outPortTypes[streamableOutPortIdx]);
        assert !inPortTypes[streamableInPortIdx].isOptional();
        assert !outPortTypes[streamableOutPortIdx].isOptional();
        m_streamableInPortIdx = streamableInPortIdx;
        m_streamableOutPortIdx = streamableOutPortIdx;
    }

    /** {@inheritDoc} */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        BufferedDataTable in = inData[0];
        ColumnRearranger r = createColumnRearranger(in.getDataTableSpec());
        BufferedDataTable out = exec.createColumnRearrangeTable(in, r, exec);
        return new BufferedDataTable[]{out};
    }

    /** {@inheritDoc} */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpec in = inSpecs[0];
        ColumnRearranger r = createColumnRearranger(in);
        DataTableSpec out = r.createSpec();
        return new DataTableSpec[]{out};
    }

    /**
     * Can the computation of the individual nodes run in parallel? Default is <code>true</code> but subclasses can
     * enforce sequential access by overwriting this method and returning <code>false</code>.
     *
     * @return true (possibly overwritten).
     */
    protected boolean isDistributable() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public InputPortRole[] getInputPortRoles() {
        InputPortRole[] in = new InputPortRole[getNrInPorts()];
        Arrays.fill(in, InputPortRole.NONDISTRIBUTED_NONSTREAMABLE);
        in[m_streamableInPortIdx] =
            isDistributable() ? org.knime.core.node.streamable.InputPortRole.DISTRIBUTED_STREAMABLE : org.knime.core.node.streamable.InputPortRole.NONDISTRIBUTED_STREAMABLE;
        return in;
    }

    /** {@inheritDoc} */
    @Override
    public OutputPortRole[] getOutputPortRoles() {
        OutputPortRole[] out = new OutputPortRole[getNrOutPorts()];
        Arrays.fill(out, OutputPortRole.NONDISTRIBUTED);
        out[m_streamableOutPortIdx] = isDistributable() ? OutputPortRole.DISTRIBUTED : OutputPortRole.NONDISTRIBUTED;
        return out;
    }

    /**
     * Creates a column rearranger that describes the changes to the input table. Sub classes will check the consistency
     * of the input table with their settings (fail with {@link InvalidSettingsException} if necessary) and then return
     * a customized {@link ColumnRearranger}.
     *
     * @param spec The spec of the input table.
     * @return A column rearranger describing the changes, never null.
     * @throws InvalidSettingsException If the settings or the input are invalid.
     */
    protected abstract ColumnRearranger createColumnRearranger(final DataTableSpec spec)
        throws InvalidSettingsException;

    /**
     * @return the streamableInPortIdx
     * @since 3.1
     */
    protected int getStreamableInPortIdx() {
        return m_streamableInPortIdx;
    }

    /**
     * @return the streamableOutPortIdx
     * @since 3.1
     */
    public int getStreamableOutPortIdx() {
        return m_streamableOutPortIdx;
    }

    /** {@inheritDoc} */
    @Override
    public StreamableFunction
        createStreamableOperator(final PartitionInfo partitionInfo, final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        DataTableSpec in = (DataTableSpec)inSpecs[m_streamableInPortIdx];
        return createColumnRearranger(in).createStreamableFunction(m_streamableInPortIdx, m_streamableOutPortIdx);
    }

    /** {@inheritDoc} */
    @Override
    protected void reset() {
        // possibly overwritten
    }

    /** {@inheritDoc} */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
        CanceledExecutionException {
        // possibly overwritten
    }

    /** {@inheritDoc} */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
        CanceledExecutionException {
        // possibly overwritten
    }

}
