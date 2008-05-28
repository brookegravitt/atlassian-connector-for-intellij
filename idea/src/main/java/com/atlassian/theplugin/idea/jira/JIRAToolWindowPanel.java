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

import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.bamboo.HtmlBambooStatusListener;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.configuration.JiraFilterEntryBean;
import com.atlassian.theplugin.configuration.JiraFiltersBean;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.configuration.ProjectToolWindowTableConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.bamboo.ToolWindowBambooContent;
import com.atlassian.theplugin.idea.jira.table.JIRATableColumnProviderImpl;
import com.atlassian.theplugin.idea.jira.table.columns.*;
import com.atlassian.theplugin.idea.ui.AtlassianTableView;
import com.atlassian.theplugin.idea.ui.CollapsibleTable;
import com.atlassian.theplugin.idea.ui.AbstractTableToolWindowPanel;
import com.atlassian.theplugin.idea.ui.TableColumnProvider;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.remoteapi.MissingPasswordHandlerJIRA;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.UIUtil;
import thirdparty.javaworld.ClasspathHTMLEditorKit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;

public class JIRAToolWindowPanel extends AbstractTableToolWindowPanel {
    private static final int PAGE_SIZE = 50;

    private transient ActionToolbar filterToolbar;
    private transient ActionToolbar filterEditToolbar;
    private JIRAIssueFilterPanel jiraIssueFilterPanel;

    private TableColumnProvider columnProvider;

    private transient JiraFiltersBean filters;
    private transient JIRAQueryFragment savedQuery;
    private final List<JIRAQueryFragment> advancedQuery;

    private final transient JIRAServerFacade jiraServerFacade;
    private final transient PluginConfigurationBean pluginConfiguration;

    private int maxIndex = PAGE_SIZE;
    private int startIndex = 0;
    private boolean nextPageAvailable = false;
    private boolean prevPageAvailable = false;
    private String sortColumn = "issuekey";
    private String sortOrder = "ASC";

    private static JIRAToolWindowPanel instance;

    private transient JIRAIssue selectedIssue = null;

    protected void handlePopupClick(Object selectedObject) {
        selectedIssue = ((JiraIssueAdapter) selectedObject).getIssue();
    }

    protected void handleDoubleClick(Object selectedObject) {
        viewIssue();        
    }

    protected String getInitialMessage() {
        return "Select a JIRA server to retrieve your issues.";
    }

    protected String getToolbarActionGroup() {
        return "ThePlugin.JIRA.ServerToolBar";
    }

    protected String getPopupActionGroup() {
        return "ThePlugin.JIRA.IssuePopupMenu";
    }

    protected TableColumnProvider getTableColumnProvider() {
        if (columnProvider == null) {
            columnProvider = new JIRATableColumnProviderImpl();
        }
        return columnProvider;
    }

    protected ProjectToolWindowTableConfiguration getTableConfiguration() {
        return projectConfiguration.getJiraConfiguration().getTableConfiguration();
    }

    public static JIRAToolWindowPanel getInstance(ProjectConfigurationBean projectConfigurationBean) {
        if (instance == null) {
            instance = new JIRAToolWindowPanel(IdeaHelper.getPluginConfiguration(), projectConfigurationBean);
        }
        return instance;
    }

    public JIRAToolWindowPanel(PluginConfigurationBean pluginConfiguration,
                               ProjectConfigurationBean projectConfigurationBean) {
        super(projectConfigurationBean);
        this.pluginConfiguration = pluginConfiguration;
        this.jiraServerFacade = JIRAServerFacadeImpl.getInstance();
        this.advancedQuery = new ArrayList<JIRAQueryFragment>();

        createFilterToolBar();
        createFilterEditToolBar();

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
    }

    public void cancelAdvancedFilter() {
        filters.setManualFilter(serializeQuery());
        filters.setSavedFilterUsed(false);
        projectConfiguration.
                getJiraConfiguration().setFiltersBean(IdeaHelper.getCurrentJIRAServer().getServer().getUid(), filters);
        hideJIRAIssuesFilter();
        filterToolbarSetVisible(true);
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
        filterEditToolbarSetVisible(false);
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


    public void viewIssue() {
        JIRAIssue issue = ((JiraIssueAdapter) table.getSelectedObject()).getIssue();
        BrowserUtil.launchBrowser(issue.getIssueUrl());
    }

    public void editIssue() {
        JIRAIssue issue = ((JiraIssueAdapter) table.getSelectedObject()).getIssue();
        BrowserUtil.launchBrowser(issue.getServerUrl() + "/secure/EditIssue!default.jspa?key=" + issue.getKey());
    }

    public void showIssueActions() {
        final JIRAIssue issue = ((JiraIssueAdapter) table.getSelectedObject()).getIssue();
        FutureTask task = new FutureTask(new Runnable() {
            public void run() {
                setStatusMessage("Getting available issue actions for issue " + issue.getKey() + "...");
                try {
                    List<JIRAAction> actions =
                            jiraServerFacade.getAvailableActions(IdeaHelper.getCurrentJIRAServer().getServer(), issue);
                    setStatusMessage("Retrieved actions for issue " + issue.getKey());
					showActionsPopup(issue, actions);
				} catch (JIRAException e) {
                    setStatusMessage("Unable to retrieve available issue actions: " + e.getMessage(), true);
                }
            }
        }, null);
        new Thread(task, "atlassian-idea-plugin show issue actions").start();

    }

	final class ActionsPopupListener implements Runnable {
		private JIRAIssue issue;
		private JList list;

		public ActionsPopupListener(JIRAIssue issue, JList list) {
			this.issue = issue;
			this.list = list;
		}

		public void run() {
			JIRAAction action = (JIRAAction) list.getSelectedValue();
			BrowserUtil.launchBrowser(issue.getServerUrl()
				+ "/secure/WorkflowUIDispatcher.jspa?id="
				+ issue.getId()
				+ "&"
				+ action.getQueryStringFragment());
		}
	}

	private void showActionsPopup(JIRAIssue issue, List<JIRAAction> actions) {
		JList list = new JList();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		DefaultListModel lm = new DefaultListModel();
		for (JIRAAction a : actions) {
			lm.addElement(a);
		}
		list.setModel(lm);
		PopupChooserBuilder builder = JBPopupFactory.getInstance().createListPopupBuilder(list);
	    builder.setTitle("Actions available for " + issue.getKey())
			.setRequestFocus(true)
			.setResizable(false)
			.setMovable(true)
			.setItemChoosenCallback(new ActionsPopupListener(issue, list))
			.createPopup()
			.showInCenterOf(this);
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
        listTableModel.setItems(new ArrayList<JiraIssueAdapter>());
        listTableModel.fireTableDataChanged();
        table.setEnabled(false);
        editorPane.setText(wrapBody("No issues for selected criteria."));
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

        checkPrevPageAvaialble();
        checkNextPageAvailable(issues);

        editorPane.setText(wrapBody("Loaded <b>" + issues.size() + "</b> issues starting at <b>" + (startIndex + 1) + "</b>."));
    }


    public void selectServer(Server server) {
        if (server != null) {
            projectConfiguration.getJiraConfiguration().setSelectedServerId(server.getUid());
            hideJIRAIssuesFilter();
            final JIRAServer jiraServer = new JIRAServer(server, jiraServerFacade);
            IdeaHelper.setCurrentJIRAServer(jiraServer);
            new Thread(new SelectServerTask(jiraServer, this), "atlassian-idea-plugin jira tab select server").start();
        }
    }

    private final class SelectServerTask implements Runnable {
        private JIRAServer jiraServer;
        private JIRAToolWindowPanel jiraPanel;

        public SelectServerTask(JIRAServer jiraServer, JIRAToolWindowPanel jiraToolWindowPanel) {
            this.jiraServer = jiraServer;
            this.jiraPanel = jiraToolWindowPanel;
        }

        public void run() {
            progressAnimation.startProgressAnimation();
            filterToolbarSetVisible(false);
            startIndex = 0;
            clearIssues();

            if (jiraServer.checkServer() == false) {
                setStatusMessage("Unable to connect to server. " + jiraServer.getErrorMessage(), true);
                progressAnimation.stopProgressAnimation();
                EventQueue.invokeLater(new MissingPasswordHandlerJIRA(jiraServerFacade, jiraServer.getServer(), jiraPanel));
                return;
            }
            setStatusMessage("Retrieving saved filters...");
            jiraServer.getSavedFilters();

            setStatusMessage("Retrieving projects...");
            jiraServer.getProjects();

            setStatusMessage("Retrieving issue types...");
            jiraServer.getIssueTypes();

            setStatusMessage("Retrieving statuses...");
            jiraServer.getStatuses();

            setStatusMessage("Retrieving resolutions...");
            jiraServer.getResolutions();

            setStatusMessage("Retrieving priorities...");
            jiraServer.getPriorieties();

            if (jiraServer.equals(IdeaHelper.getCurrentJIRAServer())) {
                filters = projectConfiguration.getJiraConfiguration()
                        .getJiraFilters(IdeaHelper.getCurrentJIRAServer().getServer().getUid());
                if (filters == null) {
                    filters = new JiraFiltersBean();
                }
                restoreQuery(filters.getManualFilter(), filters.getSavedFilter());
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        updateIssues(jiraServer);
                    }
                });
                filterToolbarSetVisible(true);
            }

            progressAnimation.stopProgressAnimation();
        }
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

    private void disablePagesButton() {
        prevPageAvailable = false;
        nextPageAvailable = false;
    }

    private void updateIssues(final JIRAServer jiraServer) {
        table.setEnabled(false);
        table.setForeground(UIUtil.getInactiveTextColor());
        clearIssues();
        disablePagesButton();
        new Thread(new IssueRefreshTask(jiraServer), "atlassian-idea-plugin jira tab update issues").start();
    }

    private final class IssueRefreshTask implements Runnable {
        private JIRAServer jiraServer;

        private IssueRefreshTask(JIRAServer jiraServer) {
            this.jiraServer = jiraServer;
        }

        public void run() {
            progressAnimation.startProgressAnimation();
            JIRAServerFacade serverFacade = jiraServerFacade;
            try {
                List<JIRAQueryFragment> query = new ArrayList<JIRAQueryFragment>();
                final List result;
                checkTableSort();
                if (filters.getSavedFilterUsed()) {
                    if (savedQuery != null) {
                        query.add(savedQuery);
                        setStatusMessage("Retrieving issues from <b>" + jiraServer.getServer().getName() + "</b>...");
                        editorPane.setCaretPosition(0);
                        result = serverFacade.getSavedFilterIssues(jiraServer.getServer(),
                                query, sortColumn, sortOrder, startIndex, maxIndex);
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                setIssues(result);
                            }
                        });
                    }
                } else {
                    for (JIRAQueryFragment jiraQueryFragment : advancedQuery) {
                        if (jiraQueryFragment.getId() != JIRAServer.ANY_ID) {
                            query.add(jiraQueryFragment);
                        }
                    }
                    setStatusMessage("Retrieving issues from <b>" + jiraServer.getServer().getName() + "</b>...");
                    editorPane.setCaretPosition(0);
                    result = serverFacade.getIssues(jiraServer.getServer(),
                            query, sortColumn, sortOrder, startIndex, maxIndex);
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            setIssues(result);
                        }
                    });
                }
            } catch (JIRAException e) {
                setStatusMessage("Error contacting server <b>" + jiraServer.getServer().getName() + "</b>", true);
            } finally {
                progressAnimation.stopProgressAnimation();
            }
        }
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
            if (IssuePriorityColumn.COLUMN_NAME.equals(columnName)) {
                sortOrder = "DESC";
            } else {
                sortOrder = "ASC";
            }
        } else {
            if (IssuePriorityColumn.COLUMN_NAME.equals(columnName)) {
                sortOrder = "ASC";
            } else {
                sortOrder = "DESC";
            }
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
        Object selectedObject = table.getSelectedObject();
        if (selectedObject != null) {
            return ((JiraIssueAdapter) selectedObject).getIssue();
        }
        return null;
    }

    public void assignIssueToMyself() {
        final JIRAIssue issue = ((JiraIssueAdapter) table.getSelectedObject()).getIssue();
        try {
            assignIssue(issue, IdeaHelper.getCurrentJIRAServer().getServer().getUserName());
        } catch (NullPointerException ex) {
            // whatever, means action was called when no issue was selected. Let's just swallow it
        }
    }

    public void assignIssueToSomebody() {
        final JIRAIssue issue = ((JiraIssueAdapter) table.getSelectedObject()).getIssue();

        final GetUserName getUserName = new GetUserName(issue.getKey());
        getUserName.show();
        if (getUserName.isOK()) {
            try {
                assignIssue(issue, getUserName.getName());
            } catch (NullPointerException ex) {
                // whatever, means action was called when no issue was selected. Let's just swallow it
            }
        }
    }

    public void createChangeListAction(Project project) {
        final JIRAIssue issue = ((JiraIssueAdapter) table.getSelectedObject()).getIssue();
        String changeListName = issue.getKey() + " - " + issue.getSummary();
        final ChangeListManager changeListManager = ChangeListManager.getInstance(project);

        LocalChangeList changeList = changeListManager.findChangeList(changeListName);
        if (changeList == null) {
            ChangesetCreate c = new ChangesetCreate(issue.getKey());
            c.setChangesetName(changeListName);
            c.setChangestComment(changeListName + "\n");
            c.setActive(true);
            c.show();
            if (c.isOK()) {
                changeListName = c.getChangesetName();
                changeList = changeListManager.addChangeList(changeListName, c.getChangesetComment());
                if (c.isActive()) {
                    changeListManager.setDefaultChangeList(changeList);
                }
            }
        } else {
            changeListManager.setDefaultChangeList(changeList);
        }
    }

    public void addCommentToIssue() {
        final JIRAIssue issue = ((JiraIssueAdapter) table.getSelectedObject()).getIssue();
        final IssueComment issueComment = new IssueComment(issue.getKey());
        issueComment.show();
        if (issueComment.isOK()) {
            FutureTask task = new FutureTask(new Runnable() {
                public void run() {
                    setStatusMessage("Commenting issue " + issue.getKey() + "...");
                    try {
                        jiraServerFacade.addComment(IdeaHelper.getCurrentJIRAServer().getServer(),
                                issue, issueComment.getComment());
                        setStatusMessage("Commented issue " + issue.getKey());
                    } catch (JIRAException e) {
                        setStatusMessage("Issue not commented: " + e.getMessage(), true);
                    }
                }
            }, null);
            new Thread(task, "atlassian-idea-plugin comment issue").start();
        }
    }

    public void logWorkForIssue() {
        final JIRAIssue issue = ((JiraIssueAdapter) table.getSelectedObject()).getIssue();
        final WorkLogCreate workLogCreate = new WorkLogCreate(issue.getKey());
        workLogCreate.show();
        if (workLogCreate.isOK()) {
            FutureTask task = new FutureTask(new Runnable() {
                public void run() {
                    setStatusMessage("Logging work for issue " + issue.getKey() + "...");
                    try {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(workLogCreate.getStartDate());
                        jiraServerFacade.logWork(IdeaHelper.getCurrentJIRAServer().getServer(),
                                issue, workLogCreate.getTimeSpentString(),
                                cal, workLogCreate.getComment());
                        setStatusMessage("Logged work for issue " + issue.getKey());
                    } catch (JIRAException e) {
                        setStatusMessage("Work not logged: " + e.getMessage(), true);
                    }
                }
            }, null);
            new Thread(task, "atlassian-idea-plugin work log").start();
        }
    }

    private void assignIssue(final JIRAIssue issue, final String assignee) {
        FutureTask task = new FutureTask(new Runnable() {
            public void run() {
                setStatusMessage("Assigning issue " + issue.getKey() + " to " + assignee + "...");
                try {
                    jiraServerFacade.setAssignee(IdeaHelper.getCurrentJIRAServer().getServer(), issue, assignee);
                    setStatusMessage("Assigned issue " + issue.getKey() + " to " + assignee);
                } catch (JIRAException e) {
                    setStatusMessage("Failed to assign issue: " + e.getMessage(), true);
                }
            }
        }, null);
        new Thread(task, "atlassian-idea-plugin assign issue issue").start();
    }

    public void createIssue() {
        final JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer();

        if (jiraServer != null) {
            final IssueCreate issueCreate = new IssueCreate(jiraServer);
            issueCreate.initData();
            issueCreate.show();
            if (issueCreate.isOK()) {
                FutureTask task = new FutureTask(new Runnable() {
                    public void run() {
                        setStatusMessage("Creating new issue...");
                        JIRAIssue newIssue;
                        String message;
                        boolean isError = false;
                        try {
                            newIssue = jiraServerFacade.createIssue(jiraServer.getServer(), issueCreate.getJIRAIssue());
                            message =
                                    "New issue created: <a href="
                                            + newIssue.getIssueUrl()
                                            + ">"
                                            + newIssue.getKey()
                                            + "</a>";
                        } catch (JIRAException e) {
                            message = "Failed to create new issue: " + e.getMessage();
                            isError = true;
                        }

                        setStatusMessage(message, isError);
                    }
                }, null);
                new Thread(task, "atlassian-idea-plugin create issue").start();
            }
        }
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

    public JIRAIssue getSelectedIssue() {
        return selectedIssue;
    }
}