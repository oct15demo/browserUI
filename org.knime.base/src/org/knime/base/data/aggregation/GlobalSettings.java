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
 */

package org.knime.base.data.aggregation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.util.CheckUtils;


/**
 * Utility class that contains general information such as the
 * column delimiter and the total number of rows.
 * The informations might be provided by the user in the node dialog.
 *
 * @author Tobias Koetter, University of Konstanz
 */
public class GlobalSettings {

    /**
     * A context the aggregation is performed in (e.g. in the GroupBy-node -> row aggregation; in the Column
     * Aggregator-node -> column aggregation).
     * @since 3.3
     */
    public static enum AggregationContext {
            /**
             * Aggregation of rows.
             */
            ROW_AGGREGATION,

            /**
             * Aggregation of columns.
             */
            COLUMN_AGGREGATION,

            /**
             * Unknown.
             */
            UNKNOWN;
    }

    /**Default global settings object that should be only used in
     * operator templates!!!*/
    public static final GlobalSettings DEFAULT = new GlobalSettings();

    /**The standard delimiter used in concatenation operators.*/
    public static final String STANDARD_DELIMITER = ", ";

    /**The maximum number of unique values. the threshold is used
     * if the method uses a limit.*/
    private final int m_maxUniqueValues;

    /**The delimiter to use for value separation.*/
    private final String m_valueDelimiter;

    private final DataTableSpec m_spec;

    private final long m_noOfRows;

    /**This key value map allows the storing of arbitrary objects associated
     * with a unique key which are accessible in the
     * <code>AggregationMethod</code> implementations.
     */
    private final Map<String, Object> m_keyValueMap;

    private final List<String> m_groupColNames;

    private final FileStoreFactory m_fileStoreFactory;

    private final AggregationContext m_aggregationContext;

    /**Constructor for class GlobalSettings.
     * This constructor is used to create a dummy object that contains
     * default settings.
     */
    private GlobalSettings() {
        this(0);
    }

    /**Constructor for class GlobalSettings that uses the standard
     * value delimiter.
     *
     * @param maxUniqueValues the maximum number of unique values to consider
     * @see #GlobalSettings(FileStoreFactory, List, int, String, DataTableSpec, int)
     */
    @Deprecated
    public GlobalSettings(final int maxUniqueValues) {
        this(maxUniqueValues, STANDARD_DELIMITER,
                new DataTableSpec(), 0);
    }

    /**Constructor for class GlobalSettings.
     * @param maxUniqueValues the maximum number of unique values to consider
     * @param valueDelimiter the delimiter to use for value separation
     * @param spec the {@link DataTableSpec} of the input table
     * @param noOfRows the number of rows of the input table
     * @see #GlobalSettings(FileStoreFactory, List, int, String, DataTableSpec, int)
     */
    @Deprecated
    public GlobalSettings(final int maxUniqueValues,
            final String valueDelimiter, final DataTableSpec spec,
            final int noOfRows) {
        this((FileStoreFactory)null, Collections.emptyList(), maxUniqueValues,
                valueDelimiter, spec, noOfRows);
    }

    /**Constructor for class GlobalSettings.
     * @param newSpec the {@link DataTableSpec} of the table to process
     * @param oldSettings the {@link GlobalSettings} object to change the
     * {@link DataTableSpec}
     * @since 2.6
     */
    public GlobalSettings(final DataTableSpec newSpec,
            final GlobalSettings oldSettings) {
        this(oldSettings.m_fileStoreFactory, oldSettings.m_groupColNames,
                oldSettings.m_maxUniqueValues, oldSettings.m_valueDelimiter,
                newSpec, oldSettings.m_noOfRows, oldSettings.m_keyValueMap, oldSettings.m_aggregationContext);
    }

    /**Constructor for class GlobalSettings.
     * @param fileStoreFactory the {@link FileStoreFactory}
     * @param groupColNames the names of the columns to group by
     * @param maxUniqueValues the maximum number of unique values to consider
     * @param valueDelimiter the delimiter to use for value separation
     * @param spec the {@link DataTableSpec} of the table to process
     * @param noOfRows the number of rows of the input table
     * @deprecated use the {@link GlobalSettingsBuilder} instead (via {@link #builder()})
     * @since 3.0
     */
    @Deprecated
    public GlobalSettings(final FileStoreFactory fileStoreFactory,
            final List<String> groupColNames, final int maxUniqueValues,
            final String valueDelimiter, final DataTableSpec spec,
            final long noOfRows) {
        this(fileStoreFactory, groupColNames, maxUniqueValues, valueDelimiter,
                spec, noOfRows, new HashMap<String, Object>(), AggregationContext.UNKNOWN);
    }

    /**Constructor for class GlobalSettings.
     * @param fileStoreFactory the {@link FileStoreFactory}
     * @param groupColNames the names of the columns to group by
     * @param maxUniqueValues the maximum number of unique values to consider
     * @param valueDelimiter the delimiter to use for value separation
     * @param spec the {@link DataTableSpec} of the table to process
     * @param noOfRows the number of rows of the input table
     * @param aggregationContext Aggregation context, not null
     * @since 2.6
     */
    private GlobalSettings(final FileStoreFactory fileStoreFactory,
            final List<String> groupColNames, final int maxUniqueValues,
            final String valueDelimiter, final DataTableSpec spec,
            final long noOfRows, final Map<String, Object> keyValueMap, final AggregationContext aggregationContext) {
        this(builder()
            .setFileStoreFactory(fileStoreFactory)
            .setGroupColNames(groupColNames)
            .setMaxUniqueValues(maxUniqueValues)
            .setValueDelimiter(valueDelimiter)
            .setDataTableSpec(spec)
            .setNoOfRows(noOfRows)
            .setKeyValueMap(keyValueMap)
            .setAggregationContext(aggregationContext));
    }

    /**
     * @param builder builder to create a new {@link GlobalSettings} instance from
     */
    private GlobalSettings(final GlobalSettingsBuilder builder) {
        m_fileStoreFactory = builder.m_fileStoreFactory;
        m_groupColNames = CheckUtils.checkArgumentNotNull(builder.m_groupColNames, "groupColNames must not be null");
        m_maxUniqueValues = builder.m_maxUniqueValues;
        CheckUtils.checkArgument(m_maxUniqueValues >= 0, "Maximum unique values must be a positive integer");
        m_valueDelimiter = CheckUtils.checkArgumentNotNull(builder.m_valueDelimiter,
            "Value delimiter should not be null");
        m_spec = CheckUtils.checkArgumentNotNull(builder.m_spec, "spec must not be null");
        m_noOfRows = builder.m_noOfRows;
        //negative row count is allowed, meaning it is not available
        //        if (noOfRows < 0) {
        //            throw new IllegalArgumentException("No of rows must be positive");
        //        }
        m_keyValueMap = CheckUtils.checkArgumentNotNull(builder.m_keyValueMap, "keyValueMap must not be null");
        m_aggregationContext =
                CheckUtils.checkArgumentNotNull(builder.m_aggregationContext, "aggregationContext must not be null");
    }

    /**
     * @return the maximum number of unique values to consider
     */
    public int getMaxUniqueValues() {
        return m_maxUniqueValues;
    }

    /**
     * @return the standard delimiter to use for value separation
     */
    public String getValueDelimiter() {
        return m_valueDelimiter;
    }


    /**
     * @return the total number of rows of the input table, -1 if not available (e.g. if used in a streaming environment)
     * @since 3.0
     */
    public long getNoOfRows() {
        return m_noOfRows;
    }

    /**
     * Returns the number of columns of the original input table.
     *
     * @return the number of columns
     */
    public int getNoOfColumns() {
        return m_spec.getNumColumns();
    }

    /**
     * Returns the number of items to be aggregated. Its either the number of rows or the number of columns, depending
     * on the context the aggregation is performed in, i.e. an aggregation of columns (Column Aggregator) or of rows
     * (GroupBy).
     *
     * @return number of items to be aggregated, might be -1 if not available (e.g. the number of rows in a streaming
     *         environment)
     * @throws IllegalStateException if no aggregation context was provided during construction
     * @since 3.3
     */
    public long getNoOfItems() {
        switch (m_aggregationContext) {
            case ROW_AGGREGATION:
                return getNoOfRows();
            case COLUMN_AGGREGATION:
                return getNoOfColumns();
            default:
                throw new IllegalStateException("Unknown aggregation context (row vs. column unknown)");
        }
    }

    /**
     * Finds the column with the specified name in the TableSpec of the
     * original input table and returns its index, or -1 if the name
     * doesn't exist in the table. This method returns
     * -1 if the argument is <code>null</code>.
     *
     * @param columnName the name to search for
     * @return the index of the column with the specified name, or -1 if not
     *         found.
     */
    public int findColumnIndex(final String columnName) {
        return m_spec.findColumnIndex(columnName);
    }

    /**
     * Returns column information of the original column with
     * the provided index.
     *
     * @param index the column index within the table
     * @return the column specification
     * @throws ArrayIndexOutOfBoundsException if the index is out of range
     */
    public DataColumnSpec getOriginalColumnSpec(final int index) {
        return m_spec.getColumnSpec(index);
    }

    /**
     * Returns the {@link DataColumnSpec} of the original column with the
     * provided name.
     * This method returns <code>null</code> if the argument is
     * <code>null</code>.
     *
     * @param column the column name to find the spec for
     * @return the column specification or <code>null</code> if not available
     */
    public DataColumnSpec getOriginalColumnSpec(final String column) {
        return m_spec.getColumnSpec(column);
    }


    /**
     * Returns the {@link FileStoreFactory} that can be used to create.
     * The method returns <code>null</code> if the {@link FileStoreFactory} is
     * not available.
     *
     * @return the {@link FileStoreFactory} to create new {@link FileStore}
     * objects. The method might return <code>null</code> if the
     * {@link FileStoreFactory} is not available
     * @since 2.6
     */
    public FileStoreFactory getFileStoreFactory() {
        return m_fileStoreFactory;
    }

    /**
     * @return unmodifiable {@link List} that contains the names of the columns
     * to group by
     * @since 2.6
     */
    public List<String> getGroupColNames() {
        return Collections.unmodifiableList(m_groupColNames);
    }

    /**
     * Allows the adding of arbitrary key value pairs.
     * @param map the {@link Map} with the values to add
     * @since 2.6
     */
    public void addValues(final Map<String, Object> map) {
        if (map == null) {
            throw new NullPointerException("Map must not be null");
        }
        try {
            if (map.containsKey(null)) {
                throw new IllegalArgumentException(
                        "Map should not contain null");
            }
        } catch (final NullPointerException e) {
            // map does not support null as key
        }

        m_keyValueMap.putAll(map);
    }

    /**
     * Allows the adding of arbitrary objects with a given <tt>key</tt>.
     * @param key the <tt>key</tt> to use. Must not be <code>null</code>.
     * @param value the value to store. Must not be <code>null</code>.
     * @return the previous value associated with <tt>key</tt>, or
     *         <code>null</code> if there was no mapping for <tt>key</tt>.
     * @since 2.6
     */
    public Object addValue(final String key, final Object value) {
        if (key == null) {
            throw new NullPointerException("key must not be null");
        }
        if (value == null) {
            throw new NullPointerException("value must not be null");
        }
        return m_keyValueMap.put(key, value);
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or
     *         {@code null} if this map contains no mapping for the key
     * @since 2.6
     */
    public Object getValue(final String key) {
        if (key == null) {
            throw new NullPointerException("key must not be null");
        }
        return m_keyValueMap.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_fileStoreFactory == null) ? 0 : m_fileStoreFactory.hashCode());
        result = prime * result + ((m_groupColNames == null) ? 0 : m_groupColNames.hashCode());
        result = prime * result + ((m_keyValueMap == null) ? 0 : m_keyValueMap.hashCode());
        result = prime * result + m_maxUniqueValues;
        result = prime * result + (int) m_noOfRows;
        result = prime * result + ((m_spec == null) ? 0 : m_spec.hashCode());
        result = prime * result + ((m_valueDelimiter == null) ? 0 : m_valueDelimiter.hashCode());
        result = prime * result + (Objects.hashCode(m_aggregationContext));
        return result;
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
        GlobalSettings other = (GlobalSettings)obj;
        if (m_fileStoreFactory == null) {
            if (other.m_fileStoreFactory != null) {
                return false;
            }
        } else if (!m_fileStoreFactory.equals(other.m_fileStoreFactory)) {
            return false;
        }
        if (m_groupColNames == null) {
            if (other.m_groupColNames != null) {
                return false;
            }
        } else if (!m_groupColNames.equals(other.m_groupColNames)) {
            return false;
        }
        if (m_keyValueMap == null) {
            if (other.m_keyValueMap != null) {
                return false;
            }
        } else if (!m_keyValueMap.equals(other.m_keyValueMap)) {
            return false;
        }
        if (m_maxUniqueValues != other.m_maxUniqueValues) {
            return false;
        }
        if (m_noOfRows != other.m_noOfRows) {
            return false;
        }
        if (m_spec == null) {
            if (other.m_spec != null) {
                return false;
            }
        } else if (!m_spec.equals(other.m_spec)) {
            return false;
        }
        if (m_valueDelimiter == null) {
            if (other.m_valueDelimiter != null) {
                return false;
            }
        } else if (!m_valueDelimiter.equals(other.m_valueDelimiter)) {
            return false;
        }
        if (!Objects.equals(m_aggregationContext, other.m_aggregationContext)) {
            return false;
        }
        return true;
    }

    /**
     * @return the builder to create {@link GlobalSettings} instances
     * @since 3.3
     */
    public static GlobalSettingsBuilder builder() {
        return new GlobalSettingsBuilder();
    }

    /**
     * Builder to create {@link GlobalSettings}-objects.
     * @since 3.3
     */
    public static class GlobalSettingsBuilder {

        private int m_maxUniqueValues;
        private String m_valueDelimiter;
        private DataTableSpec m_spec;
        private long m_noOfRows = -1;
        private Map<String, Object> m_keyValueMap = new HashMap<String, Object>();
        private List<String> m_groupColNames;
        private FileStoreFactory m_fileStoreFactory;
        private AggregationContext m_aggregationContext = AggregationContext.UNKNOWN;

        private GlobalSettingsBuilder() {
            //
        }

        /**
         * @param fileStoreFactory the file store factory to create file store cells (optional)
         * @return this builder
         */
        public GlobalSettingsBuilder setFileStoreFactory(final FileStoreFactory fileStoreFactory) {
            m_fileStoreFactory = fileStoreFactory;
            return this;
        }

        /**
         * @param maxUniqueValues the maximum number of unique values to consider
         * @return this builder
         */
        public GlobalSettingsBuilder setMaxUniqueValues(final int maxUniqueValues) {
            m_maxUniqueValues = maxUniqueValues;
            return this;
        }

        /**
         * @param spec the {@link DataTableSpec} of the table to process
         * @return this builder
         */
        public GlobalSettingsBuilder setDataTableSpec(final DataTableSpec spec) {
            m_spec = spec;
            return this;
        }

        /**
         * @param noOfRows the total number of rows of the input table, -1 if not available (e.g. if used in a streaming environment)
         * @return this builder
         */
        public GlobalSettingsBuilder setNoOfRows(final long noOfRows) {
            m_noOfRows = noOfRows;
            return this;
        }

        /**
         * @param groupColNames {@link List} that contains the names of the columns
         * to group by
         * @return this builder
         */
        public GlobalSettingsBuilder setGroupColNames(final List<String> groupColNames) {
            m_groupColNames = groupColNames;
            return this;
        }

        /**
         * @param valueDelimiter the standard delimiter to use for value separation
         * @return this builder
         */
        public GlobalSettingsBuilder setValueDelimiter(final String valueDelimiter) {
            m_valueDelimiter = valueDelimiter;
            return this;
        }


        /**
         * @param keyValueMap the keyValueMap to set
         * @return this builder
         */
        public GlobalSettingsBuilder setKeyValueMap(final Map<String, Object> keyValueMap) {
            m_keyValueMap = keyValueMap;
            return this;
        }

        /**
         * @param context the aggregation context
         * @return this builder
         */
        public GlobalSettingsBuilder setAggregationContext(final AggregationContext context) {
            m_aggregationContext = context;
            return this;
        }

        /**
         * @return a newly created {@link GlobalSettings} instance with the values taken from this builder
         */
        public GlobalSettings build() {
            return new GlobalSettings(this);
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }


    }
}
