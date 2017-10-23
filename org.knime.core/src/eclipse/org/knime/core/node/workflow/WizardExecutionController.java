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
 * Created on Jan 29, 2014 by wiswedel
 */
package org.knime.core.node.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang3.ArrayUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.node.web.ValidationError;

/**
 * A utility class received from the workflow manager that allows stepping back and forth in a wizard execution.
 * USed for the 2nd generation wizard execution based on SubNodes.
 *
 * <p>Do not use, no public API.
 *
 * @author Bernd Wiswedel, KNIME.com, Zurich, Switzerland
 * @author Christian Albrecht, KNIME.com, Zurich, Switzerland
 * @since 2.10
 */
public final class WizardExecutionController extends WebResourceController implements ExecutionController {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(WizardExecutionController.class);

    /** The history of subnodes prompted, current one on top unless {@link #ALL_COMPLETED}. Each int is
     * the subnode ID suffix */
    private final Stack<Integer> m_promptedSubnodeIDSuffixes;

    /** This is the central data structure - it holds all nodes that were halted during
     * execution = nodes that were executed and none of their successors was queued. Those
     * will be active SubNodes with at least one active QF element that can be displayed.
     * After the user got a chance to interact with the Wizard page, those nodes will be
     * re-executed but this time they will not be added/halted again (which is why the status
     * is toggled if they are already in the list - see checkHaltingCriteria()). However, if
     * it is part of a loop it will be executed a second time (after the re-execute) and then
     * execution will be halted again.
     */
    private final List<NodeID> m_waitingSubnodes;

    /** Created from workflow.
     * @param manager ...
     */
    WizardExecutionController(final WorkflowManager manager) {
        super(manager);
        m_promptedSubnodeIDSuffixes = new Stack<>();
        m_waitingSubnodes = new ArrayList<>();
    }

    /** Restored from settings.
     * @param manager ...
     * @param settings ...
     * @throws InvalidSettingsException ...
     */
    WizardExecutionController(final WorkflowManager manager, final NodeSettingsRO settings)
            throws InvalidSettingsException {
        this(manager);
        int[] levelStack = settings.getIntArray("promptedSubnodeIDs");
        m_promptedSubnodeIDSuffixes.addAll(Arrays.asList(ArrayUtils.toObject(levelStack)));
    }

    /**
     * {@inheritDoc}
     * @since 3.4
     */
    @Override
    public void checkHaltingCriteria(final NodeID source) {
        assert m_manager.isLockedByCurrentThread();
        if (m_waitingSubnodes.remove(source)) {
            // trick to handle re-execution of SubNodes properly: when the node is already
            // in the list it was just re-executed and we don't add it to the list of halted
            // nodes but removed it instead. If we see it again then it is part of a loop and
            // we will add it again).
            return;
        }
        if (isSubnodeViewAvailable(source)) {
            // add to the list so we can later avoid queuing of successors!
            m_waitingSubnodes.add(source);
        }
    }

    /** {@inheritDoc}
     * @since 3.4*/
    @Override
    public boolean isHalted(final NodeID source) {
        return m_waitingSubnodes.contains(source);
    }

    /** Get the current wizard page. Throws exception if none is available (as per {@link #hasCurrentWizardPage()}.
     * @return The current wizard page.
     * @since 3.4
     */
    public WizardPageContent getCurrentWizardPage() {
        WorkflowManager manager = m_manager;
        try (WorkflowLock lock = manager.lock()) {
            NodeContext.pushContext(manager);
            try {
                CheckUtils.checkState(hasCurrentWizardPageInternal(), "No current wizard page");
                return getWizardPageInternal(m_waitingSubnodes.get(0));
            } finally {
                NodeContext.removeLastContext();
            }
        }
    }

    /** ...
     * @return ...
     * @deprecated Use {@link #hasCurrentWizardPage()} instead.
     */
    @Deprecated
    public boolean hasNextWizardPage() {
        return hasCurrentWizardPage();
    }

    /** Returns true if the wizard was stepped forward and has a subnode awaiting input.
     * @return That property.
     * @since 2.11 */
    public boolean hasCurrentWizardPage() {
        final WorkflowManager manager = m_manager;
        try (WorkflowLock lock = manager.lock()) {
            checkDiscard();
            NodeContext.pushContext(manager);
            try {
                return hasCurrentWizardPageInternal();
            } finally {
                NodeContext.removeLastContext();
            }
        }
    }

    private boolean hasCurrentWizardPageInternal() {
        assert m_manager.isLockedByCurrentThread();
        return !m_waitingSubnodes.isEmpty();
//        if (m_promptedSubnodeIDSuffixes.isEmpty()) {
//            // stepNext not called
//            return false;
//        } else if (m_promptedSubnodeIDSuffixes.peek() == ALL_COMPLETED) {
//            // all done - result page to be shown
//            return false;
//        }
//        return true;
    }

    /** Continues the execution and executes up to, incl., the next subnode awaiting input. If no such subnode exists
     * it fully executes the workflow. */
    public void stepFirst() {
        final WorkflowManager manager = m_manager;
        try (WorkflowLock lock = manager.lock()) {
            checkDiscard();
            NodeContext.pushContext(manager);
            try {
                stepFirstInternal();
            } finally {
                NodeContext.removeLastContext();
            }
        }
    }

    private void stepFirstInternal() {
        WorkflowManager manager = m_manager;
        assert manager.isLockedByCurrentThread();
        manager.executeAll();
    }

    /**
     * @param viewContentMap
     * @return
     */
    public Map<String, ValidationError> loadValuesIntoCurrentPage(final Map<String, String> viewContentMap) {
        WorkflowManager manager = m_manager;
        try (WorkflowLock lock = manager.lock()) {
            checkDiscard();
            NodeContext.pushContext(manager);
            try {
                CheckUtils.checkState(hasCurrentWizardPageInternal(), "No current wizard page");
                return loadValuesIntoPageInternal(viewContentMap, m_waitingSubnodes.get(0), true, false);
            } finally {
                NodeContext.removeLastContext();
            }
        }
    }

    public void stepNext() {
        final WorkflowManager manager = m_manager;
        try (WorkflowLock lock = manager.lock()) {
            checkDiscard();
            NodeContext.pushContext(manager);
            try {
                stepNextInternal();
            } finally {
                NodeContext.removeLastContext();
            }
        }
    }

    private void stepNextInternal() {
        WorkflowManager manager = m_manager;
        assert manager.isLockedByCurrentThread();
        CheckUtils.checkState(hasCurrentWizardPageInternal(), "No current wizard page");
        NodeID currentID = m_waitingSubnodes.get(0);
        SubNodeContainer currentNC = manager.getNodeContainer(currentID, SubNodeContainer.class, true);
        if (currentNC.getFlowObjectStack().peek(FlowLoopContext.class) == null) {
            m_promptedSubnodeIDSuffixes.push(currentID.getIndex());
        }
        reexecuteNode(currentID);
    }

    private void reexecuteNode(final NodeID id) {
        if (m_manager.getNodeContainer(id).getInternalState().isExecuted()) {
            m_waitingSubnodes.remove(id);
            m_manager.configureNodeAndSuccessors(id, false);
        } else {
            m_manager.executeUpToHere(id);
        }
        // in case of back-stepping we need to mark all nodes again (for execution)
        m_manager.executeAll();
    }

    public boolean hasPreviousWizardPage() {
        WorkflowManager manager = m_manager;
        try (WorkflowLock lock = manager.lock()) {
            checkDiscard();
            NodeContext.pushContext(manager);
            try {
                return hasPreviousWizardPageInternal();
            } finally {
                NodeContext.removeLastContext();
            }
        }
    }

    private boolean hasPreviousWizardPageInternal() {
        assert m_manager.isLockedByCurrentThread();
        return !m_promptedSubnodeIDSuffixes.isEmpty();
    }


    public void stepBack() {
        WorkflowManager manager = m_manager;
        try (WorkflowLock lock = manager.lock()) {
            checkDiscard();
            NodeContext.pushContext(manager);
            try {
                stepBackInternal();
            } finally {
                NodeContext.removeLastContext();
            }
        }
    }

    private void stepBackInternal() {
        WorkflowManager manager = m_manager;
        assert manager.isLockedByCurrentThread();
        CheckUtils.checkState(hasPreviousWizardPageInternal(), "No more previous pages");
        int currentPage = m_promptedSubnodeIDSuffixes.pop();
        NodeID currentSNID = toNodeID(currentPage);
        m_waitingSubnodes.clear();
        m_waitingSubnodes.add(currentSNID);
        SubNodeContainer currentSN = manager.getNodeContainer(currentSNID, SubNodeContainer.class, true);
        final Integer previousSNIDSuffix = m_promptedSubnodeIDSuffixes.isEmpty()
                ? null : m_promptedSubnodeIDSuffixes.peek();
        SubNodeContainer previousSN = previousSNIDSuffix == null ? null
            : manager.getNodeContainer(toNodeID(previousSNIDSuffix), SubNodeContainer.class, true);
        LOGGER.debugWithFormat("Stepping back wizard execution - resetting Wrapped Metanode \"%s\" (%s)",
            currentSN.getNameWithID(), previousSN == null ? "no more Wrapped Metanodes to reset"
                : "new current one is \"" + previousSN.getNameWithID() + "\"");
        manager.cancelExecution(currentSN);
        manager.resetAndConfigureNodeAndSuccessors(currentSNID, false);
    }

    /** Sets manager to null. Called when new wizard is created on top of workflow. */
    @Override
    void discard() {
    }

    void save(final NodeSettingsWO settings) {
        int[] promptedSubnodeIDs = ArrayUtils.toPrimitive(
            m_promptedSubnodeIDSuffixes.toArray(new Integer[m_promptedSubnodeIDSuffixes.size()]));
        settings.addIntArray("promptedSubnodeIDs", promptedSubnodeIDs);
    }

    @Override
    boolean isResetDownstreamNodesWhenApplyingViewValue() {
        return false;
    }

    @Override
    void stateCheckWhenApplyingViewValues(final SubNodeContainer snc) {
        // the node should be executed but possibly one of the view nodes fails so it's not. We accept any.
    }

    @Override
    void stateCheckDownstreamNodesWhenApplyingViewValues(final SubNodeContainer snc, final NodeContainer downstreamNC) {
        final InternalNodeContainerState destNCState = downstreamNC.getInternalState();
        CheckUtils.checkState(destNCState.isHalted() && !destNCState.isExecuted(), "Downstream nodes of "
                + "Wrapped Metanode %s must not be in execution/executed (node %s)", snc.getNameWithID(), downstreamNC);
    }

}
