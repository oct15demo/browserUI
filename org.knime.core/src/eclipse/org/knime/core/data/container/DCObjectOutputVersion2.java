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
 * -------------------------------------------------------------------
 *
 * History
 *   Mar 29, 2006 (wiswedel): created
 */
package org.knime.core.data.container;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.BlobDataCell.BlobAddress;
import org.knime.core.data.filestore.internal.FileStoreKey;


/**
 * Object writing the binary stream of a {@link Buffer}.
 *
 * <p>This class provides functionality to write {@link DataCell} objects in
 * both possibles modes: Using a custom {@link DataCellSerializer} (for
 * {@link DataCell} class that offer it) as well as plain (and slow) java
 * serialization.
 *
 * <p>The general stream layout is as follows: Objects of this class write
 * to a {@link BlockableOutputStream} in order to write {@link DataCell}s to
 * an isolated sandbox. Each DataCell entry is preceded by one or more control
 * bytes, which are written by the associated {@link Buffer}. The {@link Buffer}
 * also decides when to close a block. {@link DataCell} which need to be java
 * serialized are written a freshly created {@link ObjectOutputStream} (for
 * each cell a new stream).
 *
 * @author Bernd Wiswedel, University of Konstanz
 */
class DCObjectOutputVersion2 implements KNIMEStreamConstants {

    /** Stream that we write to. Used to mark end of cells (or row keys). */
    private final BlockableOutputStream m_out;

    /** The Buffer that uses this stream, used to save DataCells contained
     * in (List)DataCell. */
    private final Buffer m_buffer;

    /** This stream writes to m_out and is passed to the DataCellSerializer. */
    private DCLongUTFDataOutputStream m_dataOut;

    /** Setups a new output stream.
     * @param out The stream to write to (the file)
     * @param ownerBuffer the associated buffer.
     */
    public DCObjectOutputVersion2(final OutputStream out,
            final Buffer ownerBuffer) {
        m_out = new BlockableOutputStream(out);
        m_dataOut = new DCLongUTFDataOutputStream(new DataOutputStream(m_out));
        m_buffer = ownerBuffer;
    }

    /** Writes a data cell using the serializer. No blocking is done (not here).
     * @param serializer The factory being used to write the cell.
     * @param cell The cell to be written.
     * @see DataCellSerializer#serialize(DataCell, DataCellDataOutput)
     * @throws IOException If that fails.
     */
    void writeDataCellPerKNIMESerializer(
            final DataCellSerializer<DataCell> serializer,
            final DataCell cell) throws IOException {
        serializer.serialize(cell, m_dataOut);
    }

    /** Saves file store cell.
     * @param key ...
     * @throws IOException ... */
    void writeFileStoreKey(final FileStoreKey key) throws IOException {
        key.save(m_dataOut);
    }

    /** Writes a cell using java serialization. This method will create
     * a new {@link ObjectOutputStream} for this single cell.
     * @param cell The cell to write.
     * @throws IOException If any exception occur. */
    void writeDataCellPerJavaSerialization(final DataCell cell)
        throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(m_dataOut);
        oos.writeObject(cell);
        oos.flush();
    }

    /** Writes a given blob address to the stream.
     * @param address to write out.
     * @throws IOException if that fails
     */
    void writeBlobAddress(final BlobAddress address) throws IOException {
        address.serialize(m_dataOut);
    }

    /** Writes a row key by writing the underlying string to the stream.
     * @param key Key to write
     * @throws IOException In case of stream corruption.
     */
    void writeRowKey(final RowKey key) throws IOException {
        m_dataOut.writeUTF(key.getString());
    }

    /** Writes the argument byte.
     * @param controlByte The byte to write.
     * @throws IOException In case of stream corruption.
     */
    void writeControlByte(final int controlByte) throws IOException {
        m_dataOut.write(controlByte);
    }

    /** Marks the end of the block.
     * @throws IOException In case of stream corruption.
     */
    void endBlock() throws IOException {
        m_out.endBlock();
    }

    /** Writes the row end identifier.
     * @throws IOException In case of stream corruption. */
    void endRow() throws IOException {
        m_out.write(BYTE_ROW_SEPARATOR);
    }

    /** Closes the underlying streams.
     * @throws IOException If the stream closing causes IO problems. */
    public void close() throws IOException {
        m_dataOut.close();
    }

    /** Flushes the underlying streams.
     * @throws IOException If the stream flush causes IO problems. */
    public void flush() throws IOException {
        m_dataOut.flush();
    }

    /** Stream that supports writing of encapsulated {@link DataCell} objects
     * as required by {@link DataCellDataOutput}. */
    private final class DCLongUTFDataOutputStream
        extends LongUTFDataOutputStream implements DataCellDataOutput {

        /** Delegates to super implementation.
         * @param output Forwarded to super. */
        public DCLongUTFDataOutputStream(final DataOutputStream output) {
            super(output);
        }

        /** {@inheritDoc} */
        @Override
        public void writeDataCell(final DataCell cell) throws IOException {
            m_buffer.writeDataCell(cell, DCObjectOutputVersion2.this);
        }

    }

}
