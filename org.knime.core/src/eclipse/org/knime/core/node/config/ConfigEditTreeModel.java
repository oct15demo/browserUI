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
 *   Mar 29, 2008 (wiswedel): created
 */
package org.knime.core.node.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.config.base.AbstractConfigEntry;
import org.knime.core.node.config.base.ConfigBase;
import org.knime.core.node.config.base.ConfigBaseRO;
import org.knime.core.node.config.base.ConfigBooleanEntry;
import org.knime.core.node.config.base.ConfigByteEntry;
import org.knime.core.node.config.base.ConfigCharEntry;
import org.knime.core.node.config.base.ConfigDoubleEntry;
import org.knime.core.node.config.base.ConfigEntries;
import org.knime.core.node.config.base.ConfigFloatEntry;
import org.knime.core.node.config.base.ConfigIntEntry;
import org.knime.core.node.config.base.ConfigLongEntry;
import org.knime.core.node.config.base.ConfigPasswordEntry;
import org.knime.core.node.config.base.ConfigShortEntry;
import org.knime.core.node.config.base.ConfigStringEntry;
import org.knime.core.node.util.ConvenienceMethods;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.FlowVariable.Type;

/**
 * Config editor that keeps a mask of variables to overwrite existing settings.
 * This class is used to modify node settings with values assigned from
 * flow variables. It also keeps a list of &quot;exposed variables&quot;, that
 * is each individual setting can be exported as a new variable.
 * <p>This class is not meant to be used anywhere else than in the KNIME
 * framework classes.
 * @author Bernd Wiswedel, University of Konstanz
 */
public final class ConfigEditTreeModel extends DefaultTreeModel {

    private final CopyOnWriteArrayList<ConfigEditTreeEventListener> m_listeners;

    /** Factory method that parses the settings tree and constructs a new
     * object of this class. It will use the mask as given by the second
     * argument (which may be null, however).
     * @param settingsTree The original settings object.
     * @param variableTree The variables mask.
     * @return a new object of this class.
     * @throws InvalidSettingsException If setting can't be parsed
     * @noreference This method is not intended to be referenced by clients.
     */
    public static ConfigEditTreeModel create(final ConfigBase settingsTree,
            final ConfigBaseRO variableTree) throws InvalidSettingsException {
        ConfigEditTreeModel result = create(settingsTree);
        result.getRoot().readVariablesFrom(variableTree, false);
        return result;
    }

    /** Parses a settings tree and creates an empty mask
     * (for later modification).
     * @param settingsTree to be parsed.
     * @return a new object of this class.
     */
    public static ConfigEditTreeModel create(final ConfigBase settingsTree) {
        ConfigEditTreeNode rootNode = new ConfigEditTreeNode(settingsTree);
        recursiveAdd(rootNode, settingsTree);
        ConfigEditTreeModel result = new ConfigEditTreeModel(rootNode);
        rootNode.setTreeModel(result); // allows event propagation
        return result;
    }

    /** Recursive construction of tree. */
    private static void recursiveAdd(final ConfigEditTreeNode treeNode,
            final ConfigBase configValue) {
        assert (configValue == treeNode.getUserObject().m_configEntry);
        for (String s : configValue.keySet()) {
            AbstractConfigEntry childValue = configValue.getEntry(s);
            ConfigEditTreeNode childTreeNode =
                new ConfigEditTreeNode(childValue);
            if (childValue.getType().equals(ConfigEntries.config)) {
                recursiveAdd(childTreeNode, (ConfigBase)childValue);
            }
            treeNode.add(childTreeNode);
        }
    }

    /**
     * Determines whether a flow variable at hand (represented by its
     * actual type) can be converted into a desired type. In short: string
     * accepts also double and integer, double accepts integer, integer only
     * accepts integer.
     *
     * @param desiredType The type that is requested.
     * @param actualType The actual type.
     * @return If a flow variable of type <code>actualType</code> can be
     * used to represent a flow variable of type <code>desiredType</code>.
     */
    public static boolean doesTypeAccept(final Type desiredType,
            final Type actualType) {
        if (desiredType == null || actualType == null) {
            throw new NullPointerException("Arguments must not be null");
        }
        switch (desiredType) {
        case STRING:
            return true;
        case DOUBLE:
            return Type.DOUBLE.equals(actualType)
            || Type.INTEGER.equals(actualType);
        case INTEGER:
            return Type.INTEGER.equals(actualType);
        case CREDENTIALS:
            return false;
        default:
            assert false : "unknown type: " + desiredType;
            return false;
        }
    }

    /** @param rootNode root node. */
    private ConfigEditTreeModel(final ConfigEditTreeNode rootNode) {
        super(rootNode);
        m_listeners = new CopyOnWriteArrayList<ConfigEditTreeEventListener>();
    }

    /** @return true if there is any mask (overwriting settings) below this
     * branch of the tree. */
    public boolean hasConfiguration() {
        return getRoot().hasConfiguration();
    }

    /** @return name of user parameters that are overwritten by a variable
     * (= combo boxes that have a variable selected). This is then
     * indicated in the status bar of the node dialog.
     */
    public Set<String> getVariableControlledParameters() {
        Set<String> result = new LinkedHashSet<String>();
        getRoot().addVariableControlledParameters(result);
        return result;
    }

    /** Write the mask to a config object (for storage in node settings object).
     * @param config To save to. */
    public void writeVariablesTo(final ConfigBase config) {
        // false = do not write "model" as root
        getRoot().writeVariablesTo(config, false);
    }

    /** Modifies the first argument to reflect the values of the mask
     * represented by this object.
     * @param settingsTree settings tree to modify (supposed to have
     * equivalent tree structure)
     * @param variables The has of variables-values to apply.
     * @return A list of exposed variables
     * @throws InvalidSettingsException If reading fails
     */
    public List<FlowVariable> overwriteSettings(final Config settingsTree,
            final Map<String, FlowVariable> variables)
        throws InvalidSettingsException {
        return getRoot().overwriteSettings(settingsTree, variables, false);
    }

    /** {@inheritDoc} */
    @Override
    public ConfigEditTreeNode getRoot() {
        return (ConfigEditTreeNode)super.getRoot();
    }

    /** Get the child tree node associated with the key path.
     * Returns null if there is no child.
     * @param keyPath The path the child.
     * @return the child (or a children's child) for the given key path.
     */
    public ConfigEditTreeNode findChildForKeyPath(final String[] keyPath) {
        ConfigEditTreeNode current = getRoot();
        for (String key : keyPath) {
            ConfigEditTreeNode newCurrent = null;
            for (Enumeration<?> e = current.children(); e.hasMoreElements();) {
                ConfigEditTreeNode c = (ConfigEditTreeNode)e.nextElement();
                if (c.getConfigEntry().getKey().equals(key)) {
                    newCurrent = c;
                    break;
                }
            }
            if (newCurrent == null) {
                return null;
            }
            current = newCurrent;
        }
        return current;
    }

    /** Updates this tree with the settings available in the argument list. This
     * becomes necessary if a dialog provides its "expert" settings also via
     * small button attached to different controls. This method ensures that the
     * tree and the button model are in sync.
     * @param variableModels The models that were registered at the dialog.
     */
    public void update(final Collection<FlowVariableModel> variableModels) {
        ConfigEditTreeNode rootNode = getRoot();
        for (FlowVariableModel model : variableModels) {
            rootNode.update(model);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getRoot().toString();
    }

    /** Adds new listener.
     * @param listener to be added.
     */
    public void addConfigEditTreeEventListener(
            final ConfigEditTreeEventListener listener) {
        m_listeners.add(listener);
    }

    /** Removes a registered listener.
     * @param listener to be removed.
     */
    public void removeConfigEditTreeEventListener(
            final ConfigEditTreeEventListener listener) {
        m_listeners.remove(listener);
    }

    /** Fire an event.
     * @param treePath The tree path that has changed.
     * @param useVariable The new variable, which overwrites the user settings.
     * @param exposeVariableName The variable name to expose.
     */
    void fireConfigEditTreeEvent(final String[] treePath,
            final String useVariable, final String exposeVariableName) {
        ConfigEditTreeEvent event = new ConfigEditTreeEvent(
                this, treePath, useVariable, exposeVariableName);
        for (ConfigEditTreeEventListener l : m_listeners) {
            l.configEditTreeChanged(event);
        }
    }

    /** Single Tree node implementation. */
    public static final class ConfigEditTreeNode
        extends DefaultMutableTreeNode {

        /** The tree model, which is null for all nodes accept for the root.
         * It is set after the tree nodes are constructed. Used to propagate
         * events. */
        private ConfigEditTreeModel m_treeModel;

        /** Constructs new tree node based on a representative config entry.
         * @param entry To wrap. */
        ConfigEditTreeNode(final AbstractConfigEntry entry) {
            super(new Wrapper(entry));
            setAllowsChildren(!getUserObject().isLeaf());
        }

        /**
         * @param treeModel the treeModel to set
         */
        void setTreeModel(final ConfigEditTreeModel treeModel) {
            m_treeModel = treeModel;
        }

        /** {@inheritDoc} */
        @Override
        public void setUserObject(final Object arg) {
            throw new IllegalStateException("Not intended to be called");
        }

        /** {@inheritDoc} */
        @Override
        public Wrapper getUserObject() {
            return (Wrapper)super.getUserObject();
        }

        /** {@inheritDoc} */
        @Override
        public ConfigEditTreeNode getRoot() {
            return (ConfigEditTreeNode)super.getRoot();
        }

        /** @return associated config entry. */
        public AbstractConfigEntry getConfigEntry() {
            return getUserObject().m_configEntry;
        }

        /** @param value the new variable to use. */
        public void setUseVariableName(final String value) {
            String newValue = value;
            if (value == null || value.length() == 0) {
                newValue = null;
            }
            if (!ConvenienceMethods.areEqual(
                    getUserObject().m_useVarName, newValue)) {
                getUserObject().m_useVarName = value;
                fireEvent();
            }
        }

        /** @return the new variable to use. */
        public String getUseVariableName() {
            return getUserObject().m_useVarName;
        }

        /** @param variableName The name of the variable, which represents this
         * node's value. */
        public void setExposeVariableName(final String variableName) {
            String newValue = variableName;
            if (variableName == null || variableName.length() == 0) {
                newValue = null;
            }
            if (!ConvenienceMethods.areEqual(getUserObject().m_exposeVarName,
                    newValue)) {
                getUserObject().m_exposeVarName = newValue;
                fireEvent();
            }
        }

        /** @return the exported variable name. */
        public String getExposeVariableName() {
            return getUserObject().m_exposeVarName;
        }

        private void fireEvent() {
            TreeNode[] path = getPath();
            String[] keyPath = new String[path.length - 1];
            for (int i = 0; i < keyPath.length; i++) {
                ConfigEditTreeNode node = (ConfigEditTreeNode)path[i + 1];
                keyPath[i] = node.getConfigEntry().getKey();
            }
            if (getRoot().m_treeModel != null) {
                getRoot().m_treeModel.fireConfigEditTreeEvent(
                        keyPath, getUseVariableName(), getExposeVariableName());
            }
        }

        /** Implementation of {@link ConfigEditTreeModel#hasConfiguration()}.
         * @return if mask exists in this node or any child node. */
        public boolean hasConfiguration() {
            if (getUserObject().isLeaf()) {
                return getUseVariableName() != null
                || getExposeVariableName() != null;
            }
            for (Enumeration<?> e = children(); e.hasMoreElements();) {
                ConfigEditTreeNode c = (ConfigEditTreeNode)e.nextElement();
                if (c.hasConfiguration()) {
                    return true;
                }
            }
            return false;
        }

        /** Implementation of
         * {@link ConfigEditTreeModel#getVariableControlledParameters()}.
         * @param toAdd List to add to. */
        public void addVariableControlledParameters(final Set<String> toAdd) {
            if (getUserObject().isLeaf()) {
                if (getUseVariableName() != null) {
                    toAdd.add(getUserObject().getKey());
                }
            } else {
                for (Enumeration<?> e = children(); e.hasMoreElements();) {
                    ConfigEditTreeNode c = (ConfigEditTreeNode)e.nextElement();
                    c.addVariableControlledParameters(toAdd);
                }
            }
        }

        /** Implements the functionality described in the
         * {@link ConfigEditTreeNode#update(FlowVariableModel)} method.
         * @param model The model that provides the update.
         */
        void update(final FlowVariableModel model) {
            Enumeration<?> e = children();
            int k = 0;
            while (e.hasMoreElements() && (k < model.getKeys().length)) {
                ConfigEditTreeNode c = (ConfigEditTreeNode)e.nextElement();
                if (c.getConfigEntry().getKey().equals(model.getKeys()[k])) {
                    if ((k + 1) == model.getKeys().length) {
                        // reached last entry of hierarchy: apply settings
                        c.setUseVariableName(model.getInputVariableName());
                        c.setExposeVariableName(model.getOutputVariableName());
                    } else {
                        // dive deeper into hierarchy of keys
                        k++;
                        e = c.children();
                    }
                }
            }
        }

        private static final String CFG_USED_VALUE = "used_variable";
        private static final String CFG_EXPOSED_VALUE = "exposed_variable";

        /** Persistence method to restore the tree. */
        private void readVariablesFrom(final ConfigBaseRO variableTree,
                final boolean readThisEntry) throws InvalidSettingsException {
            ConfigBaseRO subConfig;
            if (readThisEntry) {
                String key = getConfigEntry().getKey();
                if (!variableTree.containsKey(key)) {
                    return;
                }
                subConfig = variableTree.getConfigBase(key);
            } else {
                subConfig = variableTree;
            }
            if (getUserObject().isLeaf()) {
                setUseVariableName(subConfig.getString(CFG_USED_VALUE));
                setExposeVariableName(subConfig.getString(CFG_EXPOSED_VALUE));
            } else {
                for (Enumeration<?> e = children(); e.hasMoreElements();) {
                    ConfigEditTreeNode c = (ConfigEditTreeNode)e.nextElement();
                    c.readVariablesFrom(subConfig, true);
                }
            }
        }

        /** Persistence method to save the tree. */
        private void writeVariablesTo(final ConfigBase variableTree,
                final boolean writeThisEntry) {
            if (!hasConfiguration()) {
                return;
            }
            ConfigBase subConfig;
            if (writeThisEntry) {
                String key = getConfigEntry().getKey();
                subConfig = variableTree.addConfigBase(key);
            } else {
                subConfig = variableTree;
            }
            if (getUserObject().isLeaf()) {
                subConfig.addString(CFG_USED_VALUE, getUseVariableName());
                subConfig.addString(CFG_EXPOSED_VALUE, getExposeVariableName());
            } else {
                for (Enumeration<?> e = children(); e.hasMoreElements();) {
                    ConfigEditTreeNode c = (ConfigEditTreeNode)e.nextElement();
                    c.writeVariablesTo(subConfig, true);
                }
            }
        }

        /** Implementation of {@link ConfigEditTreeModel#overwriteSettings(
         * Config, Map)}, see above method description for details. */
        private List<FlowVariable> overwriteSettings(final Config counterpart,
                final Map<String, FlowVariable> variables,
                final boolean isCounterpartParent)
            throws InvalidSettingsException {
            if (!hasConfiguration()) {
                return Collections.emptyList();
            }
            List<FlowVariable> result = null;
            AbstractConfigEntry thisEntry = getConfigEntry();
            String key = thisEntry.getKey();
            AbstractConfigEntry original;
            if (isCounterpartParent) {
                original = counterpart.getEntry(key);
            } else {
                original = counterpart;
            }
            if (original == null) {
                throw new InvalidSettingsException(
                        "No matching element found for entry with key: " + key);
            }
            if (!original.getType().equals(thisEntry.getType())) {
                throw new InvalidSettingsException("Non matching config "
                        + "elements for key \"" + key + "\", "
                        + original.getType() + " vs. " + thisEntry.getType());
            }
            String varName = getUseVariableName();
            if (varName != null) {
                switch (original.getType()) {
                case xboolean:
                    String bool =
                        getStringVariable(varName, variables);
                    if (bool == null) {
                        throw new InvalidSettingsException("Value of \""
                                + varName + "\" is null");
                    }
                    bool = bool.toLowerCase();
                    if (bool.equals("true") || bool.equals("false")) {
                        counterpart.addBoolean(key, Boolean.parseBoolean(bool));
                    } else {
                        throw new InvalidSettingsException("Unable to parse \""
                                + bool + "\" (variable \"" + varName + "\")"
                                + " as boolean expression (settings "
                                + "parameter \"" + key + "\")");
                    }
                    break;
                case xchar:
                    String charS = getStringVariable(varName, variables);
                    if (charS != null && charS.length() == 1) {
                        counterpart.addChar(key, charS.charAt(0));
                    } else {
                        throw new InvalidSettingsException(
                                "Unable to parse \"" + charS + "\" (variable \""
                                + varName + "\") as char "
                                + "(settings parameter \"" + key + "\")");
                    }
                    break;
                case xtransientstring:
                    String transientS = getStringVariable(varName, variables);
                    counterpart.addTransientString(key, transientS);
                    break;
                case xstring:
                    String s = getStringVariable(varName, variables);
                    counterpart.addString(key, s);
                    break;
                case xlong:
                case xint:
                case xshort:
                case xbyte:
                    int value = getIntVariable(varName, variables);
                    int min, max;
                    switch (original.getType()) {
                    case xlong:
                        counterpart.addLong(key, value);
                        min = Integer.MIN_VALUE;
                        max = Integer.MAX_VALUE;
                        break;
                    case xint:
                        min = Integer.MIN_VALUE;
                        max = Integer.MAX_VALUE;
                        counterpart.addInt(key, value);
                        break;
                    case xshort:
                        min = Short.MIN_VALUE;
                        max = Short.MAX_VALUE;
                        counterpart.addShort(key, (short)value);
                        break;
                    case xbyte:
                        min = Byte.MIN_VALUE;
                        max = Byte.MAX_VALUE;
                        counterpart.addByte(key, (byte)value);
                        break;
                    default:
                        assert false : "Unreachable case";
                        min = Integer.MIN_VALUE;
                        max = Integer.MAX_VALUE;
                    }
                    if (value < min || value > max) {
                        throw new InvalidSettingsException(
                                "Value of variable \"" + varName
                                + "\" can't be cast to " + original.getType()
                                + "(settings parameter " + "\"" + key
                                + "\"), out of range: " + value);
                    }
                    break;
                case xfloat:
                case xdouble:
                    double doubleValue = getDoubleVariable(varName, variables);
                    if (original.getType().equals(ConfigEntries.xdouble)) {
                        counterpart.addDouble(key, doubleValue);
                    } else {
                        counterpart.addFloat(key, (float)doubleValue);
                    }
                    break;
                default:
                    assert false : "Unreachable case: " + original.getType();
                }
            }
            String newVar = getExposeVariableName();
            if (newVar != null) {
                assert isLeaf() && isCounterpartParent;
                AbstractConfigEntry newValue = counterpart.getEntry(key);
                FlowVariable exposed;
                switch (newValue.getType()) {
                case xboolean:
                    boolean b = ((ConfigBooleanEntry)newValue).getBoolean();
                    exposed = new FlowVariable(newVar, Boolean.toString(b));
                    break;
                case xstring:
                    String s = ((ConfigStringEntry)newValue).getString();
                    exposed = new FlowVariable(newVar, s);
                    break;
                case xchar:
                    char c = ((ConfigCharEntry)newValue).getChar();
                    exposed = new FlowVariable(newVar, Character.toString(c));
                    break;
                case xbyte:
                    byte by = ((ConfigByteEntry)newValue).getByte();
                    exposed = new FlowVariable(newVar, by);
                    break;
                case xshort:
                    short sh = ((ConfigShortEntry)newValue).getShort();
                    exposed = new FlowVariable(newVar, sh);
                    break;
                case xint:
                    int i = ((ConfigIntEntry)newValue).getInt();
                    exposed = new FlowVariable(newVar, i);
                    break;
                case xlong:
                    long l = ((ConfigLongEntry)newValue).getLong();
                    if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
                        throw new InvalidSettingsException(
                                "Can't export value \"" + l + "\" as "
                                + "variable \"" + newVar + "\", out of range");
                    }
                    exposed = new FlowVariable(newVar, (int)l);
                    break;
                case xfloat:
                    float f = ((ConfigFloatEntry)newValue).getFloat();
                    exposed = new FlowVariable(newVar, f);
                    break;
                case xdouble:
                    double d = ((ConfigDoubleEntry)newValue).getDouble();
                    exposed = new FlowVariable(newVar, d);
                    break;
                case xpassword:
                    String pass = ((ConfigPasswordEntry)newValue).getPassword();
                    exposed = new FlowVariable(newVar, pass);
                    break;
                default:
                    throw new InvalidSettingsException("Can't export "
                            + newValue.getType() + " as variable \""
                            + newVar + "\"");
                }
                result = new ArrayList<FlowVariable>();
                result.add(exposed);
            }

            if (!isLeaf()) {
                for (Enumeration<?> e = children(); e.hasMoreElements();) {
                    ConfigEditTreeNode c = (ConfigEditTreeNode)e.nextElement();
                    List<FlowVariable> r = c.overwriteSettings(
                            (Config)original, variables, true);
                    if (!r.isEmpty()) {
                        if (result == null) {
                            result = new ArrayList<FlowVariable>();
                        }
                        result.addAll(r);
                    }
                }
            }
            if (result == null) {
                result = Collections.emptyList();
            }
            return result;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            toString(b, "");
            return b.toString();
        }

        /** Recursion method to get a string representation of this tree.
         * @param b to append to
         * @param indent indentation.
         */
        public void toString(final StringBuilder b, final String indent) {
            boolean edited = getUseVariableName() != null
                || getExposeVariableName() != null;
            String thisEntry = getUserObject().toString() + (edited ? "*" : "");
            b.append(indent + thisEntry + "\n");
            for (Enumeration<?> e = children(); e.hasMoreElements();) {
                ConfigEditTreeNode c = (ConfigEditTreeNode)e.nextElement();
                c.toString(b, indent + "  ");
            }
        }

        /** Getter method throws excpetion of variable name does not exist
         * in map. */
        private static FlowVariable getVariable(final String varString,
                final Map<String, FlowVariable> variables)
            throws InvalidSettingsException {
            FlowVariable var = variables.get(varString);
            if (var == null) {
                throw new InvalidSettingsException(
                        "Unknown variable \"" + varString + "\"");
            }
            return var;
        }

        /** Getter method to get double value. */
        private static double getDoubleVariable(final String varString,
                final Map<String, FlowVariable> variables)
            throws InvalidSettingsException {
            FlowVariable v = getVariable(varString, variables);
            switch (v.getType()) {
            case DOUBLE:
                return v.getDoubleValue();
            case INTEGER:
                return v.getIntValue();
            default:
                throw new InvalidSettingsException("Can't evaluate variable \""
                        + varString + "\" as double expression, it is a "
                        + v.getType() + " (\"" + v + "\")");
            }
        }

        /** Getter method to get int value. */
        private static int getIntVariable(final String varString,
                final Map<String, FlowVariable> variables)
        throws InvalidSettingsException {
            FlowVariable v = getVariable(varString, variables);
            switch (v.getType()) {
            case INTEGER:
                return v.getIntValue();
            default:
                throw new InvalidSettingsException("Can't evaluate variable \""
                        + varString + "\" as integer expression, it's a "
                        + v.getType() + " (\"" + v + "\")");
            }
        }

        /** Getter method to get string value. */
        private static String getStringVariable(final String varString,
                final Map<String, FlowVariable> variables)
        throws InvalidSettingsException {
            FlowVariable v = getVariable(varString, variables);
            switch (v.getType()) {
            case INTEGER:
                return Integer.toString(v.getIntValue());
            case DOUBLE:
                return Double.toString(v.getDoubleValue());
            case STRING:
                return v.getStringValue();
            default:
                throw new InvalidSettingsException("Can't evaluate variable \""
                        + varString + "\" as string expression, it's a "
                        + v.getType() + " (\"" + v + "\")");
            }
        }

    }

    /** User object in tree node wrapping config entry, used variable name and
     * possibly a variable name for exporting the value as variable. */
    private static final class Wrapper {
        private final AbstractConfigEntry m_configEntry;
        private String m_useVarName;
        private String m_exposeVarName;

        /** @param entry Entry to wrap. */
        public Wrapper(final AbstractConfigEntry entry) {
            m_configEntry = entry;
        }

        /** @return true if this represents a leaf (not a config object) */
        boolean isLeaf() {
            return !(m_configEntry instanceof Config);
        }

        /** @return the key of this config entry. */
        String getKey() {
            return m_configEntry.getKey();
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return m_configEntry.toString();
        }
    }

}
