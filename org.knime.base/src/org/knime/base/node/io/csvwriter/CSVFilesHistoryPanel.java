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
 *   Dec 17, 2005 (wiswedel): created
 */
package org.knime.base.node.io.csvwriter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.util.LinkedHashSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.FlowVariableModelButton;
import org.knime.core.node.util.ConvenientComboBoxRenderer;
import org.knime.core.node.util.FilesHistoryPanel;
import org.knime.core.node.util.StringHistory;
import org.knime.core.util.FileUtil;
import org.knime.core.util.SimpleFileFilter;

/**
 * Panel that contains an editable Combo Box showing the file to write to and a
 * button to trigger a file chooser. The elements in the combo are files that
 * have been recently used.
 *
 * @see org.knime.core.node.util.StringHistory
 * @author Bernd Wiswedel, University of Konstanz
 */
public final class CSVFilesHistoryPanel extends JPanel {

    private final JComboBox<String> m_textBox;

    private final JButton m_chooseButton;

    private final FilesHistoryPanel.LocationCheckLabel m_warnMsg;

    /**
     * Creates new instance, sets properties, for instance renderer,
     * accordingly.
     */
    public CSVFilesHistoryPanel() {
        this(null);
    }

    /**
     * Creates new instance, sets properties, for instance renderer,
     * accordingly.
     *
     * @param fvm model to allow to use a variable instead of the textfield.
     */
    CSVFilesHistoryPanel(final FlowVariableModel fvm) {
        super(new GridBagLayout());
        m_textBox = new JComboBox<String>(new DefaultComboBoxModel<String>());
        m_textBox.setEditable(true);
        m_textBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        m_textBox.setPreferredSize(new Dimension(300, 25));
        m_textBox.setRenderer(new ConvenientComboBoxRenderer());

        // install listeners to update warn message whenever file name changes
        m_textBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                fileLocationChanged();
            }
        });
        /* install action listeners */
        m_textBox.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(final FocusEvent e) {
                fileLocationChanged();
            }
        });
        Component editor = m_textBox.getEditor().getEditorComponent();
        if (editor instanceof JTextComponent) {
            Document d = ((JTextComponent)editor).getDocument();
            d.addDocumentListener(new DocumentListener() {
                @Override
                public void changedUpdate(final DocumentEvent e) {
                    fileLocationChanged();
                }

                @Override
                public void insertUpdate(final DocumentEvent e) {
                    fileLocationChanged();
                }

                @Override
                public void removeUpdate(final DocumentEvent e) {
                    fileLocationChanged();
                }
            });
        }

        m_chooseButton = new JButton("Browse...");
        m_chooseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                String newFile = getOutputFileName();
                if (newFile != null) {
                    newFile = newFile.trim();
                    if (newFile.length() < 5
                            || newFile.lastIndexOf('.')
                                    < newFile.length() - 4) {
                        // they have no extension - add .csv
                        newFile += ".csv";
                    }
                    m_textBox.setSelectedItem(newFile);
                }
            }
        });
        m_warnMsg = FilesHistoryPanel.LocationValidation.FileOutput.createLabel();
        // this ensures correct display of the changing label content...
        m_warnMsg.setPreferredSize(new Dimension(350, 25));
        m_warnMsg.setMinimumSize(new Dimension(350, 25));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        add(m_textBox, c);
        c.gridx = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 5, 0, 0);
        add(m_chooseButton, c);

        if (fvm != null) {
            c.gridx = 2;
            c.insets = new Insets(2, 10, 2, 2);
            add(new FlowVariableModelButton(fvm), c);
            fvm.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(final ChangeEvent evt) {
                    FlowVariableModel wvm =
                            (FlowVariableModel)(evt.getSource());
                    m_textBox.setEnabled(!wvm.isVariableReplacementEnabled());
                    m_chooseButton.setEnabled(!wvm
                        .isVariableReplacementEnabled());
                }
            });
            c.insets = new Insets(0, 0, 0, 0);
        }

        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = (fvm == null) ? 2 : 3;
        add(m_warnMsg, c);

        updateHistory();
        fileLocationChanged();
    }

    private void fileLocationChanged() {
        String selFile = getSelectedFile();
        m_warnMsg.setText("");
        if ((selFile != null) && !selFile.isEmpty()) {
            try {
                URL newUrl = FileUtil.toURL(selFile);
                m_warnMsg.checkLocation(newUrl);
            } catch (InvalidPathException ex) {
                m_warnMsg.setText("Invalid file system path: " + ex.getMessage());
                m_warnMsg.setForeground(Color.RED);
            } catch (IOException ex) {
                // ignore it
            }
        }
    }

    private String getOutputFileName() {
        // file chooser triggered by choose button
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setFileFilter(new SimpleFileFilter(".csv"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        String f = m_textBox.getEditor().getItem().toString();
        File dirOrFile = getFile(f);
        if (dirOrFile.isDirectory()) {
            fileChooser.setCurrentDirectory(dirOrFile);
        } else {
            fileChooser.setSelectedFile(dirOrFile);
        }
        int r = fileChooser.showDialog(CSVFilesHistoryPanel.this, "Save");
        if (r == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file.exists() && file.isDirectory()) {
                JOptionPane.showMessageDialog(this, "Error: Please specify "
                        + "a file, not a directory.");
                return null;
            }
            return file.getAbsolutePath();
        }
        return null;
    }

    /**
     * Get currently selected file.
     *
     * @return the current file url
     * @see javax.swing.JComboBox#getSelectedItem()
     */
    public String getSelectedFile() {
        return ((JTextField) m_textBox.getEditor().getEditorComponent()).getText();
    }

    /**
     * Set the file url as default.
     *
     * @param url the file to choose
     * @see javax.swing.JComboBox#setSelectedItem(java.lang.Object)
     */
    public void setSelectedFile(final String url) {
        m_textBox.setSelectedItem(url);
    }

    /** Updates the elements in the combo box, reads the file history. */
    public void updateHistory() {
        StringHistory history =
                StringHistory.getInstance(CSVWriterNodeModel.FILE_HISTORY_ID);
        String[] allVals = history.getHistory();
        LinkedHashSet<String> list = new LinkedHashSet<String>();
        for (String s : allVals) {
            list.add(s);
        }
        DefaultComboBoxModel<String> comboModel =
                (DefaultComboBoxModel<String>)m_textBox.getModel();
        comboModel.removeAllElements();
        for (String s : list) {
            comboModel.addElement(s);
        }
        // changing the model will also change the minimum size to be
        // quite big. We have a tooltip, we don't need that
        Dimension newMin = new Dimension(0, getPreferredSize().height);
        setMinimumSize(newMin);
    }

    /**
     * Return a file object for the given fileName. It makes sure that if the
     * fileName is not absolute it will be relative to the user's home dir.
     *
     * @param fileName the file name to convert to a file
     * @return a file representing fileName
     */
    public static File getFile(final String fileName) {
        File f = new File(fileName);
        if (!f.isAbsolute()) {
            f = new File(new File(System.getProperty("user.home")), fileName);
        }
        return f;
    }

    /**
     * Adds a change listener to the panel that gets notified whenever the entered file name changes.
     *
     * @param cl a change listener
     * @since 2.11
     */
    public void addChangeListener(final ChangeListener cl) {
        ((JTextField) m_textBox.getEditor().getEditorComponent()).addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(final KeyEvent e) {
            }

            @Override
            public void keyReleased(final KeyEvent e) {
                cl.stateChanged(new ChangeEvent(e.getSource()));
            }

            @Override
            public void keyPressed(final KeyEvent e) {
            }
        });
        m_textBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                cl.stateChanged(new ChangeEvent(e.getSource()));
            }
        });
    }
}
