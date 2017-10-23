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
 *
 * History
 *   19.07.2010 (hofer): created
 */
package org.knime.base.node.preproc.autobinner.pmml;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.knime.core.node.port.pmml.preproc.PMMLPreprocPortObject;
import org.knime.core.node.port.pmml.preproc.PMMLPreprocPortObjectSpec;

/**
 *
 * @author Heiko Hofer
 */
public class PMMLDiscretizePreprocPortObject extends PMMLPreprocPortObject {
    /**
     * @noreference This class is not intended to be referenced by clients.
     * @since 3.0
     */
    public static final class Serializer extends AbstractPortObjectSerializer<PMMLDiscretizePreprocPortObject> {}

    public PMMLDiscretizePreprocPortObject() {
        // necessary for loading (see documentation of PMMLPreprocPortObject)
    }

    public PMMLDiscretizePreprocPortObject(final PMMLPreprocDiscretize operation) {
        super(operation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PMMLPreprocPortObjectSpec getSpec() {
        PMMLPreprocDiscretize op =
            (PMMLPreprocDiscretize) getOperations().get(0);
        return new PMMLDiscretizePreprocPortObjectSpec(op);
    }

    @Override
    public JComponent[] getViews() {
        PMMLPreprocDiscretize op =
            (PMMLPreprocDiscretize) getOperations().get(0);
        String text = "Discretization on column(s): " + op.getColumnNames();
        final JLabel jLabel = new JLabel(text);
        jLabel.setToolTipText(text);
        return new JComponent[] {jLabel};
    }
}

