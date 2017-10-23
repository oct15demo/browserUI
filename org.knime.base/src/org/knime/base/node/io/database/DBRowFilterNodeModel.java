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
 */
package org.knime.base.node.io.database;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.database.DatabasePortObject;
import org.knime.core.node.port.database.DatabasePortObjectSpec;
import org.knime.core.node.port.database.DatabaseQueryConnectionSettings;
import org.knime.core.node.port.database.StatementManipulator;
import org.knime.core.node.port.database.reader.DBReader;

/**
 *
 * @author Thomas Gabriel, University of Konstanz
 */
final class DBRowFilterNodeModel extends DBNodeModel {

    private final SettingsModelString m_column =
            DBRowFilterNodeDialogPane.createColumnModel();

    private final SettingsModelString m_operator =
            DBRowFilterNodeDialogPane.createOperatorModel();

    private final SettingsModelString m_value =
            DBRowFilterNodeDialogPane.createValueModel();

    /**
     * Creates a new database reader.
     */
    DBRowFilterNodeModel() {
        super(new PortType[]{DatabasePortObject.TYPE},
                new PortType[]{DatabasePortObject.TYPE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_column.saveSettingsTo(settings);
        m_operator.saveSettingsTo(settings);
        m_value.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.validateSettings(settings);
        m_column.validateSettings(settings);
        m_operator.validateSettings(settings);
        m_value.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        m_column.loadSettingsFrom(settings);
        m_operator.loadSettingsFrom(settings);
        m_value.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec)
            throws CanceledExecutionException, Exception {
        DatabasePortObject dbObj = (DatabasePortObject) inData[0];
        DatabaseQueryConnectionSettings conn = dbObj.getConnectionSettings(getCredentialsProvider());
        String newQuery = createQuery(conn.getQuery(), conn.getUtility().getStatementManipulator());
        conn = createDBQueryConnection(dbObj.getSpec(), newQuery);
        DatabasePortObject outObj = new DatabasePortObject(
                new DatabasePortObjectSpec(dbObj.getSpec().getDataTableSpec(),
                        conn.createConnectionModel()));
        return new PortObject[]{outObj};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        DatabasePortObjectSpec spec = (DatabasePortObjectSpec) inSpecs[0];
        final String columnName = m_column.getStringValue();
        if (columnName == null) {
            throw new InvalidSettingsException("No filter column selected.");
        }
        if (!spec.getDataTableSpec().containsName(columnName)) {
            throw new InvalidSettingsException("Can't filter according to "
                    + "selected column \"" + columnName + "\".");
        }
        DatabaseQueryConnectionSettings conn = spec.getConnectionSettings(getCredentialsProvider());
        String newQuery = createQuery(conn.getQuery(), conn.getUtility().getStatementManipulator());
        conn = createDBQueryConnection(spec, newQuery);
        return new PortObjectSpec[]{new DatabasePortObjectSpec(
                spec.getDataTableSpec(), conn.createConnectionModel())};
    }

    private String createQuery(final String query, final StatementManipulator manipulator) {
        final StringBuilder buf = new StringBuilder();
        final String[] queries = query.split(
                DBReader.SQL_QUERY_SEPARATOR);
        for (int i = 0; i < queries.length - 1; i++) {
            buf.append(queries[i]);
            buf.append(DBReader.SQL_QUERY_SEPARATOR);
        }
        final String selectQuery = queries[queries.length - 1];
        buf.append("SELECT * FROM (" + selectQuery + ") "
                + "table_" + System.identityHashCode(this) + " WHERE ");

        // build WHERE clause
        buf.append(manipulator.quoteColumn(m_column.getStringValue()));
        buf.append(" " + m_operator.getStringValue());
        if (!m_value.getStringValue().trim().isEmpty()) {
            buf.append(" " + m_value.getStringValue().trim());
        }
        return buf.toString();
    }

}
