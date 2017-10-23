/*
 *
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
 *   Mar 15, 2010 (morent): created
 */
package org.knime.core.node.port.pmml.preproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;

/**
 * Dummy singleton port object instance.
 * TODO: It should be investigated which
 * information can be provided to successor nodes before execution, e.g.
 * effected columns for column based operations.
 * @author Dominik Morent, KNIME.com, Zurich, Switzerland
 */
public class PMMLPreprocPortObjectSpec implements PortObjectSpec {
    private static final String COLUMN_NAMES_KEY = "columnNames";
    private static final String COLUMNS_KEY = "columns";
    private final List<String> m_columnNames;

    /**
     * @param columnNames the names of the preprocessed columns
     */
    public PMMLPreprocPortObjectSpec(final List<String> columnNames) {
        super();
        m_columnNames = columnNames;
    }

    /**
     * @param columnNames the names of the preprocessed columns
     */
    public PMMLPreprocPortObjectSpec(final String ... columnNames) {
        super();
        m_columnNames = new ArrayList<String>(Arrays.asList(columnNames));
    }

    /**
     * @return the names of the preprocessed columns
     */
    public List<String> getColumnNames() {
        return m_columnNames;
    }

    /**
     * @noreference This class is not intended to be referenced by clients.
     * @since 3.0
     */
    public static final class Serializer extends PortObjectSpecSerializer<PMMLPreprocPortObjectSpec> {
        /**
         * {@inheritDoc}
         */
        @Override
        public void savePortObjectSpec(
                final PMMLPreprocPortObjectSpec portObjectSpec,
                final PortObjectSpecZipOutputStream out)
                throws IOException {
            portObjectSpec.saveTo(out);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public PMMLPreprocPortObjectSpec loadPortObjectSpec(
                final PortObjectSpecZipInputStream in) throws IOException {
            try {
                return PMMLPreprocPortObjectSpec.loadFrom(in);
            } catch (InvalidSettingsException e) {
                throw new IOException(e);
            }
        }
    }

    /**
    *
    * @param out zipped stream to write the entries to
    * @throws IOException if something goes wrong
    */
   public void saveTo(final PortObjectSpecZipOutputStream out)
           throws IOException {
       NodeSettings settings = new NodeSettings(COLUMN_NAMES_KEY);
       settings.addStringArray(COLUMNS_KEY,
               m_columnNames.toArray(new String[m_columnNames.size()]));
       settings.saveToXML(out);
       out.close();
   }

   /**
   *
   * @param in stream reading the relevant files
   * @return a completely loaded port object spec with {@link DataTableSpec},
   *         and the sets of learning, ignored and target columns.
   * @throws IOException if something goes wrong
   * @throws InvalidSettingsException if something goes wrong
   */
  public static PMMLPreprocPortObjectSpec loadFrom(
          final PortObjectSpecZipInputStream in) throws IOException,
          InvalidSettingsException {
      NodeSettingsRO settings = NodeSettings.loadFromXML(in);
      String[] columnNames = settings.getStringArray(COLUMNS_KEY);
      return new PMMLPreprocPortObjectSpec(Arrays.asList(columnNames));
  }


    /**
     * {@inheritDoc}
     */
    @Override
    public JComponent[] getViews() {
        return null;
    }

}
