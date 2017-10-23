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
 *   18.06.2007 (thor): created
 */
package org.knime.base.node.preproc.stringreplacer;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.knime.base.util.WildcardMatcher;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;

/**
 * This is the model for the string replacer node that does the work.
 *
 * @author Thorsten Meinl, University of Konstanz
 */
public class StringReplacerNodeModel extends SimpleStreamableFunctionNodeModel {
    private final StringReplacerSettings m_settings =
            new StringReplacerSettings();


    /**
     * Creates the column rearranger that computes the new cells.
     *
     * @param spec the spec of the input table
     * @return a column rearranger
     */
    @Override
    protected ColumnRearranger createColumnRearranger(final DataTableSpec spec) throws InvalidSettingsException {
        final Pattern pattern = createPattern(m_settings);

        DataColumnSpec colSpec;
        if (m_settings.createNewColumn()) {
            colSpec = new DataColumnSpecCreator(m_settings.newColumnName(), StringCell.TYPE).createSpec();
        } else {
            colSpec = new DataColumnSpecCreator(m_settings.columnName(), StringCell.TYPE).createSpec();
        }

        final String replacement;
        if (m_settings.patternIsRegex()) {
            replacement = m_settings.replacement();
        } else {
            replacement = m_settings.replacement().replaceAll("(\\$\\d+)", "\\\\$1");
        }
        final int index = spec.findColumnIndex(m_settings.columnName());
        SingleCellFactory cf = new SingleCellFactory(colSpec) {
            @Override
            public DataCell getCell(final DataRow row) {
                DataCell cell = row.getCell(index);
                if (cell.isMissing()) {
                    return cell;
                }

                final String stringValue = ((StringValue)cell).getStringValue();
                Matcher m = pattern.matcher(stringValue);
                if (m_settings.replaceAllOccurrences()) {
                    return new StringCell(m.replaceAll(replacement));
                } else if (m.matches()) {
                    if (".*".equals(pattern.pattern())) {
                        // .* matches twice, first for the empty string and then for the whole string
                        // therefore the replacement value is doubled
                        return new StringCell(replacement);
                    } else {
                        return new StringCell(m.replaceAll(replacement));
                    }
                } else {
                    return new StringCell(stringValue);
                }
            }
        };

        ColumnRearranger crea = new ColumnRearranger(spec);
        if (m_settings.createNewColumn()) {
            if (spec.containsName(m_settings.newColumnName())) {
                throw new InvalidSettingsException("Duplicate column name: "
                        + m_settings.newColumnName());
            }
            crea.append(cf);
        } else {
            crea.replace(cf, m_settings.columnName());
        }

        return crea;
    }

    private static Pattern createPattern(final StringReplacerSettings settings) {
        String regex;
        int flags = 0;
        if (settings.patternIsRegex()) {
            regex = settings.pattern();
        } else {
            regex = WildcardMatcher.wildcardToRegex(settings.pattern(), settings.enableEscaping());
            flags = Pattern.DOTALL | Pattern.MULTILINE;
        }
        // support for \n and international characters
        if (!settings.caseSensitive()) {
            flags |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        }
        return Pattern.compile(regex, flags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        if (inSpecs[0].findColumnIndex(m_settings.columnName()) == -1) {
            throw new InvalidSettingsException("Selected column '"
                    + m_settings.columnName()
                    + "' does not exist in input table");
        }

        ColumnRearranger crea = createColumnRearranger(inSpecs[0]);
        return new DataTableSpec[]{crea.createSpec()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        exec.setMessage("Searching & Replacing");
        ColumnRearranger crea = createColumnRearranger(inData[0].getDataTableSpec());
        return new BufferedDataTable[]{exec.createColumnRearrangeTable(inData[0], crea, exec)};
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_settings.loadSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_settings.saveSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        StringReplacerSettings s = new StringReplacerSettings();
        s.loadSettings(settings);

        if (s.createNewColumn() && (s.newColumnName() == null || s.newColumnName().trim().length() == 0)) {
            throw new InvalidSettingsException("No name for the new column given");
        }
        if (s.columnName() == null) {
            throw new InvalidSettingsException("No column selected");
        }
        if (s.pattern() == null) {
            throw new InvalidSettingsException("No pattern given");
        }
        if (s.replacement() == null) {
            throw new InvalidSettingsException("No replacement string given");
        }

        // check if the '*' quantifier occurs in the regex, but not the escaped quantifier \*
        if (s.replaceAllOccurrences() && s.patternIsRegex() && s.pattern().matches(".*[^\\\\](?!\\\\)\\*.*")) {
            throw new InvalidSettingsException(
                    "'*' is not allowed when all occurrences of the "
                            + "pattern should be replaced");
        }
        createPattern(s);
    }
}
