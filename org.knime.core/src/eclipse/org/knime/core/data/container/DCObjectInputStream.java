 
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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectInputValidation;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.container.BlobDataCell.BlobAddress;


/**
 * Obsolete input stream to read Files written with Buffer objects in KNIME
 * 1.x (and 2.0TechPreview).
 * 
 * <p>
 * Input stream that is used by the Buffer to read (java.io.-)serialized  
 * <code>DataCell</code> (the ones whose type does not support customized 
 * reading/writing) and also <code>DataCell</code> objects that have been
 * written using a <code>DataCellSerializer</code>. The class extends 
 * ObjectInputStream but delegates incoming <code>readObject()</code> requests
 * to a private <code>ObjectInputStream</code>. 
 * 
 * <p>Reading <code>DataCell</code> using a <code>DataCellSerializer</code>
 * is done using the <code>readDataCell()</code> method. It will use another
 * input stream that delegates itself to the private ObjectInputStream but 
 * uses blocks to determine the end of a <code>DataCell</code>. An attempt to
 * summarize the different streams is made in the following figure (for the 
 * output stream though).
 * 
 * <p>
 * <center>
 *   <img src="doc-files/objectoutput.png" alt="Streams" align="middle">
 * </center>
 */
final class DCObjectInputStream extends ObjectInputStream {

    /** The streams that is being written to. */
    private final PriorityGlobalObjectInputStream m_inObject;
    /** Wrapped stream that is passed to the DataCellSerializer,
     * this stream reads from m_in. */
    private final ObsoleteDCDataInputStream m_dataInStream;
    /** Escapable stream, returns eof when block ends. */
    private final BlockableInputStream m_in;
    
    /**
     * Creates new input stream that reads from <code>in</code>.
     * @param in The stream to read from.
     * @throws IOException If the init of the stream reading fails.
     */
    DCObjectInputStream(final InputStream in) throws IOException {
        m_inObject = new PriorityGlobalObjectInputStream(in);
        m_in = new BlockableInputStream(m_inObject);
        m_dataInStream = 
            new ObsoleteDCDataInputStream(new DataInputStream(m_in));
    }
    
    /** Reads a data cell from the stream and pushes the stream forward to 
     * the end of the block.
     * @param serializer The factory that is used to create the cell
     * @return A new data cell instance.
     * @throws IOException If reading fails.
     * @see DataCellSerializer#deserialize(DataCellDataInput)
     */
    public DataCell readDataCell(
            final DataCellSerializer<? extends DataCell> serializer)
    throws IOException {
        try {
            return serializer.deserialize(m_dataInStream);
        } finally {
            m_in.endBlock();
        }
    }
    
    /** Reads a blob address from the stream and ends the block.
     * @return as read from the stream.
     * @throws IOException If that fails.
     */
    public BlobAddress readBlobAddress() throws IOException {
        try {
            return BlobAddress.deserialize(m_dataInStream);
        } finally {
            m_in.endBlock();
        }
    }
    
    /* The following methods all delegate to the underlying m_inObject stream.
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public int available() throws IOException {
        return m_inObject.available();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        m_inObject.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void defaultReadObject() throws IOException, ClassNotFoundException {
        m_inObject.defaultReadObject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mark(final int readlimit) {
        m_inObject.mark(readlimit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean markSupported() {
        return m_inObject.markSupported();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException {
        return m_inObject.read();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(final byte[] buf, final int off, final int len) 
        throws IOException {
        return m_inObject.read(buf, off, len);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(final byte[] b) throws IOException {
        return m_inObject.read(b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean readBoolean() throws IOException {
        return m_inObject.readBoolean();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte readByte() throws IOException {
        return m_inObject.readByte();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char readChar() throws IOException {
        return m_inObject.readChar();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double readDouble() throws IOException {
        return m_inObject.readDouble();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GetField readFields() throws IOException, ClassNotFoundException {
        return m_inObject.readFields();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float readFloat() throws IOException {
        return m_inObject.readFloat();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readFully(final byte[] buf, final int off, final int len)
        throws IOException {
        m_inObject.readFully(buf, off, len);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readFully(final byte[] buf) throws IOException {
        m_inObject.readFully(buf);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readInt() throws IOException {
        return m_inObject.readInt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long readLong() throws IOException {
        return m_inObject.readLong();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object readObjectOverride() 
        throws IOException, ClassNotFoundException {
        return m_inObject.readObject();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public short readShort() throws IOException {
        return m_inObject.readShort();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object readUnshared() throws IOException, ClassNotFoundException {
        return m_inObject.readUnshared();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readUnsignedByte() throws IOException {
        return m_inObject.readUnsignedByte();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readUnsignedShort() throws IOException {
        return m_inObject.readUnsignedShort();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String readUTF() throws IOException {
        return m_inObject.readUTF();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerValidation(
            final ObjectInputValidation obj, final int prio) 
        throws NotActiveException, InvalidObjectException {
        m_inObject.registerValidation(obj, prio);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() throws IOException {
        m_inObject.reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long skip(final long n) throws IOException {
        return m_inObject.skip(n);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int skipBytes(final int len) throws IOException {
        return m_inObject.skipBytes(len);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return m_inObject.toString();
    }
    
    /** Set the class loader to ask "first" to load classes. Used when 
     * a data cell is deserialized and all its member should be loaded in the
     * context of that class loader.
     * @param l The class loader to use, if <code>null</code> it uses the 
     * globally known class loader (GlobalClassCreator)
     */
    void setCurrentClassLoader(final ClassLoader l) {
        m_inObject.setCurrentClassLoader(l);
    }
    
    private static final class ObsoleteDCDataInputStream 
        extends LongUTFDataInputStream implements DataCellDataInput {

        /** Inherited constructor.
         * @param input Passed to super implementation.
         */
        public ObsoleteDCDataInputStream(final DataInputStream input) {
            super(input);
        }

        /** Throws always an exception as reading DataCells is not supported.
         * {@inheritDoc} */
        @Override
        public DataCell readDataCell() throws IOException {
            throw new IOException("The stream was written with a version that "
                    + "does not support reading/writing of encapsulated " 
                    + "DataCells");
        }
        
    }
    
}
