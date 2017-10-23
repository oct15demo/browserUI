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
 *   22.06.2010 (hofer): created
 */
package org.knime.base.node.preproc.autobinner.pmml;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.pmml.PMMLContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author Heiko Hofer
 */
final class PMMLDiscretizeBinContentHandler extends PMMLContentHandler {
    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(PMMLDiscretizeBinContentHandler.class);

    private Stack<PMMLContentHandler> m_contentHandlerStack;
    private List<PMMLDiscretizeBin> m_list;
    private String m_binValue;
    private List<PMMLInterval> m_intervalList;

    /**
     * @param contentHandlerStack stack of content handlers
     * @param atts the attributes of a PMMLInterval
     * @param list the list is used to store the read PMMLInterval
     */
    public PMMLDiscretizeBinContentHandler (
            final Stack<PMMLContentHandler> contentHandlerStack,
            final Attributes atts,
            final List<PMMLDiscretizeBin> list) {
        m_contentHandlerStack = contentHandlerStack;
        m_list = list;
        m_binValue = atts.getValue("binValue");
        m_intervalList = new ArrayList<PMMLInterval>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        // do nothing here

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endDocument() throws SAXException {
        // do nothing here

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(final String uri, final String localName,
            final String name)
            throws SAXException {
        if ("Extension".equals(name)) {
            // do nothing
        } else if ("DiscretizeBin".equals(name)) {
            PMMLContentHandler me = m_contentHandlerStack.pop();
            assert me == this : "Invalid content handler stack";
            // end of this element (and its life cycle)
            m_list.add(new PMMLDiscretizeBin(m_binValue, m_intervalList));
        } else {
            throw new SAXException("Invalid end of element: " + name);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(final String uri, final String localName,
            final String name,
            final Attributes atts) throws SAXException {
        if ("Extension".equals(name)) {
            LOGGER.debug("Skipping unknown extension in DiscretizeBin: "
                    + name);
        } else if ("Interval".equals(name)) {
            m_contentHandlerStack.push(new PMMLIntervalContentHandler(
                    m_contentHandlerStack, atts, m_intervalList));
        } else {
            throw new SAXException("Unknown xml element in DiscretizeBin: "
                    + name);
        }
    }


}
