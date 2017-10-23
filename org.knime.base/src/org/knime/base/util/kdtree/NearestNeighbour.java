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
package org.knime.base.util.kdtree;

/**
 * This class represents a nearest neighbour found during the search.
 *
 * @param <T> the type of the data object associated with the pattern
 * @author Thorsten Meinl, University of Konstanz
 */
public class NearestNeighbour<T> implements Comparable<NearestNeighbour<T>> {
    private final T m_data;

    private double m_distance;

    /**
     * Creates a new nearest neighbour.
     *
     * @param data the data, can be <code>null</code>
     * @param distance the distance from the query pattern
     */
    NearestNeighbour(final T data, final double distance) {
        m_data = data;
        m_distance = distance;
    }

    /**
     * Returns the data associated with the pattern.
     *
     * @return the data, can be <code>null</code>
     */
    public T getData() {
        return m_data;
    }

    /**
     * Returns the distance from the query pattern.
     *
     * @return the distance
     */
    public double getDistance() {
        return m_distance;
    }

    /**
     * Sets the distance of this nearest neighbour.
     *
     * @param newDistance the new distance
     */
    void setDistance(final double newDistance) {
        m_distance = newDistance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final NearestNeighbour<T> o) {
        // query results are sorted by *de*creasing distance
        return Double.compare(o.m_distance, this.m_distance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "(" + m_distance + ", " + m_data + ")";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_data == null) ? 0 : m_data.hashCode());
        long temp;
        temp = Double.doubleToLongBits(m_distance);
        result = prime * result + (int)(temp ^ (temp >>> 32));
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
        NearestNeighbour<?> other = (NearestNeighbour<?>)obj;
        if (m_data == null) {
            if (other.m_data != null) {
                return false;
            }
        } else if (!m_data.equals(other.m_data)) {
            return false;
        }
        if (Double.doubleToLongBits(m_distance) != Double
                .doubleToLongBits(other.m_distance)) {
            return false;
        }
        return true;
    }
}
