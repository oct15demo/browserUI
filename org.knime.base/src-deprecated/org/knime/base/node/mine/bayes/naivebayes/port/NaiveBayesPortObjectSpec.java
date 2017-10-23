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
package org.knime.base.node.mine.bayes.naivebayes.port;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.config.Config;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;


/**
 * The Naive Bayes specific port object specification implementation.
 *
 * @author Tobias Koetter, University of Konstanz
 * @deprecated the new version uses PMML as data transfer protocol instead of a proprietary one
 */
@Deprecated
public class NaiveBayesPortObjectSpec extends AbstractSimplePortObjectSpec {
    public static final class Serializer extends AbstractSimplePortObjectSpecSerializer<NaiveBayesPortObjectSpec> {}

    private static final String CNFG_CLASS_COL = "classCol";

    private static final String CNFG_SPEC = "trainingTableSpec";

    private DataTableSpec m_tableSpec;

    private DataColumnSpec m_classColumn;

    /**Constructor for class NaiveBayesPortObjectSpec.
     */
    public NaiveBayesPortObjectSpec() {
        // needed for loading
    }

    /**Constructor for class NaiveBayesPortObjectSpec.
     * @param traingDataSpec the {@link DataTableSpec} of the training data
     * table
     * @param classColumn the name of the class column
     */
    public NaiveBayesPortObjectSpec(final DataTableSpec traingDataSpec,
            final DataColumnSpec classColumn) {
        if (traingDataSpec == null) {
            throw new NullPointerException("traingDataSpec must not be null");
        }
        if (classColumn == null) {
            throw new NullPointerException("classColumn must not be null");
        }
        m_tableSpec = traingDataSpec;
        m_classColumn = classColumn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void load(final ModelContentRO model)
    throws InvalidSettingsException {
        final Config specModel = model.getConfig(CNFG_SPEC);
        m_tableSpec = DataTableSpec.load(specModel);
        final ModelContentRO classColModel =
            model.getModelContent(CNFG_CLASS_COL);
        m_classColumn = DataColumnSpec.load(classColModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void save(final ModelContentWO model) {
        final Config specModel = model.addConfig(CNFG_SPEC);
        m_tableSpec.save(specModel);
        final ModelContentWO classColModel =
            model.addModelContent(CNFG_CLASS_COL);
        m_classColumn.save(classColModel);
    }


    /**
     * @return the tableSpec of the training data
     */
    public DataTableSpec getTableSpec() {
        return m_tableSpec;
    }


    /**
     * @return the column that contained the classes
     */
    public DataColumnSpec getClassColumn() {
        return m_classColumn;
    }
}
