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
 *   14.09.2009 (Fabian Dill): created
 */
package org.knime.core.data.date;

import javax.swing.Icon;

import org.knime.core.data.DataValue.UtilityFactory;
import org.knime.core.data.DataValueComparator;
import org.knime.core.data.ExtensibleUtilityFactory;

/**
 * The {@link UtilityFactory} for the {@link DateAndTimeValue} providing access
 * to the icon, the renderer and the comparator.
 *
 * @author Fabian Dill, KNIME.com, Zurich, Switzerland
 * @deprecated use the new date&amp;time types from <tt>org.knime.time</tt> instead
 */
@Deprecated
public class DateAndTimeUtility extends ExtensibleUtilityFactory {
    /** Singleton icon to be used to display this cell type. */
    private static final Icon ICON = loadIcon(
            DateAndTimeUtility.class, "icons/date_time.png");

    private static final DataValueComparator COMPARATOR
        = new DateAndTimeComparator();

    /**
     * Default constructor.
     */
    public DateAndTimeUtility() {
        super(DateAndTimeValue.class);
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public Icon getIcon() {
        return ICON;
    }

    /**
     *
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
        return "Date and Time";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getGroupName() {
        return "Basic";
    }
}
