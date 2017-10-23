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
 */
package org.knime.base.node.util.sampledata;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Random;

import javax.swing.SizeSequence;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
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
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.streamable.BufferedDataTableRowOutput;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableOperator;


/**
 * Model that generates one DataTable with randomly distributed patterns. So far
 * everything is somewhat hardcoded.
 *
 * <p>
 * Hardcoded is now: 6 columns, 3 universes (3x2 columns) with 7 clusters ( u1:
 * 2, u2: 3, u3: 2. Cluster is defined as: In one universe, there is a cloud of
 * patterns (gaussian distributed), the other dimensions are noisy.
 *
 * @author Bernd Wiswedel, University of Konstanz
 */
public class SampleDataNodeModel extends NodeModel {

    /** configuration key: cluster count (int[]). */
    static final String CFGKEY_CLUSCOUNT = "clustercount";

    /** configuration key: universe size (int[]). */
    static final String CFGKEY_UNISIZE = "unisize";

    /** configuration key: overall pattern count. */
    static final String CFGKEY_PATCOUNT = "patcount";

    /** configuration key: standard deviation. */
    static final String CFGKEY_DEV = "stddeviation";

    /** configuration key: noise fraction. */
    static final String CFGKEY_NOISE = "noise";

    /** configuration key: random seed. */
    static final String CFGKEY_SEED = "seed";

    /**
     * Hardcode here if you prefer to have {@link Double#NaN} for noise instead
     * of a random value in a dimension (that is: If a pattern has to cluster in
     * universe 2, the attributes in all other universes are drawn randomly.
     * This flag will set their value in the resp. dimension to
     * {@link Double#NaN}. Useful if you want to display the clusters in a
     * scatterplot and don't want the noise.
     */
    public static final boolean ASSIGN_NAN_FOR_NOISE = false;

    /**
     * The number of patterns in total.
     */
    public static final int PATTERN_COUNT = 5400;

    /**
     * Standard deviation within a cluster.
     */
    public static final double STD_DEVIATION = 0.1;

    /**
     * For each "universe" a number of clusters. Each cluster will have the same
     * number of patterns in it (for pattern_count = 700 and this cluster_count
     * it will be 700/(2+3+2) = 100
     */
    public static final int[] CLUSTER_COUNT = new int[]{2, 2};

    /**
     * The dimensionality for each universe.
     */
    public static final int[] UNIVERSE_SIZE = new int[]{2, 2};

    /** The default noise fraction (0). */
    public static final double NOISEFRAC = 0.0;

    private int m_patCount;

    private double m_dev;

    private int[] m_clusterCount;

    private int[] m_uniSize;

    private double[] m_minValues;

    private double[] m_maxValues;

    private double m_noiseFrac;

    private int m_randomSeed;

    /**
     * Create new Sample.
     */
    public SampleDataNodeModel() {
        super(0, 2);
        m_clusterCount = CLUSTER_COUNT.clone();
        m_uniSize = UNIVERSE_SIZE.clone();
        m_patCount = PATTERN_COUNT;
        m_dev = STD_DEVIATION;
        m_noiseFrac = NOISEFRAC;
        m_randomSeed = (int)System.currentTimeMillis();
        initMinMax();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] data,
            final ExecutionContext exec) throws Exception {
        DataTableSpec[] outSpecs = configure(new DataTableSpec[2]);
        DataTableSpec dataSpec = outSpecs[0];
        DataTableSpec clusterSpec = outSpecs[1];
        BufferedDataTableRowOutput dataOut = new BufferedDataTableRowOutput(exec.createDataContainer(dataSpec));
        BufferedDataTableRowOutput clusterOut = new BufferedDataTableRowOutput(exec.createDataContainer(clusterSpec));
        run(dataSpec, dataOut, clusterSpec, clusterOut, exec);
        return new BufferedDataTable[] {dataOut.getDataTable(), clusterOut.getDataTable()};
    }

    private void run(final DataTableSpec spec, final RowOutput dataOutput, final DataTableSpec clusterSpec,
        final RowOutput clusterOutput, final ExecutionContext exec) throws Exception {
        Random rand = new Random(m_randomSeed);
        NodeLogger.getLogger(getClass()).info("Using '"
                + m_randomSeed + "' as seed for random data generation.");

        int dimensions = spec.getNumColumns() - 1;

        SizeSequence uniSizes = new SizeSequence(m_uniSize);
        SizeSequence clusters = new SizeSequence(m_clusterCount);
        int l = m_clusterCount.length - 1;
        final int overallClusterCount = clusters.getPosition(l)
                + clusters.getSize(l);
        final double noiseFrac = Math.min(Math.max(0.0, m_noiseFrac), 1.0);
        /*
         * the cluster centers. If a cluster doesn't restrict a dimension, the
         * value is NaN
         */
        double[][] optimalClusters = new double[Math
                                                .max(overallClusterCount, 1)][dimensions];
        if (overallClusterCount == 0) {
            Arrays.fill(optimalClusters[0], Double.NaN);
        }
        for (int c = 0; c < overallClusterCount; c++) {
            int uniToClusterIn = clusters.getIndex(c);
            int startPos = uniSizes.getPosition(uniToClusterIn);
            int endPos = startPos + uniSizes.getSize(uniToClusterIn);
            // int universeSize = m_uniSize[uniToClusterIn];
            // assert (universeSize == uniSizes.getSize(uniToClusterIn));
            for (int d = 0; d < dimensions; d++) {
                if (d < startPos || d >= endPos) {
                    optimalClusters[c][d] = Double.NaN;
                } else {
                    double min = m_minValues[d];
                    double max = m_maxValues[d];
                    double range = max - min;
                    double min2 = min + m_dev * range;
                    double max2 = max - m_dev * range;
                    double range2 = max2 - min2;
                    double center = min2 + rand.nextDouble() * range2;
                    optimalClusters[c][d] = center;
                }
            }
        }

        DataRow[] centerRows = new DataRow[overallClusterCount];
        int colNameLength = overallClusterCount + (noiseFrac > 0.0 ? 1 : 0);
        StringCell[] colNames = new StringCell[colNameLength];
        for (int i = 0; i < overallClusterCount; i++) {
            double[] cs = optimalClusters[i];
            DataCell[] cells = new DataCell[dimensions];
            for (int c = 0; c < dimensions; c++) {
                if (Double.isNaN(cs[c])) {
                    cells[c] = DataType.getMissingCell();
                } else {
                    cells[c] = new DoubleCell(cs[c]);
                }
            }
            colNames[i] = new StringCell("Cluster_" + i);
            centerRows[i] = new DefaultRow(colNames[i].toString(), cells);
        }
        if (noiseFrac > 0.0) {
            colNames[overallClusterCount] = new StringCell("Noise");
        }
        for (DataRow r : centerRows) {
            clusterOutput.push(r);
        }
        clusterOutput.close();

        /* first output (data) comes here */

        // assign attributes to patterns
        int noise = (int)(m_patCount * noiseFrac);
        int patternsPerCluster = (m_patCount - noise) / optimalClusters.length;
        int patternCount = patternsPerCluster * optimalClusters.length;
        noise = noiseFrac > 0.0 ? m_patCount - patternCount : 0;

        int pattern = 0;
        double totalCount = m_patCount;
        for (int c = 0; c < optimalClusters.length; c++) { // all clusters
            double[] centers = optimalClusters[c];
            // patterns in cluster
            for (int p = 0; p < patternsPerCluster; p++) {
                double[] d = fill(rand, centers);
                DataCell cl = (overallClusterCount > 0
                        ? colNames[c] : DataType.getMissingCell());
                DataRow r = createRow(RowKey.createRowKey(pattern), d, cl);
                dataOutput.push(r);
                final int patternTempFinal = pattern;
                exec.setProgress(pattern / totalCount, () -> ("Added row " + patternTempFinal));
                exec.checkCanceled();
                pattern++;
            }
        }
        assert (pattern == patternCount);
        double[] noiseCenter = new double[dimensions];
        Arrays.fill(noiseCenter, Double.NaN);
        // draw noise patterns
        for (int i = 0; i < noise; i++) {
            int index = i + pattern;
            double[] d = fill(rand, noiseCenter);
            DataCell cl = colNames[colNames.length - 1];
            DataRow r = createRow(RowKey.createRowKey(index), d, cl);
            dataOutput.push(r);
            exec.setProgress(index / totalCount, () -> ("Added row " + index));
            exec.checkCanceled();
        }
        dataOutput.close();
    }

    private static DataRow createRow(final RowKey key, final double[] d,
            final DataCell cl) {
        DataCell[] cells = new DataCell[d.length + 1];
        for (int i = 0; i < d.length; i++) {
            cells[i] = new DoubleCell(d[i]);
        }
        cells[d.length] = cl;
        return new DefaultRow(key, cells);
    }

    /** {@inheritDoc} */
    @Override
    public StreamableOperator createStreamableOperator(final PartitionInfo partitionInfo, final PortObjectSpec[] inSpecs)
        throws InvalidSettingsException {
        return new StreamableOperator() {
            @Override
            public void runFinal(final PortInput[] inputs, final PortOutput[] outputs,
                final ExecutionContext exec) throws Exception {
                DataTableSpec[] outSpecs = configure(new DataTableSpec[2]);
                DataTableSpec dataSpec = outSpecs[0];
                DataTableSpec clusterSpec = outSpecs[1];
                RowOutput dataOut = (RowOutput)outputs[0];
                RowOutput clusterOut = (RowOutput)outputs[1];
                run(dataSpec, dataOut, clusterSpec, clusterOut, exec);
            }
        };
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
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
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
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) {
        DataColumnSpec[] colSpecs = new DataColumnSpec[m_minValues.length + 1];
        int currentDim = 0;
        for (int u = 0; u < m_clusterCount.length; u++) {
            int dimCountInUniverse = m_uniSize[u];
            Hashtable<String, String> annot = new Hashtable<String, String>();
            annot.put("universe_name", "Universe_" + u);
            for (int i = 0; i < dimCountInUniverse; i++) {
                String n = "Universe_" + u + "_" + i;
                DataType t = DoubleCell.TYPE;
                DataColumnSpecCreator creator = new DataColumnSpecCreator(n, t);
                creator.setProperties(new DataColumnProperties(annot));
                colSpecs[currentDim++] = creator.createSpec();
            }
        }
        String n = "Cluster Membership";
        DataType t = StringCell.TYPE;
        DataColumnSpecCreator creator = new DataColumnSpecCreator(n, t);
        colSpecs[currentDim] = creator.createSpec();

        DataColumnSpec[] centerColSpec =
            new DataColumnSpec[colSpecs.length - 1];
        System.arraycopy(colSpecs, 0, centerColSpec, 0, centerColSpec.length);
        return new DataTableSpec[]{new DataTableSpec(colSpecs),
                new DataTableSpec(centerColSpec)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        settings.addDouble(CFGKEY_DEV, m_dev);
        settings.addDouble(CFGKEY_NOISE, m_noiseFrac);
        settings.addInt(CFGKEY_PATCOUNT, m_patCount);
        settings.addInt(CFGKEY_SEED, m_randomSeed);
        settings.addIntArray(CFGKEY_CLUSCOUNT, m_clusterCount);
        settings.addIntArray(SampleDataNodeModel.CFGKEY_UNISIZE, m_uniSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        double dev = settings.getDouble(CFGKEY_DEV);
        int pat = settings.getInt(CFGKEY_PATCOUNT);
        int[] clusCount = settings.getIntArray(CFGKEY_CLUSCOUNT);
        int[] uniSize = settings.getIntArray(CFGKEY_UNISIZE);
        double noise = settings.getDouble(CFGKEY_NOISE);
        settings.getInt(CFGKEY_SEED);
        if (dev <= 0.0) {
            throw new InvalidSettingsException(
                    "Std deviation must not be <= 0: " + dev);
        }
        if (pat <= 0) {
            throw new InvalidSettingsException(
                    "Pattern count must not be <= 0: " + pat);
        }
        if (clusCount.length != uniSize.length) {
            throw new InvalidSettingsException("Sizes must match: "
                    + clusCount.length + " vs. " + uniSize.length);
        }
        for (int i = 0; i < clusCount.length; i++) {
            if (clusCount[i] < 0) {
                throw new InvalidSettingsException(
                        "Cluster count must not be < 0: " + clusCount[i]);
            }
        }
        for (int i = 0; i < clusCount.length; i++) {
            if (uniSize[i] <= 0) {
                throw new InvalidSettingsException(
                        "Uni size must not be <= 0: " + uniSize[i]);
            }
        }
        if (noise < 0.0 && noise > 1.0) {
            throw new InvalidSettingsException("Noise fraction not in [0:1]: "
                    + noise);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_dev = settings.getDouble(CFGKEY_DEV);
        m_patCount = settings.getInt(CFGKEY_PATCOUNT);
        m_clusterCount = settings.getIntArray(CFGKEY_CLUSCOUNT);
        m_uniSize = settings.getIntArray(CFGKEY_UNISIZE);
        m_noiseFrac = settings.getDouble(CFGKEY_NOISE);
        m_randomSeed = settings.getInt(CFGKEY_SEED);
        initMinMax();
    }

    private void initMinMax() {
        int dimensions = 0;
        for (int i = 0; i < m_uniSize.length; i++) {
            dimensions += m_uniSize[i];
        }
        m_minValues = new double[dimensions];
        m_maxValues = new double[dimensions];
        Arrays.fill(m_minValues, 0.0);
        Arrays.fill(m_maxValues, 1.0);
    }

    private double[] fill(final Random rand, final double[] centers) {
        final int dimensions = centers.length;
        double[] result = new double[dimensions];
        for (int d = 0; d < dimensions; d++) {
            double min = m_minValues[d];
            double max = m_maxValues[d];
            double range = max - min;
            if (Double.isNaN(centers[d])) {
                if (ASSIGN_NAN_FOR_NOISE) {
                    result[d] = Double.NaN;
                } else {
                    result[d] = min + rand.nextDouble() * range;
                }

            } else {
                double val;
                do {
                    val = centers[d] + m_dev * range * rand.nextGaussian();
                } while (val < min || val > max);
                result[d] = val;
            }
        }
        return result;
    }
}
