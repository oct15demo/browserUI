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
 *   11.04.2008 (thor): created
 */
package org.knime.base.node.rules;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.knime.base.node.rules.Rule.ColumnReference;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.FlowVariableModelButton;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.DataColumnSpecListCellRenderer;
import org.knime.core.node.workflow.FlowVariable;

/**
 *
 * @author Thorsten Meinl, University of Konstanz
 * @deprecated Use the org.knime.jsnippets project's ...rules.engine classes.
 */
@Deprecated
public class RuleEngineNodeDialog extends NodeDialogPane {

    private static final NodeLogger LOGGER = NodeLogger
            .getLogger(RuleEngineNodeDialog.class);

    /** Default default label. */
    static final String DEFAULT_LABEL = "default";

    /** Default name for the newly appended column. */
    static final String NEW_COL_NAME = "prediction";

    private static final String RULE_LABEL = "Enter condition...";

    private static final String OUTCOME_LABEL = "Outcome...";

    private JTextField m_defaultLabelEditor;

    private JTextField m_newColumnName;

    private JTextField m_ruleEditor;

    private JTextField m_ruleLabelEditor;

    private JList m_variableList;

    private JList m_operatorList;

    private DefaultListModel m_variableModel;

    private DefaultListModel m_operatorModel;

    private DefaultListModel m_ruleModel;

    private JList m_rules;

    private JLabel m_error;

    private DataTableSpec m_spec;

    private JTextField m_lastUsedTextfield;

    private JCheckBox m_outcomeIsColumn;

    private JCheckBox m_defaultLabelIsColumn;

    /**
     *
     */
    public RuleEngineNodeDialog() {
        initializeComponent();
    }

    private void initializeComponent() {
        JSplitPane horizontalSplit =
                new JSplitPane(JSplitPane.VERTICAL_SPLIT, createTopPart(),
                        createBottomPart());
        addTab("Rule Editor", horizontalSplit);
    }

    /*
     * top part (from left to right) = variable list, operator list, editor box
     */
    private Box createTopPart() {
        /*
         * Variable list (column names)
         */
        m_variableModel = new DefaultListModel();
        m_variableList = new JList(m_variableModel);
        m_variableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_variableList.setCellRenderer(new DataColumnSpecListCellRenderer());
        m_variableList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(final ListSelectionEvent arg0) {
                if (arg0.getValueIsAdjusting()) {
                    return;
                }
                if (m_lastUsedTextfield != null) {
                    String existingText = m_lastUsedTextfield.getText();
                    if (existingText.equals(RULE_LABEL)) {
                        existingText = "";
                    }
                    if (!m_variableList.isSelectionEmpty()) {
                        String newText =
                                ((DataColumnSpec)m_variableList
                                        .getSelectedValue()).getName();
                        m_lastUsedTextfield.setText((existingText + " $"
                                + newText + "$").trim());
                        m_lastUsedTextfield.requestFocusInWindow();
                        if (m_lastUsedTextfield == m_ruleLabelEditor) {
                            m_outcomeIsColumn.setSelected(true);
                        }
                    }
                }
                m_variableList.clearSelection();
            }
        });
        /*
         * Operators (<, >, <=, >=, =, AND, OR, etc)
         */
        m_operatorModel = new DefaultListModel();
        m_operatorList = new JList(m_operatorModel);
        m_operatorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_operatorList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(final ListSelectionEvent arg0) {
                if (!arg0.getValueIsAdjusting()
                        && !m_operatorList.isSelectionEmpty()) {
                    String existingText = m_ruleEditor.getText();
                    if (existingText.equals(RULE_LABEL)) {
                        existingText = "";
                    }
                    String newText = (String)m_operatorList.getSelectedValue();
                    m_ruleEditor.setText(existingText + " " + newText);
                    m_ruleEditor.requestFocusInWindow();
                    m_operatorList.clearSelection();
                }
            }
        });

        Box editorBox = createEditorPart();
        Box listBox = Box.createHorizontalBox();
        JScrollPane variableScroller = new JScrollPane(m_variableList);
        variableScroller.setBorder(BorderFactory
                .createTitledBorder("Columns"));
        JScrollPane operatorScroller = new JScrollPane(m_operatorList);
        operatorScroller.setBorder(BorderFactory
                .createTitledBorder("Operators"));
        listBox.add(variableScroller);
        listBox.add(operatorScroller);

        JSplitPane splitPane =
                new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listBox, editorBox);
        Dimension minimumSize = new Dimension(200, 50);
        listBox.setMinimumSize(minimumSize);
        editorBox.setMinimumSize(minimumSize);

        Box topBox = Box.createHorizontalBox();
        topBox.add(splitPane);
        topBox.add(Box.createHorizontalGlue());
        return topBox;
    }

    /*
     * Editor part (from top to bottom, from left to right): default label
     * (label, text field) new column name (label, text field) rule editor (rule
     * text field, outcome text field, add button, clear button) error text
     * field
     */
    private Box createEditorPart() {
        /*
         * Default label box
         */
        m_defaultLabelEditor = new JTextField(10);
        m_defaultLabelEditor
                .setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        m_defaultLabelEditor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent e) {
                if (m_defaultLabelEditor.getText().equals(DEFAULT_LABEL)) {
                    m_defaultLabelEditor.setText("");
                }
                m_lastUsedTextfield = m_defaultLabelEditor;
            }
        });
        JPanel defaultLabelBox = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 20, 0, 0);
        defaultLabelBox.add(new JLabel("Default label (if no rule matches): "),
                c);
        c.insets = new Insets(0, 0, 0, 0);
        c.gridx++;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        defaultLabelBox.add(m_defaultLabelEditor, c);
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.1;
        // also add a variable Model + corresponding icon to make this
        // option controllable via a variable
        FlowVariableModel fvm =
                createFlowVariableModel(RuleEngineSettings.CFG_DEFAULT_LABEL,
                        FlowVariable.Type.STRING);
        fvm.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent evt) {
                FlowVariableModel svm = (FlowVariableModel)(evt.getSource());
                m_defaultLabelEditor.setEnabled(!svm
                        .isVariableReplacementEnabled());
            }
        });
        c.gridx++;
        defaultLabelBox.add(new FlowVariableModelButton(fvm), c);
        c.gridx++;
        defaultLabelBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        c.gridy++;
        c.gridx = 1;
        m_defaultLabelIsColumn = new JCheckBox("is a column reference");
        defaultLabelBox.add(m_defaultLabelIsColumn, c);

        /*
         * New column name box
         */
        m_newColumnName = new JTextField(10);
        m_newColumnName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        m_newColumnName.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent e) {
                if (m_newColumnName.getText().equals(NEW_COL_NAME)) {
                    m_newColumnName.setText("");
                }
            }
        });
        Box newColNameBox = Box.createHorizontalBox();
        newColNameBox.add(Box.createHorizontalStrut(20));
        newColNameBox.add(new JLabel("Appended column name: "));
        newColNameBox.add(Box.createHorizontalGlue());
        newColNameBox.add(m_newColumnName);
        newColNameBox.add(Box.createHorizontalGlue());
        newColNameBox.add(Box.createHorizontalStrut(10));
        newColNameBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        /*
         * Rule Box
         */
        JPanel ruleBox = new JPanel(new GridBagLayout());
        m_ruleEditor = new JTextField(RULE_LABEL, 35);
        m_ruleEditor.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        m_ruleEditor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent e) {
                if (m_ruleEditor.getText().equals(RULE_LABEL)) {
                    m_ruleEditor.setText("");
                }
                m_lastUsedTextfield = m_ruleEditor;
            }
        });
        m_ruleLabelEditor = new JTextField(OUTCOME_LABEL, 10);
        m_ruleLabelEditor.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        m_ruleLabelEditor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent e) {
                if (m_ruleLabelEditor.getText().equals(OUTCOME_LABEL)) {
                    m_ruleLabelEditor.setText("");
                }
                m_lastUsedTextfield = m_ruleLabelEditor;
            }
        });
        m_lastUsedTextfield = m_ruleEditor;
        /*
         * Add Button
         */
        JButton add = new JButton("Add");
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                LOGGER.debug("adding: " + m_ruleEditor.getText() + "=>"
                        + m_ruleLabelEditor.getText());
                addRule();
                m_lastUsedTextfield = m_ruleEditor;
            }
        });
        /*
         * Clear Button
         */
        JButton clear = new JButton("Clear");
        clear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                m_ruleEditor.setText("");
                m_ruleLabelEditor.setText("");
            }
        });

        m_outcomeIsColumn = new JCheckBox("is a column reference");
        /*
         * Putting the rule editor together (rule, outcome, add, clear)
         */
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 20, 0, 0);
        c.weightx = 0.8;
        c.fill = GridBagConstraints.HORIZONTAL;

        ruleBox.add(new JLabel("Condition"), c);
        c.gridx++;
        c.weightx = 0.1;
        c.insets = new Insets(0, 5, 0, 0);
        ruleBox.add(new JLabel("Outcome"), c);


        c.gridx = 0;
        c.gridy++;
        c.insets = new Insets(0, 20, 0, 0);
        ruleBox.add(m_ruleEditor, c);
        c.insets = new Insets(0, 5, 0, 0);
        c.gridx++;
        c.weightx = 0.1;
        ruleBox.add(m_ruleLabelEditor, c);
        c.insets = new Insets(0, 10, 0, 0);
        c.fill = GridBagConstraints.NONE;
        c.gridx++;
        ruleBox.add(add, c);
        c.gridx++;
        ruleBox.add(clear, c);

        c.gridy++;
        c.gridx = 1;
        c.insets = new Insets(0, 5, 0, 0);
        ruleBox.add(m_outcomeIsColumn, c);

        /*
         * Putting it all together
         */
        Box editorBox = Box.createVerticalBox();
        editorBox.add(newColNameBox);
        editorBox.add(Box.createVerticalStrut(20));
        editorBox.add(defaultLabelBox);
        editorBox.add(Box.createVerticalStrut(20));
        editorBox.add(ruleBox);
        editorBox.add(Box.createVerticalStrut(20));
        m_error = new JLabel(" ");
        m_error.setForeground(Color.RED);
        editorBox.add(m_error);
        editorBox.setBorder(BorderFactory.createEtchedBorder());
        return editorBox;
    }

    /*
     * Bottom part (from left ot right): rule list, button part
     */
    private Box createBottomPart() {
        /*
         * Put all rules row by row
         */
        Box bottom = Box.createHorizontalBox();
        /*
         * Rule List
         */
        m_ruleModel = new DefaultListModel();
        m_rules = new JList(m_ruleModel);
        m_rules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Box bottomLeft = Box.createVerticalBox();
        bottomLeft.add(new JScrollPane(m_rules));
        bottomLeft.add(Box.createVerticalGlue());
        bottom.add(bottomLeft);
        bottom.add(createButtonPart());
        return bottom;
    }

    /*
     * Button part (from top to bottom): - move box: up btn, down btn) - edit
     * box: edit button, remove button
     */
    private Box createButtonPart() {
        /*
         * Button "Up"
         */
        JButton up = new JButton("Up");
        up.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                int pos = m_rules.getSelectedIndex();
                if (pos > 0) {
                    Rule r = (Rule)m_rules.getSelectedValue();
                    m_ruleModel.remove(pos);
                    m_ruleModel.insertElementAt(r, pos - 1);
                    m_rules.setSelectedIndex(pos - 1);
                }
            }
        });
        /*
         * Button "Down"
         */
        JButton down = new JButton("Down");
        down.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                int pos = m_rules.getSelectedIndex();
                if (pos != -1 && pos < m_ruleModel.getSize() - 1) {
                    Rule r = (Rule)m_rules.getSelectedValue();
                    m_ruleModel.remove(pos);
                    m_ruleModel.insertElementAt(r, pos + 1);
                    m_rules.setSelectedIndex(pos + 1);
                }
            }
        });
        /*
         * Move buttons
         */
        Box moveBtns = Box.createVerticalBox();
        moveBtns.setBorder(BorderFactory.createTitledBorder("Move:"));
        moveBtns.add(up);
        moveBtns.add(Box.createVerticalStrut(10));
        moveBtns.add(down);
        moveBtns.add(Box.createVerticalGlue());
        moveBtns.setMaximumSize(moveBtns.getPreferredSize());
        /*
         * Button "Edit"
         */
        JButton edit = new JButton("Edit");
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                if ((m_ruleEditor.getText() != RULE_LABEL)
                        && (m_ruleEditor.getText().trim().length() > 0)) {
                    if (JOptionPane.showConfirmDialog(getPanel(),
                            "Override currently edited rule?", "Confirm...",
                            JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
                        return;
                    }
                }
                int rPos = m_rules.getSelectedIndex();
                Rule r = (Rule)m_ruleModel.get(rPos);
                m_ruleEditor.setText(r.getCondition());
                Object outcome = r.getOutcome();
                if (outcome instanceof ColumnReference) {
                    m_ruleLabelEditor.setText(r.getOutcome().toString());
                    m_outcomeIsColumn.setSelected(true);
                } else if (outcome instanceof Number) {
                    m_ruleLabelEditor.setText(outcome.toString());
                    m_outcomeIsColumn.setSelected(false);
                } else {
                    String s = r.getOutcome().toString();
                    s = s.replaceAll("\"", "");
                    m_ruleLabelEditor.setText(s);
                    m_outcomeIsColumn.setSelected(false);
                }

                m_ruleModel.removeElement(r);
            }

        });
        /*
         * Button "Remove"
         */
        JButton remove = new JButton("Remove");
        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                int pos = m_rules.getSelectedIndex();
                if (pos >= 0
                        && JOptionPane.showConfirmDialog(getPanel(),
                                "Remove selected rule?", "Confirm...",
                                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    m_ruleModel.remove(pos);
                }
            }
        });
        // make all buttons the same width
        remove.setMaximumSize(remove.getPreferredSize());
        up.setMinimumSize(remove.getPreferredSize());
        down.setMinimumSize(remove.getPreferredSize());
        edit.setMinimumSize(remove.getPreferredSize());
        /*
         * Edit buttons
         */
        Box editBtns = Box.createVerticalBox();
        editBtns.setBorder(BorderFactory.createTitledBorder("Edit/Remove"));
        editBtns.add(edit);
        editBtns.add(Box.createVerticalStrut(20));
        editBtns.add(remove);
        editBtns.add(Box.createVerticalGlue());
        editBtns.setMaximumSize(editBtns.getPreferredSize());

        Box bottomRight = Box.createVerticalBox();
        bottomRight.add(moveBtns);
        bottomRight.add(editBtns);
        bottomRight.add(Box.createVerticalGlue());
        return bottomRight;
    }

    /*
     * Adds a rule to the rule list.
     */
    private void addRule() {
        try {
            String antecedent = m_ruleEditor.getText().trim();
            m_ruleEditor.setText(antecedent);
            String consequent = m_ruleLabelEditor.getText();

            if (!m_outcomeIsColumn.isSelected()) {
                if (!consequent.startsWith("\"") && ! consequent.endsWith("\"")) {
                    try {
                        Double.parseDouble(consequent);
                        // a number => no need for quotes
                    } catch (NumberFormatException ex) {
                        // not a number => text => quote it
                        consequent = "\"" + consequent + "\"";
                    }
                }
            } else if (!consequent.startsWith("$") || !consequent.endsWith("$")) {
                m_error.setText("Column references must be enclosed in $");
                m_ruleLabelEditor.requestFocusInWindow();
                return;
            } else {
                consequent = consequent.trim();
            }
            m_ruleLabelEditor.setText(consequent);

            /*
             * Tries to create rule. If fails: set error message and caret to
             * referring position
             */
            m_ruleModel.addElement(new Rule(antecedent + "=>" + consequent,
                    m_spec));
            m_error.setText("");
            m_ruleEditor.setText("");
            m_ruleLabelEditor.setText("");
            m_outcomeIsColumn.setSelected(false);
            getPanel().repaint();
        } catch (ParseException e) {
            m_error.setText(e.getMessage());
            int offset = e.getErrorOffset();
            if (offset <= m_ruleEditor.getText().length()) {
                m_ruleEditor.requestFocusInWindow();
                m_ruleEditor.setCaretPosition(offset);
            } else if ((offset - m_ruleEditor.getText().length())
                    <= m_ruleLabelEditor.getText().length()) {
                m_ruleLabelEditor.requestFocusInWindow();
                m_ruleLabelEditor.setCaretPosition(offset
                        - m_ruleEditor.getText().length() - 2);
            }
        }
    }

    /*
     * Variables are the available column names
     */
    private List<DataColumnSpec> getVariables() {
        List<DataColumnSpec> variables = new ArrayList<DataColumnSpec>();
        if (m_spec != null) {
            for (DataColumnSpec colSpec : m_spec) {
                variables.add(colSpec);
            }
        }
        return variables;
    }

    /*
     * Operators are defined in class Rule.Operators, such as <, >, <=, >=, =,
     * AND, OR, NOT, etc.
     */
    private String[] getOperators() {
        List<String> operators = new ArrayList<String>();
        for (Rule.Operators op : Rule.Operators.values()) {
            operators.add(op.toString());
        }
        String[] array = new String[operators.size()];
        return operators.toArray(array);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings,
            final DataTableSpec[] specs) throws NotConfigurableException {
        if (specs == null || specs.length == 0 || specs[0] == null
                || specs[0].getNumColumns() == 0) {
            throw new NotConfigurableException("No columns available!");
        }
        m_ruleEditor.setText("");
        m_spec = specs[0];
        m_variableModel.clear();
        for (DataColumnSpec s : getVariables()) {
            m_variableModel.addElement(s);
        }
        m_operatorModel.clear();
        for (String op : getOperators()) {
            m_operatorModel.addElement(op);
        }
        RuleEngineSettings ruleSettings = new RuleEngineSettings();
        ruleSettings.loadSettingsForDialog(settings);
        String defaultLabel = ruleSettings.getDefaultLabel();
        m_defaultLabelEditor.setText(defaultLabel);
        m_defaultLabelIsColumn.setSelected(ruleSettings
                .getDefaultLabelIsColumn());
        String newColName = ruleSettings.getNewColName();
        m_newColumnName.setText(newColName);
        m_ruleModel.clear();
        for (String rs : ruleSettings.rules()) {
            try {
                Rule r = new Rule(rs, m_spec);
                m_ruleModel.addElement(r);
            } catch (ParseException e) {
                LOGGER.warn("Rule '" + rs + "' removed, because of "
                        + e.getMessage());
            }
        }
        m_lastUsedTextfield = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
        if (m_ruleEditor.getText().length() > 0) {
            throw new InvalidSettingsException("The condition field contains "
                    + "a rule that has not been added to the list yet.");
        }

        RuleEngineSettings ruleSettings = new RuleEngineSettings();
        ruleSettings.setDefaultLabel(m_defaultLabelEditor.getText());
        ruleSettings.setDefaultLabelIsColumn(m_defaultLabelIsColumn
                .isSelected());
        ruleSettings.setNewcolName(m_newColumnName.getText());
        for (int i = 0; i < m_ruleModel.getSize(); i++) {
            Rule r = (Rule)m_ruleModel.getElementAt(i);
            ruleSettings.addRule(r.toString());
        }
        ruleSettings.saveSettings(settings);
    }
}
