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
 * Created on Apr 16, 2013 by Berthold
 */
package org.knime.core.node.interactive;

import org.knime.core.node.web.ValidationError;
import org.knime.core.node.web.WebViewContent;




/** Interface for NodeModels that support interactive views and repeated
 * execution when the view has been modified by the user.
 *
 * @author B. Wiswedel, Th. Gabriel, M. Berthold
 * @param <REP> The concrete class of the {@link WebViewContent} acting as representation of the view.
 * @param <VAL> The concrete class of the {@link WebViewContent} acting as value of the view.
 * @since 2.8
 */
public interface InteractiveNode<REP extends ViewContent, VAL extends ViewContent> {

    /**
     * Create content which can be used by the interactive view implementation.
     * @return View representation implementation required for the interactive view.
     * @since 2.10
     */
    REP getViewRepresentation();

    /**
     * @return View value implementation required for the interactive view.
     * @since 2.10
     */
    VAL getViewValue();

    /**
     * @param viewContent The view content to load.
     * @return error or null if OK.
     * @since 2.10
     */
    ValidationError validateViewValue(VAL viewContent);

    /**
     * @param viewContent The view content to load.
     * @param useAsDefault True if node settings are to be updated by view content.
     * @since 2.10
     */
    void loadViewValue(VAL viewContent, boolean useAsDefault);

}
