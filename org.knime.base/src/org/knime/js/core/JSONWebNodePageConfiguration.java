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
 * ---------------------------------------------------------------------
 *
 * Created on 16.09.2013 by Christian Albrecht, KNIME.com AG, Zurich, Switzerland
 */
package org.knime.js.core;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.knime.core.node.KNIMEConstants;
import org.knime.js.core.layout.bs.JSONLayoutPage;
import org.knime.js.core.selections.json.JSONSelectionTranslator;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 *
 * @author Christian Albrecht, KNIME.com AG, Zurich, Switzerland
 */
@JsonAutoDetect
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class JSONWebNodePageConfiguration {

    private String m_version;
    private JSONLayoutPage m_layout;
    private JSONBlackBoard m_blackBoard;
    private List<JSONSelectionTranslator> m_selectionTranslators;

    /** Serialization constructor. Don't use. */
    public JSONWebNodePageConfiguration() { }

    /**
     * @param layout the layout
     * @param blackBoard the blackboard
     * @param selectionTranslators
     */
    public JSONWebNodePageConfiguration(final JSONLayoutPage layout, final JSONBlackBoard blackBoard, final List<JSONSelectionTranslator> selectionTranslators) {
        m_version = KNIMEConstants.VERSION;
        m_layout = layout;
        m_blackBoard = blackBoard;
        m_selectionTranslators = selectionTranslators;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return m_version;
    }

    /**
     * @return the blackBoard
     */
    public JSONBlackBoard getBlackBoard() {
        return m_blackBoard;
    }

    /**
     * @param blackBoard the blackBoard to set
     */
    public void setBlackBoard(final JSONBlackBoard blackBoard) {
        m_blackBoard = blackBoard;
    }

    /**
     * @return the layout
     */
    public JSONLayoutPage getLayout() {
        return m_layout;
    }

    /**
     * @param layout the layout to set
     */
    public void setLayout(final JSONLayoutPage layout) {
        m_layout = layout;
    }

    /**
     * @return the selectionTranslators
     */
    public List<JSONSelectionTranslator> getSelectionTranslators() {
        return m_selectionTranslators;
    }

    /**
     * @param selectionTranslators the selectionTranslators to set
     */
    public void setSelectionTranslators(final List<JSONSelectionTranslator> selectionTranslators) {
        m_selectionTranslators = selectionTranslators;
    }

    // TODO: insert other meta info about page

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
        JSONWebNodePageConfiguration other = (JSONWebNodePageConfiguration)obj;
        return new EqualsBuilder()
                .append(m_version, other.m_version)
                .append(m_layout, other.m_layout)
                .append(m_selectionTranslators, other.m_selectionTranslators)
                .append(m_blackBoard, other.m_blackBoard)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(m_version)
                .append(m_layout)
                .append(m_selectionTranslators)
                .append(m_blackBoard)
                .toHashCode();
    }

}
