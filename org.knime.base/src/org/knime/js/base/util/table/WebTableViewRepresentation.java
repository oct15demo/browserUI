/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 *   14.04.2014 (Christian Albrecht, KNIME.com AG, Zurich, Switzerland): created
 */
package org.knime.js.base.util.table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.js.core.JSONDataTable;
import org.knime.js.core.JSONViewContent;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 *
 * @author Christian Albrecht, KNIME.com AG, Zurich, Switzerland, University of Konstanz
 */
@JsonAutoDetect
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class WebTableViewRepresentation extends JSONViewContent {

    private JSONDataTable m_table;
    private JSONNumberFormatter m_numberFormatter;
    private boolean m_enableSelection;
    private int m_tableHeight;
    private boolean m_fullFrame;

    /** Serialization constructor. Don't use. */
    public WebTableViewRepresentation() { }

    /**
     * @param table
     */
    public WebTableViewRepresentation(final JSONDataTable table) {
        setTable(table);
    }

    /**
     * @return The JSON data table.
     */
    @JsonProperty("table")
    public JSONDataTable getTable() {
        return m_table;
    }

    /**
     * @param table The table to set.
     */
    @JsonProperty("table")
    public void setTable(final JSONDataTable table) {
        m_table = table;
    }

    /**
     * @return the numberFormatter
     */
    @JsonProperty("numberFormatter")
    public JSONNumberFormatter getNumberFormatter() {
        return m_numberFormatter;
    }

    /**
     * @param numberFormatter the numberFormatter to set
     */
    @JsonProperty("numberFormatter")
    public void setNumberFormatter(final JSONNumberFormatter numberFormatter) {
        m_numberFormatter = numberFormatter;
    }

    /**
     * @return the enableSelection
     */
    public boolean getEnableSelection() {
        return m_enableSelection;
    }

    /**
     * @param enableSelection the enableSelection to set
     */
    public void setEnableSelection(final boolean enableSelection) {
        m_enableSelection = enableSelection;
    }

    /**
     * @return the height
     */
    public int getTableHeight() {
        return m_tableHeight;
    }

    /**
     * @param height the height to set
     */
    public void setTableHeight(final int height) {
        m_tableHeight = height;
    }

    /**
     * @return true if full frame table
     */
    public boolean getFullFrame() {
        return m_fullFrame;
    }

    /**
     * @param full true if full frame table
     */
    public void setFullFrame(final boolean full) {
        m_fullFrame = full;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public void saveToNodeSettings(final NodeSettingsWO settings) {
        // save nothing?
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public void loadFromNodeSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        // load nothing?
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        WebTableViewRepresentation other = (WebTableViewRepresentation)obj;
        return new EqualsBuilder()
                .append(m_table, other.m_table)
                .append(m_numberFormatter, other.m_numberFormatter)
                .append(m_enableSelection, other.m_enableSelection)
                .append(m_tableHeight, other.m_tableHeight)
                .append(m_fullFrame, other.m_fullFrame)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(m_table)
                .append(m_numberFormatter)
                .append(m_enableSelection)
                .append(m_tableHeight)
                .append(m_fullFrame)
                .toHashCode();
    }

}
