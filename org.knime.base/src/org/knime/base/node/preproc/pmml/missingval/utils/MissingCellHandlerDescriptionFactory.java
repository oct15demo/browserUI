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
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.knime.base.node.preproc.pmml.missingval.MissingCellHandlerFactory;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.util.CheckUtils;
import org.knime.missingval.v10.MissingcellhandlerDocument;
import org.knime.workbench.repository.util.NodeFactoryHTMLCreator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Factory for missing cell handler descriptions.
 *
 * @author Marcel Hanser, Alexander Fillbrunn
 * @since 2.12
 */
public final class MissingCellHandlerDescriptionFactory {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(MissingCellHandlerDescriptionFactory.class);

    private static final String SHORT_DESCRIPTION_TEMPLATE;

    private static final XmlOptions OPTIONS = new XmlOptions();
    static {
        Map<String, String> namespaceMap = new HashMap<String, String>(1);
        namespaceMap.put("", MissingcellhandlerDocument.type.getContentModel().getName().getNamespaceURI());
        OPTIONS.setLoadSubstituteNamespaces(namespaceMap);

        SHORT_DESCRIPTION_TEMPLATE = readFile("short_description_template.html");
    }

    private MissingCellHandlerDescriptionFactory() {
    }

    /**
     * Creates the default {@link MissingCellHandlerDescription}
     * for the given missing cell handler factory. The description xml file.
     * which is named as the simple class name of the missing cell handler factory + .xml
     * is expected to be located at the same factory.
     *
     * @param fac the missing cell handler factory to create the description for
     * @return creates parses the configuration file expected in the same package as the given distance factory class
     */
    public static final MissingCellHandlerDescription getDescription(final MissingCellHandlerFactory fac) {
        Class<?> factoryClass = CheckUtils.checkNotNull(fac).getClass();
        // do some stuff to determine the version
        LOGGER.debugWithFormat("Loading description for factory: %s", factoryClass);
        String descriptionFile = factoryClass.getSimpleName() + ".xml";
        InputStream resourceAsStream = factoryClass.getResourceAsStream(descriptionFile);

        if (resourceAsStream != null) {
            try {
                return new MissingCellHandlerDescriptionV1(resourceAsStream);
            } catch (XmlException | IOException e) {
                LOGGER.error("Error during loading description for factory: " + factoryClass, e);
                return errorDescription(fac.getDisplayName());
            } finally {
                IOUtils.closeQuietly(resourceAsStream);
            }
        }
        return emptyDescription(fac.getDisplayName());
    }

    /**
     * Adds the short description of the given {@link MissingCellHandlerFactory}s to the fullDescription DOM-Element.
     *
     * @param fullDescription DOM-Element of a Knime-Node
     * @param factoriesOfType registration types
     */
    public static void addShortDescriptionToNodeDescription(final Element fullDescription,
        final Iterable<MissingCellHandlerFactory> factoriesOfType) {
        CheckUtils.checkNotNull(factoriesOfType);
        StringBuilder builder =
            new StringBuilder("<option name='Missing Value Handler Selection' optional='false'>"
                + "Select and configure the missing value handler to be used for data types or columns. "
                + "Handlers that do not produce valid PMML 4.2 are marked with an asterisk (*).");

        for (MissingCellHandlerFactory reg : factoriesOfType) {
            String shortDescription = StringEscapeUtils.escapeXml(reg.getDescription().getShortDescription());
            String name = reg.getDescription().getName();
            if (!reg.producesPMML4_2()) {
                name += MissingCellHandlerFactory.NO_PMML_INDICATOR;
            }
            String subDescription = SHORT_DESCRIPTION_TEMPLATE.replace("[NAME]", name).replace("[SHORT_DESCRIPTION]",
                shortDescription);
            try {
                // try to parse the xml snippet and ignore it if that fails
                loadXmlFromString(subDescription);
                builder.append(subDescription);
            } catch (ParserConfigurationException | SAXException | IOException e2) {
                LOGGER.coding("Fail on adding description for missing cell handler: " + reg.getID(), e2);
            }
        }
        builder.append("</option>");
        Document subDescriptionNode;
        try {
            subDescriptionNode = loadXmlFromString(builder.toString());
            Node importedNode = fullDescription.getOwnerDocument().importNode(subDescriptionNode.getFirstChild(), true);
            fullDescription.appendChild(importedNode);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            // that should not happen, as it would be a bug!
            LOGGER.coding("Invalid html fallback handling.", e);
            throw new IllegalArgumentException("Invalid html fallback handling.", e);
        }
    }

    /**
     * @param xml to parse
     * @return the parsed XML document
     * @throws ParserConfigurationException if the parsing fails
     * @throws SAXException if the parsing fails
     * @throws IOException if the parsing fails
     */
    public static Document loadXmlFromString(final String xml) throws ParserConfigurationException, SAXException,
        IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

    /**
     * @param factoryClass
     * @return
     */
    private static MissingCellHandlerDescription emptyDescription(final String name) {
        return new DefaultMissingCellHandlerDescription("No description provided.", name);
    }

    /**
     * @param factoryClass
     * @return
     */
    private static MissingCellHandlerDescription errorDescription(final String name) {
        return new DefaultMissingCellHandlerDescription("Error during description parsing, see the log.", name);
    }

    private static String readFile(final String fileName) {
        InputStream resourceAsStream = MissingCellHandlerDescriptionFactory.class.getResourceAsStream(fileName);
        CheckUtils.checkArgument(resourceAsStream != null, "Resource : '%s' cannot be found", fileName);
        try {
            return IOUtils.toString(resourceAsStream).replace("[CSS]", NodeFactoryHTMLCreator.instance.getCss());
        } catch (IOException e) {
            LOGGER.coding("Error reading " + fileName, e);
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(resourceAsStream);
        }
    }

    /**
     * A {@link MissingCellHandlerDescription} implementation
     * for missing cell handlers without an explicit description xml.
     *
     * @author Marcel Hanser, Alexander Fillbrunn
     */
    private static final class DefaultMissingCellHandlerDescription implements MissingCellHandlerDescription {

        private final String m_defaultDescription;

        private final String m_name;

        /**
         * @param defaultDescription
         * @param name
         */
        private DefaultMissingCellHandlerDescription(final String defaultDescription, final String name) {
            super();
            m_defaultDescription = defaultDescription;
            m_name = name;
        }

        /**
         * {@inheritDoc}
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
            return m_defaultDescription;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isMissing() {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Element getElement() {
            return null;
        }
    }
}
