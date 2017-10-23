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
package org.knime.base.node.io.pmml.read;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.dmg.pmml.TransformationDictionaryDocument.TransformationDictionary;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeCreationContext;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.pmml.PMMLPortObject;
import org.knime.core.node.port.pmml.PMMLPortObjectSpec;
import org.knime.core.node.port.pmml.PMMLPortObjectSpecCreator;
import org.knime.core.node.util.CheckUtils;

/**
 *
 * @author Fabian Dill, University of Konstanz
 */
public class PMMLReaderNodeModel extends NodeModel {

    private final SettingsModelString m_file = PMMLReaderNodeDialog
            .createFileChooserModel();


    private PMMLPortObject m_pmmlPort;
    private boolean m_hasPMMLIn;

    /**
     * Default constructor for a PMML Reader with optional input port.
     */
    public PMMLReaderNodeModel() {
        this(true);
    }

    /**
     * Create a new PMML reader node model with optional PMML in port.
     * @param hasPMMLIn if true, the node has an optional PMML input
     * @since 3.2
     */
    public PMMLReaderNodeModel(final boolean hasPMMLIn) {
        super(hasPMMLIn ? new PortType[]{PMMLPortObject.TYPE_OPTIONAL}
            : new PortType[0], new PortType[]{PMMLPortObject.TYPE});
        m_hasPMMLIn = hasPMMLIn;
    }

    /**
     * Called by the node factory if the node is instantiated due to a file
     * drop.
     *
     * @param context the node creation context
     */
    public PMMLReaderNodeModel(final NodeCreationContext context) {
        this(false);
        m_file.setStringValue(context.getUrl().toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        // read the data dictionary and the mining schema and create a
        // PMMLPortObjectSpec
        String fileS = m_file.getStringValue();
        String warning = CheckUtils.checkSourceFile(fileS);
        if (warning != null) {
            setWarningMessage(warning);
        }

        URL url = getURLFromSettings(fileS);
        try {
            PMMLImport pmmlImport = new PMMLImport(url, false);
            m_pmmlPort = pmmlImport.getPortObject();
        } catch (IllegalArgumentException e) {
            String msg = "File \"" + url + "\" is not a valid PMML file:\n" + e.getMessage();
            setWarningMessage(msg);
            throw new InvalidSettingsException(msg);
        } catch (XmlException e) {
            throw new InvalidSettingsException(e);
        } catch (IOException e) {
            throw new InvalidSettingsException(e);
        }
        PMMLPortObjectSpec parsedSpec = m_pmmlPort.getSpec();
        PMMLPortObjectSpec outSpec = createPMMLOutSpec(
                m_hasPMMLIn ? (PMMLPortObjectSpec)inSpecs[0] : null, parsedSpec);
        return new PortObjectSpec[]{outSpec};
    }

    /**
     * @param inModelSpec the spec of the optional PMML in port
     * @param parsedSpec the spec of the parsed PMML document
     * @return the merged {@link PMMLPortObjectSpec}
     */
    private PMMLPortObjectSpec createPMMLOutSpec(
            final PMMLPortObjectSpec inModelSpec,
            final PMMLPortObjectSpec parsedSpec)
            throws InvalidSettingsException {
        PMMLPortObjectSpec outSpec = parsedSpec;
        if (inModelSpec != null) {
            List<String> preprocCols = inModelSpec.getPreprocessingFields();
            for (String colName : preprocCols) {
                if (!parsedSpec.getActiveFields().contains(colName)) {
                    throw new InvalidSettingsException("Preprocessing column "
                            + colName
                            + " is not contained in the read PMML file.");
                }
            }
            PMMLPortObjectSpecCreator creator = new PMMLPortObjectSpecCreator(
                    parsedSpec);
            creator.addPreprocColNames(preprocCols);
            outSpec = creator.createSpec();
        }
        return outSpec;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
        PMMLPortObject inPort = m_hasPMMLIn ? (PMMLPortObject)inData[0] : null;
        if (inPort != null) {
            TransformationDictionary dict
                    = TransformationDictionary.Factory.newInstance();
            dict.setDerivedFieldArray(inPort.getDerivedFields());
            m_pmmlPort.addGlobalTransformations(dict);
            m_pmmlPort.moveGlobalTransformationsToModel();
        }
        return new PortObject[]{m_pmmlPort};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_file.loadSettingsFrom(settings);
    }

    /** Convert argument string to a URL.
      * @param fileS The file string (a url or a file path)
      * @return The url (if it's a path then file access is checked)
      * @throws InvalidSettingsException If no valid url given.
      */
    static URL getURLFromSettings(final String fileS)
        throws InvalidSettingsException {
        if (fileS == null || fileS.length() == 0) {
            throw new InvalidSettingsException("No file/url specified");
        }

        try {
            return new URL(fileS);
        } catch (MalformedURLException e) {
            File tmp = new File(fileS);
            if (tmp.isFile() && tmp.canRead()) {
                try  {
                    return tmp.getAbsoluteFile().toURI().toURL();
                } catch (MalformedURLException e1) {
                    throw new InvalidSettingsException(e1);
                }
            }
            throw new InvalidSettingsException("File/URL \"" + fileS
                       + "\" cannot be parsed as a URL or represents a non exising file location");
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // nothing to unregister
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_file.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_file.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // no internals to load
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // no internals to save
    }
}
