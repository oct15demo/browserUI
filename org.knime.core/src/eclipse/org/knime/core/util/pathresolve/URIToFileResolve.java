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
 */
package org.knime.core.util.pathresolve;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A service interface to convert a URI into a local file. The URI is usually
 * (always?) either a file URI or a URI pointing into the KNIME TeamSpace (also file
 * based), e.g. "knime:/MOUNT_ID/some/path/workflow.knime".
 *
 * <p>
 * This interface is used to resolve URIs that are stored as part of referenced
 * metanode templates. It is not meant to be implemented by third-party plug-ins.
 *
 * @author Bernd Wiswedel, KNIME.com, Zurich, Switzerland
 * @noimplement
 */
public interface URIToFileResolve {
    /**
     * Resolves the given URI into a local file. If the URI doesn't denote a local file, <code>null</code> is returned.
     *
     * @param uri The URI, e.g. "knime:/MOUNT_ID/some/path/workflow.knime"
     * @return the local file represented by the URI or <code>null</code>
     * @throws IOException If the URI can't be resolved
     */
    public File resolveToFile(final URI uri) throws IOException;

    /**
     * Resolves the given URI into a local file. If the URI doesn't denote a local file, <code>null</code> is returned.
     * @param uri The URI, e.g. "knime:/MOUNT_ID/some/path/workflow.knime"
     * @param monitor a progress monitor, must not be <code>null</code>
     * @return the local file represented by the URI or <code>null</code>
     * @throws IOException If the URI can't be resolved
     * @since 2.6
     */
    public File resolveToFile(final URI uri, IProgressMonitor monitor) throws IOException;

    /**
     * Resolves the given URI into a local file. If the URI does not represent a
     * local file (e.g. a remote file on a server) it is downloaded first to a
     * temporary directory and the the temporary copy is returned. If it
     * represents a local file the behavior is the same as in
     * {@link #resolveToFile(URI)}.
     *
     * @param uri The URI, e.g. "knime:/MOUNT_ID/some/path/workflow.knime"
     * @return the file represented by the URI or a temporary copy of that file
     *         if it represents a remote file
     * @throws IOException If the URI can't be resolved
     */
    public File resolveToLocalOrTempFile(final URI uri) throws IOException;

    /**
     * Resolves the given URI into a local file. If the URI does not represent a
     * local file (e.g. a remote file on a server) it is downloaded first to a
     * temporary directory and the the temporary copy is returned. If it
     * represents a local file the behavior is the same as in
     * {@link #resolveToFile(URI)}.
     *
     * @param uri The URI, e.g. "knime:/MOUNT_ID/some/path/workflow.knime"
     * @param monitor a progress monitor, must not be <code>null</code>
     * @return the file represented by the URI or a temporary copy of that file
     *         if it represents a remote file
     * @throws IOException If the URI can't be resolved
     * @since 2.6
     */
    public File resolveToLocalOrTempFile(final URI uri, IProgressMonitor monitor) throws IOException;

    /**
     * Returns <code>true</code>, if this is a URI that is relative to the current mountpoint (of the flow it is used
     * in). It can only be resolved in the context of a flow. Contains the corresponding keyword as host.
     *
     * @param uri to check
     * @return <code>true</code> if argument URI is mount point relative, <code>false</code> if not.
     * @since 2.8
     */
    public boolean isMountpointRelative(final URI uri);

    /**
     * Returns <code>true</code>, if this is a URI that is relative to the workflow it is used in. It can only be
     * resolved in the context of a flow. Contains the corresponding keyword as host.
     *
     * @param uri to check
     * @return <code>true</code> if argument URI is workflow relative, <code>false</code> if not.
     * @since 2.8
     */
    public boolean isWorkflowRelative(final URI uri);

    /**
     * Returns <code>true</code>, if this is a URI that is relative to the node it is used in. It can only be
     * resolved in the context of a flow. Contains the corresponding keyword as host.
     *
     * @param uri to check
     * @return <code>true</code> if argument URI is node relative, <code>false</code> if not.
     * @since 2.10
     */
    public boolean isNodeRelative(final URI uri);
}
