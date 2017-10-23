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
package org.knime.core.util;

/**
 * A Boolean object whose value can be changed after construction.
 * 
 * @author ohl, University of Konstanz
 */
public class MutableBoolean {

    private boolean m_value;

    /**
     * Constructor setting its initial value from the boolean value specified.
     * 
     * @param val the initial value.
     */
    public MutableBoolean(final boolean val) {
        m_value = val;
    }

    /**
     * Constructor deriving its initial value from the specified string. The
     * value of the new object is set <code>true</code>, if and only if the
     * string is not null and equals (ignoring case) "true".
     * 
     * @param s the string to be converted to a the initial value.
     */
    public MutableBoolean(final String s) {
        this(Boolean.parseBoolean(s));
    }

    /**
     * Sets the value of this MutableBoolean.
     * 
     * @param val the new value.
     */
    public void setValue(final boolean val) {
        m_value = val;
    }

    /**
     * Returns the value of this object as a boolean primitive.
     * 
     * @return the primitive <code>boolean</code> value of this object.
     */
    public boolean booleanValue() {
        return m_value;
    }

    /**
     * Returns a <tt>String</tt> representing this objects value. If this
     * object represents the value <code>true</code>, a string equal to
     * <code>"true"</code> is returned. Otherwise, a string equal to
     * <code>"false"</code> is returned.
     * 
     * @return a string representation of this object.
     */
    @Override
    public String toString() {
        return m_value ? "true" : "false";
    }

}
