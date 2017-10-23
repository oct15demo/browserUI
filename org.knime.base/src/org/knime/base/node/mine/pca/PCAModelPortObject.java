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
 *   04.10.2006 (uwe): created
 */

package org.knime.base.node.mine.pca;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObject;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

/**
 * Port model object transporting the pca transformation.
 *
 * @author uwe, University of Konstanz
 */
public class PCAModelPortObject extends AbstractSimplePortObject {
    /**
     * @noreference This class is not intended to be referenced by clients.
     * @since 3.0
     */
    public static final class Serializer extends AbstractSimplePortObjectSerializer<PCAModelPortObject> {}

    private static final String EIGENVECTOR_ROW_KEYPREFIX = "eigenvectorRow";

    private static final String EIGENVALUES_KEY = "eigenvalues";

    private static final String COLUMN_NAMES_KEY = "columnNames";

    private static final String CENTER_KEY = "center";

    /**
     * Define port type of objects of this class when used as PortObjects.
     */
    public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(PCAModelPortObject.class);

    private String[] m_inputColumnNames;

    private double[] m_center;

    private double[][] m_eigenVectors;

    private double[] m_eigenvalues;

    /**
     * empty constructor.
     */
    public PCAModelPortObject() {
        //
    }

    /**
     * construct port model object with values.
     *
     * @param eigenVectors eigenvectors of pca matrix
     * @param eigenvalues eigenvalues of pca matrix
     * @param inputColumnNames names of input columns
     * @param center center of original data (data must be centered)
     */
    public PCAModelPortObject(final double[][] eigenVectors,
            final double[] eigenvalues, final String[] inputColumnNames,
            final double[] center) {

        m_eigenVectors = eigenVectors;
        m_eigenvalues = eigenvalues;
        m_inputColumnNames = inputColumnNames;
        m_center = center;

    }

    /**
     * get center of input data (for centering test data).
     *
     * @return center
     */
    public double[] getCenter() {
        return m_center;
    }

    /**
     * get names of input columns.
     *
     * @return names of input columns
     */
    public String[] getInputColumnNames() {
        return m_inputColumnNames;
    }

    /**
     * @return eigenvalues of pca matrix
     */
    public double[] getEigenvalues() {
        return m_eigenvalues;
    }

    /**
     * @return eigenvectors of pca matrix
     */
    public double[][] getEigenVectors() {
        return m_eigenVectors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PortObjectSpec getSpec() {

        final PCAModelPortObjectSpec spec =
                new PCAModelPortObjectSpec(m_inputColumnNames);
        if (m_eigenvalues != null) {
            spec.setEigenValues(m_eigenvalues);
        }
        return spec;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary() {

        return m_eigenvalues.length + " principal components";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JComponent[] getViews() {
        final String description = "<html>" + getSummary() + "</html>";

        final JLabel label = new JLabel(description);
        label.setName("PCA port");
        return new JComponent[]{label};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void load(final ModelContentRO model, final PortObjectSpec spec,
            final ExecutionMonitor exec) throws InvalidSettingsException,
            CanceledExecutionException {
        m_center = model.getDoubleArray(CENTER_KEY);
        m_inputColumnNames = model.getStringArray(COLUMN_NAMES_KEY);
        m_eigenvalues = model.getDoubleArray(EIGENVALUES_KEY);
        m_eigenVectors = new double[m_eigenvalues.length][];
        for (int i = 0; i < m_eigenVectors.length; i++) {
            m_eigenVectors[i] =
                    model.getDoubleArray(EIGENVECTOR_ROW_KEYPREFIX + i);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void save(final ModelContentWO model, final ExecutionMonitor exec)
            throws CanceledExecutionException {
        model.addDoubleArray(CENTER_KEY, m_center);
        model.addStringArray(COLUMN_NAMES_KEY, m_inputColumnNames);
        model.addDoubleArray(EIGENVALUES_KEY, m_eigenvalues);
        for (int i = 0; i < m_eigenVectors.length; i++) {
            model.addDoubleArray(EIGENVECTOR_ROW_KEYPREFIX + i,
                    m_eigenVectors[i]);
        }
    }

}
