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
 *   30.07.2007 (thor): created
 */
package org.knime.base.node.preproc.joiner;

import java.awt.Component;
import java.awt.Font;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.node.util.DataColumnSpecListCellRenderer;

/**
 * This class renders a list with {@link DataColumnSpec}s and strings. Column
 * specs get a nice icon describing the type in front of the column name,
 * string columns are rendered italic.
 *
 * @author Thorsten Meinl, University of Konstanz
 * @deprecated Use {@link DataColumnSpecListCellRenderer}.
 */
@Deprecated
public class ColumnSpecListRenderer extends DefaultListCellRenderer {
    private static final long serialVersionUID = 1L;

    private static final Font RK_FONT;

    private static final Font CS_FONT;

    static {
        JLabel l = new JLabel();
        RK_FONT =
                new Font(l.getFont().getName(), Font.ITALIC, l.getFont()
                        .getSize());
        CS_FONT = l.getFont();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getListCellRendererComponent(final JList list,
            final Object value, final int index, final boolean isSelected,
            final boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index,
                        isSelected, cellHasFocus);

        if (value instanceof String) { // this is the Row Key
            setFont(RK_FONT);
            setText(value.toString());
        } else if (value instanceof DataColumnSpec) {
            setFont(CS_FONT);
            setText(((DataColumnSpec)value).getName());
            setIcon(((DataColumnSpec)value).getType().getIcon());
        } else {
            setFont(CS_FONT);
            setText(value.toString());
        }

        return this;
    }
}
