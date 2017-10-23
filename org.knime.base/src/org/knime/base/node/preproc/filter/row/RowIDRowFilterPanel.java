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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.knime.base.node.preproc.filter.row.rowfilter.IRowFilter;
import org.knime.base.node.preproc.filter.row.rowfilter.RowIDRowFilter;
import org.knime.core.node.InvalidSettingsException;

/**
 *
 * @author Peter Ohl, University of Konstanz
 */
public class RowIDRowFilterPanel extends RowFilterPanel {
    private JTextArea m_errText;

    private JTextField m_regExpr;

    private JCheckBox m_caseSensitive;

    private JCheckBox m_startsWith;

    /**
     * Creates a new panel for a row ID filter.
     */
    public RowIDRowFilterPanel() {
        super(400, 350);

        m_errText = new JTextArea();
        m_errText.setEditable(false);
        m_errText.setLineWrap(true);
        m_errText.setWrapStyleWord(true);
        m_errText.setBackground(getBackground());
        m_errText.setFont(new Font(m_errText.getFont().getName(), Font.BOLD, m_errText.getFont().getSize()));
        m_errText.setMinimumSize(new Dimension(350, 50));
        m_errText.setMaximumSize(new Dimension(350, 100));
        m_errText.setForeground(Color.RED);

        m_regExpr = new JTextField();
        m_caseSensitive = new JCheckBox("case sensitive match");
        m_startsWith = new JCheckBox("row ID must only start with expression");
        m_startsWith
                .setToolTipText("if not checked the entire row ID must match");
        m_regExpr.getDocument().addDocumentListener(new DocumentListener() {
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
        m_regExpr.setMaximumSize(new Dimension(6022, 25));
        m_regExpr.setMinimumSize(new Dimension(150, 25));
        m_regExpr.setPreferredSize(new Dimension(100, 25));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory
            .createEtchedBorder(), "Row ID pattern"));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(2, 2, 2, 2);
        c.anchor = GridBagConstraints.WEST;

        panel.add(new JLabel("Regular expression   "), c);
        c.gridx = 1;

        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(m_regExpr, c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 2;


        panel.add(m_caseSensitive, c);
        c.gridy++;
        panel.add(m_startsWith, c);
        c.gridy++;
        c.weighty = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        panel.add(m_errText, c);

        this.add(panel);

        updateErrText();
    }

    /**
     * Called when the textfield content changes.
     */
    protected void regExprChanged() {
        updateErrText();
    }

    private void updateErrText() {
        m_errText.setText("");
        if (m_regExpr.getText().length() <= 0) {
            m_errText.setText("Enter a valid regular expression");
            return;
        }
        try {
            Pattern.compile(m_regExpr.getText());
        } catch (PatternSyntaxException pse) {
            m_errText.setText("Error in regular expression ('"
                    + pse.getMessage() + "')");
        }
    }

    /**
     * @return <code>true</code> if an error message is currently displayed in
     *         the panel
     */
    boolean hasErrors() {
        return m_errText.getText().length() != 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadSettingsFromFilter(final IRowFilter filter) throws InvalidSettingsException {
        if (!(filter instanceof RowIDRowFilter)) {
            throw new InvalidSettingsException("RegExpr filter panel can only "
                    + "load settings from a RegExprRowFilter");
        }

        RowIDRowFilter reFilter = (RowIDRowFilter)filter;

        m_caseSensitive.setSelected(reFilter.getCaseSensitivity());
        m_startsWith.setSelected(reFilter.getStartsWith());
        m_regExpr.setText(reFilter.getRegExpr());
        regExprChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IRowFilter createFilter(final boolean include) throws InvalidSettingsException {
        // just in case, because the err text is the indicator for err existence
        updateErrText();

        if (hasErrors()) {
            throw new InvalidSettingsException(m_errText.getText());
        }

        return new RowIDRowFilter(m_regExpr.getText(), include, m_caseSensitive
                .isSelected(), m_startsWith.isSelected());

    }
}
