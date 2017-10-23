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
 */
package org.knime.core.data.renderer;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DoubleValue;


/**
 * Render to display a double value using a given {@link NumberFormat}.
 * If no number format is given, the full precision is used, i.e.
 * {@link Double#toString(double)}.
 *
 * @author Bernd Wiswedel, University of Konstanz
 */
@SuppressWarnings("serial")
public class DoubleValueRenderer extends DefaultDataValueRenderer {
    /**
     * Factory for percentage renderers.
     *
     * @since 2.8
     */
    public static final class PercentageRendererFactory extends AbstractDataValueRendererFactory {
        private static final String DESCRIPTION = "Percentage";

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
            return DESCRIPTION;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DataValueRenderer createRenderer(final DataColumnSpec colSpec) {
            return new DoubleValueRenderer(new DecimalFormat("###0.0#%"), DESCRIPTION);
        }
    }


    /**
     * Factory for standard renderers.
     *
     * @since 2.8
     */
    public static final class StandardRendererFactory extends AbstractDataValueRendererFactory {
        private static final String DESCRIPTION = "Standard Double";

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
            return DESCRIPTION;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DataValueRenderer createRenderer(final DataColumnSpec colSpec) {
            return new DoubleValueRenderer(NumberFormat.getNumberInstance(Locale.US), DESCRIPTION);
        }
    }


    /**
     * Factory for full precision renderers.
     *
     * @since 2.8
     */
    public static final class FullPrecisionRendererFactory extends AbstractDataValueRendererFactory {
        private static final String DESCRIPTION = "Full Precision";

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
            return DESCRIPTION;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DataValueRenderer createRenderer(final DataColumnSpec colSpec) {
            return new DoubleValueRenderer(null, DESCRIPTION);
        }
    }

    /**
     * Singleton for percentage.
     * @deprecated Do not use this singleton instance, renderers are not thread-safe!
     */
    @Deprecated
    public static final DataValueRenderer PERCENT_RENDERER = new DoubleValueRenderer(new DecimalFormat("###0.0#%"),
        PercentageRendererFactory.DESCRIPTION);

    /**
     * Singleton for ordinary representation.
     * @deprecated Do not use this singleton instance, renderers are not thread-safe!
     */
    @Deprecated
    public static final DataValueRenderer STANDARD_RENDERER = new DoubleValueRenderer(
        NumberFormat.getNumberInstance(Locale.US), StandardRendererFactory.DESCRIPTION);

    /**
     * Singleton for full precision representation.
     * @deprecated Do not use this singleton instance, renderers are not thread-safe!
     */
    @Deprecated
    public static final DataValueRenderer FULL_PRECISION_RENDERER =
        new DoubleValueRenderer(null, FullPrecisionRendererFactory.DESCRIPTION);

    /** disable grouping in renderer */
    static {
        NumberFormat.getNumberInstance(Locale.US).setGroupingUsed(false);
    }

    /** Used to get a string representation of the double value. */
    private final NumberFormat m_format;

    /**
     * Instantiates a new object using a given format.
     * @param format To be used to render this object, may be <code>null</code>
     * to use full precision.
     * @param desc The description to the renderer
     * @throws NullPointerException
     * If <code>desc</code> argument is <code>null</code>.
     */
    public DoubleValueRenderer(final NumberFormat format, final String desc) {
        super(desc);
        if (desc == null) {
            throw new IllegalArgumentException("Description must not be null.");
        }
        m_format = format;
    }

    /**
     * Formats the object. If <code>value</code> is instance of
     * <code>DoubleValue</code>, the renderer's formatter is used to get a
     * string from the double value of the cell. Otherwise the
     * <code>value</code>'s <code>toString()</code> method is used.
     * @param value The value to be rendered.
     * @see javax.swing.table.DefaultTableCellRenderer#setValue(Object)
     */
    @Override
    protected void setValue(final Object value) {
        Object newValue;
        if (value instanceof DoubleValue) {
            DoubleValue cell = (DoubleValue)value;
            double d = cell.getDoubleValue();
            if (Double.isNaN(d)) {
                newValue = "NaN";
            } else {
                newValue = m_format != null
                    ? m_format.format(d) : Double.toString(d);
            }
        } else {
            // missing data cells will also end up here
            newValue = value;
        }
        super.setValue(newValue);
    }
}
