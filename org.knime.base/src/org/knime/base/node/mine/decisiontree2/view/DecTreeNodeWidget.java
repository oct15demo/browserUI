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
 * History
 *   22.07.2010 (hofer): created
 */
package org.knime.base.node.mine.decisiontree2.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.knime.base.node.mine.decisiontree2.model.DecisionTreeNode;
import org.knime.base.node.mine.decisiontree2.model.DecisionTreeNodeSplit;
import org.knime.base.node.mine.decisiontree2.view.graph.ComponentNodeWidget;
import org.knime.base.node.mine.decisiontree2.view.graph.HierarchicalGraphView;
import org.knime.core.data.DataCell;
import org.knime.core.data.property.ColorAttr;

/**
 * A view for a {@link DecisionTreeNode}.
 *
 * @author Heiko Hofer
 */
public final class DecTreeNodeWidget
            extends ComponentNodeWidget<DecisionTreeNode> {
    private boolean m_displayTable;
    private boolean m_displayChart;
    private boolean m_tableCollapsed;
    private boolean m_chartCollapsed;
    private CollapsiblePanel m_table;
    private CollapsiblePanel m_chart;
    private String m_colorColumn;

    /**
     * @param graph the graph this widget is element of
     * @param decNode the model for this widget
     * @param colorColumn the column used for coloring
     *                      ({link DecisionTreeNode.covoredColors()})
     * @param tableCollapsed true when table should be collapsed initially
     * @param chartCollapsed true when chart should be collapsed initially
     */
    public DecTreeNodeWidget(
            final HierarchicalGraphView<DecisionTreeNode> graph,
            final DecisionTreeNode decNode,
            final String colorColumn, final boolean tableCollapsed,
            final boolean chartCollapsed) {
        super(graph, decNode);
        m_tableCollapsed = tableCollapsed;
        m_chartCollapsed = chartCollapsed;
        m_colorColumn = colorColumn;
        m_displayTable = true;
        m_displayChart = true;
    }

    /**
     * @param graph the graph this widget is element of
     * @param decNode the model for this widget
     * @param colorColumn the column used for coloring
     *                      ({link DecisionTreeNode.covoredColors()})
     * @param tableCollapsed true when table should be collapsed initially
     * @param chartCollapsed true when chart should be collapsed initially
     * @param displayTable true when the table should be displayed
     * @param displayChart true when the chart should be displayed
     */
    public DecTreeNodeWidget(
            final HierarchicalGraphView<DecisionTreeNode> graph,
            final DecisionTreeNode decNode,
            final String colorColumn, final boolean tableCollapsed,
            final boolean chartCollapsed, final boolean displayTable,
            final boolean displayChart) {
        super(graph, decNode);
        m_tableCollapsed = tableCollapsed;
        m_chartCollapsed = chartCollapsed;
        m_colorColumn = colorColumn;
        m_displayTable = displayTable;
        m_displayChart = displayChart;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JComponent createComponent() {
        float scale = getScaleFactor();
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(0, 0, 0, 0);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        JPanel nodePanel = createNodeLabelPanel(scale);
        nodePanel.setOpaque(false);
        p.add(nodePanel, c);
        // add table
        if (m_displayTable) {
            c.gridy++;
            JPanel tablePanel = createTablePanel(scale);
            tablePanel.setOpaque(false);
            m_table = new CollapsiblePanel("Table:", tablePanel, scale);
            m_table.setOpaque(false);
            m_table.setCollapsed(m_tableCollapsed);
            p.add(m_table, c);
        }
        // add histogram if colors are provided
        if (m_displayChart) {
            HashMap<Color, Double> nodeColors = getUserObject().coveredColors();
            HashMap<Color, Double> rootColors =
                null != getGraphView().getRootNode()
                ? getGraphView().getRootNode().coveredColors()
                : getUserObject().coveredColors();
            if (null != nodeColors && null != rootColors
                    && rootColors.size() > 1) {
                c.gridy++;
                JPanel chartPanel = createChartPanel(nodeColors, rootColors,
                        scale);
                chartPanel.setOpaque(false);
                m_chart = new CollapsiblePanel("Chart:", chartPanel, scale);
                m_chart.setOpaque(false);
                m_chart.setCollapsed(m_chartCollapsed);
                p.add(m_chart, c);
            }
        }
        return p;
    }

    /**
     * @return the tableCollapsed
     */
    boolean getTableCollapsed() {
        return null != m_table ? m_table.isCollapsed()
                : m_tableCollapsed;
    }

    /**
     * @param collapsed the tableCollapsed to set
     */
    void setTableCollapsed(final boolean collapsed) {
        m_tableCollapsed = collapsed;
        if (m_table != null) {
            m_table.setCollapsed(collapsed);
        }
    }

    /**
     * @return the chartCollapsed
     */
    boolean getChartCollapsed() {
        return null != m_chart ? m_chart.isCollapsed()
                : m_chartCollapsed;
    }

    /**
     * @param collapsed the chartCollapsed to set
     */
    void setChartCollapsed(final boolean collapsed) {
        m_chartCollapsed = collapsed;
        if (m_chart != null) {
            m_chart.setCollapsed(collapsed);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setScaleFactor(final float scale) {
        // remember if table is collapsed
        if (null != m_table) {
            m_tableCollapsed = m_table.isCollapsed();
        }
        // remember if chart is collapsed
        if (null != m_chart) {
            m_chartCollapsed = m_chart.isCollapsed();
        }
        super.setScaleFactor(scale);
    }

    /** The panel at the top displaying the node label.
     * @param scale
     * @return A label, e.g. "Iris-versicolor (45/46)" */
    private JPanel createNodeLabelPanel(final float scale) {
        int gap = Math.round(5 * scale);
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, gap, gap));
        StringBuilder label = new StringBuilder();
        DecisionTreeNode node = getUserObject();
        DataCell majorityClass = node.getMajorityClass();
        LinkedHashMap<DataCell, Double> classCounts =
            getUserObject().getClassCounts();

        double totalClassCount = node.getEntireClassCount();
        // bug fix 6308: external PMML might not have ScoreDistribution so data is void
        // try to handle gracefully
        Double myClassCountDouble = classCounts.get(majorityClass);
        double myClassCount = myClassCountDouble == null ? 0.0 : myClassCountDouble.doubleValue();

        label.append(majorityClass.toString());
        label.append(" (").append(convertCount(myClassCount));
        label.append("/").append(convertCount(totalClassCount)).append(")");

        p.add(scaledLabel(label.toString(), scale));
        return p;
    }

    // The table
    private JPanel createTablePanel(final float scale) {
        LinkedHashMap<DataCell, Double> classCounts =
            getUserObject().getClassCounts();
        double entireClassCounts = getUserObject().getEntireClassCount();
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        int gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        int bw = Math.round(1 * scale);
        c.insets = new Insets(bw, bw, bw, bw);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 1;
        p.add(scaledLabel("Category", scale), c);
        c.gridx++;
        p.add(scaledLabel("% ", scale, SwingConstants.RIGHT), c);
        c.gridx++;
        p.add(scaledLabel("n ", scale, SwingConstants.RIGHT), c);
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        p.add(new MyJSeparator(), c);
        c.gridwidth = 1;
        DataCell majorityClass = getUserObject().getMajorityClass();
        List<Double> classFreqList = new ArrayList<Double>();
        for (DataCell cell : classCounts.keySet()) {
            JLabel classLabel = scaledLabel(cell.toString(), scale);
            c.gridy++;
            c.gridx = 0;
            p.add(classLabel, c);
            c.gridx++;
            double classFreq = classCounts.get(cell) / entireClassCounts;
            classFreqList.add(classFreq);
            p.add(scaledLabel(convertPercentage(classFreq), scale,
                    SwingConstants.RIGHT), c);
            c.gridx++;
            Double classCountValue = classCounts.get(cell);
            p.add(scaledLabel(convertCount(classCountValue), scale,
                    SwingConstants.RIGHT), c);
            if (cell.equals(majorityClass)) {
                c.gridx = 0;
                JComponent comp = new JPanel();
                comp.setMinimumSize(classLabel.getPreferredSize());
                comp.setPreferredSize(classLabel.getPreferredSize());
                comp.setBackground(new Color(225, 225, 225));
                c.gridwidth = gridwidth;
                p.add(comp, c);
                c.gridwidth = 1;
            }
        }
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = gridwidth;
        p.add(new MyJSeparator(), c);
        c.gridwidth = 1;
        c.gridy++;
        c.gridx = 0;
        p.add(scaledLabel("Total", scale), c);
        c.gridx++;
        double nominator = null != getGraphView().getRootNode()
            ? getGraphView().getRootNode().getEntireClassCount()
            : getUserObject().getEntireClassCount();
        double coverage = entireClassCounts / nominator;
        p.add(scaledLabel(convertPercentage(coverage), scale, SwingConstants.RIGHT), c);
        c.gridx++;
        p.add(scaledLabel(convertCount(entireClassCounts), scale,
                SwingConstants.RIGHT), c);
        return p;
    }

    private JLabel scaledLabel(final String label, final float scale) {
        JLabel l = new JLabel(label);
        l.setFont(l.getFont().deriveFont(l.getFont().getSize() * scale));
        return l;
    }

    private JLabel scaledLabel(final String label, final float scale,
            final int horizontalAlignment) {
        JLabel l = new JLabel(label, horizontalAlignment);
        l.setFont(l.getFont().deriveFont(l.getFont().getSize() * scale));
        return l;
    }

    // the chart
    private JPanel createChartPanel(final HashMap<Color, Double> nodeColors,
            final HashMap<Color, Double> rootColors, final float scale) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        int gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        int bw = Math.round(1 * scale);
        c.insets = new Insets(bw, bw, bw, bw);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        if (null != m_colorColumn) {
            p.add(scaledLabel("Color column: " + m_colorColumn, scale), c);
            c.gridy++;
        }
        double entireClassCounts = 0;
        for (Double d : nodeColors.values()) {
            entireClassCounts += d;
        }
        List<Double> classFreq = new ArrayList<Double>();
        List<Color> color = new ArrayList<Color>();
        for (Color co : rootColors.keySet()) {
            Double freq = null != nodeColors.get(co)
                                ? nodeColors.get(co) : 0;
            color.add(co);
            classFreq.add(freq / entireClassCounts);
        }
        c.gridwidth = gridwidth;
        MyHistogram histogram = new MyHistogram(classFreq, color, scale);
        p.add(histogram, c);
        c.gridwidth = 1;

        return p;
    }

    private static final DecimalFormat DECIMAL_FORMAT_ONE = initFormat("0.0");
    private static final DecimalFormat DECIMAL_FORMAT = initFormat("0");
    private static DecimalFormat initFormat(final String pattern) {
        DecimalFormat df = new DecimalFormat(pattern);
        df.setGroupingUsed(true);
        df.setGroupingSize(3);
        return df;
    }

    private static String convertCount(final double value) {
        // show integer as integer (without decimal places)
        if (Double.compare(Math.ceil(value), value) == 0) {
            return DECIMAL_FORMAT.format(value);
        }
        return DECIMAL_FORMAT_ONE.format(value);
    }

    private static String convertPercentage(final double value) {
        return DECIMAL_FORMAT_ONE.format(100.0 * value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConnectorLabelAbove() {
        String str = getUserObject().getPrefix();
        if (null != str && !str.equals("root")) {
            if (getUserObject().getParent() instanceof DecisionTreeNodeSplit) {
                DecisionTreeNodeSplit parent =
                    (DecisionTreeNodeSplit)getUserObject().getParent();
                if (str.startsWith(parent.getSplitAttr())) {
                    str = str.substring(parent.getSplitAttr().length());
                    return str.trim();
                }
            }
            return str;
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConnectorLabelBelow() {
        if (getUserObject() instanceof DecisionTreeNodeSplit) {
            return ((DecisionTreeNodeSplit)getUserObject()).getSplitAttr();
        } else {
            return null;
        }
    }

    private static class MyJSeparator extends JComponent {
        private static final long serialVersionUID = -5611048590057773103L;

        /**
         * {@inheritDoc}
         */
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(2, 2);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void paintComponent(final Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            Paint origPaint = g2.getPaint();
            g2.setPaint(ColorAttr.BORDER);
            Dimension s = getSize();
            g2.drawLine(0, s.height / 2, s.width, s.height / 2);
            g2.setPaint(origPaint);
        }
    }

    private static class MyHistogram extends JComponent {
        private static final long serialVersionUID = 1797977517857868138L;

        private List<Double> m_classFreqList;
        private List<Color> m_colorList;

        private float m_scale;

        /**
         * @param classFreqList
         * @param colorList
         * @param scale the scale factor
         */
        public MyHistogram(final List<Double> classFreqList,
                final List<Color> colorList, final float scale) {
            m_classFreqList = classFreqList;
            m_colorList = colorList;
            m_scale = scale;
//            super.setDoubleBuffered(false);
//            super.setBackground(ColorAttr.BACKGROUND);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(2,
                    Math.round(40 * m_scale));
        }
        /**
         * {@inheritDoc}
         */
        @Override
        protected void paintComponent(final Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            Rectangle b = new Rectangle(
                   getInsets().left, getInsets().top,
                   getSize().width - getInsets().left - getInsets().right - 1,
                   getSize().height - getInsets().top - getInsets().bottom - 1);
            Color prevColor = g.getColor();
            g2.setColor(ColorAttr.BACKGROUND);
            g2.fill(b);
            // draw histogram rectangles
            int barCount = m_classFreqList.size();
            double barWidth = b.width / (double)barCount;
            for (int i = 0; i < m_classFreqList.size(); i++) {
                Double f = m_classFreqList.get(i);
                int barHeight = (int)Math.round(f * b.height);
                Rectangle barBounds = new Rectangle(
                        b.x + (int)(i * barWidth),
                        b.y + b.height - barHeight,
                        (int)barWidth, barHeight);
                g2.setColor(m_colorList.get(i));
                g2.fillRect(barBounds.x, barBounds.y,
                        barBounds.width, barBounds.height);
                g2.setColor(ColorAttr.BORDER);
                g2.drawRect(barBounds.x, barBounds.y,
                        barBounds.width, barBounds.height);
            }
            // draw border of the histogram
            int width = (int)(barWidth * m_classFreqList.size());
            g2.setColor(ColorAttr.BORDER);
            g2.drawLine(b.x, b.y, b.x, b.y + b.height - 1);
            g2.drawLine(b.x, b.y + b.height, b.x + width, b.y + b.height);
            g2.drawLine(b.x + width, b.y, b.x + width, b.y + b.height - 1);
            g2.setColor(prevColor);
        }
    }
}
