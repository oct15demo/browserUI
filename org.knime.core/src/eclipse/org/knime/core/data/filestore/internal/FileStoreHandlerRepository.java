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
 * ------------------------------------------------------------------------
 *
 * History
 *   Jul 2, 2012 (wiswedel): created
 */
package org.knime.core.data.filestore.internal;

import java.util.UUID;

/** Lookup for file store handlers. A workflow (manager) has a {@link WorkflowFileStoreHandlerRepository} as
 * member that is passed to nodes and tables to lookup file stores.
 * If run outside the usual execution mode, we use a {@link NotInWorkflowFileStoreHandlerRepository},
 * which most of the times only throws exceptions when asked for a file store.
 *
 * @author Bernd Wiswedel, KNIME.com, Zurich, Switzerland
 */
public abstract class FileStoreHandlerRepository {

    /** Get handler to ID (which is part of a saved data stream)
     * @param storeHandlerUUID The handler ID.
     * @return The handler for the id, never null. */
    public abstract IFileStoreHandler getHandler(final UUID storeHandlerUUID);

    /** Get handler to ID (which is part of a saved data stream). Throws exception when ID
     * is unknown, returns never <code>null</code>.
     * @param storeHandlerUUID The handler ID.
     * @return The handler for the id, never null. */
    public abstract IFileStoreHandler getHandlerNotNull(final UUID storeHandlerUUID);

    abstract void printValidFileStoreHandlersToLogDebug();

    /**
     * @param writableFileStoreHandler */
    public abstract void removeFileStoreHandler(final IWriteFileStoreHandler writableFileStoreHandler);

    /**
     * @param writableFileStoreHandler */
    public abstract void addFileStoreHandler(IWriteFileStoreHandler writableFileStoreHandler);
}
