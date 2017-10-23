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
 *   07.07.2005 (mb): created
 */
package org.knime.core.data;

import javax.swing.Icon;

/**
 * Interface supporting fuzzy interval cells holding support and core min and
 * max values.
 *
 * @author Michael Berthold, University of Konstanz
 */
public interface FuzzyIntervalValue extends DataValue {

    /** Meta information to this value type.
     * @see DataValue#UTILITY
     */
    UtilityFactory UTILITY = new FuzzyIntervalUtilityFactory();

    /**
     * @return Minimum support value.
     */
    double getMinSupport();

    /**
     * @return Minimum core value.
     */
    double getMinCore();

    /**
     * @return Maximum core value.
     */
    double getMaxCore();

    /**
     * @return Maximum support value.
     */
    double getMaxSupport();

    /**
     * @return The center of gravity of this fuzzy membership function.
     */
    double getCenterOfGravity();

    /** Implementations of the meta information of this value class. */
    class FuzzyIntervalUtilityFactory extends ExtensibleUtilityFactory {
        /** Singleton icon to be used to display this cell type. */
        private static final Icon ICON =
            loadIcon(FuzzyIntervalValue.class, "/icon/fuzzyintervalicon.png");

        private static final FuzzyIntervalValueComparator COMPARATOR =
            new FuzzyIntervalValueComparator();

        /** Only subclasses are allowed to instantiate this class. */
        protected FuzzyIntervalUtilityFactory() {
            super(FuzzyIntervalValue.class);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Icon getIcon() {
            return ICON;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected DataValueComparator getComparator() {
            return COMPARATOR;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getName() {
            return "Fuzzy interval";
        }
    }
}
