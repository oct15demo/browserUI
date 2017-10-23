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
 * Created on 17.03.2014 by Christian Albrecht, KNIME.com AG, Zurich, Switzerland
 */
package org.knime.js.core.layout.grid;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 *
 * @author Christian Albrecht, KNIME.com AG, Zurich, Switzerland
 */
@JsonAutoDetect
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class JSONGridContent {

    private boolean m_containsView;

    private String m_nodeID;

    private List<JSONGridEntry> m_nestedGridContent;

    private JSONGridSizing m_sizing;

    /**
     * @return the containsView
     */
    public boolean getContainsView() {
        return m_containsView;
    }

    /**
     * @param containsView the containsView to set
     */
    public void setContainsView(final boolean containsView) {
        m_containsView = containsView;
    }

    /**
     * @return the nodeID
     */
    public String getNodeID() {
        return m_nodeID;
    }

    /**
     * @param nodeID the nodeID to set
     */
    public void setNodeID(final String nodeID) {
        m_nodeID = nodeID;
    }

    /**
     * @return the gridContent
     */
    public List<JSONGridEntry> getNestedGridContent() {
        return m_nestedGridContent;
    }

    /**
     * @param nestedGridContent the gridContent to set
     */
    public void setNestedGridContent(final List<JSONGridEntry> nestedGridContent) {
        m_nestedGridContent = nestedGridContent;
    }

    /**
     * @return the sizing
     */
    public JSONGridSizing getSizing() {
        return m_sizing;
    }

    /**
     * @param sizing the sizing to set
     */
    public void setSizing(final JSONGridSizing sizing) {
        m_sizing = sizing;
    }

}
