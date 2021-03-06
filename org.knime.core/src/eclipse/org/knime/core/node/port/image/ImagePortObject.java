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
 */
package org.knime.core.node.port.image;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.image.ImageContent;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.AbstractPortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

/**
 * Port object representing a simple image (png, ...).
 *
 * @author Thomas Gabriel, KNIME.com, Zurich, Switzerland
 */
public class ImagePortObject extends AbstractPortObject {
    /**
     * @noreference This class is not intended to be referenced by clients.
     * @since 3.0
     */
    public static final class Serializer extends AbstractPortObjectSerializer<ImagePortObject> {}

    /** Convenience accessor for the port type. */
    public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(ImagePortObject.class);

    /**
     * Convenience accessor for the optional port type.
     * @since 2.12
     **/
    public static final PortType TYPE_OPTIONAL =
        PortTypeRegistry.getInstance().getPortType(ImagePortObject.class, true);

    private static final NodeLogger LOGGER = NodeLogger
            .getLogger(ImagePortObject.class);

    private ImageContent m_content;

    private ImagePortObjectSpec m_spec;

    /** Empty framework constructor. <b>Do not use!</b> */
    public ImagePortObject() {
        // no op
    }

    /**
     * Create new port object based on the given arguments. The cell class that
     * is generated by the {@link ImageContent#toImageCell()} method must be
     * compatible to all DataValue of the spec's
     * {@link ImagePortObjectSpec#getDataType()} return value.
     *
     * @param content The image content.
     * @param spec The spec.
     * @throws NullPointerException If either argument is null.
     */
    public ImagePortObject(final ImageContent content,
            final ImagePortObjectSpec spec) {
        if (spec == null || content == null) {
            throw new NullPointerException("Argument must not be null.");
        }
        m_content = content;
        m_spec = spec;
    }

    /**
     * Produces a single data cell containing the image. This method also
     * verifies the requirements stated in the
     * {@link #ImagePortObject(ImageContent, ImagePortObjectSpec) constructor}.
     *
     * @return A new cell representing the image.
     * @see ImageContent#toImageCell()
     */
    public DataCell toDataCell() {
        DataType typeInSpec = m_spec.getDataType();
        DataCell result = m_content.toImageCell();
        if (!typeInSpec.isASuperTypeOf(result.getType())) {
            LOGGER.coding("Inconsistent data types for image cell \""
                    + result.getClass().getName() + "\" -- expected type \""
                    + typeInSpec + "\"");
            return DataType.getMissingCell();
        } else {
            return result;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getSummary() {
        return m_content.getSummary();
    }

    /** {@inheritDoc} */
    @Override
    public ImagePortObjectSpec getSpec() {
        return m_spec;
    }

    /** {@inheritDoc} */
    @Override
    public JComponent[] getViews() {
        @SuppressWarnings("serial")
        JComponent c = new JComponent() {
            /** {@inheritDoc} */
            @Override
            protected void paintComponent(final Graphics g) {
                super.paintComponent(g);
                Graphics gClip =
                        g.create(getX(), getY(), getWidth(), getHeight());
                Graphics2D g2d = (Graphics2D)gClip;
                m_content.paint(g2d, getWidth(), getHeight());
            }

            /** {@inheritDoc} */
            @Override
            public Dimension getPreferredSize() {
                return m_content.getPreferredSize();
            }
        };
        JScrollPane jsp = new JScrollPane(c);
        jsp.setName("Image");
        return new JComponent[]{jsp};
    }

    /** {@inheritDoc} */
    @Override
    protected void load(final PortObjectZipInputStream in,
            final PortObjectSpec spec, final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        ZipEntry nextEntry = in.getNextEntry();
        String contentClName = nextEntry.getName();

        Class<? extends ImageContent> contentCl;
        try {
            contentCl =
                    (Class<? extends ImageContent>)Class.forName(contentClName);
        } catch (ClassNotFoundException ex) {
            throw new IOException("ImageContent class '" + contentClName + "'"
                    + " does not exist", ex);
        }

        if (!ImageContent.class.isAssignableFrom(contentCl)) {
            throw new IOException("Class '" + contentClName
                    + "' is not an ImageContent");
        }

        Constructor<? extends ImageContent> cons;
        try {
            cons = contentCl.getConstructor(InputStream.class);
        } catch (Exception ex) {
            throw new IOException("ImageContent class '" + contentClName + "' "
                    + "is missing a required constructor, see javadoc", ex);
        }

        try {
            m_content = cons.newInstance(in);
        } catch (Exception ex) {
            throw new IOException("Could not create an instance of '"
                    + contentClName + "'", ex);
        }
        in.close();
        m_spec = (ImagePortObjectSpec)spec;
    }

    /** {@inheritDoc} */
    @Override
    protected void save(final PortObjectZipOutputStream out,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        out.putNextEntry(new ZipEntry(m_content.getClass().getName()));
        m_content.save(out);
        out.close();
    }
}
