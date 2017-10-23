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
 *   18.09.2008 (ohl): created
 */
package org.knime.base.node.io.filereader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataContainer;
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
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.util.DuplicateKeyException;
import org.knime.core.util.tokenizer.SettingsStatus;

/**
 *
 * @author ohl, University of Konstanz
 * @deprecated the File Reader now has the same functionality
 */
@Deprecated
public class VariableFileReaderNodeModel extends NodeModel {

    /** The node logger fot this class. */
    private static final NodeLogger LOGGER =
            NodeLogger.getLogger(VariableFileReaderNodeModel.class);

    /*
     * The settings structure used to create a DataTable from during execute.
     */
    private VariableFileReaderNodeSettings m_frSettings;

    /**
     * Creates a new model that creates and holds a Filetable.
     */
    public VariableFileReaderNodeModel() {
        super(new PortType[]{FlowVariablePortObject.TYPE},
                new PortType[]{BufferedDataTable.TYPE});
        m_frSettings = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // m_frSettings = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
        Map<String, FlowVariable> stack =
                createStack(m_frSettings.getVariableName());
        VariableFileReaderNodeSettings settings =
                m_frSettings.createSettingsFrom(stack);

        LOGGER.info("Preparing to read from '"
                + m_frSettings.getDataFileLocation().toString() + "'.");

        // check again the settings - especially file existence (under Linux
        // files could be deleted/renamed since last config-call...
        SettingsStatus status = settings.getStatusOfSettings(true, null);
        if (status.getNumOfErrors() > 0) {
            throw new InvalidSettingsException(status.getAllErrorMessages(10));
        }

        DataTableSpec tSpec = settings.createDataTableSpec();

        FileTable fTable =
                new FileTable(tSpec, settings, settings.getSkippedColumns(),
                        exec);

        // create a DataContainer and fill it with the rows read. It is faster
        // then reading the file every time (for each row iterator), and it
        // collects the domain for each column for us. Also, if things fail,
        // the error message is printed during file reader execution (were it
        // belongs to) and not some time later when a node uses the row
        // iterator from the file table.

        BufferedDataContainer c =
                exec.createDataContainer(fTable.getDataTableSpec(), /* initDomain= */
                true);
        int row = 0;
        FileRowIterator it = fTable.iterator();
        try {
            if (it.getZipEntryName() != null) {
                // seems we are reading a ZIP archive.
                LOGGER.info("Reading entry '" + it.getZipEntryName()
                        + "' from the specified ZIP archive.");
            }

            while (it.hasNext()) {
                row++;
                DataRow next = it.next();
                String message =
                        "Caching row #" + row + " (\"" + next.getKey() + "\")";
                exec.setMessage(message);
                exec.checkCanceled();
                c.addRowToTable(next);
            }

            if (it.zippedSourceHasMoreEntries()) {
                // after reading til the end of the file this returns a valid
                // result
                setWarningMessage("Source is a ZIP archive with multiple "
                        + "entries. Only reading first entry!");
            }

        } catch (DuplicateKeyException dke) {
            String msg = dke.getMessage();
            if (msg == null) {
                msg = "Duplicate row IDs";
            }
            msg += ". Consider making IDs unique in the advanced settings.";
            DuplicateKeyException newDKE = new DuplicateKeyException(msg);
            newDKE.initCause(dke);
            throw newDKE;
        } finally {
            c.close();
        }

        // user settings allow for truncating the table
        if (it.iteratorEndedEarly()) {
            setWarningMessage("Data was truncated due to user settings.");
        }
        BufferedDataTable out = c.getTable();

        // closes all sources.
        fTable.dispose();

        return new BufferedDataTable[]{out};
    }

    private final Map<String, FlowVariable> createStack(final String varName) {
        String loc = peekFlowVariableString(varName);
        FlowVariable scopeVar = new FlowVariable(varName, loc);
        Map<String, FlowVariable> stack = new HashMap<String, FlowVariable>();
        if (scopeVar != null) {
            stack.put(varName, scopeVar);
        }
        return stack;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {

        if (m_frSettings == null) {
            throw new InvalidSettingsException("No Settings available.");
        }

        // see if settings are good enough for execution
        // don't check file existence. File location is set with variable
        // in execute. (bug2415)
        SettingsStatus status = m_frSettings.getStatusOfSettings(false, null);
        if (status.getNumOfErrors() == 0) {
            VariableFileReaderNodeSettings withLoc;
            try {
                withLoc =
                        m_frSettings
                                .createSettingsFrom(createStack(m_frSettings
                                        .getVariableName()));
            } catch (Exception e) {
                throw new InvalidSettingsException(e.getMessage(), e);
            }
            return new DataTableSpec[]{withLoc.createDataTableSpec()};
        }

        throw new InvalidSettingsException(status.getAllErrorMessages(0));
    }

    /*
     * validates the settings object, or reads its settings from it. Depending
     * on the specified value of the 'validateOnly' parameter.
     */
    private void readSettingsFromConfiguration(final NodeSettingsRO settings,
            final boolean validateOnly) throws InvalidSettingsException {
        if (settings == null) {
            throw new NullPointerException(
                    "Can't read filereader node settings"
                            + " from null config object");
        }
        // will puke and die if config is not readable.
        VariableFileReaderNodeSettings newSettings =
                new VariableFileReaderNodeSettings(settings);

        // check consistency of settings.
        SettingsStatus status = newSettings.getStatusOfSettings();
        if (status.getNumOfErrors() > 0) {
            throw new InvalidSettingsException(status.getAllErrorMessages(0));
        }

        if (!validateOnly) {
            // everything looks good - take over the new settings.
            m_frSettings = newSettings;
        }
    }

    /**
     * Reads in all user settings of the model. If they are incomplete,
     * inconsistent, or in any way invalid it will throw an exception.
     *
     * @param settings the object to read the user settings from. Must not be
     *            <code>null</code> and must be validated with the validate
     *            method below.
     * @throws InvalidSettingsException if the settings are incorrect - which
     *             should not happen as they are supposed to be validated
     *             before.
     * @see NodeModel#loadValidatedSettingsFrom(NodeSettingsRO)
     * @see NodeModel#validateSettings(NodeSettingsRO)
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        readSettingsFromConfiguration(settings, /* validateOnly = */false);
    }

    /**
     * Writes the current user settings into a configuration object.
     *
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {

        if (settings == null) {
            throw new NullPointerException("Can't write filereader node "
                    + "settings into null config object.");
        }
        VariableFileReaderNodeSettings s = m_frSettings;

        if (s == null) {
            s = new VariableFileReaderNodeSettings();
        }
        s.saveToConfiguration(settings);

    }

    /**
     * Checks all user settings in the specified spec object. If they are
     * incomplete, inconsistent, or in any way invalid it will throw an
     * exception.
     *
     * @param settings the object to read the user settings from. Must not be
     *            <code>null</code>.
     * @throws InvalidSettingsException if the settings in the specified object
     *             are incomplete, inconsistent, or in any way invalid.
     * @see NodeModel#validateSettings(NodeSettingsRO)
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        readSettingsFromConfiguration(settings, /* validateOnly = */true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        /*
         * This is a special "deal" for the file reader: The file reader, if
         * previously executed, has data at it's output - even if the file that
         * was read doesn't exist anymore. In order to warn the user that the
         * data cannot be recreated we check here if the file exists and set a
         * warning message if it doesn't.
         */
        if (m_frSettings == null) {
            // no settings - no checking.
            return;
        }

        URL location = m_frSettings.getDataFileLocation();
        try {
            if ((location == null)
                    || !location.toString().startsWith("file://")) {
                // We can only check files. Other protocols are ignored.
                return;
            }

            if (location.openStream() == null) {
                setWarningMessage("The file '" + location.toString()
                        + "' can't be accessed anymore!");
            }
        } catch (IOException ioe) {
            setWarningMessage("The file '" + location.toString()
                    + "' can't be accessed anymore!");
        } catch (NullPointerException npe) {
            // thats a bug in the windows open stream
            // a path like c:\blah\ \ (space as dir) causes a NPE.
            setWarningMessage("The file '" + location.toString()
                    + "' can't be accessed anymore!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // no internals to save.
        return;
    }

}
