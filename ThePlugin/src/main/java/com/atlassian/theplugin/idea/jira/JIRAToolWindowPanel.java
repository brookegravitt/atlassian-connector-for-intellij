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

package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.bamboo.HtmlBambooStatusListener;
import com.atlassian.theplugin.configuration.*;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.bamboo.ToolWindowBambooContent;
import com.atlassian.theplugin.idea.jira.table.JIRATableColumnProvider;
import com.atlassian.theplugin.idea.jira.table.columns.*;
import com.atlassian.theplugin.idea.ui.AtlassianTableView;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.api.JIRASavedFilterBean;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.UIUtil;
import thirdparty.javaworld.ClasspathHTMLEditorKit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;

public class JIRAToolWindowPanel extends JPanel {
	private static final int PAGE_SIZE = 50;

	private JEditorPane editorPane;
	private JPanel toolBarPanel;
	private ListTableModel listTableModel;
	private AtlassianTableView table;
	private JScrollPane scrollTable;
	private static final Dimension ED_PANE_MINE_SIZE = new Dimension(200, 200);
	private transient ActionToolbar filterToolbar;
	private transient ActionToolbar filterEditToolbar;
	private JIRAIssueFilterPanel jiraIssueFilterPanel;

	private ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();

	private transient JiraFiltersBean filters;
	private transient JIRAQueryFragment savedQuery;
	private final List<JIRAQueryFragment> advancedQuery;

	private final transient Project project;
	private final transient JIRAServerFacade jiraServerFacade;
	private final transient PluginConfigurationBean pluginConfiguration;
	private final transient ProjectConfigurationBean projectConfiguration;

	private int maxIndex = PAGE_SIZE;
	private int startIndex = 0;
	private boolean nextPageAvailable = false;
	private boolean prevPageAvailable = false;
	private String sortColumn = "issuekey";
	private String sortOrder = "ASC";

	public JIRAToolWindowPanel(PluginConfigurationBean pluginConfiguration,
							   ProjectConfigurationBean projectConfigurationBean,
							   Project project,
							   JIRAServerFacade jiraServerFacade) {

		setLayout(new BorderLayout());

		this.pluginConfiguration = pluginConfiguration;
		this.projectConfiguration = projectConfigurationBean;
		this.project = project;
		this.jiraServerFacade = jiraServerFacade;
		this.advancedQuery = new ArrayList<JIRAQueryFragment>();

		setBackground(UIUtil.getTreeTextBackground());

		toolBarPanel = new JPanel(new BorderLayout());
		ActionManager aManager = ActionManager.getInstance();
		ActionGroup serverToolBar = (ActionGroup) aManager.getAction("ThePlugin.JIRA.ServerToolBar");
		ActionToolbar actionToolbar = aManager.createActionToolbar(
				"atlassian.toolwindow.serverToolBar", serverToolBar, true);


		toolBarPanel.add(actionToolbar.getComponent(), BorderLayout.NORTH);

		add(toolBarPanel, BorderLayout.NORTH);

		editorPane = new ToolWindowBambooContent();
		editorPane.setEditorKit(new ClasspathHTMLEditorKit());
		JScrollPane pane = setupPane(editorPane, wrapBody("Select a JIRA server to retrieve your issues."));
		editorPane.setMinimumSize(ED_PANE_MINE_SIZE);
		add(pane, BorderLayout.SOUTH);

		TableColumnInfo[] columns = JIRATableColumnProvider.makeColumnInfo();
		listTableModel = new ListTableModel(columns);
		listTableModel.setSortable(true);
		table = new AtlassianTableView(listTableModel,
				projectConfigurationBean.getJiraConfiguration().getTableConfiguration());
		table.prepareColumns(columns, JIRATableColumnProvider.makeRendererInfo());

		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) { // on double click, just open the issue
				if (e.getClickCount() == 2) {
					String issue = ((JiraIssueAdapter) table.getSelectedObject()).getIssueUrl();
					if (issue != null) {
						BrowserUtil.launchBrowser(issue);
					}
				}
			}

			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e) { // on right click, show a context menu for this issue
				if (e.isPopupTrigger() && table.isEnabled()) {
					JIRAIssue issue = ((JiraIssueAdapter) table.getSelectedObject()).getIssue();

					if (issue != null) {
						Point p = new Point(e.getX(), e.getY());
						JPopupMenu contextMenu = createContextMenu(issue);
						contextMenu.show(table, p.x, p.y);
					}
				}
			}
		});

		createFilterToolBar();
		createFilterEditToolBar();

		scrollTable = new JScrollPane(table);
		add(scrollTable, BorderLayout.CENTER, 0);

		// initialize animated panel functionality
		progressAnimation.configure(this, scrollTable, BorderLayout.CENTER);
		jiraIssueFilterPanel = new JIRAIssueFilterPanel(progressAnimation);
	}

	public List<JiraFilterEntryBean> serializeQuery() {
		List<JiraFilterEntryBean> query = new ArrayList<JiraFilterEntryBean>();
		for (JIRAQueryFragment jiraQueryFragment : advancedQuery) {
			query.add(new JiraFilterEntryBean(jiraQueryFragment.getMap()));
		}
		return query;
	}

	public void restoreQuery(List<JiraFilterEntryBean> query, JiraFilterEntryBean savedFilter) {
		advancedQuery.clear();
		for (JiraFilterEntryBean filterMapBean : query) {
			Map<String, String> filter = filterMapBean.getFilterEntry();
			String className = filter.get("filterTypeClass");
			try {
				Class c = Class.forName(className);
				advancedQuery.add((JIRAQueryFragment) c.getConstructor(Map.class).newInstance(filter));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}
		}
		if (savedFilter != null) {
			savedQuery = new JIRASavedFilterBean(savedFilter.getFilterEntry());
		} else {
			savedQuery = null;
		}
	}

	public void applyAdvancedFilter() {
		advancedQuery.clear();
		advancedQuery.addAll(jiraIssueFilterPanel.getFilter());
		startIndex = 0;
		updateIssues(IdeaHelper.getCurrentJIRAServer());
		filters.setManualFilter(serializeQuery());
		filters.setSavedFilterUsed(false);
		projectConfiguration.
				getJiraConfiguration().setFiltersBean(IdeaHelper.getCurrentJIRAServer().getServer().getUid(), filters);
		hideJIRAIssuesFilter();
		filterToolbarSetVisible(true);
		filterEditToolbarSetVisible(false);
	}

	public void cancelAdvancedFilter() {
		filters.setManualFilter(serializeQuery());
		filters.setSavedFilterUsed(false);
		projectConfiguration.
				getJiraConfiguration().setFiltersBean(IdeaHelper.getCurrentJIRAServer().getServer().getUid(), filters);
		hideJIRAIssuesFilter();
		filterToolbarSetVisible(true);
		filterEditToolbarSetVisible(false);
	}

	public void clearAdvancedFilter() {
		advancedQuery.clear();
		JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer();
		if (jiraServer != null) {
			jiraIssueFilterPanel.setJiraServer(jiraServer, advancedQuery);
		}
		filterToolbarSetVisible(false);
		filterEditToolbarSetVisible(true);
		setScrollPaneViewport(jiraIssueFilterPanel.$$$getRootComponent$$$());
	}

	public final void hideJIRAIssuesFilter() {
		setScrollPaneViewport(table);
	}

	public final void showJIRAIssueFilter() {
		JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer();
		if (jiraServer != null) {
			jiraIssueFilterPanel.setJiraServer(jiraServer, advancedQuery);
		}
		filterToolbarSetVisible(false);
		filterEditToolbarSetVisible(true);
		setScrollPaneViewport(jiraIssueFilterPanel.$$$getRootComponent$$$());
	}

	private void setScrollPaneViewport(JComponent component) {
		scrollTable.setViewportView(component);
	}

	private JPopupMenu createContextMenu(JIRAIssue issue) {
		JPopupMenu contextMenu = new JPopupMenu();
		contextMenu.add(makeWebUrlMenu("View", issue.getIssueUrl()));
		contextMenu.add(makeWebUrlMenu("Edit",
				issue.getServerUrl() + "/secure/EditIssue!default.jspa?key=" + issue.getKey()));
		contextMenu.addSeparator();
		contextMenu.add(new JMenuItem(new CommentIssueAction()));
		contextMenu.add(makeWebUrlMenu("Log Work", issue.getServerUrl()
				+ "/secure/CreateWorklog!default.jspa?key=" + issue.getKey()));
//		contextMenu.add(makeWebUrlMenu("Commit Changes Against Issue", issue.getServerUrl()
//				+ "/secure/EditIssue!default.jspa?key=" + issue.getKey()));
		contextMenu.add(new JMenuItem(new CreateChangeListAction(issue, project)));

		return contextMenu;
	}

	private JMenuItem makeWebUrlMenu(String menuName, final String url) {
		JMenuItem viewInBrowser = new JMenuItem();
		viewInBrowser.setText(menuName);
		viewInBrowser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BrowserUtil.launchBrowser(url);
			}
		});
		return viewInBrowser;
	}

	private JScrollPane setupPane(JEditorPane pane, String initialText) {
		pane.setText(initialText);
		JScrollPane scrollPane = new JScrollPane(pane,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setWheelScrollingEnabled(true);
		return scrollPane;
	}

	public void refreshIssuesPage() {
		if (IdeaHelper.getCurrentJIRAServer() != null) {
			updateIssues(IdeaHelper.getCurrentJIRAServer());
			serializeQuery();
		}
	}

	public void refreshIssues() {
		startIndex = 0;
		refreshIssuesPage();
	}

	public void clearIssues() {
		startIndex = 0;
		listTableModel.setItems(new ArrayList<JiraIssueAdapter>());
		listTableModel.fireTableDataChanged();
		table.setEnabled(false);
		editorPane.setText(wrapBody("No issues for server."));
	}

	public void setIssues(List<JIRAIssue> issues) {
		List<JiraIssueAdapter> adapters = new ArrayList<JiraIssueAdapter>();
		for (JIRAIssue issue : issues) {
			adapters.add(new JiraIssueAdapter(
					issue,
					pluginConfiguration.getJIRAConfigurationData().isDisplayIconDescription()));
		}
		listTableModel.setItems(adapters);
		listTableModel.fireTableDataChanged();
		table.setEnabled(true);
		table.setForeground(UIUtil.getActiveTextColor());
		checkNextPageAvailable(issues);
		editorPane.setText(wrapBody("Loaded <b>" + issues.size() + "</b> issues starting at <b>" + (startIndex + 1) + "</b>."));
	}

	private String wrapBody(String s) {
		return "<html>" + HtmlBambooStatusListener.BODY_WITH_STYLE + s + "</body></html>";

	}

	public void selectServer(Server server) {
		if (server != null) {
			projectConfiguration.getJiraConfiguration().setSelectedServerId(server.getUid());

			final JIRAServer jiraServer = new JIRAServer(server, jiraServerFacade);
			IdeaHelper.setCurrentJIRAServer(jiraServer);

			new Thread(new SelectServerTask(jiraServer), "atlassian-idea-plugin jira tab select server").start();
		}
	}

	public ProgressAnimationProvider getProgressAnimation() {
		return progressAnimation;
	}

	private class SelectServerTask implements Runnable {
		private JIRAServer jiraServer;

		public SelectServerTask(JIRAServer jiraServer) {
			this.jiraServer = jiraServer;
		}

		public void run() {
			progressAnimation.startProgressAnimation();
			filterToolbarSetVisible(false);
			clearIssues();
			setStatusMessage("Retrieving statuses...");
			List statuses = jiraServer.getStatuses(); // ensure statuses are cached
			if (!jiraServer.isValidServer()) {
				setStatusMessage("Unable to connect to server." + jiraServer.getErrorMessage());
				return;
			}
			String msg = "Found <b>" + statuses.size() + "</b> statuses.<br>";
			setStatusMessage(msg + "Retrieving issue types...");
			List types = jiraServer.getIssueTypes(); // ensure types are cached
			if (!jiraServer.isValidServer()) {
				setStatusMessage("Unable to connect to server." + jiraServer.getErrorMessage());
				return;
			}
			msg += "Found <b>" + types.size() + "</b> issue types.<br>";
			setStatusMessage(msg + "Retrieving projects...");
			jiraServer.getProjects(); // ensure projects are cached
			if (!jiraServer.isValidServer()) {
				setStatusMessage("Unable to connect to server." + jiraServer.getErrorMessage());
				return;
			}
			setStatusMessage(msg + "Retrieving saved filters...");
			jiraServer.getSavedFilters();
			if (!jiraServer.isValidServer()) {
				setStatusMessage("Unable to connect to server." + jiraServer.getErrorMessage());
				return;
			}

			if (jiraServer.equals(IdeaHelper.getCurrentJIRAServer())) {
				filters = projectConfiguration.getJiraConfiguration()
						.getJiraFilters(IdeaHelper.getCurrentJIRAServer().getServer().getUid());
				if (filters == null) {
					filters = new JiraFiltersBean();
				}
				restoreQuery(filters.getManualFilter(), filters.getSavedFilter());
				updateIssues(jiraServer);
				filterToolbarSetVisible(true);
			}

			progressAnimation.stopProgressAnimation();
		}
	}


	private void setStatusMessage(String msg) {
		editorPane.setText(wrapBody("<table width=\"100%\"><tr><td colspan=\"2\">" + msg + "</td></tr></table>"));
	}

	private void createFilterToolBar() {
		ActionManager actionManager = ActionManager.getInstance();
		ActionGroup filterToolBar = (ActionGroup) actionManager.getAction("ThePlugin.JIRA.FilterToolBar");
		filterToolbar = actionManager.createActionToolbar("atlassian.toolwindow.filterToolBar",
				filterToolBar, true);
		toolBarPanel.add(filterToolbar.getComponent(), BorderLayout.CENTER);
		filterToolbarSetVisible(false);
	}

	private void createFilterEditToolBar() {
		ActionManager actionManager = ActionManager.getInstance();
		ActionGroup filterEditToolBar = (ActionGroup) actionManager.getAction("ThePlugin.JIRA.FilterEditToolBar");
		filterEditToolbar = actionManager.createActionToolbar("atlassian.toolwindow.filterEditToolBar",
				filterEditToolBar, true);
		toolBarPanel.add(filterEditToolbar.getComponent(), BorderLayout.SOUTH);
		filterEditToolbarSetVisible(false);
	}

	private void filterToolbarSetVisible(boolean visible) {
		filterToolbar.getComponent().setVisible(visible);
	}

	private void filterEditToolbarSetVisible(boolean visible) {
		filterEditToolbar.getComponent().setVisible(visible);
	}

	public void prevPage() {
		startIndex -= PAGE_SIZE;
		checkPrevPageAvaialble();
		updateIssues(IdeaHelper.getCurrentJIRAServer());
	}

	public void nextPage() {
		startIndex += PAGE_SIZE;
		checkPrevPageAvaialble();
		updateIssues(IdeaHelper.getCurrentJIRAServer());
	}

	private void checkPrevPageAvaialble() {
		if (startIndex < 0) {
			startIndex = 0;
		}
		if (startIndex == 0) {
			prevPageAvailable = false;
		} else {
			prevPageAvailable = true;
		}
	}

	private void checkNextPageAvailable(List result) {
		if (result.size() < PAGE_SIZE) {
			nextPageAvailable = false;
		} else {
			nextPageAvailable = true;
		}
	}	

	private void updateIssues(final JIRAServer jiraServer) {
		table.setEnabled(false);
		table.setForeground(UIUtil.getInactiveTextColor());

		final Server server = jiraServer.getServer();
		editorPane.setText(wrapBody("<table width=\"100%\"><tr><td colspan=\"2\">Retrieving your issues from <b>"
				+ server.getName() + "</b>...</td></tr></table>"));
		editorPane.setCaretPosition(0);

		FutureTask task = new FutureTask(new Runnable() {
			public void run() {
				progressAnimation.startProgressAnimation();
				JIRAServerFacade serverFacade = jiraServerFacade;
				try {
					List<JIRAQueryFragment> query = new ArrayList<JIRAQueryFragment>();
					List result;
					checkTableSort();
					if (filters.getSavedFilterUsed()) {
						query.add(savedQuery);
						result = serverFacade.getSavedFilterIssues(jiraServer.getServer(),
								query, sortColumn, sortOrder, startIndex, maxIndex);
					} else {
						for (JIRAQueryFragment jiraQueryFragment : advancedQuery) {
							if (jiraQueryFragment.getId() != JIRAServer.ANY_ID) {
								query.add(jiraQueryFragment);
							}
						}
						result = serverFacade.getIssues(jiraServer.getServer(),
								query, sortColumn,	sortOrder, startIndex, maxIndex);
					}
					setIssues(result);
				} catch (JIRAException e) {
					editorPane.setText(wrapBody("<table width=\"100%\"><tr><td colspan=\"2\">Error contacting server <b>"
							+ server.getName() + "</b>?</td></tr></table>"));
				} finally {
					progressAnimation.stopProgressAnimation();
				}
			}
		}, null);

		new Thread(task, "atlassian-idea-plugin jira tab update issues").start();
	}

	private void checkTableSort() {
		String columnName = table.getTableViewModel().getColumnName(table.getTableViewModel().getSortedColumnIndex());
		if (IssueTypeColumn.COLUMN_NAME.equals(columnName)) {
			sortColumn = "issuetype";
		} else {
			if (IssueStatusColumn.COLUMN_NAME.equals(columnName)) {
				sortColumn = "status";
			} else {
				if (IssuePriorityColumn.COLUMN_NAME.equals(columnName)) {
					sortColumn = "priority";
				} else {
					if (IssueKeyColumn.COLUMN_NAME.equals(columnName)) {
						sortColumn = "issuekey";
					} else {
						if (IssueSummaryColumn.COLUMN_NAME.equals(columnName)) {
							sortColumn = "description";
						}
					}
				}
			}			
		}
		if (table.getTableViewModel().getSortingType() == 1) {
			sortOrder = "ASC";
		} else {
			sortOrder = "DESC";
		}
	}

	public void addQueryFragment(JIRAQueryFragment fragment) {
		savedQuery = fragment;
		if (fragment != null) {
			filters.setSavedFilter(new JiraFilterEntryBean(fragment.getMap()));
			filters.setSavedFilterUsed(true);
		} else {
			filters.setSavedFilterUsed(false);
		}
		startIndex = 0;
		projectConfiguration.getJiraConfiguration().
				setFiltersBean(IdeaHelper.getCurrentJIRAServer().getServer().getUid(), filters);
	}


	public List<JiraIssueAdapter> getIssues() {
		return (List<JiraIssueAdapter>) listTableModel.getItems();
	}

	public JIRAIssue getCurrentIssue() {
		return ((JiraIssueAdapter) table.getSelectedObject()).getIssue();
	}

	public class CommentIssueAction extends AbstractAction {
		public CommentIssueAction() {
			putValue(Action.NAME, "Add Comment");
		}

		public void actionPerformed(ActionEvent e) {
			JIRAIssue issue = ((JiraIssueAdapter) table.getSelectedObject()).getIssue();
			IssueComment issueComment = new IssueComment(
					jiraServerFacade, IdeaHelper.getCurrentJIRAServer(), getIssues());
			issueComment.setIssue(issue);
			issueComment.show();
		}
	}

	public static class CreateChangeListAction extends AbstractAction {
		private final transient Project project;
		private String changeListName;
		private String issueKey;

		public CreateChangeListAction(JIRAIssue issue, Project project) {
			this.project = project;
			issueKey = issue.getKey();
			changeListName = issueKey + " - " + issue.getSummary();

			if (ChangeListManager.getInstance(project).findChangeList(changeListName) == null) {
				putValue(Action.NAME, "Create ChangeList");
			} else {
				putValue(Action.NAME, "Activate ChangeList");
			}

		}

		public void actionPerformed(ActionEvent event) {

			final ChangeListManager changeListManager = ChangeListManager.getInstance(project);

			LocalChangeList changeList = changeListManager.findChangeList(changeListName);
			if (changeList == null) {
				ChangesetCreate c = new ChangesetCreate(issueKey);
				c.setChangesetName(changeListName);
				c.show();
				if (c.isOK()) {
					changeListName = c.getChangesetName();
					changeList = changeListManager.addChangeList(changeListName, changeListName + "\n");
					changeListManager.setDefaultChangeList(changeList);
				}
			}
		}
	}

	public AtlassianTableView getTable() {
		return table;
	}

	public JiraFiltersBean getFilters() {
		return filters;
	}

	public boolean isNextPageAvailable() {
		return nextPageAvailable;
	}

	public boolean isPrevPageAvailable() {
		return prevPageAvailable;
	}
}