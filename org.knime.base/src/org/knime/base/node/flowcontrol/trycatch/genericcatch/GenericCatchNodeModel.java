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
package org.knime.base.node.flowcontrol.trycatch.genericcatch;

import java.io.File;
import java.io.IOException;

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
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;
import org.knime.core.node.port.inactive.InactiveBranchConsumer;
import org.knime.core.node.port.inactive.InactiveBranchPortObject;
import org.knime.core.node.port.inactive.InactiveBranchPortObjectSpec;
import org.knime.core.node.workflow.FlowTryCatchContext;
import org.knime.core.node.workflow.ScopeEndNode;

/**
 * End of an Try-Catch Enclosure. Takes the first input if active (i.e. did
 * not fail during execution) or the second one if not. Hence the second port
 * can be fed with the default object used downstream from here when the original
 * branch failed.
 *
 * @author M. Berthold, University of Konstanz
 */
public class GenericCatchNodeModel extends NodeModel
implements ScopeEndNode<FlowTryCatchContext>, InactiveBranchConsumer {

//    private static final NodeLogger LOGGER = NodeLogger.getLogger(GenericCatchNodeModel.class);

    // new since 2.11
    private SettingsModelBoolean m_alwaysPopulate = GenericCatchNodeDialog.getAlwaysPopulate();
    private SettingsModelString m_defaultText = GenericCatchNodeDialog.getDefaultMessage();
    private SettingsModelString m_defaultVariable = GenericCatchNodeDialog.getDefaultVariable();
    private SettingsModelString m_defaultStackTrace = GenericCatchNodeDialog.getDefaultStackTrace();

    /**
     * Two inputs, one output.
     *
     * @param ptype type of ports.
     */
    protected GenericCatchNodeModel(final PortType ptype) {
        super(new PortType[] {ptype, ptype}, new PortType[] {ptype, FlowVariablePortObject.TYPE});
    }

    /** Generic constructor.
     */
    protected GenericCatchNodeModel() {
        this(PortObject.TYPE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        if (m_alwaysPopulate.getBooleanValue()) {
            pushFlowVariableString("FailingNode", m_defaultVariable.getStringValue());
            pushFlowVariableString("FailingNodeMessage", m_defaultText.getStringValue());
            pushFlowVariableString("FailingNodeStackTrace", m_defaultStackTrace.getStringValue());
        }

        if (!(inSpecs[0] instanceof InactiveBranchPortObjectSpec)) {
            // main branch is active - no failure so far...
            return new PortObjectSpec[]{inSpecs[0], FlowVariablePortObjectSpec.INSTANCE};
        }
        // main branch inactive, grab spec from alternative (default) input
        return new PortObjectSpec[]{inSpecs[1], FlowVariablePortObjectSpec.INSTANCE};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {
        if (!(inData[0] instanceof InactiveBranchPortObject)) {
            // main branch is active - no failure so far...
            return new PortObject[]{inData[0], FlowVariablePortObject.INSTANCE};
        }
        // main branch inactive, grab spec from alternative (default) input
        // and push error reasons on stack (they come from the ScopeObject
        // which will we removed after this node, closing the scope).
        FlowTryCatchContext ftcc = getFlowContext();
        if ((ftcc != null) && (ftcc.hasErrorCaught())) {
            pushFlowVariableString("FailingNode", ftcc.getNode());
            pushFlowVariableString("FailingNodeMessage", ftcc.getReason());
            pushFlowVariableString("FailingNodeStackTrace", ftcc.getStacktrace());
        } else if (m_alwaysPopulate.getBooleanValue()) {
            pushFlowVariableString("FailingNode", m_defaultVariable.getStringValue());
            pushFlowVariableString("FailingNodeMessage", m_defaultText.getStringValue());
            pushFlowVariableString("FailingNodeStackTrace", m_defaultStackTrace.getStringValue());
        }
        return new PortObject[]{inData[1], FlowVariablePortObject.INSTANCE};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_alwaysPopulate.saveSettingsTo(settings);
        m_defaultText.saveSettingsTo(settings);
        m_defaultVariable.saveSettingsTo(settings);
        m_defaultStackTrace.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        if (settings.containsKey(m_defaultText.getKey())) {
            m_alwaysPopulate.validateSettings(settings);
            m_defaultText.validateSettings(settings);
            m_defaultVariable.validateSettings(settings);
            m_defaultStackTrace.validateSettings(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        if (settings.containsKey(m_defaultText.getKey())) {
            m_alwaysPopulate.loadSettingsFrom(settings);
            m_defaultText.loadSettingsFrom(settings);
            m_defaultVariable.loadSettingsFrom(settings);
            m_defaultStackTrace.loadSettingsFrom(settings);
        }
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
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
    }

}
