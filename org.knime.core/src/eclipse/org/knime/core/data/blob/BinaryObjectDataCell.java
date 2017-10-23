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
 * Created on Sep 12, 2012 by wiswedel
 */
package org.knime.core.data.blob;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataType;
import org.knime.core.data.DataTypeRegistry;
import org.knime.core.data.DataValue;
import org.knime.core.node.NodeLogger;

/** Cell implementation of {@link BinaryObjectDataValue} that keeps the binary content in a byte array.
 *
 * @author Bernd Wiswedel, KNIME.com, Zurich, Switzerland
 * @since 2.7
 */
@SuppressWarnings("serial")
public final class BinaryObjectDataCell extends DataCell implements BinaryObjectDataValue {
    /**
     * Serializer for {@link BinaryObjectDataCell}s.
     *
     * @noreference This class is not intended to be referenced by clients.
     * @since 3.0
     */
    public static final class BinaryObjectCellSerializer implements DataCellSerializer<BinaryObjectDataCell> {
            /** {@inheritDoc} */
            @Override
            public BinaryObjectDataCell deserialize(final DataCellDataInput input)
                    throws IOException {
                int length = input.readInt();
                byte[] bytes = new byte[length];
                input.readFully(bytes);
                int md5Length = input.readInt();
                byte[] md5sum = new byte[md5Length];
                input.readFully(md5sum);
                return new BinaryObjectDataCell(bytes, md5sum);
            }

        /** {@inheritDoc} */
        @Override
        public void serialize(final BinaryObjectDataCell cell, final DataCellDataOutput output)
                throws IOException {
            output.writeInt(cell.m_bytes.length);
            output.write(cell.m_bytes);
            output.writeInt(cell.m_md5sum.length);
            output.write(cell.m_md5sum);
        }
    }

    /** Type associated with this cells implementing {@link BinaryObjectDataValue}. */
    public static final DataType TYPE = DataType.getType(BinaryObjectDataCell.class);

    private final byte[] m_bytes;
    private final byte[] m_md5sum;

    /**
     * Serializer as required by {@link DataCell} class.
     *
     * @return A serializer.
     * @noreference This method is not intended to be referenced by clients.
     * @deprecated user {@link DataTypeRegistry#getSerializer(Class)} instead
     */
    @Deprecated
    public static final DataCellSerializer<BinaryObjectDataCell> getCellSerializer() {
        return new BinaryObjectCellSerializer();
    }

    /** Constructor used by factory.
     * @param bytes Bytes to wrap.
     * @param md5sum The MD5 of the byte array -- needed for hash code and equality check
     * @throws NullPointerException If argument is null.
     */
    BinaryObjectDataCell(final byte[] bytes, final byte[] md5sum) {
        if (bytes == null) {
            throw new NullPointerException("Argument must not be null.");
        }
        m_bytes = bytes;
        m_md5sum = md5sum;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        try {
            return BinaryObjectCellFactory.getHexDump(openInputStream(), 1024);
        } catch (IOException e) {
            return "Failed rendering: " + e.getMessage();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean equalsDataCell(final DataCell dc) {
        BinaryObjectDataCell odc = (BinaryObjectDataCell)dc;
        return odc.length() == length() && Arrays.equals(odc.m_md5sum, m_md5sum);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        long length = length();
        return (int)(length ^ (length >>> 32)) ^ Arrays.hashCode(m_md5sum);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long length() {
        return m_bytes.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream openInputStream() throws IOException {
        return new ByteArrayInputStream(m_bytes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean equalContent(final DataValue otherValue) {
        try {
            return BinaryObjectDataValue.equalContent(this, (BinaryObjectDataValue)otherValue);
        } catch (IOException ex) {
            NodeLogger.getLogger(getClass()).error("I/O error while comparing contents: " + ex.getMessage(), ex);
            return false;
        }
    }
}
