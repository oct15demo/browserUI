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
 *   27.04.2010 (hofer): created
 */
package org.knime.base.node.preproc.autobinner.pmml;

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
final class PMMLIntervalContentHandler  extends PMMLContentHandler {
    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(PMMLIntervalContentHandler.class);

    private Stack<PMMLContentHandler> m_contentHandlerStack;
    private List<PMMLInterval> m_list;
    private PMMLInterval m_element;

    /**
     * @param contentHandlerStack stack of content handlers
     * @param atts the attributes of a PMMLInterval
     * @param list the list is used to store the read PMMLInterval
     */
    public PMMLIntervalContentHandler(
            final Stack<PMMLContentHandler> contentHandlerStack,
            final Attributes atts,
            final List<PMMLInterval> list) {
        m_contentHandlerStack = contentHandlerStack;
        m_list = list;

        PMMLInterval.Closure closure =
            PMMLInterval.Closure.valueOf(atts.getValue("closure"));

        double leftMargin = null != atts.getValue("leftMargin")
            ? Double.valueOf(atts.getValue("leftMargin"))
            : Double.NEGATIVE_INFINITY;
        double rightMargin = null != atts.getValue("rightMargin")
            ? Double.valueOf(atts.getValue("rightMargin"))
            : Double.POSITIVE_INFINITY;

        m_element = new PMMLInterval(leftMargin, rightMargin, closure);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endDocument() throws SAXException {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(final String uri, final String localName,
            final String name)
            throws SAXException {
        if ("Extension".equals(name)) {
            // sub element, ignore
        } else if ("Interval".equals(name)) {
            PMMLContentHandler me = m_contentHandlerStack.pop();
            assert me == this : "Invalid content handler stack";
            m_list.add(m_element);
        } else {
            throw new SAXException("Invalid end of element: " + name);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(final String uri,
            final String localName, final String name,
            final Attributes atts) throws SAXException {
        if ("Extension".equals(name)) {
            LOGGER.debug("Skipping unknown extension in Interval: " + name);
        } else {
            throw new SAXException("Unknown xml element in Interval: "
                    + name);
        }
    }

}
