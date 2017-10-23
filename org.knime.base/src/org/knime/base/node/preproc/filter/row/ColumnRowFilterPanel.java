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
 *   18.07.2005 (ohl): created
 */
package org.knime.base.node.preproc.filter.row;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.knime.base.node.preproc.filter.row.rowfilter.AttrValueRowFilter;
import org.knime.base.node.preproc.filter.row.rowfilter.ColValFilterOldObsolete;
import org.knime.base.node.preproc.filter.row.rowfilter.IRowFilter;
import org.knime.base.node.preproc.filter.row.rowfilter.MissingValueRowFilter;
import org.knime.base.node.preproc.filter.row.rowfilter.RangeRowFilter;
import org.knime.base.node.preproc.filter.row.rowfilter.StringCompareRowFilter;
import org.knime.base.util.WildcardMatcher;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.DataValueComparator;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.LongValue;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.LongCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.FlowVariableModelButton;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.ColumnSelectionComboxBox;
import org.knime.core.node.workflow.FlowVariable;

/**
 *
 * @author Peter Ohl, University of Konstanz
 */
public class ColumnRowFilterPanel extends RowFilterPanel implements
        ItemListener {

    /** object version for serialization. */
    static final long serialVersionUID = 1;

    private ColumnSelectionComboxBox m_colCombo;

    private JCheckBox m_deepFiltering;

    private JRadioButton m_useRange;

    private JRadioButton m_useRegExpr;
    private FlowVariableModelButton m_regExprVarButton;

    private ButtonGroup m_radios;

    private JLabel m_lowerLabel;

    private JTextField m_lowerBound;

    private JLabel m_upperLabel;

    private JTextField m_upperBound;

    private JLabel m_regLabel;

    private JComboBox m_regExpr;

    private JCheckBox m_caseSensitive;

    private JCheckBox m_hasWildCards;

    private JCheckBox m_isRegExpr;

    private JTextArea m_errText;

    private DataTableSpec m_tSpec;

    private JRadioButton m_useMissValue;

    /**
     * Craetes a new panel for column content filter settings.
     * @param parentPane the underlying dialog pane
     * @param tSpec table spec containing column specs to select from
     * @throws NotConfigurableException it tspec is <code>null</code> or emtpy
     */
    public ColumnRowFilterPanel(final RowFilterNodeDialogPane parentPane,
            final DataTableSpec tSpec)
            throws NotConfigurableException {

        super(400, 350);

        if ((tSpec == null) || (tSpec.getNumColumns() <= 0)) {
            throw new IllegalArgumentException("Don't instantiate with "
                    + "useless table spec");
        }
        m_tSpec = tSpec;

        instantiateComponents(parentPane, m_tSpec);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory
            .createEtchedBorder(), "Column value matching"));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 2, 2, 2);


        panel.add(new JLabel("Column to test:   "), c);
        c.gridx = 1;
        panel.add(m_colCombo, c);
        c.gridx = 0;
        c.gridy++;

        c.gridwidth = 2;
        panel.add(m_deepFiltering, c);


        c.gridy++;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        JPanel matchPanel = new JPanel(new GridBagLayout());
        panel.add(matchPanel, c);

        matchPanel
            .setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Matching criteria"));

        // pattern matching
        c.gridwidth = 4;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.weighty = 0;

        matchPanel.add(m_useRegExpr, c);

        c.gridy++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(2, 22, 2, 0);
        matchPanel.add(m_regExpr, c);

        c.gridx = 4;
        c.gridwidth = 1;
        c.weightx = 0;
        c.insets = new Insets(2, 2, 2, 2);
        c.fill = GridBagConstraints.NONE;
        matchPanel.add(m_regExprVarButton, c);


        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.insets = new Insets(2, 22, 2, 0);
        matchPanel.add(m_caseSensitive, c);

        c.gridx = 2;
        c.insets = new Insets(2, 2, 2, 2);
        matchPanel.add(m_hasWildCards, c);

        c.gridy++;
        matchPanel.add(m_isRegExpr, c);

        // range checking
        c.gridwidth = 4;
        c.gridx = 0;
        c.gridy++;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.weighty = 0;
        c.insets = new Insets(20, 2, 2, 2);
        matchPanel.add(m_useRange, c);

        c.gridy++;
        c.gridwidth = 1;
        c.insets = new Insets(2, 22, 2, 10);
        c.anchor = GridBagConstraints.EAST;
        matchPanel.add(m_lowerLabel, c);

        c.gridx = 1;
        c.insets = new Insets(2, 2, 2, 2);
        c.gridwidth = 3;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        matchPanel.add(m_lowerBound, c);


        c.gridx = 0;
        c.gridy++;
        c.insets = new Insets(2, 22, 2, 10);
        c.gridwidth = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        matchPanel.add(m_upperLabel, c);

        c.gridx = 1;
        c.insets = new Insets(2, 2, 2, 2);
        c.gridwidth = 3;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        matchPanel.add(m_upperBound, c);


        // missing values
        c.gridwidth = 4;
        c.gridx = 0;
        c.gridy++;
        c.insets = new Insets(20, 2, 2, 2);
        matchPanel.add(m_useMissValue, c);


        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        c.gridwidth = 5;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        matchPanel.add(m_errText, c);

        add(panel);
    }

    @SuppressWarnings("unchecked")
    private void instantiateComponents(final RowFilterNodeDialogPane parentPane,
            final DataTableSpec tSpec)
            throws NotConfigurableException {
        /* instantiate the col idx selector, depending on the table spec */
        assert ((tSpec != null) && (tSpec.getNumColumns() > 0));
        Vector<String> colNames = new Vector<String>();
        for (int c = 0; c < tSpec.getNumColumns(); c++) {
            colNames.add(tSpec.getColumnSpec(c).getName());
        }
        m_colCombo =
                new ColumnSelectionComboxBox((Border)null, DataValue.class);
        m_colCombo.update(tSpec, null);
        m_colCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                selectedColChanged();
            }
        });
        m_deepFiltering = new JCheckBox("filter based on collection elements");
        /* the selectors for what kind of checking will be done */
        m_useRange = new JRadioButton("use range checking");
        m_useRegExpr = new JRadioButton("use pattern matching");
        m_useMissValue = new JRadioButton("only missing values match");
        m_useRange.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                radiosChanged();
            }
        });
        m_useRegExpr.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                radiosChanged();
            }
        });
        m_useMissValue.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                radiosChanged();
            }
        });
        m_radios = new ButtonGroup();
        m_radios.add(m_useRange);
        m_radios.add(m_useRegExpr);
        m_radios.add(m_useMissValue);
        /* the bound edit fields */
        m_lowerLabel = new JLabel("lower bound:");
        m_lowerLabel.setToolTipText(
                "Smallest value of the range to be filtered.");
        m_lowerBound = new JTextField();
        m_upperLabel = new JLabel("upper bound:");
        m_upperLabel.setToolTipText(
                "Largest value of the range to be filtered.");
        m_upperBound = new JTextField();
        m_lowerBound.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent e) {
                boundsChanged();
            }
            @Override
            public void removeUpdate(final DocumentEvent e) {
                boundsChanged();
            }
            @Override
            public void changedUpdate(final DocumentEvent e) {
                boundsChanged();
            }
        });
        m_upperBound.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent e) {
                boundsChanged();
            }
            @Override
            public void removeUpdate(final DocumentEvent e) {
                boundsChanged();
            }
            @Override
            public void changedUpdate(final DocumentEvent e) {
                boundsChanged();
            }
        });
        /* the regular expression stuff */
        m_regLabel = new JLabel("pattern:");
        // it's important that the column selection is created before!
        m_regExpr = new JComboBox(getPossibleValuesOfSelectedColumn());
        m_regExpr.setEditable(true);
        m_regExpr.setSelectedItem("");
        m_regExpr.setMinimumSize(new Dimension(50, m_regExpr.getPreferredSize().height));
        // m_regExpr.setPreferredSize(new Dimension(200, 50));
        m_regExpr.setMaximumSize(new Dimension(300, 50));
        JTextField ed = (JTextField)m_regExpr.getEditor().getEditorComponent();
        ed.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent e) {
                regExprChanged();
            }
            @Override
            public void removeUpdate(final DocumentEvent e) {
                regExprChanged();
            }
            @Override
            public void changedUpdate(final DocumentEvent e) {
                regExprChanged();
            }
        });
        m_regExpr.addItemListener(this);
        /* add flow variable button for the pattern/regexpr */
        FlowVariableModel fvm = parentPane.createFlowVariableModel(
                new String[] {RowFilterNodeModel.CFGFILTER,
                        StringCompareRowFilter.CFGKEY_PATTERN},
                FlowVariable.Type.STRING);
        fvm.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent evt) {
                FlowVariableModel fvm =
                    (FlowVariableModel)(evt.getSource());
                m_regExpr.setEnabled(!fvm.isVariableReplacementEnabled());
                if (fvm.isVariableReplacementEnabled()
                        && m_regExpr.getSelectedItem().equals("")) {
                    // TODO: replace with more meaningful default - empty
                    // pattern are rejected by dialog.
                    m_regExpr.setSelectedItem(fvm.getInputVariableName());
                }
            }
        });
        m_regExprVarButton = new FlowVariableModelButton(fvm);
        m_caseSensitive = new JCheckBox("case sensitive match");
        m_isRegExpr = new JCheckBox("regular expression");
        m_hasWildCards = new JCheckBox("contains wild cards");
        m_hasWildCards.setToolTipText("insert '?' or '*' to match any one "
                + "character or any sequence (including none) of characters.");
        m_isRegExpr.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                wildRegExprChanged(e);
                // also trigger regular expression recompile
                regExprChanged();
            }
        });
        m_hasWildCards.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                wildRegExprChanged(e);
                // also trigger regular expression recompile
                regExprChanged();
            }
        });
        /* and a label to display errors/warnings */
        m_errText = new JTextArea();
        m_errText.setEditable(false);
        m_errText.setLineWrap(true);
        m_errText.setWrapStyleWord(true);
        m_errText.setBackground(getBackground());
        m_errText.setFont(new Font(m_errText.getFont().getName(), Font.BOLD, m_errText.getFont().getSize()));
        m_errText.setMinimumSize(new Dimension(350, 50));
        m_errText.setMaximumSize(new Dimension(350, 100));
        m_errText.setForeground(Color.RED);
        /* set the default values */
        m_useRegExpr.setSelected(true);
    }

    /**
     * The item change listener for the regular expression box. Needs to be
     * re-registered from time to time.
     *
     * @param e the item event.
     */
    @Override
    public void itemStateChanged(final ItemEvent e) {
        if (e.getSource() == m_regExpr) {
            if (m_regExpr.getSelectedIndex() >= 0) {
                // if user selected a possible value, it must match exactly
                m_caseSensitive.setSelected(true);
                m_isRegExpr.setSelected(false);
                m_hasWildCards.setSelected(false);
                regExprChanged();
            }
        }
    }

    /**
     * For the selected column (from the combobox) it get the possible values,
     * and returns a vector with the string representations for them. If no
     * possible values are specified, an empty vector is returned.
     *
     * @return it returns a vector with the string representations of the
     *         currently selected column. If that's not possible (because no
     *         column is selected, or the selected one has no possible values)
     *         it returns an empty vector.
     */
    protected Vector<String> getPossibleValuesOfSelectedColumn() {
        Vector<String> result = new Vector<String>();
        String col = m_colCombo.getSelectedColumn();
        if (col == null) {
            return result;
        }
        DataColumnSpec spec = m_tSpec.getColumnSpec(col);
        if (spec == null) {
            return result;
        }
        if (!spec.getDomain().hasValues()) {
            return result;
        }
        // convert all data cells to string representations
        for (DataCell v : spec.getDomain().getValues()) {
            result.add(v.toString());
        }
        Collections.sort(result);
        return result;
    }

    /**
     * Called when the 'is regular expression' or 'has wildcards' checkbox was
     * clicked. Ensures only one of them is checked.
     *
     * @param e the event flying
     */
    protected void wildRegExprChanged(final ItemEvent e) {
        if ((e.getSource() == m_isRegExpr) && m_isRegExpr.isSelected()) {
            m_hasWildCards.setSelected(false);
        }
        if ((e.getSource() == m_hasWildCards) && m_hasWildCards.isSelected()) {
            m_isRegExpr.setSelected(false);
        }
    }

    /**
     * Called when user pushes the buttons.
     */
    protected void radiosChanged() {

        m_upperLabel.setEnabled(m_useRange.isSelected());
        m_upperBound.setEnabled(m_useRange.isSelected());
        m_lowerLabel.setEnabled(m_useRange.isSelected());
        m_lowerBound.setEnabled(m_useRange.isSelected());

        m_regLabel.setEnabled(m_useRegExpr.isSelected());
        m_regExpr.setEnabled(m_useRegExpr.isSelected());
        m_caseSensitive.setEnabled(m_useRegExpr.isSelected());
        m_isRegExpr.setEnabled(m_useRegExpr.isSelected());
        m_hasWildCards.setEnabled(m_useRegExpr.isSelected());

        // have the err text updated
        if (m_useMissValue.isSelected()) {
            setErrMsg("");
        } else if (m_useRange.isSelected()) {
            boundsChanged();
        } else {
            regExprChanged();
        }

    }

    /**
     * Called when user changes the values for the lower or upper bounds.
     */
    protected void boundsChanged() {
        // check if the entered value somehow goes along with the selected col.
        setErrMsg("");
        if (m_tSpec == null) {
            return;
        }
        if (getSelectedColumnName() == null) {
            return;
        }
        if (!m_useRange.isSelected()) {
            return;
        }
        DataCell lowBound = null;
        DataCell hiBound = null;
        try {
            lowBound = getLowerBoundCell();
            hiBound = getUpperBoundCell();
        } catch (InvalidSettingsException ise) {
            setErrMsg(ise.getMessage());
            return;
        }
        if ((lowBound == null) && (hiBound == null)) {
            setErrMsg("Specify at least one range boundary");
            return;
        }
        if ((lowBound != null) && (hiBound != null)) {
            DataValueComparator comp;
            comp =
                    DataType.getCommonSuperType(lowBound.getType(),
                            hiBound.getType()).getComparator();
            if (comp.compare(hiBound, lowBound) == -1) {
                setErrMsg("The lower bound must be smaller than the"
                        + " upper bound");
                return;
            }
        }

        if (((lowBound != null) && (lowBound instanceof StringCell))
                || ((hiBound != null) && (hiBound instanceof StringCell))) {
            setErrMsg("Warning: String comparison is used for "
                    + "range checking. May not work as expected!");
        }
    }

    /**
     * Called when the user selects a new column.
     */
    protected void selectedColChanged() {
        //enable/disable the deep filtering option if the selected column is a collection column
        updateDeepFiltering();

        /*
         * fill the regExpr combo with the new possible values
         */
        // de-register the item listener - we are changing the selection
        m_regExpr.removeItemListener(this);

        String oldVal = (String)m_regExpr.getEditor().getItem();
        m_regExpr.setModel(new DefaultComboBoxModel(
                getPossibleValuesOfSelectedColumn()));
        m_regExpr.setSelectedItem(oldVal);

        // register us again with the regExpr box
        m_regExpr.addItemListener(this);
        regExprChanged();

        /*
         * trigger bounds check
         */
        boundsChanged();
    }

    /**
     * Updates the state (enabled/disabled) of the deep filtering option depending on the currently selected
     * filter column.
     */
    private void updateDeepFiltering() {
        if (m_colCombo == null) {
            return;
        }
        final String col = m_colCombo.getSelectedColumn();
        if (col == null) {
            return;
        }
        if (m_tSpec == null) {
            return;
        }
        final DataColumnSpec spec = m_tSpec.getColumnSpec(col);
        if (spec == null) {
            return;
        }
        if (m_deepFiltering == null) {
            return;
        }
        m_deepFiltering.setEnabled(spec.getType().isCollectionType());
    }

    /**
     * Checks the entered (or selected) regular expression and sets an error.
     */
    protected void regExprChanged() {
        setErrMsg("");
        if (((String)m_regExpr.getEditor().getItem()).length() <= 0) {
            setErrMsg("Enter valid pattern");
            validate();
            return;
        }
        try {
            String pattern = (String)m_regExpr.getEditor().getItem();
            if (m_hasWildCards.isSelected()) {
                pattern = WildcardMatcher.wildcardToRegex(pattern);
                Pattern.compile(pattern);
            } else if (m_isRegExpr.isSelected()) {
                Pattern.compile(pattern);
            }
        } catch (PatternSyntaxException pse) {
            setErrMsg("Error in pattern. ('" + pse.getMessage() + "')");
            validate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadSettingsFromFilter(final IRowFilter filter)
            throws InvalidSettingsException {
        // accept ColValFilterOldObsolete for backward compatibility
        if (filter instanceof ColValFilterOldObsolete) {
            loadSettingsFromColValFilter((ColValFilterOldObsolete)filter);
        } else {
            if (!(filter instanceof AttrValueRowFilter)) {
                throw new InvalidSettingsException("Column value row filter "
                        + "panel can only load settings from a range filter, "
                        + "an attribute value filter.");
            }
            AttrValueRowFilter avFilter = (AttrValueRowFilter)filter;
            String colName = avFilter.getColName();
            if (colName != null) {
                m_colCombo.setSelectedColumn(colName);
            }
            m_deepFiltering.setSelected(avFilter.getDeepFiltering());
            updateDeepFiltering();

            if (filter instanceof StringCompareRowFilter) {
                StringCompareRowFilter f = (StringCompareRowFilter)filter;

                m_useRegExpr.setSelected(true);
                m_regExpr.setSelectedItem(f.getPattern());
                m_isRegExpr.setSelected(f.getIsRegExpr());
                m_hasWildCards.setSelected(f.getHasWildcards());
                m_caseSensitive.setSelected(f.getCaseSensitive());

            } else if (filter instanceof RangeRowFilter) {
                RangeRowFilter f = (RangeRowFilter)filter;

                m_useRange.setSelected(true);
                String upper = "";
                String lower = "";
                if (f.getUpperBound() != null) {
                    upper = f.getUpperBound().toString();
                }
                if (f.getLowerBound() != null) {
                    lower = f.getLowerBound().toString();
                }
                m_upperBound.setText(upper);
                m_lowerBound.setText(lower);

            } else if (filter instanceof MissingValueRowFilter) {

                m_useMissValue.setSelected(true);
            } else {
                // you must implement functionality here if you create a new
                // attribute value filter.
                assert false;
            }
        }
    }

    private void loadSettingsFromColValFilter(final ColValFilterOldObsolete f) {

        String colName = f.getColumnName();
        if (colName != null) {
            m_colCombo.setSelectedColumn(colName);
        }
        if (f.getFilterMissingValues()) {
            m_useMissValue.setSelected(true);
        } else if (f.rangeSet()) {
            String upper = "";
            String lower = "";
            if (f.getUpperBound() != null) {
                upper = f.getUpperBound().toString();
            }
            if (f.getLowerBound() != null) {
                lower = f.getLowerBound().toString();
            }
            m_upperBound.setText(upper);
            m_lowerBound.setText(lower);
            if (m_useRange.isEnabled()) {
                m_useRange.setSelected(true);
            }
        } else {
            m_useRegExpr.setSelected(true);
            m_regExpr.setSelectedItem(f.getRegExpr());
            m_caseSensitive.setSelected(f.caseSensitiveMatch());
        }

        m_isRegExpr.setSelected(true);
        m_hasWildCards.setSelected(false);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IRowFilter createFilter(final boolean include)
            throws InvalidSettingsException {
        if (hasErrors()) {
            throw new InvalidSettingsException(getErrMsg());
        }

        String colName = getSelectedColumnName();
        if ((colName == null) || (colName.length() == 0)) {
            setErrMsg("Select a valid column");
            validate();
            throw new InvalidSettingsException(getErrMsg());
        }
        boolean deepFiltering = getDeepFiltering();

        if (m_useRange.isSelected()) {
            DataCell loBound = getLowerBoundCell();
            DataCell hiBound = getUpperBoundCell();
            if ((loBound == null) && (hiBound == null)) {
                setErrMsg("Enter at least one valid range boundary");
                validate();
                throw new InvalidSettingsException(getErrMsg());
            }

            return new RangeRowFilter(colName, include, deepFiltering, loBound, hiBound);
        }

        if (m_useRegExpr.isSelected()) {
            String regExpr = (String)m_regExpr.getEditor().getItem();
            if (regExpr.length() <= 0) {
                setErrMsg("Enter a valid pattern to match");
                validate();
                throw new InvalidSettingsException(getErrMsg());
            }
            return new StringCompareRowFilter(regExpr, colName,
                    include, deepFiltering, m_caseSensitive.isSelected(), m_hasWildCards
                            .isSelected(), m_isRegExpr.isSelected());
        }

        if (m_useMissValue.isSelected()) {
            return new MissingValueRowFilter(colName, include, deepFiltering);
        }

        throw new InvalidSettingsException("Internal Error. "
                + "Please change some settings and try again. Sorry");
    }

    /**
     * @return the deep filtering flag
     */
    private boolean getDeepFiltering() {
        return m_deepFiltering.isSelected();
    }

    /**
     * @return the selected name of the column to test.
     */
    private String getSelectedColumnName() {

        return m_colCombo.getSelectedColumn();
    }

    /*
     * returns a DataCell of the entered value in the upper bound field. Will
     * return null if no text is entered in the corresponding edit field, if no
     * valid column index is set, if the entered value is not valid (with
     * respect to the selected column and its type - if a tablespec is set). In
     * the latter case the errText field is updated. If no table spec is set it
     * will create the most specific cell that can hold the value (IntCell,
     * DoubleCell, or finally StringCell)
     *
     * @return a DataCell of the entered value in the upper bound field or null
     * if a problem occurred.
     */
    private DataCell getUpperBoundCell() throws InvalidSettingsException {
        return getBoundCell(m_upperBound, "upper");
    }

    /*
     * returns a DataCell of the entered value in the lower bound field. Will
     * return null if no text is entered in the corresponding edit field. Will
     * throw an exception if no valid column index is set, if the entered value
     * is not valid (with respect to the selected column and its type - if a
     * tablespec is set). It will not update the errText field. If no table spec
     * is set it will create the most specific cell that can hold the value
     * (IntCell, DoubleCell, or finally StringCell)
     *
     * @return a DataCell of the entered value in the lower bound field or no
     * text was entered.
     */
    private DataCell getLowerBoundCell() throws InvalidSettingsException {
        return getBoundCell(m_lowerBound, "lower");
    }

    /* method used from the above */
    private DataCell getBoundCell(final JTextField editField, final String name)
            throws InvalidSettingsException {
        if (editField.getText().length() <= 0) {
            return null;
        }
        String colName = getSelectedColumnName();
        if ((colName == null) || (colName.length() == 0)) {
            throw new InvalidSettingsException("Invalid columns selection");
        }

        if (m_tSpec != null) {
            final DataType origType = m_tSpec.getColumnSpec(colName).getType();
            final DataType cType;
            if (m_deepFiltering.isSelected() &&  origType.isCollectionType()) {
                cType =  origType.getCollectionElementType();
            } else {
                cType =  origType;
            }
            if (cType.isCompatible(IntValue.class)) {
                // first try making of an IntCell
                try {
                    int lb = Integer.parseInt(editField.getText());
                    return new IntCell(lb);
                } catch (NumberFormatException nfe) {
                    throw new InvalidSettingsException(
                            "Number format error in " + name
                                    + " bound number: Enter a valid integer.");
                }
            } else if (cType.isCompatible(LongValue.class)) {
                try {
                    long lb = Long.parseLong(editField.getText());
                    return new LongCell(lb);
                } catch (NumberFormatException nfe) {
                    throw new InvalidSettingsException(
                            "Number format error in " + name
                                    + " bound number: Enter a valid number.");
                }

            } else if (cType.isCompatible(DoubleValue.class)) {
                try {
                    double lb = Double.parseDouble(editField.getText());
                    return new DoubleCell(lb);
                } catch (NumberFormatException nfe) {
                    throw new InvalidSettingsException(
                            "Number format error in " + name
                                    + " bound number: enter a valid "
                                    + "float number");
                }
            } else {
                return new StringCell(editField.getText());
            }
        } else {
            // if we got no column type
            return new StringCell(editField.getText());
        }

    }

    /**
     * @return true if the settings in the panel are invalid, false if they are
     *         consistent and usable.
     */
    public boolean hasErrors() {
        if (m_errText.getText().length() <= 0) {
            return false;
        }
        if (m_errText.getText().substring(0, 7).equalsIgnoreCase("WARNING")) {
            return false;
        }
        return true;
    }

    /**
     * @return a message to the user if hasErrors returns true
     */
    public String getErrMsg() {
        return m_errText.getText();
    }

    private void setErrMsg(final String msg) {
        m_errText.setText(msg);
        m_errText.setToolTipText(msg);
    }
}
