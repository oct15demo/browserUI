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
 *   21.01.2010 (hofer): created
 */
package org.knime.base.node.mine.regression.logistic.learner3;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.knime.base.node.mine.regression.pmmlgreg.PMMLGeneralRegressionTranslator;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.NominalValue;
import org.knime.core.data.vector.bitvector.BitVectorValue;
import org.knime.core.data.vector.bytevector.ByteVectorValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.pmml.PMMLPortObject;
import org.knime.core.node.port.pmml.PMMLPortObjectSpec;
import org.knime.core.node.util.filter.InputFilter;
import org.knime.core.node.util.filter.NameFilterConfiguration;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterConfiguration;

/**
 * NodeModel to the logistic regression learner node. It delegates the calculation to <code>LogRegLearner</code>.
 *
 * @author Heiko Hofer
 * @author Gabor Bakos
 * @since 3.1
 */
public final class LogRegLearnerNodeModel extends NodeModel {
    private final LogRegLearnerSettings m_settings;

    /** The learned regression model. */
    private LogisticRegressionContent m_content;

    /** Inits a new node model, it will have 1 data input, 1 model and 1 data output. */
    public LogRegLearnerNodeModel() {
        super(new PortType[]{BufferedDataTable.TYPE}, new PortType[]{PMMLPortObject.TYPE, BufferedDataTable.TYPE});
        m_settings = new LogRegLearnerSettings();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_settings.saveSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        LogRegLearner.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_settings.loadSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        final BufferedDataTable data = (BufferedDataTable)inObjects[0];
        final DataTableSpec tableSpec = data.getDataTableSpec();

        final LogRegLearner learner = new LogRegLearner(new PortObjectSpec[]{tableSpec}, m_settings);
        m_content = learner.execute(new PortObject[]{data}, exec);
        String warn = learner.getWarningMessage();
        if (warn != null) {
            setWarningMessage(warn);
        }

        // second argument is ignored since we provide a spec
        PMMLPortObject outPMMLPort =
            new PMMLPortObject((PMMLPortObjectSpec)learner.getOutputSpec()[0], null, tableSpec);
        PMMLGeneralRegressionTranslator trans =
            new PMMLGeneralRegressionTranslator(m_content.createGeneralRegressionContent());
        outPMMLPort.addModelTranslater(trans);
        return new PortObject[]{outPMMLPort, m_content.createTablePortObject(exec)};
    }

    /** {@inheritDoc} */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        LogRegLearner learner = new LogRegLearner(inSpecs, m_settings);
        return learner.getOutputSpec();
    }

    /**
     * Returns <code>true</code> if model is available, i.e. node has been executed.
     *
     * @return if model has been executed
     */
    protected boolean isDataAvailable() {
        return m_content != null;
    }

    /**
     * Get all parameters to the currently learned model.
     *
     * @return a reference to the current values
     */
    public LogisticRegressionContent getRegressionContent() {
        return m_content;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        m_content = null;
    }

    private static final String FILE_SAVE = "model.xml.gz";

    private static final String CFG_SETTINGS = "internals";

    private static final String CFG_LOGREG_CONTENT = "logreg_content";

    private static final String CFG_SPEC = "spec";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        File inFile = new File(internDir, FILE_SAVE);
        ModelContentRO c =
            ModelContent.loadFromXML(new BufferedInputStream(new GZIPInputStream(new FileInputStream(inFile))));
        try {
            ModelContentRO specContent = c.getModelContent(CFG_SPEC);
            DataTableSpec spec = DataTableSpec.load(specContent);
            ModelContentRO parContent = c.getModelContent(CFG_LOGREG_CONTENT);
            m_content = LogisticRegressionContent.load(parContent, spec);
        } catch (InvalidSettingsException ise) {
            IOException ioe = new IOException("Unable to restore state: " + ise.getMessage());
            ioe.initCause(ise);
            throw ioe;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        ModelContent content = new ModelContent(CFG_SETTINGS);
        ModelContentWO specContent = content.addModelContent(CFG_SPEC);
        m_content.getSpec().getDataTableSpec().save(specContent);
        ModelContentWO parContent = content.addModelContent(CFG_LOGREG_CONTENT);
        m_content.save(parContent);
        File outFile = new File(internDir, FILE_SAVE);
        content.saveToXML(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(outFile))));
    }

    /**
     * A new configuration to store the settings. Also enables the type filter.
     *
     * @return A new {@link DataColumnSpecFilterConfiguration}.
     */
    static final DataColumnSpecFilterConfiguration createDCSFilterConfiguration() {
        return new DataColumnSpecFilterConfiguration("column-filter", new InputFilter<DataColumnSpec>() {
            @Override
            public boolean include(final DataColumnSpec name) {
                final DataType type = name.getType();
                return type.isCompatible(DoubleValue.class) || type.isCompatible(BitVectorValue.class)
                    || type.isCompatible(ByteVectorValue.class) || type.isCompatible(NominalValue.class);
            }
        }, NameFilterConfiguration.FILTER_BY_NAMEPATTERN | DataColumnSpecFilterConfiguration.FILTER_BY_DATATYPE);
    }

}
