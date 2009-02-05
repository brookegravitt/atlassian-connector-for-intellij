package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.UiTaskExecutor;
import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.configuration.JiraFilterConfigurationBean;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.PluginToolWindowPanel;
import com.atlassian.theplugin.idea.action.issues.RunIssueActionAction;
import com.atlassian.theplugin.idea.config.ProjectCfgManager;
import com.atlassian.theplugin.idea.jira.tree.JIRAFilterTree;
import com.atlassian.theplugin.idea.jira.tree.JIRAIssueTreeBuilder;
import com.atlassian.theplugin.idea.jira.tree.JiraFilterTreeSelectionListener;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.idea.ui.PopupAwareMouseAdapter;
import com.atlassian.theplugin.jira.JIRAIssueProgressTimestampCache;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.jira.model.*;
import com.atlassian.theplugin.remoteapi.MissingPasswordHandlerJIRA;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.TreeSpeedSearch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class IssuesToolWindowPanel extends PluginToolWindowPanel implements DataProvider {

	public static final String PLACE_PREFIX = IssuesToolWindowPanel.class.getSimpleName();
	private final PluginConfiguration pluginConfiguration;
	private JiraWorkspaceConfiguration jiraProjectCfg;
	private final UiTaskExecutor uiTaskExecutor;

	private static final String SERVERS_TOOL_BAR = "ThePlugin.JiraServers.ServersToolBar";
	private JIRAFilterListModel jiraFilterListModel;
	private JIRAIssueTreeBuilder issueTreeBuilder;
	private JIRAIssueListModelBuilder jiraIssueListModelBuilder;
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
	private JTree issueTree;
	private JIRAIssueListModel baseIssueListModel;
	private Timer timer;

	private static final int ONE_SECOND = 1000;

	public IssuesToolWindowPanel(@NotNull final Project project,
			@NotNull final PluginConfiguration pluginConfiguration,
			@NotNull final JiraWorkspaceConfiguration jiraProjectConfiguration,
			@NotNull final IssueToolWindowFreezeSynchronizator freezeSynchronizator,
			@NotNull final UiTaskExecutor uiTaskExecutor) {
		super(project, SERVERS_TOOL_BAR, THE_PLUGIN_JIRA_ISSUES_ISSUES_TOOL_BAR);

		this.pluginConfiguration = pluginConfiguration;
		this.jiraProjectCfg = jiraProjectConfiguration;
		this.uiTaskExecutor = uiTaskExecutor;

		jiraServerFacade = JIRAServerFacadeImpl.getInstance();

		if (jiraProjectConfiguration.getView() != null && jiraProjectConfiguration.getView().getGroupBy() != null) {
			groupBy = jiraProjectConfiguration.getView().getGroupBy();
			groupSubtasksUnderParent = jiraProjectConfiguration.getView().isCollapseSubtasksUnderParent();
		} else {
			groupBy = JiraIssueGroupBy.TYPE;
			groupSubtasksUnderParent = false;
		}
		jiraFilterListModel = getJIRAFilterListModel();
		baseIssueListModel = JIRAIssueListModelImpl.createInstance();
		JIRAIssueListModel sortingIssueListModel = new SortingByPriorityJIRAIssueListModel(baseIssueListModel);
		searchingIssueListModel = new SearchingJIRAIssueListModel(sortingIssueListModel);
		currentIssueListModel = searchingIssueListModel;

		jiraIssueListModelBuilder = IdeaHelper.getProjectComponent(project, JIRAIssueListModelBuilderImpl.class);
		issueTreeBuilder = new JIRAIssueTreeBuilder(getGroupBy(), groupSubtasksUnderParent, currentIssueListModel);

		jiraServerModel = IdeaHelper.getProjectComponent(project, JIRAServerModelImpl.class);

		jiraIssueListModelBuilder.setModel(baseIssueListModel);
		jiraFilterListModelBuilder = IdeaHelper.getProjectComponent(project, JIRAFilterListBuilder.class);
		if (jiraFilterListModelBuilder != null) {
			jiraFilterListModelBuilder.setListModel(jiraFilterListModel);
			jiraFilterListModelBuilder.setProjectId(CfgUtil.getProjectId(project));
			jiraFilterListModelBuilder.setJiraWorkspaceCfg(jiraProjectConfiguration);
		}
		currentIssueListModel.addModelListener(new JIRAIssueListModelListener() {
			public void modelChanged(JIRAIssueListModel model) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JiraIssueAdapter.clearCache();
						JiraServerCfg srvcfg = jiraFilterTree.getSelectedServer();
						if (srvcfg == null) {
//							setStatusMessage("Server not defined", true);
							setStatusMessage("Nothing selected", false, false);
							issueTreeBuilder.rebuild(getRightTree(), getRightScrollPane());
							return;
						}
						Map<String, String> projectMap = new HashMap<String, String>();
						for (JIRAProject p : jiraServerModel.getProjects(srvcfg)) {
							projectMap.put(p.getKey(), p.getName());
						}
						issueTreeBuilder.setProjectKeysToNames(projectMap);
						issueTreeBuilder.rebuild(getRightTree(), getRightScrollPane());
						expandAllRightTreeNodes();
						setStatusMessage("Loaded " + currentIssueListModel.getIssues().size() + " issues", false, true);
					}
				});
			}

			public void issuesLoaded(JIRAIssueListModel model, int loadedIssues) {
				if (loadedIssues >= pluginConfiguration.getJIRAConfigurationData().getPageSize()) {
					enableGetMoreIssues(true);
				} else {
					enableGetMoreIssues(false);
				}
			}
		});

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

		manualFilterEditDetailsPanel = new JiraManualFilterDetailsPanel(jiraFilterListModel, jiraProjectCfg,
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

		jiraFilterTree.addSelectionListener(new JiraFilterTreeSelectionListener() {

			public void selectedSavedFilterNode(final JIRASavedFilter savedFilter, final JiraServerCfg jiraServerCfg) {
				hideManualFilterPanel();
				refreshIssues(savedFilter, jiraServerCfg, true);
				jiraProjectConfiguration.getView().setViewServerId(jiraServerCfg.getServerId().toString());
				jiraProjectConfiguration.getView().setViewFilterId(Long.toString(savedFilter.getId()));
			}

			public void selectedManualFilterNode(final JIRAManualFilter manualFilter, final JiraServerCfg jiraServerCfg) {
				showManualFilterPanel(manualFilter, jiraServerCfg);
				jiraProjectConfiguration.getView().setViewServerId(jiraServerCfg.getServerId().toString());
				jiraProjectConfiguration.getView().setViewFilterId(JiraFilterConfigurationBean.MANUAL_FILTER_LABEL);

				refreshIssues(manualFilter, jiraServerCfg, true);
			}

			public void selectionCleared() {
				hideManualFilterPanel();

				enableGetMoreIssues(false);

				jiraProjectConfiguration.getView().setViewServerId("");
				jiraProjectConfiguration.getView().setViewFilterId("");

				jiraIssueListModelBuilder.reset();
			}
		});

	}

	protected void showManualFilterPanel(final JIRAManualFilter manualFilter, final JiraServerCfg jiraServerCfg) {
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


	private void addIssuesTreeListeners() {
		getRightTree().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				JIRAIssue issue = currentIssueListModel.getSelectedIssue();
				if (e.getKeyCode() == KeyEvent.VK_ENTER && issue != null) {
					openIssue(issue);
				}
			}
		});

		getRightTree().addMouseListener(new PopupAwareMouseAdapter() {

			@Override
			public void mouseClicked(final MouseEvent e) {
				final JIRAIssue issue = currentIssueListModel.getSelectedIssue();
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 && issue != null) {
					openIssue(issue);
				}
			}

			@Override
			protected void onPopup(MouseEvent e) {
				int selRow = getRightTree().getRowForLocation(e.getX(), e.getY());
				TreePath selPath = getRightTree().getPathForLocation(e.getX(), e.getY());
				if (selRow != -1 && selPath != null) {
					getRightTree().setSelectionPath(selPath);
					if (currentIssueListModel.getSelectedIssue() != null) {
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
					event.getPresentation().setText("Available Workflow Actions");
				}
			}
		};
		actionGroup.add(submenu);

		final JIRAIssue issue = currentIssueListModel.getSelectedIssue();
		List<JIRAAction> actions = JiraIssueAdapter.get(issue).getCachedActions();
		if (actions != null) {
			for (JIRAAction a : actions) {
				submenu.add(new RunIssueActionAction(this, jiraServerFacade, issue, a));
			}
		} else {
			Thread t = new Thread() {
				@Override
				public void run() {
					try {
						JiraServerCfg jiraServer = jiraFilterTree.getSelectedServer();

						if (jiraServer != null) {
							final List<JIRAAction> actions = jiraServerFacade.getAvailableActions(jiraServer, issue);

							JiraIssueAdapter.get(issue).setCachedActions(actions);
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									JPopupMenu pMenu = popup.getComponent();
									if (pMenu.isVisible()) {
										for (JIRAAction a : actions) {
											submenu.add(new RunIssueActionAction(IssuesToolWindowPanel.this,
													jiraServerFacade, issue, a));
										}

										// magic that makes the popup update itself. Don't ask - it is some sort of voodoo
										pMenu.setVisible(false);
										pMenu.setVisible(true);
									}
								}
							});
						}
					} catch (JIRAException e) {
						setStatusMessage("Query for issue " + issue.getKey() + " actions failed: " + e.getMessage(), true);
					} catch (NullPointerException e) {
						// somebody unselected issue in the table, so let's just skip
					}
				}
			};
			t.start();
		}
	}


	public void openIssue(@NotNull JIRAIssue issue) {
		IdeaHelper.getIssueToolWindow(getProject())
				.showIssue(jiraFilterTree.getSelectedServer(), issue, baseIssueListModel);
	}

	public void openIssue(@NotNull final String issueKey) {
		JIRAIssue issue = null;
		for (JIRAIssue i : baseIssueListModel.getIssues()) {
			if (i.getKey().equals(issueKey)) {
				issue = i;
				break;
			}
		}

		if (issue != null) {
			openIssue(issue);
		} else {
			Task.Backgroundable task = new Task.Backgroundable(getProject(), "Fetching JIRA issue " + issueKey, false) {
				private JIRAIssue issue;
				private Throwable exception;

				@Override
				public void onSuccess() {
					JiraServerCfg server = jiraFilterTree.getSelectedServer();
					if (getProject().isDisposed()) {
						return;
					}
					if (exception != null) {
						final String serverName = server != null ? server.getName() : "[UNDEFINED!]";
						DialogWithDetails.showExceptionDialog(getProject(),
								"Cannot fetch issue " + issueKey + " from server " + serverName, exception, "Error");
						return;
					}
					if (issue != null) {
						IdeaHelper.getIssueToolWindow(getProject()).showIssue(
								server, issue, baseIssueListModel);
					}
				}

				@Override
				public void run(@NotNull ProgressIndicator progressIndicator) {
					progressIndicator.setIndeterminate(true);
					try {
						final JiraServerCfg jiraServer = jiraFilterTree.getSelectedServer();
						if (jiraServer != null) {
							issue = jiraServerFacade.getIssue(jiraServer, issueKey);
							jiraIssueListModelBuilder.updateIssue(issue, jiraServer);
						} else {
							exception = new RuntimeException("No JIRA server defined!");
						}
					} catch (JIRAException e) {
						exception = e;
					}
				}
			};
			task.queue();
		}
	}

	public void viewIssueInBrowser() {
		JIRAIssue issue = currentIssueListModel.getSelectedIssue();
		if (issue != null) {
			BrowserUtil.launchBrowser(issue.getIssueUrl());
		}
	}

	public void editIssueInBrowser() {
		JIRAIssue issue = currentIssueListModel.getSelectedIssue();
		if (issue != null) {
			BrowserUtil.launchBrowser(issue.getServerUrl() + "/secure/EditIssue!default.jspa?key=" + issue.getKey());
		}
	}

	public void assignIssueToMyself(@NotNull final JIRAIssue issue) {
//		final JIRAIssue issue = currentIssueListModel.getSelectedIssue();
		if (issue == null) {
			return;
		}
		try {
			JiraServerCfg jiraServer = jiraFilterTree.getSelectedServer();
			if (jiraServer != null) {
				assignIssue(issue, jiraServer.getUsername());
			}
		} catch (NullPointerException ex) {
			// whatever, means action was called when no issue was selected. Let's just swallow it
		}
	}

	public void assignIssueToSomebody(@NotNull final JIRAIssue issue) {
//		final JIRAIssue issue = currentIssueListModel.getSelectedIssue();
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
				setStatusMessage("Assigning issue " + issue.getKey() + " to " + assignee + "...");
				try {

					JiraServerCfg jiraServer = jiraFilterTree.getSelectedServer();
					if (jiraServer != null) {
						jiraServerFacade.setAssignee(jiraServer, issue, assignee);
						setStatusMessage("Assigned issue " + issue.getKey() + " to " + assignee);
						jiraIssueListModelBuilder.updateIssue(issue, jiraServer);
					}
				} catch (JIRAException e) {
					setStatusMessage("Failed to assign issue " + issue.getKey() + ": " + e.getMessage(), true);
				}
			}
		};

		ProgressManager.getInstance().run(assign);
	}

	public void createChangeListAction(@NotNull final JIRAIssue issue) {
		String changeListName = issue.getKey() + " - " + issue.getSummary();
		final ChangeListManager changeListManager = ChangeListManager.getInstance(getProject());

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
		final JIRAIssue issue = currentIssueListModel.getSelectedIssue();
		final IssueCommentDialog issueCommentDialog = new IssueCommentDialog(issue.getKey());
		issueCommentDialog.show();
		if (issueCommentDialog.isOK()) {
			Task.Backgroundable comment = new Task.Backgroundable(getProject(), "Commenting Issue", false) {
				@Override
				public void run(@NotNull final ProgressIndicator indicator) {
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							setStatusMessage("Commenting issue " + issue.getKey() + "...");
						}
					});
					try {
						JiraServerCfg jiraServer = jiraFilterTree.getSelectedServer();
						if (jiraServer != null) {
							jiraServerFacade.addComment(jiraServer, issue, issueCommentDialog.getComment());
							EventQueue.invokeLater(new Runnable() {
								public void run() {
									setStatusMessage("Commented issue " + issue.getKey());
								}
							});
						}
					} catch (final JIRAException e) {
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								setStatusMessage("Issue not commented: " + e.getMessage(), true);
							}
						});
					}
				}
			};

			ProgressManager.getInstance().run(comment);
		}
	}

	public void logWorkForIssue(final JIRAIssue issue) {
		final JiraServerCfg jiraServer = jiraFilterTree.getSelectedServer();
		final WorkLogCreate workLogCreate = new WorkLogCreate(jiraServer, jiraServerFacade, issue, getProject());
		workLogCreate.show();
		if (workLogCreate.isOK()) {

			Task.Backgroundable logWork = new Task.Backgroundable(getProject(), "Logging Work", false) {
				@Override
				public void run(@NotNull final ProgressIndicator indicator) {
					setStatusMessage("Logging work for issue " + issue.getKey() + "...");
					try {
						Calendar cal = Calendar.getInstance();
						cal.setTime(workLogCreate.getStartDate());


						if (jiraServer != null) {
							String newRemainingEstimate = workLogCreate.getUpdateRemainingManually()
									? workLogCreate.getRemainingEstimateString() : null;
							jiraServerFacade.logWork(jiraServer, issue, workLogCreate.getTimeSpentString(),
									cal, workLogCreate.getComment(),
									!workLogCreate.getLeaveRemainingUnchanged(), newRemainingEstimate);
							JIRAIssueProgressTimestampCache.getInstance().setTimestamp(jiraServer, issue);
							if (workLogCreate.isStopProgressSelected()) {
								setStatusMessage("Stopping work for issue " + issue.getKey() + "...");
								jiraServerFacade.progressWorkflowAction(jiraServer, issue,
										workLogCreate.getStopProgressAction());
								setStatusMessage("Work logged and progress stopped for issue " + issue.getKey());
								jiraIssueListModelBuilder.updateIssue(issue, jiraServer);
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

	public void startWorkingOnIssue(@NotNull final JIRAIssue issue) {
//		final JIRAIssue issue = currentIssueListModel.getSelectedIssue();
		if (issue == null) {
			return;
		}
		createChangeListAction(issue);
		final JiraServerCfg server = jiraFilterTree.getSelectedServer();

		Task.Backgroundable startWorkOnIssue = new Task.Backgroundable(getProject(), "Starting Work on Issue", false) {

			@Override
			public void run(@NotNull final ProgressIndicator indicator) {
				JIRAIssue myIssue = currentIssueListModel.getSelectedIssue();
				setStatusMessage("Assigning issue " + myIssue.getKey() + " to me...");
				try {
					jiraServerFacade.setAssignee(server, myIssue, server.getUsername());
					List<JIRAAction> actions = jiraServerFacade.getAvailableActions(server, myIssue);
					boolean found = false;
					for (JIRAAction a : actions) {
						if (a.getId() == Constants.JiraActionId.START_PROGRESS.getId()) {
							setStatusMessage("Starting progress on " + myIssue.getKey() + "...");
							jiraServerFacade.progressWorkflowAction(server, myIssue, a);
							JIRAIssueProgressTimestampCache.getInstance().setTimestamp(server, myIssue);
							setStatusMessage("Started progress on " + myIssue.getKey());
							found = true;
							jiraIssueListModelBuilder.updateIssue(myIssue, server);
							break;
						}
					}
					if (!found) {
						setStatusMessage("Progress on "
								+ myIssue.getKey()
								+ "  not started - no such workflow action available");
					}
				} catch (JIRAException e) {
					setStatusMessage("Error starting progress on issue: " + e.getMessage(), true);
				} catch (NullPointerException e) {
					// eeeem - now what?
				}
			}
		};

		ProgressManager.getInstance().run(startWorkOnIssue);
	}


	private void refreshFilterModel() {
		try {
			jiraFilterListModelBuilder.rebuildModel();
		} catch (JIRAFilterListBuilder.JIRAServerFiltersBuilderException e) {
			//@todo show in message editPane
			setStatusMessage("Some Jira servers did not return saved filters", true);
		}
	}

//	public void setIssuesFilterParams(JiraServerCfg server, List<JIRAQueryFragment> manualFilter) {
//		jiraIssueListModelBuilder.setServer(server);
//		jiraIssueListModelBuilder.setCustomFilter(manualFilter);
//	}
//
//	public void setIssuesFilterParams(JiraServerCfg server, JIRASavedFilter savedFilter) {
//		jiraIssueListModelBuilder.setServer(server);
//		jiraIssueListModelBuilder.setSavedFilter(savedFilter);
//	}

	public void refreshIssues(final boolean reload) {
		JIRAManualFilter manualFilter = jiraFilterTree.getSelectedManualFilter();
		JIRASavedFilter savedFilter = jiraFilterTree.getSelectedSavedFilter();
		JiraServerCfg serverCfg = jiraFilterTree.getSelectedServer();
		if (savedFilter != null) {
			refreshIssues(savedFilter, serverCfg, reload);
		} else if (manualFilter != null) {
			refreshIssues(manualFilter, serverCfg, reload);
		}
	}

	private void refreshIssues(final JIRAManualFilter manualFilter, final JiraServerCfg jiraServerCfg, final boolean reload) {
		if (WindowManager.getInstance().getIdeFrame(getProject()) == null) {
			return;
		}
		Task.Backgroundable task = new Task.Backgroundable(getProject(), "Retrieving issues", false) {
			@Override
			public void run(@NotNull final ProgressIndicator indicator) {
				try {
					getStatusBarPane().setMessage("Loading issues...", false);
					jiraIssueListModelBuilder.addIssuesToModel(manualFilter, jiraServerCfg,
							pluginConfiguration.getJIRAConfigurationData().getPageSize(), reload);
				} catch (JIRAException e) {
					setStatusMessage(e.getMessage(), true);
				}
			}
		};
		ProgressManager.getInstance().run(task);
	}

	private void refreshIssues(final JIRASavedFilter savedFilter, final JiraServerCfg jiraServerCfg, final boolean reload) {
		if (WindowManager.getInstance().getIdeFrame(getProject()) == null) {
			return;
		}
		Task.Backgroundable task = new Task.Backgroundable(getProject(), "Retrieving issues", false) {
			@Override
			public void run(@NotNull final ProgressIndicator indicator) {
				try {
					getStatusBarPane().setMessage("Loading issues...", false);
					jiraIssueListModelBuilder.addIssuesToModel(savedFilter, jiraServerCfg,
							pluginConfiguration.getJIRAConfigurationData().getPageSize(), reload);
				} catch (JIRAException e) {
					setStatusMessage(e.getMessage(), true);
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

	public void projectUnregistered() {
	}


	public JiraIssueGroupBy getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(JiraIssueGroupBy groupBy) {
		this.groupBy = groupBy;
		issueTreeBuilder.setGroupBy(groupBy);
		issueTreeBuilder.rebuild(getRightTree(), getRightPanel());
		expandAllRightTreeNodes();

		// store in project workspace
		jiraProjectCfg.getView().setGroupBy(groupBy);
	}

	public void createIssue() {
		JIRAIssueListModelBuilder builder = IdeaHelper.getProjectComponent(getProject(), JIRAIssueListModelBuilderImpl.class);
		if (builder == null) {
			return;
		}

		final JiraServerCfg server = jiraFilterTree.getSelectedServer();

		if (server != null) {
			final IssueCreateDialog issueCreateDialog = new IssueCreateDialog(jiraServerModel, server,
					jiraProjectCfg, uiTaskExecutor);

			issueCreateDialog.initData();
			issueCreateDialog.show();
			if (issueCreateDialog.isOK()) {

				Task.Backgroundable createTask = new Task.Backgroundable(getProject(), "Creating Issue", false) {
					@Override
					public void run(@NotNull final ProgressIndicator indicator) {
						setStatusMessage("Creating new issue...");
						String message;
						boolean isError = false;
						try {
							JIRAIssue issueToCreate = issueCreateDialog.getJIRAIssue();
							JIRAIssue createdIssue = jiraServerFacade.createIssue(server, issueToCreate);

							message = "New issue created: <a href="
									+ createdIssue.getIssueUrl()
									+ ">"
									+ createdIssue.getKey()
									+ "</a>";

							jiraIssueListModelBuilder.updateIssue(createdIssue, server);
						} catch (JIRAException e) {
							message = "Failed to create new issue: " + e.getMessage();
							isError = true;
						}

						final String msg = message;
						setStatusMessage(msg, isError);
					}
				};

				ProgressManager.getInstance().run(createTask);
			}
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
			expandAllRightTreeNodes();
			jiraProjectCfg.getView().setCollapseSubtasksUnderParent(groupSubtasksUnderParent);
		}
	}

	public JIRAFilterListModel getJIRAFilterListModel() {
		if (jiraFilterListModel == null) {
			jiraFilterListModel = new JIRAFilterListModel();
		}
		return jiraFilterListModel;
	}

	public JiraServerCfg getSelectedServer() {
		return jiraFilterTree.getSelectedServer();
	}


	private class MetadataFetcherBackgroundableTask extends Task.Backgroundable {
		public MetadataFetcherBackgroundableTask() {
			super(IssuesToolWindowPanel.this.getProject(), "Retrieving JIRA information", false);
		}

		@Override
		public void run(@NotNull final ProgressIndicator indicator) {
			try {
				jiraServerModel.setModelFrozen(true);
				jiraServerModel.clearAll();

				for (JiraServerCfg server : IdeaHelper.getCfgManager()
						.getAllEnabledJiraServers(CfgUtil.getProjectId(getProject()))) {
					if (!jiraServerModel.checkServer(server)) {
						setStatusMessage("Unable to connect to server. " + jiraServerModel.getErrorMessage(server), true);
						EventQueue.invokeLater(new MissingPasswordHandlerJIRA(jiraServerFacade, server));
						continue;
					}//@todo remove  saved filters download or merge with existing in listModel
					final String serverStr = "[" + server.getName() + "] ";
					setStatusMessage(serverStr + "Retrieving saved filters...");
					jiraServerModel.getSavedFilters(server);
					setStatusMessage(serverStr + "Retrieving projects...");
					jiraServerModel.getProjects(server);
					setStatusMessage(serverStr + "Retrieving issue types...");
					jiraServerModel.getIssueTypes(server, null);
					setStatusMessage(serverStr + "Retrieving statuses...");
					jiraServerModel.getStatuses(server);
					setStatusMessage(serverStr + "Retrieving resolutions...");
					jiraServerModel.getResolutions(server);
					setStatusMessage(serverStr + "Retrieving priorities...");
					jiraServerModel.getPriorities(server);
					setStatusMessage(serverStr + "Retrieving projects...");
					jiraServerModel.getProjects(server);
					setStatusMessage(serverStr + "Server data query finished");
				}
			} finally {
				jiraServerModel.setModelFrozen(false);
			}
		}

		@Override
		public void onSuccess() {
			refreshFilterModel();
			jiraFilterListModel.fireModelChanged();
		}
	}

	@Nullable
	public Object getData(@NotNull final String dataId) {
		if (dataId.equals(Constants.ISSUE)) {
			return currentIssueListModel.getSelectedIssue();
		}
		return null;
	}

	private class LocalConfigurationListener extends ConfigurationListenerAdapter {
		@Override
		public void jiraServersChanged(ProjectConfiguration newConfiguration) {
			refreshModels();
		}

		//		@Override
//		public void serverDataChanged(ServerId serverId) {
//			ServerCfg server = cfgManager.getServer(CfgUtil.getProjectId(project), serverId);
//			if (server.getServerType() == ServerType.JIRA_SERVER) {
//				refreshModels();
//			}
//		}

	}

	@Override
	public JTree createRightTree() {
		if (issueTree == null) {
			issueTree = new JTree();
		}

		new TreeSpeedSearch(issueTree);

		issueTreeBuilder.rebuild(issueTree, getRightPanel());
		return issueTree;
	}

	@Override
	public JTree createLeftTree() {
		if (jiraFilterTree == null) {
			jiraFilterTree = new JIRAFilterTree(jiraProjectCfg, getJIRAFilterListModel());
		}

		return jiraFilterTree;
	}

	@Override
	public String getActionPlaceName() {
		return PLACE_PREFIX + this.getProject().getName();
	}

}
