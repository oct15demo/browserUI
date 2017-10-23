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
 *   29.09.2006 (Fabian Dill): created
 */
package org.knime.base.node.viz.plotter.box;

import org.knime.base.node.viz.plotter.DataProvider;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeView;

/**
 *
 * @author Fabian Dill, University of Konstanz
 */
public class BoxPlotNodeView extends NodeView<BoxPlotNodeModel> {

    private BoxPlotter m_plotter;

    /**
     *
     * @param model the model
     * @param plotter the plotter
     */
    public BoxPlotNodeView(final BoxPlotNodeModel model,
            final BoxPlotter plotter) {
        super(model);
        m_plotter = plotter;
        m_plotter.setDataProvider(model);
        getJMenuBar().add(m_plotter.getHiLiteMenu());
        setComponent(m_plotter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {
        if (getNodeModel() != null) {
            NodeModel model = getNodeModel();
            m_plotter.reset();
            m_plotter.setHiLiteHandler(model.getInHiLiteHandler(0));
            m_plotter.setAntialiasing(true);
            m_plotter.setDataProvider((DataProvider)model);
            m_plotter.updatePaintModel();
            m_plotter.fitToScreen();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {
        /*
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                m_plotter.fitToScreen();
            }
        });
        */
    }

}
