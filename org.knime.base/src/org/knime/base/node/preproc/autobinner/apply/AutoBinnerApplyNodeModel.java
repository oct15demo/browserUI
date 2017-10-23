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
 */
package org.knime.base.node.preproc.autobinner.apply;

import java.io.File;
import java.io.IOException;

import org.knime.base.node.preproc.autobinner.pmml.PMMLDiscretizePreprocPortObjectSpec;
import org.knime.base.node.preproc.autobinner.pmml.PMMLPreprocDiscretize;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.pmml.preproc.PMMLPreprocPortObject;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortObjectInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.StreamableFunction;
import org.knime.core.node.streamable.StreamableOperator;

/**
 * The Model for the Binner (Apply) Node.
 *
 * @author Heiko Hofer
 */
final class AutoBinnerApplyNodeModel extends NodeModel {

    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(AutoBinnerApplyNodeModel.class);

    /** Creates a new instance. */
    AutoBinnerApplyNodeModel() {
        super(new PortType[]{PMMLPreprocPortObject.TYPE,
                BufferedDataTable.TYPE},
                new PortType[]{BufferedDataTable.TYPE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
        throws InvalidSettingsException {
        PMMLDiscretizePreprocPortObjectSpec pmmlPortSpec =
            (PMMLDiscretizePreprocPortObjectSpec)inSpecs[0];
        PMMLPreprocDiscretize op =
            pmmlPortSpec.getOperation();
        DataTableSpec dataSpec = (DataTableSpec)inSpecs[1];
        AutoBinnerApply applier = new AutoBinnerApply();
        return new PortObjectSpec[] {applier.getOutputSpec(op, dataSpec)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inObjects,
            final ExecutionContext exec) throws Exception {
        PMMLPreprocPortObject pmmlPort = (PMMLPreprocPortObject)inObjects[0];
        BufferedDataTable inTable = (BufferedDataTable)inObjects[1];
        validatePMMLPort(pmmlPort);
        PMMLPreprocDiscretize op =
            (PMMLPreprocDiscretize)pmmlPort.getOperations().get(0);
        AutoBinnerApply applier = new AutoBinnerApply();
        BufferedDataTable binnedData = applier.execute(op, inTable, exec);
        return new PortObject[] {binnedData};
    }

    private void validatePMMLPort(final PMMLPreprocPortObject pmmlPort) throws InvalidSettingsException {
        if (pmmlPort.getOperations().isEmpty()) {
            throw new InvalidSettingsException("Binner Settings not available");
        }
        if (pmmlPort.getOperations().size() > 1) {
            LOGGER.warn("Model contains more than one operation. Will skip "
                    + "all except the first one.");
        }
        if (!(pmmlPort.getOperations().get(0)
                instanceof PMMLPreprocDiscretize)) {
            throw new InvalidSettingsException("Binner Settings not available");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamableOperator createStreamableOperator(final PartitionInfo partitionInfo, final PortObjectSpec[] inSpecs)
        throws InvalidSettingsException {
        return new StreamableOperator() {

            @Override
            public void runFinal(final PortInput[] inputs, final PortOutput[] outputs, final ExecutionContext exec) throws Exception {
                PMMLPreprocPortObject pmmlPort = (PMMLPreprocPortObject)((PortObjectInput)inputs[0]).getPortObject();
                validatePMMLPort(pmmlPort);
                PMMLPreprocDiscretize op = (PMMLPreprocDiscretize)pmmlPort.getOperations().get(0);
                AutoBinnerApply applier = new AutoBinnerApply();
                ColumnRearranger core = applier.getRearranger(op, (DataTableSpec)inSpecs[1]);
                StreamableFunction func = core.createStreamableFunction(1, 0);
                func.runFinal(inputs, outputs, exec);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputPortRole[] getInputPortRoles() {
        return new InputPortRole[]{InputPortRole.NONDISTRIBUTED_NONSTREAMABLE, InputPortRole.DISTRIBUTED_STREAMABLE};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputPortRole[] getOutputPortRoles() {
        return new OutputPortRole[]{OutputPortRole.DISTRIBUTED};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // nothing to reset
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // node has no settings
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        // node has no settings
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // node has no settings
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // node has no internal data
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // node has no internal data
    }
}
