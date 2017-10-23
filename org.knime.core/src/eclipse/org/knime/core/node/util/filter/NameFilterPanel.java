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
package org.knime.core.node.util.filter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.util.ListModelFilterUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.util.filter.NameFilterConfiguration.EnforceOption;

/**
 * Name filter panel with additional enforce include/exclude radio buttons.
 *
 * @author Thomas Gabriel, KNIME.com AG, Zurich, Switzerland
 * @author Patrick Winter, KNIME.com AG, Zurich, Switzerland
 *
 * @since 2.6
 *
 * @param <T> the instance T this object is parametrized on
 */
@SuppressWarnings("serial")
public abstract class NameFilterPanel<T> extends JPanel {

    /** Name for the filter by name type. */
    private static final String NAME = "Manual Selection";

    /** Line border for include names. */
    private static final Border INCLUDE_BORDER = BorderFactory.createLineBorder(new Color(0, 221, 0), 2);

    /** Line border for exclude names. */
    private static final Border EXCLUDE_BORDER = BorderFactory.createLineBorder(new Color(240, 0, 0), 2);

    /** Include list. */
    @SuppressWarnings("rawtypes")
    private final JList m_inclList;

    /** Include model. */
    @SuppressWarnings("rawtypes")
    private final DefaultListModel m_inclMdl;

    /** Exclude list. */
    @SuppressWarnings("rawtypes")
    private final JList m_exclList;

    /** Exclude model. */
    @SuppressWarnings("rawtypes")
    private final DefaultListModel m_exclMdl;

    /** Highlight all search hits in the include model. */
    private final JCheckBox m_markAllHitsIncl;

    /** Highlight all search hits in the exclude model. */
    private final JCheckBox m_markAllHitsExcl;

    /** Radio button for the exclusion option. */
    private final JRadioButton m_enforceExclusion;

    /** Radio button for the inclusion option. */
    private final JRadioButton m_enforceInclusion;

    /** Remove all button. */
    private final JButton m_remAllButton;

    /** Remove button. */
    private final JButton m_remButton;

    /** Add all button. */
    private final JButton m_addAllButton;

    /** Add button. */
    private final JButton m_addButton;

    /** Search Field in include list. */
    private final JTextField m_searchFieldIncl;

    /** Search Button for include list. */
    private final JButton m_searchButtonIncl;

    /** Search Field in exclude list. */
    private final JTextField m_searchFieldExcl;

    /** Search Button for exclude list. */
    private final JButton m_searchButtonExcl;

    /** List of T elements to keep initial ordering of names. */
    private final LinkedHashSet<T> m_order = new LinkedHashSet<T>();

    /** Border of the include panel, keep it so we can change the title. */
    private final TitledBorder m_includeBorder;

    /** Border of the include panel, keep it so we can change the title. */
    private final TitledBorder m_excludeBorder;

    private final HashSet<T> m_hideNames = new HashSet<T>();

    private List<ChangeListener> m_listeners;

    /** The filter used to filter out/in valid elements. */
    private InputFilter<T> m_filter;

    private String m_currentType = NameFilterConfiguration.TYPE;

    private ButtonGroup m_typeGroup;

    private JPanel m_typePanel;

    private JPanel m_filterPanel;

    private JPanel m_nameFilterPanel;

    /**
     * additional checkbox for the middle button panel
     * @since 3.4
     */
    private JCheckBox m_additionalCheckbox;

    private PatternFilterPanel<T> m_patternPanel;

    private JRadioButton m_nameButton;

    private JRadioButton m_patternButton;

    private TreeMap<Integer, String> m_typePriorities = new TreeMap<Integer, String>();

    private List<String> m_invalidIncludes = new ArrayList<String>(0);

    private List<String> m_invalidExcludes = new ArrayList<String>(0);

    private String[] m_availableNames = new String[0];

    /**
     * Creates a panel allowing the user to select elements.
     */
    protected NameFilterPanel() {
        this(false, null);
    }

    /**
     * Creates a new filter panel with three component which are the include list, button panel to shift elements
     * between the two lists, and the exclude list. The include list then will contain all values to filter.
     *
     * @param showSelectionListsOnly if set, the component shows only the basic include/exclude selection panel - no
     *            additional search boxes, force-include-options, etc.
     */
    protected NameFilterPanel(final boolean showSelectionListsOnly) {
        this(showSelectionListsOnly, null);
    }

    /**
     * Creates a new filter column panel with three component which are the include list, button panel to shift elements
     * between the two lists, and the exclude list. The include list then will contain all values to filter.
     * Additionally a {@link InputFilter} can be specified, based on which the shown items are shown or not. The filter
     * can be <code>null
     * </code>, in which case it is simply not used at all.
     *
     * @param showSelectionListsOnly if set, the component shows only the basic include/exclude selection panel - no
     *            additional search boxes, force-include-options, etc.
     * @param filter A filter that specifies which items are shown in the panel (and thus are possible to include or
     *            exclude) and which are not shown.
     */
    protected NameFilterPanel(final boolean showSelectionListsOnly, final InputFilter<T> filter) {
        this(showSelectionListsOnly, filter, null);
    }

    /**
     * Creates a new filter column panel with three component which are the include list, button panel to shift elements
     * between the two lists, and the exclude list. The include list then will contain all values to filter.
     * Additionally a {@link InputFilter} can be specified, based on which the shown items are shown or not. The filter
     * can be <code>null</code>, in which case it is simply not used at all.
     *
     * @param showSelectionListsOnly if set, the component shows only the basic include/exclude selection panel - no
     *            additional search boxes, force-include-options, etc.
     * @param filter A filter that specifies which items are shown in the panel (and thus are possible to include or
     *            exclude) and which are not shown.
     * @param searchLabel text to show next to the search fields
     * @since 3.4
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected NameFilterPanel(final boolean showSelectionListsOnly, final InputFilter<T> filter,
        final String searchLabel) {
        super(new GridLayout(1, 1));
        m_filter = filter;
        m_patternPanel = getPatternFilterPanel(filter);
        m_patternPanel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                fireFilteringChangedEvent();
            }
        });
        m_patternButton = createButtonToFilterPanel(PatternFilterConfiguration.TYPE, "Wildcard/Regex Selection");

        // keeps buttons such add 'add', 'add all', 'remove', and 'remove all'
        final JPanel buttonPan = new JPanel();
        buttonPan.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        buttonPan.setLayout(new BoxLayout(buttonPan, BoxLayout.Y_AXIS));
        buttonPan.add(Box.createVerticalStrut(20));

        m_addButton = new JButton("add >>");
        m_addButton.setMaximumSize(new Dimension(125, 25));
        buttonPan.add(m_addButton);
        m_addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent ae) {
                onAddIt();
            }
        });
        buttonPan.add(Box.createVerticalStrut(25));

        m_addAllButton = new JButton("add all >>");
        m_addAllButton.setMaximumSize(new Dimension(125, 25));
        buttonPan.add(m_addAllButton);
        m_addAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent ae) {
                onAddAll();
            }
        });
        buttonPan.add(Box.createVerticalStrut(25));

        m_remButton = new JButton("<< remove");
        m_remButton.setMaximumSize(new Dimension(125, 25));
        buttonPan.add(m_remButton);
        m_remButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent ae) {
                onRemIt();
            }
        });
        buttonPan.add(Box.createVerticalStrut(25));

        m_remAllButton = new JButton("<< remove all");
        m_remAllButton.setMaximumSize(new Dimension(125, 25));
        buttonPan.add(m_remAllButton);
        m_remAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent ae) {
                onRemAll();
            }
        });
        m_additionalCheckbox = createAdditionalButton();
        if (m_additionalCheckbox != null){
            buttonPan.add(Box.createVerticalStrut(25));
            m_additionalCheckbox.setMaximumSize(new Dimension(125, 25));
            buttonPan.add(m_additionalCheckbox);
            m_additionalCheckbox.addActionListener(e -> fireFilteringChangedEvent());
        }
        buttonPan.add(Box.createVerticalStrut(20));
        buttonPan.add(Box.createGlue());

        // include list
        m_inclMdl = new DefaultListModel();
        m_inclList = new JList(m_inclMdl);
        m_inclList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        m_inclList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent me) {
                if (me.getClickCount() == 2) {
                    onRemIt();
                    me.consume();
                }
            }
        });
        final JScrollPane jspIncl = new JScrollPane(m_inclList);
        jspIncl.setMinimumSize(new Dimension(150, 155));

        m_searchFieldIncl = new JTextField(8);
        m_searchButtonIncl = new JButton("Search");
        ActionListener actionListenerIncl = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                ListModelFilterUtils.onSearch(m_inclList, m_inclMdl, m_searchFieldIncl.getText(),
                    m_markAllHitsIncl.isSelected());
            }
        };
        m_searchFieldIncl.addActionListener(actionListenerIncl);
        m_searchButtonIncl.addActionListener(actionListenerIncl);
        JPanel inclSearchPanel = new JPanel(new BorderLayout());
        inclSearchPanel.add(new JLabel((searchLabel != null ? searchLabel : "Column(s)")+": "), BorderLayout.WEST);
        inclSearchPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        inclSearchPanel.add(m_searchFieldIncl, BorderLayout.CENTER);
        inclSearchPanel.add(m_searchButtonIncl, BorderLayout.EAST);
        m_markAllHitsIncl = new JCheckBox("Select all search hits");
        ActionListener actionListenerAllIncl = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                m_inclList.clearSelection();
                ListModelFilterUtils.onSearch(m_inclList, m_inclMdl, m_searchFieldIncl.getText(),
                    m_markAllHitsIncl.isSelected());
            }
        };
        m_markAllHitsIncl.addActionListener(actionListenerAllIncl);
        inclSearchPanel.add(m_markAllHitsIncl, BorderLayout.PAGE_END);
        JPanel includePanel = new JPanel(new BorderLayout());
        m_includeBorder = BorderFactory.createTitledBorder(INCLUDE_BORDER, " Include ");
        includePanel.setBorder(m_includeBorder);
        includePanel.add(inclSearchPanel, BorderLayout.NORTH);
        includePanel.add(jspIncl, BorderLayout.CENTER);

        // exclude list
        m_exclMdl = new DefaultListModel();
        m_exclList = new JList(m_exclMdl);
        m_exclList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        m_exclList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent me) {
                if (me.getClickCount() == 2) {
                    onAddIt();
                    me.consume();
                }
            }
        });

        // set renderer for items in the in- and exclude list
        m_inclList.setCellRenderer(getListCellRenderer());
        m_exclList.setCellRenderer(getListCellRenderer());

        final JScrollPane jspExcl = new JScrollPane(m_exclList);
        jspExcl.setMinimumSize(new Dimension(150, 155));

        m_searchFieldExcl = new JTextField(8);
        m_searchButtonExcl = new JButton("Search");
        ActionListener actionListenerExcl = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                ListModelFilterUtils.onSearch(m_exclList, m_exclMdl, m_searchFieldExcl.getText(),
                    m_markAllHitsExcl.isSelected());
            }
        };
        m_searchFieldExcl.addActionListener(actionListenerExcl);
        m_searchButtonExcl.addActionListener(actionListenerExcl);
        JPanel exclSearchPanel = new JPanel(new BorderLayout());
        exclSearchPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        exclSearchPanel.add(new JLabel((searchLabel != null ? searchLabel : "Column(s)")+": "), BorderLayout.WEST);
        exclSearchPanel.add(m_searchFieldExcl, BorderLayout.CENTER);
        exclSearchPanel.add(m_searchButtonExcl, BorderLayout.EAST);
        m_markAllHitsExcl = new JCheckBox("Select all search hits");
        ActionListener actionListenerAllExcl = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                m_exclList.clearSelection();
                ListModelFilterUtils.onSearch(m_exclList, m_exclMdl, m_searchFieldExcl.getText(),
                    m_markAllHitsExcl.isSelected());
            }
        };
        m_markAllHitsExcl.addActionListener(actionListenerAllExcl);
        exclSearchPanel.add(m_markAllHitsExcl, BorderLayout.PAGE_END);
        JPanel excludePanel = new JPanel(new BorderLayout());
        m_excludeBorder = BorderFactory.createTitledBorder(EXCLUDE_BORDER, " Exclude ");
        excludePanel.setBorder(m_excludeBorder);
        excludePanel.add(exclSearchPanel, BorderLayout.NORTH);
        excludePanel.add(jspExcl, BorderLayout.CENTER);

        JPanel buttonPan2 = new JPanel(new GridLayout());
        Border border = BorderFactory.createTitledBorder(" Select ");
        buttonPan2.setBorder(border);
        buttonPan2.add(buttonPan);

        // add force incl/excl buttons
        m_enforceInclusion = new JRadioButton("Enforce inclusion");
        m_enforceExclusion = new JRadioButton("Enforce exclusion");
        m_enforceInclusion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                cleanInvalidValues();
            }
        });
        m_enforceExclusion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                cleanInvalidValues();
            }
        });
        if (!showSelectionListsOnly) {
            final ButtonGroup forceGroup = new ButtonGroup();
            m_enforceInclusion.setToolTipText("Force the set of included names to stay the same.");
            forceGroup.add(m_enforceInclusion);
            includePanel.add(m_enforceInclusion, BorderLayout.SOUTH);
            m_enforceExclusion.setToolTipText("Force the set of excluded names to stay the same.");
            forceGroup.add(m_enforceExclusion);
            m_enforceExclusion.doClick();
            excludePanel.add(m_enforceExclusion, BorderLayout.SOUTH);
        }

        // adds include, button, exclude component
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.X_AXIS));
        center.add(excludePanel);
        center.add(buttonPan2);
        center.add(includePanel);
        m_nameFilterPanel = center;
        initPanel();
    }

    /**
     * @param filter
     * @return the PatternFilterPanel to be used.
     * @since 3.4
     * @noreference This method is not intended to be referenced by clients outside the KNIME core.
     */
    protected PatternFilterPanel<T> getPatternFilterPanel(final InputFilter<T> filter) {
        return new PatternFilterPanel<T>(this, filter);
    }

    /**
     * @return an additional button to be added to the center panel. To be overwritten by subclasses
     * @since 3.4
     * @nooverride This method is not intended to be re-implemented or extended by clients outside KNIME core.
     */
    protected JCheckBox createAdditionalButton(){
        return null;
    }

    /** The additional button as created by {@link #createAdditionalButton()} or an empty optional if the method
     * was not overridden.
     * @return the additionalCheckbox
     * @since 3.4
     */
    protected final Optional<JCheckBox> getAdditionalButton() {
        return Optional.ofNullable(m_additionalCheckbox);
    }

    /** @return a list cell renderer from items to be rendered in the filer */
    @SuppressWarnings("rawtypes")
    protected abstract ListCellRenderer getListCellRenderer();

    /**
     * Get the a T for the given name.
     *
     * @param name a string to retrieve T for.
     * @return an instance of T
     */
    protected abstract T getTforName(final String name);

    /**
     * Returns the name for the given T.
     *
     * @param t to retrieve the name for
     * @return the name represented by T
     */
    protected abstract String getNameForT(final T t);

    /**
     * Enables or disables all components on this panel. {@inheritDoc}
     */
    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        m_searchFieldIncl.setEnabled(enabled);
        m_searchButtonIncl.setEnabled(enabled);
        m_searchFieldExcl.setEnabled(enabled);
        m_searchButtonExcl.setEnabled(enabled);
        m_inclList.setEnabled(enabled);
        m_exclList.setEnabled(enabled);
        m_markAllHitsIncl.setEnabled(enabled);
        m_markAllHitsExcl.setEnabled(enabled);
        m_remAllButton.setEnabled(enabled);
        m_remButton.setEnabled(enabled);
        m_addAllButton.setEnabled(enabled);
        m_addButton.setEnabled(enabled);
        m_enforceInclusion.setEnabled(enabled);
        m_enforceExclusion.setEnabled(enabled);
        if (m_additionalCheckbox != null) {
            m_additionalCheckbox.setEnabled(enabled);
        }
        Enumeration<AbstractButton> buttons = m_typeGroup.getElements();
        while (buttons.hasMoreElements()) {
            buttons.nextElement().setEnabled(enabled);
        }
        m_patternPanel.setEnabled(enabled);
    }

    /**
     * Adds a listener which gets informed whenever the column filtering changes.
     *
     * @param listener the listener
     */
    public void addChangeListener(final ChangeListener listener) {
        if (m_listeners == null) {
            m_listeners = new ArrayList<ChangeListener>();
        }
        m_listeners.add(listener);
    }

    /**
     * Removes the given listener from this filter column panel.
     *
     * @param listener the listener.
     */
    public void removeChangeListener(final ChangeListener listener) {
        if (m_listeners != null) {
            m_listeners.remove(listener);
        }
    }

    /**
     * Removes all column filter change listener.
     */
    public void removeAllColumnFilterChangeListener() {
        if (m_listeners != null) {
            m_listeners.clear();
        }
    }

    /**
     * Updates this filter panel by removing all current selections from the include and exclude list. The include list
     * will contains all column names from the spec afterwards.
     *
     * @param config to be loaded from
     * @param names array of names to be included or excluded (preserve order)
     */
    public void loadConfiguration(final NameFilterConfiguration config, final String[] names) {
        final List<String> ins = Arrays.asList(config.getIncludeList());
        final List<String> exs = Arrays.asList(config.getExcludeList());
        if (supportsInvalidValues()) {
            m_invalidIncludes = new ArrayList<String>(Arrays.asList(config.getRemovedFromIncludeList()));
            m_invalidExcludes = new ArrayList<String>(Arrays.asList(config.getRemovedFromExcludeList()));
        }
        this.update(ins, exs, names);
        switch (config.getEnforceOption()) {
            case EnforceExclusion:
                m_enforceExclusion.doClick();
                break;
            case EnforceInclusion:
                m_enforceInclusion.doClick();
                break;
        }
        m_patternPanel.loadConfiguration(config.getPatternConfig(), names);
        setPatternFilterEnabled(config.isPatternFilterEnabled());
        m_currentType = config.getType();
        boolean typeOk = false;
        Enumeration<AbstractButton> buttons = m_typeGroup.getElements();
        while (buttons.hasMoreElements()) {
            AbstractButton button = buttons.nextElement();
            if (button.getActionCommand().equals(m_currentType)) {
                button.setSelected(true);
                typeOk = true;
                break;
            }
        }
        if (!typeOk) {
            m_currentType = NameFilterConfiguration.TYPE;
            m_nameButton.setSelected(true);
        }
        updateFilterPanel();
        repaint();
    }

    /**
     * Update this panel with the given include, exclude lists and the array of all possible values.
     *
     * @param ins include list
     * @param exs exclude list
     * @param names all available names
     * @noreference This method is not intended to be referenced by clients.
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     */
    @SuppressWarnings("unchecked")
    public void update(final List<String> ins, final List<String> exs, final String[] names) {
        m_availableNames = names;
        // clear internal member
        m_order.clear();
        m_inclMdl.removeAllElements();
        m_exclMdl.removeAllElements();
        m_hideNames.clear();

        for (final String name : m_invalidIncludes) {
            final T t = getTforName(name);
            m_inclMdl.addElement(t);
            m_order.add(t);
        }
        for (final String name : m_invalidExcludes) {
            final T t = getTforName(name);
            m_exclMdl.addElement(t);
            m_order.add(t);
        }

        for (final String name : names) {
            final T t = getTforName(name);

            // continue if filter is set and current item t is filtered out
            if (m_filter != null) {
                if (!m_filter.include(t)) {
                    continue;
                }
            }

            // if item is not filtered out, add it to include or exclude list
            if (ins.contains(name)) {
                m_inclMdl.addElement(t);
            } else if (exs.contains(name)) {
                m_exclMdl.addElement(t);
            }
            m_order.add(t);
        }

        repaint();
    }

    /**
     * Save this configuration.
     *
     * @param config settings to be saved into
     */
    public void saveConfiguration(final NameFilterConfiguration config) {
        // save enforce option
        if (m_enforceExclusion.isSelected()) {
            config.setEnforceOption(EnforceOption.EnforceExclusion);
        } else {
            config.setEnforceOption(EnforceOption.EnforceInclusion);
        }

        // save include list
        final Set<T> incls = getIncludeList();
        final String[] ins = new String[incls.size()];
        int index = 0;
        for (T t : incls) {
            ins[index++] = getNameForT(t);
        }
        config.setIncludeList(ins);

        // save exclude option
        final Set<T> excls = getExcludeList();
        final String[] exs = new String[excls.size()];
        index = 0;
        for (T t : excls) {
            exs[index++] = getNameForT(t);
        }
        config.setExcludeList(exs);

        config.setRemovedFromIncludeList(getInvalidIncludes());
        config.setRemovedFromExcludeList(getInvalidExcludes());

        try {
            config.setType(m_currentType);
        } catch (InvalidSettingsException e) {
            NodeLogger.getLogger(getClass()).coding("Could not save settings as the selected filter type '"
                    + m_currentType + "' - this was a valid type when the configuration was loaded");
        }
        m_patternPanel.saveConfiguration(config.getPatternConfig());
    }

    /** @return list of all included T's */
    public Set<T> getIncludeList() {
        final Set<T> list = new LinkedHashSet<T>();
        for (int i = 0; i < m_inclMdl.getSize(); i++) {
            @SuppressWarnings("unchecked")
            T t = (T)m_inclMdl.getElementAt(i);
            if (!isInvalidValue(getNameForT(t))) {
                list.add(t);
            }
        }
        return list;
    }

    /** @return list of all excluded T's */
    public Set<T> getExcludeList() {
        final Set<T> list = new LinkedHashSet<T>();
        for (int i = 0; i < m_exclMdl.getSize(); i++) {
            @SuppressWarnings("unchecked")
            T t = (T)m_exclMdl.getElementAt(i);
            if (!isInvalidValue(getNameForT(t))) {
                list.add(t);
            }
        }
        return list;
    }

    /**
     * Get the invalid values in the include list.
     */
    private String[] getInvalidIncludes() {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < m_inclMdl.getSize(); i++) {
            @SuppressWarnings("unchecked")
            String name = getNameForT((T)m_inclMdl.getElementAt(i));
            if (isInvalidValue(name)) {
                list.add(name);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Get the invalid values in the exclude list.
     */
    private String[] getInvalidExcludes() {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < m_exclMdl.getSize(); i++) {
            @SuppressWarnings("unchecked")
            String name = getNameForT((T)m_exclMdl.getElementAt(i));
            if (isInvalidValue(name)) {
                list.add(name);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Check if a value is invalid.
     *
     * @param value The value to check
     * @return true if the given name is invalid, false otherwise
     */
    private boolean isInvalidValue(final String value) {
        return m_invalidIncludes.contains(value) || m_invalidExcludes.contains(value);
    }

    /**
     * Removes the given columns form either include or exclude list and notifies all listeners. Does not throw an
     * exception if the argument contains <code>null</code> elements or is not contained in any of the lists.
     *
     * @param names a list of names to hide from the filter
     */
    public void hideNames(final T... names) {
        boolean changed = false;
        for (T name : names) {
            if (m_inclMdl.contains(name)) {
                m_hideNames.add(name);
                changed |= m_inclMdl.removeElement(name);
            } else if (m_exclMdl.contains(name)) {
                m_hideNames.add(name);
                changed |= m_exclMdl.removeElement(name);
            }
        }
        if (changed) {
            fireFilteringChangedEvent();
        }
    }

    /** Re-adds all remove/hidden names to the exclude list. */
    @SuppressWarnings("unchecked")
    public void resetHiding() {
        if (m_hideNames.isEmpty()) {
            return;
        }
        // add all selected elements from the include to the exclude list
        HashSet<Object> hash = new HashSet<Object>();
        hash.addAll(m_hideNames);
        for (Enumeration<?> e = m_exclMdl.elements(); e.hasMoreElements();) {
            hash.add(e.nextElement());
        }
        m_exclMdl.removeAllElements();
        for (T name : m_order) {
            if (hash.contains(name)) {
                m_exclMdl.addElement(name);
            }
        }
        m_hideNames.clear();
    }

    /**
     * Sets the title of the include panel.
     *
     * @param title the new title
     */
    public void setIncludeTitle(final String title) {
        m_includeBorder.setTitle(title);
    }

    /**
     * Sets the title of the exclude panel.
     *
     * @param title the new title
     */
    public void setExcludeTitle(final String title) {
        m_excludeBorder.setTitle(title);
    }

    /**
     * Setter for the original "Remove All" button.
     *
     * @param text the new button title
     */
    public void setRemoveAllButtonText(final String text) {
        m_remAllButton.setText(text);
    }

    /**
     * Setter for the original "Add All" button.
     *
     * @param text the new button title
     */
    public void setAddAllButtonText(final String text) {
        m_addAllButton.setText(text);
    }

    /**
     * Setter for the original "Remove" button.
     *
     * @param text the new button title
     */
    public void setRemoveButtonText(final String text) {
        m_remButton.setText(text);
    }

    /**
     * Setter for the original "Add" button.
     *
     * @param text the new button title
     */
    public void setAddButtonText(final String text) {
        m_addButton.setText(text);
    }

    /**
     * Sets the internal used {@link InputFilter} and calls the {@link #update(List, List, String[])} method to update
     * the panel.
     *
     * @param filter the new {@link InputFilter} to use
     */
    public void setNameFilter(final InputFilter<T> filter) {
        List<String> inclList = new ArrayList<String>(getIncludedNamesAsSet());
        List<String> exclList = new ArrayList<String>(getExcludedNamesAsSet());
        m_filter = filter;
        m_patternPanel.setFilter(filter);
        update(inclList, exclList, m_availableNames);
    }

    /**
     * Returns a set of the names of all included items.
     *
     * @return a set of the names of all included items.
     */
    public Set<String> getIncludedNamesAsSet() {
        Set<T> inclList = getIncludeList();
        Set<String> inclNames = new LinkedHashSet<String>(inclList.size());
        for (T t : inclList) {
            inclNames.add(getNameForT(t));
        }
        return inclNames;
    }

    /**
     * Returns a set of the names of all excluded items.
     *
     * @return a set of the names of all excluded items.
     */
    public Set<String> getExcludedNamesAsSet() {
        Set<T> exclList = getExcludeList();
        Set<String> exclNames = new LinkedHashSet<String>(exclList.size());
        for (T t : exclList) {
            exclNames.add(getNameForT(t));
        }
        return exclNames;
    }

    /**
     * Returns all values include and exclude in its original order they have added to this panel.
     *
     * @return a set of string containing all values from the in- and exclude list
     */
    public Set<String> getAllValues() {
        final Set<String> set = new LinkedHashSet<String>();
        for (T t : m_order) {
            set.add(getNameForT(t));
        }
        return Collections.unmodifiableSet(set);
    }

    /**
     * Returns all objects T in its original order.
     *
     * @return a set of T objects retrieved from the in- and exclude list
     */
    public Set<T> getAllValuesT() {
        return Collections.unmodifiableSet(m_order);
    }

    /**
     * Adds the type to the given radio button. Used by subclasses to add a different type of filtering.
     *
     * @param radioButton Radio button to the type that will be added.
     * @param priority the priority of this type (the bigger, the further to the right). The priority of the default
     * type is 0 while the others are usually their FILTER_BY_X flags. If the given priority is already present than a
     * priority bigger then the currently biggest will be used.
     * @see NameFilterConfiguration#setType(String)
     * @since 2.9
     */
    protected void addType(final JRadioButton radioButton, final int priority) {
        int correctPriority = priority;
        if (m_typePriorities.containsKey(priority)) {
            correctPriority = m_typePriorities.lastKey() + 1;
        }
        m_typePriorities.put(correctPriority, radioButton.getActionCommand());
        m_typeGroup.add(radioButton);
        m_typePanel.removeAll();
        Map<String, AbstractButton> buttonMap = new LinkedHashMap<String, AbstractButton>();
        Enumeration<AbstractButton> buttons = m_typeGroup.getElements();
        while (buttons.hasMoreElements()) {
            AbstractButton button = buttons.nextElement();
            buttonMap.put(button.getActionCommand(), button);
        }
        for (String type : m_typePriorities.values()) {
            m_typePanel.add(buttonMap.get(type));
        }
        radioButton.setEnabled(isEnabled());
        updateTypePanel();
    }

    /**
     * Remove the type to the given radio button.
     *
     * @param radioButton Radio button to the type that will be removed.
     * @since 2.9
     */
    protected void removeType(final JRadioButton radioButton) {
        m_typeGroup.remove(radioButton);
        m_typePanel.remove(radioButton);
        String type = radioButton.getActionCommand();
        for (Entry<Integer, String> entry : m_typePriorities.entrySet()) {
            if (type.equals(entry.getValue())) {
                m_typePriorities.remove(entry.getKey());
                break;
            }
        }
        // Reset to default type if current type has been removed
        if (m_currentType.equals(radioButton.getActionCommand())) {
            m_currentType = NameFilterConfiguration.TYPE;
            updateFilterPanel();
        }
        updateTypePanel();
    }

    /**
     * Returns the panel to the given filter type.
     *
     * @param type The type
     * @return The panel to the type
     * @since 2.9
     */
    protected JPanel getFilterPanel(final String type) {
        return new JPanel();
    }

    /**
     * Creates a JRadioButton to the given FilterTypePanel.
     *
     * The created button will be initialized with the correct description, and action listener.
     *
     * @param actionCommand The action command that identifies the type that this button belongs to
     * @param label The label of this button that is shown to the user
     * @return The JRadioButton
     * @since 2.9
     */
    protected JRadioButton createButtonToFilterPanel(final String actionCommand, final String label) {
        JRadioButton button = new JRadioButton(label);
        button.setActionCommand(actionCommand);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (e.getActionCommand() != null) {
                    String oldType = m_currentType;
                    m_currentType = e.getActionCommand();
                    if (!m_currentType.equals(oldType)) {
                        fireFilteringChangedEvent();
                    }
                    updateFilterPanel();
                }
            }
        });
        return button;
    }

    /**
     * Informs the registered listeners of changes to the configuration.
     *
     * @since 2.9
     */
    protected void fireFilteringChangedEvent() {
        if (m_listeners != null) {
            for (ChangeListener listener : m_listeners) {
                listener.stateChanged(new ChangeEvent(this));
            }
        }
    }

    /**
     * Checks if class supports the creation of invalid values.
     *
     * If the class does support invalid values it must return an object representing the invalid value in the
     * getTForName() method and must be able to recreate the name from this object in the getNameForT() method.
     *
     * @return true if the class supports invalid values, false otherwise
     * @since 2.10
     */
    protected boolean supportsInvalidValues() {
        return false;
    }

    /**
     * Called by the 'remove >>' button to exclude the selected elements from the include list.
     */
    @SuppressWarnings("unchecked")
    private void onRemIt() {
        // add all selected elements from the include to the exclude list
        @SuppressWarnings("deprecation")
        Object[] o = m_inclList.getSelectedValues();
        HashSet<Object> hash = new HashSet<Object>();
        hash.addAll(Arrays.asList(o));
        for (Enumeration<?> e = m_exclMdl.elements(); e.hasMoreElements();) {
            hash.add(e.nextElement());
        }
        boolean changed = false;
        for (int i = 0; i < o.length; i++) {
            changed |= m_inclMdl.removeElement(o[i]);
        }
        m_exclMdl.removeAllElements();
        for (T c : m_order) {
            if (hash.contains(c)) {
                m_exclMdl.addElement(c);
                String name = getNameForT(c);
                if (m_invalidIncludes.remove(name)) {
                    m_invalidExcludes.add(name);
                }
            }
        }
        if (changed) {
            cleanInvalidValues();
            fireFilteringChangedEvent();
        }
    }

    /**
     * Called by the 'remove >>' button to exclude all elements from the include list.
     */
    @SuppressWarnings("unchecked")
    private void onRemAll() {
        boolean changed = m_inclMdl.elements().hasMoreElements();
        m_inclMdl.removeAllElements();
        m_exclMdl.removeAllElements();
        m_invalidExcludes.addAll(m_invalidIncludes);
        m_invalidIncludes.clear();
        for (T c : m_order) {
            if (!m_hideNames.contains(c)) {
                m_exclMdl.addElement(c);
            }
        }
        if (changed) {
            cleanInvalidValues();
            fireFilteringChangedEvent();
        }
    }

    /**
     * Called by the '<< add' button to include the selected elements from the exclude list.
     */
    @SuppressWarnings("unchecked")
    private void onAddIt() {
        // add all selected elements from the exclude to the include list
        @SuppressWarnings("deprecation")
        Object[] o = m_exclList.getSelectedValues();
        HashSet<Object> hash = new HashSet<Object>();
        hash.addAll(Arrays.asList(o));
        for (Enumeration<?> e = m_inclMdl.elements(); e.hasMoreElements();) {
            hash.add(e.nextElement());
        }
        boolean changed = false;
        for (int i = 0; i < o.length; i++) {
            changed |= m_exclMdl.removeElement(o[i]);
        }
        m_inclMdl.removeAllElements();
        for (T c : m_order) {
            if (hash.contains(c)) {
                m_inclMdl.addElement(c);
                String name = getNameForT(c);
                if (m_invalidExcludes.remove(name)) {
                    m_invalidIncludes.add(name);
                }
            }
        }
        if (changed) {
            cleanInvalidValues();
            fireFilteringChangedEvent();
        }
    }

    /**
     * Called by the '<< add all' button to include all elements from the exclude list.
     */
    @SuppressWarnings("unchecked")
    private void onAddAll() {
        boolean changed = m_exclMdl.elements().hasMoreElements();
        m_inclMdl.removeAllElements();
        m_exclMdl.removeAllElements();
        m_invalidIncludes.addAll(m_invalidExcludes);
        m_invalidExcludes.clear();
        for (T c : m_order) {
            if (!m_hideNames.contains(c)) {
                m_inclMdl.addElement(c);
            }
        }
        if (changed) {
            cleanInvalidValues();
            fireFilteringChangedEvent();
        }
    }

    @SuppressWarnings("unchecked")
    private void cleanInvalidValues() {
        if (m_enforceExclusion.isSelected()) {
            for (int i = 0; i < m_inclMdl.getSize(); i++) {
                String name = getNameForT((T)m_inclMdl.getElementAt(i));
                if (isInvalidValue(name)) {
                    m_invalidIncludes.remove(name);
                    m_order.remove(m_inclMdl.getElementAt(i));
                    m_inclMdl.remove(i--);
                }
            }
        } else {
            for (int i = 0; i < m_exclMdl.getSize(); i++) {
                String name = getNameForT((T)m_exclMdl.getElementAt(i));
                if (isInvalidValue(name)) {
                    m_invalidExcludes.remove(name);
                    m_order.remove(m_exclMdl.getElementAt(i));
                    m_exclMdl.remove(i--);
                }
            }
        }
        if (m_inclMdl.isEmpty()) {
            m_inclList.setToolTipText(null);
        }
        if (m_exclMdl.isEmpty()) {
            m_exclList.setToolTipText(null);
        }
    }

    /**
     * Initializes the panel with the mode selection panel and the panel holding the currently active filter.
     */
    private void initPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        // Setup the mode panel, containing the options by column name and by column type
        m_typeGroup = new ButtonGroup();
        m_typePanel = new JPanel();
        m_typePanel.setLayout(new BoxLayout(m_typePanel, BoxLayout.X_AXIS));
        m_nameButton = createButtonToFilterPanel(NameFilterConfiguration.TYPE, NAME);
        // Default has priority 0 which is smaller than FILTER_BY_NAMEPATTERN
        addType(m_nameButton, 0);
        // Setup the filter panel which will contain the filter for the selected mode
        m_filterPanel = new JPanel(new BorderLayout());
        // Activate the selected filter
        m_filterPanel.add(m_nameFilterPanel);
        m_nameButton.setSelected(true);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(m_typePanel, gbc);
        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        panel.add(m_filterPanel, gbc);
        this.add(new JScrollPane(panel));
        updateTypePanel();
        updateFilterPanel();
    }

    private void updateTypePanel() {
        m_typePanel.setVisible(m_typeGroup.getButtonCount() > 1);
        m_typePanel.revalidate();
        m_typePanel.repaint();
    }

    /**
     * Changes the content of the filter panel to the currently active filter.
     */
    private void updateFilterPanel() {
        m_filterPanel.removeAll();
        if (NameFilterConfiguration.TYPE.equals(m_currentType)) {
            m_filterPanel.add(m_nameFilterPanel);
        } else if (PatternFilterConfiguration.TYPE.equals(m_currentType)) {
            m_filterPanel.add(m_patternPanel);
        } else {
            m_filterPanel.add(getFilterPanel(m_currentType));
        }
        // Revalidate and repaint are needed to update the view
        m_filterPanel.revalidate();
        m_filterPanel.repaint();
    }

    /**
     * Enables or disables the pattern filter.
     *
     * @param enabled If the pattern filter should be enabled
     * @since 2.9
     */
    private void setPatternFilterEnabled(final boolean enabled) {
        boolean wasEnabled = Collections.list(m_typeGroup.getElements()).contains(m_patternButton);
        if (wasEnabled != enabled) {
            if (enabled) {
                addType(m_patternButton, NameFilterConfiguration.FILTER_BY_NAMEPATTERN);
            } else {
                removeType(m_patternButton);
            }
        }
    }

    /**
     * @param exclude title for the left box
     * @param include title for the right box
     * @since 3.4
     */
    public void setPatternFilterBorderTitles(final String exclude, final String include) {
        m_patternPanel.setBorderTitles(exclude, include);
    }

    /**
     * sets the text of the "Include Missing Value"-checkbox
     * @param newText
     * @since 3.4
     */
    public void setAdditionalCheckboxText(final String newText){
        if (m_additionalCheckbox != null) {
            m_additionalCheckbox.setText(newText);
            m_additionalCheckbox.setToolTipText(newText);
        }
    }

    /**
     * sets the text of the "Include Missing Value"-checkbox
     * @param newText
     * @since 3.4
     */
    public void setAdditionalPatternCheckboxText(final String newText){
        m_patternPanel.setAdditionalCheckboxText(newText);
    }

}
