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
 *   Jun 19, 2007 (ohl): created
 */
package org.knime.base.node.preproc.cellsplit;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * Model of the node that splits one column into many, based on a user specified
 * delimiter.
 * 
 * @author ohl, University of Konstanz
 */
public class CellSplitterNodeModel extends NodeModel {

    private CellSplitterSettings m_settings = new CellSplitterSettings();

    /**
     * The constructor.
     */
    public CellSplitterNodeModel() {
        super(1, 1); // one data input, one data output
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {

        String errMsg = m_settings.getStatus(inSpecs[0]);
        if (errMsg != null) {
            throw new InvalidSettingsException(errMsg);
        }

        // obsolete as we allow only string columns in the getStatus now.

        // // warn the user if a column of other than string type is selected
        // if ((inSpecs != null) && (inSpecs[0] != null)) {
        // DataColumnSpec cSpec =
        // inSpecs[0].getColumnSpec(m_settings.getColumnName());
        // if ((cSpec != null)
        // && (!cSpec.getType().isCompatible(StringValue.class))) {
        // setWarningMessage("The selected column is not of "
        // + "type 'String'");
        // }
        // }

        // only if we don't need to guess the type we set it here.
        // Guessing is done in the execute method
        if (!m_settings.isGuessNumOfCols()) {
            try {
                m_settings = CellSplitterCellFactory.createNewColumnTypes(null,
                                m_settings, null);
            } catch (CanceledExecutionException cee) {
                // can't happen
            }
        }

        DataTableSpec outSpec = null;

        if ((inSpecs[0] != null) 
           && (((!m_settings.isGuessNumOfCols()) && m_settings.isOutputAsCols())
                || !m_settings.isOutputAsCols())) {
            // if we are supposed to guess we don't know the num of cols here
            outSpec = createColumnRearranger(inSpecs[0]).createSpec();
        }

        return new DataTableSpec[]{outSpec};

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

        // sanity check. Shouldn't go off.
        String err = m_settings.getStatus(inData[0].getDataTableSpec());
        if (err != null) {
            throw new IllegalStateException(err);
        }

        m_settings =
                CellSplitterCellFactory.createNewColumnTypes(inData[0],
                        m_settings, exec.createSubExecutionContext(0.5));

        BufferedDataTable outTable =
                exec.createColumnRearrangeTable(inData[0],
                        createColumnRearranger(inData[0].getDataTableSpec()),
                        exec.createSubExecutionContext(0.5));

        return new BufferedDataTable[]{outTable};
    }

    private ColumnRearranger createColumnRearranger(
            final DataTableSpec inTableSpec) {
        ColumnRearranger c = new ColumnRearranger(inTableSpec);
        c.append(new CellSplitterCellFactory(inTableSpec, m_settings));
        return c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // no internals to save here.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_settings = new CellSplitterSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // nothing to reset today
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // nothing worth saving
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_settings.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        CellSplitterSettings s = new CellSplitterSettings(settings);
        String msg = s.getStatus(null);
        if (msg != null) {
            throw new InvalidSettingsException(msg);
        }
    }

}
