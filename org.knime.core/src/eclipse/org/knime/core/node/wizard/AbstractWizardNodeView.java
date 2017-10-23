/*
 * ------------------------------------------------------------------------
 *
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
 *   23.09.2014 (Christian Albrecht, KNIME.com AG, Zurich, Switzerland): created
 */
package org.knime.core.node.wizard;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.knime.core.node.AbstractNodeView;
import org.knime.core.node.AbstractNodeView.ViewableModel;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.interactive.DefaultReexecutionCallback;
import org.knime.core.node.interactive.InteractiveView;
import org.knime.core.node.interactive.InteractiveViewDelegate;
import org.knime.core.node.interactive.ReexecutionCallback;
import org.knime.core.node.web.ValidationError;
import org.knime.core.node.web.WebViewContent;
import org.knime.core.node.workflow.NodeID;
import org.knime.core.node.workflow.WorkflowManager;

/**
 *
 * @author Christian Albrecht, KNIME.com AG, Zurich, Switzerland
 * @param <T> requires a {@link NodeModel} implementing {@link WizardNode} as well
 * @param <REP> the {@link WebViewContent} implementation used as view representation
 * @param <VAL> the {@link WebViewContent} implementation used as view value
 * @since 2.11
 */
public abstract class AbstractWizardNodeView<T extends ViewableModel & WizardNode<REP, VAL>, REP extends WebViewContent, VAL extends WebViewContent>
    extends AbstractNodeView<T> implements InteractiveView<T, REP, VAL> {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(AbstractWizardNodeView.class);
    private static final String EXT_POINT_ID = "org.knime.core.WizardNodeView";

    private final InteractiveViewDelegate<VAL> m_delegate;

    private AtomicReference<VAL> m_lastRetrievedValue = new AtomicReference<VAL>();

    /** Label for discard option.
     * @since 3.4 */
    protected final static String DISCARD_LABEL = "Discard Changes";
    /** Description text for discard option.
     * @since 3.4 */
    protected final static String DISCARD_DESCRIPTION = "Discards any changes made and closes the view.";
    /** Label for apply temporarily option.
     * @since 3.4 */
    protected final static String APPLY_LABEL = "Apply settings temporarily";
    /** Description text template for apply temporarily option.
     * @since 3.4 */
    protected final static String APPLY_DESCRIPTION_FORMAT = "Applies the current view settings to the node%s"
            + " and triggers a re-execute of the node. This option will not override the default node settings "
            + "set in the dialog. Changes will be lost when the node is reset.";
    /** Label for apply as new default option.
     * @since 3.4*/
    protected final static String APPLY_DEFAULT_LABEL = "Apply settings as new default";
    /** Description text template for apply as new default option.
     * @since 3.4 */
    protected final static String APPLY_DEFAULT_DESCRIPTION_FORMAT = "Applies the current view settings as the new default node settings%s"
            + " and triggers a re-execute of the node. This option will override the settings set in the node dialog "
            + "and changes made will remain applied after a node reset.";

    /**
     * @param nodeModel
     * @since 3.4
     */
    protected AbstractWizardNodeView(final T nodeModel) {
        super(nodeModel);
        m_delegate = new InteractiveViewDelegate<VAL>();
    }

    @Override
    public void setWorkflowManagerAndNodeID(final WorkflowManager wfm, final NodeID id) {
        m_delegate.setWorkflowManagerAndNodeID(wfm, id);
    }

    @Override
    public boolean canReExecute() {
        return m_delegate.canReExecute();
    }

    /**
     * @since 2.10
     */
    @Override
    public void triggerReExecution(final VAL value, final boolean useAsNewDefault, final ReexecutionCallback callback) {
        m_delegate.triggerReExecution(value, useAsNewDefault, callback);
    }

    /**
     * @since 3.4
     */
    public void callViewableModelChanged() {
        try {
            // call model changed on concrete implementation
            modelChanged();
        } catch (NullPointerException npe) {
            LOGGER.coding("AbstractWizardNodeView.modelChanged() causes "
                   + "NullPointerException during notification of a "
                   + "changed model, reason: " + npe.getMessage(), npe);
        } catch (Throwable t) {
            LOGGER.error("AbstractWizardNodeView.modelChanged() causes an error "
                   + "during notification of a changed model, reason: "
                   + t.getMessage(), t);
        }
    }

    /**
     * @return
     * @since 3.4
     */
    protected final WizardNode<REP, VAL> getModel() {
        return super.getViewableModel();
    }

    /**
     * @return The current html file object.
     */
    protected File getViewSource() {
        String viewPath = getModel().getViewHTMLPath();
        if (viewPath != null && !viewPath.isEmpty()) {
            return new File(viewPath);
        }
        return null;
    }

     /**
     * {@inheritDoc}
     */
    @Override
    protected void callCloseView() {
        closeView();
    }

    /**
     * @return The node views creator instance.
     */
    protected WizardViewCreator<REP, VAL> getViewCreator() {
        return getModel().getViewCreator();
    }

    /**
     * Called on view close.
     */
    protected abstract void closeView();

    /**
     * @return the lastRetrievedValue
     * @since 3.4
     */
    public VAL getLastRetrievedValue() {
        return m_lastRetrievedValue.get();
    }

    /**
     * @param lastRetrievedValue the lastRetrievedValue to set
     * @since 3.4
     */
    protected void setLastRetrievedValue(final VAL lastRetrievedValue) {
        m_lastRetrievedValue.set(lastRetrievedValue);
    }

    /**
     * @param useAsDefault true if changed values are supposed to be applied as new node default, false otherwise
     * @return true if apply was successful, false otherwise
     * @since 3.4
     */
    protected boolean applyTriggered(final boolean useAsDefault) {
        if (!viewInteractionPossible() || !checkSettingsChanged()) {
            return true;
        }
        boolean valid = validateCurrentValueInView();
        if (valid) {
            String jsonString = retrieveCurrentValueFromView();
            try {
                VAL viewValue = getModel().createEmptyViewValue();
                viewValue.loadFromStream(new ByteArrayInputStream(jsonString.getBytes(Charset.forName("UTF-8"))));
                setLastRetrievedValue(viewValue);
                ValidationError error = getModel().validateViewValue(viewValue);
                if (error != null) {
                    showValidationErrorInView(error.getError());
                    return false;
                }
                if (getModel() instanceof NodeModel) {
                    triggerReExecution(viewValue, useAsDefault, new DefaultReexecutionCallback());
                } else {
                    getModel().loadViewValue(viewValue, useAsDefault);
                }

                return true;
            } catch (Exception e) {
                LOGGER.error("Could not set error message or trigger re-execution: " + e.getMessage(), e);
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Check if the view value represented in the currently open view has changed from the view value represented in the node model.
     *
     * @return true if the settings have changed, false otherwise or if status cannot be determined
     * @since 3.4
     */
    protected boolean checkSettingsChanged() {
        if (!viewInteractionPossible()) {
            return false;
        }
        String jsonString = retrieveCurrentValueFromView();
        if (jsonString == null) {
         // no view value present in view
            return false;
        }
        try {
            VAL viewValue = getModel().createEmptyViewValue();
            viewValue.loadFromStream(new ByteArrayInputStream(jsonString.getBytes(Charset.forName("UTF-8"))));
            VAL currentViewValue = getModel().getViewValue();
            if (currentViewValue != null) {
                return !currentViewValue.equals(viewValue);
            }
        } catch (Exception e) {
            LOGGER.error("Could not create view value for comparison: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Query if an interaction with the concrete view is possible.
     * @return true, if interaction is possible, false otherwise
     * @since 3.4
     */
    protected abstract boolean viewInteractionPossible();

    /**
     * Execute JavaScript code in view to determine if the current settings validate.
     * @return true, if validation succeeds, false otherwise
     * @since 3.4
     */
    protected abstract boolean validateCurrentValueInView();

    /**
     * Execute JavaScript code in view to retrieve the current view settings.
     * @return the JSON serialized view value string
     * @since 3.4
     */
    protected abstract String retrieveCurrentValueFromView();

    /**
     * Execute JavaScript code in view to display a validation error.
     * @param error the errror to display
     * @since 3.4
     */
    protected abstract void showValidationErrorInView(String error);

    /**
     * Called when a close dialog is supposed to be shown with options on how to deal with changed settings in the view.
     * @return true, if discard is chosen or a subsequent apply was successful, false otherwise
     * @since 3.4
     */
    protected boolean showCloseDialog() {
        String title = "View settings changed";
        String message = "View settings have changed. Please choose one of the following options:";
        return showApplyOptionsDialog(true, title, message);
    }

    /**
     * Called when an apply dialog (temporary or default apply) is supposed to be shown.
     * @return true, if a subsequent apply was successful, false otherwise
     * @since 3.4
     */
    protected boolean showApplyDialog() {
        String title = "Apply view settings";
        String message = "Please choose one of the following options:";
        return showApplyOptionsDialog(false, title, message);
    }

    /**
     * Displays a dialog to ask user how to handle settings changed in view.
     * @param showDiscardOption true, if discard option is to be displayed, false otherwise
     * @param title the title of the dialog
     * @param message the message of the dialog
     * @return true, if discard is chosen or a subsequent apply was successful, false otherwise
     * @since 3.4
     */
    protected abstract boolean showApplyOptionsDialog(final boolean showDiscardOption, final String title, final String message);

    /**
     * Queries extension point for additional {@link AbstractWizardNodeView} implementations.
     * @return A list with all registered view implementations.
     */
    @SuppressWarnings("unchecked")
    public static List<WizardNodeViewExtension> getAllWizardNodeViews() {
        List<WizardNodeViewExtension> viewExtensionList = new ArrayList<WizardNodeViewExtension>();
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint(EXT_POINT_ID);
        assert point != null : "Invalid extension point id: " + EXT_POINT_ID;

        for (IExtension ext : point.getExtensions()) {
            IConfigurationElement[] elements = ext.getConfigurationElements();
            for (IConfigurationElement viewElement : elements) {
                String viewClassName = viewElement.getAttribute("viewClass");
                String viewName = viewElement.getAttribute("name");
                String viewDesc = viewElement.getAttribute("description");
                Class<AbstractWizardNodeView<?, ?, ?>> viewClass;
                try {
                    viewClass = (Class<AbstractWizardNodeView<?, ?, ?>>)Class.forName(viewClassName);
                    viewExtensionList.add(new WizardNodeViewExtension(viewClass, viewName, viewDesc));
                } catch (ClassNotFoundException ex) {
                    NodeLogger.getLogger(AbstractWizardNodeView.class).coding(
                        "Could not find implementation for " + viewClassName, ex);
                }
            }
        }
        return viewExtensionList;
    }

    /**
     * Implementation of a WizardNodeView from extension point.
     *
     * @author Christian Albrecht, KNIME.com AG, Zurich, Switzerland
     */
    public static class WizardNodeViewExtension {

        private Class<AbstractWizardNodeView<?, ?, ?>> m_viewClass;
        private String m_viewName;
        private String m_viewDescription;

        /**
         * Creates a new WizardNodeViewExtension.
         *
         * @param viewClass the class holding the view implementation
         * @param viewName the name of the view
         * @param viewDescription the optional description of the view
         */
        public WizardNodeViewExtension(final Class<AbstractWizardNodeView<?, ?, ?>> viewClass, final String viewName,
            final String viewDescription) {
            m_viewClass = viewClass;
            m_viewName = viewName;
            m_viewDescription = viewDescription;
        }

        /**
         * @return the viewClass
         */
        public Class<AbstractWizardNodeView<? extends ViewableModel, ? extends WebViewContent, ? extends WebViewContent>>
            getViewClass() {
            return m_viewClass;
        }

        /**
         * @return the viewName
         */
        public String getViewName() {
            return m_viewName;
        }

        /**
         * @return the viewDescription
         */
        public String getViewDescription() {
            return m_viewDescription;
        }
    }

}
