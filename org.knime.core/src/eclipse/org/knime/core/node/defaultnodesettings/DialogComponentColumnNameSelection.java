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
 *   21.09.2005 (mb): created
 *   2006-05-26 (tm): reviewed
 */
package org.knime.core.node.defaultnodesettings;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.ColumnFilter;
import org.knime.core.node.util.ColumnSelectionPanel;
import org.knime.core.node.util.DataValueColumnFilter;

/**
 * Provides a standard component for a dialog that allows to select a column in
 * a given {@link org.knime.core.data.DataTableSpec}. Provides label and list
 * (possibly filtered by a given {@link org.knime.core.data.DataCell} type) as
 * well as functionality to load/store into a settings model.
 * The column name selection list will provide a RowID option if the provided
 * settings model object is an instance of {@link SettingsModelColumnName} which
 * provides the additional method <code>useRowID</code> to check if the
 * RowID was selected.
 *
 * @author M. Berthold, University of Konstanz
 */
public class DialogComponentColumnNameSelection extends DialogComponent {

    /** Contains all column names matching the given given filter class. */
    private final ColumnSelectionPanel m_chooser;

    private final JLabel m_label;

    private final int m_specIndex;

    private ColumnFilter m_columnFilter;

    private final boolean m_isRequired;

    /**
     * Constructor that puts label and combobox into the panel. The dialog will
     * not open until the incoming table spec contains a column compatible to
     * one of the specified {@link DataValue} classes.
     *
     * @param model the model holding the value of this component. If the model
     * is an instance of {@link SettingsModelColumnName} a RowID option is
     * added to the select list.
     * @param label label for dialog in front of checkbox
     * @param specIndex index of (input) port listing available columns
     * @param columnFilter {@link ColumnFilter}. The combo box
     *            will allow to select only columns compatible with the
     *            column filter. All other columns will be ignored.
     */
    public DialogComponentColumnNameSelection(final SettingsModelString model,
            final String label, final int specIndex,
            final ColumnFilter columnFilter) {
        this(model, label, specIndex, true, columnFilter);
    }

    /**
     * Constructor that puts label and combobox into the panel. The dialog will
     * not open until the incoming table spec contains a column compatible to
     * one of the specified {@link DataValue} classes.
     *
     * @param model the model holding the value of this component. If the model
     * is an instance of {@link SettingsModelColumnName} a RowID option is
     * added to the select list.
     * @param label label for dialog in front of checkbox
     * @param specIndex index of (input) port listing available columns
     * @param classFilter which classes are available for selection
     */
    public DialogComponentColumnNameSelection(final SettingsModelString model,
            final String label, final int specIndex,
            final Class<? extends DataValue>... classFilter) {
        this(model, label, specIndex, true, classFilter);
    }

    /**
     * Constructor that puts label and combobox into the panel.
     *
     * @param model the model holding the value of this component. If the model
     * is an instance of {@link SettingsModelColumnName} a RowID option is
     * added to the select list.
     * @param label label for dialog in front of checkbox
     * @param specIndex index of (input) port listing available columns
     * @param isRequired true, if the component should throw an exception in
     *            case of no available compatible column, false otherwise.
     * @param classFilter which classes are available for selection
     */
    public DialogComponentColumnNameSelection(final SettingsModelString model,
            final String label, final int specIndex, final boolean isRequired,
            final Class<? extends DataValue>... classFilter) {
        this(model, label, specIndex, isRequired,
                new DataValueColumnFilter(classFilter));
    }

    /**
     * Constructor that puts label and combobox into the panel.
     *
     * @param model the model holding the value of this component. If the model
     * is an instance of {@link SettingsModelColumnName} a RowID option is
     * added to the select list.
     * @param label label for dialog in front of checkbox
     * @param specIndex index of (input) port listing available columns
     * @param isRequired true, if the component should throw an exception in
     *            case of no available compatible column, false otherwise.
     * @param addNoneCol true, if a none option should be added to the column
     * list
     * @param classFilter which classes are available for selection
     */
    public DialogComponentColumnNameSelection(final SettingsModelString model,
            final String label, final int specIndex, final boolean isRequired,
            final boolean addNoneCol,
            final Class<? extends DataValue>... classFilter) {
        this(model, label, specIndex, isRequired, addNoneCol,
                new DataValueColumnFilter(classFilter));
    }

    /**
     * Constructor that puts label and combobox into the panel.
     *
     * @param model the model holding the value of this component. If the model
     * is an instance of {@link SettingsModelColumnName} a RowID option is
     * added to the select list.
     * @param label label for dialog in front of checkbox
     * @param specIndex index of (input) port listing available columns
     * @param isRequired true, if the component should throw an exception in
     *            case of no available compatible column, false otherwise.
     * @param columnFilter {@link ColumnFilter}. The combo box
     *            will allow to select only columns compatible with the
     *            column filter. All other columns will be ignored.
     */
    public DialogComponentColumnNameSelection(final SettingsModelString model,
            final String label, final int specIndex, final boolean isRequired,
            final ColumnFilter columnFilter) {
        this(model, label, specIndex, isRequired, false, columnFilter);
    }

    /**
     * Constructor that puts label and combobox into the panel.
     *
     * @param model the model holding the value of this component. If the model
     * is an instance of {@link SettingsModelColumnName} a RowID option is
     * added to the select list.
     * @param label label for dialog in front of checkbox
     * @param specIndex index of (input) port listing available columns
     * @param isRequired true, if the component should throw an exception in
     *            case of no available compatible column, false otherwise.
     * @param addNoneCol true, if a none option should be added to the column
     * list
     * @param columnFilter {@link ColumnFilter}. The combo box
     *            will allow to select only columns compatible with the
     *            column filter. All other columns will be ignored.
     */
    public DialogComponentColumnNameSelection(final SettingsModelString model,
            final String label, final int specIndex, final boolean isRequired,
            final boolean addNoneCol, final ColumnFilter columnFilter) {
        super(model);
        m_label = new JLabel(label);
        getComponentPanel().add(m_label);
        m_isRequired = isRequired;
        m_columnFilter = columnFilter;
        final boolean addRowID = (model instanceof SettingsModelColumnName);
        m_chooser = new ColumnSelectionPanel((Border)null, m_columnFilter,
                addNoneCol, addRowID);
        m_chooser.setRequired(m_isRequired);
        getComponentPanel().add(m_chooser);
        m_specIndex = specIndex;

        // we are not listening to the selection panel and not updating the
        // model on a selection change. We set the value in the model right
        // before save

        m_chooser.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    // a new item got selected, update the model
                    updateModel();
                }
            }
        });

        // update the selection panel, when the model was changed
        getModel().prependChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                // if only the value in the model changes we only set the
                // selected column in the component.
                if ((getModel() instanceof SettingsModelColumnName)
                        && ((SettingsModelColumnName)getModel()).useRowID()) {
                    m_chooser.setRowIDSelected();
                } else {
                    m_chooser.setSelectedColumn(
                            ((SettingsModelString)getModel()).getStringValue());
                }
//              update the enable status
                setEnabledComponents(getModel().isEnabled());
            }
        });
        //call this method to be in sync with the settings model
        updateComponent();
    }

    /** Returns the {@link DataColumnSpec} of the currently selected item.
     * This method delegates to
     * {@link ColumnSelectionPanel#getSelectedColumnAsSpec()}.
     * @return The currently selected item as {@link DataColumnSpec} or null
     * if none is selected (the list is empty) or the RowID should be used
     * (check return value of the useRowID method).
     */
    public final DataColumnSpec getSelectedAsSpec() {
        return m_chooser.getSelectedColumnAsSpec();
    }

    /** Returns the name of the currently selected item. This method delegates
     * to {@link ColumnSelectionPanel#getSelectedColumn()}.
     * @return The name of the currently selected item or null if none is
     * selected (the list is empty) or the RowID should be used
     * (check return value of the useRowID method).
     */
    public final String getSelected() {
        return m_chooser.getSelectedColumn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateComponent() {
        String classCol = ((SettingsModelString)getModel()).getStringValue();
        if ((classCol == null) || (classCol.length() == 0)) {
            classCol = "** Unknown column **";
        }
        try {
            final DataTableSpec spec =
                (DataTableSpec)getLastTableSpec(m_specIndex);
            boolean useRowID = getModel() instanceof SettingsModelColumnName
                && ((SettingsModelColumnName)getModel()).useRowID();
            if (spec != null) {
                m_chooser.update(spec, classCol, useRowID);
            } else if (!m_isRequired) {
                m_chooser.update(new DataTableSpec(), classCol, useRowID);
            }
        } catch (final NotConfigurableException e1) {
            // we check the correctness of the table spec before, so
            // this exception shouldn't fly.
            assert false;
        }

        // update the enable status
        setEnabledComponents(getModel().isEnabled());
    }

    /**
     * Transfers the selected value from the component into the settings model.
     */
    private void updateModel() {
        if (getModel() instanceof SettingsModelColumnName) {
            ((SettingsModelColumnName)getModel()).setSelection(
                    m_chooser.getSelectedColumn(), m_chooser.rowIDSelected());
        } else {
            ((SettingsModelString)getModel()).setStringValue(
                    m_chooser.getSelectedColumn());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkConfigurabilityBeforeLoad(final PortObjectSpec[] specs)
            throws NotConfigurableException {
        /*
         * this is a bit of code duplication: if the selection panel is set to
         * require at least one selectable column in the specs, it will fail
         * during update if no such column is present. We check this here, to
         * avoid loading if no column is selectable, so that the update with a
         * new value (following this method call) will not fail.
         */
        if ((specs == null) || (specs.length <= m_specIndex)) {
            throw new NotConfigurableException("Need input table spec to "
                    + "configure dialog. Configure or execute predecessor "
                    + "nodes.");
        }
        DataTableSpec spec;
        try {
            spec = (DataTableSpec)specs[m_specIndex];
        } catch (final ClassCastException cce) {
            throw new NotConfigurableException("Wrong type of PortObject for"
                    + " ColumnNameSelection, expecting DataTableSpec!");
        }
        //if it's not required we don't need to check if at least one column
        //matches the criteria
        if (!m_isRequired) {
            return;
        }
        // spec is null if the port is not connected
        if (spec == null) {
            throw new NotConfigurableException("Need input table spec to "
                    + "configure dialog. Configure or execute predecessor "
                    + "nodes.");
        }
        // now check if at least one column is compatible to the column filter
        for (final DataColumnSpec col : spec) {
            if (m_columnFilter.includeColumn(col)) {
                // we found one column we are compatible to - cool!
                return;
            }
        }
        //no column compatible to the current filter
        throw new NotConfigurableException(m_columnFilter.allFilteredMsg());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettingsBeforeSave()
            throws InvalidSettingsException {
        // just in case we didn't get notified about the last selection ...
        updateModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setEnabledComponents(final boolean enabled) {
        m_chooser.setEnabled(enabled);
        m_label.setEnabled(enabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setToolTipText(final String text) {
        m_chooser.setToolTipText(text);
        m_label.setToolTipText(text);
    }

    /**
     * Sets the new column filter and updates the component.
     * @param filter {@link ColumnFilter} to use
     * @throws NotConfigurableException If the spec does not contain at least
     * one compatible type.
     * @since 2.10
     */
    public void setColumnFilter(final ColumnFilter filter) throws NotConfigurableException {
        m_columnFilter = filter;
        m_chooser.setColumnFilter(filter);
    }
}
