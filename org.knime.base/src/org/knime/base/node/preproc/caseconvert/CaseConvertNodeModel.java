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
 *   15.06.2007 (cebron): created
 */
package org.knime.base.node.preproc.caseconvert;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnDomain;
import org.knime.core.data.DataColumnDomainCreator;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;

/**
 * NodeModel for the CaseConverter Node.
 *
 * @author cebron, University of Konstanz
 */
public class CaseConvertNodeModel extends SimpleStreamableFunctionNodeModel {

    /**
     * Key for the included columns in the NodeSettings.
     */
    public static final String CFG_INCLUDED_COLUMNS = "include";

    /**
     * Key for the uppercase mode in the NodeSettings.
     */
    public static final String CFG_UPPERCASE = "uppercase";

    /*
     * The included columns.
     */
    private String[] m_inclCols;

    /*
     * The selected mode.
     */
    private boolean m_uppercase;


    /**
     * Constructor with one inport and one outport.
     */
    public CaseConvertNodeModel() {
        m_uppercase = true;
        m_inclCols = new String[]{};
    }

    /** {@inheritDoc} */
    @Override
    protected ColumnRearranger createColumnRearranger(
            final DataTableSpec inSpec) throws InvalidSettingsException {
        // find indices to work on.
        int[] indices = new int[m_inclCols.length];
        if (indices.length == 0) {
            setWarningMessage("No columns selected");
        }
        for (int i = 0; i < indices.length; i++) {
            int colIndex = inSpec.findColumnIndex(m_inclCols[i]);
            if (colIndex >= 0) {
                indices[i] = colIndex;
            } else {
                throw new InvalidSettingsException("Column index for "
                        + m_inclCols[i] + " not found.");
            }
        }
        ConverterFactory converterFac =
                new ConverterFactory(indices, inSpec);
        ColumnRearranger colre = new ColumnRearranger(inSpec);
        colre.replace(converterFac, indices);
        return colre;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_inclCols =
                settings.getStringArray(CFG_INCLUDED_COLUMNS,
                        new String[]{});
        m_uppercase =
                settings.getBoolean(CFG_UPPERCASE, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
       settings.addStringArray(CFG_INCLUDED_COLUMNS, m_inclCols);
       settings.addBoolean(CFG_UPPERCASE, m_uppercase);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    }

    /**
     * The CellFactory to produce the new converted cells.
     *
     * @author cebron, University of Konstanz
     */
    private class ConverterFactory implements CellFactory {

        /*
         * Column indices to use.
         */
        private int[] m_colindices;

        /*
         * Original DataTableSpec.
         */
        private DataTableSpec m_spec;

        private Locale m_locale;

        /**
         *
         * @param colindices the column indices to use.
         * @param spec the original DataTableSpec.
         */
        ConverterFactory(final int[] colindices, final DataTableSpec spec) {
            m_colindices = colindices;
            m_spec = spec;
            m_locale = Locale.getDefault();
        }

        /** {@inheritDoc} */
        @Override
        public DataCell[] getCells(final DataRow row) {
            DataCell[] newcells = new DataCell[m_colindices.length];
            for (int i = 0; i < newcells.length; i++) {
                DataCell dc = row.getCell(m_colindices[i]);
                if (!dc.isMissing()) {
                    String newstring = null;
                    if (m_uppercase) {
                        newstring =
                                ((StringValue)dc).getStringValue().toUpperCase(
                                        m_locale);
                    } else {
                        newstring =
                                ((StringValue)dc).getStringValue().toLowerCase(
                                        m_locale);
                    }
                    newcells[i] = new StringCell(newstring);
                } else {
                    newcells[i] = DataType.getMissingCell();
                }
            }
            return newcells;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DataColumnSpec[] getColumnSpecs() {
            DataColumnSpec[] newcolspecs =
                    new DataColumnSpec[m_colindices.length];
            for (int i = 0; i < newcolspecs.length; i++) {
                DataColumnSpec colspec = m_spec.getColumnSpec(m_colindices[i]);
                DataColumnDomain domain = colspec.getDomain();
                Set<DataCell> newdomainvalues = new LinkedHashSet<DataCell>();
                DataColumnSpecCreator colspeccreator =
                        new DataColumnSpecCreator(colspec);
                // can only persist domain if input is StringCell Type
                if (domain.hasValues()
                        && colspec.getType().equals(StringCell.TYPE)) {
                    for (DataCell dc : domain.getValues()) {
                        String newstring = null;
                        if (m_uppercase) {
                            newstring =
                                    ((StringValue)dc).getStringValue()
                                            .toUpperCase(m_locale);
                        } else {
                            newstring =
                                    ((StringValue)dc).getStringValue()
                                            .toLowerCase(m_locale);
                        }
                        newdomainvalues.add(new StringCell(newstring));
                    }
                    DataColumnDomainCreator domaincreator =
                            new DataColumnDomainCreator();
                    domaincreator.setValues(newdomainvalues);
                    colspeccreator.setDomain(domaincreator.createDomain());
                }
                colspeccreator.setType(StringCell.TYPE);
                newcolspecs[i] = colspeccreator.createSpec();
            }
            return newcolspecs;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setProgress(final int curRowNr, final int rowCount,
                final RowKey lastKey, final ExecutionMonitor exec) {
            exec.setProgress((double)curRowNr / (double)rowCount, "Converting");
        }
    } // end ConverterFactory
}
