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
 */
package org.knime.core.node.port.viewproperty;

import javax.swing.JComponent;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.ModelContent;
import org.knime.core.node.config.Config;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.core.node.workflow.ModelContentOutPortView;

/**
 * <code>PortObject</code> implementation for {@link FilterDefinitionHandlerPortObject}
 * which are part of a <code>DataTableSpec</code>.
 *
 * @author Simon Schmid, KNIME.com, Konstanz, Germany
 * @since 3.3
 */
public class FilterDefinitionHandlerPortObject extends ViewPropertyPortObject {
    /**
     * @noreference This class is not intended to be referenced by clients.
     */
    public static final class Serializer extends AbstractSimplePortObjectSerializer<FilterDefinitionHandlerPortObject> {}

    /** Convenience access method for port type. */
    public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(FilterDefinitionHandlerPortObject.class);

    /** Type representing this port object as optional. */
    public static final PortType TYPE_OPTIONAL =
        PortTypeRegistry.getInstance().getPortType(FilterDefinitionHandlerPortObject.class, true);

    /** Public no arg constructor required by super class.
     * <p>
     * <b>This constructor should only be used by the framework.</b> */
    public FilterDefinitionHandlerPortObject() {
    }

    /** Constructor used to instantiate this object during a node's execute
     * method.
     * @param spec The accompanying spec
     * @param portSummary A summary returned in the {@link #getSummary()}
     * method.
     * @throws NullPointerException If spec argument is <code>null</code>.
     */
    public FilterDefinitionHandlerPortObject(
            final DataTableSpec spec, final String portSummary) {
        super(spec, portSummary);
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public JComponent[] getViews() {
        ModelContent model = new ModelContent("FilterDefinition");
        Config columnConfig = model.addConfig("Column");
        getSpec().forEach(col -> col.getFilterHandler().ifPresent(handler -> handler.save(columnConfig.addConfig(col.getName()))));
        return new JComponent[] {new ModelContentOutPortView(model)};
    }

}
