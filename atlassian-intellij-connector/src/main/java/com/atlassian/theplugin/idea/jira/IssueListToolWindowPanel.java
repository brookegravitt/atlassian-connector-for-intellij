package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.configuration.IssueRecentlyOpenBean;
import com.atlassian.theplugin.configuration.JiraFilterConfigurationBean;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.PluginToolWindowPanel;
import com.atlassian.theplugin.idea.action.issues.RunIssueActionAction;
import com.atlassian.theplugin.idea.action.issues.activetoolbar.ActiveIssueUtils;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.atlassian.theplugin.idea.jira.tree.JIRAFilterTree;
import com.atlassian.theplugin.idea.jira.tree.JIRAIssueTreeBuilder;
import com.atlassian.theplugin.idea.jira.tree.JIRAIssueTreeNode;
import com.atlassian.theplugin.idea.jira.tree.JiraFilterTreeSelectionListener;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.idea.ui.PopupAwareMouseAdapter;
import com.atlassian.theplugin.jira.JIRAIssueProgressTimestampCache;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.jira.cache.RecentlyOpenIssuesCache;
import com.atlassian.theplugin.jira.model.*;
import com.atlassian.theplugin.remoteapi.MissingPasswordHandlerJIRA;
import com.atlassian.theplugin.remoteapi.MissingPasswordHandlerQueue;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;

public final class IssueListToolWindowPanel extends PluginToolWindowPanel implements DataProvider, IssueActionProvider {

	public static final String PLACE_PREFIX = IssueListToolWindowPanel.class.getSimpleName();
	private ProjectCfgManagerImpl projectCfgManager;
	private final PluginConfiguration pluginConfiguration;
	private JiraWorkspaceConfiguration jiraWorkspaceConfiguration;

	private static final String SERVERS_TOOL_BAR = "ThePlugin.JiraServers.ServersToolBar";
	private JIRAFilterListModel jiraFilterListModel;
	private JIRAIssueTreeBuilder issueTreeBuilder;
	private JIRAIssueListModelBuilder jiraIssueListModelBuilder;
	private RecentlyOpenIssuesCache recentlyOpenIssuesCache;
	private JIRAFilterListBuilder jiraFilterListModelBuilder;
	private JiraIssueGroupBy groupBy;
	@NotNull
	private final JiraManualFilterDetailsPanel manualFilterEditDetailsPanel;
	private JIRAFilterTree jiraFilterTree;

	private JIRAServerFacade jiraServerFacade;

	private JIRAIssueListModel currentIssueListModel;

	private SearchingJIRAIssueListModel searchingIssueListModel;

	private JIRAServerModel jiraServerModel;

	private ConfigurationListener configListener = new LocalConfigurationListener();

	private boolean groupSubtasksUnderParent;

	private static final String THE_PLUGIN_JIRA_ISSUES_ISSUES_TOOL_BAR = "ThePlugin.JiraIssues.IssuesToolBar";

	private JIRAIssueListModel baseIssueListModel;

	private Timer timer;

	private static final int ONE_SECOND = 1000;

    public IssueListToolWindowPanel(@NotNull final Project project,
			@NotNull final ProjectCfgManagerImpl projectCfgManager,
			@NotNull final PluginConfiguration pluginConfiguration,
			@NotNull final JiraWorkspaceConfiguration jiraWorkspaceConfiguration,
			@NotNull final IssueToolWindowFreezeSynchronizator freezeSynchronizator,
			@NotNull final JIRAIssueListModel issueModel,
			@NotNull final JIRAIssueListModelBuilder jiraIssueListModelBuilder,
			@NotNull final RecentlyOpenIssuesCache recentlyOpenIssuesCache,
			@NotNull final JIRAFilterListBuilder filterListBuilder,
			@NotNull final JIRAServerModel jiraServerModel) {
		super(project, SERVERS_TOOL_BAR, THE_PLUGIN_JIRA_ISSUES_ISSUES_TOOL_BAR);

		this.projectCfgManager = projectCfgManager;
		this.pluginConfiguration = pluginConfiguration;
		this.jiraWorkspaceConfiguration = jiraWorkspaceConfiguration;
		this.jiraIssueListModelBuilder = jiraIssueListModelBuilder;
		this.recentlyOpenIssuesCache = recentlyOpenIssuesCache;

		jiraServerFacade = JIRAServerFacadeImpl.getInstance();

		if (jiraWorkspaceConfiguration.getView() != null && jiraWorkspaceConfiguration.getView().getGroupBy() != null) {
			groupBy = jiraWorkspaceConfiguration.getView().getGroupBy();
			groupSubtasksUnderParent = jiraWorkspaceConfiguration.getView().isCollapseSubtasksUnderParent();
		} else {
			groupBy = JiraIssueGroupBy.TYPE;
			groupSubtasksUnderParent = false;
		}
		jiraFilterListModel = getJIRAFilterListModel();
		baseIssueListModel = issueModel;
		JIRAIssueListModel sortingIssueListModel = new SortingByPriorityJIRAIssueListModel(baseIssueListModel);
		searchingIssueListModel = new SearchingJIRAIssueListModel(sortingIssueListModel);
		currentIssueListModel = searchingIssueListModel;

		issueTreeBuilder = new JIRAIssueTreeBuilder(getGroupBy(), groupSubtasksUnderParent, currentIssueListModel,
				jiraServerModel, projectCfgManager);

		this.jiraServerModel = jiraServerModel;

		jiraIssueListModelBuilder.setModel(baseIssueListModel);
		jiraFilterListModelBuilder = filterListBuilder;
		if (jiraFilterListModelBuilder != null) {
			jiraFilterListModelBuilder.setListModel(jiraFilterListModel);
//			jiraFilterListModelBuilder.setProjectId(CfgUtil.getProjectId(project));
			jiraFilterListModelBuilder.setJiraWorkspaceCfg(jiraWorkspaceConfiguration);
		}
		currentIssueListModel.addModelListener(new LocalJiraIssueListModelListener());

		currentIssueListModel.addFrozenModelListener(new FrozenModelListener() {
			public void modelFrozen(FrozenModel model, boolean frozen) {
				if (getStatusBarPane() != null) {
					getStatusBarPane().setEnabled(!frozen);
				}
				if (getSearchField() != null) {
					getSearchField().setEnabled(!frozen);
				}
			}
		});

		manualFilterEditDetailsPanel = new JiraManualFilterDetailsPanel(jiraFilterListModel, this.jiraWorkspaceConfiguration,
				getProject(), jiraServerModel);

		getStatusBarPane().addMoreIssuesListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				refreshIssues(false);
			}
		});

		addIssuesTreeListeners();
		addSearchBoxListener();
		freezeSynchronizator.setIssueModel(currentIssueListModel);
		freezeSynchronizator.setServerModel(jiraServerModel);
		freezeSynchronizator.setFilterModel(jiraFilterListModel);

		init(0);


		jiraFilterTree.addSelectionListener(new LocalJiraFilterTreeSelectionListener());

		jiraFilterListModel.addModelListener(new JIRAFilterListModelListener() {
			public void manualFilterChanged(final JIRAManualFilter manualFilter, final ServerData jiraServer) {
				// refresh issue list
				refreshIssues(manualFilter, jiraServer, true);
			}

			public void modelChanged(final JIRAFilterListModel listModel) {
			}

			public void serverRemoved(final JIRAFilterListModel filterListModel) {
			}

			public void serverAdded(final JIRAFilterListModel filterListModel) {
			}

			public void serverNameChanged(final JIRAFilterListModel filterListModel) {
			}
		});

	}

	protected void showManualFilterPanel(final JIRAManualFilter manualFilter, final ServerData jiraServerCfg) {
		getSplitLeftPane().setOrientation(true);
		manualFilterEditDetailsPanel.setFilter(manualFilter, jiraServerCfg);
		getSplitLeftPane().setSecondComponent(manualFilterEditDetailsPanel);
		getSplitLeftPane().setProportion(MANUAL_FILTER_PROPORTION_VISIBLE);
	}

	protected void hideManualFilterPanel() {
		getSplitLeftPane().setOrientation(true);
		getSplitLeftPane().setSecondComponent(null);
		getSplitLeftPane().setProportion(MANUAL_FILTER_PROPORTION_HIDDEN);
	}

	public ProjectCfgManagerImpl getProjectCfgManager() {
		return projectCfgManager;
	}

	public void init() {
		ProgressManager.getInstance().run(new Task.Backgroundable(project, "Retrieving recently viewed issues", false) {
			public void run(@NotNull final ProgressIndicator progressindicator) {
				recentlyOpenIssuesCache.loadRecenltyOpenIssues();
			}
		});
	}

	@Override
	protected void addSearchBoxListener() {
		getSearchField().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				triggerDelayedSearchBoxUpdate();
			}

			public void removeUpdate(DocumentEvent e) {
				triggerDelayedSearchBoxUpdate();
			}

			public void changedUpdate(DocumentEvent e) {
				triggerDelayedSearchBoxUpdate();
			}
		});
		getSearchField().addKeyboardListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					getSearchField().addCurrentTextToHistory();
				}
			}

			public void keyReleased(KeyEvent e) {
			}
		});
	}

	private void triggerDelayedSearchBoxUpdate() {
		if (timer != null && timer.isRunning()) {
			return;
		}
		timer = new Timer(ONE_SECOND, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchingIssueListModel.setSearchTerm(getSearchField().getText());
			}
		});
		timer.setRepeats(false);
		timer.start();
	}

	public JIRAIssueListModel getBaseIssueListModel() {
		return baseIssueListModel;
	}


	private void addIssuesTreeListeners() {
		getRightTree().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				JIRAIssue issue = getSelectedIssue();
				if (e.getKeyCode() == KeyEvent.VK_ENTER && issue != null) {
					openIssue(issue, true);
				}
			}
		});

		getRightTree().addMouseListener(new PopupAwareMouseAdapter() {

			@Override
			public void mouseClicked(final MouseEvent e) {
				final JIRAIssue issue = getSelectedIssue();
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 && issue != null) {
					openIssue(issue, true);
				}
			}

			@Override
			protected void onPopup(MouseEvent e) {
				int selRow = getRightTree().getRowForLocation(e.getX(), e.getY());
				TreePath selPath = getRightTree().getPathForLocation(e.getX(), e.getY());
				if (selRow != -1 && selPath != null) {
					getRightTree().setSelectionPath(selPath);
					if (getSelectedIssue() != null) {
						launchContextMenu(e);
					}
				}
			}
		});
	}

	private void launchContextMenu(MouseEvent e) {
		final DefaultActionGroup actionGroup = new DefaultActionGroup();

		final ActionGroup configActionGroup = (ActionGroup) ActionManager
				.getInstance().getAction("ThePlugin.JiraIssues.IssuePopupMenu");
		actionGroup.addAll(configActionGroup);

		final ActionPopupMenu popup = ActionManager.getInstance().createActionPopupMenu("Context menu", actionGroup);
		addIssueActionsSubmenu(actionGroup, popup);

		final JPopupMenu jPopupMenu = popup.getComponent();
		jPopupMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	private void addIssueActionsSubmenu(DefaultActionGroup actionGroup, final ActionPopupMenu popup) {
		final DefaultActionGroup submenu = new DefaultActionGroup("Querying for Actions... ", true) {
			@Override
			public void update(AnActionEvent event) {
				super.update(event);

				if (getChildrenCount() > 0) {
					event.getPresentation().setText("Issue Workflow Actions");
				}
			}
		};
		actionGroup.add(submenu);

		final JIRAIssue issue = getSelectedIssue();
		List<JIRAAction> actions = JiraIssueAdapter.get(issue).getCachedActions();
		if (actions != null) {
			for (JIRAAction a : actions) {
				submenu.add(new RunIssueActionAction(this, jiraServerFacade, issue, a, jiraIssueListModelBuilder));
			}
		} else {
			Thread t = new Thread() {
				@Override
				public void run() {
					try {
						ServerData jiraServer = issue.getServer();

						if (jiraServer != null) {
							final List<JIRAAction> actions = jiraServerFacade.getAvailableActions(jiraServer, issue);

							JiraIssueAdapter.get(issue).setCachedActions(actions);
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									JPopupMenu pMenu = popup.getComponent();
									if (pMenu.isVisible()) {
										for (JIRAAction a : actions) {
											submenu.add(new RunIssueActionAction(IssueListToolWindowPanel.this,
													jiraServerFacade, issue, a, jiraIssueListModelBuilder));
										}

										// magic that makes the popup update itself. Don't ask - it is some sort of voodoo
										pMenu.setVisible(false);
										pMenu.setVisible(true);
									}
								}
							});
						}
					} catch (JIRAException e) {
						setStatusErrorMessage("Query for issue " + issue.getKey() + " actions failed: " + e.getMessage(), e);
					} catch (NullPointerException e) {
						// somebody unselected issue in the table, so let's just skip
					}
				}
			};
			t.start();
		}
	}

	private JIRAIssue getSelectedIssue() {
		return getRightTree().getSelectedIssue();
	}

	public void openIssue(@NotNull JIRAIssue issue, boolean reload) {
		if (issue.getServer() != null) {
			recentlyOpenIssuesCache.addIssue(issue);
			// todo check active issue
			IdeaHelper.getIssueDetailsToolWindow(getProject()).showIssue(issue, baseIssueListModel);
            if (reload) {
                IdeaHelper.getIssueDetailsToolWindow(getProject()).refresh(issue.getKey());
            }

		}
	}

	public void openIssue(@NotNull final String issueKey, @NotNull final ServerData jiraServer) {
		JIRAIssue issue = null;
		for (JIRAIssue i : baseIssueListModel.getIssues()) {
			if (i.getKey().equals(issueKey) && i.getServer().getServerId().equals(jiraServer.getServerId())) {
				issue = i;
				break;
			}
		}

		if (issue != null) {
			openIssue(issue, true);
		} else {
			Task.Backgroundable task = new Task.Backgroundable(getProject(), "Fetching JIRA issue " + issueKey, false) {
				private JIRAIssue issue;
				private Throwable exception;

				@Override
				public void run(@NotNull ProgressIndicator progressIndicator) {
					progressIndicator.setIndeterminate(true);
					try {
						if (jiraServer != null) {
							issue = jiraServerFacade.getIssue(jiraServer, issueKey);
							jiraIssueListModelBuilder.updateIssue(issue);
//							recentlyOpenIssuesCache.addIssue(issue);
						} else {
							exception = new RuntimeException("No JIRA server defined!");
						}
					} catch (JIRAException e) {
						exception = e;
					}
				}

				@Override
				public void onSuccess() {
					if (getProject().isDisposed()) {
						return;
					}
					if (exception != null) {
						final String serverName = jiraServer != null ? jiraServer.getName() : "[UNDEFINED!]";
						DialogWithDetails.showExceptionDialog(getProject(),
								"Cannot fetch issue " + issueKey + " from server " + serverName, exception);
						return;
					}
					if (issue != null) {
						openIssue(issue, true);
						if (jiraFilterTree.isRecentlyOpenSelected()) {
							refreshRecenltyOpenIssues(false);
						}
					}
				}
			};
			task.queue();
		}
	}

	public boolean openIssue(@NotNull final String issueKey, @NotNull final String serverUrl) {

		ServerData server = CfgUtil.findServer(
				serverUrl, projectCfgManager.getAllServers(ServerType.JIRA_SERVER), projectCfgManager);

		if (server != null) {
			openIssue(issueKey, server);
			return true;
		}

		// server not found by exact url, trying to remove protocol from the address (http vs https) and slash at the end
		URL url;

		try {
			url = new URL(serverUrl);
		} catch (MalformedURLException e) {
			PluginUtil.getLogger().warn("Error opening issue. Invalid url [" + serverUrl + "]", e);
			return false;
		}

		server = CfgUtil.findServer(url, projectCfgManager.getAllServers(ServerType.JIRA_SERVER), projectCfgManager);

		if (server != null) {
			openIssue(issueKey, server);
			return true;
		}

		return false;
	}

	public void assignIssueToMyself(@NotNull final JIRAIssue issue) {
		// todo remove if statement
		if (issue == null) {
			return;
		}
		try {
			ServerData jiraServer = getSelectedServer();
			if (jiraServer != null) {
				assignIssue(issue, jiraServer.getUserName());
			}
		} catch (NullPointerException ex) {
			// todo remove NPE catch
			// whatever, means action was called when no issue was selected. Let's just swallow it
		}
	}

	public void assignIssueToSomebody(@NotNull final JIRAIssue issue) {
		if (issue == null) {
			return;
		}
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

	private void assignIssue(final JIRAIssue issue, final String assignee) {

		Task.Backgroundable assign = new Task.Backgroundable(getProject(), "Assigning Issue", false) {

			@Override
			public void run(@NotNull final ProgressIndicator indicator) {
				setStatusInfoMessage("Assigning issue " + issue.getKey() + " to " + assignee + "...");
				try {

					ServerData jiraServer = getSelectedServer();
					if (jiraServer != null) {
						jiraServerFacade.setAssignee(jiraServer, issue, assignee);
						setStatusInfoMessage("Assigned issue " + issue.getKey() + " to " + assignee);
						jiraIssueListModelBuilder.reloadIssue(issue.getKey(), jiraServer);
					}
				} catch (JIRAException e) {
					setStatusErrorMessage("Failed to assign issue " + issue.getKey() + ": " + e.getMessage(), e);
				}
			}
		};

		ProgressManager.getInstance().run(assign);
	}

	public boolean createChangeListAction(@NotNull final JIRAIssue issue) {
		String changeListName = issue.getKey() + " - " + issue.getSummary();
		final ChangeListManager changeListManager = ChangeListManager.getInstance(getProject());
		ChangesetCreate c;

		LocalChangeList changeList = changeListManager.findChangeList(changeListName);
		if (changeList == null) {
			c = new ChangesetCreate(issue.getKey());
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
			return true;
		}

		return c.isOK();
	}

	public void addCommentToSelectedIssue() {
		// todo move getSelectedIssue from the model to the tree
		final JIRAIssue issue = getSelectedIssue();
		if (issue != null) {
			addCommentToIssue(issue.getKey(), issue.getServer());
		}
	}

	public void addCommentToIssue(final String issueKey, final ServerData jiraServer) {
		final IssueCommentDialog issueCommentDialog = new IssueCommentDialog(issueKey);
		issueCommentDialog.show();
		if (issueCommentDialog.isOK()) {
			Task.Backgroundable comment = new Task.Backgroundable(getProject(), "Commenting Issue", false) {
				@Override
				public void run(@NotNull final ProgressIndicator indicator) {
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							setStatusInfoMessage("Commenting issue " + issueKey + "...");
						}
					});
					try {
						if (jiraServer != null) {
							jiraServerFacade.addComment(jiraServer, issueKey, issueCommentDialog.getComment());
							EventQueue.invokeLater(new Runnable() {
								public void run() {
									setStatusInfoMessage("Commented issue " + issueKey);
								}
							});
						}
					} catch (final JIRAException e) {
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								setStatusErrorMessage("Issue not commented: " + e.getMessage(), e);
							}
						});
					}
				}
			};

			ProgressManager.getInstance().run(comment);
		}
	}

	public boolean logWorkOrDeactivateIssue(final JIRAIssue issue, final ServerData jiraServer, String initialLog,
			final boolean deactivateIssue, DeactivateIssueResultHandler resultHandler) {
		if (issue != null) {
			final WorkLogCreateAndMaybeDeactivateDialog dialog =
					new WorkLogCreateAndMaybeDeactivateDialog(jiraServer, issue, getProject(), initialLog,
							deactivateIssue, jiraWorkspaceConfiguration);
			dialog.show();
			if (dialog.isOK()) {
				Task.Backgroundable logWork =
						new LogWorkWorkerTask(issue, dialog, jiraServer, deactivateIssue, resultHandler);
				ProgressManager.getInstance().run(logWork);
			}
			return dialog.isOK();
		}
		return false;
	}

	/**
	 * Blocking method. Must be called in the background thread.
	 *
	 * @param issue issue to work on
	 * @return modified issue or the same issue if no modification performed
	 */
	private JIRAIssue assignIssueAndPutInProgress(@NotNull final JIRAIssue issue) {
		JIRAIssue updatedIssue = issue;
		final ServerData server = issue.getServer();

		if (!issue.getAssigneeId().equals(server.getUserName())) {
			setStatusInfoMessage("Assigning issue " + issue.getKey() + " to me...");
			try {
				jiraServerFacade.setAssignee(server, issue, server.getUserName());
			} catch (JIRAException e) {
				final String msg = "Error starting progress on issue. Assigning failed: ";
				setStatusErrorMessage(msg + e.getMessage(), e);
				PluginUtil.getLogger().warn(msg + e.getMessage(), e);
				return updatedIssue;
			}
		}

		setStatusInfoMessage("Retrieving available actions for issue");
		List<JIRAAction> actions;
		try {
			actions = jiraServerFacade.getAvailableActions(server, issue);
		} catch (JIRAException e) {
			final String msg = "Error starting progress on issue. Retrieving actions failed: ";
			setStatusErrorMessage(msg + e.getMessage(), e);
			PluginUtil.getLogger().warn(msg + e.getMessage(), e);
			return updatedIssue;
		}

        boolean statusChanged = false;
        for (JIRAAction a : actions) {
            if (a.getId() == Constants.JiraActionId.START_PROGRESS.getId()) {
                setStatusInfoMessage("Starting progress on " + issue.getKey() + "...");
                try {
                    jiraServerFacade.progressWorkflowAction(server, issue, a);
                    statusChanged = true;
                } catch (JIRAException e) {
                    final String msg = "Error starting progress on issue. Perform workflow action failed: ";
                    setStatusErrorMessage(msg + e.getMessage(), e);
                    PluginUtil.getLogger().warn(msg + e.getMessage(), e);
                    return updatedIssue;
                }
                JIRAIssueProgressTimestampCache.getInstance().setTimestamp(server, issue);
                break;
            }
        }

        if (!statusChanged) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Messages.showInfoMessage(project, "Available actions do not allow to change state to In Progress"
                            , "Cannot start progress on " + issue.getKey());
                }
            });
        }
        
		setStatusInfoMessage("Refreshing issue");
		try {
			updatedIssue = jiraServerFacade.getIssue(server, issue.getKey());
		} catch (JIRAException e) {
			setStatusErrorMessage("Error starting progress on issue: " + e.getMessage(), e);
			PluginUtil.getLogger().warn("Error refreshing issue: " + e.getMessage(), e);
			return updatedIssue;
		}

		if (updatedIssue.getStatusId() != Constants.JiraStatusId.IN_PROGRESS.getId()) {
			setStatusErrorMessage("Progress on " + issue.getKey() + " not started on JIRA side");
		} else {
			setStatusInfoMessage("Progress on " + issue.getKey() + " started");
		}

		return updatedIssue;
	}

	public void startWorkingOnIssueAndActivate(@NotNull final JIRAIssue issue, final ActiveJiraIssue newActiveIssue) {

		final boolean isOk = createChangeListAction(issue);

		if (isOk) {
			ProgressManager.getInstance().run(new Task.Backgroundable(getProject(), "Starting Work on Issue", false) {

				private JIRAIssue updatedIssue = issue;

				@Override
				public void run(@NotNull final ProgressIndicator indicator) {
					updatedIssue = assignIssueAndPutInProgress(issue);
				}

				public void onSuccess() {
					jiraIssueListModelBuilder.updateIssue(updatedIssue);
					ActiveIssueUtils.setActiveJiraIssue(project, newActiveIssue, updatedIssue);
				}
			});
		} else {
			ActiveIssueUtils.setActiveJiraIssue(project, null, issue);
		}

		pluginConfiguration.getGeneralConfigurationData().bumpCounter("a");
	}

	private void refreshFilterModel() {
		try {
			jiraFilterListModelBuilder.rebuildModel(jiraServerModel);
		} catch (JIRAFilterListBuilder.JIRAServerFiltersBuilderException e) {
			Collection<Throwable> exceptions = new ArrayList<Throwable>();
			for (JIRAException ex : e.getExceptions().values()) {
				exceptions.add(ex);
			}
			//@todo show in message editPane
			setStatusErrorMessages("Some Jira servers did not return saved filters", exceptions);
		}
	}


	public void refreshIssues(final boolean reload) {
		JIRAManualFilter manualFilter = jiraFilterTree.getSelectedManualFilter();
		JIRASavedFilter savedFilter = jiraFilterTree.getSelectedSavedFilter();
		ServerData serverCfg = getSelectedServer();
		if (savedFilter != null) {
			refreshIssues(savedFilter, serverCfg, reload);
		} else if (manualFilter != null) {
			refreshIssues(manualFilter, serverCfg, reload);
		} else if (jiraFilterTree.isRecentlyOpenSelected()) {
			refreshRecenltyOpenIssues(reload);
		}
	}

	private void refreshIssues(final JIRAManualFilter manualFilter, final ServerData jiraServerCfg, final boolean reload) {
		if (WindowManager.getInstance().getIdeFrame(getProject()) == null) {
			return;
		}
		Task.Backgroundable task = new Task.Backgroundable(getProject(), "Retrieving issues", false) {
			@Override
			public void run(@NotNull final ProgressIndicator indicator) {
				try {
					getStatusBarPane().setInfoMessage("Loading issues...", false);
					jiraIssueListModelBuilder.addIssuesToModel(manualFilter, jiraServerCfg,
							pluginConfiguration.getJIRAConfigurationData().getPageSize(), reload);
				} catch (JIRAException e) {
					setStatusErrorMessage(e.getMessage(), e);
				}
			}
		};
		ProgressManager.getInstance().run(task);
	}

	private void refreshIssues(final JIRASavedFilter savedFilter, final ServerData jiraServerCfg, final boolean reload) {
		if (WindowManager.getInstance().getIdeFrame(getProject()) == null) {
			return;
		}
		Task.Backgroundable task = new Task.Backgroundable(getProject(), "Retrieving issues", false) {
			@Override
			public void run(@NotNull final ProgressIndicator indicator) {
				try {
					getStatusBarPane().setInfoMessage("Loading issues...", false);
					jiraIssueListModelBuilder.addIssuesToModel(savedFilter, jiraServerCfg,
							pluginConfiguration.getJIRAConfigurationData().getPageSize(), reload);
				} catch (JIRAException e) {
					setStatusErrorMessage(e.getMessage(), e);
				}
			}
		};
		ProgressManager.getInstance().run(task);
	}


	private void refreshRecenltyOpenIssues(final boolean reload) {
		if (WindowManager.getInstance().getIdeFrame(getProject()) == null) {
			return;
		}
		Task.Backgroundable task = new Task.Backgroundable(getProject(), "Retrieving issues", false) {
			@Override
			public void run(@NotNull final ProgressIndicator indicator) {
				try {
					getStatusBarPane().setInfoMessage("Loading issues...", false);
					jiraIssueListModelBuilder.addRecenltyOpenIssuesToModel(reload);
				} catch (JIRAException e) {
					setStatusErrorMessage(e.getMessage(), e);
				}
			}
		};
		ProgressManager.getInstance().run(task);
	}

	@SuppressWarnings({"UnusedDeclaration"})
	public void configurationUpdated(final ProjectConfiguration aProjectConfiguration) {
		refreshModels();
	}

	/**
	 * Must be called from dispatch thread
	 */
	public void refreshModels() {
		Task.Backgroundable task = new MetadataFetcherBackgroundableTask();
		ProgressManager.getInstance().run(task);
	}


	public void projectRegistered() {

	}

	public JiraIssueGroupBy getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(JiraIssueGroupBy groupBy) {
		this.groupBy = groupBy;
		issueTreeBuilder.setGroupBy(groupBy);
		issueTreeBuilder.rebuild(getRightTree(), getRightPanel());
//		expandAllRightTreeNodes();

		// store in project workspace
		jiraWorkspaceConfiguration.getView().setGroupBy(groupBy);
	}

	public void createIssue() {

		if (jiraIssueListModelBuilder == null) {
			return;
		}

		final ServerData server = getSelectedServer();

		if (server != null) {
			final IssueCreateDialog issueCreateDialog =
					new IssueCreateDialog(this, project, jiraServerModel, server, jiraWorkspaceConfiguration);

			issueCreateDialog.initData();
			issueCreateDialog.show();
		}
	}

	public ConfigurationListener getConfigListener() {
		return configListener;
	}

	public boolean isGroupSubtasksUnderParent() {
		return groupSubtasksUnderParent;
	}

	public void setGroupSubtasksUnderParent(boolean state) {
		if (state != groupSubtasksUnderParent) {
			groupSubtasksUnderParent = state;
			issueTreeBuilder.setGroupSubtasksUnderParent(groupSubtasksUnderParent);
			issueTreeBuilder.rebuild(getRightTree(), getRightScrollPane());
//			expandAllRightTreeNodes();
			jiraWorkspaceConfiguration.getView().setCollapseSubtasksUnderParent(groupSubtasksUnderParent);
		}
	}

	public JIRAFilterListModel getJIRAFilterListModel() {
		if (jiraFilterListModel == null) {
			jiraFilterListModel = new JIRAFilterListModel();
		}
		return jiraFilterListModel;
	}

	public ServerData getSelectedServer() {
		ServerData server = jiraFilterTree != null ? jiraFilterTree.getSelectedServer() : null;
		if (server != null) {
			return server;
		}

		if (getSelectedIssue() != null) {
			return getSelectedIssue().getServer();
		}

		if (projectCfgManager.getDefaultJiraServer() != null) {
			return projectCfgManager.getDefaultJiraServer();
		}


		return null;
	}

	public boolean isRecentlyOpenFilterSelected() {
		return jiraFilterTree != null && jiraFilterTree.isRecentlyOpenSelected();
	}

	/**
	 * @return list of recenlty open issues loaded earlier
	 */
	public List<JIRAIssue> getLoadedRecenltyOpenIssues() {
		return new ArrayList<JIRAIssue>(recentlyOpenIssuesCache.getLoadedRecenltyOpenIssues());
	}

//	public List<JIRAIssue> loadRecenltyOpenIssues() {
//		return new ArrayList<JIRAIssue>(recentlyOpenIssuesCache.loadRecenltyOpenIssues());
//	}


	private class MetadataFetcherBackgroundableTask extends Task.Backgroundable {
		private Collection<ServerData> servers = null;
		private boolean refreshIssueList = false;

		/**
		 * Clear server model and refill it with all enabled servers' data
		 */
		public MetadataFetcherBackgroundableTask() {
			super(IssueListToolWindowPanel.this.getProject(), "Retrieving JIRA information", false);
			fillServerData();
			jiraServerModel.clearAll();
			refreshIssueList = true;
		}

		private void fillServerData() {
			servers = new ArrayList<ServerData>();
			for (JiraServerCfg serverCfg : projectCfgManager.getAllEnabledJiraServers()) {
				servers.add(projectCfgManager.getServerData(serverCfg));
			}
		}

		/**
		 * Add requestes server's data to the server model
		 *
		 * @param server		   server added to the model with all fetched data
		 * @param refreshIssueList refresh issue list
		 */
		public MetadataFetcherBackgroundableTask(final JiraServerCfg server, boolean refreshIssueList) {
			super(IssueListToolWindowPanel.this.getProject(), "Retrieving JIRA information", false);
			this.servers = Arrays.asList(projectCfgManager.getServerData(server));
			this.refreshIssueList = refreshIssueList;
		}

		@Override
		public void run(@NotNull final ProgressIndicator indicator) {

			try {
				jiraServerModel.setModelFrozen(true);

				for (ServerData server : servers) {
					try {
						//returns false if no cfg is available or login failed
						Boolean serverCheck = jiraServerModel.checkServer(server);
						if (serverCheck == null || !serverCheck) {
							setStatusErrorMessage("Unable to connect to server. " + jiraServerModel.getErrorMessage(server));
							MissingPasswordHandlerQueue.addHandler(new MissingPasswordHandlerJIRA(jiraServerFacade,
									(JiraServerCfg) projectCfgManager.getServer(server), project));
							continue;
						}//@todo remove  saved filters download or merge with existing in listModel

						final String serverStr = "[" + server.getName() + "] ";
						setStatusInfoMessage(serverStr + "Retrieving saved filters...");
						jiraServerModel.getSavedFilters(server);
						setStatusInfoMessage(serverStr + "Retrieving projects...");
						jiraServerModel.getProjects(server);
						setStatusInfoMessage(serverStr + "Retrieving issue types...");
						jiraServerModel.getIssueTypes(server, null, true);
						setStatusInfoMessage(serverStr + "Retrieving statuses...");
						jiraServerModel.getStatuses(server);
						setStatusInfoMessage(serverStr + "Retrieving resolutions...");
						jiraServerModel.getResolutions(server, true);
						setStatusInfoMessage(serverStr + "Retrieving priorities...");
						jiraServerModel.getPriorities(server, true);
						setStatusInfoMessage(serverStr + "Retrieving projects...");
						jiraServerModel.getProjects(server);
						setStatusInfoMessage(serverStr + "Server data query finished");
					} catch (RemoteApiException e) {
						setStatusErrorMessage("Unable to connect to server. " + jiraServerModel.getErrorMessage(server), e);
					} catch (JIRAException e) {
						setStatusErrorMessage("Cannot download details:" + e.getMessage(), e);
					}
				}
			} finally {
				// todo it should be probably called in the UI thread as most frozen listeners do something with UI controls
				jiraServerModel.setModelFrozen(false);
			}
		}

		@Override
		public void onSuccess() {
			refreshFilterModel();
			if (refreshIssueList) {
				jiraFilterListModel.fireModelChanged();
			} else {
				jiraFilterListModel.fireServerAdded();
			}
		}
	}

	@Nullable
	public Object getData(@NotNull final String dataId) {
		if (dataId.equals(Constants.ISSUE)) {
			return getSelectedIssue();
		}
		if (dataId.equals(Constants.SERVER)) {
			return getSelectedServer();
		}
		return null;
	}

	private class LocalConfigurationListener extends ConfigurationListenerAdapter {

		@Override
		public void serverConnectionDataChanged(final ServerId serverId) {
			ServerCfg server = projectCfgManager.getServer(serverId);
			if (server instanceof JiraServerCfg && server.getServerType() == ServerType.JIRA_SERVER) {
				jiraServerModel.clear(server.getServerId());
				Task.Backgroundable task = new MetadataFetcherBackgroundableTask((JiraServerCfg) server, true);
				ProgressManager.getInstance().run(task);
			}
		}

		@Override
		public void serverNameChanged(final ServerId serverId) {
			ServerCfg server = projectCfgManager.getServer(serverId);
			if (server instanceof JiraServerCfg) {
				jiraServerModel.replace(projectCfgManager.getServerData(server));
				refreshFilterModel();
				jiraFilterListModel.fireServerNameChanged();
			}
		}

		@Override
		public void serverDisabled(final ServerId serverId) {
			ServerCfg server = projectCfgManager.getServer(serverId);
			if (server instanceof JiraServerCfg && server.getServerType() == ServerType.JIRA_SERVER) {
				removeServer(serverId, recenltyViewedAffected(server));
			}
		}

		@Override
		public void serverRemoved(final ServerCfg oldServer) {
			if (oldServer instanceof JiraServerCfg && oldServer.getServerType() == ServerType.JIRA_SERVER) {
				removeServer(oldServer.getServerId(), recenltyViewedAffected(oldServer));
			}
		}

		@Override
		public void serverEnabled(final ServerId serverId) {
			ServerCfg server = projectCfgManager.getServer(serverId);
			addServer(server, recenltyViewedAffected(server));
		}

		@Override
		public void serverAdded(final ServerCfg newServer) {
			addServer(newServer, false);
		}

		private void addServer(final ServerCfg server, boolean refreshIssueList) {
			if (server instanceof JiraServerCfg && server.getServerType() == ServerType.JIRA_SERVER) {
				Task.Backgroundable task = new MetadataFetcherBackgroundableTask((JiraServerCfg) server, refreshIssueList);
				ProgressManager.getInstance().run(task);

			}
		}

		private void removeServer(final ServerId serverId, final boolean reloadIssueList) {
			jiraServerModel.clear(serverId);
			refreshFilterModel();
			jiraFilterListModel.fireServerRemoved();

			if (reloadIssueList) {
				refreshRecenltyOpenIssues(true);
			}
		}

		private boolean recenltyViewedAffected(final ServerCfg server) {
			if (server instanceof JiraServerCfg && server.getServerType() == ServerType.JIRA_SERVER
					&& jiraFilterTree.isRecentlyOpenSelected()) {
				// check if some recenlty open issue come from enabled server; if yes then return true
				JiraWorkspaceConfiguration conf = IdeaHelper.getProjectComponent(project, JiraWorkspaceConfiguration.class);
				if (conf != null) {
					final Collection<IssueRecentlyOpenBean> recentlyOpen = conf.getRecentlyOpenIssuess();
					if (recentlyOpen != null) {
						for (IssueRecentlyOpenBean i : recentlyOpen) {
							if (i.getServerId().equals(server.getServerId())) {
								return true;
							}
						}
					}
				}
			}
			return false;
		}

	}

	@Override
	public JTree createRightTree() {
		JiraIssueListTree issueTree = new JiraIssueListTree();

		new TreeSpeedSearch(issueTree) {
			@Override
			protected boolean isMatchingElement(Object o, String s) {
				TreePath tp = (TreePath) o;
				Object node = tp.getLastPathComponent();
				if (node instanceof JIRAIssueTreeNode) {
					JIRAIssueTreeNode jitn = (JIRAIssueTreeNode) node;
					JIRAIssue issue = jitn.getIssue();
					return issue.getKey().toLowerCase().contains(s.toLowerCase())
							|| issue.getSummary().toLowerCase().contains(s.toLowerCase());
				} else {
					return super.isMatchingElement(o, s);
				}
			}
		};

		issueTreeBuilder.rebuild(issueTree, getRightPanel());
		return issueTree;
	}

	@Override
	public JiraIssueListTree getRightTree() {
		return (JiraIssueListTree) super.getRightTree();
	}

	@Override
	public JTree createLeftTree() {
		if (jiraFilterTree == null) {
			jiraFilterTree = new JIRAFilterTree(jiraWorkspaceConfiguration, getJIRAFilterListModel());
		}

		return jiraFilterTree;
	}

	@Override
	public String getActionPlaceName() {
		return PLACE_PREFIX + this.getProject().getName();
	}

	private class LocalJiraFilterTreeSelectionListener implements JiraFilterTreeSelectionListener {

		public void selectedSavedFilterNode(final JIRASavedFilter savedFilter, final ServerData jiraServerCfg) {
			hideManualFilterPanel();
			refreshIssues(savedFilter, jiraServerCfg, true);
			jiraWorkspaceConfiguration.getView().setViewServerIdd((ServerIdImpl) jiraServerCfg.getServerId());
			jiraWorkspaceConfiguration.getView().setViewFilterId(Long.toString(savedFilter.getId()));
		}

		public void selectedManualFilterNode(final JIRAManualFilter manualFilter, final ServerData jiraServerCfg) {
			showManualFilterPanel(manualFilter, jiraServerCfg);
			jiraWorkspaceConfiguration.getView().setViewServerIdd((ServerIdImpl) jiraServerCfg.getServerId());
			jiraWorkspaceConfiguration.getView().setViewFilterId(JiraFilterConfigurationBean.MANUAL_FILTER);

			refreshIssues(manualFilter, jiraServerCfg, true);
		}

		public void selectionCleared() {
			hideManualFilterPanel();

			enableGetMoreIssues(false);

			jiraWorkspaceConfiguration.getView().setViewServerIdd(null);
			jiraWorkspaceConfiguration.getView().setViewFilterId("");

			jiraIssueListModelBuilder.reset();
		}

		public void selectedRecentlyOpenNode() {
			hideManualFilterPanel();

			// refresh issues view
			refreshRecenltyOpenIssues(true);

			jiraWorkspaceConfiguration.getView().setViewServerIdd(null);
			jiraWorkspaceConfiguration.getView().setViewFilterId(JiraFilterConfigurationBean.RECENTLY_OPEN_FILTER);
		}
	}

	private class LogWorkWorkerTask extends Task.Backgroundable {
		private final JIRAIssue issue;
		private final WorkLogCreateAndMaybeDeactivateDialog dialog;
		private final ServerData jiraServer;
		private final boolean deactivateIssue;
		private DeactivateIssueResultHandler resultHandler;
		private boolean commitSuccess;

		public LogWorkWorkerTask(JIRAIssue issue, WorkLogCreateAndMaybeDeactivateDialog dialog,
				ServerData jiraServer, boolean deactivateIssue,
				DeactivateIssueResultHandler resultHandler) {

			super(IssueListToolWindowPanel.this.getProject(),
					deactivateIssue ? "Stopping Work" : "Logging Work", false);

			this.issue = issue;
			this.dialog = dialog;
			this.jiraServer = jiraServer;
			this.deactivateIssue = deactivateIssue;
			this.resultHandler = resultHandler;
		}

		@Override
		public void run(@NotNull final ProgressIndicator indicator) {
			try {
				if (jiraServer != null) {
					if (!deactivateIssue) {
						logWork();
					} else {

						// 1.
						commitChanges();

						if (commitSuccess) {
							try {
								// 2.
								logWork();

								// 3.

								// I should use a temporary variable to make the code more readable,
								// but the code is now indented funny in a weird way, so I am leaving
								// it as is so that the future generations have some fun looking at it too :P
								if (!runWorkflowAction(new DeactivateIssueResultHandler() {
									public void success() {
										if (resultHandler != null) {
											resultHandler.success();
										}
									}

									public void failure(Throwable problem) {
										if (resultHandler != null) {
											resultHandler.failure(problem);
										}
									}
								})) {
									// workflow action not selected, need to call result handler manually
									if (resultHandler != null) {
										resultHandler.success();
									}
								}

								// 4.
								setStatusInfoMessage("Deactivated issue " + issue.getKey());
//								jiraIssueListModelBuilder.reloadIssue(issue.getKey(), jiraServer);
							} catch (JIRAException e) {
								if (resultHandler != null) {
									resultHandler.failure(e);
								}
								throw e;
							}
						} else if (resultHandler != null) {
							resultHandler.failure(new JIRAException("Failed to commit changes"));
						}
					}
				}
			} catch (JIRAException e) {
				if (deactivateIssue) {
					setStatusErrorMessage("Issue not deactivated: " + e.getMessage(), e);
				} else {
					setStatusErrorMessage("Work not logged: " + e.getMessage(), e);
				}
			}
		}

		private void commitChanges() {
			if (dialog.isCommitChanges()) {
				final ChangeListManager changeListManager = ChangeListManager.getInstance(project);
				final LocalChangeList list = dialog.getCurrentChangeList();
				list.setComment(dialog.getComment());

				ApplicationManager.getApplication().invokeAndWait(new Runnable() {
					public void run() {
						setStatusInfoMessage("Committing changes...");
						FileDocumentManager.getInstance().saveAllDocuments();
						List<Change> selectedChanges = dialog.getSelectedChanges();
						commitSuccess = changeListManager.commitChangesSynchronouslyWithResult(
								list, selectedChanges);
					}
				}, ModalityState.defaultModalityState());

				if (commitSuccess) {
					WorkLogCreateAndMaybeDeactivateDialog.AfterCommit afterCommit =
							dialog.getAfterCommitChangeSetAction();

					switch (afterCommit) {
						case DEACTIVATE_CHANGESET:

							activateDefaultChangeList(changeListManager);
							break;
						case REMOVE_CHANGESET:
							activateDefaultChangeList(changeListManager);
							if (!"default".equalsIgnoreCase(dialog.getCurrentChangeList().getName())) {
								// PL-1612 - belt and suspenders probably, but just to be sure
								try {
									changeListManager.removeChangeList(dialog.getCurrentChangeList());
								} catch (Exception e) {
									// stupid IDEA 7 API. I hate you
									if (e instanceof IncorrectOperationException) {
										LoggerImpl.getInstance().warn(e);
									} else {
										throw new RuntimeException(e);
									}
								}
							}
							break;
						default:
							break;
					}
					setStatusInfoMessage("Deactivated issue " + issue.getKey());
				} else {
					setStatusErrorMessage(
							"Failed to commit change list while deactivating issue " + issue.getKey());
				}
			} else {
				// oh well, not having to commit is also a sort of success :) - yeah, I know, I suck
				commitSuccess = true;
			}
		}

		private boolean runWorkflowAction(final DeactivateIssueResultHandler handler) {
			JIRAAction selectedAction = dialog.getSelectedAction();
			if (selectedAction != null) {
				setStatusInfoMessage("Running action [" + selectedAction.getName()
						+ "] on issue " + issue.getKey());
				final RunIssueActionAction riaa = new RunIssueActionAction(IssueListToolWindowPanel.this,
						jiraServerFacade, issue, selectedAction, jiraIssueListModelBuilder);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						riaa.runIssueAction(project, handler);
					}
				});
				return true;
			}
			return false;
		}

		private void logWork() throws JIRAException {
			if (dialog.isLogTime()) {
				setStatusInfoMessage("Logging work for issue " + issue.getKey() + "...");
				Calendar cal = Calendar.getInstance();
				cal.setTime(dialog.getStartDate());

				String newRemainingEstimate = dialog.getRemainingEstimateUpdateMode()
						.equals(RemainingEstimateUpdateMode.MANUAL)
						? dialog.getRemainingEstimateString() : null;
				jiraServerFacade.logWork(jiraServer, issue, dialog.getTimeSpentString(),
						cal, null,
						!dialog.getRemainingEstimateUpdateMode()
								.equals(RemainingEstimateUpdateMode.UNCHANGED),
						newRemainingEstimate);
				JIRAIssueProgressTimestampCache.getInstance().setTimestamp(
						jiraServer, issue);
				setStatusInfoMessage("Logged work for issue " + issue.getKey());
			}
		}

		private void activateDefaultChangeList(ChangeListManager changeListManager) {
			List<LocalChangeList> chLists = changeListManager.getChangeLists();
			for (LocalChangeList chl : chLists) {
				if ("default".equalsIgnoreCase(chl.getName())) {
					changeListManager.setDefaultChangeList(chl);
					break;
				}
			}
		}
	}

	private class LocalJiraIssueListModelListener implements JIRAIssueListModelListener {
		private boolean singleIssueChanged = false;

		public void issueUpdated(final JIRAIssue issue) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					singleIssueChanged = true;
					JiraIssueAdapter.clearCache(issue);
					ActiveIssueUtils.checkIssueState(project, issue);
				}
			});
		}

		public void modelChanged(JIRAIssueListModel model) {
			SwingUtilities.invokeLater(new ModelChangedRunnable());

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (!singleIssueChanged) {
						jiraIssueListModelBuilder.checkActiveIssue(currentIssueListModel.getIssues());
					}
					singleIssueChanged = false;
				}
			});
		}

		public void issuesLoaded(JIRAIssueListModel model, int loadedIssues) {
			if (loadedIssues >= pluginConfiguration.getJIRAConfigurationData().getPageSize()) {
				enableGetMoreIssues(true);
				setStatusInfoMessage("Loaded " + loadedIssues + " issues", true);
			} else {
				enableGetMoreIssues(false);
				setStatusInfoMessage("Loaded " + loadedIssues + " issues");
			}
		}

		private class ModelChangedRunnable implements Runnable {
			public void run() {

				// kalamon: not sure how this is possible given that both are @NotNull annotated, but see PL-1540
				if (jiraServerModel == null || projectCfgManager == null) {
					return;
				}

				JiraIssueAdapter.clearCache();
				ServerData srvcfg = getSelectedServer();
				if (srvcfg == null && !isRecentlyOpenFilterSelected()) {
					setStatusInfoMessage("Nothing selected");
					issueTreeBuilder.rebuild(getRightTree(), getRightScrollPane());
				} else if (srvcfg == null && isRecentlyOpenFilterSelected()) {
					Map<Pair<String, ServerId>, String> projects = new HashMap<Pair<String, ServerId>, String>();
					for (JiraServerCfg server : projectCfgManager.getAllEnabledJiraServers()) {
						try {
							for (JIRAProject p : jiraServerModel.getProjects(projectCfgManager.getServerData(server))) {
								projects.put(new Pair<String, ServerId>(p.getKey(), server.getServerId()), p.getName());
							}
						} catch (JIRAException e) {
							setStatusErrorMessage("Cannot retrieve projects from server [" + server.getName() + "]. "
									+ e.getMessage(), e);
						}
					}
					issueTreeBuilder.setProjectKeysToNames(projects);
					issueTreeBuilder.rebuild(getRightTree(), getRightScrollPane());
				} else if (srvcfg != null) {
					Map<Pair<String, ServerId>, String> projectMap = new HashMap<Pair<String, ServerId>, String>();
					try {
						for (JIRAProject p : jiraServerModel.getProjects(srvcfg)) {
							projectMap.put(new Pair<String, ServerId>(p.getKey(), srvcfg.getServerId()),
									p.getName());
						}
					} catch (JIRAException e) {
						setStatusErrorMessage("Cannot retrieve projects from server [" + srvcfg.getName() + "]. "
								+ e.getMessage(), e);
					}
					issueTreeBuilder.setProjectKeysToNames(projectMap);
					issueTreeBuilder.rebuild(getRightTree(), getRightScrollPane());
//					expandAllRightTreeNodes();
				}
			}
		}
	}
}
