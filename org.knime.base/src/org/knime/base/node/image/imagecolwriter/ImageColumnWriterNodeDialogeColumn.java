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
 *   29.10.2010 (meinl): created
 */
package org.knime.base.node.image.imagecolwriter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.image.ImageValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.workflow.FlowVariable.Type;
import org.knime.core.util.FileUtil;

/**
 * This is the dialog for the Image writer.
 *
 * @author Thorsten Meinl, University of Konstanz
 * @author Sebastian Peter, University of Konstanz
 */
public class ImageColumnWriterNodeDialogeColumn extends DefaultNodeSettingsPane {
    @SuppressWarnings("unchecked")
    private final DialogComponentColumnNameSelection m_imageColumn =
            new DialogComponentColumnNameSelection(new SettingsModelString(
                    "imageColumn", null), "Image column", 0, ImageValue.class);

    private final DialogComponentFileChooser m_dirChooser = new DialogComponentFileChooser(
        new SettingsModelString("directory", null), ImageColumnWriterNodeDialogeColumn.class.getName(),
        JFileChooser.SAVE_DIALOG, true, createFlowVariableModel("directory", Type.STRING));

    private final DialogComponentBoolean m_overwrite =
            new DialogComponentBoolean(new SettingsModelBoolean("overwrite",
                    false), "Overwrite existing files");

    /**
     * Creates a new dialog.
     */
    public ImageColumnWriterNodeDialogeColumn() {
        m_dirChooser.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                String selFile = ((SettingsModelString) m_dirChooser.getModel()).getStringValue();
                if ((selFile != null) && !selFile.isEmpty()) {
                    try {
                        URL newUrl = FileUtil.toURL(selFile);
                        Path path = FileUtil.resolveToPath(newUrl);
                        m_overwrite.getModel().setEnabled(path != null);
                    } catch (IOException | URISyntaxException | InvalidPathException ex) {
                        // ignore
                    }
                }
            }
        });
        m_dirChooser.setBorderTitle("Output directory");

        addDialogComponent(m_dirChooser);
        addDialogComponent(m_overwrite);
        addDialogComponent(m_imageColumn);
    }
}
