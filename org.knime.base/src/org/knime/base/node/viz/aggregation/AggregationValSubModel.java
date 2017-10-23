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
 * -------------------------------------------------------------------
 *
 * History
 *    12.09.2007 (Tobias Koetter): created
 */

package org.knime.base.node.viz.aggregation;

import java.awt.Color;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.RowKey;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.config.ConfigRO;
import org.knime.core.node.config.ConfigWO;

/**
 * This class holds the data of a sub model which represents rows of the
 * same color.
 * @author Tobias Koetter, University of Konstanz
 * @param <S> the shape of this sub model
 * @param <H> the optional hilite shape
 */
public abstract class AggregationValSubModel <S extends Shape, H extends Shape>
implements Serializable, AggregationModel<S, H> {

    private static final long serialVersionUID = -1627330636768050680L;

    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(AggregationValSubModel.class);

    private static final String CFG_COLOR_RGB = "colorRGB";
    private static final String CFG_HILITING = "hiliting";
    private static final String CFG_AGGR_SUM = "aggrSum";
    private static final String CFG_ROW_COUNTER = "rowCount";
    private static final String CFG_VALUE_COUNTER = "valueCount";

    private final Color m_color;
    private final boolean m_supportHiliting;
    private double m_aggrSum = 0;
    /**The number of values without missing values!*/
    private int m_valueCounter = 0;
    /**The number of rows including empty value rows.*/
    private int m_rowCounter = 0;
    private boolean m_isSelected = false;
    private boolean m_presentable = false;
    private S m_shape;
    private final Set<RowKey> m_rowKeys;
    private final Set<RowKey> m_hilitedRowKeys;
    private H m_hilitedShape;

    /**Constructor for class AttributeValColorModel.
     * @param color the color to use for this sub element
     */
    protected AggregationValSubModel(final Color color) {
        this(color, false);
    }

    /**Constructor for class AttributeValColorModel.
     * @param color the color to use for this sub element
     * @param supportHiliting if hiliting support should be enabled
     */
    protected AggregationValSubModel(final Color color,
            final boolean supportHiliting) {
        m_color = color;
        m_supportHiliting = supportHiliting;
        if (m_supportHiliting) {
            m_rowKeys  = new HashSet<RowKey>();
            m_hilitedRowKeys = new HashSet<RowKey>();
        } else {
            m_rowKeys = null;
            m_hilitedRowKeys = null;
        }
    }

    /**Constructor for class AttributeValColorModel (used for cloning).
     * @param color the color to use for this sub element
     * @param supportHiliting if hiliting should be supported
     * @param aggrSum the sum of the aggregation values
     * @param valueCounter the number of aggregation values
     * @param rowCounter the number of rows incl. missing values
     */
    protected AggregationValSubModel(final Color color,
            final boolean supportHiliting, final double aggrSum,
            final int valueCounter, final int rowCounter) {
        m_color = color;
        m_aggrSum = aggrSum;
        m_valueCounter = valueCounter;
        m_rowCounter = rowCounter;
        m_supportHiliting = supportHiliting;
        if (m_supportHiliting) {
            m_rowKeys  = new HashSet<RowKey>();
            m_hilitedRowKeys = new HashSet<RowKey>();
        } else {
            m_rowKeys = null;
            m_hilitedRowKeys = null;
        }
    }

    /**Constructor for class AggregationValSubModel.
     * @param config the config object to use
     * @throws InvalidSettingsException if the config object is invalid
     */
    protected AggregationValSubModel(final ConfigRO config)
    throws InvalidSettingsException {
        this(new Color(config.getInt(CFG_COLOR_RGB)),
        config.getBoolean(CFG_HILITING),
        config.getDouble(CFG_AGGR_SUM),
        config.getInt(CFG_VALUE_COUNTER),
        config.getInt(CFG_ROW_COUNTER));
    }

    /**
     * Adds the given values to the sub element.
     * @param rowKey the rowkey of the row to add
     * @param aggrValCell the value cell of the aggregation column of this sub
     * element
     */
    protected void addDataRow(final RowKey rowKey,
            final DataCell aggrValCell) {
        if (aggrValCell != null && !aggrValCell.isMissing()) {
            m_aggrSum += ((DoubleValue)aggrValCell).getDoubleValue();
            m_valueCounter++;
        }
        m_rowCounter++;
        if (m_supportHiliting) {
            m_rowKeys.add(rowKey);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRowCount() {
        return m_rowCounter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getValueCount() {
        return m_valueCounter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getAggregationSum() {
        return m_aggrSum;
    }

    /**
     * @param method the {@link AggregationMethod} to use
     * @return the aggregation value of this sub element
     */
    @Override
    public double getAggregationValue(final AggregationMethod method) {
        if (AggregationMethod.COUNT.equals(method)) {
            return m_rowCounter;
        } else if (AggregationMethod.VALUE_COUNT.equals(method)) {
            return m_valueCounter;
        } else if (AggregationMethod.SUM.equals(method)) {
            return m_aggrSum;
        } else if (AggregationMethod.AVERAGE.equals(method)) {
            if (m_valueCounter == 0) {
                //avoid division by 0
                return 0;
            }
            return m_aggrSum / m_valueCounter;
        }
       throw new IllegalArgumentException("Aggregation method "
               + method + " not supported.");
    }

    /**
     * @return the color to use for this sub element
     */
    @Override
    public Color getColor() {
        return m_color;
    }

    /**
     * @return the shape
     */
    @Override
    public S getShape() {
        return m_shape;
    }

    /**
     * @param shape the new shape of this sub element
     * @param calculator the hilite shape calculator
     */
    protected void setShape(final S shape,
            final HiliteShapeCalculator<S, H> calculator) {
        if (shape == null) {
            m_presentable = false;
        } else {
            m_presentable = true;
        }
        m_shape = shape;
        calculateHilitedShape(calculator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public H getHiliteShape() {
        return m_hilitedShape;
    }

    /**
     * @param shape the new hilite shape
     */
    protected void setHiliteShape(final H shape) {
        m_hilitedShape = shape;
    }

    /**
     * @return <code>true</code> if the element is selected
     */
    @Override
    public boolean isSelected() {
        return m_isSelected;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return m_rowCounter < 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPresentable() {
        return m_presentable;
    }

    /**
     * @param presentable <code>true</code> if this element is presentable
     * @param calculator the hilite shape calculator
     */
    protected void setPresentable(final boolean presentable,
            final HiliteShapeCalculator<S, H> calculator) {
        if (m_presentable == presentable) {
            return;
        }
        m_presentable = presentable;
        //recalculate the hilite shape
        calculateHilitedShape(calculator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        //this element has no name
        return null;
    }

    /**
     * @return the enableHiliting variable
     */
    @Override
    public boolean supportsHiliting() {
        return m_supportHiliting;
    }

    /**
     * @param isSelected set to <code>true</code> if the element is selected
     */
    protected void setSelected(final boolean isSelected) {
        this.m_isSelected = isSelected;
    }

    /**
     * Selects this element if the element rectangle contains the given
     * point.
     * @param point the {@link Point} to check
     * @return <code>true</code> if the element contains the point
     */
    public boolean selectElement(final Point point) {
        if (m_shape != null
                    && m_shape.contains(point)) {
            setSelected(true);
            return true;
        }
        return false;
    }

    /**
     * Selects this element if the element rectangle intersect the given
     * rectangle.
     * @param rect the {@link Rectangle2D} to check
     * @return <code>true</code> if the element intersects the rectangle
     */
    public boolean selectElement(final Rectangle2D rect) {
        if (m_shape != null
                && m_shape.intersects(rect)) {
        setSelected(true);
        return true;
        }
        return false;
    }

    /**
     * @return the keys of the rows in this element.
     */
    public Set<RowKey> getKeys() {
        return m_rowKeys;
    }

    /**
     * @return <code>true</code> if at least one row of this element is hilited
     */
    @Override
    public boolean isHilited() {
        if (!m_supportHiliting) {
            throw new UnsupportedOperationException(
                    "Hilitign is not supported");
        }
        return m_hilitedRowKeys.size() > 0;
    }

    /**
     * @return the keys of the hilited rows in this element
     */
    public Set<RowKey> getHilitedKeys() {
        if (!m_supportHiliting) {
            throw new UnsupportedOperationException(
                    "Hilitign is not supported");
        }
        return m_hilitedRowKeys;
    }

    /**
     * Clears the hilite counter.
     */
    protected void clearHilite() {
        if (!m_supportHiliting) {
            throw new UnsupportedOperationException(
                    "Hilitign is not supported");
        }
        m_hilitedRowKeys.clear();
        setHiliteShape(null);
    }

    /**
     * @param hilitedKeys the hilited keys
     * @param calculator the hilite shape calculator
     * @return <code>true</code> if at least one key has been added
     */
    protected boolean setHilitedKeys(final Collection<RowKey> hilitedKeys,
            final HiliteShapeCalculator<S, H> calculator) {
        if (!m_supportHiliting) {
            throw new UnsupportedOperationException(
                    "Hilitign is not supported");
        }
        boolean changed = false;
        for (final RowKey key : hilitedKeys) {
            if (m_rowKeys.contains(key)) {
                if (m_hilitedRowKeys.add(key)) {
                    changed = true;
                }
            }
        }
        if (changed) {
            calculateHilitedShape(calculator);
        }
        return changed;
    }

    /**
     * @param unhilitedKeys the keys which should be unhilited
     * @param calculator the hilite shape calculator
     * @return <code>true</code> if at least one key has been removed
     */
    protected boolean removeHilitedKeys(
            final Collection<RowKey> unhilitedKeys,
            final HiliteShapeCalculator<S, H> calculator) {
        if (!m_supportHiliting) {
            throw new UnsupportedOperationException(
                    "Hilitign is not supported");
        }
        final boolean changed = m_hilitedRowKeys.removeAll(unhilitedKeys);
        if (changed) {
            calculateHilitedShape(calculator);
        }
        return changed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHiliteRowCount() {
        if (!m_supportHiliting) {
            throw new UnsupportedOperationException(
                    "Hilitign is not supported");
        }
        return m_hilitedRowKeys.size();
    }

    /**
     * Calculates the hilite rectangle or resets the hilite rectangle to
     * <code>null</code> if no rows are hilited.
     * @param calculator the hilite shape calculator
     */
    protected void calculateHilitedShape(
            final HiliteShapeCalculator<S, H> calculator) {
        if (calculator == null) {
            return;
        }
        if (supportsHiliting()) {
            setHiliteShape(calculator.calculateHiliteShape(this));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AggregationValSubModel <S, H> clone()
    throws CloneNotSupportedException {
        LOGGER.debug("Entering clone() of class AggregationValSubModel.");
        final long startTime = System.currentTimeMillis();
        AggregationValSubModel <S, H> clone =
            null;
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            new ObjectOutputStream(baos).writeObject(this);
            final ByteArrayInputStream bais =
                new ByteArrayInputStream(baos.toByteArray());
            clone = (AggregationValSubModel <S, H>)
            new ObjectInputStream(bais).readObject();
        } catch (final Exception e) {
            final String msg =
                "Exception while cloning aggregation value sub model: "
                + e.getMessage();
              LOGGER.debug(msg);
              throw new CloneNotSupportedException(msg);
        }

        final long endTime = System.currentTimeMillis();
        final long durationTime = endTime - startTime;
        LOGGER.debug("Time for cloning. " + durationTime + " ms");
        LOGGER.debug("Exiting clone() of class AggregationValSubModel.");
        return clone;
    }

    /**
     * @param config the config object to use
     * @param exec the optional {@link ExecutionMonitor} to provide progress messages
     * @throws CanceledExecutionException if the operation is canceled
     */
    public void save2File(final ConfigWO config,
            final ExecutionMonitor exec) throws CanceledExecutionException {
        config.addInt(CFG_COLOR_RGB, getColor().getRGB());
        config.addBoolean(CFG_HILITING, supportsHiliting());
        config.addDouble(CFG_AGGR_SUM, getAggregationSum());
        config.addInt(CFG_ROW_COUNTER, getRowCount());
        config.addInt(CFG_VALUE_COUNTER, getValueCount());
        if (exec != null) {
            exec.checkCanceled();
        }
    }
}
