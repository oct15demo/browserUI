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
 * History
 *   01.02.2006 (sieb): created
 */
package org.knime.base.util.coordinate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataValue;

/**
 * This class represents a nominal coordinate defined by a given
 * {@link org.knime.core.data.DataColumnSpec}. The nominal values are arranged
 * equidistant and in the order given in the
 * {@link org.knime.core.data.DataColumnSpec}.
 *
 * @author Christoph Sieb, University of Konstanz
 * @author Fabian Dill, University of Konstanz
 */
public class NominalCoordinate extends Coordinate {

    /**
     * The number of different nominal values.
     */
    private int m_numberPossibleValues;

    /**
     * Holds the possible values as <code>DataCell</code>s.
     */
    private Vector<DataCell> m_possibleValues;

    /**
     * Constructs a nominal coordinate according to the given column spec.
     *
     * @param dataColumnSpec the column spec to create this coordinate from
     */
    NominalCoordinate(final DataColumnSpec dataColumnSpec) {
        super(dataColumnSpec);

        // check the column type first.
        // to be nominal it must be possible to receive
        // string representations of all possible values
        Set<DataCell> possibleValues = dataColumnSpec.getDomain().getValues();

        if (possibleValues == null) {
            throw new IllegalArgumentException("The type of the given column "
                    + "must be a nominal one: "
                    + dataColumnSpec.getType().toString());
        }

        // now initiate the possible values vector
        // this is needed to rearrange the nominal values later
        m_possibleValues = new Vector<DataCell>(possibleValues.size());
        Iterator<DataCell> possibleValuesIter = possibleValues.iterator();
        while (possibleValuesIter.hasNext()) {
            DataCell posValue = possibleValuesIter.next();
            m_possibleValues.add(posValue);
        }

        m_numberPossibleValues = possibleValues.size();
    }

    /**
     * Changes the position of a nominal value on the axis. This is due to
     * reordering nominal values which do not have a inherent order.
     *
     * @param nomValue the value to replace
     * @param index the position to set the value
     */
    public void changeValuePosition(final DataCell nomValue, final int index) {

        // first remove the value to relocate
        m_possibleValues.remove(nomValue);

        // re-add the value at the specified location
        m_possibleValues.add(index, nomValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getUnusedDistBetweenTicks(final double absoluteLength) {
        return absoluteLength / m_numberPossibleValues;
    }

    /**
     * Returns an array with the position of all ticks and their corresponding
     * nominal domain values given an absolute length. The nominal values are
     * arranged equidistant with a half the distance at the beginning of the
     * coordinate and half the distance at the end.
     *
     * @param absoluteLength the absolute length the domain is mapped on
     *
     * @return the mapping of tick positions and corresponding domain values
     */
    @Override
    protected CoordinateMapping[] getTickPositionsWithLabels(
            final double absoluteLength) {

        if (m_numberPossibleValues <= 0) {
            return null;
        }

        // the mapping to create and return
        NominalCoordinateMapping[] mappings = null;

        mappings = new NominalCoordinateMapping[m_numberPossibleValues];

        // calculate the distance between two nominal values and
        // calculate half of the equidistance for the initial tick
        double equiDistance = absoluteLength / m_numberPossibleValues;
        double halfEquiDistance = equiDistance / 2;

        // Iterate over all possible values
        Iterator<DataCell> possibleValuesIter = m_possibleValues.iterator();

        DataCell next = possibleValuesIter.next();

        // set first tick half the equidistance from the beginning of the
        // mapping
        mappings[0] =
                new NominalCoordinateMapping(next.toString(), halfEquiDistance);
        mappings[0].setValues(next);

        // the rest of the values is set according to distance behind the first
        // mapping

        for (int i = 1; possibleValuesIter.hasNext(); i++) {
            next = possibleValuesIter.next();
            String domainValue = next.toString();
            double mappingValue = halfEquiDistance + equiDistance * i;
            mappings[i] =
                    new NominalCoordinateMapping(domainValue, mappingValue);
            mappings[i].setValues(next);
        }

        return mappings;
    }

    /**
     *
     * @param absLength the available length.
     * @return a reduced mapping with a minimum distance between the values and
     *         dots if some values were left out,
     */
    public CoordinateMapping[] getReducedTickPositions(final int absLength) {

        if (m_numberPossibleValues <= 0) {
            return null;
        }

        int tickWidth = (int)Math.ceil(absLength / (double) m_numberPossibleValues);
        if (tickWidth == 0) {
            tickWidth = 1;
        }
        int leaveOut = DEFAULT_ABSOLUTE_TICK_DIST / tickWidth;
        CoordinateMapping[] mappings = getTickPositions(absLength);
        CoordinateMapping[] reduced;
        if (leaveOut > 1) {
            // no desired value
            reduced = new CoordinateMapping[mappings.length / leaveOut + 1];

            for (int i = 0; i < reduced.length; i++) {
                int index = i * leaveOut;
                if (index > mappings.length - 1) {
                    index = mappings.length - 1;
                }
                if (i % 2 == 0) {
                    reduced[i] = mappings[index];
                } else {
                    reduced[i] =
                            new NominalCoordinateMapping("...", mappings[index]
                                    .getMappingValue());
                    if (leaveOut == 1) {
                        String val = mappings[index].getDomainValueAsString();
                        reduced[i] =
                                new NominalCoordinateMapping(val,
                                        mappings[index].getMappingValue());
                    }
                    List<DataValue> tickValues = new ArrayList<DataValue>();
                    int start = (i - 1) * leaveOut + 1;
                    int end = (i + 1) * leaveOut;
                    if (end >= mappings.length) {
                        end = mappings.length - 1;
                    }
                    if (i == reduced.length - 2) {
                        // end = mappings.length - 2;
                    }
                    for (int j = start; j < end; j++) {
                        for (DataValue dv : mappings[j].getValues()) {
                            tickValues.add(dv);

                        }
                    }
                    reduced[i].setValues(tickValues.toArray(new DataValue[0]));
                }
            }

        } else {
            reduced = mappings;
        }

        return reduced;
    }

    /**
     * Calculates a numeric mapping assuming a column with a given number of
     * possible values.
     *
     * {@inheritDoc}
     */
    @Override
    protected double calculateMappedValueInternal(
            final DataCell domainValueCell, final double absoluteLength) {

        // get the mapping for all values dependent on the absolute mapping
        // length
        CoordinateMapping[] mappings = getTickPositions(absoluteLength);

        // get the mapping position of the domain value from the mapping array
        double mappedValue = -1;
        for (CoordinateMapping mapping : mappings) {
            for (DataValue v : mapping.getValues()) {
                if (v.equals(domainValueCell)) {
                    mappedValue = mapping.getMappingValue();
                    return mappedValue;
                }
            }
        }

        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNominal() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDesiredValues(final DataValue... values) {
        if (values != null && values.length > 0) {
            getDesiredValuesSet().addAll(Arrays.asList(values));
        }
    }
}
