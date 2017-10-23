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
 *   17.07.2006 (cebron): created
 */
package org.knime.base.node.mine.cluster.fuzzycmeans;

import java.util.Random;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.RowKey;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;

/**
 * The Fuzzy c-means algorithm.
 *
 * @author Nicolas Cebron, University of Konstanz
 */
public class FCMAlgorithmMemory extends FCMAlgorithm {

    /*
     * Data to be clustered
     */
    private double[][] m_data;

    /*
     * Rowkeys assigned to each position of the double data array
     */
    private RowKey[] m_keys;


    /**
     * Constructor for a Fuzzy c-means algorithm (with no noise detection).
     *
     * @param nrClusters the number of cluster prototypes to use
     * @param fuzzifier allows the clusters to overlap
     */
    public FCMAlgorithmMemory(final int nrClusters, final double fuzzifier) {
        super(nrClusters, fuzzifier);
    }

    /**
     * Constructor for a Fuzzy c-means algorithm with noise detection. It can be
     * indicated, whether the delta value of the noise cluster should be updated
     * automatically or if it should be calculated automatically. The last
     * parameter specifies either the delta value or the lambda value, depending
     * on the boolean flag in the parameter before.
     *
     * @param nrClusters the number of clusters to use
     * @param fuzzifier the fuzzifier, controls how much the clusters can
     *            overlap
     * @param calculateDelta indicate whether delta should be calculated
     *            automatically
     * @param deltalambda the delta value, if the previous parameter is
     *            <code>false</code>, the lambda value otherwise
     */
    public FCMAlgorithmMemory(final int nrClusters, final double fuzzifier,
            final boolean calculateDelta, final double deltalambda) {
        super(nrClusters, fuzzifier, calculateDelta, deltalambda);
    }

    /**
     * Inits the cluster centers and the weight matrix. Must be called before
     * the iterations are carried out.
     *
     * @param nrRows number of rows in the DataTable
     * @param dimension the dimension of the table
     * @param table the table to use.
     */
    @Override
    public void init(final int nrRows, final int dimension,
            final DataTable table) {
       init(nrRows, dimension, table, new Random());
    }

    /**
     * Inits the cluster centers and the weight matrix. Must be called before
     * the iterations are carried out.
     *
     * @param nrRows number of rows in the DataTable
     * @param dimension the dimension of the table
     * @param table the table to use
     * @param random a random number generator for initializing the cluster centers
     * @since 2.8
     */
    @Override
    public void init(final int nrRows, final int dimension,
            final DataTable table, final Random random) {
       super.init(nrRows, dimension, table, random);
       initData(table);
        // TODO: checks on table: only double columns, nrRows, dimension
    }


    /**
     * Inits the cluster centers and the weight matrix. Must be called before
     * the iterations are carried out.
     *
     * @param keys the RowKeys for each data row.
     * @param data the DaaTable as 2 dimensional double array.
     */
    public void init(final RowKey[] keys, final double[][] data) {
       init(keys, data, new Random());
    }


    /**
     * Inits the cluster centers and the weight matrix. Must be called before
     * the iterations are carried out.
     *
     * @param keys the RowKeys for each data row
     * @param data the DaaTable as 2 dimensional double array
     * @param random a random number generator for initializing the cluster centers
     * @since 2.8
     */
    public void init(final RowKey[] keys, final double[][] data, final Random random) {
       assert (keys.length == data.length);
       super.init(data.length, data[0].length, null, random);
       m_data = data;
       m_keys = keys;
    }

    /*
     * Reads the data from the given DataTable in the doublearray m_data
     */
    private void initData(final DataTable table) {
        m_data = new double[getNrRows()][getDimension()];
        m_keys = new RowKey[getNrRows()];
        int curRow = 0;
        for (DataRow dRow : table) {
            m_keys[curRow] = dRow.getKey();
            for (int j = 0; j < dRow.getNumCells(); j++) {
                if (!(dRow.getCell(j).isMissing())) {
                    DoubleValue dv = (DoubleValue)dRow.getCell(j);
                    m_data[curRow][j] = dv.getDoubleValue();

                } else {
                    m_data[curRow][j] = 0;
                }
            }
            curRow++;
        }
    }

    /**
     * Please make sure to call init() first in order to guarantee that
     * the DataTable is converted.
     *
     * @return the input DataTable converted as a double array. If it has not
     * been produced yet, null is returned.
     */
    public double[][] getConvertedData() {
        assert m_data != null : "Please initialize first";
        return m_data;
    }

    /**
     * @param table DataTable to convert.
     * @return two-dimensional double array.
     */
    public double[][] getConvertedData(final DataTable table) {
        return getConvertedData(table, new Random());
    }

    /**
     * @param table DataTable to convert
     * @return two-dimensional double array
     * @param random a random number generator for initializing the cluster centers
     * @since 2.8
     */
    public double[][] getConvertedData(final DataTable table, final Random random) {
        init(table, random);
        return m_data;
    }


    /**
     * Please make sure to call init() first in order to guarantee that
     * the DataTable is converted.
     *
     * @return the RowKeys assigned to each position of the produced
     * double array
     * @see #getConvertedData()
     */
    public RowKey[] getRowKeys() {
        assert m_keys != null : "Please initialize first";
        return m_keys;
    }

    /**
     * Does one iteration in the Fuzzy c-means algorithm. First, the weight
     * matrix is updated and then the cluster prototypes are recalculated.
     *
     * @param exec execution context to cancel the execution
     * @return the total change in the cluster prototypes. Allows to decide
     *         whether the algorithm can be stopped.
     * @throws CanceledExecutionException if the operation is canceled
     */
    @Override
    public double doOneIteration(final ExecutionContext exec)
            throws CanceledExecutionException {
        assert (m_data != null);
        if (exec != null) {
            exec.checkCanceled();
        }
        updateWeightMatrix(m_data);
        setTotalChange(0.0);
        updateClusterCenters(m_data);
        return getTotalChange();
    }

    /*
     * The update method for the weight matrix
     */
    private void updateWeightMatrix(final double[][] data) {
       int nrRows = getNrRows();
       double[][] clusters = getClusters();
       double[][] weightMatrix = getweightMatrix();
       boolean noise = isNoise();
       double fuzzifier = getFuzzifier();
       double delta = getDelta();
        for (int currentRow = 0; currentRow < nrRows; currentRow++) {
            double[] row = data[currentRow];
            int i = 0;

            // first check if the actual row is equal to a cluster center
            int sameCluster = -1;

            int nrClusters = (noise) ? clusters.length - 1
                    : clusters.length;
            while ((sameCluster < 0) && (i < nrClusters)) {
                for (int j = 0; j < row.length; j++) {
                    if (row[j] == clusters[i][j]) {
                        sameCluster = i;
                    } else {
                        sameCluster = -1;
                        break;
                    }
                }
                i++;
            }

            /*
             * The weight of a data point is 1 if it is exactly on the position
             * of the cluster, in this case 0 for the others
             */
            if (sameCluster >= 0) {
                for (i = 0; i < weightMatrix[0].length; i++) {
                    if (i != sameCluster) {
                        setWeightMatrixValue(currentRow, i, 0);
                    } else {
                        setWeightMatrixValue(currentRow, i, 1);
                    }
                }
            } else {
                // calculate the fuzzy membership to each cluster
                for (int j = 0; j < clusters.length; j++) {
                    // for each cluster
                    double distNumerator = 0;
                    if (noise && j == clusters.length - 1) {
                        distNumerator = Math.pow(delta, 2.0);
                    } else {
                        distNumerator = getDistance(clusters[j], row);
                    }
                    double sum = 0;
                    for (int k = 0; k < clusters.length; k++) {
                        double distance = 0;
                        if (noise && k == clusters.length - 1) {
                            distance = Math.pow(delta, 2.0);
                        } else {
                            distance = getDistance(clusters[k], row);
                        }
                        sum += Math.pow((distNumerator / distance),
                                (1.0 / (fuzzifier - 1.0)));
                    }
                    setWeightMatrixValue(currentRow, j, (1 / sum));
                }
            }
        }
    }

    /*
     * Helper method for the quadratic distance between two double-arrays.
     *
     */
    private double getDistance(final double[] vector1, final double[] vector2) {
        double distance = 0.0;
        assert (vector1.length == vector2.length);
        for (int i = 0; i < vector1.length; i++) {
            double diff = 0;
            diff = vector1[i] - vector2[i];
            distance += diff * diff;
        }
        return distance;
    }

    /*
     * The update method for the cluster centers
     */
    private void updateClusterCenters(final double[][] data) {
        int dimension = getDimension();
        int nrRows = getNrRows();
        int nrClusters = getNrClusters();
        double[][] clusters = getClusters();
        double[][] weightMatrix = getweightMatrix();
        double fuzzifier = getFuzzifier();
        boolean noise = isNoise();
        double[] sumNumerator = new double[dimension];
        double sumDenominator = 0;
        double sumupdate = 0;
        // for each cluster center
        for (int c = 0; c < nrClusters; c++) {
            if (noise && (c == nrClusters - 1)) {
                break;
            }
            for (int j = 0; j < dimension; j++) {
                sumNumerator[j] = 0;
            }
            sumDenominator = 0;

            int i = 0;
           for (int currentRow = 0; currentRow < nrRows; currentRow++) {
                double[] row = data[currentRow];
                // for all attributes in X
                for (int j = 0; j < dimension; j++) {

                            sumNumerator[j] += Math.pow(weightMatrix[i][c],
                                    fuzzifier)
                                    * row[j];

                }
                sumDenominator += Math.pow(weightMatrix[i][c], fuzzifier);
                i++;
                if (noise && isCalculateDelta()) {
                    sumupdate += getDistance(clusters[c], row);
                }
            } // end while for all datarows sum up
            for (int j = 0; j < dimension; j++) {
                double newValue = sumNumerator[j] / sumDenominator;
                addTotalChange(Math.abs(clusters[c][j] - newValue));
                setClusterValue(c, j, newValue);
            }
        }

        /*
         * Update the delta-value automatically if choosen.
         */
        if (noise && isCalculateDelta()) {
            setDelta(Math.sqrt(getLambda()
                    * (sumupdate / (nrRows * (clusters.length - 1)))));
        }

    } // end update cluster centers



}
