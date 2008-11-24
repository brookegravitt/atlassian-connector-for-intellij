package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.configuration.JiraFilterConfigurationBean;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.action.issues.RunIssueActionAction;
import com.atlassian.theplugin.idea.jira.editor.vfs.JiraIssueVirtualFile;
import com.atlassian.theplugin.idea.jira.tree.JIRAFilterTree;
import com.atlassian.theplugin.idea.jira.tree.JIRAIssueTreeBuilder;
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
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.SearchTextField;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
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

public final class IssuesToolWindowPanel extends JPanel implements DataProvider {
	private static final Key<IssuesToolWindowPanel> WINDOW_PROJECT_KEY = Key.create(IssuesToolWindowPanel.class.getName());
	private static final float ISSUES_PANEL_SPLIT_RATIO = 0.3f;
	private static final float MANUAL_FILTER_PROPORTION_VISIBLE = 0.5f;
	private static final float MANUAL_FILTER_PROPORTION_HIDDEN = 0.9f;

	private Project project;
	private PluginConfigurationBean pluginConfiguration;
	private ProjectConfigurationBean projectConfigurationBean;
	private CfgManager cfgManager;
	private JPanel serversPanel = new JPanel(new BorderLayout());
	private JPanel issuesPanel = new JPanel(new BorderLayout());
	private final Splitter splitPane = new Splitter(true, ISSUES_PANEL_SPLIT_RATIO);
	private static final String SERVERS_TOOL_BAR = "ThePlugin.JiraServers.ServersToolBar";
	private JIRAFilterListModel jiraFilterListModel;
	private JIRAIssueTreeBuilder issueTreeBuilder;
	private JTree issueTree;
	private JIRAIssueListModelBuilder jiraIssueListModelBuilder;
	private JIRAFilterListBuilder jiraFilterListModelBuilder;
	private Splitter splitFilterPane;
	private JIRAIssueGroupBy groupBy;
	private static final int JIRA_ISSUE_PAGE_SIZE = 25;
	private JIRAManualFilterDetailsPanel manualFilterEditDetailsPanel;


	private JIRAServerFacade jiraServerFacade;
	private SearchTextField searchField = new SearchTextField();

	private JScrollPane issueTreescrollPane;
	private JIRAFilterTree serversTree;

	private MessageScrollPane messagePane;
	private JIRAIssueListModel currentIssueListModel;
	private SearchingJIRAIssueListModel searchingIssueListModel;

	private JIRAServerModel jiraServerModel;
	private ConfigurationListener configListener = new LocalConfigurationListener();
	private IssueToolWindowFreezeSynchronizator freezeSynchronizator;


	public IssuesToolWindowPanel(@NotNull final Project project, @NotNull final PluginConfigurationBean pluginConfiguration,
			@NotNull final ProjectConfigurationBean projectConfigurationBean, @NotNull final CfgManager cfgManager) {
		this.project = project;
		this.pluginConfiguration = pluginConfiguration;
		this.projectConfigurationBean = projectConfigurationBean;
		this.cfgManager = cfgManager;

		jiraServerFacade = JIRAServerFacadeImpl.getInstance();

		setLayout(new BorderLayout());
		this.messagePane = new MessageScrollPane("Issues panel");
		add(messagePane, BorderLayout.SOUTH);

		if (projectConfigurationBean.getJiraConfiguration() != null
				&& projectConfigurationBean.getJiraConfiguration().getView() != null
				&& projectConfigurationBean.getJiraConfiguration().getView().getGroupBy() != null) {
			groupBy = projectConfigurationBean.getJiraConfiguration().getView().getGroupBy();
		} else {
			groupBy = JIRAIssueGroupBy.TYPE;
		}
		jiraFilterListModel = new JIRAFilterListModel();
		JIRAIssueListModel baseIssueListModel = JIRAIssueListModelImpl.createInstance();
		JIRAIssueListModel sortingIssueListModel = new SortingByPriorityJIRAIssueListModel(baseIssueListModel);
		searchingIssueListModel = new SearchingJIRAIssueListModel(sortingIssueListModel);
		currentIssueListModel = searchingIssueListModel;

		jiraIssueListModelBuilder = IdeaHelper.getProjectComponent(project, JIRAIssueListModelBuilderImpl.class);
		issueTreeBuilder = new JIRAIssueTreeBuilder(getGroupBy(), currentIssueListModel);

		jiraServerModel = IdeaHelper.getProjectComponent(project, JIRAServerModelImpl.class);

		splitPane.setShowDividerControls(false);
		splitPane.setFirstComponent(createFilterContent());
		splitPane.setSecondComponent(createIssuesContent());
		splitPane.setHonorComponentsMinimumSize(true);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				final Dimension dimension = e.getComponent().getSize();
				final boolean doVertical = dimension.getWidth() < dimension.getHeight();
				if (doVertical != splitPane.getOrientation()) {
					splitPane.setOrientation(doVertical);
				}

			}
		});

		add(splitPane, BorderLayout.CENTER);

		jiraIssueListModelBuilder.setModel(baseIssueListModel);
		jiraFilterListModelBuilder = IdeaHelper.getProjectComponent(project, JIRAFilterListBuilder.class);
		if (jiraFilterListModelBuilder != null) {
			jiraFilterListModelBuilder.setListModel(jiraFilterListModel);
			jiraFilterListModelBuilder.setProjectId(CfgUtil.getProjectId(project));
			jiraFilterListModelBuilder.setProjectConfigurationBean(projectConfigurationBean);
		}
		currentIssueListModel.addModelListener(new JIRAIssueListModelListener() {
			public void modelChanged(JIRAIssueListModel model) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JiraIssueAdapter.clearCache();
						JiraServerCfg srvcfg = jiraIssueListModelBuilder.getServer();
						if (srvcfg == null) {
							messagePane.setMessage("Server not defined", true);
							return;
						}
						Map<String, String> projectMap = new HashMap<String, String>();
						for (JIRAProject p : jiraServerModel.getProjects(srvcfg)) {
							projectMap.put(p.getKey(), p.getName());
						}
						issueTreeBuilder.setProjectKeysToNames(projectMap);
						issueTreeBuilder.rebuild(issueTree, issueTreescrollPane);
						expandAllIssueTreeNodes();
						messagePane.setMessage("Loaded " + currentIssueListModel.getIssues().size() + " issues");
					}
				});
			}

			public void issuesLoaded(JIRAIssueListModel model, int loadedIssues) {
				if (loadedIssues >= JIRA_ISSUE_PAGE_SIZE) {
					messagePane.enableGetMoreIssues(true);
				} else {
					messagePane.enableGetMoreIssues(false);
				}
			}
		});

		currentIssueListModel.addFrozenModelListener(new FrozenModelListener(){

			public void modelFrozen(FrozenModel model, boolean frozen) {
				if (messagePane != null) {
					messagePane.setEnabled(!frozen);
				}

				if (searchField != null) {
					searchField.setEnabled(!frozen);
				}

			}
		});

		jiraFilterListModel.addModelListener(new JIRAFilterListModelListener() {
			public void modelChanged(JIRAFilterListModel listModel) {
			}

			public void selectedManualFilter(final JiraServerCfg jiraServer, final List<JIRAQueryFragment> manualFilter) {
				showManualFilterPanel(true);
				setIssuesFilterParams(jiraServer, manualFilter);
				refreshIssues();

				projectConfigurationBean.getJiraConfiguration().getView().setViewServerId(jiraServer.getServerId().toString());
				projectConfigurationBean.getJiraConfiguration().getView().setViewServerId(jiraServer.getServerId().toString());
				projectConfigurationBean.getJiraConfiguration().getView()
						.setViewFilterId(JiraFilterConfigurationBean.MANUAL_FILTER_LABEL);
			}

			public void modelFrozen(boolean frozen) {
			}

			public void selectedSavedFilter(final JiraServerCfg jiraServer, final JIRASavedFilter savedFilter) {
				showManualFilterPanel(false);
				setIssuesFilterParams(jiraServer, savedFilter);
				refreshIssues();

				projectConfigurationBean.getJiraConfiguration().getView().setViewServerId(jiraServer.getServerId().toString());
				projectConfigurationBean.getJiraConfiguration().getView().setViewFilterId(Long.toString(savedFilter.getId()));
			}
		});


		messagePane.addMoreIssuesListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent e) {
					getNextIssues();
				}
			});

		addIssuesTreeListeners();
		addSearchBoxListener();
		freezeSynchronizator = new IssueToolWindowFreezeSynchronizator(jiraFilterListModel, currentIssueListModel,
				jiraServerModel);


	}

	private void addSearchBoxListener() {
		searchField.addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				searchingIssueListModel.setSearchTerm(searchField.getText());
			}

			public void removeUpdate(DocumentEvent e) {
				searchingIssueListModel.setSearchTerm(searchField.getText());
			}

			public void changedUpdate(DocumentEvent e) {
				searchingIssueListModel.setSearchTerm(searchField.getText());
			}
		});
		searchField.addKeyboardListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					searchField.addCurrentTextToHistory();
				}
			}

			public void keyReleased(KeyEvent e) {
			}
		});
	}

	private void addIssuesTreeListeners() {
		issueTree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				JIRAIssue issue = currentIssueListModel.getSelectedIssue();
				if (e.getKeyCode() == KeyEvent.VK_ENTER && issue != null) {
					openIssue(issue);
				}
			}
		});

		issueTree.addMouseListener(new PopupAwareMouseAdapter() {

			@Override
			public void mouseClicked(final MouseEvent e) {
				final JIRAIssue issue = currentIssueListModel.getSelectedIssue();
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 && issue != null) {
					openIssue(issue);
				}
			}

			@Override
			protected void onPopup(MouseEvent e) {
				int selRow = issueTree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = issueTree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1 && selPath != null) {
					issueTree.setSelectionPath(selPath);
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
						JiraServerCfg jiraServer = jiraIssueListModelBuilder.getServer();

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


	public void openIssue(@Nullable JIRAIssue issue) {
		if (issue != null) {
			FileEditorManager manager = FileEditorManager.getInstance(project);
			VirtualFile vf = findOpenJiraIssues(issue.getKey(), manager);
			if (vf == null) {
				vf = new JiraIssueVirtualFile(issue);
			}
			// either opens a new editor, or focuses the already open one
			manager.openFile(vf, true);
		}
	}

	private JiraIssueVirtualFile findOpenJiraIssues(@NotNull String issueKey, @NotNull FileEditorManager manager) {
		VirtualFile[] files = manager.getOpenFiles();
		JiraIssueVirtualFile vf = null;
		for (VirtualFile f : files) {
			if (f instanceof JiraIssueVirtualFile) {
				JiraIssueVirtualFile jivf = (JiraIssueVirtualFile) f;
				if (jivf.getIssue().getKey().equals(issueKey)) {
					vf = jivf;
					break;
				}
			}
		}
		return vf;
	}


	public void openIssue(final String issueKey) {
		final FileEditorManager manager = FileEditorManager.getInstance(project);
		JiraIssueVirtualFile vf = findOpenJiraIssues(issueKey, manager);

		if (vf != null) {
			manager.openFile(vf, true);
			try {
				jiraIssueListModelBuilder.updateIssue(vf.getIssue());
			} catch (JIRAException e) {
				DialogWithDetails.showExceptionDialog(project, "Cannot open JIRA issue in internal editor ["
						+ issueKey + "]", e, "Error");
			}
		} else {
			Task.Backgroundable task = new Task.Backgroundable(project, "Fetching JIRA issue " + issueKey, false) {
				private JIRAIssue issue;
				private Throwable exception;

				@Override
				public void onSuccess() {
					if (project.isDisposed()) {
						return;
					}
					if (exception != null) {
						final String serverName = jiraIssueListModelBuilder.getServer() != null
								? jiraIssueListModelBuilder.getServer().getName()
								: "[UNDEFINED!]";
						DialogWithDetails.showExceptionDialog(project, "Cannot fetch issue " + issueKey + " from server "
								+ serverName, exception, "Error");
						return;
					}
					if (issue != null) {
						final JiraIssueVirtualFile issueVirtualFile = new JiraIssueVirtualFile(issue);
						manager.openFile(issueVirtualFile, true);
					}
				}

				@Override
				public void run(@NotNull ProgressIndicator progressIndicator) {
					progressIndicator.setIndeterminate(true);
					try {
						final JiraServerCfg jiraServer = jiraIssueListModelBuilder.getServer();
						if (jiraServer != null) {
							issue = jiraServerFacade.getIssue(jiraServer, issueKey);
							jiraIssueListModelBuilder.updateIssue(issue);
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

	public void assignIssueToMyself() {
		final JIRAIssue issue = currentIssueListModel.getSelectedIssue();
		if (issue == null) {
			return;
		}
		try {
			JiraServerCfg jiraServer = jiraIssueListModelBuilder.getServer();
			if (jiraServer != null) {
				assignIssue(issue, jiraServer.getUsername());
			}
		} catch (NullPointerException ex) {
			// whatever, means action was called when no issue was selected. Let's just swallow it
		}
	}

	public void assignIssueToSomebody() {
		final JIRAIssue issue = currentIssueListModel.getSelectedIssue();
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

		Task.Backgroundable assign = new Task.Backgroundable(project, "Assigning Issue", false) {

			@Override
			public void run(@NotNull final ProgressIndicator indicator) {
				setStatusMessage("Assigning issue " + issue.getKey() + " to " + assignee + "...");
				try {

					JiraServerCfg jiraServer = jiraIssueListModelBuilder.getServer();
					if (jiraServer != null) {
						jiraServerFacade.setAssignee(jiraServer, issue, assignee);
						setStatusMessage("Assigned issue " + issue.getKey() + " to " + assignee);
						jiraIssueListModelBuilder.updateIssue(issue);
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
		final JIRAIssue issue = currentIssueListModel.getSelectedIssue();
		final IssueComment issueComment = new IssueComment(issue.getKey());
		issueComment.show();
		if (issueComment.isOK()) {
			Task.Backgroundable comment = new Task.Backgroundable(project, "Commenting Issue", false) {
				@Override
				public void run(@NotNull final ProgressIndicator indicator) {
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							setStatusMessage("Commenting issue " + issue.getKey() + "...");
						}
					});
					try {
						JiraServerCfg jiraServer = jiraIssueListModelBuilder.getServer();
						if (jiraServer != null) {
							jiraServerFacade.addComment(jiraServer, issue, issueComment.getComment());
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

	public void logWorkForIssue() {
		final JIRAIssue issue = currentIssueListModel.getSelectedIssue();
		final JiraServerCfg jiraServer = jiraIssueListModelBuilder.getServer();
		final WorkLogCreate workLogCreate = new WorkLogCreate(jiraServer, jiraServerFacade, issue, project);
		workLogCreate.show();
		if (workLogCreate.isOK()) {

			Task.Backgroundable logWork = new Task.Backgroundable(project, "Logging Work", false) {
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
								jiraIssueListModelBuilder.updateIssue(issue);
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

	public void startWorkingOnIssue() {
		final JIRAIssue issue = currentIssueListModel.getSelectedIssue();
		if (issue == null) {
			return;
		}
		createChangeListAction(issue);
		final JiraServerCfg server = jiraIssueListModelBuilder.getServer();

		Task.Backgroundable startWorkOnIssue = new Task.Backgroundable(project, "Starting Work on Issue", false) {

			@Override
			public void run(@NotNull final ProgressIndicator indicator) {
				JIRAIssue myIssue = currentIssueListModel.getSelectedIssue();
				setStatusMessage("Assigning issue " + myIssue.getKey() + " to myself...");
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
							jiraIssueListModelBuilder.updateIssue(myIssue);
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

	public void setIssuesFilterParams(JiraServerCfg server, List<JIRAQueryFragment> manualFilter) {
		jiraIssueListModelBuilder.setServer(server);
		jiraIssueListModelBuilder.setCustomFilter(manualFilter);
	}

	public void setIssuesFilterParams(JiraServerCfg server, JIRASavedFilter savedFilter) {
		jiraIssueListModelBuilder.setServer(server);
		jiraIssueListModelBuilder.setSavedFilter(savedFilter);
	}

	public void refreshIssues() {
		getIssues(true);
	}

	public void getNextIssues() {
		getIssues(false);
	}

	private void getIssues(final boolean reload) {
		Task.Backgroundable task = new Task.Backgroundable(project, "Retrieving issues", false) {
			@Override
			public void run(@NotNull final ProgressIndicator indicator) {
				try {
					messagePane.setMessage("Loading issues...");
					jiraIssueListModelBuilder.addIssuesToModel(JIRA_ISSUE_PAGE_SIZE, reload);
				} catch (JIRAException e) {
					setStatusMessage(e.getMessage(), true);
				}
			}
		};

		ProgressManager.getInstance().run(task);
	}

	private JComponent createIssuesContent() {
		issuesPanel = new JPanel(new BorderLayout());

		issueTreescrollPane = new JScrollPane(createIssuesTree(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		issueTreescrollPane.setWheelScrollingEnabled(true);

		issuesPanel.add(issueTreescrollPane, BorderLayout.CENTER);
		issuesPanel.add(createIssuesToolbar(), BorderLayout.NORTH);
		return issuesPanel;
	}

	private JTree createIssuesTree() {
		issueTree = new JTree();
		issueTreeBuilder.rebuild(issueTree, issuesPanel);
		return issueTree;
	}

	public void expandAllIssueTreeNodes() {
		for (int i = 0; i < issueTree.getRowCount(); i++) {
			issueTree.expandRow(i);
		}
	}

	public void collapseAllIssueTreeNodes() {
		for (int i = 0; i < issueTree.getRowCount(); i++) {
			issueTree.collapseRow(i);
		}
	}

	public void expandAllFilterTreeNodes() {
		serversTree.expandAll();
	}

	public void collapseAllFilterTreeNodes() {
		serversTree.collapseAll();
	}

	private JComponent createIssuesToolbar() {
		ActionManager actionManager = ActionManager.getInstance();
		ActionGroup toolbar = (ActionGroup) actionManager.getAction("ThePlugin.JiraIssues.IssuesToolBar");
		ActionToolbar actionToolbar = actionManager
				.createActionToolbar(" ThePlugin.JiraIssues.IssuesToolBar.Place", toolbar, true);


		CellConstraints cc = new CellConstraints();

		final JPanel toolBarPanel = new JPanel(
				new FormLayout("left:1dlu:grow, right:1dlu:grow, left:pref:grow, right:pref:grow", "pref:grow"));
		toolBarPanel.add(new JLabel("Group By "), cc.xy(2, 1));
		toolBarPanel.add(actionToolbar.getComponent(), cc.xy(2 + 1, 1));
		toolBarPanel.add(searchField, cc.xy(2 + 2, 1));

		return toolBarPanel;
	}

	private JComponent createFilterContent() {
		serversPanel = new JPanel(new BorderLayout());

		serversTree = createJiraServersTree(jiraFilterListModel);
		JScrollPane filterListScrollPane = new JScrollPane(serversTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		manualFilterEditDetailsPanel = new JIRAManualFilterDetailsPanel(jiraFilterListModel, projectConfigurationBean,
																			project, jiraServerModel);

		filterListScrollPane.setWheelScrollingEnabled(true);
		splitFilterPane = new Splitter(false, 1.0f);
		splitFilterPane.setOrientation(true);
		serversPanel.add(filterListScrollPane, BorderLayout.CENTER);
		serversPanel.add(createServersToolbar(), BorderLayout.NORTH);

		//create manual filter panel
		splitFilterPane.setFirstComponent(serversPanel);

		return splitFilterPane;
	}

	private void showManualFilterPanel(boolean visible) {
		splitFilterPane.setOrientation(true);

		if (visible) {
			if (manualFilterEditDetailsPanel != null) {
				manualFilterEditDetailsPanel.setText(jiraFilterListModel.getJiraSelectedManualFilter().toHTML());
			}
			splitFilterPane.setSecondComponent(manualFilterEditDetailsPanel);
			splitFilterPane.setProportion(MANUAL_FILTER_PROPORTION_VISIBLE);

		} else {
			splitFilterPane.setSecondComponent(null);
			splitFilterPane.setProportion(MANUAL_FILTER_PROPORTION_HIDDEN);
		}
	}


	private JComponent createServersToolbar() {
		ActionManager actionManager = ActionManager.getInstance();
		ActionGroup toolbar = (ActionGroup) actionManager.getAction(SERVERS_TOOL_BAR);
		ActionToolbar actionToolbar = actionManager.createActionToolbar("ThePlugin.Issues.ServersToolBar.Place", toolbar, true);

		return actionToolbar.getComponent();
	}

	private JIRAFilterTree createJiraServersTree(JIRAFilterListModel listModel) {
		return new JIRAFilterTree(listModel);
	}

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

	public void setStatusMessage(final String message) {
		messagePane.setMessage(message);
	}

	public void setStatusMessage(final String msg, final boolean isError) {
		messagePane.setMessage(msg, isError);
	}

	public JIRAIssueGroupBy getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(JIRAIssueGroupBy groupBy) {
		this.groupBy = groupBy;
		issueTreeBuilder.setGroupBy(groupBy);
		issueTreeBuilder.rebuild(issueTree, issuesPanel);
		expandAllIssueTreeNodes();

		// store in project workspace
		projectConfigurationBean.getJiraConfiguration().getView().setGroupBy(groupBy);
	}

	public void createIssue() {
		JIRAIssueListModelBuilder builder = IdeaHelper.getProjectComponent(project, JIRAIssueListModelBuilderImpl.class);
		if (builder == null) {
			return;
		}

		final JiraServerCfg server = builder.getServer();

		if (server != null) {
			final IssueCreate issueCreate = new IssueCreate(jiraServerModel, server);

			issueCreate.initData();
			issueCreate.show();
			if (issueCreate.isOK()) {

				Task.Backgroundable createTask = new Task.Backgroundable(project, "Creating Issue", false) {
					@Override
					public void run(@NotNull final ProgressIndicator indicator) {
						setStatusMessage("Creating new issue...");
						String message;
						boolean isError = false;
						try {
							JIRAIssue issueToCreate = issueCreate.getJIRAIssue();
							JIRAIssue createdIssue = jiraServerFacade.createIssue(server, issueToCreate);

							message = "New issue created: <a href="
									+ createdIssue.getIssueUrl()
									+ ">"
									+ createdIssue.getKey()
									+ "</a>";

							jiraIssueListModelBuilder.updateIssue(createdIssue);
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

	private class MetadataFetcherBackgroundableTask extends Task.Backgroundable {
		public MetadataFetcherBackgroundableTask() {
			super(IssuesToolWindowPanel.this.project, "Retrieving JIRA information", false);
		}

		@Override
		public void run(@NotNull final ProgressIndicator indicator) {
			try {
			jiraServerModel.setModelFrozen(true);
			jiraServerModel.clearAll();

			for (JiraServerCfg server : IdeaHelper.getCfgManager()
					.getAllEnabledJiraServers(CfgUtil.getProjectId(project))) {
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
			}}

			finally {
				jiraServerModel.setModelFrozen(false);
			}


			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					refreshFilterModel();
					jiraFilterListModel.fireModelChanged();
				}
			});
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
		public void serverDataUpdated(ServerId serverId) {
			ServerCfg server = cfgManager.getServer(CfgUtil.getProjectId(project), serverId);
			if (server.getServerType() == ServerType.JIRA_SERVER) {
				refreshModels();
			}
		}

		//		@Override
//		public void serverConnectionDataUpdated(ServerId serverId) {
//			// refresh the Issues View only if currently displayed server has been changed
//			if (jiraIssueListModelBuilder.getServer().getServerId().equals(serverId)) {
//				refreshModels();
//			}
//		}
//
//		@Override
//		public void serverNameUpdated(ServerId serverId) {
//			ServerCfg server = cfgManager.getServer(CfgUtil.getProjectId(project), serverId);
//			// refresh the view only if JIRA enabled server has been changed
//			if (server.isEnabled() && server.getServerType() == ServerType.JIRA_SERVER) {
//				// todo PL-854 refresh only labels on the filter tree
////				refreshFilterModel();
////				jiraFilterListModel.fireModelChanged();
//			}
//		}
//
//		@Override
//		public void serverEnabled(ServerId serverId) {
//			ServerCfg server = cfgManager.getServer(CfgUtil.getProjectId(project), serverId);
//			// refresh the view only if JIRA enabled server has been changed
//			if (server.getServerType() == ServerType.JIRA_SERVER) {
//				// todo PL-854 load data for newly enabled server
////				refreshFilterModel();
////				jiraFilterListModel.fireModelChanged();
//			}
//		}
//
//		@Override
//		public void serverDisabled(ServerId serverId) {
//			// todo PL-854 refresh only Filters View without reloading data from server
////				refreshFilterModel();
////				jiraFilterListModel.fireModelChanged();
//		}
//
//		@Override
//		public void serverAdded(ServerCfg newServer) {
//			// refresh Filters View only if new enabled server has been added
//			if (newServer.isEnabled()) {
//				// do the same functionality as for enabled server
//				serverEnabled(newServer.getServerId());
//			}
//		}
//
//		@Override
//		public void serverRemoved(ServerCfg oldServer) {
//			// refresh Filter View only if removed server was enabled
//			if (oldServer.isEnabled()) {
//				// do the same functionality as for disabled server
//				serverDisabled(oldServer.getServerId());
//			}
//		}
	}
}
