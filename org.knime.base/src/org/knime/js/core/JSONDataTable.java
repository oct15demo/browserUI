/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 * Created on 19.03.2013 by Christian Albrecht, KNIME.com AG, Zurich, Switzerland
 */
package org.knime.js.core;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.knime.base.data.xml.SvgCell;
import org.knime.base.data.xml.SvgValue;
import org.knime.core.data.BooleanValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValueComparator;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.MissingCell;
import org.knime.core.data.NominalValue;
import org.knime.core.data.RowIterator;
import org.knime.core.data.StringValue;
import org.knime.core.data.date.DateAndTimeCell;
import org.knime.core.data.date.DateAndTimeCellFactory;
import org.knime.core.data.date.DateAndTimeValue;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.image.png.PNGImageContent;
import org.knime.core.data.image.png.PNGImageValue;
import org.knime.core.data.time.duration.DurationCellFactory;
import org.knime.core.data.time.duration.DurationValue;
import org.knime.core.data.time.localdate.LocalDateCellFactory;
import org.knime.core.data.time.localdate.LocalDateValue;
import org.knime.core.data.time.localdatetime.LocalDateTimeCellFactory;
import org.knime.core.data.time.localdatetime.LocalDateTimeValue;
import org.knime.core.data.time.localtime.LocalTimeCellFactory;
import org.knime.core.data.time.localtime.LocalTimeValue;
import org.knime.core.data.time.period.PeriodCellFactory;
import org.knime.core.data.time.period.PeriodValue;
import org.knime.core.data.time.zoneddatetime.ZonedDateTimeCellFactory;
import org.knime.core.data.time.zoneddatetime.ZonedDateTimeValue;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.js.core.JSONDataTableSpec.JSTypes;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;


/**
 *
 * @author Christian Albrecht, KNIME.com AG, Zurich, Switzerland
 * @since 2.9
 */
@JsonAutoDetect
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class JSONDataTable {

    /** Config key to load/save table */
    public static final String KNIME_DATA_TABLE_CONF = "knimeDataTableJSON";

    /** Config key for the table ID. */
    public static final String TABLE_ID = "tableID";

    private static final NodeLogger LOGGER = NodeLogger.getLogger(JSONDataTable.class);

    /* serialized members */
    private String m_id;
    private JSONDataTableSpec m_spec;
    private JSONDataTableRow[] m_rows;
    private Object[][] m_extensions;

    /* builder members */
    private DataTable m_dataTable;
    private int m_firstRow;
    private int m_maxRows;
    private String[] m_excludeColumns;
    private String[] m_includeColumns;
    private boolean m_excludeColumnsWithMissingValues;
    private boolean m_keepFilterColumns;
    private boolean m_excludeRowsWithMissingValues;
    private int m_rowsWithMissingValuesRemoved;
    private String[] m_columnsRemoved;

    /** Empty serialization constructor. Don't use.*/
    public JSONDataTable() {
        // do nothing
    }

    /**
     * Creates a new data table which can be serialized into a JSON string from a given BufferedDataTable.
     * @param dTable the data table to read the rows from
     * @param firstRow the first row to store (must be greater than zero)
     * @param numOfRows the number of rows to store (must be zero or more)
     * @param execMon the object listening to our progress and providing cancel functionality.
     * @throws CanceledExecutionException If the execution of the node has been cancelled.
     * @deprecated Use Builder instead.
     */
    @Deprecated
    public JSONDataTable(final DataTable dTable, final int firstRow,
            final int numOfRows, final ExecutionMonitor execMon)
            throws CanceledExecutionException {
        this(dTable, firstRow, numOfRows, null, new String[0], execMon);
    }

    /**
     * Creates a new data table which can be serialized into a JSON string from a given BufferedDataTable.
     * @param dTable the data table to read the rows from
     * @param firstRow the first row to store (must be greater than zero)
     * @param maxRows the number of rows to store (must be zero or more)
     * @param id An optional id to assign to this table instance
     * @param execMon the object listening to our progress and providing cancel functionality.
     * @throws CanceledExecutionException If the execution of the node has been cancelled.
     * @deprecated Use Builder instead.
     */
    @Deprecated
    public JSONDataTable(final DataTable dTable, final int firstRow,
            final int maxRows, final String id, final ExecutionMonitor execMon)
            throws CanceledExecutionException {
        this(dTable, firstRow, maxRows, id, new String[0], execMon);
    }

    /**
     * Creates a new data table which can be serialized into a JSON string from a given BufferedDataTable.
     * @param dTable the data table to read the rows from
     * @param firstRow the first row to store (must be greater than zero)
     * @param maxRows the number of rows to store (must be zero or more)
     * @param excludeColumns a list of columns to exclude
     * @param execMon the object listening to our progress and providing cancel functionality.
     * @throws CanceledExecutionException If the execution of the node has been cancelled.
     * @deprecated Use Builder instead.
     */
    @Deprecated
    public JSONDataTable(final DataTable dTable, final int firstRow,
            final int maxRows, final String[] excludeColumns, final ExecutionMonitor execMon)
            throws CanceledExecutionException {
        this(dTable, firstRow, maxRows, null, excludeColumns, execMon);
    }

    /**
     * Creates a new data table which can be serialized into a JSON string from a given BufferedDataTable.
     * @param dTable the data table to read the rows from
     * @param firstRow the first row to store (must be greater than zero)
     * @param maxRows the number of rows to store (must be zero or more)
     * @param id An optional id to assign to this table instance
     * @param excludeColumns a list of columns to exclude
     * @param execMon the object listening to our progress and providing cancel functionality.
     * @throws CanceledExecutionException If the execution of the node has been cancelled.
     * @deprecated Use Builder instead.
     */
    @Deprecated
    public JSONDataTable(final DataTable dTable, final int firstRow,
            final int maxRows, final String id, final String[] excludeColumns,
            final ExecutionMonitor execMon)
            throws CanceledExecutionException {
        m_dataTable = dTable;
        m_firstRow = firstRow;
        m_maxRows = maxRows;
        m_id = id;
        m_excludeColumns = excludeColumns;
        buildJSONTable(execMon);
    }

    private void buildJSONTable(final ExecutionMonitor execMon) throws CanceledExecutionException, IllegalArgumentException {
        if (m_dataTable == null) {
            throw new NullPointerException("Must provide non-null data table"
                    + " for DataArray");
        }
        if (m_firstRow < 1) {
            throw new IllegalArgumentException("Starting row must be greater"
                    + " than zero");
        }
        if (m_maxRows < 0) {
            throw new IllegalArgumentException("Number of rows to read must be"
                    + " greater than or equal zero");
        }

        int numOfColumns = 0;
        ArrayList<Integer> includeColIndices = new ArrayList<Integer>();
        List<String> excludedColumns = new ArrayList<String>();
        DataTableSpec spec = m_dataTable.getDataTableSpec();
        for (int i = 0; i < spec.getNumColumns(); i++) {
            String colName = spec.getColumnNames()[i];
            boolean include = true;
            if (m_includeColumns != null) {
                include &= Arrays.asList(m_includeColumns).contains(colName);
            } else if (m_excludeColumns != null) {
                include &= !Arrays.asList(m_excludeColumns).contains(colName);
            }
            if (m_keepFilterColumns && spec.getColumnSpec(i).getFilterHandler().isPresent()) {
                include = true;
            }
            if (include) {
                includeColIndices.add(i);
                numOfColumns++;
            } else {
                excludedColumns.add(colName);
            }
        }
        m_columnsRemoved = excludedColumns.toArray(new String[0]);
        long numOfRows = m_maxRows;
        if (m_dataTable instanceof BufferedDataTable) {
            numOfRows = Math.min(((BufferedDataTable)m_dataTable).size(), m_maxRows);
        }

        //int numOfColumns = spec.getNumColumns();
        DataCell[] maxValues = new DataCell[numOfColumns];
        DataCell[] minValues = new DataCell[numOfColumns];
        Object[] minJSONValues = new Object[numOfColumns];
        Object[] maxJSONValues = new Object[numOfColumns];

        // create a new list for the values - but only for native string columns
        Vector<LinkedHashSet<Object>> possValues = new Vector<LinkedHashSet<Object>>();
        possValues.setSize(numOfColumns);
        String[] filterIds = new String[numOfColumns];
        boolean[] containsMissingValues = new boolean[numOfColumns];
        for (int c = 0; c < numOfColumns; c++) {
            DataColumnSpec columnSpec = spec.getColumnSpec(includeColIndices.get(c));
            if (columnSpec.getType().isCompatible(NominalValue.class)) {
                possValues.set(c, new LinkedHashSet<Object>());
            }
            if (columnSpec.getFilterHandler().isPresent()) {
                filterIds[c] = columnSpec.getFilterHandler().get().getModel().getFilterUUID().toString();
            }
        }

        RowIterator rIter = m_dataTable.iterator();
        int currentRowNumber = 0;
        int numRows = 0;

        ArrayList<String> rowColorList = new ArrayList<String>();
        ArrayList<JSONDataTableRow> rowList = new ArrayList<JSONDataTableRow>();

        while ((rIter.hasNext()) && (currentRowNumber + m_firstRow - 1 < m_maxRows)) {
            // get the next row
            DataRow row = rIter.next();
            currentRowNumber++;

            if (currentRowNumber < m_firstRow) {
                // skip all rows until we see the specified first row
                continue;
            }

            String rC = CSSUtils.cssHexStringFromColor(spec.getRowColor(row).getColor());
            rowColorList.add(rC);

            String rowKey = row.getKey().getString();
            JSONDataTableRow currentRow = new JSONDataTableRow(rowKey, numOfColumns);
            rowList.add(currentRow);
            numRows++;

            // add cells, check min, max values and possible values for each column
            for (int c = 0; c < numOfColumns; c++) {
                int col = includeColIndices.get(c);
                DataCell cell = row.getCell(col);

                Object cellValue;
                if (cell.isMissing()) {
                    cellValue = null;
                    if (m_excludeRowsWithMissingValues) {
                        rowList.remove(currentRow);
                        m_rowsWithMissingValuesRemoved++;
                        numRows--;
                        break;
                    }
                    containsMissingValues[c] = true;
                } else {
                    cellValue = getJSONCellValue(cell);
                }

                currentRow.getData()[c] = cellValue;
                if (cellValue == null) {
                    continue;
                }

                DataValueComparator comp =
                        spec.getColumnSpec(col).getType().getComparator();

                // test the min value
                if (minValues[c] == null) {
                    minValues[c] = cell;
                    minJSONValues[c] = getJSONCellValue(cell);
                } else {
                    if (comp.compare(minValues[c], cell) > 0) {
                        minValues[c] = cell;
                        minJSONValues[c] = getJSONCellValue(cell);
                    }
                }
                // test the max value
                if (maxValues[c] == null) {
                    maxValues[c] = cell;
                    maxJSONValues[c] = getJSONCellValue(cell);
                } else {
                    if (comp.compare(maxValues[c], cell) < 0) {
                        maxValues[c] = cell;
                        maxJSONValues[c] = getJSONCellValue(cell);
                    }
                }
                // add it to the possible values if we record them for this col
                LinkedHashSet<Object> possVals = possValues.get(c);
                if (possVals != null) {
                    // non-string cols have a null list and will be skipped here
                    possVals.add(getJSONCellValue(cell));
                }
            }
            if (execMon != null) {
                execMon.setProgress(((double)currentRowNumber - m_firstRow) / numOfRows,
                    "Creating JSON table. Processing row " + (currentRowNumber - m_firstRow) + " of " + numOfRows);
            }
        }

        // TODO: remove extensions?
        Object[][] extensionArray = null;

        JSONDataTableSpec jsonTableSpec = new JSONDataTableSpec(spec, excludedColumns.toArray(new String[0]), numRows);
        jsonTableSpec.setMinValues(minJSONValues);
        jsonTableSpec.setMaxValues(maxJSONValues);
        jsonTableSpec.setPossibleValues(possValues);
        jsonTableSpec.setRowColorValues(rowColorList.toArray(new String[0]));
        jsonTableSpec.setFilterIds(filterIds);
        jsonTableSpec.setContainsMissingValues(containsMissingValues);

        setSpec(jsonTableSpec);
        setRows(rowList.toArray(new JSONDataTableRow[0]));
        setExtensions(extensionArray);

        if(m_excludeColumnsWithMissingValues) {
            removeMissingValueColumns();
        }
    }

    private synchronized void removeMissingValueColumns() {
        List<String> colsToRemove = new ArrayList<String>();
        boolean[] missingValues = m_spec.getContainsMissingValues();
        String[] filters = m_spec.getFilterIds();
        String[] colNames = m_spec.getColNames();
        for (int i = 0; i < missingValues.length; i++) {
            if (missingValues[i]) {
                if (m_keepFilterColumns && filters[i] != null) {
                    continue;
                }
                colsToRemove.add(colNames[i]);
            }
        }
        if (colsToRemove.size() > 0) {
            m_columnsRemoved = ArrayUtils.addAll(m_columnsRemoved, colsToRemove.toArray(new String[0]));
            for (String colToRemove : colsToRemove) {
                int index = m_spec.getColumnIndex(colToRemove);
                if (index < 0) {
                    continue;
                }
                for (JSONDataTableRow row : m_rows) {
                    row.m_data = ArrayUtils.remove(row.m_data, index);
                }
                m_spec.removeColumn(colToRemove);
            }
        }
    }

    /**
     * Creates a new buffered data table from this table instance.
     * @param exec The execution context
     * @return The newly created {@link BufferedDataTable}.
     */
    public BufferedDataTable createBufferedDataTable(final ExecutionContext exec) {
        DataTableSpec spec = m_spec.createDataTableSpec();
        BufferedDataContainer container = exec.createDataContainer(spec);
        for (JSONDataTableRow row : m_rows) {
            DataCell[] dataCells = new DataCell[row.getData().length];
            for (int colId = 0; colId < row.getData().length; colId++) {
                Object value = row.getData()[colId];
                DataType type = spec.getColumnSpec(colId).getType();
                if (type.isCompatible(SvgValue.class)) {
                    try {
                        dataCells[colId] = new SvgCell(value.toString());
                    } catch (IOException e) {
                        dataCells[colId] = new MissingCell(e.getMessage());
                    }
                } else if (type.isCompatible(PNGImageValue.class)) {
                    byte[] imageBytes = Base64.decodeBase64(value.toString());
                    dataCells[colId] = (new PNGImageContent(imageBytes)).toImageCell();
                } else if (type.isCompatible(BooleanValue.class)) {
                    Boolean bVal = null;
                    if (value instanceof Boolean) {
                        bVal = (Boolean)value;
                    } else if (value instanceof String) {
                        bVal = Boolean.parseBoolean((String)value);
                    }
                    if (bVal == null) {
                        dataCells[colId] = new MissingCell("Value " + value + "could not be parsed as boolean.");
                    } else {
                        dataCells[colId] = BooleanCell.get(bVal);
                    }
                } else if (type.isCompatible(DateAndTimeValue.class)) {
                    Long lVal = null;
                    // Is value a long? (old format for DateAndTime)
                    if (value instanceof Long) {
                        lVal = (Long)value;
                    } else {
                        try {
                            lVal = Long.parseLong((String)value);
                        } catch(NumberFormatException e) {
                            lVal = null;
                        }
                    }

                    if (lVal == null) {
                        // Parse string value to DateAndTime.
                        try {
                            dataCells[colId] = DateAndTimeCellFactory.create((String)value);
                        } catch (IllegalArgumentException ex) {
                            dataCells[colId] = new MissingCell("Value " + value + "could not be parsed as date and time.");
                        }
                    } else {
                        // Parse long value to DateAndTime.
                        dataCells[colId] = new DateAndTimeCell(lVal, true, true, true);
                    }
                } else if (type.isCompatible(LocalDateValue.class)) {
                    if (value instanceof String) {
                        try {
                            dataCells[colId] = LocalDateCellFactory.create((String)value);
                        } catch (DateTimeParseException ex) {
                            dataCells[colId] = new MissingCell("Value " + value + "could not be parsed as local date.");
                        } catch (IllegalArgumentException ex) {
                            dataCells[colId] = new MissingCell("Value " + value + "could not be parsed as local date.");
                        }
                    } else {
                        dataCells[colId] = new MissingCell("Value " + value + "could not be parsed as string.");
                    }
                } else if (type.isCompatible(LocalDateTimeValue.class)) {
                    if (value instanceof String) {
                        try {
                            dataCells[colId] = LocalDateTimeCellFactory.create((String)value);
                        } catch (DateTimeParseException ex) {
                            dataCells[colId] = new MissingCell("Value " + value + "could not be parsed as local date and time.");
                        } catch (IllegalArgumentException ex) {
                            dataCells[colId] = new MissingCell("Value " + value + "could not be parsed as local date and time.");
                        }
                    } else {
                        dataCells[colId] = new MissingCell("Value " + value + "could not be parsed as string.");
                    }
                } else if (type.isCompatible(LocalTimeValue.class)) {
                    if (value instanceof String) {
                        try {
                            dataCells[colId] = LocalTimeCellFactory.create((String)value);
                        } catch (DateTimeParseException ex) {
                            dataCells[colId] = new MissingCell("Value " + value + "could not be parsed as local time.");
                        } catch (IllegalArgumentException ex) {
                            dataCells[colId] = new MissingCell("Value " + value + "could not be parsed as local time.");
                        }
                    } else {
                        dataCells[colId] = new MissingCell("Value " + value + "could not be parsed as string.");
                    }
                } else if (type.isCompatible(ZonedDateTimeValue.class)) {
                    if (value instanceof String) {
                        try {
                            dataCells[colId] = ZonedDateTimeCellFactory.create((String)value);
                        } catch (DateTimeParseException ex) {
                            dataCells[colId] = new MissingCell("Value " + value + "could not be parsed as zoned date and time.");
                        } catch (IllegalArgumentException ex) {
                            dataCells[colId] = new MissingCell("Value " + value + "could not be parsed as zoned date and time.");
                        }
                    } else {
                        dataCells[colId] = new MissingCell("Value " + value + "could not be parsed as string.");
                    }
                } else if (type.isCompatible(DurationValue.class)) {
                    if (value instanceof String) {
                        try {
                            dataCells[colId] = DurationCellFactory.create((String)value);
                        } catch (DateTimeParseException ex) {
                            dataCells[colId] = new MissingCell("Value " + value + "could not be parsed as duration.");
                        } catch (IllegalArgumentException ex) {
                            dataCells[colId] = new MissingCell("Value " + value + "could not be parsed as duration.");
                        }
                    } else {
                        dataCells[colId] = new MissingCell("Value " + value + "could not be parsed as string.");
                    }
                } else if (type.isCompatible(PeriodValue.class)) {
                    if (value instanceof String) {
                        try {
                            dataCells[colId] = PeriodCellFactory.create((String)value);
                        } catch (DateTimeParseException ex) {
                            dataCells[colId] = new MissingCell("Value " + value + "could not be parsed as period.");
                        } catch (IllegalArgumentException ex) {
                            dataCells[colId] = new MissingCell("Value " + value + "could not be parsed as as period.");
                        }
                    } else {
                        dataCells[colId] = new MissingCell("Value " + value + "could not be parsed as string.");
                    }
                } else if (type.isCompatible(IntValue.class)) {
                    Number nVal = null;
                    if (value instanceof Number) {
                        nVal = (Number)value;
                    } else  if (value instanceof String) {
                        // CHECK: Should there be exception handling here?
                        nVal = Integer.parseInt((String)value);
                    }
                    if (nVal == null) {
                        dataCells[colId] = new MissingCell("Value " + value + "could not be parsed as number.");
                    } else {
                        dataCells[colId] = new IntCell(nVal.intValue());
                    }
                } else if (type.isCompatible(DoubleValue.class)) {
                    Number nVal = null;
                    if (value instanceof Number) {
                        nVal = (Number)value;
                    } else  if (value instanceof String) {
                        // CHECK: Should there be exception handling here?
                        nVal = Double.parseDouble((String)value);
                    }
                    if (nVal == null) {
                        dataCells[colId] = new MissingCell("Value " + value + "could not be parsed as number.");
                    } else {
                        dataCells[colId] = new DoubleCell(nVal.doubleValue());
                    }
                } else if (type.isCompatible(StringValue.class)) {
                    dataCells[colId] = new StringCell(value.toString());
                } else {
                    dataCells[colId] = new MissingCell("Type conversion to " + type + " not supported.");
                }
            }
            DataRow newRow = new DefaultRow(row.getRowKey(), dataCells);
            container.addRowToTable(newRow);
        }
        container.close();
        return container.getTable();
    }

    private Object getJSONCellValue(final DataCell cell) {
        JSTypes jsType = JSONDataTableSpec.getJSONType(cell.getType());
        switch (jsType) {
            case BOOLEAN:
                return ((BooleanValue)cell).getBooleanValue();
            case DATE_TIME:
                if (cell.getType().isCompatible(DateAndTimeValue.class)) {
                    //TODO: legacy date/time needs to return ISO-8601 string as well?
                    // does it work with timestamps?
                    return ((DateAndTimeValue)cell).getUTCTimeInMillis();
                }
                //all other date and time types return ISO-8601 formatted string
                return cell.toString();
            case NUMBER:
                return ((DoubleValue)cell).getDoubleValue();
            case STRING:
                return ((StringValue)cell).getStringValue();
            case PNG:
                return new String(Base64.encodeBase64(((PNGImageValue)cell).getImageContent().getByteArray()));
            case SVG:
                return ((SvgValue)cell).toString();
            default:
                return cell.toString();
        }
    }

    /**
     * @return the id
     */
    public String getId() {
        return m_id;
    }

    /**
     * @param id the id to set
     */
    public void setId(final String id) {
        m_id = id;
    }

    /**
     * @return the spec
     */
    public JSONDataTableSpec getSpec() {
        return m_spec;
    }

    /**
     * @param spec the spec to set
     */
    public void setSpec(final JSONDataTableSpec spec) {
        m_spec = spec;
    }

    /**
     * @return the table rows
     * @since 2.10
     */
    public JSONDataTableRow[] getRows() {
        return m_rows;
    }

    /**
     * @param rows the rows to set
     * @since 2.10
     */
    public void setRows(final JSONDataTableRow[] rows) {
        m_rows = rows;
    }

    /**
     * @return the extension defined on the table
     */
    public Object[][] getExtensions() {
        return m_extensions;
    }

    /**
     * @param extensions the extensions to set on the table
     */
    public void setExtensions(final Object[][] extensions) {
        m_extensions = extensions;
    }

    /**
     * @return the number of rows removed during build, due to missing values, can only be called right after a build
     */
    @JsonIgnore
    public int numberRemovedRowsWithMissingValues() {
        return m_rowsWithMissingValuesRemoved;
    }

    /**
     * @return an array of all column names removed during the build of the table, may be null, can only be called right
     *         after a build
     */
    @JsonIgnore
    public String[] getColumnsRemovedDuringBuild() {
        return m_columnsRemoved;
    }

    /**
     *
     * @author Christian Albrecht, KNIME.com AG, Zurich, Switzerland
     * @since 2.10
     */
    @JsonAutoDetect
    public static class JSONDataTableRow {

        private String m_rowKey;

        private Object[] m_data;

        /** Empty serialization constructor. Don't use.*/
        public JSONDataTableRow() { }

        public JSONDataTableRow(final String rowKey, final int numColumns) {
            m_rowKey = rowKey;
            m_data = new Object[numColumns];
        }

        public JSONDataTableRow(final String rowKey, final Object[] data) {
            m_rowKey = rowKey;
            m_data = data;
        }

        /**
         * @return the rowKey
         */
        public String getRowKey() {
            return m_rowKey;
        }

        /**
         * @param rowKey the rowKey to set
         */
        public void setRowKey(final String rowKey) {
            m_rowKey = rowKey;
        }

        /**
         * @return the data
         */
        public Object[] getData() {
            return m_data;
        }

        /**
         * @param data the data to set
         * @since 2.10
         */
        public void setData(final Object[] data) {
            m_data = data;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return new HashCodeBuilder()
                    .append(m_rowKey)
                    .append(m_data)
                    .toHashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            JSONDataTableRow other = (JSONDataTableRow)obj;
            return new EqualsBuilder()
                    .append(m_rowKey, other.m_rowKey)
                    .append(m_data, other.m_data)
                    .isEquals();
        }
    }

    /**
     * @return A new table builder to which table options can be added incrementally.
     */
    @JsonIgnore
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Builder for a {@link JSONDataTable}. */
    @JsonIgnoreType
    public static class Builder {

        private String m_id = null;
        private DataTable m_dataTable = null;
        private Integer m_firstRow = null;
        private Integer m_maxRows = null;
        private String[] m_excludeColumns = null;
        private String[] m_includeColumns = null;
        private Boolean m_excludeColumnsWithMissingValues = null;
        private Boolean m_keepFilterColumns = null;
        private Boolean m_excludeRowsWithMissingValues = null;

        private Builder() { /* simple hidden default constructor */ }

        /**
         * @param id the table ID
         * @return This builder instance, which can be used for method chaining.
         */
        public Builder setId(final String id) {
            m_id = id;
            return this;
        }

        /**
         * @param dataTable the table to be serialized
         * @return This builder instance, which can be used for method chaining.
         */
        public Builder setDataTable(final DataTable dataTable) {
            m_dataTable = dataTable;
            return this;
        }

        /**
         * @param firstRow the first row number to be included in the result, must be larger than zero
         * @return This builder instance, which can be used for method chaining.
         */
        public Builder setFirstRow(final int firstRow) {
            m_firstRow = firstRow;
            return this;
        }

        /**
         * @param maxRows the maximum number of rows to include in the result, must be larger than zero
         * @return This builder instance, which can be used for method chaining.
         */
        public Builder setMaxRows(final int maxRows) {
            m_maxRows = maxRows;
            return this;
        }

        /**
         * @param excludeColumns an array of columns to exclude during the build of the JSONDataTable. Not listed columns are included.
         * Include and exclude columns are mutually exclusive and can not both be set.
         * Can be used in conjunction with {@link #keepFilterColumns(boolean)}.
         * To query if and how many columns where excluded during build call {@link JSONDataTable#getColumnsRemovedDuringBuild()}.
         * @return This builder instance, which can be used for method chaining.
         */
        public Builder setExcludeColumns(final String[] excludeColumns) {
            m_excludeColumns = excludeColumns;
            return this;
        }

        /**
         * @param includeColumns an array of columns to include during the build of the JSONDataTable. Not listed columns are excluded.
         * Include and exclude columns are mutually exclusive and can not both be set.
         * Can be used in conjunction with {@link #keepFilterColumns(boolean)}.
         * To query if and how many columns where excluded during build call {@link JSONDataTable#getColumnsRemovedDuringBuild()}.
         * @return This builder instance, which can be used for method chaining.
         */
        public Builder setIncludeColumns(final String[] includeColumns) {
            m_includeColumns = includeColumns;
            return this;
        }

        /**
         * @param exclude true, if columns containing missing values should be excluded, false otherwise.
         * This can be used in conjunction with {@link #keepFilterColumns(boolean)}.
         * To query if and how many columns where excluded during build call {@link JSONDataTable#getColumnsRemovedDuringBuild()}.
         * @return This builder instance, which can be used for method chaining.
         */
        public Builder excludeColumnsWithMissingValues(final boolean exclude) {
            m_excludeColumnsWithMissingValues = exclude;
            return this;
        }

        /**
         * @param keep true, if columns with existing filter definitions should be kept,
         * even if they are listed in exclude columns or would be omitted by missing value handling.
         * @return This builder instance, which can be used for method chaining.
         */
        public Builder keepFilterColumns(final boolean keep) {
            m_keepFilterColumns = keep;
            return this;
        }

        /**
         * @param exclude true, if rows containing missing values should be excluded, false otherwise.
         * To query if and how many rows where excluded during build call {@link JSONDataTable#numberRemovedRowsWithMissingValues()}.
         * @return This builder instance, which can be used for method chaining.
         */
        public Builder excludeRowsWithMissingValues(final boolean exclude) {
            m_excludeRowsWithMissingValues = exclude;
            return this;
        }

        /**
         * Builds a new JSONDataTable instance from the current configuration of this builder.
         *
         * @param exec an execution monitor for setting progress, may be null
         * @return a new JSONDataTable instance
         * @throws CanceledExecutionException If the execution of the node has been cancelled.
         * @throws IllegalArgumentException If the configuration of the builder is faulty.
         */
        public JSONDataTable build(final ExecutionMonitor exec) throws CanceledExecutionException, IllegalArgumentException {
            JSONDataTable result = new JSONDataTable();
            if (m_dataTable == null) {
                throw new IllegalArgumentException("Must provide non-null data table for JSONDataTable construction.");
            }
            result.m_dataTable = m_dataTable;
            if (m_id != null) {
                result.m_id = m_id;
            }
            if (m_firstRow == null) {
                //first row defaults to one
                m_firstRow = 1;
            }
            if (m_maxRows == null) {
                if (m_dataTable instanceof BufferedDataTable) {
                    //max rows defaults to table size
                     m_maxRows = Math.toIntExact(Math.min(Integer.MAX_VALUE, ((BufferedDataTable)m_dataTable).size()));
                } else {
                    // can't determine default, this will result in building error
                    m_maxRows = 0;
                }
            }
            result.m_firstRow = m_firstRow;
            result.m_maxRows = m_maxRows;
            if (m_excludeColumns != null && m_includeColumns != null) {
                throw new IllegalArgumentException("Exclude and include columns are mutually exclusive and cannot both be set");
            }
            if (m_excludeColumns != null) {
                result.m_excludeColumns = m_excludeColumns;
            }
            if (m_includeColumns != null) {
                result.m_includeColumns = m_includeColumns;
            }
            if (m_excludeColumnsWithMissingValues != null) {
                result.m_excludeColumnsWithMissingValues = m_excludeColumnsWithMissingValues;
            }
            if (m_keepFilterColumns != null) {
                result.m_keepFilterColumns = m_keepFilterColumns;
            }
            if (m_excludeRowsWithMissingValues != null) {
                result.m_excludeRowsWithMissingValues = m_excludeRowsWithMissingValues;
            }
            result.buildJSONTable(exec);
            return result;
        }

    }

    /**
     * @param settings the settings to load from
     * @since 2.10
     */
    @JsonIgnore
    public void saveJSONToNodeSettings(final NodeSettingsWO settings) {
        ObjectMapper mapper = new ObjectMapper();
        String tableString = null;
        try {
            tableString = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) { /*do nothing*/ }
        settings.addString(KNIME_DATA_TABLE_CONF, tableString);
    }

    /**
     * Loads a table from the settings given. If any errors occur null is returned.
     * @param settings the settings to load from
     * @return the table
     * @since 2.10
     */
    @JsonIgnore
    public static JSONDataTable loadFromNodeSettings(final NodeSettingsRO settings) {
        String tableString = settings.getString(KNIME_DATA_TABLE_CONF, null);
        if (tableString == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JSONDataTable table = new JSONDataTable();
        ObjectReader reader = mapper.readerForUpdating(table);
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(table.getClass().getClassLoader());
            reader.readValue(tableString);
            return table;
        } catch (IOException e) {
            LOGGER.error("Unable to load JSON data table: " + e.getMessage(), e);
            return null;
        } finally {
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(m_id)
                .append(m_spec)
                .append(m_rows)
                .append(m_extensions)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        JSONDataTable other = (JSONDataTable)obj;
        return new EqualsBuilder()
                .append(m_id, other.m_id)
                .append(m_spec, other.m_spec)
                .append(m_rows, other.m_rows)
                .append(m_extensions, other.m_extensions)
                .isEquals();
    }
}
