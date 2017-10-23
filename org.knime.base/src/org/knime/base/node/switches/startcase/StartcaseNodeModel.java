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
 *   Sept 17 2008 (mb): created (from wiswedel's TableToVariableNode)
 */
package org.knime.base.node.switches.startcase;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.inactive.InactiveBranchPortObject;
import org.knime.core.node.port.inactive.InactiveBranchPortObjectSpec;

/**
 * Start of a CASE Statement. Takes the table from one branch and
 * outputs it to exactly one outport.
 *
 * @author M. Berthold, University of Konstanz
 */
public class StartcaseNodeModel extends NodeModel {

    private static final String ACTIVATE_OUTPUT_CFG = "activate_all_outputs_during_configure";

    private SettingsModelString m_selectedPort = createChoiceModel();
    private final SettingsModelBoolean m_activateAllOutputsDuringConfigureModel =
            createActivateAllOutputsDuringConfigureModel();
    /**
     * One input, four output.
     */
    protected StartcaseNodeModel() {
        super(1, StartcaseNodeDialog.options.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        int index = Integer.parseInt(m_selectedPort.getStringValue());
        if ((index < 0) || (index >= getNrOutPorts())) {
            throw new InvalidSettingsException("Invalid Port Index " + index);
        }
        PortObjectSpec[] outspecs = new PortObjectSpec[getNrOutPorts()];
        PortObjectSpec defSpec = m_activateAllOutputsDuringConfigureModel.getBooleanValue()
                ? inSpecs[0] : InactiveBranchPortObjectSpec.INSTANCE;
        Arrays.fill(outspecs, defSpec);
        outspecs[index] = inSpecs[0];
        return outspecs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
        int index = Integer.parseInt(m_selectedPort.getStringValue());
        if ((index < 0) || (index >= getNrOutPorts())) {
            throw new IllegalArgumentException("Invalid Port Index.");
        }
        PortObject[] outs = new PortObject[getNrOutPorts()];
        Arrays.fill(outs, InactiveBranchPortObject.INSTANCE);
        outs[index] = inData[0];
        return outs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_selectedPort.saveSettingsTo(settings);
        m_activateAllOutputsDuringConfigureModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_selectedPort.validateSettings(settings);
        if (settings.containsKey(ACTIVATE_OUTPUT_CFG)) { // added in 2.12
            m_activateAllOutputsDuringConfigureModel.validateSettings(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        if (settings.containsKey(ACTIVATE_OUTPUT_CFG)) { // added 2.12
            m_activateAllOutputsDuringConfigureModel.loadSettingsFrom(settings);
        } else {
            m_activateAllOutputsDuringConfigureModel.setBooleanValue(false);
        }
        m_selectedPort.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // empty
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // empty
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // empty
    }

    /**
    *
    * @return name of PMML file model
    */
   static SettingsModelString createChoiceModel() {
       return new SettingsModelString("PortIndex", "0");
   }

   static SettingsModelBoolean createActivateAllOutputsDuringConfigureModel() {
       return new SettingsModelBoolean(ACTIVATE_OUTPUT_CFG, true);
   }

}
