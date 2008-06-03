/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.idea.crucible;


import com.atlassian.theplugin.commons.bamboo.HtmlBambooStatusListener;
import com.atlassian.theplugin.commons.crucible.*;
import com.atlassian.theplugin.commons.crucible.api.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.CustomFilterData;
import com.atlassian.theplugin.commons.crucible.api.rest.CrucibleFiltersBean;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.configuration.JiraFiltersBean;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.bamboo.ToolWindowBambooContent;
import com.atlassian.theplugin.idea.ui.CollapsibleTable;
import com.atlassian.theplugin.idea.ui.TableColumnProvider;
import com.atlassian.theplugin.idea.ui.TableItemSelectedListener;
import com.atlassian.theplugin.jira.JIRAServer;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.UIUtil;
import thirdparty.javaworld.ClasspathHTMLEditorKit;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class CrucibleTableToolWindowPanel extends JPanel implements CrucibleStatusListener, TableItemSelectedListener {
    private transient ActionToolbar filterEditToolbar;
    private static CrucibleTableToolWindowPanel instance;
    private TableColumnProvider columnProvider;

	private CrucibleCustomFilterPanel crucibleCustomFilterPanel;

    private ProjectConfigurationBean projectConfiguration;
    private JPanel toolBarPanel;
    private JPanel dataPanelsHolder;
    private ToolWindowBambooContent editorPane;

	public CrucibleFiltersBean getFilters() {
		return filters;
	}

	private transient CrucibleFiltersBean filters;


    protected JScrollPane tablePane;
    protected ListTableModel listTableModel;
    protected static final Dimension ED_PANE_MINE_SIZE = new Dimension(200, 200);
    protected ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();

    protected TableColumnProvider tableColumnProvider = new CrucibleTableColumnProviderImpl();

    private ReviewDataInfoAdapter selectedItem;

    private Map<PredefinedFilter, CollapsibleTable> tables = new HashMap<PredefinedFilter, CollapsibleTable>();
    private CollapsibleTable crucible15Table = null;

    private static CrucibleServerFacade serverFacade;
    private CrucibleVersion crucibleVersion = CrucibleVersion.UNKNOWN;
    private static final String TO_REVIEW_AS_ACTIVE_REVIEWER = "To review as active reviewer";

    protected String getInitialMessage() {

        return "Waiting for Crucible review info.";
    }

    protected String getToolbarActionGroup() {
        return "ThePlugin.CrucibleToolWindowToolBar";
    }

    protected String getPopupActionGroup() {
        return "ThePlugin.Crucible.ReviewPopupMenu";
    }

    protected TableColumnProvider getTableColumnProvider() {
        if (columnProvider == null) {
            columnProvider = new CrucibleTableColumnProviderImpl();
        }
        return columnProvider;
    }

	public void applyAdvancedFilter() {
		if (crucibleCustomFilterPanel.getFilter() != null) {
			CustomFilterData filter = crucibleCustomFilterPanel.getFilter();

			filters.safeAddCrucibleFilter(filter);
			projectConfiguration.
                getCrucibleConfiguration().safeAddCrucibleFilter(filter);
		}

		hideCrucibleCustomFilter();
	}

	public void cancelAdvancedFilter() {
		hideCrucibleCustomFilter();

	}


	public void clearAdvancedFilter() {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public static CrucibleTableToolWindowPanel getInstance(ProjectConfigurationBean projectConfigurationBean) {
        if (instance == null) {
            instance = new CrucibleTableToolWindowPanel(projectConfigurationBean);
            serverFacade = CrucibleServerFacadeImpl.getInstance();
        }
        return instance;
    }

    public CrucibleTableToolWindowPanel(ProjectConfigurationBean projectConfigurationBean) {
        super(new BorderLayout());

        this.projectConfiguration = projectConfigurationBean;

        setBackground(UIUtil.getTreeTextBackground());

        toolBarPanel = new JPanel(new BorderLayout());
        ActionManager actionManager = ActionManager.getInstance();
        ActionGroup toolbar = (ActionGroup) actionManager.getAction(getToolbarActionGroup());
        ActionToolbar actionToolbar = actionManager.createActionToolbar(
                "atlassian.toolwindow.serverToolBar", toolbar, true);
        toolBarPanel.add(actionToolbar.getComponent(), BorderLayout.NORTH);
        add(toolBarPanel, BorderLayout.NORTH);

        editorPane = new ToolWindowBambooContent();
        editorPane.setEditorKit(new ClasspathHTMLEditorKit());
        JScrollPane pane = setupPane(editorPane, wrapBody(getInitialMessage()));
        editorPane.setMinimumSize(ED_PANE_MINE_SIZE);
        add(pane, BorderLayout.SOUTH);

        dataPanelsHolder = new JPanel();
        dataPanelsHolder.setLayout(new VerticalFlowLayout());

        tablePane = new JScrollPane(dataPanelsHolder,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tablePane.setWheelScrollingEnabled(true);

        add(tablePane, BorderLayout.CENTER);

        progressAnimation.configure(this, tablePane, BorderLayout.CENTER);

        //crucibleFacade = CrucibleServerFacadeImpl.getInstance();
		createFilterEditToolBar("atlassian.toolwindow.crucibleFilterEditToolBar", "ThePlugin.Crucible.FilterEditToolBar");
		this.crucibleCustomFilterPanel = new CrucibleCustomFilterPanel();
		filters = projectConfiguration.getCrucibleConfiguration().getCrucibleFilters();
	}

    private void switchToCrucible16Filter() {
        for (int i = 0; i < IdeaHelper.getPluginConfiguration().getCrucibleConfigurationData().getFilters().length; ++i) {
            if (crucible15Table != null) {
                dataPanelsHolder.remove(crucible15Table);
                crucible15Table = null;
            }
            showPredefinedFilter(
                    PredefinedFilter.values()[i],
                    IdeaHelper.getPluginConfiguration().getCrucibleConfigurationData().getFilters()[i]);
        }
    }

    /**
     * Method adds or removes CollapsibleTable for given filter type
     *
     * @param filter  predefined filter type
     * @param visible panel added when true, removed when false
     */
    public void showPredefinedFilter(PredefinedFilter filter, boolean visible) {
        if (visible) {
            CollapsibleTable table = new CollapsibleTable(
                    tableColumnProvider,
                    projectConfiguration.getCrucibleConfiguration().getTableConfiguration(),
                    filter.getFilterName(),
                    "atlassian.toolwindow.serverToolBar",
                    "ThePlugin.CrucibleReviewToolBar",
                    "Context menu",
                    getPopupActionGroup());
            table.addItemSelectedListener(this);
            TableView.restore(projectConfiguration.getCrucibleConfiguration().getTableConfiguration(),
                    table.getTable());

            dataPanelsHolder.add(table);
            tables.put(filter, table);
        } else {
            if (tables.containsKey(filter)) {
                dataPanelsHolder.remove(tables.get(filter));
                tables.remove(filter);
            }
        }
        dataPanelsHolder.validate();
        tablePane.repaint();
    }

    public void switchToCrucible15Filter() {
            crucible15Table = new CollapsibleTable(
                    tableColumnProvider,
                    projectConfiguration.getCrucibleConfiguration().getTableConfiguration(),
                    TO_REVIEW_AS_ACTIVE_REVIEWER,
                    "atlassian.toolwindow.serverToolBar",
                    "ThePlugin.CrucibleReviewToolBar",
                    "Context menu",
                    getPopupActionGroup());
            crucible15Table.addItemSelectedListener(this);
            TableView.restore(projectConfiguration.getCrucibleConfiguration().getTableConfiguration(),
                    crucible15Table.getTable());

        for (CollapsibleTable table : tables.values()) {
            dataPanelsHolder.remove(table);
        }
        tables.clear();
        dataPanelsHolder.add(crucible15Table);
        dataPanelsHolder.validate();
        tablePane.repaint();
    }

    protected JScrollPane setupPane(JEditorPane pane, String initialText) {
        pane.setText(initialText);
        JScrollPane scrollPane = new JScrollPane(pane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setWheelScrollingEnabled(true);
        return scrollPane;
    }

    protected String wrapBody(String s) {
        return "<html>" + HtmlBambooStatusListener.BODY_WITH_STYLE + s + "</body></html>";

    }

    protected void setStatusMessage(String msg) {
        setStatusMessage(msg, false);
    }

    protected void setStatusMessage(String msg, boolean isError) {
        editorPane.setBackground(isError ? Color.RED : Color.WHITE);
        editorPane.setText(wrapBody("<table width=\"100%\"><tr><td colspan=\"2\">" + msg + "</td></tr></table>"));
    }

    public void viewReview() {
        if (selectedItem != null) {
            BrowserUtil.launchBrowser(selectedItem.getReviewUrl());
        }
    }

    public ProgressAnimationProvider getProgressAnimation() {
        return progressAnimation;
    }

    public CrucibleVersion getCrucibleVersion() {
        return crucibleVersion;
    }

    /*
    Crucible 1.5
     */
    public void updateReviews(Collection<ReviewDataInfo> reviews) {
        this.crucibleVersion = CrucibleVersion.CRUCIBLE_15;
        if (crucible15Table == null) {
            switchToCrucible15Filter();
        }
        List<ReviewDataInfoAdapter> reviewDataInfoAdapters = new ArrayList<ReviewDataInfoAdapter>();
        for (ReviewDataInfo review : reviews) {
            reviewDataInfoAdapters.add(new ReviewDataInfoAdapter(review));
        }

        crucible15Table.getListTableModel().setItems(reviewDataInfoAdapters);
        crucible15Table.getListTableModel().fireTableDataChanged();
        crucible15Table.getTable().revalidate();
        crucible15Table.getTable().setEnabled(true);
        crucible15Table.getTable().setForeground(UIUtil.getActiveTextColor());
        crucible15Table.setTitle(TO_REVIEW_AS_ACTIVE_REVIEWER + " (" + reviewDataInfoAdapters.size() + ")");
        crucible15Table.expand();

        StringBuffer sb = new StringBuffer();
        sb.append("Loaded <b>");
        sb.append(reviews.size());
        sb.append(" open code reviews</b> for you.");
        setStatusMessage(sb.toString());
    }

    /*
    Crucible 1.6
     */
    public void updateReviews(Map<PredefinedFilter, List<ReviewDataInfo>> reviews) {
        this.crucibleVersion = CrucibleVersion.CRUCIBLE_16;
        if (tables.isEmpty()) {
            switchToCrucible16Filter();
        }
        int reviewCount = 0;
        for (PredefinedFilter predefinedFilter : reviews.keySet()) {
            List<ReviewDataInfo> reviewList = reviews.get(predefinedFilter);
            if (reviewList != null) {
                List<ReviewDataInfoAdapter> reviewDataInfoAdapters = new ArrayList<ReviewDataInfoAdapter>();
                for (ReviewDataInfo review : reviewList) {
                    reviewDataInfoAdapters.add(new ReviewDataInfoAdapter(review));
                }
                CollapsibleTable table = tables.get(predefinedFilter);
                if (table != null) {
                    table.getListTableModel().setItems(reviewDataInfoAdapters);
                    table.getListTableModel().fireTableDataChanged();
                    table.getTable().revalidate();
                    table.setEnabled(true);
                    table.setForeground(UIUtil.getActiveTextColor());
                }
                table.setTitle(predefinedFilter.getFilterName() + " (" + reviewList.size() + ")");
                reviewCount += reviewList.size();
            }
        }
        StringBuffer sb = new StringBuffer();
        sb.append("Loaded <b>");
        sb.append(reviewCount);
        sb.append(" code reviews</b> for defined filters.");
        setStatusMessage(sb.toString());
    }

    public void itemSelected(Object item, int noClicks) {
        selectedItem = (ReviewDataInfoAdapter) item;
        if (noClicks == 2) {
            viewReview();
        }
    }

    public void resetState() {
    }

	public void showCrucibleCustomFilter() {
        JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer();

        filterEditToolbarSetVisible(true);
	 	crucibleCustomFilterPanel.setFilter(filters.getManualFilter().isEmpty() ? null : filters.getManualFilter().get(0));		
		setScrollPaneViewport(crucibleCustomFilterPanel.$$$getRootComponent$$$());
	}

	public final void hideCrucibleCustomFilter() {
        setScrollPaneViewport(dataPanelsHolder);
        filterEditToolbarSetVisible(false);
    }

    public void collapseAllPanels() {
        for (CollapsibleTable collapsibleTable : tables.values()) {
            collapsibleTable.collapse();
        }
    }

    public void expandAllPanels() {
        for (CollapsibleTable collapsibleTable : tables.values()) {
            collapsibleTable.expand();
        }
    }

	protected void filterEditToolbarSetVisible(boolean visible) {
        filterEditToolbar.getComponent().setVisible(visible);
    }

	protected   void createFilterEditToolBar(String place, String toolbarName) {
        ActionManager actionManager = ActionManager.getInstance();
        ActionGroup filterEditToolBar = (ActionGroup) actionManager.getAction(toolbarName);
        filterEditToolbar = actionManager.createActionToolbar(place,
                filterEditToolBar, true);
        toolBarPanel.add(filterEditToolbar.getComponent(), BorderLayout.SOUTH);
        filterEditToolbarSetVisible(false);
    }

	protected void setScrollPaneViewport(JComponent component) {
			tablePane.setViewportView(component);
		}
	

}