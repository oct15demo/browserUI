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
 *   29.01.2008 (cebron): created
 */
package org.knime.base.node.mine.cluster.assign;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.knime.base.node.mine.cluster.PMMLClusterTranslator;
import org.knime.base.node.mine.cluster.PMMLClusterTranslator.ComparisonMeasure;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.StringCell;
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
import org.knime.core.node.port.pmml.PMMLPortObject;
import org.knime.core.node.port.pmml.PMMLPortObjectSpec;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortObjectInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.StreamableFunction;
import org.knime.core.node.streamable.StreamableOperator;
import org.knime.core.pmml.PMMLModelType;
import org.w3c.dom.Node;

/**
 *
 * @author cebron, University of Konstanz
 */
public class ClusterAssignerNodeModel extends NodeModel {

   private static final int PMML_PORT = 0;
   private static final int DATA_PORT = 1;

   /** The node logger for this class. */
   private static final NodeLogger LOGGER =
           NodeLogger.getLogger(ClusterAssignerNodeModel.class);

    /**
     *
     */
    public ClusterAssignerNodeModel() {
        super(new PortType[] {
                PMMLPortObject.TYPE,
                BufferedDataTable.TYPE},
                new PortType[] {BufferedDataTable.TYPE});
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        PMMLPortObjectSpec spec = ((PMMLPortObjectSpec)inSpecs[PMML_PORT]);
        DataTableSpec dataSpec = (DataTableSpec) inSpecs[DATA_PORT];
        ColumnRearranger colre = new ColumnRearranger(dataSpec);

        colre.append(new ClusterAssignFactory(
                null, null, createNewOutSpec(dataSpec),
                findLearnedColumnIndices(dataSpec,
                        new HashSet<String>(spec.getLearningFields()))));

        DataTableSpec out = colre.createSpec();
        return new DataTableSpec[]{out};
    }

    private DataColumnSpec createNewOutSpec(final DataTableSpec inSpec) {
        String newColName = DataTableSpec.getUniqueColumnName(inSpec,
                "Cluster");
        return new DataColumnSpecCreator(newColName, StringCell.TYPE)
            .createSpec();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
        BufferedDataTable data =(BufferedDataTable) inData[1];
        ColumnRearranger colre = createColumnRearranger((PMMLPortObject) inData[0], data.getDataTableSpec());
        BufferedDataTable bdt =
                exec.createColumnRearrangeTable(data, colre, exec);
        return new BufferedDataTable[]{bdt};
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
                PMMLPortObject portObj = (PMMLPortObject) ((PortObjectInput) inputs[0]).getPortObject();
                ColumnRearranger colre = createColumnRearranger(portObj, (DataTableSpec) inSpecs[1]);
                StreamableFunction func = colre.createStreamableFunction(1, 0);
                func.runFinal(inputs, outputs, exec);
            }
        };
    }

    private ColumnRearranger createColumnRearranger(final PMMLPortObject port, final DataTableSpec inSpec)
        throws InvalidSettingsException {

        List<Node> models = port.getPMMLValue().getModels(PMMLModelType.ClusteringModel);
        if (models.isEmpty()) {
            String msg = "No Clustering Model found.";
            LOGGER.error(msg);
            throw new RuntimeException(msg);
        }
        PMMLClusterTranslator trans = new PMMLClusterTranslator();
        port.initializeModelTranslator(trans);

        ComparisonMeasure measure = trans.getComparisonMeasure();
        List<Prototype> prototypes = new ArrayList<Prototype>();
        String[] labels = trans.getLabels();
        double[][] protos = trans.getPrototypes();
        for (int i = 0; i < protos.length; i++) {
            double[] prototype = protos[i];
            prototypes.add(new Prototype(prototype, new StringCell(labels[i])));
        }
        ColumnRearranger colre = new ColumnRearranger(inSpec);
        colre.append(new ClusterAssignFactory(measure, prototypes, createNewOutSpec(inSpec),
            findLearnedColumnIndices(inSpec, trans.getUsedColumns())));
        return colre;
    }



    private static int[] findLearnedColumnIndices(final DataTableSpec ospec,
            final Set<String> learnedCols)
            throws InvalidSettingsException {
        int[] colIndices = new int[learnedCols.size()];
        int idx = 0;
        for (String s : learnedCols) {
            int i = ospec.findColumnIndex(s);
            if (i < 0) {
                throw new InvalidSettingsException("Column \""
                        + s + "\" not found in data input spec.");
            }
            colIndices[idx++] = i;
        }
        return colIndices;
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
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    }

    private static class ClusterAssignFactory extends SingleCellFactory {
        private final ComparisonMeasure m_measure;
        private final List<Prototype> m_prototypes;
        private final int[] m_colIndices;

        /**
         * Constructor.
         * @param measure comparison measure
         * @param prototypes list of prototypes
         * @param newColspec the DataColumnSpec of the appended column
         * @param learnedCols columns used for training
         */
        ClusterAssignFactory(final ComparisonMeasure measure,
                final List<Prototype> prototypes,
                final DataColumnSpec newColspec,
                final int[] learnedCols) {
            super(newColspec);
            m_measure = measure;
            m_prototypes = prototypes;
            m_colIndices = learnedCols;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DataCell getCell(final DataRow row) {
            double mindistance = Double.MAX_VALUE;
            DataCell winnercell = DataType.getMissingCell();
            for (Prototype proto : m_prototypes) {
                double dist;
                // TODO: if prototypes are normalized
                // we have to normalize input data here
                if (m_measure.equals(ComparisonMeasure.squaredEuclidean)) {
                    dist = proto.getSquaredEuclideanDistance(row, m_colIndices);
                } else {
                    dist = proto.getDistance(row, m_colIndices);
                }
                if (dist < mindistance) {
                    mindistance = dist;
                    if (mindistance >= 0.0) {
                        winnercell = proto.getLabel();
                    }
                }
            }
            return winnercell;
        }

    }
}
