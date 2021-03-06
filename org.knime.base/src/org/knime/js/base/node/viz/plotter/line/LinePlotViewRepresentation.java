/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 *  propagated with or for interoperation with KNIME. The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 *
 * History
 *   13.05.2014 (Christian Albrecht, KNIME.com AG, Zurich, Switzerland): created
 */
package org.knime.js.base.node.viz.plotter.line;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.js.core.JSONViewContent;
import org.knime.js.core.components.datetime.SettingsModelDateTimeOptions.JSONDateTimeOptions;
import org.knime.js.core.datasets.JSONKeyedValues2DDataset;
import org.knime.js.core.warnings.JSONWarnings;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 *
 * @author Christian Albrecht, KNIME.com AG, Zurich, Switzerland, University of Konstanz
 */
@JsonAutoDetect
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class LinePlotViewRepresentation extends JSONViewContent {

    private JSONKeyedValues2DDataset m_keyedDataset;

    private boolean m_showLegend;
    private boolean m_autoRangeAxes;
    private boolean m_useDomainInformation;
    private boolean m_showGrid;
    private boolean m_showCrosshair;
    private boolean m_snapToPoints;
    private boolean m_resizeToWindow;
    private boolean m_displayFullscreenButton;

    private boolean m_enableViewConfiguration;
    private boolean m_enableTitleChange;
    private boolean m_enableSubtitleChange;
    private boolean m_enableXColumnChange;
    private boolean m_enableYColumnChange;
    private boolean m_enableXAxisLabelEdit;
    private boolean m_enableYAxisLabelEdit;
    private boolean m_enableDotSizeChange;
    private boolean m_enableZooming;
    private boolean m_enableDragZooming;
    private boolean m_enablePanning;
    private boolean m_showZoomResetButton;

    private boolean m_enableSelection;
    private boolean m_enableRectangleSelection;
    private boolean m_enableLassoSelection;

    private int m_imageWidth;
    private int m_imageHeight;
    private String m_backgroundColor;
    private String m_dataAreaColor;
    private String m_gridColor;

    private boolean m_enableStaggeredRendering = true;

    private String m_missingValueMethod;

    private boolean m_showWarningInView;
    private JSONWarnings m_warnings = new JSONWarnings();

    private boolean m_reportOnMissingValues;

    private JSONDateTimeOptions m_dateTimeFormats;

    /**
     * @return the keyedDataset
     */
    public JSONKeyedValues2DDataset getKeyedDataset() {
        return m_keyedDataset;
    }

    /**
     * @param keyedDataset the keyedDataset to set
     */
    public void setKeyedDataset(final JSONKeyedValues2DDataset keyedDataset) {
        m_keyedDataset = keyedDataset;
    }

    /**
     * @return the showLegend
     */
    public boolean getShowLegend() {
        return m_showLegend;
    }

    /**
     * @param showLegend the showLegend to set
     */
    public void setShowLegend(final boolean showLegend) {
        m_showLegend = showLegend;
    }

    /**
     * @return the autoRange
     */
    public boolean getAutoRangeAxes() {
        return m_autoRangeAxes;
    }

    /**
     * @param autoRangeAxes the autoRange to set
     */
    public void setAutoRangeAxes(final boolean autoRangeAxes) {
        m_autoRangeAxes = autoRangeAxes;
    }

    /**
     * @return the useDomainInformation
     */
    public boolean getUseDomainInformation() {
        return m_useDomainInformation;
    }

    /**
     * @param useDomainInformation the useDomainInformation to set
     */
    public void setUseDomainInformation(final boolean useDomainInformation) {
        m_useDomainInformation = useDomainInformation;
    }

    /**
     * @return the showGrid
     */
    public boolean getShowGrid() {
        return m_showGrid;
    }

    /**
     * @param showGrid the showGrid to set
     */
    public void setShowGrid(final boolean showGrid) {
        m_showGrid = showGrid;
    }

    /**
     * @return the showCrosshair
     */
    public boolean getShowCrosshair() {
        return m_showCrosshair;
    }

    /**
     * @param showCrosshair the showCrosshair to set
     */
    public void setShowCrosshair(final boolean showCrosshair) {
        m_showCrosshair = showCrosshair;
    }

    /**
     * @return the snapToPoints
     */
    public boolean getSnapToPoints() {
        return m_snapToPoints;
    }

    /**
     * @param snapToPoints the snapToPoints to set
     */
    public void setSnapToPoints(final boolean snapToPoints) {
        m_snapToPoints = snapToPoints;
    }

    /**
     * @return the resizeToWindow
     */
    public boolean getResizeToWindow() {
        return m_resizeToWindow;
    }

    /**
     * @param resizeToWindow the resizeToWindow to set
     */
    public void setResizeToWindow(final boolean resizeToWindow) {
        m_resizeToWindow = resizeToWindow;
    }

    /**
     * @return the displayFullscreenButton
     */
    public boolean getDisplayFullscreenButton() {
        return m_displayFullscreenButton;
    }

    /**
     * @param displayFullscreenButton the displayFullscreenButton to set
     */
    public void setDisplayFullscreenButton(final boolean displayFullscreenButton) {
        m_displayFullscreenButton = displayFullscreenButton;
    }

    /**
     * @return the allowViewConfiguration
     */
    public boolean getEnableViewConfiguration() {
        return m_enableViewConfiguration;
    }

    /**
     * @param enableViewConfiguration the allowViewConfiguration to set
     */
    public void setEnableViewConfiguration(final boolean enableViewConfiguration) {
        m_enableViewConfiguration = enableViewConfiguration;
    }

    /**
     * @return the enableTitleChange
     */
    public boolean getEnableTitleChange() {
        return m_enableTitleChange;
    }

    /**
     * @param enableTitleChange the enableTitleChange to set
     */
    public void setEnableTitleChange(final boolean enableTitleChange) {
        m_enableTitleChange = enableTitleChange;
    }

    /**
     * @return the enableSubtitleChange
     */
    public boolean getEnableSubtitleChange() {
        return m_enableSubtitleChange;
    }

    /**
     * @param enableSubtitleChange the enableSubtitleChange to set
     */
    public void setEnableSubtitleChange(final boolean enableSubtitleChange) {
        m_enableSubtitleChange = enableSubtitleChange;
    }

    /**
     * @return the enableXColumnChange
     */
    public boolean getEnableXColumnChange() {
        return m_enableXColumnChange;
    }

    /**
     * @param enableXColumnChange the enableXColumnChange to set
     */
    public void setEnableXColumnChange(final boolean enableXColumnChange) {
        m_enableXColumnChange = enableXColumnChange;
    }

    /**
     * @return the enableYColumnChange
     */
    public boolean getEnableYColumnChange() {
        return m_enableYColumnChange;
    }

    /**
     * @param enableYColumnChange the enableYColumnChange to set
     */
    public void setEnableYColumnChange(final boolean enableYColumnChange) {
        m_enableYColumnChange = enableYColumnChange;
    }

    /**
     * @return the enableXAxisLabelEdit
     */
    public boolean getEnableXAxisLabelEdit() {
        return m_enableXAxisLabelEdit;
    }

    /**
     * @param enableXAxisLabelEdit the enableXAxisLabelEdit to set
     */
    public void setEnableXAxisLabelEdit(final boolean enableXAxisLabelEdit) {
        m_enableXAxisLabelEdit = enableXAxisLabelEdit;
    }

    /**
     * @return the enableYAxisLabelEdit
     */
    public boolean getEnableYAxisLabelEdit() {
        return m_enableYAxisLabelEdit;
    }

    /**
     * @param enableYAxisLabelEdit the enableYAxisLabelEdit to set
     */
    public void setEnableYAxisLabelEdit(final boolean enableYAxisLabelEdit) {
        m_enableYAxisLabelEdit = enableYAxisLabelEdit;
    }

    /**
     * @return the allowDotSizeChange
     */
    public boolean getEnableDotSizeChange() {
        return m_enableDotSizeChange;
    }

    /**
     * @param enableDotSizeChange the allowDotSizeChange to set
     */
    public void setEnableDotSizeChange(final boolean enableDotSizeChange) {
        m_enableDotSizeChange = enableDotSizeChange;
    }

    /**
     * @return the allowZooming
     */
    public boolean getEnableZooming() {
        return m_enableZooming;
    }

    /**
     * @param enableZooming the allowZooming to set
     */
    public void setEnableZooming(final boolean enableZooming) {
        m_enableZooming = enableZooming;
    }

    /**
     * @return the enableDragZooming
     */
    public boolean getEnableDragZooming() {
        return m_enableDragZooming;
    }

    /**
     * @param enableDragZooming the enableDragZooming to set
     */
    public void setEnableDragZooming(final boolean enableDragZooming) {
        m_enableDragZooming = enableDragZooming;
    }

    /**
     * @return the allowPanning
     */
    public boolean getEnablePanning() {
        return m_enablePanning;
    }

    /**
     * @param enablePanning the allowPanning to set
     */
    public void setEnablePanning(final boolean enablePanning) {
        m_enablePanning = enablePanning;
    }

    /**
     * @return the showZoomResetButton
     */
    public boolean getShowZoomResetButton() {
        return m_showZoomResetButton;
    }

    /**
     * @param showZoomResetButton the showZoomResetButton to set
     */
    public void setShowZoomResetButton(final boolean showZoomResetButton) {
        m_showZoomResetButton = showZoomResetButton;
    }

    /**
     * @return the enableSelection
     */
    public boolean getEnableSelection() {
        return m_enableSelection;
    }

    /**
     * @param enableSelection the enableSelection to set
     */
    public void setEnableSelection(final boolean enableSelection) {
        m_enableSelection = enableSelection;
    }

    /**
     * @return the enableRectangleSelection
     */
    public boolean getEnableRectangleSelection() {
        return m_enableRectangleSelection;
    }

    /**
     * @param enableRectangleSelection the enableRectangleSelection to set
     */
    public void setEnableRectangleSelection(final boolean enableRectangleSelection) {
        m_enableRectangleSelection = enableRectangleSelection;
    }

    /**
     * @return the enableLassoSelection
     */
    public boolean getEnableLassoSelection() {
        return m_enableLassoSelection;
    }

    /**
     * @param enableLassoSelection the enableLassoSelection to set
     */
    public void setEnableLassoSelection(final boolean enableLassoSelection) {
        m_enableLassoSelection = enableLassoSelection;
    }

    /**
     * @return the imageWidth
     */
    public int getImageWidth() {
        return m_imageWidth;
    }

    /**
     * @param imageWidth the imageWidth to set
     */
    public void setImageWidth(final int imageWidth) {
        m_imageWidth = imageWidth;
    }

    /**
     * @return the imageHeight
     */
    public int getImageHeight() {
        return m_imageHeight;
    }

    /**
     * @param imageHeight the imageHeight to set
     */
    public void setImageHeight(final int imageHeight) {
        m_imageHeight = imageHeight;
    }

    /**
     * @return the backgroundColor
     */
    public String getBackgroundColor() {
        return m_backgroundColor;
    }

    /**
     * @param backgroundColor the backgroundColor to set
     */
    public void setBackgroundColor(final String backgroundColor) {
        m_backgroundColor = backgroundColor;
    }

    /**
     * @return the dataAreaColor
     */
    public String getDataAreaColor() {
        return m_dataAreaColor;
    }

    /**
     * @param dataAreaColor the dataAreaColor to set
     */
    public void setDataAreaColor(final String dataAreaColor) {
        m_dataAreaColor = dataAreaColor;
    }

    /**
     * @return the gridColor
     */
    public String getGridColor() {
        return m_gridColor;
    }

    /**
     * @param gridColor the gridColor to set
     */
    public void setGridColor(final String gridColor) {
        m_gridColor = gridColor;
    }

    /**
     * @return the enableStaggeredRendering
     */
    public boolean getEnableStaggeredRendering() {
        return m_enableStaggeredRendering;
    }

    /**
     * @param enableStaggeredRendering the enableStaggeredRendering to set
     */
    public void setEnableStaggeredRendering(final boolean enableStaggeredRendering) {
        m_enableStaggeredRendering = enableStaggeredRendering;
    }

    /**
     * @return the missingValueMethod
     */
    public String getMissingValueMethod() {
        return m_missingValueMethod;
    }

    /**
     * @param missingValueMethod the missingValueMethod to set
     */
    public void setMissingValueMethod(final String missingValueMethod) {
        m_missingValueMethod = missingValueMethod;
    }

    /**
     * @return the showWarningInView
     */
    public boolean getShowWarningInView() {
        return m_showWarningInView;
    }

    /**
     * @param showWarningInView the showWarningInView to set
     */
    public void setShowWarningInView(final boolean showWarningInView) {
        m_showWarningInView = showWarningInView;
    }

    /**
     * @return the warnings
     */
    public JSONWarnings getWarnings() {
        return m_warnings;
    }

    /**
     * @param warnings the warnings to set
     */
    public void setWarnings(final JSONWarnings warnings) {
        m_warnings = warnings;
    }

    /**
     * @return the dateTimeFormats
     */
    public JSONDateTimeOptions getDateTimeFormats() {
        return m_dateTimeFormats;
    }

    /**
     * @param dateTimeFormats the dateTimeFormats to set
     */
    public void setDateTimeFormats(final JSONDateTimeOptions dateTimeFormats) {
        m_dateTimeFormats = dateTimeFormats;
    }

    /**
     * @return the reportOnMissingValues
     */
    public boolean getReportOnMissingValues() {
        return m_reportOnMissingValues;
    }

    /**
     * @param reportOnMissingValues the reportOnMissingValues to set
     */
    public void setReportOnMissingValues(final boolean reportOnMissingValues) {
        m_reportOnMissingValues = reportOnMissingValues;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveToNodeSettings(final NodeSettingsWO settings) {
        settings.addBoolean(LinePlotViewConfig.SHOW_LEGEND, getShowLegend());
        settings.addBoolean(LinePlotViewConfig.AUTO_RANGE_AXES, getAutoRangeAxes());
        settings.addBoolean(LinePlotViewConfig.USE_DOMAIN_INFO, getUseDomainInformation());
        settings.addBoolean(LinePlotViewConfig.SHOW_GRID, getShowGrid());
        settings.addBoolean(LinePlotViewConfig.SHOW_CROSSHAIR, getShowCrosshair());
        settings.addBoolean(LinePlotViewConfig.SNAP_TO_POINTS, getSnapToPoints());
        settings.addBoolean(LinePlotViewConfig.RESIZE_TO_WINDOW, getResizeToWindow());

        settings.addBoolean(LinePlotViewConfig.ENABLE_CONFIG, getEnableViewConfiguration());
        settings.addBoolean(LinePlotViewConfig.ENABLE_TTILE_CHANGE, getEnableTitleChange());
        settings.addBoolean(LinePlotViewConfig.ENABLE_SUBTTILE_CHANGE, getEnableSubtitleChange());
        settings.addBoolean(LinePlotViewConfig.ENABLE_X_COL_CHANGE, getEnableXColumnChange());
        settings.addBoolean(LinePlotViewConfig.ENABLE_Y_COL_CHANGE, getEnableYColumnChange());
        settings.addBoolean(LinePlotViewConfig.ENABLE_X_LABEL_EDIT, getEnableXAxisLabelEdit());
        settings.addBoolean(LinePlotViewConfig.ENABLE_Y_LABEL_EDIT, getEnableYAxisLabelEdit());
        settings.addBoolean(LinePlotViewConfig.ENABLE_DOT_SIZE_CHANGE, getEnableDotSizeChange());

        settings.addBoolean(LinePlotViewConfig.ENABLE_PANNING, getEnablePanning());
        settings.addBoolean(LinePlotViewConfig.ENABLE_ZOOMING, getEnableZooming());
        settings.addBoolean(LinePlotViewConfig.ENABLE_DRAG_ZOOMING, getEnableDragZooming());
        settings.addBoolean(LinePlotViewConfig.SHOW_ZOOM_RESET_BUTTON, getShowZoomResetButton());

        settings.addBoolean(LinePlotViewConfig.ENABLE_SELECTION, getEnableSelection());
        settings.addBoolean(LinePlotViewConfig.ENABLE_RECTANGLE_SELECTION, getEnableRectangleSelection());
        settings.addBoolean(LinePlotViewConfig.ENABLE_LASSO_SELECTION, getEnableLassoSelection());

        settings.addInt(LinePlotViewConfig.IMAGE_WIDTH, getImageWidth());
        settings.addInt(LinePlotViewConfig.IMAGE_HEIGHT, getImageHeight());
        settings.addString(LinePlotViewConfig.BACKGROUND_COLOR, getBackgroundColor());
        settings.addString(LinePlotViewConfig.DATA_AREA_COLOR, getDataAreaColor());
        settings.addString(LinePlotViewConfig.GRID_COLOR, getGridColor());

        settings.addBoolean("hasDataset", m_keyedDataset != null);
        if (m_keyedDataset != null) {
            NodeSettingsWO datasetSettings = settings.addNodeSettings("dataset");
            m_keyedDataset.saveToNodeSettings(datasetSettings);
        }

        // added with 3.3
        settings.addBoolean(LinePlotViewConfig.CFG_DISPLAY_FULLSCREEN_BUTTON, getDisplayFullscreenButton());

        // added with 3.4
        settings.addString(LinePlotViewConfig.MISSING_VALUE_METHOD, getMissingValueMethod());
        settings.addBoolean(LinePlotViewConfig.SHOW_WARNING_IN_VIEW, getShowWarningInView());
        m_warnings.saveToNodeSettings(settings);
        settings.addBoolean(LinePlotViewConfig.REPORT_ON_MISSING_VALUES, getReportOnMissingValues());

        m_dateTimeFormats.saveToNodeSettings(settings.addNodeSettings(LinePlotViewConfig.DATE_TIME_FORMATS));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadFromNodeSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        setShowLegend(settings.getBoolean(LinePlotViewConfig.SHOW_LEGEND));
        setAutoRangeAxes(settings.getBoolean(LinePlotViewConfig.AUTO_RANGE_AXES));
        setUseDomainInformation(settings.getBoolean(LinePlotViewConfig.USE_DOMAIN_INFO));
        setShowGrid(settings.getBoolean(LinePlotViewConfig.SHOW_GRID));
        setShowCrosshair(settings.getBoolean(LinePlotViewConfig.SHOW_CROSSHAIR));
        setSnapToPoints(settings.getBoolean(LinePlotViewConfig.SNAP_TO_POINTS));
        setResizeToWindow(settings.getBoolean(LinePlotViewConfig.RESIZE_TO_WINDOW));

        setEnableViewConfiguration(settings.getBoolean(LinePlotViewConfig.ENABLE_CONFIG));
        setEnableTitleChange(settings.getBoolean(LinePlotViewConfig.ENABLE_TTILE_CHANGE));
        setEnableSubtitleChange(settings.getBoolean(LinePlotViewConfig.ENABLE_SUBTTILE_CHANGE));
        setEnableXColumnChange(settings.getBoolean(LinePlotViewConfig.ENABLE_X_COL_CHANGE));
        setEnableYColumnChange(settings.getBoolean(LinePlotViewConfig.ENABLE_Y_COL_CHANGE));
        setEnableXAxisLabelEdit(settings.getBoolean(LinePlotViewConfig.ENABLE_X_LABEL_EDIT));
        setEnableYAxisLabelEdit(settings.getBoolean(LinePlotViewConfig.ENABLE_Y_LABEL_EDIT));
        setEnableDotSizeChange(settings.getBoolean(LinePlotViewConfig.ENABLE_DOT_SIZE_CHANGE));

        setEnablePanning(settings.getBoolean(LinePlotViewConfig.ENABLE_PANNING));
        setEnableZooming(settings.getBoolean(LinePlotViewConfig.ENABLE_ZOOMING));
        setEnableDragZooming(settings.getBoolean(LinePlotViewConfig.ENABLE_DRAG_ZOOMING));
        setShowZoomResetButton(settings.getBoolean(LinePlotViewConfig.SHOW_ZOOM_RESET_BUTTON));

        setEnableSelection(settings.getBoolean(LinePlotViewConfig.ENABLE_SELECTION));
        setEnableRectangleSelection(settings.getBoolean(LinePlotViewConfig.ENABLE_RECTANGLE_SELECTION));
        setEnableLassoSelection(settings.getBoolean(LinePlotViewConfig.ENABLE_LASSO_SELECTION));

        setImageWidth(settings.getInt(LinePlotViewConfig.IMAGE_WIDTH));
        setImageHeight(settings.getInt(LinePlotViewConfig.IMAGE_HEIGHT));
        setBackgroundColor(settings.getString(LinePlotViewConfig.BACKGROUND_COLOR));
        setDataAreaColor(settings.getString(LinePlotViewConfig.DATA_AREA_COLOR));
        setGridColor(settings.getString(LinePlotViewConfig.GRID_COLOR));

        m_keyedDataset = null;
        boolean hasDataset = settings.getBoolean("hasDataset");
        if (hasDataset) {
            NodeSettingsRO datasetSettings = settings.getNodeSettings("dataset");
            m_keyedDataset = new JSONKeyedValues2DDataset();
            m_keyedDataset.loadFromNodeSettings(datasetSettings);
        }

        // added with 3.3
        setDisplayFullscreenButton(settings.getBoolean(LinePlotViewConfig.CFG_DISPLAY_FULLSCREEN_BUTTON, LinePlotViewConfig.DEFAULT_DISPLAY_FULLSCREEN_BUTTON));

        // added with 3.4
        setMissingValueMethod(settings.getString(LinePlotViewConfig.MISSING_VALUE_METHOD, LinePlotViewConfig.MISSING_VALUE_METHOD_DEFAULT));
        setShowWarningInView(settings.getBoolean(LinePlotViewConfig.SHOW_WARNING_IN_VIEW, LinePlotViewConfig.DEFAULT_SHOW_WARNING_IN_VIEW));
        m_warnings.loadFromNodeSettings(settings);
        setReportOnMissingValues(settings.getBoolean(LinePlotViewConfig.REPORT_ON_MISSING_VALUES, LinePlotViewConfig.DEFAULT_REPORT_ON_MISSING_VALUES));

        if (settings.containsKey(LinePlotViewConfig.DATE_TIME_FORMATS)) {
            m_dateTimeFormats = JSONDateTimeOptions.loadFromNodeSettings(settings.getNodeSettings(LinePlotViewConfig.DATE_TIME_FORMATS));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        LinePlotViewRepresentation other = (LinePlotViewRepresentation)obj;
        return new EqualsBuilder()
                .append(m_keyedDataset, other.m_keyedDataset)
                .append(m_showLegend, other.m_showLegend)
                .append(m_autoRangeAxes, other.m_autoRangeAxes)
                .append(m_useDomainInformation, other.m_useDomainInformation)
                .append(m_showGrid, other.m_showGrid)
                .append(m_showCrosshair, other.m_showCrosshair)
                .append(m_snapToPoints, other.m_snapToPoints)
                .append(m_resizeToWindow, other.m_resizeToWindow)
                .append(m_displayFullscreenButton, other.m_displayFullscreenButton)
                .append(m_enableViewConfiguration, other.m_enableViewConfiguration)
                .append(m_enableTitleChange, other.m_enableTitleChange)
                .append(m_enableSubtitleChange, other.m_enableSubtitleChange)
                .append(m_enableXColumnChange, other.m_enableXColumnChange)
                .append(m_enableYColumnChange, other.m_enableYColumnChange)
                .append(m_enableXAxisLabelEdit, other.m_enableXAxisLabelEdit)
                .append(m_enableYAxisLabelEdit, other.m_enableYAxisLabelEdit)
                .append(m_enableDotSizeChange, other.m_enableDotSizeChange)
                .append(m_enableZooming, other.m_enableZooming)
                .append(m_enableDragZooming, other.m_enableDragZooming)
                .append(m_enablePanning, other.m_enablePanning)
                .append(m_showZoomResetButton, other.m_showZoomResetButton)
                .append(m_enableSelection, other.m_enableSelection)
                .append(m_enableRectangleSelection, other.m_enableRectangleSelection)
                .append(m_enableLassoSelection, other.m_enableLassoSelection)
                .append(m_imageWidth, other.m_imageWidth)
                .append(m_imageHeight, other.m_imageHeight)
                .append(m_dateTimeFormats, other.m_dateTimeFormats)
                .append(m_backgroundColor, other.m_backgroundColor)
                .append(m_dataAreaColor, other.m_dataAreaColor)
                .append(m_gridColor, other.m_gridColor)
                .append(m_enableStaggeredRendering, other.m_enableStaggeredRendering)
                .append(m_missingValueMethod, other.m_missingValueMethod)
                .append(m_showWarningInView, other.m_showWarningInView)
                .append(m_warnings, other.m_warnings)
                .append(m_reportOnMissingValues, other.m_reportOnMissingValues)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(m_keyedDataset)
                .append(m_showLegend)
                .append(m_autoRangeAxes)
                .append(m_useDomainInformation)
                .append(m_showGrid)
                .append(m_showCrosshair)
                .append(m_snapToPoints)
                .append(m_resizeToWindow)
                .append(m_displayFullscreenButton)
                .append(m_enableViewConfiguration)
                .append(m_enableTitleChange)
                .append(m_enableSubtitleChange)
                .append(m_enableXColumnChange)
                .append(m_enableYColumnChange)
                .append(m_enableXAxisLabelEdit)
                .append(m_enableYAxisLabelEdit)
                .append(m_enableDotSizeChange)
                .append(m_enableZooming)
                .append(m_enableDragZooming)
                .append(m_enablePanning)
                .append(m_showZoomResetButton)
                .append(m_enableSelection)
                .append(m_enableRectangleSelection)
                .append(m_enableLassoSelection)
                .append(m_imageWidth)
                .append(m_imageHeight)
                .append(m_dateTimeFormats)
                .append(m_backgroundColor)
                .append(m_dataAreaColor)
                .append(m_gridColor)
                .append(m_enableStaggeredRendering)
                .append(m_missingValueMethod)
                .append(m_showWarningInView)
                .append(m_warnings)
                .append(m_reportOnMissingValues)
                .toHashCode();
    }
}
