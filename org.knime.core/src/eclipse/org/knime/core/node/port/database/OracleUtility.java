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
 *   08.05.2014 (thor): created
 */
package org.knime.core.node.port.database;

import org.knime.core.node.port.database.aggregation.function.AvgDistinctDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.CorrDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.CountDistinctDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.CovarPopDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.CovarSampDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.MaxDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.MinDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.RegrAvgXDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.RegrAvgYDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.RegrCountDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.RegrInterceptDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.RegrR2DBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.RegrSXXDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.RegrSXYDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.RegrSYYDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.RegrSlopeDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.StdDevPopDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.StdDevSampDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.SumDistinctDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.VarPopDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.VarSampDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.oracle.ApproxCountDistinctDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.oracle.CorrKDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.oracle.CorrSDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.oracle.MedianDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.oracle.StatsBinomialTestDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.oracle.StatsCrosstabDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.oracle.StatsFTestDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.oracle.StatsKsTestDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.oracle.StatsModeDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.oracle.StatsMwTestDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.oracle.StatsOneWayAnovaDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.oracle.StatsTTestIndepDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.oracle.StatsTTestIndepUDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.oracle.StatsTTestOneDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.oracle.StatsTTestPairedDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.oracle.StatsWsrTestDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.oracle.StdDevDBAggregationFunction;
import org.knime.core.node.port.database.aggregation.function.oracle.VarianceDBAggregationFunction;
import org.knime.core.node.port.database.connection.DBConnectionFactory;
import org.knime.core.node.port.database.connection.DBDriverFactory;
import org.knime.core.node.port.database.tablecreator.DBTableCreator;


/**
 * Database utility for Oracle.
 *
 * @author Thorsten Meinl, KNIME.com, Zurich, Switzerland
 * @since 2.10
 */
public class OracleUtility extends DatabaseUtility {
    private static class OracleStatementManipulator extends StatementManipulator {


        /**
         * Constructor of class {@link OracleStatementManipulator}.
         */
       public OracleStatementManipulator() {
           super(true);
       }


        /**
         * {@inheritDoc}
         */
        @Override
        public String limitRows(final String sql, final long count) {
            return "SELECT * FROM (" + sql + ") " + getTempTableName() + " WHERE rownum <= " + count;
        }


        @Override
        public String randomRows(final String sql, final long count) {
            final String stmt = sql + " ORDER BY dbms_random.value";
            return limitRows(stmt, count);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String forMetadataOnly(final String sql) {
            return limitRows(sql, 0);
        }

        /**
         * {@inheritDoc}
         * @deprecated
         */
        @Deprecated
        @Override
        public String quoteColumn(final String colName) {
            return "\"" + colName + "\"";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String quoteIdentifier(final String identifier) {
            return quoteColumn(identifier);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String dropTable(final String tableName, final boolean cascade) {
            final StringBuilder buf = new StringBuilder("DROP TABLE ");
            buf.append(tableName);
            if (cascade) {
                buf.append(" CASCADE CONSTRAINTS");
            }
            return buf.toString();
        }

//        /**
//         * {@inheritDoc}
//         */
//        @Override
//        public String limitRows(final String sql, final long count, final long offset) {
//            String tempTableName = getTempTableName();
//
//          return "SELECT * FROM (SELECT rownum __rnum__, " + tempTableName + ".* FROM (" + sql + ") " + tempTableName
//                + " WHERE rownum <= " + (offset + count) + ") " + getTempTableName() + " WHERE rnum > " + offset;
//        }
    }

    private static final StatementManipulator MANIPULATOR = new OracleStatementManipulator();

    /**The unique database identifier.
     * @since 2.11*/
    public static final String DATABASE_IDENTIFIER = "oracle";

    /**
     * Constructor.
     */
    public OracleUtility() {
        super(DATABASE_IDENTIFIER, MANIPULATOR, new AvgDistinctDBAggregationFunction.Factory(),
            new CorrDBAggregationFunction.Factory(), new CorrSDBAggregationFunction.Factory(),
            new CorrKDBAggregationFunction.Factory(), new CountDistinctDBAggregationFunction.Factory(),
            new CovarPopDBAggregationFunction.Factory(), new CovarSampDBAggregationFunction.Factory(),
            new MinDBAggregationFunction.Factory(), new MaxDBAggregationFunction.Factory(),
            new MedianDBAggregationFunction.Factory(), new RegrSlopeDBAggregationFunction.Factory(),
            new RegrInterceptDBAggregationFunction.Factory(), new RegrCountDBAggregationFunction.Factory(),
            new RegrR2DBAggregationFunction.Factory(), new RegrAvgXDBAggregationFunction.Factory(),
            new RegrAvgYDBAggregationFunction.Factory(), new RegrSXXDBAggregationFunction.Factory(),
            new RegrSYYDBAggregationFunction.Factory(), new RegrSXYDBAggregationFunction.Factory(),
            new StdDevDBAggregationFunction.Factory(), new StdDevPopDBAggregationFunction.Factory(),
            new StdDevSampDBAggregationFunction.Factory(), new SumDistinctDBAggregationFunction.Factory(),
            new VarPopDBAggregationFunction.Factory(), new VarSampDBAggregationFunction.Factory(),
            new StatsModeDBAggregationFunction.Factory(), new StatsCrosstabDBAggregationFunction.Factory(),
            new StatsKsTestDBAggregationFunction.Factory(), new StatsWsrTestDBAggregationFunction.Factory(),
            new StatsOneWayAnovaDBAggregationFunction.Factory(), new StatsBinomialTestDBAggregationFunction.Factory(),
            new StatsFTestDBAggregationFunction.Factory(), new StatsMwTestDBAggregationFunction.Factory(),
            new StatsTTestPairedDBAggregationFunction.Factory(), new StatsTTestIndepDBAggregationFunction.Factory(),
            new StatsTTestIndepUDBAggregationFunction.Factory(), new StatsTTestOneDBAggregationFunction.Factory(),
            new ApproxCountDistinctDBAggregationFunction.Factory(), new VarianceDBAggregationFunction.Factory());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DBConnectionFactory createConnectionFactory(final DBDriverFactory df) {
        return new OracleCachedConnectionFactory(df);
    }

    @Override
    public boolean supportsRandomSampling() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsCase() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DBTableCreator getTableCreator(final String schema, final String tableName, final boolean isTempTable) {
        return new OracleTableCreator(getStatementManipulator(), schema, tableName, isTempTable);
    }
}
