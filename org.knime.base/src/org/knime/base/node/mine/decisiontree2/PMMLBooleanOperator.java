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
 *   Sep 4, 2009 (morent): created
 */
package org.knime.base.node.mine.decisiontree2;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains the boolean operators that are specified in PMML for
 * CompoundPrediates and SimpleSetPredicates.
 *
 * @author Dominik Morent, KNIME.com, Zurich, Switzerland
 */
public enum PMMLBooleanOperator {
    /** or operator. */
    OR("or"),
    /** and operator. */
    AND("and"),
    /** xor operator. */
    XOR("xor"),
    /**
     * Surrogate operator. Used for cases where a missing value appears in the
     * evaluation. An expression <code>surrogate(a,b)</code> is equivalent to
     * <code>if not unknown(a) then a else b</code>.
     */
    SURROGATE("surrogate");

    private final String m_represent;

    private PMMLBooleanOperator(final String represent) {
        m_represent = represent;
    }

    private PMMLBooleanOperator() {
        m_represent = null;
    }

    private static final Map<String, PMMLBooleanOperator> LOOKUP =
            new HashMap<String, PMMLBooleanOperator>();

    static {
        for (PMMLBooleanOperator op
                : EnumSet.allOf(PMMLBooleanOperator.class)) {
            LOOKUP.put(op.toString(), op);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (m_represent != null) {
            return m_represent;
        }
        return super.toString();
    }

    /**
     * Returns the corresponding operator for the passed representation.
     *
     * @param represent the representation to find the operator for
     * @return the operator
     * @throws InstantiationException  - if no such PMML operator exists
     */
    public static PMMLBooleanOperator get(final String represent)
            throws InstantiationException {
        PMMLBooleanOperator pmmlBooleanOperator = LOOKUP.get(represent);
        if (pmmlBooleanOperator == null) {
            throw new InstantiationException("Illegal PMML boolean operator "
                    + "type '" + represent);
        }

        return pmmlBooleanOperator;
    }
}
