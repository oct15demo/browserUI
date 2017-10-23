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
 *   29.11.2004 (ohl): created
 */
package org.knime.core.util.tokenizer;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * Created for each delimiter for the <code>FileTokenizer</code> keeping its
 * specifics.
 * 
 * @author Peter Ohl, University of Konstanz
 */
public class Delimiter {
    private final String m_delim;

    private boolean m_combineMultiple;

    private boolean m_return;

    private boolean m_include;

    /* keys used to store parameters in a config object */
    private static final String CFGKEY_DELIM = "pattern";

    private static final String CFGKEY_COMBINE = "combineMultiple";

    private static final String CFGKEY_RETURN = "returnAsToken";

    private static final String CFGKEY_INCLUDE = "includeInToken";

    /**
     * Creates a new delimiter object. Only constructed by the
     * <code>FileTokenizerSettings</code> class.
     * 
     * @see TokenizerSettings
     * @param pattern the delimiter patter
     * @param combineConsecutive boolean flag
     * @param returnAsSeparateToken boolean flag
     * @param includeInToken boolean flag
     */
    public Delimiter(final String pattern, final boolean combineConsecutive,
            final boolean returnAsSeparateToken, final boolean includeInToken) {

        m_delim = pattern;
        m_combineMultiple = combineConsecutive;
        m_return = returnAsSeparateToken;
        m_include = includeInToken;
    }

    /**
     * Creates a new <code>Delimiter</code> object and sets its parameters
     * from the <code>config</code> object. If config doesn't contain all
     * necessary parameters or contains inconsistent settings it will throw an
     * IllegalArguments exception
     * 
     * @param settings an object the parameters are read from.
     * @throws InvalidSettingsException if the config is invalid. Right?
     */
    Delimiter(final NodeSettingsRO settings) throws InvalidSettingsException {
        if (settings == null) {
            throw new NullPointerException("Can't initialize from a null "
                    + "object! Settings incomplete!!");
        }

        try {
            m_delim = settings.getString(CFGKEY_DELIM);
        } catch (InvalidSettingsException ice) {
            throw new InvalidSettingsException("Illegal config object for "
                    + "delimiter (missing key)! Settings incomplete!");

        }

        /*
         * if settings doesn't contain the key it will return the passed default
         * value
         */
        try {
            if (settings.containsKey(CFGKEY_COMBINE)) {
                m_combineMultiple = settings.getBoolean(CFGKEY_COMBINE);
            } else {
                m_combineMultiple = false;
            }
            if (settings.containsKey(CFGKEY_RETURN)) {
                m_return = settings.getBoolean(CFGKEY_RETURN);
            } else {
                m_return = false;
            }
            if (settings.containsKey(CFGKEY_INCLUDE)) {
                m_include = settings.getBoolean(CFGKEY_INCLUDE);
            } else {
                m_include = false;
            }
        } catch (InvalidSettingsException ice) {
            assert false;
            m_combineMultiple = false;
            m_return = false;
            m_include = false;
        }
    }

    /**
     * Writes the object into a <code>NodeSettings</code> object. If this
     * config object is then used to construct a new <code>Delimiter</code>
     * this and the new object should be identical.
     * 
     * @param cfg a config object the internal values of this object will be
     *            stored into.
     */
    void saveToConfig(final NodeSettingsWO cfg) {
        if (cfg == null) {
            throw new NullPointerException("Can't save 'delimiter' "
                    + "to null config!");
        }

        cfg.addString(CFGKEY_DELIM, getDelimiter());
        cfg.addBoolean(CFGKEY_COMBINE, combineConsecutiveDelims());
        cfg.addBoolean(CFGKEY_INCLUDE, includeInToken());
        cfg.addBoolean(CFGKEY_RETURN, returnAsToken());

    }

    /**
     * @return The delimiter pattern.
     */
    public String getDelimiter() {
        return m_delim;
    }

    /**
     * @return <code>true</code> if consecutive appearances of this delimiter
     *         should be combined.
     */
    public boolean combineConsecutiveDelims() {
        return m_combineMultiple;
    }

    /**
     * @return <code>true</code> if this delimiter should be returned as
     *         separate token.
     */
    public boolean returnAsToken() {
        return m_return;
    }

    /**
     * @return <code>true</code> if this delimiter should be included in the
     *         prev. token.
     */
    public boolean includeInToken() {
        return m_include;
    }

    /**
     * @return The first character of this delimiter pattern.
     */
    public char getFirstChar() {
        return m_delim.charAt(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return TokenizerSettings.printableStr(getDelimiter());
    }

    /*
     * --- the equal and hash functions only look at the delimier. The don't
     * compare the value of any flag. --------------
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Delimiter)) {
            return false;
        }
        return this.getDelimiter().equals(((Delimiter)obj).getDelimiter());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getDelimiter().hashCode();
    }

} // Delimiter
