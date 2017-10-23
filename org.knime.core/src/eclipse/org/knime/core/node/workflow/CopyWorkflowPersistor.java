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
 *   Jun 9, 2008 (wiswedel): created
 */
package org.knime.core.node.workflow;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.container.ContainerTable;
import org.knime.core.data.filestore.internal.FileStoreHandlerRepository;
import org.knime.core.data.filestore.internal.WorkflowFileStoreHandlerRepository;
import org.knime.core.internal.ReferencedFile;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.workflow.WorkflowManager.AuthorInformation;

/**
 * @author Bernd Wiswedel, University of Konstanz
 */
class CopyWorkflowPersistor implements WorkflowPersistor {

    private final Map<Integer, NodeContainerPersistor> m_ncs;
    private final Set<ConnectionContainerTemplate> m_cons;
    private final NodeUIInformation m_inportUIInfo;
    private final WorkflowPortTemplate[] m_inportTemplates;
    private final NodeUIInformation m_outportUIInfo;
    private final WorkflowPortTemplate[] m_outportTemplates;
    private final EditorUIInformation m_editorUIInformation;
    private final String m_name;
    private final WorkflowCipher m_workflowCipher;
    private final MetaNodeTemplateInformation m_templateInformation;
    private final AuthorInformation m_authorInformation;
    private final CopyNodeContainerMetaPersistor m_metaPersistor;
    private final HashMap<Integer, ContainerTable> m_tableRep;
    private final WorkflowFileStoreHandlerRepository m_fileStoreHandlerRepository;
    private final List<FlowVariable> m_workflowVariables;
    private final List<Credentials> m_credentials;
    private final List<WorkflowAnnotation> m_workflowAnnotations;
    private final boolean m_isProject;
    /** Create copy persistor.
     * @param original To copy from
     * @param tableRep The table map in the target
     * @param preserveDeletableFlags Whether to keep the "is-deletable" flags
     *        in the target.
     * @param isUndoableDeleteCommand If to keep the location of the node
     *        directories (important for undo of delete commands, see
     *        {@link WorkflowManager#copy(boolean, WorkflowCopyContent)}
     *        for details.)
     */
    @SuppressWarnings("unchecked")
    CopyWorkflowPersistor(final WorkflowManager original,
            final HashMap<Integer, ContainerTable> tableRep,
            final FileStoreHandlerRepository fileStoreHandlerRepository,
            final boolean preserveDeletableFlags,
            final boolean isUndoableDeleteCommand) {
        m_inportUIInfo = original.getInPortsBarUIInfo() != null
            ? NodeUIInformation.builder(original.getInPortsBarUIInfo()).build() : null;
        m_outportUIInfo = original.getOutPortsBarUIInfo() != null
            ? NodeUIInformation.builder(original.getOutPortsBarUIInfo()).build() : null;
        m_isProject = original.isProject();
        m_inportTemplates = new WorkflowPortTemplate[original.getNrInPorts()];
        m_outportTemplates = new WorkflowPortTemplate[original.getNrOutPorts()];
        for (int i = 0; i < m_inportTemplates.length; i++) {
            WorkflowInPort in = original.getInPort(i);
            m_inportTemplates[i] =
                new WorkflowPortTemplate(i, in.getPortType());
        }
        for (int i = 0; i < m_outportTemplates.length; i++) {
            WorkflowOutPort in = original.getOutPort(i);
            m_outportTemplates[i] =
                new WorkflowPortTemplate(i, in.getPortType());
        }
        m_editorUIInformation = original.getEditorUIInformation() != null
            ? EditorUIInformation.builder(original.getEditorUIInformation()).build() : null;
        m_name = original.getNameField();
        m_workflowCipher = original.getWorkflowCipher().clone();
        m_templateInformation = original.getTemplateInformation().clone();
        m_authorInformation = original.getAuthorInformation();
        m_metaPersistor = new CopyNodeContainerMetaPersistor(
                original, preserveDeletableFlags, isUndoableDeleteCommand);
        if (m_isProject) {
            assert m_outportTemplates.length == 0
                    && m_inportTemplates.length == 0;
            m_tableRep = new GlobalTableRepository();
            m_fileStoreHandlerRepository = new WorkflowFileStoreHandlerRepository();
        } else {
            m_fileStoreHandlerRepository = null;
            m_tableRep = null;
        }
        m_ncs = new LinkedHashMap<Integer, NodeContainerPersistor>();
        m_cons = new LinkedHashSet<ConnectionContainerTemplate>();
        for (NodeContainer nc : original.getNodeContainers()) {
            m_ncs.put(nc.getID().getIndex(), nc.getCopyPersistor(
                    m_tableRep, null, true, isUndoableDeleteCommand));
        }

        for (ConnectionContainer cc : original.getConnectionContainers()) {
            m_cons.add(new ConnectionContainerTemplate(cc, true));
        }
        List<FlowVariable> vars = original.getWorkflowVariables();
        m_workflowVariables = vars == null ? Collections.EMPTY_LIST
                : new ArrayList<FlowVariable>(vars);
        m_credentials = new ArrayList<Credentials>();
        for (Credentials c : original.getCredentialsStore().getCredentials()) {
            m_credentials.add(c.clone());
        }
        m_workflowAnnotations = new ArrayList<WorkflowAnnotation>();
        for (WorkflowAnnotation w : original.getWorkflowAnnotations()) {
            WorkflowAnnotation anno = isUndoableDeleteCommand ? w : w.clone();
            m_workflowAnnotations.add(anno);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isProject() {
        return m_isProject;
    }

    /** {@inheritDoc} */
    @Override
    public Set<ConnectionContainerTemplate> getConnectionSet() {
        return m_cons;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowContext getWorkflowContext() {
        return null; // context is determined by target workflow - in most cases the content is not a project anyway
    }

    /** {@inheritDoc} */
    @Override
    public Set<ConnectionContainerTemplate> getAdditionalConnectionSet() {
        return Collections.emptySet();
    }

    /** {@inheritDoc} */
    @Override
    public HashMap<Integer, ContainerTable> getGlobalTableRepository() {
        return m_tableRep;
    }

    /** {@inheritDoc} */
    @Override
    public WorkflowFileStoreHandlerRepository getFileStoreHandlerRepository() {
        assert isProject() : "only to be called on projects";
        return m_fileStoreHandlerRepository;
    }

    /** {@inheritDoc} */
    @Override
    public NodeUIInformation getInPortsBarUIInfo() {
        return m_inportUIInfo;
    }

    /** {@inheritDoc} */
    @Override
    public WorkflowPortTemplate[] getInPortTemplates() {
        return m_inportTemplates;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return m_name;
    }

    /** {@inheritDoc} */
    @Override
    public WorkflowCipher getWorkflowCipher() {
        return m_workflowCipher;
    }

    /** {@inheritDoc} */
    @Override
    public MetaNodeTemplateInformation getTemplateInformation() {
        return m_templateInformation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorInformation getAuthorInformation() {
        return m_authorInformation;
    }

    /** {@inheritDoc} */
    @Override
    public List<FlowVariable> getWorkflowVariables() {
        return m_workflowVariables;
    }

    /** {@inheritDoc} */
    @Override
    public List<Credentials> getCredentials() {
        return m_credentials;
    }

    /** {@inheritDoc} */
    @Override
    public List<WorkflowAnnotation> getWorkflowAnnotations() {
        return m_workflowAnnotations;
    }

    /** {@inheritDoc} */
    @Override
    public NodeSettingsRO getWizardExecutionControllerState() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public List<ReferencedFile> getObsoleteNodeDirectories() {
        return Collections.emptyList();
    }

    /** {@inheritDoc} */
    @Override
    public NodeUIInformation getOutPortsBarUIInfo() {
        return m_outportUIInfo;
    }

    /** {@inheritDoc} */
    @Override
    public WorkflowPortTemplate[] getOutPortTemplates() {
        return m_outportTemplates;
    }

    /** {@inheritDoc} */
    @Override
    public EditorUIInformation getEditorUIInformation() {
        return m_editorUIInformation;
    }

    /** {@inheritDoc} */
    @Override
    public NodeContainerMetaPersistor getMetaPersistor() {
        return m_metaPersistor;
    }

    /** {@inheritDoc} */
    @Override
    public FileWorkflowPersistor.LoadVersion getLoadVersion() {
        return FileWorkflowPersistor.LoadVersion.UNKNOWN;
    }

    /** {@inheritDoc} */
    @Override
    public Map<Integer, NodeContainerPersistor> getNodeLoaderMap() {
        return m_ncs;
    }

    /** {@inheritDoc} */
    @Override
    public NodeContainer getNodeContainer(final WorkflowManager parent,
            final NodeID id) {
        return parent.createSubWorkflow(this, id);
    }

    /** {@inheritDoc} */
    @Override
    public void loadNodeContainer(final Map<Integer, BufferedDataTable> tblRep,
            final ExecutionMonitor exec, final LoadResult loadResult) {
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsResetAfterLoad() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDirtyAfterLoad() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean mustComplainIfStateDoesNotMatch() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public InputStream decipherInput(final InputStream input) {
        throw new IllegalStateException("Method not to be called");
    }

    /** {@inheritDoc} */
    @Override
    public void postLoad(final WorkflowManager wfm, final LoadResult loadResult) {
    }

    /** {@inheritDoc} */
    @Override
    public boolean mustWarnOnDataLoadError() {
        return true;
    }

}
