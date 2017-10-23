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
 *   Mar 15, 2007 (mb): created
 */
package org.knime.core.node.workflow;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.node.util.ConvenienceMethods;
import org.knime.core.node.workflow.CredentialsStore.CredentialsFlowVariableValue;

/**
 * FlowVariable holding local variables of basic types which can
 * be passed along connections in a workflow.
 *
 * @author M. Berthold, University of Konstanz
 */
public final class FlowVariable extends FlowObject {

    /** reserved prefix for global flow variables.
     * @deprecated Use {@link Scope#getPrefix()} of enum constant
     * {@link Scope#Global} instead. */
    @Deprecated
    public static final String GLOBAL_CONST_ID = "knime";

    /** Type of a variable, supports currently only scalars. */
    public static enum Type {
        /** double type. */
        DOUBLE,
        /** int type. */
        INTEGER,
        /** String type. */
        STRING,
        /** Credentials, currently filtered from {@link FlowObjectStack#getAvailableFlowVariables()}.
         * @since 3.1 */
        CREDENTIALS;
    }

    /** Scope of variable. */
    public static enum Scope {
        /** (VM-)Global constant, such as workspace location. */
        Global("knime"),
        /** Ordinary workflow or flow variable. */
        Flow(""),
        /** Node local flow variable, e.g. node drop location. */
        Local("knime.node");

        private final String m_prefix;

        /** Create scope with given prefix. */
        private Scope(final String prefix) {
            m_prefix = prefix;
        }

        /** @return the prefix */
        public String getPrefix() {
            return m_prefix;
        }

        /** Throws IllegalFlowObjectStackException if the name of the variable
         * is inconsistent for this scope.
         * @param name Name to test
         * @throws IllegalFlowObjectStackException If name is invalid
         */
        public void verifyName(final String name) {
            if (m_prefix.length() > 0 && !name.startsWith(m_prefix)) {
                throw new IllegalFlowObjectStackException(
                        "Invalid " + name()
                        + " variable, name must start with \""
                        +  m_prefix + "\": " + name);
            }
            switch (this) {
            case Flow:
                if (name.startsWith(Global.m_prefix)
                        || name.startsWith(Local.m_prefix)) {
                    throw new IllegalFlowObjectStackException(
                            "Invalid flow variable, invalid prefix: "
                            + name);
                }
                break;
            default:
                // ignore
            }
        }
    }

    private final Type m_type;
    private final Scope m_scope;
    private final String m_name;
    private String m_valueS = null;
    private double m_valueD = Double.NaN;
    private int m_valueI = 0;
    private CredentialsFlowVariableValue m_valueCredentials;


    private FlowVariable(final String name, final Type type,
            final Scope scope) {
        if (name == null || type == null) {
            throw new NullPointerException("Argument must not be null");
        }
        if (name.trim().isEmpty()) {
            throw new IllegalFlowObjectStackException("Invalid (empty) flow variable name");
        }
        scope.verifyName(name);
        m_name = name;
        m_type = type;
        m_scope = scope;
    }

    /** create new FlowVariable representing a string.
     *
     * @param name of the variable
     * @param valueS string value
     */
    public FlowVariable(final String name, final String valueS) {
        this(name, valueS, Scope.Flow);
    }

    /** create new FlowVariable representing a double.
     *
     * @param name of the variable
     * @param valueD double value
     */
    public FlowVariable(final String name, final double valueD) {
        this(name, valueD, Scope.Flow);
    }

    /** create new FlowVariable representing an integer.
     *
     * @param name of the variable
     * @param valueI int value
     */
    public FlowVariable(final String name, final int valueI) {
        this(name, valueI, Scope.Flow);
    }

    /** create new FlowVariable representing a string which can either
     * be a global workflow variable or a local one.
     *
     * @param name of the variable
     * @param valueS string value
     * @param scope Scope of variable
     */
    FlowVariable(final String name, final String valueS, final Scope scope) {
        this(name, Type.STRING, scope);
        m_valueS = valueS;
    }

    /** create new FlowVariable representing a double which can either
     * be a global workflow variable or a local one.
     *
     * @param name of the variable
     * @param valueD double value
     * @param scope Scope of variable
     */
    FlowVariable(final String name, final double valueD, final Scope scope) {
        this(name, Type.DOUBLE, scope);
        m_valueD = valueD;
    }

    /** create new FlowVariable representing an integer which can either
     * be a global workflow variable or a local one.
     *
     * @param name of the variable
     * @param valueI int value
     * @param scope Scope of variable
     */
    FlowVariable(final String name, final int valueI, final Scope scope) {
        this(name, Type.INTEGER, scope);
        m_valueI = valueI;
    }

    /** create new FlowVariable representing a credentials object. (Flow Scope)
     *
     * @param name of the variable
     * @param valueC credentials object (usually has the same name except for name uniquification in subnode).
     * @noreference This constructor is not intended to be referenced by clients.
     */
    public FlowVariable(final String name, final CredentialsFlowVariableValue valueC) {
        this(name, Type.CREDENTIALS, Scope.Flow);
        m_valueCredentials = CheckUtils.checkArgumentNotNull(valueC);
    }

    /**
     * @return name of variable.
     */
    public String getName() {
        return m_name;
    }

    /**
     * @return true if the variable is a global workflow variable.
     */
    public boolean isGlobalConstant() {
        return m_scope.equals(Scope.Global);
    }

    /** @return the scope */
    public Scope getScope() {
        return m_scope;
    }

    /** @return the type */
    public Type getType() {
        return m_type;
    }

    /**
     * @return get string value of the variable or null if it's not a string.
     */
    public String getStringValue() {
        if (m_type != Type.STRING) {
            return null;
        }
        return m_valueS;
    }

    /**
     * @return get double value of the variable or Double.NaN if it's not a
     * double.
     */
    public double getDoubleValue() {
        if (m_type != Type.DOUBLE) {
            return Double.NaN;
        }
        return m_valueD;
    }

    /**
     * @return get int value of the variable or 0 if it's not an integer.
     */
    public int getIntValue() {
        if (m_type != Type.INTEGER) {
            return 0;
        }
        return m_valueI;
    }

    /** Temporary workaround to represent credentials in flow variables. This is a framework method that is going
     * to change in future versions.
     * @return the valueCredentials or null.
     * @noreference This method is not intended to be referenced by clients.
     */
    CredentialsFlowVariableValue getCredentialsValue() {
        if (m_type != Type.CREDENTIALS) {
            return null;
        }
        return m_valueCredentials;
    }

    /**
     * @return value of the variable as string (independent of type).
     * @since 2.6
     */
    public String getValueAsString() {
        switch (m_type) {
        case DOUBLE: return Double.toString(m_valueD);
        case INTEGER: return Integer.toString(m_valueI);
        case STRING: return m_valueS;
        case CREDENTIALS: return "Credentials: " + m_valueCredentials.getName();
        }
        return "invalid type";
    }

    /** Saves this flow variable to a settings object. This method writes
     * directly into the argument object (no creating of intermediate child).
     * @param settings To save to.
     */
    void save(final NodeSettingsWO settings) {
        settings.addString("name", getName());
        settings.addString("class", getType().name());
        switch (getType()) {
        case INTEGER:
            settings.addInt("value", getIntValue());
            break;
        case DOUBLE:
            settings.addDouble("value", getDoubleValue());
            break;
        case STRING:
            settings.addString("value", getStringValue());
            break;
        case CREDENTIALS:
            NodeSettingsWO subSettings = settings.addNodeSettings("value");
            m_valueCredentials.save(subSettings);
            break;
        default:
            assert false : "Unknown variable type: " + getType();
        }
    }

    /**
     * Read a flow variable from a settings object. This is the counterpart
     * to {@link #save(NodeSettingsWO)}.
     * @param sub To load from.
     * @return A new {@link FlowVariable} read from the settings object.
     * @throws InvalidSettingsException If that fails for any reason.
     */
    static FlowVariable load(final NodeSettingsRO sub)
        throws InvalidSettingsException {
        String name = sub.getString("name");
        String typeS = sub.getString("class");
        if (typeS == null || name == null) {
            throw new InvalidSettingsException("name or type is null");
        }
        Type varType;
        try {
            varType = Type.valueOf(typeS);
        } catch (final IllegalArgumentException e) {
            throw new InvalidSettingsException("invalid type " + typeS);
        }
        FlowVariable v;
        switch (varType) {
        case DOUBLE:
            v = new FlowVariable(name, sub.getDouble("value"));
            break;
        case INTEGER:
            v = new FlowVariable(name, sub.getInt("value"));
            break;
        case STRING:
            v = new FlowVariable(name, sub.getString("value"));
            break;
        case CREDENTIALS:
            NodeSettingsRO subSettings = sub.getNodeSettings("value");
            CredentialsFlowVariableValue credentialsValue = CredentialsFlowVariableValue.load(subSettings);
            v = new FlowVariable(name, credentialsValue);
            break;
        default:
            throw new InvalidSettingsException("Unknown type " + varType);
        }
        return v;
    }

    /** Clones this object, setting a new name (used in subnode, puts a unique identifier).
     * @param name new identifier, not null.
     * @return a 'clone' of this with a new name.
     * @noreference This method is not intended to be referenced by clients.
     */
    public FlowVariable withNewName(final String name) {
        FlowVariable clone = new FlowVariable(name, m_type, m_scope);
        clone.m_valueD = m_valueD;
        clone.m_valueI = m_valueI;
        clone.m_valueS = m_valueS;
        clone.m_valueCredentials = m_valueCredentials;
        return clone;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        String value;
        switch (m_type) {
        case DOUBLE: value = Double.toString(m_valueD); break;
        case INTEGER: value = Integer.toString(m_valueI); break;
        case STRING: value = m_valueS; break;
        case CREDENTIALS: value = m_valueCredentials.toString(); break;
        default: throw new InternalError("m_type must not be null");
        }
        return m_name + "\" (" + m_type + ": " + value + ")";
    }

    /** {@inheritDoc}
     * Only compares type and value, not the owner and not the scope. */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FlowVariable)) {
            return false;
        }
        FlowVariable v = (FlowVariable)obj;
        if (!v.getType().equals(getType())) {
            return false;
        }
        if (!v.getName().equals(getName())) {
            return false;
        }
        boolean valueEqual;
        switch (getType()) {
        case DOUBLE:
            valueEqual = Double.compare(v.getDoubleValue(), getDoubleValue()) == 0;
            break;
        case INTEGER:
            valueEqual = v.getIntValue() == getIntValue();
            break;
        case STRING:
            valueEqual = ConvenienceMethods.areEqual(
                    v.getStringValue(), getStringValue());
            break;
        case CREDENTIALS:
            valueEqual = ConvenienceMethods.areEqual(v.getCredentialsValue(), getCredentialsValue());
            break;
        default:
            throw new IllegalStateException("Unsupported variable type "
                    + getType() + " (not implemented)");
        }
        return valueEqual;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return getType().hashCode() ^ getName().hashCode();
    }

}
