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
 * Created on 30.04.2013 by Christian Albrecht, KNIME.com AG, Zurich, Switzerland
 */
package org.knime.core.node.web;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;

/**
 *
 * @author Christian Albrecht, KNIME.com AG, Zurich, Switzerland
 * @since 2.9
 */
public final class WebResourceLocator {

    /**
     *
     * @author Christian Albrecht, KNIME.com AG, Zurich, Switzerland
     */
    public enum WebResourceType {

        /**
         * Javascript file.
         */
        JAVASCRIPT,

        /**
         * CSS file.
         */
        CSS,

        /**
         * General file or folder to be available.
         */
        FILE
    }

    private final String m_pluginName;
    private final String m_relativePathTarget;
    private final String m_relativePathSource;
    private final WebResourceType m_type;

    public WebResourceLocator(final String pluginName, final String relativePathTarget, final WebResourceType type) {
        this(pluginName, relativePathTarget, relativePathTarget, type);
    }

    /**
     * @param pluginName
     * @param relativePathSource
     * @since 2.10
     *
     */
    public WebResourceLocator(final String pluginName, final String relativePathSource, final String relativePathTarget, final WebResourceType type) {
        m_pluginName = pluginName;
        m_relativePathSource = relativePathSource.startsWith("/") ? relativePathSource : "/" + relativePathSource;
        m_relativePathTarget = relativePathTarget.startsWith("/") ? relativePathTarget : "/" + relativePathTarget;
        m_type = type;
    }

    /**
     * @return the m_pluginName
     */
    public String getPluginName() {
        return m_pluginName;
    }

    /**
     * @return the relativePathTarget
     * @since 2.10
     */
    public String getRelativePathTarget() {
        return m_relativePathTarget;
    }

    /**
     * @return the m_type
     */
    public WebResourceType getType() {
        return m_type;
    }

    /**
     * @noreference This method is not intended to be referenced by clients.
     * @return the {@link File} that is denoted through this locator.
     * @throws IOException if resource cannot be resolved.
     */
    public File getResource() throws IOException {
        URL url = new URL("platform:/plugin/" + m_pluginName);
        File dir = new File(FileLocator.resolve(url).getFile());
        return new File(dir, m_relativePathSource);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "WebResourceLocator [m_pluginName=" + m_pluginName + ", m_relativePathTarget=" + m_relativePathTarget
            + ", m_relativePathSource=" + m_relativePathSource + ", m_type=" + m_type + "]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_pluginName == null) ? 0 : m_pluginName.hashCode());
        result = prime * result + ((m_relativePathSource == null) ? 0 : m_relativePathSource.hashCode());
        result = prime * result + ((m_relativePathTarget == null) ? 0 : m_relativePathTarget.hashCode());
        result = prime * result + ((m_type == null) ? 0 : m_type.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        WebResourceLocator other = (WebResourceLocator)obj;
        if (m_pluginName == null) {
            if (other.m_pluginName != null) {
                return false;
            }
        } else if (!m_pluginName.equals(other.m_pluginName)) {
            return false;
        }
        if (m_relativePathSource == null) {
            if (other.m_relativePathSource != null) {
                return false;
            }
        } else if (!m_relativePathSource.equals(other.m_relativePathSource)) {
            return false;
        }
        if (m_relativePathTarget == null) {
            if (other.m_relativePathTarget != null) {
                return false;
            }
        } else if (!m_relativePathTarget.equals(other.m_relativePathTarget)) {
            return false;
        }
        if (m_type != other.m_type) {
            return false;
        }
        return true;
    }
}
