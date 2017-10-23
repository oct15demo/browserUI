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
 */
package org.knime.base.node.preproc.valcount;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValueComparator;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.property.hilite.DefaultHiLiteMapper;
import org.knime.core.node.property.hilite.HiLiteHandler;
import org.knime.core.node.property.hilite.HiLiteTranslator;
import org.knime.core.util.MutableInteger;

/**
 * This is the model for the value counter node that does all the work.
 *
 * @author Thorsten Meinl, University of Konstanz
 */
public class ValueCounterNodeModel extends NodeModel {
    private final ValueCounterSettings m_settings = new ValueCounterSettings();

    private static final DataColumnSpec COL_SPEC =
            new DataColumnSpecCreator("count", IntCell.TYPE).createSpec();

    private static final DataTableSpec TABLE_SPEC = new DataTableSpec(COL_SPEC);

    private final HiLiteTranslator m_translator = new HiLiteTranslator();

    /**
     * Creates a new value counter model.
     */
    public ValueCounterNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        String colName = m_settings.columnName();
        if (colName == null) {
            throw new InvalidSettingsException("No column selected");
        }
        int index = inSpecs[0].findColumnIndex(colName);
        if (index == -1) {
            if (inSpecs[0].getNumColumns() == 1) {
                index = 0;
                m_settings.columnName(inSpecs[0].getColumnSpec(0).getName());
            } else {
                throw new InvalidSettingsException("Column '" + colName
                        + "' does not exist");
            }
        }

        return new DataTableSpec[]{TABLE_SPEC};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        final int colIndex =
                inData[0].getDataTableSpec().findColumnIndex(
                        m_settings.columnName());
        final double max = inData[0].getRowCount();
        int rowCount = 0;
        Map<DataCell, Set<RowKey>> hlMap =
                new HashMap<DataCell, Set<RowKey>>();
        Map<DataCell, MutableInteger> countMap =
            new HashMap<DataCell, MutableInteger>();

        for (DataRow row : inData[0]) {
            exec.checkCanceled();
            exec.setProgress(rowCount++ / max, countMap.size()
                    + " different values found");
            DataCell cell = row.getCell(colIndex);

            MutableInteger count = countMap.get(cell);
            if (count == null) {
                count = new MutableInteger(0);
                countMap.put(cell, count);
            }
            count.inc();

            if (m_settings.hiliting()) {
                Set<RowKey> s = hlMap.get(cell);
                if (s == null) {
                    s = new HashSet<RowKey>();
                    hlMap.put(cell, s);
                }
                s.add(row.getKey());
            }
        }

        final DataValueComparator comp =
                inData[0].getDataTableSpec().getColumnSpec(colIndex).getType()
                        .getComparator();

        List<Map.Entry<DataCell, MutableInteger>> sorted =
                new ArrayList<Map.Entry<DataCell, MutableInteger>>(countMap
                        .entrySet());
        Collections.sort(sorted,
                new Comparator<Map.Entry<DataCell, MutableInteger>>() {
                    public int compare(
                            final Map.Entry<DataCell, MutableInteger> o1,
                            final Entry<DataCell, MutableInteger> o2) {
                        return comp.compare(o1.getKey(), o2.getKey());
                    }
                });

        BufferedDataContainer cont = exec.createDataContainer(TABLE_SPEC);
        for (Map.Entry<DataCell, MutableInteger> entry : sorted) {
            RowKey newKey = new RowKey(entry.getKey().toString());
            cont.addRowToTable(new DefaultRow(newKey,
                    new int[]{entry.getValue().intValue()}));
        }
        cont.close();

        if (m_settings.hiliting()) {
            Map<RowKey, Set<RowKey>> temp = new HashMap<RowKey, Set<RowKey>>();
            for (Map.Entry<DataCell, Set<RowKey>> entry : hlMap.entrySet()) {
                RowKey newKey = new RowKey(entry.getKey().toString());
                temp.put(newKey, entry.getValue());
            }
            m_translator.setMapper(new DefaultHiLiteMapper(temp));
        }
        return new BufferedDataTable[]{cont.getTable()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        File f = new File(nodeInternDir, "Hiliting.conf.gz");
        if (f.exists() && f.canRead()) {
            InputStream in = new GZIPInputStream(new BufferedInputStream(
                    new FileInputStream(f)));
            NodeSettingsRO s = NodeSettings.loadFromXML(in);
            in.close();
            try {
                m_translator.setMapper(DefaultHiLiteMapper.load(s));
            } catch (InvalidSettingsException ex) {
                throw new IOException(ex);
            }
        }
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
        m_translator.setMapper(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        if (m_settings.hiliting()) {
            NodeSettings s = new NodeSettings("Hiliting");
            ((DefaultHiLiteMapper)m_translator.getMapper()).save(s);
            File f = new File(nodeInternDir, "Hiliting.conf.gz");
            OutputStream out = new GZIPOutputStream(new BufferedOutputStream(
                    new FileOutputStream(f)));
            s.saveToXML(out);
            out.close();
        }
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
        ValueCounterSettings s = new ValueCounterSettings();
        s.loadSettings(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInHiLiteHandler(final int inIndex, 
            final HiLiteHandler hiLiteHdl) {
        m_translator.removeAllToHiliteHandlers();
        m_translator.addToHiLiteHandler(hiLiteHdl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HiLiteHandler getOutHiLiteHandler(final int outIndex) {
        return m_translator.getFromHiLiteHandler();
    }
}
