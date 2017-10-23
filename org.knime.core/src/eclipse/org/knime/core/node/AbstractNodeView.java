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
package org.knime.core.node;

import java.awt.Rectangle;

import org.knime.core.node.AbstractNodeView.ViewableModel;
import org.knime.core.node.workflow.NodeContext;



/**
 * Abstract implementation of a node view. A node view is the visual component
 * to a node, displaying a computed result (or the ongoing computation).
 *
 * <p>
 * <strong>Note:</strong>Concrete implementations do not inherit from this class
 * directly but extend from {@link NodeView}. Nodes that open an external
 * application extend from {@link ExternalApplicationNodeView}.
 *
 * @author Bernd Wiswedel, University of Konstanz
 * @param <T> the implementation of the {@link NodeModel} this node view
 *          is based on
 */
public abstract class AbstractNodeView<T extends ViewableModel> {

    /**
     * The node logger for this class; do not make static to make sure the right
     * class name is printed in messages.
     */
    private final NodeLogger m_logger;

    /** Name of view. Used for log messages and suggested view title (if
     * subclass allows view titles). */
    private String m_viewName;

    /** Holds the underlying <code>ViewableModel</code> of type T, never null. */
    private final T m_viewableModel;

    /**
     * The node context for this view.
     * @since 2.8
     */
    protected final NodeContext m_nodeContext;


    /** Creates new view. This constructor keeps the node model reference and
     * instantiates the logger.
     * @param viewableModel The underlying viewable model.
     * @throws NullPointerException If the <code>nodeModel</code> is null.
     * @since 3.4
     */
    protected AbstractNodeView(final T viewableModel) {
        if (viewableModel == null) {
            throw new IllegalArgumentException("Node model must not be null");
        }
        m_logger = NodeLogger.getLogger(this.getClass());
        m_viewableModel = viewableModel;

        m_nodeContext = NodeContext.getContext();
        m_logger.assertLog(m_nodeContext != null, "No node context available in constructor of node view "
            + getClass().getName());
    }

    /**
     * Get reference to underlying <code>ViewableModel</code>, never null.
     * @return ViewableModel reference.
     * @since 3.4
     */
    protected final T getViewableModel() {
        return m_viewableModel;
    }

    /** Get reference to logger, never null. The logger is customized with an
     * appropriate name (currently the runtime class of the view).
     * @return reference to logger */
    public NodeLogger getLogger() {
        return m_logger;
    }

    /**
     * Called from the model that something has changed. It calls the abstract
     * method {@link #modelChanged()}.
     *
     * <p>This method is called when the node makes state transitions that
     * affect the node model content, i.e. after execute or reset or when the
     * highlight handler has changed.
     */
    void callModelChanged() {
        try {
            // CALL abstract model changed
            modelChanged();
        } catch (NullPointerException npe) {
            m_logger.coding("NodeView.modelChanged() causes "
                   + "NullPointerException during notification of a "
                   + "changed model, reason: " + npe.getMessage(), npe);
        } catch (Throwable t) {
            m_logger.error("NodeView.modelChanged() causes an error "
                   + "during notification of a changed model, reason: "
                   + t.getMessage(), t);
        }
    }

    /**
     * Method is invoked when the underlying <code>NodeModel</code> has
     * changed. Also the HiLightHandler have changed. Note, the
     * <code>NodeModel</code> content may be not available. The framework
     * calls this method in the EventDispatchThread (GUI updates can be done
     * directly in the calling thread).
     */
    protected abstract void modelChanged();

    /**
     * This method can be overridden by views that want to receive
     * events from their assigned models via the
     * {@link NodeModel#notifyViews(Object)} method. Can be used to
     * iteratively update the view during execute.
     *
     * @param arg The argument that is provided in the
     * {@linkplain NodeModel#notifyViews(Object) notifyViews} method.
     */
    protected void updateModel(final Object arg) {
        // dummy statement to get rid of 'parameter not used' warning.
        assert arg == arg;
    }

    /** Called from the framework to open a new view or bring an existing
     * view to front. The title serves as default view name and should be
     * shown in the view title bar if possible. This method must be called
     * at most once per view instance!
     * @param title The view title.
     * @see #closeView() */
    final void openView(final String title, final Rectangle knimeWindowBounds) {
        if (m_viewableModel instanceof NodeModel) {
            ((NodeModel)m_viewableModel).registerView(this);
        }
        m_viewName = title;
        callOpenView(title, knimeWindowBounds);
    }

    /** Direct(!) subclasses override this method and open the view or frame.
     * This method is called
     * at most once.
     * @param title the default title of the view. It should be shown in the
     * view title bar (if at all possible).
     * @since 2.8
     */
    protected abstract void callOpenView(final String title);

    /** Direct(!) subclasses override this method and open the view or frame.
     * This method is called
     * at most once.
     * @param title the default title of the view. It should be shown in the
     * view title bar (if at all possible).
     * @param knimeWindowBounds the window bounds
     * @since 2.12
     */
    protected void callOpenView(final String title, final Rectangle knimeWindowBounds) {
        callOpenView(title);
    }

    /** Closes the view and disposes all allocated resources. The view is not
     * meant to be opened again. This method is the counterpart to
     * {@link #openView(String, Rectangle)}. (Core) Sub-classes may widen the scope of this
     * method. */
    void closeView() {
        /* needs to be called like this to avoid publicizing model methods */
        if (m_viewableModel instanceof NodeModel) {
            ((NodeModel)m_viewableModel).unregisterView(this);
        }
        callCloseView();
    }

    /** Called from {@link #closeView()} to close the view and release all
     * allocated resources. The view will not be opened again.
     *
     * @since 2.8
     **/
    protected abstract void callCloseView();

    /** @return the viewName as set in the {@link #openView(String, Rectangle)} method. */
    String getViewName() {
        return m_viewName;
    }

    /**
     * Interface for viewable model content.
     *
     * @author Bernd Wiswedel, Christian Albrecht, KNME.com AG, Zurich, Switzerland
     * @since 3.4
     */
    public static interface ViewableModel {
        /* empty for now */
    }

}
