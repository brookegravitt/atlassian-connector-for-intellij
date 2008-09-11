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

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.configuration.JiraFilterEntryBean;
import com.atlassian.theplugin.configuration.JiraFiltersBean;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.configuration.ProjectToolWindowTableConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.action.jira.FilterTypeAction;
import com.atlassian.theplugin.idea.action.jira.JIRANextPageAction;
import com.atlassian.theplugin.idea.action.jira.JIRAPreviousPageAcion;
import com.atlassian.theplugin.idea.action.jira.JIRAShowIssuesFilterAction;
import com.atlassian.theplugin.idea.action.jira.RunJIRAActionAction;
import com.atlassian.theplugin.idea.action.jira.SavedFilterComboAction;
import com.atlassian.theplugin.idea.jira.table.JIRATableColumnProviderImpl;
import com.atlassian.theplugin.idea.jira.table.columns.IssueKeyColumn;
import com.atlassian.theplugin.idea.jira.table.columns.IssuePriorityColumn;
import com.atlassian.theplugin.idea.jira.table.columns.IssueStatusColumn;
import com.atlassian.theplugin.idea.jira.table.columns.IssueSummaryColumn;
import com.atlassian.theplugin.idea.jira.table.columns.IssueTypeColumn;
import com.atlassian.theplugin.idea.ui.AbstractTableToolWindowPanel;
import com.atlassian.theplugin.idea.ui.TableColumnProvider;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.api.JIRAAction;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.api.JIRASavedFilterBean;
import com.atlassian.theplugin.remoteapi.MissingPasswordHandlerJIRA;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class JIRAToolWindowPanel extends AbstractTableToolWindowPanel<JiraIssueAdapter> {
    private static final int PAGE_SIZE = 50;
	public static final String JIRA_ATLASSIAN_TOOLWINDOW_SERVER_TOOL_BAR = "atlassian.jira.toolwindow";
	private static final Key<JIRAToolWindowPanel> WINDOW_PROJECT_KEY =  Key.create(JIRAToolWindowPanel.class.getName());
	
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



    private transient JIRAIssue selectedIssue = null;
	private CfgManager cfgManager;

	@Override
	protected void handlePopupClick(Object selectedObject) {
        selectedIssue = ((JiraIssueAdapter) selectedObject).getIssue();
    }

	public static JIRAToolWindowPanel getInstance(Project project, ProjectConfigurationBean projectConfigurationBean,
			final CfgManager cfgManager) {

        JIRAToolWindowPanel window = project.getUserData(WINDOW_PROJECT_KEY);

        if (window == null) {
            window = new JIRAToolWindowPanel(project, IdeaHelper.getPluginConfiguration(),
					projectConfigurationBean, cfgManager);
            project.putUserData(WINDOW_PROJECT_KEY, window);
        }
        return window;
    }

	@Override
	protected void handleDoubleClick(Object selectedObject) {
		AnAction action = ActionManager.getInstance().getAction("ThePlugin.JIRA.OpenIssue");
		action.actionPerformed(new AnActionEvent(null, DataManager.getInstance().getDataContext(this),
				ActionPlaces.UNKNOWN, action.getTemplatePresentation(),
				ActionManager.getInstance(), 0));
    }

    @Override
	protected String getInitialMessage() {
        return "Select a JIRA server to retrieve your issues.";
    }

    @Override
	protected String getToolbarActionGroup() {
        return "ThePlugin.JIRA.ServerToolBar";
    }

    @Override
	protected String getPopupActionGroup() {
        return "ThePlugin.JIRA.IssuePopupMenu";
    }

    @Override
	protected TableColumnProvider getTableColumnProvider() {
        if (columnProvider == null) {
            columnProvider = new JIRATableColumnProviderImpl();
        }
        return columnProvider;
    }

    @Override
	protected ProjectToolWindowTableConfiguration getTableConfiguration() {
        return projectConfiguration.getJiraConfiguration().getTableConfiguration();
    }

    public JIRAToolWindowPanel(Project project, PluginConfigurationBean pluginConfiguration,
			ProjectConfigurationBean projectConfigurationBean, final CfgManager cfgManager) {
        super(project, projectConfigurationBean);
		this.cfgManager = cfgManager;
		this.project = project;
		this.pluginConfiguration = pluginConfiguration;
        this.jiraServerFacade = JIRAServerFacadeImpl.getInstance();
        this.advancedQuery = new ArrayList<JIRAQueryFragment>();

        createFilterToolBar();
        createFilterEditToolBar();

        jiraIssueFilterPanel = new JIRAIssueFilterPanel(project);
    }

    public ArrayList<JiraFilterEntryBean> serializeQuery() {
        ArrayList<JiraFilterEntryBean> query = new ArrayList<JiraFilterEntryBean>();
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
                Class<?> c = Class.forName(className);
                advancedQuery.add((JIRAQueryFragment) c.getConstructor(Map.class).newInstance(filter));
            } catch (Exception e) {
				LoggerImpl.getInstance().error(e);
            }
        }
        if (savedFilter != null) {
            savedQuery = new JIRASavedFilterBean(savedFilter.getFilterEntry());
        } else {
            savedQuery = null;
        }
    }

    @Override
	public void applyAdvancedFilter() {
        advancedQuery.clear();
        advancedQuery.addAll(jiraIssueFilterPanel.getFilter());
        startIndex = 0;
        updateIssues(IdeaHelper.getCurrentJIRAServer(project));
        filters.setManualFilter(serializeQuery());
        filters.setSavedFilterUsed(false);
		JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer(project);

		if (jiraServer != null) {
		projectConfiguration.
                getJiraConfiguration().setFiltersBean(
				jiraServer.getServer().getServerId().toString(), filters);
		}

		hideJIRAIssuesFilter();
        filterToolbarSetVisible(true);
    }

    @Override
	public void cancelAdvancedFilter() {
        filters.setManualFilter(serializeQuery());
        filters.setSavedFilterUsed(false);
		JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer(project);

		if (jiraServer != null) {
			projectConfiguration.getJiraConfiguration()
					.setFiltersBean(jiraServer.getServer().getServerId().toString(), filters);
		}

		hideJIRAIssuesFilter();
        filterToolbarSetVisible(true);
    }

    @Override
	public void clearAdvancedFilter() {
        advancedQuery.clear();
        JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer(project);
        if (jiraServer != null) {
            jiraIssueFilterPanel.setJiraServer(jiraServer, advancedQuery);
        }
        filterToolbarSetVisible(false);
        filterEditToolbarSetVisible(true);
        setScrollPaneViewport(jiraIssueFilterPanel.$$$getRootComponent$$$());
    }

	public String getActionPlace() {
		return JIRA_ATLASSIAN_TOOLWINDOW_SERVER_TOOL_BAR + project.getName();
	}

	public final void hideJIRAIssuesFilter() {
        setScrollPaneViewport(table);
        filterEditToolbarSetVisible(false);
    }

    public final void showJIRAIssueFilter() {
        JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer(project);
        if (jiraServer != null) {
            jiraIssueFilterPanel.setJiraServer(jiraServer, advancedQuery);
        }
        filterToolbarSetVisible(false);
        filterEditToolbarSetVisible(true);
        setScrollPaneViewport(jiraIssueFilterPanel.$$$getRootComponent$$$());
    }


    public void viewIssue() {
		final JiraIssueAdapter issueAdapter = table.getSelectedObject();
		if (issueAdapter == null) {
			return;
		}
		JIRAIssue issue = issueAdapter.getIssue();
        BrowserUtil.launchBrowser(issue.getIssueUrl());
    }

    public void editIssue() {
		final JiraIssueAdapter issueAdapter = table.getSelectedObject();
		if (issueAdapter == null) {
			return;
		}
		JIRAIssue issue = issueAdapter.getIssue();
        BrowserUtil.launchBrowser(issue.getServerUrl() + "/secure/EditIssue!default.jspa?key=" + issue.getKey());
    }

    public void showIssueActions() {
		final JiraIssueAdapter adapter = table.getSelectedObject();
		if (adapter == null) {
			return;
		}
		final JIRAIssue issue = adapter.getIssue();
		List<JIRAAction> actions = adapter.getCachedActions();
		if (actions != null) {
			showActionsPopup(adapter, actions);
		} else {
			Thread thread = new Thread("atlassian-idea-plugin show issue actions") {
                @Override
				public void run() {
                    setStatusMessage("Getting available issue actions for issue " + issue.getKey() + "...");
                    try {
						JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer(project);
						if (jiraServer != null) {
							List<JIRAAction> actions =
									jiraServerFacade.getAvailableActions(jiraServer.getServer(), issue);
							adapter.setCachedActions(actions);
							setStatusMessage("Retrieved actions for issue " + issue.getKey());
							showActionsPopup(adapter, actions);
						}
					} catch (JIRAException e) {
                        setStatusMessage("Unable to retrieve available issue actions: " + e.getMessage(), true);
                    }
                }

            };
            thread.start();
		}
    }

	public void selectLastActiveServer() {

		String uuidString = projectConfiguration.getJiraConfiguration().getSelectedServerId();

		if (uuidString != null) {
				final ServerId serverId = new ServerId(uuidString);
				ServerCfg serverCfg = cfgManager.getServer(CfgUtil.getProjectId(project), serverId);
				if (serverCfg != null && serverCfg instanceof JiraServerCfg && serverCfg.isEnabled()) {
					selectServer((JiraServerCfg) serverCfg);
				}
			}
	}

	final class ActionsPopupListener implements Runnable {
		private JiraIssueAdapter adapter;
		private JList list;

		public ActionsPopupListener(JiraIssueAdapter adapter, JList list) {
			this.adapter = adapter;
			this.list = list;
		}

		public void run() {
			JIRAAction action = (JIRAAction) list.getSelectedValue();
			RunJIRAActionAction ja = new RunJIRAActionAction(JIRAToolWindowPanel.this, jiraServerFacade, adapter, action);
			ja.runIssueActionOrLaunchBrowser(project);
		}
	}

	private void showActionsPopup(JiraIssueAdapter adapter, List<JIRAAction> actions) {
		JList list = new JList();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		DefaultListModel lm = new DefaultListModel();
		for (JIRAAction a : actions) {
			lm.addElement(a);
		}
		list.setModel(lm);
		PopupChooserBuilder builder = JBPopupFactory.getInstance().createListPopupBuilder(list);
	    builder.setTitle("Actions available for " + adapter.getKey())
			.setRequestFocus(true)
			.setResizable(false)
			.setMovable(true)
			.setItemChoosenCallback(new ActionsPopupListener(adapter, list))
			.createPopup()
			.showInCenterOf(this);
	}

	@Override
	protected void addCustomSubmenus(DefaultActionGroup actionGroup, final ActionPopupMenu popup) {
		final DefaultActionGroup submenu = new DefaultActionGroup("Querying for Actions... ", true) {
			@Override
			public void update(AnActionEvent event) {
				super.update(event);

				if (getChildrenCount() > 0) {
					event.getPresentation().setText("Available Workflow Actions");
				}
			}
		};
		actionGroup.add(submenu);

		final JiraIssueAdapter adapter = table.getSelectedObject();
		if (adapter == null) {
			return;
		}
		final JIRAIssue issue = adapter.getIssue();
		List<JIRAAction> actions = adapter.getCachedActions();
		if (actions != null) {
			for (JIRAAction a : actions) {
				submenu.add(new RunJIRAActionAction(this, jiraServerFacade, adapter, a));
			}
		} else {
			new Thread() {
				@Override
				public void run() {
					try {
						JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer(project);

						if (jiraServer != null) {
							final List<JIRAAction> actions =
									jiraServerFacade.getAvailableActions(jiraServer.getServer(), issue);
							adapter.setCachedActions(actions);
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									JPopupMenu pMenu = popup.getComponent();
									if (pMenu.isVisible()) {
										for (JIRAAction a : actions) {
											submenu.add(new RunJIRAActionAction(JIRAToolWindowPanel.this,
													jiraServerFacade, adapter, a));
										}
										// magic that makes the popup update itself. Don't ask - it is some sort of voodoo
										pMenu.setVisible(false);
										pMenu.setVisible(true);
									}
								}
							});
						}
					} catch (JIRAException e) {
						setStatusMessage("Query for issue actions failed: " + e.getMessage(), true);
					} catch (NullPointerException e) {
						// somebody unselected issue in the table, so let's just skip
					}
				}
			}
			.start();
		}
	}

	
	public void refreshIssuesPage() {
		JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer(project);
		if (jiraServer != null) {
            updateIssues(jiraServer);
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


    public void selectServer(JiraServerCfg server) {
        if (server != null) {
            projectConfiguration.getJiraConfiguration().setSelectedServerId(server.getServerId().toString());
            hideJIRAIssuesFilter();
            final JIRAServer jiraServer = new JIRAServer(server, jiraServerFacade);
            IdeaHelper.setCurrentJIRAServer(jiraServer);
            Task.Backgroundable task = new SelectServerTask(jiraServer, this);
			ProgressManager.getInstance().run(task);
		}
    }

    private final class SelectServerTask extends Task.Backgroundable {
        private JIRAServer jiraServer;
        private JIRAToolWindowPanel jiraPanel;

        public SelectServerTask(JIRAServer jiraServer, JIRAToolWindowPanel jiraToolWindowPanel) {
			super(project, "Retrieving Data from JIRA", false);
			this.jiraServer = jiraServer;
            this.jiraPanel = jiraToolWindowPanel;
        }

        public void run(final ProgressIndicator indicator) {
            filterToolbarSetVisible(false);
            startIndex = 0;
            clearIssues();

            if (!jiraServer.checkServer()) {
                setStatusMessage("Unable to connect to server. " + jiraServer.getErrorMessage(), true);
//                progressAnimation.stopProgressAnimation();
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

            if (jiraServer.equals(IdeaHelper.getCurrentJIRAServer(project))) {
                filters = projectConfiguration.getJiraConfiguration()
                        .getJiraFilters(IdeaHelper.getCurrentJIRAServer(project).getServer().getServerId().toString());
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
        }
    }

    private void createFilterToolBar() {
        ActionManager actionManager = ActionManager.getInstance();
		DefaultActionGroup filterToolBar = new DefaultActionGroup("ThePlugin.JIRA.FilterToolBar" + project.getName(), false);

		filterToolBar.add(new FilterTypeAction());
		filterToolBar.add(new SavedFilterComboAction(project));
		filterToolBar.add(new JIRAShowIssuesFilterAction());
		filterToolBar.addSeparator();
		filterToolBar.add(new JIRAPreviousPageAcion());
		filterToolBar.add(new JIRANextPageAction());

		filterToolbar = actionManager.createActionToolbar("atlassian.toolwindow.filterToolBar" + project.getName(),
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

    @Override
	protected void filterEditToolbarSetVisible(boolean visible) {
        filterEditToolbar.getComponent().setVisible(visible);
    }

    public void prevPage() {
        startIndex -= PAGE_SIZE;
        checkPrevPageAvaialble();
		JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer(project);
		if (jiraServer != null) {
			updateIssues(jiraServer);
		}
	}

    public void nextPage() {
        startIndex += PAGE_SIZE;
        checkPrevPageAvaialble();
		JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer(project);
		if (jiraServer != null) {
			updateIssues(jiraServer);
		}
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

    private void checkNextPageAvailable(List<?> result) {
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
        //table.setEnabled(false);
        //table.setForeground(UIUtil.getInactiveTextColor());
        //clearIssues();
        disablePagesButton();

		final JIRAServerFacade serverFacade = jiraServerFacade;

		final List<JIRAQueryFragment> query = new ArrayList<JIRAQueryFragment>();

                checkTableSort();
                if (filters.getSavedFilterUsed()) {
                    if (savedQuery != null) {
                        query.add(savedQuery);
                        setStatusMessage("Retrieving issues from <b>" + jiraServer.getServer().getName() + "</b>...");
                        editorPane.setCaretPosition(0);

						Task.Backgroundable getIssues = new Task.Backgroundable(project, "Retrieving JIRA Issues", false) {
							private boolean failed = false;
							private List<JIRAIssue> result;

							public void run(final ProgressIndicator indicator) {
								try {
									result = serverFacade.getSavedFilterIssues(jiraServer.getServer(),
										query, sortColumn, sortOrder, startIndex, maxIndex);
								} catch (JIRAException e) {
									failed = true;
								}
							}

							public void onSuccess() {
								if (failed) {
									setStatusMessage("Error contacting server <b>"
											+ jiraServer.getServer().getName() + "</b>", true);
								} else {
									setIssues(result);
								}
							}
						};

						ProgressManager.getInstance().run(getIssues);
					}
                } else {
                    for (JIRAQueryFragment jiraQueryFragment : advancedQuery) {
                        if (jiraQueryFragment.getId() != JIRAServer.ANY_ID) {
                            query.add(jiraQueryFragment);
                        }
                    }
                    setStatusMessage("Retrieving issues from <b>" + jiraServer.getServer().getName() + "</b>...");
                    editorPane.setCaretPosition(0);

					Task.Backgroundable getIssues = new Task.Backgroundable(project, "Retrieving JIRA Issues", false) {
							private boolean failed = false;
							private List<JIRAIssue> result;

							public void run(final ProgressIndicator indicator) {
								try {
									result = serverFacade.getIssues(jiraServer.getServer(),
										query, sortColumn, sortOrder, startIndex, maxIndex);
								} catch (JIRAException e) {
									failed = true;
								}
							}

							public void onSuccess() {
								if (failed) {
									setStatusMessage("Error contacting server <b>"
											+ jiraServer.getServer().getName() + "</b>", true);
								} else {
									setIssues(result);
								}
							}
						};

						ProgressManager.getInstance().run(getIssues);
                }

//		Task.Backgroundable refresh = new IssueRefreshTask(jiraServer);
//		ProgressManager.getInstance().run(refresh);
	}

    private final class IssueRefreshTask extends Task.Backgroundable {
        private JIRAServer jiraServer;

        private IssueRefreshTask(JIRAServer jiraServer) {
			super(project, "Retrieving JIRA Issues", false);
			this.jiraServer = jiraServer;
        }

		private boolean failed = false;
		private List<JIRAIssue> result;

		public void run(final ProgressIndicator indicator) {
			JIRAServerFacade serverFacade = jiraServerFacade;
			try {
				List<JIRAQueryFragment> query = new ArrayList<JIRAQueryFragment>();
				result = serverFacade.getSavedFilterIssues(jiraServer.getServer(),
						query, sortColumn, sortOrder, startIndex, maxIndex);
			} catch (JIRAException e) {
				failed = true;
			}
		}

		public void onSuccess() {
			if (failed) {
				setStatusMessage("Error contacting server <b>"
						+ jiraServer.getServer().getName() + "</b>", true);
			} else {
				setIssues(result);
			}
		}
	}

    private void checkTableSort() {
		final int sortedColumnIndex = table.getTableViewModel().getSortedColumnIndex();

		if (sortedColumnIndex < 0) {
			return;
		}


		String columnName  = table.getTableViewModel().getColumnName(sortedColumnIndex);
		
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
		JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer(project);
		if (jiraServer != null) {
			projectConfiguration.getJiraConfiguration().
					setFiltersBean(jiraServer.getServer().getServerId().toString(), filters);
		}
	}

    @SuppressWarnings("unchecked")
    public List<JiraIssueAdapter> getIssues() {
        return listTableModel.getItems();
    }

    public JIRAIssue getCurrentIssue() {
        Object selectedObject = table.getSelectedObject();
        if (selectedObject != null) {
            return ((JiraIssueAdapter) selectedObject).getIssue();
        }
        return null;
    }

    public void assignIssueToMyself() {
        JiraIssueAdapter adapter = table.getSelectedObject();
        if (adapter == null) {
            return;
        }
        final JIRAIssue issue = adapter.getIssue();
        try {
			JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer(project);
			if (jiraServer != null) {
				assignIssue(issue, jiraServer.getServer().getUsername());
			}
		} catch (NullPointerException ex) {
            // whatever, means action was called when no issue was selected. Let's just swallow it
        }
    }

    public void assignIssueToSomebody() {
        final JIRAIssue issue = table.getSelectedObject().getIssue();

        final GetUserNameDialog getUserNameDialog = new GetUserNameDialog(issue.getKey());
        getUserNameDialog.show();
        if (getUserNameDialog.isOK()) {
            try {
                assignIssue(issue, getUserNameDialog.getName());
            } catch (NullPointerException ex) {
                // whatever, means action was called when no issue was selected. Let's just swallow it
            }
        }
    }

    public void createChangeListAction(Project projectArg) {
        JiraIssueAdapter adapter = table.getSelectedObject();
        if (adapter == null) {
            return;
        }
        final JIRAIssue issue = adapter.getIssue();
        String changeListName = issue.getKey() + " - " + issue.getSummary();
        final ChangeListManager changeListManager = ChangeListManager.getInstance(projectArg);

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
        JiraIssueAdapter adapter = table.getSelectedObject();
        if (adapter == null) {
            return;
        }
        final JIRAIssue issue = adapter.getIssue();
        final IssueComment issueComment = new IssueComment(issue.getKey());
        issueComment.show();
        if (issueComment.isOK()) {
			Task.Backgroundable comment = new Task.Backgroundable(project, "Commenting Issue", false) {
				public void run(final ProgressIndicator indicator) {
					setStatusMessage("Commenting issue " + issue.getKey() + "...");
                    try {
						JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer(project);
						if (jiraServer != null) {
							jiraServerFacade.addComment(jiraServer.getServer(),
									issue, issueComment.getComment());
							setStatusMessage("Commented issue " + issue.getKey());
						}
					} catch (JIRAException e) {
                        setStatusMessage("Issue not commented: " + e.getMessage(), true);
                    }
				}
			};

			ProgressManager.getInstance().run(comment);
		}
   }

    public void logWorkForIssue() {
		JiraIssueAdapter adapter = table.getSelectedObject();
        if (adapter == null) {
            return;
        }
        final JIRAIssue issue = adapter.getIssue();
        final WorkLogCreate workLogCreate = new WorkLogCreate(jiraServerFacade, adapter, project);
        workLogCreate.show();
        if (workLogCreate.isOK()) {

			Task.Backgroundable logWork = new Task.Backgroundable(project, "Logging Work", false) {
				public void run(final ProgressIndicator indicator) {
					setStatusMessage("Logging work for issue " + issue.getKey() + "...");
                    try {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(workLogCreate.getStartDate());
						JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer(project);

						if (jiraServer != null) {
							JiraServerCfg server = jiraServer.getServer();
							String newRemainingEstimate = workLogCreate.getUpdateRemainingManually()
									? workLogCreate.getRemainingEstimateString() : null;
							jiraServerFacade.logWork(server, issue, workLogCreate.getTimeSpentString(),
									cal, workLogCreate.getComment(),
									!workLogCreate.getLeaveRemainingUnchanged(), newRemainingEstimate);
							if (workLogCreate.isStopProgressSelected()) {
								setStatusMessage("Stopping work for issue " + issue.getKey() + "...");
								jiraServerFacade.progressWorkflowAction(server, issue, workLogCreate.getInProgressAction());
								setStatusMessage("Work logged and progress stopped for issue " + issue.getKey());
								refreshIssuesPage();
							} else {
								setStatusMessage("Logged work for issue " + issue.getKey());
							}
						}
					} catch (JIRAException e) {
                        setStatusMessage("Work not logged: " + e.getMessage(), true);
                    }
				}
			};

			ProgressManager.getInstance().run(logWork);
        }
    }

    private void assignIssue(final JIRAIssue issue, final String assignee) {

		Task.Backgroundable assign = new Task.Backgroundable(project, "Assigning Issue", false) {

			public void run(final ProgressIndicator indicator) {
				setStatusMessage("Assigning issue " + issue.getKey() + " to " + assignee + "...");
                try {

					JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer(project);
					if (jiraServer != null) {
						jiraServerFacade.setAssignee(jiraServer.getServer(), issue, assignee);
						setStatusMessage("Assigned issue " + issue.getKey() + " to " + assignee);
					}
				} catch (JIRAException e) {
                    setStatusMessage("Failed to assign issue: " + e.getMessage(), true);
                }
			}
		};

		ProgressManager.getInstance().run(assign);
    }

    public void createIssue() {
		final JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer(project);

        if (jiraServer != null) {
            final IssueCreate issueCreate = new IssueCreate(jiraServer);
            issueCreate.initData();
            issueCreate.show();
            if (issueCreate.isOK()) {

				Task.Backgroundable createTask = new Task.Backgroundable(project, "Creating Issue", false) {
					public void run(final ProgressIndicator indicator) {
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
				};

				ProgressManager.getInstance().run(createTask);
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