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
 *   07.05.2012 (kilian): created
 */
package org.knime.base.node.preproc.rounddouble;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.knime.base.node.preproc.rounddouble.RoundDoubleConfigKeys.RoundOutputType;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;

/**
 * Creating data cells containing the rounded values.
 *
 * @author Kilian Thiel, KNIME.com, Berlin, Germany
 */
class RoundDoubleCellFactory extends AbstractCellFactory {

    private final int m_precision;

    private final RoundingMode m_roundingMode;

    private final NumberMode m_numberMode;

    private final RoundOutputType m_outputType;

    private final int[] m_colIndexToRound;


    /**
     * Creates instance of <code>RoundDoubleCellFactory</code> with specified precision.
     *
     * @param precision The decimal place to round to.
     * @param numberMode The mode of the precision to round to (decimal place, significant figures).
     * @param roundingMode The mode to round the double values. additional column or if the old values will be replaced.
     * @param outputType Specifies whether rounded values will be represented as strings or doubles.
     * @param colIndexToRound The indices of the columns containing the values to round.
     * @param newColSpecs The specs of the new columns (replaced or appended).
     */
    RoundDoubleCellFactory(final int precision, final NumberMode numberMode, final RoundingMode roundingMode,
                           final RoundOutputType outputType, final int[] colIndexToRound,
       final DataColumnSpec[] newColSpecs) {
        super(newColSpecs);

        // check for invalid arguments
        if (roundingMode == null) {
            throw new IllegalArgumentException(
                    "Rounding mode may not be null!");
        }
        if (colIndexToRound == null) {
            throw new IllegalArgumentException(
                    "Array of column indices to round may not be null!");
        }
        if (numberMode == null) {
            throw new IllegalArgumentException(
                    "Number mode may not be null!");
        }

        m_precision = precision;
        m_roundingMode = roundingMode;
        m_outputType = outputType;
        m_colIndexToRound = colIndexToRound;
        m_numberMode = numberMode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell[] getCells(final DataRow row) {
        DataCell[] newCells = new DataCell[m_colIndexToRound.length];
        int noCols = row.getNumCells();
        int nextIndexToRound = 0;
        int currIndexToRound = -1;

        // walk through all columns and round if specified
        for (int i = 0; i < noCols; i++) {

            // get next index of column to round (if still columns to round
            // are available).
            if (nextIndexToRound < m_colIndexToRound.length) {
                currIndexToRound = m_colIndexToRound[nextIndexToRound];
            }

            // if value needs to be rounded
            if (i == currIndexToRound) {
                final DataCell outCell;
                if (row.getCell(i).isMissing()) {
                    outCell = DataType.getMissingCell();
                } else {
                    double value = ((DoubleValue)row.getCell(i)).getDoubleValue();

                    // check for infinity or nan
                    if (Double.isInfinite(value) || Double.isNaN(value)) {
                        switch (m_outputType) {
                            case Double:
                                // this isn't nice as we shouldn't have NaN and Inf in the input ...
                                // but that's a problem somewhere else
                                outCell = new DoubleCell(value);
                                break;
                            default:
                                outCell = new StringCell(new Double(value).toString());
                        }
                    } else {
                        // do not use constructor, see AP-7016
                        BigDecimal bd = BigDecimal.valueOf(value).stripTrailingZeros();

                        switch (m_numberMode) {
                            case DECIMAL_PLACES:
                                bd = bd.setScale(m_precision, m_roundingMode);
                                break;
                            case SIGNIFICANT_FIGURES:
                                bd = bd.round(new MathContext(m_precision, m_roundingMode));
                                break;
                            default:
                                throw new IllegalStateException();
                        }
                        outCell = m_outputType.createCell(bd);
                    }
                }
                // increment index of included column indices
                newCells[nextIndexToRound++] = outCell;
            }
        }

        return newCells;
    }

}
