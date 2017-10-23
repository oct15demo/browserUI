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
 *   Jun 5, 2008 (wiswedel): created
 */
package org.knime.core.node.port;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;

import org.knime.core.data.util.NonClosableInputStream;
import org.knime.core.data.util.NonClosableOutputStream;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;

/**
 * Abstract implementation of general port objects. Extending this class
 * (as opposed to just implementing {@link PortObject}) has the advantage that
 * the serializing methods are enforced by abstract methods (rather than
 * defining a static method with a particular name as given by the interface).
 *
 * <p>Subclasses <b>must</b> provide an empty no-arg constructor with public
 * scope (which will be used to restore the content). They are encouraged to
 * also provide a convenience access member such as
 * <pre>
 *   public static final PortType TYPE = new PortType(FooModelPortObject.class);
 * </pre>
 * and to narrow the return type of the {@link PortObject#getSpec() getSpec()}
 * method. Derived classes don't need to provide a static serializer method as
 * required by the interface {@link PortObject}.
 *
 * @author Bernd Wiswedel, University of Konstanz
 */
public abstract class AbstractPortObject implements PortObject {
    /** Public no-arg constructor. Subclasses must also provide such a
     * constructor in order to allow the serializer to instantiate them using
     * reflection. */
    public AbstractPortObject() {
    }

    /** Saves this object to an output stream. This method represents the
     * implementation of {@link PortObject.PortObjectSerializer
     * #savePortObject(PortObject, PortObjectZipOutputStream,
     * ExecutionMonitor)}.
     * @param out A clean directory to write to.
     * @param exec For progress/cancelation.
     * @throws IOException If writing fails
     * @throws CanceledExecutionException If canceled.
     */
    protected abstract void save(
            final PortObjectZipOutputStream out, ExecutionMonitor exec)
        throws IOException, CanceledExecutionException;

    /** Loads the content into the freshly instantiated object. This method
     * is called at most once in the life time of the object
     * (after the serializer has created a new object using the public no-arg
     * constructor.)
     * @param in To restore from
     * @param spec The accompanying spec (which can be safely cast to the
     * expected class).
     * @param exec For progress/cancelation.
     * @throws IOException If reading fails.
     * @throws CanceledExecutionException If canceled.
     */
    protected abstract void load(final PortObjectZipInputStream in,
            final PortObjectSpec spec, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException;

    /**
     * Abstract implementation of the a serializer for all {@link AbstractPortObject}s. Subclasses can simply extend
     * this class with the appropriate type without implementing any methods.
     *
     * @since 3.0
     */
    public static abstract class AbstractPortObjectSerializer<T extends AbstractPortObject> extends PortObjectSerializer<T> {
        /** {@inheritDoc} */
        @Override
        public T loadPortObject(final PortObjectZipInputStream in, final PortObjectSpec spec,
            final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
            AbstractPortObject result;
            ZipEntry entry = in.getNextEntry();
            if (!entry.getName().equals("meta.xml")) {
                throw new IOException("Expected meta.xml file in stream, got "
                        + entry.getName());
            }
            InputStream noneCloseIn = new NonClosableInputStream.Zip(in);
            ModelContentRO meta = ModelContent.loadFromXML(noneCloseIn);
            String className;
            try {
                className = meta.getString("class_name");
            } catch (InvalidSettingsException e1) {
                throw new IOException("Unable to load settings", e1);
            }
            Class<?> cl;
            try {
                cl = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(
                        "Unable to load class " + className, e);
            }
            if (!AbstractPortObject.class.isAssignableFrom(cl)) {
                throw new RuntimeException(
                        "Class \"" + className + "\" is not of type "
                        + AbstractPortObject.class.getSimpleName());
            }
            Class<? extends AbstractPortObject> acl =
                cl.asSubclass(AbstractPortObject.class);
            try {
                result = acl.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(
                        "Failed to instantiate class \""
                        + acl.getSimpleName()
                        + "\" (failed to invoke no-arg constructor): "
                        + e.getMessage(), e);
            }
            // current zip entry was already closed by ModelContent.loadFrom...
            result.load(in, spec, exec);
            return (T)result;
        }

        /** {@inheritDoc} */
        @Override
        public void savePortObject(final T portObject, final PortObjectZipOutputStream out, final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
            // this is going to throw a runtime exception in case...
            out.putNextEntry(new ZipEntry("meta.xml"));
            ModelContent meta = new ModelContent("meta.xml");
            meta.addInt("version", 1);
            meta.addString("class_name", portObject.getClass().getName());
            meta.saveToXML(new NonClosableOutputStream.Zip(out));
            portObject.save(out, exec);
        }
    }
}
