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
 *   30.10.2005 (mb): created
 */
package org.knime.base.node.io.portobject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortUtil;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.util.FileUtil;

/**
 * Read ModelContent object from file.
 *
 * @author M. Berthold, University of Konstanz
 */
class PortObjectReaderNodeModel extends NodeModel {

    /** key for filename entry in config object. */
    static final String FILENAME = "filename";

    private final SettingsModelString m_fileName =
            new SettingsModelString(FILENAME, null);

    /**
     * Constructor: Create new NodeModel with only one Model Input Port.
     * @param type Type of output
     */
    PortObjectReaderNodeModel(final PortType type) {
        super(new PortType[0], new PortType[]{type});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_fileName.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_fileName.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_fileName.loadSettingsFrom(settings);
    }

    /**
     * Execute does nothing - the reading of the file and writing to the
     * NodeSettings object has already happened during savePredictorParams.
     *
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] data,
            final ExecutionContext exec) throws Exception {
        String fileName = m_fileName.getStringValue();
        InputStream inStream = null;
        try {
            inStream = FileUtil.openInputStream(fileName);
            return new PortObject[]{
                    PortUtil.readObjectFromStream(inStream, exec)};
        } catch (IOException ioe) {
            throw new InvalidSettingsException(
                    "Failed to extract object from \""
                    + fileName + "\": " + ioe.getMessage(), ioe);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    NodeLogger.getLogger(getClass()).debug(
                            "Failed closing stream", e);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void reset() {
    }

    /** {@inheritDoc} */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        final String fileName = m_fileName.getStringValue();
        String warning = CheckUtils.checkSourceFile(fileName);
        if (warning != null) {
            setWarningMessage(warning);
        }

        InputStream inStream = null;
        try {
            inStream = FileUtil.openInputStream(fileName);
            return new PortObjectSpec[]{
                    PortUtil.readObjectSpecFromStream(inStream)};
        } catch (IOException ioe) {
            throw new InvalidSettingsException(
                    "Failed to extract specification from \""
                    + fileName + "\": " + ioe.getMessage(), ioe);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    NodeLogger.getLogger(getClass()).debug(
                            "Failed closing stream", e);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // nothing to do here
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // nothing to do here
    }

}
