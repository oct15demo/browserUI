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
 *   Jul 2, 2009 (wiswedel): created
 */
package org.knime.core.node.workflow;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.container.ContainerTable;
import org.knime.core.data.filestore.internal.WorkflowFileStoreHandlerRepository;
import org.knime.core.internal.ReferencedFile;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.workflow.WorkflowManager.AuthorInformation;

/**
 * Persistor that is used to represent, for instance the clipboard content.
 * It contains a list of nodes the connections connecting them. It does not
 * support any of the "load" routines as it is an in-memory persistor. Instead
 * it throws exceptions when any of the load routines are called.
 * @author Bernd Wiswedel, University of Konstanz
 */
public class PasteWorkflowContentPersistor implements WorkflowPersistor {

    private final Set<ConnectionContainerTemplate> m_connectionSet;
    private final Set<ConnectionContainerTemplate> m_additionalConnectionSet;
    private final Map<Integer, NodeContainerPersistor> m_loaderMap;
    private final WorkflowAnnotation[] m_copiedAnnotations;
    private final boolean m_isUndoableDeleteCommand;

    /** Create new persistor.
     * @param loaderMap The loader map.
     * @param connectionSet A copy of connection clones.
     * @param additionalConnectionSet see {@link #getAdditionalConnectionSet()}
     * @param copiedAnnotations Copied workflow annotations.
     * @param isUndoableDeleteCommand If false, annotations will be cloned in
     *        {@link #getWorkflowAnnotations()}.
     */
    PasteWorkflowContentPersistor(
            final Map<Integer, NodeContainerPersistor> loaderMap,
            final Set<ConnectionContainerTemplate> connectionSet,
            final Set<ConnectionContainerTemplate> additionalConnectionSet,
            final WorkflowAnnotation[] copiedAnnotations,
            final boolean isUndoableDeleteCommand) {
        m_connectionSet = connectionSet;
        m_additionalConnectionSet = additionalConnectionSet;
        m_loaderMap = loaderMap;
        m_copiedAnnotations = copiedAnnotations;
        m_isUndoableDeleteCommand = isUndoableDeleteCommand;
    }

    /** {@inheritDoc} */
    @Override
    public Set<ConnectionContainerTemplate> getConnectionSet() {
        return m_connectionSet;
    }

    /** {@inheritDoc} */
    @Override
    public Set<ConnectionContainerTemplate> getAdditionalConnectionSet() {
        return m_additionalConnectionSet;
    }

    /** {@inheritDoc} */
    @Override
    public HashMap<Integer, ContainerTable> getGlobalTableRepository() {
        throwUnsupportedOperationException();
        return null;
    }

    /** {@inheritDoc}
     * @since 2.6 */
    @Override
    public WorkflowFileStoreHandlerRepository getFileStoreHandlerRepository() {
        throwUnsupportedOperationException();
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public WorkflowPortTemplate[] getInPortTemplates() {
        throwUnsupportedOperationException();
        return null;
    }

    /** {@inheritDoc}
     * @since 3.5*/
    @Override
    public NodeUIInformation getInPortsBarUIInfo() {
        throwUnsupportedOperationException();
        return null;
    }

    /** {@inheritDoc}
     * @since 2.6 */
    @Override
    public boolean isProject() {
        throwUnsupportedOperationException();
        return false;
    }

    /**
     * {@inheritDoc}
     * @since 2.8
     */
    @Override
    public WorkflowContext getWorkflowContext() {
        throwUnsupportedOperationException();
        return null;
    }

    /** {@inheritDoc}
     * @since 2.10*/
    @Override
    public FileWorkflowPersistor.LoadVersion getLoadVersion() {
        throwUnsupportedOperationException();
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        throwUnsupportedOperationException();
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public WorkflowCipher getWorkflowCipher() {
        throwUnsupportedOperationException();
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public MetaNodeTemplateInformation getTemplateInformation() {
        throwUnsupportedOperationException();
        return null;
    }

    /** {@inheritDoc}
     * @since 2.8*/
    @Override
    public AuthorInformation getAuthorInformation() {
        throwUnsupportedOperationException();
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Map<Integer, NodeContainerPersistor> getNodeLoaderMap() {
        return m_loaderMap;
    }

    /** {@inheritDoc} */
    @Override
    public WorkflowPortTemplate[] getOutPortTemplates() {
        throwUnsupportedOperationException();
        return null;
    }

    /** {@inheritDoc}
     * @since 3.5*/
    @Override
    public NodeUIInformation getOutPortsBarUIInfo() {
        throwUnsupportedOperationException();
        return null;
    }

    /** {@inheritDoc}
     * @since 2.6 */
    @Override
    public EditorUIInformation getEditorUIInformation() {
        throwUnsupportedOperationException();
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public List<FlowVariable> getWorkflowVariables() {
        throwUnsupportedOperationException();
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public List<Credentials> getCredentials() {
        throwUnsupportedOperationException();
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public List<WorkflowAnnotation> getWorkflowAnnotations() {
        if (m_isUndoableDeleteCommand) {
            return Arrays.asList(m_copiedAnnotations);
        } else {
            // must create a new fresh copy on each invocation
            // (multiple pastes possible)
            ArrayList<WorkflowAnnotation> result =
                new ArrayList<WorkflowAnnotation>(m_copiedAnnotations.length);
            for (WorkflowAnnotation a : m_copiedAnnotations) {
                result.add(a.clone());
            }
            return result;
        }
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
    public boolean mustWarnOnDataLoadError() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public NodeContainerMetaPersistor getMetaPersistor() {
        throwUnsupportedOperationException();
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public NodeContainer getNodeContainer(final WorkflowManager parent,
            final NodeID id) {
        throwUnsupportedOperationException();
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDirtyAfterLoad() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void loadNodeContainer(final Map<Integer, BufferedDataTable> tblRep,
            final ExecutionMonitor exec, final LoadResult loadResult)
            throws InvalidSettingsException, CanceledExecutionException,
            IOException {
        throwUnsupportedOperationException();
    }

    /** {@inheritDoc}
     * @since 2.10*/
    @Override
    public void postLoad(final WorkflowManager wfm, final LoadResult loadResult) {
    }

    /** {@inheritDoc} */
    @Override
    public boolean mustComplainIfStateDoesNotMatch() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsResetAfterLoad() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public InputStream decipherInput(final InputStream input) {
        throwUnsupportedOperationException();
        return null;
    }

    /** Throws a new exception with a meaningful error message.
     * It is called when a non supported method is invoked.
     */
    protected void throwUnsupportedOperationException() {
        String methodName = "<unknown>";
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        // top most element is this method, at index [1] we find the calling
        // method name.
        if (callStack.length > 3) {
            methodName = callStack[2].getMethodName() + "\"";
        }
        throw new UnsupportedOperationException("Calling \"" + methodName
                + "\" not allowed on \"" + getClass().getSimpleName() + "\"");
    }
}
