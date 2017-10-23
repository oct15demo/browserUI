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
 *   27.02.2006 (dill): created
 */
package org.knime.base.node.preproc.bitvector.create;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import org.knime.core.node.NodeView;


/**
 * The BitvectorGeneratorView provides information about the generation of the
 * bitsets out of the data. In particular, this is the number of processed rows,
 * the resulting bit vector length, the total number of generated zeros and ones
 * and the resulting ratio from 1s to 0s.
 *
 * @author Fabian Dill, University of Konstanz
 */
public class CreateBitVectorView extends NodeView<CreateBitVectorNodeModel> {

    private JEditorPane m_pane;

    private static final int ROUNDING_CONSTANT = 10000;

    /**
     * Creates the view instance or the BitVectorGeneratorNode with the
     * BitVectorGeneratorNodeModel as the underlying model.
     *
     * @param model the underlying node model
     */
    CreateBitVectorView(final CreateBitVectorNodeModel model) {
        super(model);
        m_pane = new JEditorPane();
        m_pane.setEditable(false);
        m_pane.setText("No data available");
        setComponent(new JScrollPane(m_pane));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {
        CreateBitVectorNodeModel model =
            getNodeModel();
        if (model != null) {
            setTextArea();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
        // empty
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {
        CreateBitVectorNodeModel model = (getNodeModel());
        if (model != null) {
            setTextArea();
        }
    }

    private void setTextArea() {
        CreateBitVectorNodeModel model = getNodeModel();
        StringBuffer buffer = new StringBuffer("<html></body>");
        buffer.append("<h2>Bit Vector Generator Information:</h2>");
        buffer.append("<hr>");
        buffer.append("<table>");
        buffer.append("<tr><td>Number of processed rows: </td>"
                + "<td align=\"right\">" + model.getNumberOfProcessedRows()
                + " </td></tr>");
        buffer.append("<tr><td>Total number of 0s: </td>"
                + "<td align=\"right\">" + model.getTotalNrOf0s()
                + " </td></tr>");
        buffer.append("<tr><td>Total number of 1s: </td>"
                + "<td align=\"right\">" + model.getTotalNrOf1s()
                + "</td></tr>");
        double ratio = 0.0;
        if (model.getTotalNrOf0s() > 0) {
            ratio = (int)(((double)model.getTotalNrOf1s() / (double)model
                .getTotalNrOf0s()) * ROUNDING_CONSTANT);
            ratio = ratio / ROUNDING_CONSTANT;
        }
        buffer.append("<tr><td>Ratio of 1s to 0s: </td>"
                + "<td align=\"right\">" + ratio + "</td></tr></table>");
        buffer.append("</body></html>");
        m_pane = new JEditorPane("text/html", "");
        m_pane.setText(buffer.toString());
        m_pane.setEditable(false);
        setComponent(new JScrollPane(m_pane));
    }
}
