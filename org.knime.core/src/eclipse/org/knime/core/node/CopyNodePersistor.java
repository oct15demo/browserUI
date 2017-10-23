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
 *   Aug 13, 2009 (wiswedel): created
 */
package org.knime.core.node;

import org.knime.core.data.filestore.internal.IFileStoreHandler;
import org.knime.core.internal.ReferencedFile;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.workflow.WorkflowPersistor.LoadResult;

/**
 * A persistor cloning a node's settings. It does not retain port objects or
 * node internals. Used by copy&amp;paste and undo.
 *
 * @author Bernd Wiswedel, University of Konstanz
 */
public class CopyNodePersistor implements NodePersistor {

    /** Apply the settings to the new node.
     * @param node the node just created.
     */
    public void loadInto(final Node node) {
        try {
            node.load(this, new ExecutionMonitor(), new LoadResult("ignored"));
        } catch (CanceledExecutionException e) {
            // ignored, can't happen
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConfigured() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDirtyAfterLoad() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExecuted() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void setDirtyAfterLoad() {
    }

    /** {@inheritDoc} */
    @Override
    public PortObject[] getInternalHeldPortObjects() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public ReferencedFile getNodeInternDirectory() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public PortObject getPortObject(final int outportIndex) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public PortObjectSpec getPortObjectSpec(final int outportIndex) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getPortObjectSummary(final int outportIndex) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getWarningMessage() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasContent() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean mustWarnOnDataLoadError() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsResetAfterLoad() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void setNeedsResetAfterLoad() {
    }

    /** {@inheritDoc}
     * @since 2.6*/
    @Override
    public IFileStoreHandler getFileStoreHandler() {
        return null;
    }

}
