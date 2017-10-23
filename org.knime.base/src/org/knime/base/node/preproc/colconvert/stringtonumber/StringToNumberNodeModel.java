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
 * --------------------------------------------------------------------
 *
 * History
 *   03.07.2007 (cebron): created
 */
package org.knime.base.node.preproc.colconvert.stringtonumber;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.LongCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionWithInternalsNodeModel;
import org.knime.core.node.streamable.simple.SimpleStreamableOperatorInternals;

/**
 * The NodeModel for the String to Number Node that converts strings to numbers.
 *
 * @author cebron, University of Konstanz
 */
public class StringToNumberNodeModel extends SimpleStreamableFunctionWithInternalsNodeModel<SimpleStreamableOperatorInternals> {

    /**
     * The possible types that the string can be converted to.
     */
    public static final DataType[] POSSIBLETYPES =
        new DataType[]{DoubleCell.TYPE, IntCell.TYPE, LongCell.TYPE};

    /* Node Logger of this class. */
    private static final NodeLogger LOGGER =
            NodeLogger.getLogger(StringToNumberNodeModel.class);

    /*
     * Config key for the operator internals to propagate error messages.
     */
    private static final String CFG_KEY_ERROR_MESSAGES = "error_message";

    /**
     * Key for the included columns in the NodeSettings.
     */
    public static final String CFG_INCLUDED_COLUMNS = "include";

    /**
     * Key for the decimal separator in the NodeSettings.
     */
    public static final String CFG_DECIMALSEP = "decimal_separator";

    /**
     * Key for the thousands separator in the NodeSettings.
     */
    public static final String CFG_THOUSANDSSEP = "thousands_separator";

    /**
     * Key for the parsing type in the NodeSettings.
     */
    public static final String CFG_PARSETYPE = "parse_type";

    /**
     * Key for parsing with optional trailing {@code d}, {@code f}, {@code l}.
     * @since 2.12
     */
    public static final String CFG_GENERIC_PARSE = "generic_parse";

    /**
     * The default decimal separator.
     */
    public static final String DEFAULT_DECIMAL_SEPARATOR = ".";

    /**
     * The default thousands separator.
     */
    public static final String DEFAULT_THOUSANDS_SEPARATOR = "";

    /**
     * By default do not accept type suffices.
     * @since 2.12
     */
    public static final boolean DEFAULT_GENERIC_PARSE = false;
    /** For compatibility reasons accept type suffices. */
    static final boolean COMPAT_GENERIC_PARSE = true;

    /*
     * The included columns.
     */
    private SettingsModelFilterString m_inclCols =
            new SettingsModelFilterString(CFG_INCLUDED_COLUMNS);

    /*
     * The decimal separator
     */
    private String m_decimalSep = DEFAULT_DECIMAL_SEPARATOR;

    /*
     * The thousands separator
     */
    private String m_thousandsSep = DEFAULT_THOUSANDS_SEPARATOR;

    private DataType m_parseType = POSSIBLETYPES[0];

    private boolean m_genericParse = DEFAULT_GENERIC_PARSE;

    /**
     * Constructor with one inport and one outport.
     */
    public StringToNumberNodeModel() {
        super(SimpleStreamableOperatorInternals.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        ColumnRearranger colre = createColumnRearranger(inSpecs[0]);
        DataTableSpec newspec = colre.createSpec();
        return new DataTableSpec[]{newspec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        DataTableSpec inspec = inData[0].getDataTableSpec();
        List<String> inclcols = m_inclCols.getIncludeList();
        if (inclcols.size() == 0) {
            // nothing to convert, let's return the input table.
            setWarningMessage("No columns selected,"
                    + " returning input DataTable.");
            return new BufferedDataTable[]{inData[0]};
        }

        SimpleStreamableOperatorInternals internals = createStreamingOperatorInternals();
        ColumnRearranger colre = createColumnRearranger(inspec, internals);

        BufferedDataTable resultTable =
                exec.createColumnRearrangeTable(inData[0], colre, exec);

        warningMessage(internals);

        return new BufferedDataTable[]{resultTable};
    }

    private void warningMessage(final SimpleStreamableOperatorInternals internals) {
        String errorMessage;
        try {
            errorMessage = internals.getConfig().getString(CFG_KEY_ERROR_MESSAGES);
        } catch (InvalidSettingsException e) {
            // if no warning message has been set
            return;
        }
        StringBuilder warnings = new StringBuilder();
        if (errorMessage.length() > 0) {
            warnings.append("Problems occurred, see Console messages.\n");
        }
        if (warnings.length() > 0) {
            LOGGER.warn(errorMessage);
            setWarningMessage(warnings.toString().replaceAll("[\r\n]+$", ""));
        }
    }

    /**
     * {@inheritDoc}
     * @since 3.1
     */
    @Override
    protected ColumnRearranger createColumnRearranger(final DataTableSpec spec,
        final SimpleStreamableOperatorInternals emptyInternals) throws InvalidSettingsException {
        int[] indices = findColumnIndices(spec);
        ConverterFactory converterFac = new ConverterFactory(indices, spec, m_parseType, emptyInternals);
        ColumnRearranger colre = new ColumnRearranger(spec);
        List<String> inclcols = m_inclCols.getIncludeList();
        if (inclcols.size() == 0) {
            // nothing to convert, let's return the input table.
            emptyInternals.getConfig().addString(CFG_KEY_ERROR_MESSAGES,
                "No columns selected, returning input DataTable.");
            return colre;
        }
        colre.replace(converterFac, indices);
        return colre;
    }

	private int[] findColumnIndices(final DataTableSpec spec)
			throws InvalidSettingsException {
		List<String> inclcols = m_inclCols.getIncludeList();
		StringBuilder warnings = new StringBuilder();
		if (inclcols.size() == 0) {
			warnings.append("No columns selected");
		}
		Vector<Integer> indicesvec = new Vector<Integer>();
		if (m_inclCols.isKeepAllSelected()) {
			for (DataColumnSpec cspec : spec) {
				if (cspec.getType().isCompatible(StringValue.class)) {
					indicesvec.add(spec.findColumnIndex(cspec.getName()));
				}
			}
		} else {
			for (int i = 0; i < inclcols.size(); i++) {
				int colIndex = spec.findColumnIndex(inclcols.get(i));
				if (colIndex >= 0) {
					DataType type = spec.getColumnSpec(colIndex).getType();
					if (type.isCompatible(StringValue.class)) {
						indicesvec.add(colIndex);
					} else {
						warnings.append("Ignoring column \""
								+ spec.getColumnSpec(colIndex).getName()
								+ "\"\n");
					}
				} else {
					throw new InvalidSettingsException("Column \""
							+ inclcols.get(i) + "\" not found.");
				}
			}
		}
		if (warnings.length() > 0) {
			setWarningMessage(warnings.toString());
		}
		int[] indices = new int[indicesvec.size()];
		for (int i = 0; i < indices.length; i++) {
			indices[i] = indicesvec.get(i);
		}
		return indices;
	}

	/**
     * {@inheritDoc}
     * @since 3.1
     */
    @Override
    protected SimpleStreamableOperatorInternals
        mergeStreamingOperatorInternals(final SimpleStreamableOperatorInternals[] operatorInternals) {
        StringBuilder errorMessages = new StringBuilder();
        for (int i = 0; i < operatorInternals.length; i++) {
            try {
                errorMessages.append(operatorInternals[i].getConfig().getString(CFG_KEY_ERROR_MESSAGES));
                errorMessages.append("\n");
            } catch (InvalidSettingsException e) {
              //if no warning message has been set -> nothing to do
            }
        }
        SimpleStreamableOperatorInternals res = new SimpleStreamableOperatorInternals();
        res.getConfig().addString(CFG_KEY_ERROR_MESSAGES, errorMessages.toString().trim());
        return res;
    }

    /**
     * {@inheritDoc}
     * @since 3.1
     */
    @Override
    protected void finishStreamableExecution(final SimpleStreamableOperatorInternals operatorInternals) {
        warningMessage(operatorInternals);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // empty
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_inclCols.loadSettingsFrom(settings);
        m_decimalSep =
                settings.getString(CFG_DECIMALSEP, DEFAULT_DECIMAL_SEPARATOR);
        m_thousandsSep =
                settings.getString(CFG_THOUSANDSSEP,
                        DEFAULT_THOUSANDS_SEPARATOR);
        m_parseType = settings.getDataType(CFG_PARSETYPE, POSSIBLETYPES[0]);
        // added in 2.12
        m_genericParse = settings.getBoolean(CFG_GENERIC_PARSE, COMPAT_GENERIC_PARSE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_inclCols.saveSettingsTo(settings);
        settings.addString(CFG_DECIMALSEP, m_decimalSep);
        settings.addString(CFG_THOUSANDSSEP, m_thousandsSep);
        settings.addDataType(CFG_PARSETYPE, m_parseType);
        // added in 2.12
        settings.addBoolean(CFG_GENERIC_PARSE, m_genericParse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_inclCols.validateSettings(settings);
        String decimalsep =
                settings.getString(CFG_DECIMALSEP, DEFAULT_DECIMAL_SEPARATOR);
        String thousandssep =
                settings.getString(CFG_THOUSANDSSEP,
                        DEFAULT_THOUSANDS_SEPARATOR);
        if (decimalsep == null || thousandssep == null) {
            throw new InvalidSettingsException("Separators must not be null");
        }
        if (decimalsep.length() > 1 || thousandssep.length() > 1) {
            throw new InvalidSettingsException(
                    "Illegal separator length, expected a single character");
        }

        if (decimalsep.equals(thousandssep)) {
            throw new InvalidSettingsException(
                    "Decimal and thousands separator must not be the same.");
        }
        DataType myType = settings.getDataType(CFG_PARSETYPE, POSSIBLETYPES[0]);
        boolean found = false;
        for (DataType type : POSSIBLETYPES) {
            if (type.equals(myType)) {
                found = true;
            }
        }
        if (!found) {
            throw new InvalidSettingsException("Illegal parse type: " + myType);
        }

        //No need to check generic parse.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // empty.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // empty.
    }

    /**
     * The CellFactory to produce the new converted cells.
     *
     * @author cebron, University of Konstanz
     */
    private class ConverterFactory extends AbstractCellFactory {

        /*
         * Column indices to use.
         */
        private int[] m_colindices;

        /*
         * Original DataTableSpec.
         */
        private DataTableSpec m_spec;

        /*
         * Error messages.
         */
        private String m_error;

        /** Number of parsing errors. */
        private int m_parseErrorCount;

        private DataType m_type;

        /* streamable operator internals to propagate the error messages */
        private SimpleStreamableOperatorInternals m_internals;

        /**
         *
         * @param colindices the column indices to use.
         * @param spec the original DataTableSpec.
         * @param type the {@link DataType} to convert to.
         */
        ConverterFactory(final int[] colindices, final DataTableSpec spec,
                final DataType type, final SimpleStreamableOperatorInternals internals) {
            m_colindices = colindices;
            m_spec = spec;
            m_type = type;
            m_parseErrorCount = 0;
            m_internals = internals;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DataCell[] getCells(final DataRow row) {
            DataCell[] newcells = new DataCell[m_colindices.length];
            for (int i = 0; i < newcells.length; i++) {
                DataCell dc = row.getCell(m_colindices[i]);
                // should be a DoubleCell, otherwise copy original cell.
                if (!dc.isMissing()) {
                    final String s = ((StringValue)dc).getStringValue();
                    if (s.trim().length() == 0) {
                        newcells[i] = DataType.getMissingCell();
                        continue;
                    }
                    try {
                        String corrected = s;
                        if (m_thousandsSep != null
                                && m_thousandsSep.length() > 0) {
                            // remove thousands separator
                            corrected = s.replaceAll(
                                    Pattern.quote(m_thousandsSep),
                                    "");
                        }
                        if (!".".equals(m_decimalSep)) {
                            if (corrected.contains(".")) {
                                throw new NumberFormatException(
                                        "Invalid floating point number");
                            }
                            if (m_decimalSep != null
                                    && m_decimalSep.length() > 0) {
                                // replace custom separator with standard
                                corrected =
                                        corrected.replaceAll(Pattern
                                                .quote(m_decimalSep), ".");
                            }
                        }

                        if (!m_genericParse) {
                            corrected = check(corrected);
                        }
                        if (m_type.equals(DoubleCell.TYPE)) {
                            double parsedDouble = Double.parseDouble(corrected);
                            newcells[i] = new DoubleCell(parsedDouble);
                        } else if (m_type.equals(IntCell.TYPE)) {
                            int parsedInteger = Integer.parseInt(corrected);
                            newcells[i] = new IntCell(parsedInteger);
                        } else if (m_type.equals(LongCell.TYPE)) {
                            long parsedLong = Long.parseLong(corrected);
                            newcells[i] = new LongCell(parsedLong);
                        } else {
                            m_error = "No valid parse type.";
                        }
                    } catch (NumberFormatException e) {
                        if (m_parseErrorCount == 0) {
                            m_error =
                                    "'" + s + "' (RowKey: "
                                            + row.getKey().toString()
                                            + ", Position: " + m_colindices[i]
                                            + ")";
                            LOGGER.debug(e.getMessage());
                        }
                        m_parseErrorCount++;
                        newcells[i] = DataType.getMissingCell();
                    }
                } else {
                    newcells[i] = DataType.getMissingCell();
                }
            }
            return newcells;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DataColumnSpec[] getColumnSpecs() {
            DataColumnSpec[] newcolspecs =
                    new DataColumnSpec[m_colindices.length];
            for (int i = 0; i < newcolspecs.length; i++) {
                DataColumnSpec colspec = m_spec.getColumnSpec(m_colindices[i]);
                DataColumnSpecCreator colspeccreator = null;
                if (m_type.equals(DoubleCell.TYPE)) {
                    // change DataType to DoubleCell
                    colspeccreator =
                            new DataColumnSpecCreator(colspec.getName(),
                                    DoubleCell.TYPE);
                } else if (m_type.equals(IntCell.TYPE)) {
                    // change DataType to IntCell
                    colspeccreator =
                            new DataColumnSpecCreator(colspec.getName(),
                                    IntCell.TYPE);
                } else if (m_type.equals(LongCell.TYPE)) {
                    // change DataType to LongCell
                    colspeccreator =
                            new DataColumnSpecCreator(colspec.getName(),
                                LongCell.TYPE);
                } else {
                    colspeccreator =
                            new DataColumnSpecCreator("Invalid parse mode",
                                    DataType.getMissingCell().getType());
                }
                newcolspecs[i] = colspeccreator.createSpec();
            }
            return newcolspecs;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void afterProcessing() {
            String message;
            switch (m_parseErrorCount) {
                case 0:
                    message = "";
                    break;
                case 1:
                    message = "Could not parse cell with value " + m_error;
                    break;
                default:
                    message = "Values in " + m_parseErrorCount
                            + " cells could not be parsed, first error: " + m_error;
            }
            m_internals.getConfig().addString(CFG_KEY_ERROR_MESSAGES, message);
        }



    } // end ConverterFactory

    /**
     * @param corrected A potential number as a (non-empty){@link String}.
     * @return The original value.
     * @throws NumberFormatException when the value ends in {@code d} or {@code f}.
     * @since 2.12
     */
    public static String check(final String corrected) {
        char c = Character.toLowerCase(corrected.charAt(corrected.length() - 1));
        switch (c) {
            //case 'l': //int/long do not parse with l suffix, nor double
            case 'd': //intentional fall-through
            case 'f':
                throw new NumberFormatException(corrected + " is invalid because of its suffix.");
            default:
            return corrected;
        }
    }
}
