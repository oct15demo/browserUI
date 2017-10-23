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
 *   Jun 12, 2012 (wiswedel): created
 */
package org.knime.core.node.streamable;

/**
 * A port role describes the requirements for a node's input object. Some nodes
 * may only need to see the data once (so they are streamable), others may need
 * multiple iterations on the data and can't be parallelized.
 *
 * @author Bernd Wiswedel, KNIME.com, Zurich, Switzerland
 * @since 2.6
 */
public final class InputPortRole extends PortRole {

    /** Distributable and streamable. */
    public static final InputPortRole DISTRIBUTED_STREAMABLE = new InputPortRole(true, true);

    /** Distributable, not streamable. */
    public static final InputPortRole DISTRIBUTED_NONSTREAMABLE = new InputPortRole(true, false);

    /** Non distributable but streamable. */
    public static final InputPortRole NONDISTRIBUTED_STREAMABLE = new InputPortRole(false, true);

    /** Non distributable, not streamable. */
    public static final InputPortRole NONDISTRIBUTED_NONSTREAMABLE = new InputPortRole(false, false);

    private final boolean m_isStreamable;

    /**
     * @param isDistributable
     * @param isStreamable
     * @param isBuffered
     */
    private InputPortRole(final boolean isDistributable, final boolean isStreamable) {
        super(isDistributable);
        m_isStreamable = isStreamable;
    }

    /**
     * Defines that the input data is streamable, node implementations will only
     * need to see the data once (using a {@link RowInput}). If false, full
     * access on the data is required, i.e. access to the buffered table or a
     * model (model ports are always non-streamable).
     *
     * @return the isStreamable
     */
    public boolean isStreamable() {
        return m_isStreamable;
    }

    /** Getter method for on of the static fields based on the argument flags.
     * @param isDistributable distributable flag
     * @param isStreamable streamable flag
     * @return One of the static fields, never null.
     * @since 3.2 */
    public static InputPortRole get(final boolean isDistributable, final boolean isStreamable) {
        if (isDistributable) {
            return isStreamable ? DISTRIBUTED_STREAMABLE : DISTRIBUTED_NONSTREAMABLE;
        } else {
            return isStreamable ? NONDISTRIBUTED_STREAMABLE : NONDISTRIBUTED_NONSTREAMABLE;
        }
    }

}
