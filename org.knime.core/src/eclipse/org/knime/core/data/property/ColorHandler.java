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
 *   06.02.2006 (tg): created
 */
package org.knime.core.data.property;

import org.knime.core.data.DataCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.config.ConfigRO;
import org.knime.core.node.config.ConfigWO;

/**
 * Final <code>ColorHandler</code> implementation as container which forwards 
 * color requests for a {@link org.knime.core.data.DataCell} to its underlying 
 * {@link org.knime.core.data.property.ColorHandler.ColorModel}. 
 * The <code>ColorModel</code> can be loaded and saved
 * from <code>Config</code> object. 
 * 
 * @author Thomas Gabriel, University of Konstanz
 */
public final class ColorHandler implements PropertyHandler {
    
    /** Config key for the color model class. */
    private static final String CFG_COLOR_MODEL_CLASS = "color_model_class";
    
    /** Config key for the color model config. */
    private static final String CFG_COLOR_MODEL = "color_model";
    
    /**
     * Holds the color model, that is, <code>DataCell</code> to 
     * <code>ColorAttr</code> mapping.
     */
    private final ColorModel m_model;
    
    /**
     * Create new color handler with the given <code>ColorModel</code>.
     * @param model the color model which has the color settings
     * @throws IllegalArgumentException if the model is <code>null</code>
     */
    public ColorHandler(final ColorModel model) {
        if (model == null) {
            throw new IllegalArgumentException("ColorModel must not be null.");
        }
        m_model = model;
    }
    
    /**
     * Returns a <code>ColorAttr</code> object as specified by the content
     * of the given <code>DataCell</code>. Requests are forwarded to the 
     * underlying <code>ColorModel</code>. If no <code>ColorAttr</code>
     * is assigned to the given <code>dc</code>, this method returns the
     * {@link ColorAttr#DEFAULT} as default color, but never <code>null</code>.
     * 
     * @param dc <code>DataCell</code> used to generate color
     * @return a <code>ColorAttr</code> object assigned to the given cell
     * @see ColorAttr#DEFAULT
     */
    public ColorAttr getColorAttr(final DataCell dc) {
        ColorAttr color = m_model.getColorAttr(dc);
        if (color == null) {
            return ColorAttr.DEFAULT;
        }
        return color;
    }
    
    /**
     * Saves the underlying <code>ColorModel</code> to the given
     * <code>Config</code> by adding the <code>ColorModel</code> class as 
     * String and calling 
     * {@link ColorModel#save(ConfigWO)} within the model.
     * @param config color settings are saved to
     * @throws NullPointerException if the <i>config</i> is <code>null</code>
     */
    public void save(final ConfigWO config) {
        config.addString(CFG_COLOR_MODEL_CLASS, m_model.getClass().getName());
        m_model.save(config.addConfig(CFG_COLOR_MODEL));
    }
    
    /**
     * Reads the color model settings from the given <code>Config</code>, inits 
     * a new <code>ColorModel</code>, and returns a new 
     * <code>ColorHandler</code>.
     * @param config read color settings from
     * @return a new <code>ColorHandler</code> object created with the color 
     *         model settings read from <code>config</code>
     * @throws InvalidSettingsException if either the class or color model
     *         settings could not be read
     * @throws NullPointerException if the <code>config</code> is 
     *         <code>null</code> 
     */
    public static ColorHandler load(final ConfigRO config) 
            throws InvalidSettingsException {
        String modelClass = config.getString(CFG_COLOR_MODEL_CLASS);
        if (modelClass.equals(ColorModelNominal.class.getName())) {
            ConfigRO subConfig = config.getConfig(CFG_COLOR_MODEL);
            return new ColorHandler(ColorModelNominal.load(subConfig));
        } else if (modelClass.equals(ColorModelRange.class.getName())) {
            ConfigRO subConfig = config.getConfig(CFG_COLOR_MODEL);
            return new ColorHandler(ColorModelRange.load(subConfig));
        } else {
            throw new InvalidSettingsException("Unknown ColorModel class: "
                    + modelClass);
        }
    }
    
    /**
     * Returns the underlying color model that is derived from class
     * <code>ColorModel</code>.
     * @return the <code>ColorModel</code> of this handler
     */
    public ColorModel getColorModel() {
        return m_model;
    }
    
    /**
     * Returns a String summary of the underlying 
     * {@link org.knime.core.data.property.ColorHandler.ColorModel}.
     * 
     * @return a String summary of the <code>ColorModel</code>
     */
    @Override
    public String toString() {
        return m_model.toString();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof ColorHandler)) {
            return false;
        }
        return m_model.equals(((ColorHandler)obj).m_model);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return m_model.hashCode();
    }
 
    /**
     * Interface allowing requests for {@link ColorAttr} by {@link DataCell}.
     */
    public interface ColorModel {
        /**
         * Returns a <code>ColorAttr</code> for the given <code>DataCell</code>.
         * @param dc the <code>DataCell</code> to get the color for
         * @return a <code>ColorAttr</code> object, but not <code>null</code>
         */
        ColorAttr getColorAttr(DataCell dc);
        /**
         * Saves this <code>ColorModel</code> to the given 
         * <code>ConfigWO</code>.
         * @param config used to save this <code>ColorModel</code> to
         */
        void save(ConfigWO config);
    }

}
