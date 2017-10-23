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
 *   Aug 14, 2009 (wiswedel): created
 */
package org.knime.core.node;

import java.awt.Rectangle;


/**
 * Node view which opens an external application. Opening, closing and
 * updating the application is task of derived view classes.
 *
 * @author Bernd Wiswedel, University of Konstanz
 * @param <T> The node model class.
 */
public abstract class ExternalApplicationNodeView<T extends NodeModel>
    extends AbstractNodeView<T> {

    /** Creates the view instance but does not open the external application
     * yet.
     * @param model The node model assigned to the view, must not be null.
     */
    protected ExternalApplicationNodeView(final T model) {
        super(model);
    }

    /** {@inheritDoc}
     * @since 2.8
     */
    @Override
    protected void callCloseView() {
        onClose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void callOpenView(final String title) {
        onOpen(title);
    }

    /**
     * Get reference to underlying <code>NodeModel</code>, never null.
     * @return NodeModel reference.
     * @since 3.4
     */
    protected T getNodeModel() {
        return super.getViewableModel();
    }

    /**
     * {@inheritDoc}
     * @since 2.12
     */
    @Override
    protected void callOpenView(final String title, final Rectangle knimeWindowBounds) {
        onOpen(title, knimeWindowBounds);
    }

    /** To be called by client code when the external view is closed. This will
     * initiate the usual closing procedure, i.e. unregistering this view from
     * its model and also calling {@link #onClose()}.
     * {@inheritDoc} */
    @Override
    public final void closeView() {
        super.closeView();
    }

    /**
     * Open the external application.
     * @param title The desired title of the application, possibly ignored.
     */
    protected abstract void onOpen(final String title);

    /**
     * Open the external application.
     * @param title The desired title of the application, possibly ignored.
     * @param knimeWindowBounds Bounds of the KNIME window, of interest for window alignment.
     * @since 2.12
     */
    protected void onOpen(final String title, final Rectangle knimeWindowBounds) {
        // Default is to ignore knimeWindowBounds
        onOpen(title);
    }

    /** Close the view. This method is called when the node is deleted or
     * {@link #closeView()} is called. This method should not be used if the
     * external application initiates the close operation. Clients should call
     * {@link #closeView()} instead. */
    protected abstract void onClose();

}
