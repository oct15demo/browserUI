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
package org.knime.base.node.io.database;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.database.DatabaseConnectionPortObject;
import org.knime.core.node.port.database.DatabaseConnectionPortObjectSpec;
import org.knime.core.node.port.database.DatabaseConnectionSettings;
import org.knime.core.node.port.database.reader.DBReader;
import org.knime.core.node.property.hilite.HiLiteHandler;
import org.knime.core.util.MutableInteger;

/**
 *
 * @author Thomas Gabriel, University of Konstanz
 */
final class DatabaseLoopingNodeModel extends DBReaderNodeModel {

    private final SettingsModelString m_columnModel
        = DatabaseLoopingNodeDialogPane.createColumnModel();

    private final SettingsModelBoolean m_aggByRow
        = DatabaseLoopingNodeDialogPane.createAggregateModel();

    private final SettingsModelBoolean m_appendGridColumn
        = DatabaseLoopingNodeDialogPane.createGridColumnModel();

    private final SettingsModelIntegerBounded m_noValues
        = DatabaseLoopingNodeDialogPane.createNoValuesModel();

    private final HiLiteHandler m_hilite = new HiLiteHandler();

    /** Place holder for table name. */
    private static final String TABLE_NAME_PLACE_HOLDER = "<table_name>";
    /** Place holder for table column name. */
    private static final String TABLE_COLUMN_PLACE_HOLDER = "<table_column>";
    /** Place holder for the possible values. */
    private static final String IN_PLACE_HOLDER = "#PLACE_HOLDER_DO_NOT_EDIT#";

    /**
     *
     */
    DatabaseLoopingNodeModel() {
        super(new PortType[]{BufferedDataTable.TYPE, DatabaseConnectionPortObject.TYPE_OPTIONAL},
            new PortType[]{BufferedDataTable.TYPE});
        setQuery("SELECT * FROM " + TABLE_NAME_PLACE_HOLDER
                + " WHERE " + TABLE_COLUMN_PLACE_HOLDER
                + " IN ('" + IN_PLACE_HOLDER + "')");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        final DataTableSpec lastSpec = getLastSpec();
        if (lastSpec != null) {
            return new DataTableSpec[]{lastSpec};
        }
        final DataTableSpec tableSpec = (DataTableSpec)inSpecs[0];
        String column = m_columnModel.getStringValue();
        if (column == null) {
            throw new InvalidSettingsException("No column selected.");
        }
        final int colIdx = tableSpec.findColumnIndex(column);
        if (colIdx < 0) {
            throw new InvalidSettingsException("Column '" + column + "' not found in input data.");
        }

        if ((inSpecs.length > 1) && (inSpecs[1] instanceof DatabaseConnectionPortObjectSpec)) {
            DatabaseConnectionSettings connSettings =
                ((DatabaseConnectionPortObjectSpec)inSpecs[1]).getConnectionSettings(getCredentialsProvider());
            m_settings.setValidateQuery(connSettings.getRetrieveMetadataInConfigure());
        } else {
            m_settings.setValidateQuery(true);
        }
        if (!m_settings.getValidateQuery()) {
            setLastSpec(null);
            return new DataTableSpec[] {null};
        }
        final String oQuery = getQuery();
        PortObjectSpec[] spec = null;
        try {
            final String newQuery;
            newQuery = createDummyValueQuery(tableSpec, colIdx, oQuery);
            setQuery(newQuery);
            spec = new DataTableSpec[] {getResultSpec(inSpecs)};
        } catch (InvalidSettingsException e) {
            setLastSpec(null);
            throw e;
        } catch (SQLException ex) {
            setLastSpec(null);
            Throwable cause = ExceptionUtils.getRootCause(ex);
            if (cause == null) {
                cause = ex;
            }
            throw new InvalidSettingsException("Could not determine table spec from database query: "
                + cause.getMessage(), ex);
        } finally {
            setQuery(oQuery);
        }
        if (spec[0] == null) {
            return spec;
        } else {
            final DataTableSpec resultSpec = createSpec((DataTableSpec)spec[0], tableSpec.getColumnSpec(column));
            setLastSpec(resultSpec);
            return new DataTableSpec[]{resultSpec};
        }
    }

    private static String createDummyValueQuery(final DataTableSpec tableSpec, final int colIdx, final String oQuery) {
        if (tableSpec.getColumnSpec(colIdx).getType().isCompatible(DoubleValue.class)) {
            //this is a numeric column use 0 instead of empty string
            return oQuery.replace(IN_PLACE_HOLDER, "0");
        } else {
            return oQuery.replace(IN_PLACE_HOLDER, "");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
        final BufferedDataTable inputTable = (BufferedDataTable)inData[0];
        final long rowCount = inputTable.size();

        final String column = m_columnModel.getStringValue();
        final DataTableSpec spec = inputTable.getDataTableSpec();
        final int colIdx = spec.findColumnIndex(column);
        if (colIdx < 0) {
            throw new InvalidSettingsException("Column " + column + " not found in input table.");
        }
        final Set<DataCell> values = new HashSet<>();
        BufferedDataContainer buf = null;
        final String oQuery = getQuery();
        final Collection<DataCell> curSet = new LinkedHashSet<>();
        final DBReader load = loadConnectionSettings(inData[getNrInPorts()-1]);
        try {
            final int noValues = m_noValues.getIntValue();
            MutableInteger rowCnt = new MutableInteger(0);
            for (Iterator<DataRow> it = inputTable.iterator(); it.hasNext();) {
                exec.checkCanceled();
                DataCell cell = it.next().getCell(colIdx);
                if (values.contains(cell) && !it.hasNext() && curSet.isEmpty()) {
                    continue;
                }

                values.add(cell);
                curSet.add(cell);
                if (curSet.size() == noValues || !it.hasNext()) {
                    StringBuilder queryValues = new StringBuilder();
                    for (DataCell v : curSet) {
                        if (queryValues.length() > 0) {
                            queryValues.append("','");
                        }
                        queryValues.append(v.toString());
                    }
                    String newQuery = parseQuery(oQuery.replaceAll(IN_PLACE_HOLDER, queryValues.toString()));
                    load.updateQuery(newQuery);
                    exec.setProgress(values.size() * (double)noValues / rowCount, "Selecting all values \""
                        + queryValues + "\"...");
                    final BufferedDataTable table = getResultTable(exec, inData, load);
                    if (buf == null) {
                        DataTableSpec resSpec = table.getDataTableSpec();
                        buf = exec.createDataContainer(createSpec(resSpec,
                                spec.getColumnSpec(column)));
                    }
                    if (m_aggByRow.getBooleanValue()) {
                        aggregate(table, rowCnt, buf,
                                CollectionCellFactory.createListCell(curSet));
                    } else {
                        notAggregate(table, rowCnt, buf,
                                CollectionCellFactory.createListCell(curSet));
                    }
                    curSet.clear();
                }
            }

            if (buf == null) {
                // create empty dummy container with spec generated during #configure
                final PortObjectSpec[] inSpec;
                if ((inData.length > 1) && (inData[1] instanceof DatabaseConnectionPortObject)) {
                    DatabaseConnectionPortObject dbPort = (DatabaseConnectionPortObject)inData[1];
                    inSpec = new PortObjectSpec[]{inputTable.getSpec(), dbPort.getSpec()};
                } else {
                    inSpec = new PortObjectSpec[]{inputTable.getSpec()};
                }
                final String newQuery = createDummyValueQuery(spec, colIdx, oQuery);
                setQuery(newQuery);
                final DataTableSpec resultSpec = getResultSpec(inSpec);
                final DataTableSpec outSpec = createSpec(resultSpec, spec.getColumnSpec(column));
                buf = exec.createDataContainer(outSpec);
            }
            buf.close();
        } catch (CanceledExecutionException cee) {
            throw cee;
        } catch (Exception e) {
            setLastSpec(null);
            throw e;
        } finally {
            // reset query to original
            setQuery(oQuery);
        }
        final BufferedDataTable resultTable = buf.getTable();
        setLastSpec(resultTable.getDataTableSpec());
        return new BufferedDataTable[]{resultTable};
    }

    private DataTableSpec createSpec(final DataTableSpec spec,
            final DataColumnSpec gridSpec) {
        int nrCols = spec.getNumColumns();
        DataColumnSpec[] cspecs;
        if (m_appendGridColumn.getBooleanValue()) {
            cspecs = new DataColumnSpec[nrCols + 1];
            DataColumnSpecCreator crSpec =
                new DataColumnSpecCreator(gridSpec);
            // fix 2971: use column type from underlying cell
            crSpec.setType(ListCell.getCollectionType(gridSpec.getType()));
            if (spec.containsName(gridSpec.getName())) {
                crSpec.setName(spec.getName() + "#" + gridSpec.getName());
            }
            cspecs[nrCols] = crSpec.createSpec();
        } else {
            cspecs = new DataColumnSpec[nrCols];
        }
        for (int i = 0; i < nrCols; i++) {
            DataColumnSpec cspec = spec.getColumnSpec(i);
            if (m_aggByRow.getBooleanValue()) {
                cspec = new DataColumnSpecCreator(cspec.getName(),
                    StringCell.TYPE).createSpec();
            }
            cspecs[i] = cspec;
        }
        return new DataTableSpec(cspecs);
    }

    private void aggregate(final DataTable table, final MutableInteger rowCnt,
            final BufferedDataContainer buf, final DataCell gridValue) {
        final DataTableSpec spec = table.getDataTableSpec();
        @SuppressWarnings("unchecked")
        Set<DataCell>[] values = new LinkedHashSet[spec.getNumColumns()];
        for (final DataRow resRow : table) {
            for (int i = 0; i < values.length; i++) {
                if (values[i] == null) {
                    values[i] = new LinkedHashSet<>(1);
                }
                values[i].add(resRow.getCell(i));
            }
        }
        DataCell[] cells;
        if (m_appendGridColumn.getBooleanValue()) {
            cells = new DataCell[values.length + 1];
            cells[cells.length - 1] = gridValue;
        } else {
            cells = new DataCell[values.length];
        }
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                cells[i] = DataType.getMissingCell();
            } else {
                StringBuilder builder = new StringBuilder();
                for (DataCell cell : values[i]) {
                    if (builder.length() > 0) {
                        builder.append(",");
                    }
                    builder.append(cell.toString());
                }
                cells[i] = new StringCell(builder.toString());
            }
        }
        rowCnt.inc();
        final RowKey rowKey = RowKey.createRowKey(rowCnt.intValue());
        buf.addRowToTable(new DefaultRow(rowKey, cells));
    }

    private void notAggregate(final DataTable table,
            final MutableInteger rowCnt,
            final BufferedDataContainer buf, final DataCell gridValue) {
        for (final DataRow resRow : table) {
            rowCnt.inc();
            final RowKey rowKey = RowKey.createRowKey(rowCnt.intValue());
            // override data row to replace row key
            buf.addRowToTable(new DataRow() {
                private final int m_nrCells =
                    (m_appendGridColumn.getBooleanValue()
                            ? resRow.getNumCells() + 1 : resRow.getNumCells());
                @Override
                public DataCell getCell(final int index) {
                    if (m_appendGridColumn.getBooleanValue()
                            && index == resRow.getNumCells()) {
                        return gridValue;
                    } else {
                        return resRow.getCell(index);
                    }
                }
                @Override
                public RowKey getKey() {
                    return rowKey;
                }
                @Override
                public int getNumCells() {
                    return m_nrCells;
                }
                @Override
                public Iterator<DataCell> iterator() {
                    return resRow.iterator();
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        String query = getQuery();
        if (query.contains(TABLE_COLUMN_PLACE_HOLDER)) {
            throw new InvalidSettingsException(
                    "Table column placeholder not replaced.");
        }
        if (query.contains(TABLE_NAME_PLACE_HOLDER)) {
            throw new InvalidSettingsException(
                    "Table name placeholder not replaced.");
        }
        if (!query.contains(IN_PLACE_HOLDER)) {
            throw new InvalidSettingsException(
                "Do not replace WHERE-clause placeholder in SQL query.");
        }
        m_columnModel.loadSettingsFrom(settings);
        m_aggByRow.loadSettingsFrom(settings);
        m_appendGridColumn.loadSettingsFrom(settings);
        m_noValues.loadSettingsFrom(settings);
        setLastSpec(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_columnModel.saveSettingsTo(settings);
        m_aggByRow.saveSettingsTo(settings);
        m_appendGridColumn.saveSettingsTo(settings);
        m_noValues.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.validateSettings(settings);
        m_columnModel.validateSettings(settings);
        m_aggByRow.validateSettings(settings);
        m_appendGridColumn.validateSettings(settings);
        m_noValues.validateSettings(settings);
        // do not check validateQuery, it does not exist before 2.10
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HiLiteHandler getOutHiLiteHandler(final int outIndex) {
        return m_hilite;
    }
}
