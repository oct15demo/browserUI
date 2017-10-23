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
 * -------------------------------------------------------------------
 *
 * History
 *   08.06.2006 (Tobias Koetter): created
 */
package org.knime.base.node.viz.pie.node;

import org.knime.base.node.viz.pie.datamodel.PieVizModel;
import org.knime.base.node.viz.pie.impl.PiePlotter;
import org.knime.base.node.viz.pie.impl.PieProperties;
import org.knime.core.node.NodeView;
import org.knime.core.node.property.hilite.HiLiteHandler;

/**
 * The abstract node view which contains the pie chart panel.
 *
 * @author Tobias Koetter, University of Konstanz
 * @param <P> the {@link PieProperties} implementation
 * @param <D> the {@link PieVizModel}implementation
 * @param <T> the {@link PieNodeModel} implementation
 *
 */
public abstract class PieNodeView<P extends PieProperties<D>,
D extends PieVizModel, T extends PieNodeModel<D>> extends NodeView<T> {

    private PiePlotter<P, D> m_plotter;

    /**
     * Creates a new view instance for the histogram node.
     *
     * @param nodeModel the corresponding node model
     */
    @SuppressWarnings("unchecked")
    protected PieNodeView(final T nodeModel) {
        super(nodeModel);

    }

    /**
     * Whenever the model changes an update for the plotter is triggered and new
     * HiLiteHandler are set.
     */
    @Override
    public void modelChanged() {
        final T model = getNodeModel();
        if (model == null) {
            return;
        }
        if (m_plotter != null) {
            m_plotter.reset();
        }
        final D vizModel = model.getVizModel();
        if (vizModel == null) {
            return;
        }
        if (m_plotter == null) {
            m_plotter = getPlotter(vizModel, model.getInHiLiteHandler(0));
            if (vizModel.supportsHiliting()) {
                // add the hilite menu to the menu bar of the node view
                getJMenuBar().add(m_plotter.getHiLiteMenu());
            }
            setComponent(m_plotter);
        }
        m_plotter.setHiLiteHandler(model.getInHiLiteHandler(0));
        m_plotter.setVizModel(vizModel);
        m_plotter.updatePaintModel();
        if (getComponent() == null) {
            setComponent(m_plotter);
        }
    }

    /**
     * @param vizModel the pie visualization model
     * @param handler the hilite handler
     * @return the plotter implementation
     */
    protected abstract PiePlotter<P, D> getPlotter(final D vizModel,
            final HiLiteHandler handler);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
        return;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {
        return;
    }
}
