/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
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
 *   11.11.2014 (thor): created
 */
package org.knime.core.util;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

/**
 * Wrapper around native C library functions.
 *
 * @author Thorsten Meinl, KNIME.com, Zurich, Switzerland
 * @since 2.11
 */
public abstract class CLibrary  {
    /**
     * Sets an environment variable.
     *
     * @param env the variable's name
     * @param value the variable's value
     */
    public abstract void setenv(String env, String value);

    static class UnixCLibrary extends CLibrary {
        static final UnixCLibrary INSTANCE = new UnixCLibrary();

        private interface UnixCLibraryWrapper extends Library {
            static final UnixCLibraryWrapper NATIVE_INSTANCE = (UnixCLibraryWrapper)
                    Native.loadLibrary("c", UnixCLibraryWrapper.class);
            void setenv(String env, String value, int replace);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void setenv(final String env, final String value) {
            UnixCLibraryWrapper.NATIVE_INSTANCE.setenv(env, value, 1);
        }
    }

    static class WindowsCLibrary extends CLibrary {
        static final WindowsCLibrary INSTANCE = new WindowsCLibrary();

        private interface WindowsCLibraryWrapper extends Library {
            static final WindowsCLibraryWrapper NATIVE_INSTANCE = (WindowsCLibraryWrapper)
                    Native.loadLibrary("msvcrt", WindowsCLibraryWrapper.class);
            int _putenv(String name);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setenv(final String env, final String value) {
            WindowsCLibraryWrapper.NATIVE_INSTANCE._putenv(env + "=" + value);
        }
    }


    /**
     * Returns an instance for this C library wrapper.
     *
     * @return an instance
     */
    public static CLibrary getInstance() {
        return Platform.isWindows() ? WindowsCLibrary.INSTANCE : UnixCLibrary.INSTANCE;
    }
}