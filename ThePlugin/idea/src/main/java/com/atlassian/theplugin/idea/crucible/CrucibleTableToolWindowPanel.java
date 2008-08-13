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


import com.atlassian.theplugin.commons.bamboo.StausIconBambooListener;
import com.atlassian.theplugin.commons.crucible.CrucibleFiltersBean;
import com.atlassian.theplugin.commons.crucible.CrucibleVersion;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilterBean;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.util.Logger;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.idea.CrucibleReviewWindow;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.ThePluginProjectComponent;
import com.atlassian.theplugin.idea.bamboo.ToolWindowBambooContent;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.events.ShowReviewEvent;
import com.atlassian.theplugin.idea.ui.AtlassianTableView;
import com.atlassian.theplugin.idea.ui.CollapsibleTable;
import com.atlassian.theplugin.idea.ui.TableColumnProvider;
import com.atlassian.theplugin.idea.ui.TableItemSelectedListener;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.Key;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.UIUtil;
import thirdparty.javaworld.ClasspathHTMLEditorKit;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class CrucibleTableToolWindowPanel extends JPanel implements CrucibleStatusListener, TableItemSelectedListener {
	public static final String PLACE_PREFIX = CrucibleTableToolWindowPanel.class.getSimpleName();
	private static final Key<CrucibleTableToolWindowPanel> WINDOW_PROJECT_KEY
			= Key.create(CrucibleTableToolWindowPanel.class.getName());
	private Project project;
	private transient ActionToolbar filterEditToolbar;
	private static CrucibleTableToolWindowPanel instance;
	private TableColumnProvider columnProvider;

	private CrucibleCustomFilterPanel crucibleCustomFilterPanel;

	private ProjectConfigurationBean projectCfg;
	private JPanel toolBarPanel;
	private JPanel dataPanelsHolder;
	private ToolWindowBambooContent editorPane;
	private CrucibleReviewActionListener listener = new CrucibleReviewActionListener();

	public CrucibleFiltersBean getFilters() {
		return filters;
	}

	private transient CrucibleFiltersBean filters;

	protected JScrollPane tablePane;
	protected ListTableModel listTableModel;
	protected static final Dimension ED_PANE_MINE_SIZE = new Dimension(200, 200);
	protected ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();

	protected TableColumnProvider tableColumnProvider = new CrucibleTableColumnProviderImpl();

	private ReviewData selectedItem;

	private Map<PredefinedFilter, CollapsibleTable> tables = new HashMap<PredefinedFilter, CollapsibleTable>();
	private Map<String, CollapsibleTable> customTables = new HashMap<String, CollapsibleTable>();

	private CrucibleVersion crucibleVersion = CrucibleVersion.UNKNOWN;

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
			CustomFilterBean filter = crucibleCustomFilterPanel.getFilter();

			filters.getManualFilter().put(filter.getTitle(), filter);
			projectCfg.
					getCrucibleConfiguration().getCrucibleFilters().getManualFilter().put(filter.getTitle(), filter);
			CrucibleStatusChecker checker = null;
			ThePluginProjectComponent projectComponent = IdeaHelper.getCurrentProjectComponent();
			if (projectComponent != null) {
				checker = projectComponent.getCrucibleStatusChecker();
			}
			refreshReviews(checker);
		}
		hideCrucibleCustomFilter();
	}

	public void cancelAdvancedFilter() {
		hideCrucibleCustomFilter();
	}


	public void clearAdvancedFilter() {
	}

	public static CrucibleTableToolWindowPanel getInstance(com.intellij.openapi.project.Project project,
			ProjectConfigurationBean projectConfigurationBean) {

		CrucibleTableToolWindowPanel window = project.getUserData(WINDOW_PROJECT_KEY);

		if (window == null) {
			window = new CrucibleTableToolWindowPanel(project, projectConfigurationBean);
			project.putUserData(WINDOW_PROJECT_KEY, window);
			CrucibleReviewWindow.getInstance(project);
		}
		return window;
	}

	public CrucibleTableToolWindowPanel(Project project, ProjectConfigurationBean projectConfigurationBean) {
		super(new BorderLayout());
		this.project = project;
		this.projectCfg = projectConfigurationBean;

		setBackground(UIUtil.getTreeTextBackground());

		toolBarPanel = new JPanel(new BorderLayout());

		createFilterToolBar();
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

		createFilterEditToolBar(getPlaceName(), "ThePlugin.Crucible.FilterEditToolBar");
		this.crucibleCustomFilterPanel = new CrucibleCustomFilterPanel();
		filters = projectCfg.getCrucibleConfiguration().getCrucibleFilters();
		if (filters.getReadStored() == null) {
			filters.getPredefinedFilters()[PredefinedFilter.ToReview.ordinal()] = true;
			filters.getPredefinedFilters()[PredefinedFilter.OutForReview.ordinal()] = true;
			filters.setReadStored(true);
		}
	}

	private void createFilterToolBar() {
		ActionManager actionManager = ActionManager.getInstance();
		ActionGroup toolbar = (ActionGroup) actionManager.getAction(getToolbarActionGroup());
		ActionToolbar actionToolbar = actionManager.createActionToolbar(
				getPlaceName(), toolbar, true);
		toolBarPanel.add(actionToolbar.getComponent(), BorderLayout.NORTH);
	}

	protected void createFilterEditToolBar(String place, String toolbarName) {
		ActionManager actionManager = ActionManager.getInstance();
		ActionGroup filterEditToolBar = (ActionGroup) actionManager.getAction(toolbarName);
		filterEditToolbar = actionManager.createActionToolbar(place,
				filterEditToolBar, true);
		toolBarPanel.add(filterEditToolbar.getComponent(), BorderLayout.SOUTH);
		filterEditToolbarSetVisible(false);
	}


	private void switchToCrucible16Filter() {
		for (int i = 0;
			 i < projectCfg.getCrucibleConfiguration().getCrucibleFilters().getPredefinedFilters().length; ++i) {

			showPredefinedFilter(
					PredefinedFilter.values()[i],
					projectCfg.getCrucibleConfiguration().getCrucibleFilters().getPredefinedFilters()[i],
					null);
		}

		for (String s : projectCfg.getCrucibleConfiguration().getCrucibleFilters().getManualFilter().keySet()) {
			CustomFilterBean filter = projectCfg.getCrucibleConfiguration()
					.getCrucibleFilters().getManualFilter().get(s);

			if (filter.isEnabled()) {
				this.showCustomFilter(true, null);
				break;
			}
		}
	}


	private String getPlaceName() {
		return PLACE_PREFIX + this.project.getName();
	}

	/**
	 * Method adds or removes CollapsibleTable for given filter type
	 *
	 * @param filter  predefined filter type
	 * @param visible panel added when true, removed when false
	 */
	public void showPredefinedFilter(PredefinedFilter filter, boolean visible, CrucibleStatusChecker checker) {
		if (visible) {
			CollapsibleTable table = new CollapsibleTable(
					tableColumnProvider,
					projectCfg.getCrucibleConfiguration().getTableConfiguration(),
					filter.getFilterName(),
					null,
					null,
					getPlaceName(),
					getPopupActionGroup());
			table.addItemSelectedListener(this);
			TableView.restore(projectCfg.getCrucibleConfiguration().getTableConfiguration(),
					table.getTable());

			dataPanelsHolder.add(table);
			tables.put(filter, table);

			refreshReviews(checker);
		} else {
			if (tables.containsKey(filter)) {
				dataPanelsHolder.remove(tables.get(filter));
				tables.remove(filter);
			}
		}
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
		return "<html>" + StausIconBambooListener.BODY_WITH_STYLE + s + "</body></html>";

	}

	protected void setStatusMessage(String msg) {
		setStatusMessage(msg, false);
	}

	protected void setStatusMessage(String msg, boolean isError) {
		editorPane.setBackground(isError ? Color.RED : Color.WHITE);
		editorPane.setText(wrapBody("<table width=\"100%\"><tr><td colspan=\"2\">" + msg + "</td></tr></table>"));
	}

	public ProgressAnimationProvider getProgressAnimation() {
		return progressAnimation;
	}

	public CrucibleVersion getCrucibleVersion() {
		return crucibleVersion;
	}

	public void updateReviews(Map<PredefinedFilter, ReviewNotificationBean> reviews, Map<String,
			ReviewNotificationBean> customFilterReviews) {

		Set<ReviewData> uniqueReviews = new HashSet<ReviewData>();

		this.crucibleVersion = CrucibleVersion.CRUCIBLE_16;
		if (tables.isEmpty()) {
			switchToCrucible16Filter();
		}
		for (PredefinedFilter predefinedFilter : reviews.keySet()) {
			if (reviews.get(predefinedFilter).getException() == null) {
				List<ReviewData> reviewList = reviews.get(predefinedFilter).getReviews();
				uniqueReviews.addAll(reviewList);
				if (reviewList != null) {
					CollapsibleTable table = tables.get(predefinedFilter);
					if (table != null) {
						table.getListTableModel().setItems(reviewList);
						table.getListTableModel().fireTableDataChanged();
						table.getTable().revalidate();
						table.setEnabled(true);
						table.setForeground(UIUtil.getActiveTextColor());
						table.setTitle(predefinedFilter.getFilterName() + " (" + reviewList.size() + ")");
					}
				}
			}
		}

		for (String filterName : customFilterReviews.keySet()) {
			if (customFilterReviews.get(filterName).getException() == null) {
				List<ReviewData> reviewList = customFilterReviews.get(filterName).getReviews();
				uniqueReviews.addAll(reviewList);
				if (reviewList != null) {
					CollapsibleTable table = customTables.get(filterName);
					if (table != null) {
						table.getListTableModel().setItems(reviewList);
						table.getListTableModel().fireTableDataChanged();
						table.getTable().revalidate();
						table.setEnabled(true);
						table.setForeground(UIUtil.getActiveTextColor());
						table.setTitle(filterName + " (" + reviewList.size() + ")");
					}
				}
			}
		}


		StringBuffer sb = new StringBuffer();
		sb.append("Loaded <b>");
		sb.append(uniqueReviews.size());
		sb.append(" code reviews</b> for defined filters.");
		setStatusMessage(sb.toString());

		uniqueReviews.clear();
	}

	public void itemSelected(AtlassianTableView table, int noClicks) {
		if (table.getSelectedObject() != null && table.getSelectedObject() instanceof ReviewData) {
			selectedItem = (ReviewData) table.getSelectedObject();

			for (CollapsibleTable collapsibleTable : tables.values()) {
				if (!collapsibleTable.getTable().equals(table)) {
					collapsibleTable.clearSelection();
				}
			}
			for (CollapsibleTable collapsibleTable : customTables.values()) {
				if (!collapsibleTable.getTable().equals(table)) {
					collapsibleTable.clearSelection();
				}
			}

			if (noClicks == 2) {
				CrucibleReviewWindow.getInstance(project);
				IdeaHelper.getReviewActionEventBroker(project).trigger(new ShowReviewEvent(
						listener, selectedItem));
			}
		}
	}

	public void resetState() {
	}

	public final void hideCrucibleCustomFilter() {
		setScrollPaneViewport(dataPanelsHolder);
		filterEditToolbarSetVisible(false);
	}

	public void collapseAllPanels() {
		for (CollapsibleTable collapsibleTable : tables.values()) {
			collapsibleTable.collapse();
		}
		for (CollapsibleTable collapsibleTable : customTables.values()) {
			collapsibleTable.collapse();
		}
	}

	public void expandAllPanels() {
		for (CollapsibleTable collapsibleTable : tables.values()) {
			collapsibleTable.expand();
		}
		for (CollapsibleTable collapsibleTable : customTables.values()) {
			collapsibleTable.expand();
		}
	}

	protected void filterEditToolbarSetVisible(boolean visible) {
		filterEditToolbar.getComponent().setVisible(visible);
	}

	protected void setScrollPaneViewport(JComponent component) {
		tablePane.setViewportView(component);
	}

	public ReviewData getSelectedReview() {
		if (selectedItem != null) {
			return this.selectedItem;
		}
		return null;
	}

	public PermId getSelectedReviewId() {
		if (selectedItem != null) {
			return this.selectedItem.getPermId();
		}
		return null;
	}

	private void showCrucibleCustomFilterPanel() {
		filterEditToolbarSetVisible(true);
		setScrollPaneViewport(crucibleCustomFilterPanel.$$$getRootComponent$$$());
	}

	public void showCrucibleCustomFilter() {
		if (!projectCfg.getCrucibleConfiguration().getCrucibleFilters().getManualFilter().isEmpty()) {
			for (String filterName : projectCfg.getCrucibleConfiguration()
					.getCrucibleFilters().getManualFilter().keySet()) {

				crucibleCustomFilterPanel.setFilter(
						projectCfg.getCrucibleConfiguration().getCrucibleFilters().getManualFilter().get(filterName));
				break;
			}
			showCrucibleCustomFilterPanel();
		} else {
			addCustomFilter();
		}
	}

	public void addCustomFilter() {
		String newName = FilterNameUtil.suggestNewName(
				projectCfg.getCrucibleConfiguration().getCrucibleFilters().getManualFilter());
		CustomFilterBean newFilter = new CustomFilterBean();
		newFilter.setTitle(newName);
		crucibleCustomFilterPanel.setFilter(newFilter);
		showCrucibleCustomFilterPanel();
	}

	public void removeCustomFilter() {

	}

	public void removeItemSelectedListener(TableItemSelectedListener tableListener) {
		for (CollapsibleTable table : tables.values()) {
			table.removeItemSelectedListener(tableListener);
		}


		for (CollapsibleTable table : customTables.values()) {
			table.removeItemSelectedListener(tableListener);
		}
	}


	public void showCustomFilter(boolean visible, CrucibleStatusChecker checker) {
		if (!projectCfg.getCrucibleConfiguration().getCrucibleFilters().getManualFilter().isEmpty()) {
			for (String filterName : projectCfg.getCrucibleConfiguration().getCrucibleFilters().getManualFilter().keySet()) {
				CustomFilterBean filter
						= projectCfg.getCrucibleConfiguration().getCrucibleFilters().getManualFilter().get(filterName);
				if (visible) {
					if (!customTables.containsKey(filter.getTitle())) {
						CollapsibleTable table = new CollapsibleTable(
								tableColumnProvider,
								projectCfg.getCrucibleConfiguration().getTableConfiguration(),
								filter.getTitle(),
								null,
								null,
								getPlaceName(),
								getPopupActionGroup());
						table.addItemSelectedListener(this);
						TableView.restore(projectCfg.getCrucibleConfiguration().getTableConfiguration(),
								table.getTable());

						dataPanelsHolder.add(table);
						customTables.put(filter.getTitle(), table);

						refreshReviews(checker);
					}
				} else {
					if (customTables.containsKey(filter.getTitle())) {
						dataPanelsHolder.remove(customTables.get(filter.getTitle()));
						customTables.remove(filter.getTitle());
					}
				}
			}
			dataPanelsHolder.validate();
			tablePane.repaint();
		}
	}

	public void refreshReviews(final CrucibleStatusChecker checker) {
		if (checker != null) {
			if (checker.canSchedule()) {
				final ProgressAnimationProvider animator = getProgressAnimation();
				final Logger log = PluginUtil.getLogger();

				new Thread(new Runnable() {
					public void run() {
						Thread t = new Thread(checker.newTimerTask(), "Manual Crucible panel refresh (checker)");
						animator.startProgressAnimation();
						t.start();
						try {
							t.join();
						} catch (InterruptedException e) {
							log.warn(e.toString());
						} finally {
							animator.stopProgressAnimation();
						}
					}
				}, "Manual Crucible panel refresh").start();
			}
		}
	}

	public ProjectConfigurationBean getProjectCfg() {
		return projectCfg;
	}
}