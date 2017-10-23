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
 */
package org.knime.core.data.image.png;

import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataTypeRegistry;
import org.knime.core.data.DataValue;
import org.knime.core.data.container.BlobDataCell;


/** Blob implemenation of a PNG image cell.
 * @author Thomas Gabriel, KNIME.com AG, Zurich
 */
@SuppressWarnings("serial")
public class PNGImageBlobCell extends BlobDataCell implements PNGImageValue {
    /**
     * Serialier for {@link PNGImageBlobCell}s.
     *
     * @since 3.0
     * @noreference This class is not intended to be referenced by clients.
     */
    public static final class PNGSerializer implements DataCellSerializer<PNGImageBlobCell> {
        @Override
        public PNGImageBlobCell deserialize(final DataCellDataInput input) throws IOException {
            return new PNGImageBlobCell(PNGImageContent.deserialize(input));
        }

        @Override
        public void serialize(final PNGImageBlobCell cell, final DataCellDataOutput output) throws IOException {
            cell.m_content.serialize(output);
        }
    }

    /**
     * Serializer as required by parent class.
     *
     * @return A serializer for reading/writing cells of this kind.
     * @deprecated use {@link DataTypeRegistry#getSerializer(Class)} instead
     */
    @Deprecated
    public static DataCellSerializer<PNGImageBlobCell> getCellSerializer() {
        return new PNGSerializer();
    }

    private final PNGImageContent m_content;

    /** Package scope method to create PNG image cell. Used from the
     * {@link PNGImageContent#toImageCell()} method.
     * @param content The content to wrap.
     */
    PNGImageBlobCell(final PNGImageContent content) {
        if (content == null) {
            throw new NullPointerException("Argument must not be null.");
        }
        m_content = content;
    }

    /** {@inheritDoc} */
    @Override
    public PNGImageContent getImageContent() {
        return m_content;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return m_content.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean equalsDataCell(final DataCell dc) {
        PNGImageBlobCell ic = (PNGImageBlobCell) dc;
        return m_content.equals(ic.m_content);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean equalContent(final DataValue otherValue) {
        return PNGImageValue.equalContent(this, (PNGImageValue)otherValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return m_content.hashCode();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public String getImageExtension() {

        return "png";
    }

}
