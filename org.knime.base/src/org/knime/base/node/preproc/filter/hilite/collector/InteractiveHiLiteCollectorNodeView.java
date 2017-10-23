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
 * Created on 16.04.2013 by Christian Albrecht, KNIME.com AG, Zurich, Switzerland
 */
package org.knime.base.node.preproc.filter.hilite.collector;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.knime.core.data.DataTable;
import org.knime.core.node.interactive.DefaultReexecutionCallback;
import org.knime.core.node.interactive.InteractiveClientNodeView;
import org.knime.core.node.property.hilite.HiLiteHandler;
import org.knime.core.node.property.hilite.KeyEvent;
import org.knime.core.node.tableview.TableContentModel;
import org.knime.core.node.tableview.TableView;

/**
 *
 * @author Christian Albrecht, KNIME.com AG, Zurich, Switzerland
 * @since 2.8
 */
public class InteractiveHiLiteCollectorNodeView extends InteractiveClientNodeView<InteractiveHiLiteCollectorNodeModel, InteractiveHiLiteCollectorViewContent, InteractiveHiLiteCollectorViewContent> {

    private final TableView m_table;

    /**
     * @param nodeModel
     */
    protected InteractiveHiLiteCollectorNodeView(final InteractiveHiLiteCollectorNodeModel nodeModel) {
        super(nodeModel);
        //super.setShowNODATALabel(false);
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        p.setBorder(BorderFactory.createTitledBorder(" Append Annotation "));

        final JCheckBox checkBox = new JCheckBox("New Column");

        final JTextField textField = new JTextField();
        textField.setPreferredSize(new Dimension(150,
                Math.max(20, textField.getHeight())));
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final java.awt.event.KeyEvent e) {
                if (e == null) {
                    return;
                }
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    appendAnnotation(
                            textField.getText(), checkBox.isSelected());
                }
            }
        });
        p.add(textField);

        JButton button = new JButton("Apply");
        button.setPreferredSize(new Dimension(100,
                Math.max(25, button.getHeight())));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (e == null) {
                    return;
                }
                appendAnnotation(textField.getText(), checkBox.isSelected());
            }
        });
        p.add(button);
        p.add(checkBox);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(p, BorderLayout.NORTH);
        TableContentModel cview = new TableContentModel() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void hiLite(final KeyEvent e) {
                modelChanged();
            }
            /**
             * {@inheritDoc}
             */
            @Override
            public void unHiLite(final KeyEvent e) {
                modelChanged();
            }
            /**
             * {@inheritDoc}
             */
            @Override
            public void unHiLiteAll(final KeyEvent e) {
                modelChanged();
            }
        };
        m_table = new TableView(cview);
        super.getJMenuBar().add(m_table.createHiLiteMenu());
        m_table.setPreferredSize(new Dimension(425, 250));
        m_table.setShowColorInfo(false);
        panel.add(m_table, BorderLayout.CENTER);
        super.setComponent(panel);
    }

    private void appendAnnotation(final String anno, final boolean newColumn) {
        if (anno != null && !anno.isEmpty()) {
            getNodeModel().appendAnnotation(anno, newColumn);
            //FIXME: Put annotation map in view content
            triggerReExecution(new InteractiveHiLiteCollectorViewContent(), false, new DefaultReexecutionCallback());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {
        DataTable data = super.getNodeModel().getHiLiteAnnotationsTable();
        m_table.setDataTable(data);
        HiLiteHandler hdl = super.getNodeModel().getInHiLiteHandler(0);
        m_table.setHiLiteHandler(hdl);
        m_table.setColumnWidth(50);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateModel(final Object arg) {
        modelChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {
        // TODO Auto-generated method stub

    }

}
