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
 *   27.03.2014 (Marcel Hanser): created
 */
package org.knime.base.node.preproc.pmml.missingval.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.xmlbeans.XmlException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.util.CheckUtils;
import org.knime.missingval.v10.MissingcellhandlerDocument;
import org.w3c.dom.Element;

/**
 *
 * @author Marcel Hanser, Alexander Fillbrunn
 * @since 2.12
 */
final class MissingCellHandlerDescriptionV1 implements MissingCellHandlerDescription {
    private static final NodeLogger LOGGER = NodeLogger.getLogger(MissingCellHandlerDescriptionV1.class);

    private final MissingcellhandlerDocument m_document;

    private final String m_name;

    private final String m_shortDescription;

    /**
     * @param stream the XML stream to parse
     * @throws IOException if parsing fails
     * @throws XmlException if parsing fails
     */
    MissingCellHandlerDescriptionV1(final InputStream stream) throws XmlException, IOException {
        super();
        this.m_document = MissingcellhandlerDocument.Factory.parse(stream);
        this.m_name = CheckUtils.checkNotNull(m_document.getMissingcellhandler().getName());
        String descr;
        try {
            descr = CheckUtils.checkNotNull(m_document.getMissingcellhandler().getStringValue());
        } catch (NullPointerException e) {
            descr = "No description provided.";
        }
        this.m_shortDescription = descr;
    }

    /**
     * @return the name
     */
    @Override
    public String getName() {
        return m_name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getShortDescription() {
        return m_shortDescription;
    }

    /**
     * @return the root element
     */
    @Override
    public Element getElement() {
        return (Element)m_document.getMissingcellhandler().getDomNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMissing() {
        return false;
    }
}
