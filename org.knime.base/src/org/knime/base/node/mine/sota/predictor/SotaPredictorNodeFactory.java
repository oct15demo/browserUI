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
 *   30.03.2007 (thiel): created
 */
package org.knime.base.node.mine.sota.predictor;

import org.knime.base.node.mine.sota.SotaPortObjectSpec;
import org.knime.base.node.mine.util.PredictorNodeDialog;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.port.PortObjectSpec;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class SotaPredictorNodeFactory
extends NodeFactory<SotaPredictorNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new PredictorNodeDialog(SotaPredictorNodeModel.createAppendProbabilities()) {
            /**
             * {@inheritDoc}
             */
            @Override
            protected void extractTargetColumn(final PortObjectSpec[] specs) {
                if (specs[0] instanceof SotaPortObjectSpec) {
                    final SotaPortObjectSpec sotaSpec = (SotaPortObjectSpec)specs[0];
                    final DataColumnSpec classColumnSpec = sotaSpec.getClassColumnSpec();
                    setLastTargetColumn(classColumnSpec == null ? new DataColumnSpecCreator("No class", StringCell.TYPE)
                        .createSpec() : classColumnSpec);
                } else {
                    throw new IllegalArgumentException("Wrong input: " + specs[0].getClass());
                }
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SotaPredictorNodeModel createNodeModel() {
        return new SotaPredictorNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<SotaPredictorNodeModel> createNodeView(
            final int viewIndex, final SotaPredictorNodeModel nodeModel) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean hasDialog() {
        return true;
    }

}
