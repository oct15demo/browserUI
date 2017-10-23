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
 * Created on Oct 5, 2013 by wiswedel
 */
package org.knime.core.node.workflow;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.knime.core.data.container.ContainerTable;
import org.knime.core.data.filestore.internal.FileStoreHandlerRepository;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.wizard.WizardNodeLayoutInfo;
import org.knime.core.node.workflow.WorkflowPersistor.LoadResult;
import org.knime.core.node.workflow.WorkflowPersistor.WorkflowPortTemplate;

/**
 *
 * @author wiswedel
 * @since 2.10
 */
public class CopySubNodeContainerPersistor
    extends CopySingleNodeContainerPersistor implements SubNodeContainerPersistor {

    private final WorkflowPersistor m_workflowPersistor;
    private final WorkflowPortTemplate[] m_inPortTemplates;
    private final WorkflowPortTemplate[] m_outPortTemplates;
    private final int m_virtualInNodeIDSuffix;
    private final int m_virtualOutNodeIDSuffix;
    private final Map<Integer, WizardNodeLayoutInfo> m_layoutInfo;
    private final String m_layoutJSONString;
    private final MetaNodeTemplateInformation m_templateInformation;

    /**
     * @param original
     * @param tableRep
     * @param fileStoreHandlerRepository
     * @param preserveDeletableFlags
     * @param isUndoableDeleteCommand
     */
    public CopySubNodeContainerPersistor(final SubNodeContainer original,
        final HashMap<Integer, ContainerTable> tableRep,
        final FileStoreHandlerRepository fileStoreHandlerRepository, final boolean preserveDeletableFlags,
        final boolean isUndoableDeleteCommand) {
        super(original, preserveDeletableFlags, isUndoableDeleteCommand);
        m_workflowPersistor = new CopyWorkflowPersistor(original.getWorkflowManager(), tableRep,
            fileStoreHandlerRepository, preserveDeletableFlags, isUndoableDeleteCommand) {
            @Override
            public void postLoad(final WorkflowManager wfm, final LoadResult loadResult) {
                NodeContainerParent ncParent = wfm.getDirectNCParent();
                SubNodeContainer subnode = (SubNodeContainer)ncParent;
                subnode.postLoadWFM();
            }
        };
        m_inPortTemplates = new WorkflowPortTemplate[original.getNrInPorts()];
        for (int i = 0; i < m_inPortTemplates.length; i++) {
            m_inPortTemplates[i] = new WorkflowPortTemplate(i, original.getInPort(i).getPortType());
        }
        m_outPortTemplates = new WorkflowPortTemplate[original.getNrOutPorts()];
        for (int i = 0; i < m_outPortTemplates.length; i++) {
            m_outPortTemplates[i] = new WorkflowPortTemplate(i, original.getOutPort(i).getPortType());
        }
        m_virtualInNodeIDSuffix = original.getVirtualInNode().getID().getIndex();
        m_virtualOutNodeIDSuffix = original.getVirtualOutNode().getID().getIndex();
        m_layoutInfo = new HashMap<Integer, WizardNodeLayoutInfo>();
        Map<Integer, WizardNodeLayoutInfo> orgLayoutInfo = original.getLayoutInfo();
        for (Entry<Integer, WizardNodeLayoutInfo> layoutEntry : orgLayoutInfo.entrySet()) {
            Integer id = new Integer(layoutEntry.getKey());
            WizardNodeLayoutInfo newInfo = layoutEntry.getValue().clone();
            m_layoutInfo.put(id, newInfo);
        }
        m_layoutJSONString = new String(original.getLayoutJSONString());
        m_templateInformation = original.getTemplateInformation().clone();
    }

    /** {@inheritDoc} */
    @Override
    public SubNodeContainer getNodeContainer(final WorkflowManager parent, final NodeID id) {
        return new SubNodeContainer(parent, id, this);
    }

    /** {@inheritDoc} */
    @Override
    public void loadNodeContainer(
            final Map<Integer, BufferedDataTable> tblRep,
            final ExecutionMonitor exec, final LoadResult loadResult) {
    }

    /** {@inheritDoc} */
    @Override
    public WorkflowPersistor getWorkflowPersistor() {
        return m_workflowPersistor;
    }

    /** {@inheritDoc} */
    @Override
    public WorkflowPortTemplate[] getInPortTemplates() {
        return m_inPortTemplates;
    }

    /** {@inheritDoc} */
    @Override
    public WorkflowPortTemplate[] getOutPortTemplates() {
        return m_outPortTemplates;
    }

    /** {@inheritDoc} */
    @Override
    public int getVirtualInNodeIDSuffix() {
        return m_virtualInNodeIDSuffix;
    }

    /** {@inheritDoc} */
    @Override
    public int getVirtualOutNodeIDSuffix() {
        return m_virtualOutNodeIDSuffix;
    }

    /** {@inheritDoc} */
    @Deprecated
    @Override
    public Map<Integer, WizardNodeLayoutInfo> getLayoutInfo() {
        return m_layoutInfo;
    }

    /**
     * {@inheritDoc}
     * @since 3.1
     */
    @Override
    public String getLayoutJSONString() {
        return m_layoutJSONString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaNodeTemplateInformation getTemplateInformation() {
        return m_templateInformation;
    }

}

