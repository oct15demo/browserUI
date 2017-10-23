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
package org.knime.core.node.port;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.swing.JComponent;

import org.knime.core.internal.SerializerMethodLoader.Serializer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.ModelContent;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.util.ConvenienceMethods;


/**
 * General interface for objects that are passed along node
 * connections. Most prominent example of such an object is
 * {@link org.knime.core.node.BufferedDataTable}.
 * <code>PortObjects</code> contain the actual data or models, which are used
 * during a node's
 * {@link NodeModel#execute(PortObject[], ExecutionContext) execution}.
 * <br>
 * <p><b>Important:</b> Implementors of this interface must also provide a
 * {@link PortObjectSerializer}, which is used to save and load instances. The serializer must be registered at
 * the extension point <tt>org.knime.core.PortType</tt>.
 * <br>
 * There are two exceptions to this rule: Objects of class {@link BufferedDataTable} and
 * {@link ModelContent} are treated separately, they don't need to be registered.
 *
 * <p>
 * <b>Note:</b> The API of this class is not finalized and may slightly change
 * in future versions.
 *
 * @see org.knime.core.node.BufferedDataTable
 * @see PortObjectSpec
 * @see PortType
 * @see AbstractPortObject
 * @see AbstractSimplePortObject
 * @since 2.0
 * @author Bernd Wiswedel &amp; Michael Berthold, University of Konstanz
 */
public interface PortObject {
    /**
     * Type for generic ports.
     * @since 3.0
     */
    public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(PortObject.class);

    /**
     * Type for generic optional ports.
     * @since 3.0
     */
    public static final PortType TYPE_OPTIONAL = PortTypeRegistry.getInstance().getPortType(PortObject.class, true);

    /** Factory class that's used for writing and loading objects of class
     * denoted by <code>T</code>. See description of class {@link PortObject}
     * for details.
     * @param <T> class of the object to save or load. */
    abstract static class PortObjectSerializer<T extends PortObject>
        implements Serializer<T> {

        /** Saves the portObject to an output stream. There is no need
         * to also save the {@link PortObjectSpec} associated with the port
         * object as the framework will save both in different places and
         * will provide the spec when {@link #loadPortObject
         * PortObjectZipInputStream, PortObjectSpec, ExecutionMonitor)}
         * is called.
         * @param portObject The object to save.
         * @param out Where to save to
         * @param exec To report progress to and to check for cancelation.
         * @throws IOException If that fails for IO problems.
         * @throws CanceledExecutionException If canceled.
         */
        public abstract void savePortObject(final T portObject,
                final PortObjectZipOutputStream out,
                final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException;

        /** Load a portObject from an input stream.
         * @param in Where to load from
         * @param spec The spec that was associated with the object. It can
         * safely be cast to the expected PortObjectSpec class.
         * @param exec To report progress to and to check for cancelation.
         * @return The restored object.
         * @throws IOException If that fails for IO problems.
         * @throws CanceledExecutionException If canceled.
         */
        public abstract T loadPortObject(final PortObjectZipInputStream in,
                final PortObjectSpec spec, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException;


        /**
         * Returns the port object class that this serializer reads and writes. The class is determined from the generic
         * argument.
         *
         * @return a port object class
         * @since 3.0
         */
        @SuppressWarnings("unchecked")
        Class<T> getObjectClass() {
             for (Type type : ConvenienceMethods.getAllGenericInterfaces(getClass())) {
                if (type instanceof ParameterizedType) {
                    Type rawType = ((ParameterizedType) type).getRawType();
                    Type typeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
                    if (PortObjectSerializer.class == rawType) {
                        if (typeArgument instanceof Class) {
                            return (Class<T>)typeArgument;
                        } else if (typeArgument instanceof ParameterizedType) { // e.g. ImgPlusCell<T>
                            return (Class<T>)((ParameterizedType) typeArgument).getRawType();
                        }
                    }
                }
            }

            for (Type type : ConvenienceMethods.getAllGenericSuperclasses(getClass())) {
                if (type instanceof ParameterizedType) {
                    Type typeArgument = ((ParameterizedType)type).getActualTypeArguments()[0];
                    if (PortObject.class.isAssignableFrom((Class<?>)typeArgument)) {
                        if (typeArgument instanceof Class) {
                            return (Class<T>)typeArgument;
                        } else if (typeArgument instanceof ParameterizedType) {
                            return (Class<T>)((ParameterizedType) typeArgument).getRawType();
                        }
                    }
                }
            }

            try {
                Class<T> c = (Class<T>)getClass()
                    .getMethod("loadPortObject", PortObjectZipInputStream.class, PortObjectSpec.class)
                    .getGenericReturnType();
                if (!PortObject.class.isAssignableFrom(c) || ((c.getModifiers() & Modifier.ABSTRACT) != 0)) {
                    NodeLogger.getLogger(getClass())
                        .coding(getClass().getName() + " does not use generics properly, the type of the created port "
                            + "object is '" + c.getName() + "'. Please fix your implementation by specifying a "
                            + "non-abstract port object type in the extended PortObjectSerializer class.");
                    return (Class<T>)PortObject.class;
                } else {
                    return c;
                }
            } catch (NoSuchMethodException ex) {
                // this is not possible
                throw new AssertionError("Someone removed the 'loadPortObject' method from this class");
            }
        }
    }

    /** Get a short summary of this <code>PortObject</code>.
     * The return value will be shown in a node port's tooltip, for instance.
     * @return Summary of the object's content, suitable for a tooltip. Empty
     * strings and null result values are ok (though not encouraged).
     */
    String getSummary();

    /**
     * Get specification to this port object. That is, the corresponding
     * {@link PortObjectSpec} which is used to configure any successor node
     * after execution, e.g. a <code>BufferedDataTable</code> can return a
     * <code>DataTableSpec</code>.
     *
     * <p>Subclasses should narrow the return type if possible.
     *
     * @return underlying <code>PortObjectSpec</code> or any derived spec,
     *         never <code>null</code>.
     */
    PortObjectSpec getSpec();

    /**
     * The returned views are displayed in the out port view of the referring
     * node. Each component is displayed in an extra tab. The name of the
     * component is used as the title for the tab. It is important that no
     * external members are kept in the component so it can be deleted, when
     * the port object is deleted. If the port object has no view return an
     * empty array.
     *
     * <p><b>Note</b, instances implementing {@link PortObjectView} will be
     * treated specially by the framework. See interface definition for details.
     *
     * @return an array of views for the port object, each displayed as a tab
     * in the out port view
     */
    public JComponent[] getViews();

}
