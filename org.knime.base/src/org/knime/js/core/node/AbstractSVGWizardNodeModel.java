/*
 * ------------------------------------------------------------------------
 *
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
 * History
 *   07.11.2014 (Christian Albrecht, KNIME.com AG, Zurich, Switzerland): created
 */
package org.knime.js.core.node;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.knime.base.data.xml.SvgCell;
import org.knime.base.data.xml.SvgImageContent;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.port.image.ImagePortObjectSpec;
import org.knime.core.node.wizard.WizardNode;
import org.knime.js.core.JSONViewContent;

/**
 *
 * @author Christian Albrecht, KNIME.com AG, Zurich, Switzerland
 * @param <REP> The concrete class of the {@link JSONViewContent} acting as representation of the view.
 * @param <VAL> The concrete class of the {@link JSONViewContent} acting as value of the view.
 * @since 2.11
 */
public abstract class AbstractSVGWizardNodeModel<REP extends JSONViewContent, VAL extends JSONViewContent> extends
    AbstractImageWizardNodeModel<REP, VAL> {

    /**
     * Creates a new {@link WizardNode} model with the given number (and types!) of input and output types.
     *
     * @param inPortTypes an array of non-null in-port types
     * @param outPortTypes an array of non-null out-port types
     * @param viewName the view name
     */
    protected AbstractSVGWizardNodeModel(final PortType[] inPortTypes, final PortType[] outPortTypes, final String viewName) {
        super(inPortTypes, outPortTypes, viewName);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected final ImagePortObject createImagePortObjectFromView(final String imageData, final String error) throws IOException {
        String xmlPrimer = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
        String svgPrimer =
            xmlPrimer + "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.0//EN\" "
                + "\"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">";
        String image = imageData;
        String errorText = error;
        if (StringUtils.isEmpty(image)) {
            if (StringUtils.isEmpty(errorText)) {
                errorText = "JavaScript returned nothing. Possible implementation error.";
            }
            image = "<svg width=\"600px\" height=\"40px\">"
                    + "<text x=\"0\" y=\"20\" font-family=\"sans-serif;\" font-size=\"10\">"
               + "SVG retrieval failed: " + errorText + "</text></svg>";
        }
        image = svgPrimer + image;
        InputStream is = new ByteArrayInputStream(image.getBytes("UTF-8"));
        ImagePortObjectSpec imageSpec = new ImagePortObjectSpec(SvgCell.TYPE);
        return new ImagePortObject(new SvgImageContent(is), imageSpec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getExtractImageMethodName() {
        return "getSVG";
    }
}
