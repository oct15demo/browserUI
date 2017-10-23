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
 */
package org.knime.core.data;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.knime.core.internal.SerializerMethodLoader.Serializer;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.util.ConvenienceMethods;

/**
 * Interface for classes that can read or write specific {@link DataCell} implementations. Using
 * <code>DataCellSerializer</code> implementations is usually considerably faster than ordinary Java serialization.
 * <br>
 * The framework creates one serializer instance that is re-used. Therefore serializers must be thread-safe. Serializers
 * must be registered at the extension point <code>org.knime.core.DataType</code> under the appropriate DataType.
 * <br>
 * For further details see the {@link DataCell} description and the
 * <a href="doc-files/newtypes.html#newtypes">manual</a> on how to define new types.
 *
 * @param <T> a {@link org.knime.core.data.DataValue} implementation being read or written
 * @author Bernd Wiswedel, University of Konstanz
 */
public interface DataCellSerializer<T extends DataCell> extends Serializer<T> {

    /**
     * Saves the <code>cell</code> to the <code>output</code> stream.
     *
     * @param cell the <code>DataCell</code> to save
     * @param output the place to write to
     * @throws IOException if writing fails
     */
    void serialize(final T cell, final DataCellDataOutput output)
        throws IOException;

    /**
     * Loads a new instance of {@link org.knime.core.data.DataCell} of type
     * <code>T</code> from the <code>input</code> stream, which represents a
     * former serialized <code>DataCell</code> content.
     *
     * @param input the source to load from, never <code>null</code>
     * @return a new {@link org.knime.core.data.DataValue} instance of type
     *         <code>T</code> representing a former serialized
     *         <code>DataCell</code>
     * @throws IOException if loading fails
     */
    T deserialize(final DataCellDataInput input) throws IOException;


    /**
     * Returns the cell class that this serializer reads and writes. The class is determined from the generic argument.
     *
     * @return a cell class
     * @since 3.0
     * @nooverride This default method is not intended to be re-implemented or extended by clients.
     */
    @SuppressWarnings("unchecked")
    default Class<T> getCellClass() {
         for (Type type : ConvenienceMethods.getAllGenericInterfaces(getClass())) {
            if (type instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) type).getRawType();
                Type typeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
                if (DataCellSerializer.class == rawType) {
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
                if (DataCell.class.isAssignableFrom((Class<?>)typeArgument)) {
                    if (typeArgument instanceof Class) {
                        return (Class<T>)typeArgument;
                    } else if (typeArgument instanceof ParameterizedType) {
                        return (Class<T>)((ParameterizedType) typeArgument).getRawType();
                    }
                }
            }
        }

        try {
            Class<T> c = (Class<T>)getClass().getMethod("deserialize", DataCellDataInput.class).getGenericReturnType();
            if (!DataCell.class.isAssignableFrom(c) || ((c.getModifiers() & Modifier.ABSTRACT) != 0)) {
                NodeLogger.getLogger(getClass())
                    .coding(getClass().getName() + " does not use generics properly, the type of the created cells is '"
                        + c.getName() + "'. Please fix your implementation by specifying a non-abstract data cell type "
                        + "in the implemented DataCellSerializer interface.");
                return (Class<T>)DataCell.class;
            } else {
                return c;
            }
        } catch (NoSuchMethodException ex) {
            // this is not possible
            throw new AssertionError("Someone removed the 'deserialize' method from this interface");
        }
    }
}
