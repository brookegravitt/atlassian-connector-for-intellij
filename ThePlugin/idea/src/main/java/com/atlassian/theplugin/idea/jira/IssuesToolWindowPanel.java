package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.ConfigurationListener;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.action.issues.RunIssueActionAction;
import com.atlassian.theplugin.idea.jira.editor.vfs.JiraIssueVirtualFile;
import com.atlassian.theplugin.idea.jira.tree.JIRAFilterTree;
import com.atlassian.theplugin.idea.jira.tree.JIRAIssueTreeBuilder;
import com.atlassian.theplugin.jira.JIRAIssueProgressTimestampCache;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.jira.model.*;
import com.atlassian.theplugin.remoteapi.MissingPasswordHandlerJIRA;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.HyperlinkLabel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class IssuesToolWindowPanel extends JPanel implements ConfigurationListener {
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
	private JPanel manualFilterPanel;
	private JIRAIssueFilterPanel jiraIssueFilterPanel;
	private JScrollPane manualFiltereditScrollPane;
	private JIRAServerFacade jiraServerFacade;

	private JScrollPane issueTreescrollPane;

	public MessageScrollPane getMessagePane() {
		return messagePane;
	}

	private MessageScrollPane messagePane;
	private JIRAIssueListModel jiraIssueListModel;

	private final Map<JiraServerCfg, JIRAServer> jiraServerCache = new HashMap<JiraServerCfg, JIRAServer>();

	private IssuesToolWindowPanel(
			final Project project, final PluginConfigurationBean pluginConfiguration,
			final ProjectConfigurationBean projectConfigurationBean, final CfgManager cfgManager) {
		this.project = project;
		this.pluginConfiguration = pluginConfiguration;
		this.projectConfigurationBean = projectConfigurationBean;
		this.cfgManager = cfgManager;

		jiraServerFacade = JIRAServerFacadeImpl.getInstance();

		setLayout(new BorderLayout());
		this.messagePane = new MessageScrollPane("Issues panel");
		add(messagePane, BorderLayout.SOUTH);


		groupBy = JIRAIssueGroupBy.TYPE;

		jiraFilterListModel = new JIRAFilterListModel();
		jiraIssueListModel = JIRAIssueListModelImpl.createInstance();
		jiraIssueListModelBuilder = IdeaHelper.getProjectComponent(project, JIRAIssueListModelBuilderImpl.class);
		issueTreeBuilder = new JIRAIssueTreeBuilder(getGroupBy(), jiraIssueListModel);

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

		jiraIssueListModelBuilder.setModel(jiraIssueListModel);
		jiraFilterListModelBuilder = IdeaHelper.getProjectComponent(project, JIRAFilterListBuilder.class);
		if (jiraFilterListModelBuilder != null) {
			jiraFilterListModelBuilder.setListModel(jiraFilterListModel);
			jiraFilterListModelBuilder.setProjectId(CfgUtil.getProjectId(project));
			jiraFilterListModelBuilder.setProjectConfigurationBean(projectConfigurationBean);
		}
		jiraIssueListModel.addModelListener(new JIRAIssueListModelListener() {
			public void modelChanged(JIRAIssueListModel model) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JiraIssueAdapter.clearCache();
						JiraServerCfg srvcfg = jiraIssueListModelBuilder.getServer();
						if (srvcfg == null) {
							return;
						}
						JIRAServer server = jiraServerCache.get(srvcfg);
						Map<String, String> projectMap = new HashMap<String, String>();
						for (JIRAProject p : server.getProjects()) {
							projectMap.put(p.getKey(), p.getName());
						}
						issueTreeBuilder.setProjectKeysToNames(projectMap);
						issueTreeBuilder.rebuild(issueTree, issueTreescrollPane.getViewport());
						expandAllIssueTreeNodes();
						messagePane.setStatus("Loaded " + jiraIssueListModel.getIssues().size() + " issues");
					}
				});
			}
		});
		jiraFilterListModel.addModelListener(new JIRAFilterListModelListener() {
			public void modelChanged(JIRAFilterListModel listModel) {
			}

			public void selectedManualFilter(final JiraServerCfg jiraServer, final List<JIRAQueryFragment> manualFilter) {
				// this has to be removed as soon as we remove the old tool window
				IdeaHelper.setCurrentJIRAServer(jiraServerCache.get(jiraServer));
				showManualFilterPanel(true);
				setIssuesFilterParams(jiraServer, manualFilter);
				refreshIssues();
			}

			public void selectedSavedFilter(final JiraServerCfg jiraServer, final JIRASavedFilter savedFilter) {
				showManualFilterPanel(false);
				// this has to be removed as soon as we remove the old tool window
				IdeaHelper.setCurrentJIRAServer(jiraServerCache.get(jiraServer));
				setIssuesFilterParams(jiraServer, savedFilter);
				refreshIssues();
			}
		});

		messagePane.addMoreListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				Messages.showErrorDialog("This feature is not implemented yet, see bug PL-804", "Not Implemented");
			}
		});

		addIssuesTreeListeners();

		refreshModels();
	}

	private void addIssuesTreeListeners() {
		issueTree.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				JIRAIssue issue = jiraIssueListModel.getSelectedIssue();
				if (e.getKeyCode() == KeyEvent.VK_ENTER && issue != null) {
					launchOpenIsueAction();
				}
			}
		});

		issueTree.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				JIRAIssue issue = jiraIssueListModel.getSelectedIssue();
				if (e.getButton() == MouseEvent.BUTTON1	&& e.getClickCount() == 2 && issue != null) {
					launchOpenIsueAction();
				} else if (e.getButton() == MouseEvent.BUTTON3	&& e.getClickCount() == 1) {
					int selRow = issueTree.getRowForLocation(e.getX(), e.getY());
					TreePath selPath = issueTree.getPathForLocation(e.getX(), e.getY());
					if (selRow != -1 && selPath != null) {
						issueTree.setSelectionPath(selPath);
						if (jiraIssueListModel.getSelectedIssue() != null) {
							launchContextMenu(e);
						}
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
			public void update(AnActionEvent event) {
				super.update(event);

				if (getChildrenCount() > 0) {
					event.getPresentation().setText("Available Workflow Actions");
				}
			}
		};
		actionGroup.add(submenu);

		final JIRAIssue issue = jiraIssueListModel.getSelectedIssue();
		List<JIRAAction> actions = JiraIssueAdapter.get(issue).getCachedActions();
		if (actions != null) {
			for (JIRAAction a : actions) {
				submenu.add(new RunIssueActionAction(this, jiraServerFacade, issue, a));
			}
		} else {
			Thread t = new Thread() {
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

	private void launchOpenIsueAction() {
		AnAction action = ActionManager.getInstance().getAction("ThePlugin.JiraIssues.OpenIssue");
		action.actionPerformed(new AnActionEvent(null, DataManager.getInstance().getDataContext(this),
				ActionPlaces.UNKNOWN, action.getTemplatePresentation(),
				ActionManager.getInstance(), 0));
	}

	public void openIssue(AnActionEvent event) {
		JIRAIssue issue = jiraIssueListModel.getSelectedIssue();
		if (issue != null) {
			FileEditorManager manager =
					FileEditorManager.getInstance(DataKeys.PROJECT.getData(event.getDataContext()));
			VirtualFile[] files = manager.getOpenFiles();
			VirtualFile vf = null;
			for (VirtualFile f : files) {
				if (f instanceof JiraIssueVirtualFile) {
					JiraIssueVirtualFile jivf = (JiraIssueVirtualFile) f;
					if (jivf.getIssue().getKey().equals(issue.getKey())) {
						vf = f;
						break;
					}
				}
			}

			if (vf == null) {
				vf = new JiraIssueVirtualFile(issue);
			}
			// either opens a new editor, or focuses the already open one
			manager.openFile(vf, true);
		}
	}

	public void viewIssueInBrowser() {
		JIRAIssue issue = jiraIssueListModel.getSelectedIssue();
		if (issue != null) {
			BrowserUtil.launchBrowser(issue.getIssueUrl());
		}
	}

	public void editIssueInBrowser() {
		JIRAIssue issue = jiraIssueListModel.getSelectedIssue();
		if (issue != null) {
			BrowserUtil.launchBrowser(issue.getServerUrl() + "/secure/EditIssue!default.jspa?key=" + issue.getKey());
		}
	}

	public void assignIssueToMyself() {
		final JIRAIssue issue = jiraIssueListModel.getSelectedIssue();
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
		final JIRAIssue issue = jiraIssueListModel.getSelectedIssue();
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

			public void run(final ProgressIndicator indicator) {
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

	public void createChangeListAction() {
		final JIRAIssue issue = jiraIssueListModel.getSelectedIssue();
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
		final JIRAIssue issue = jiraIssueListModel.getSelectedIssue();
		final IssueComment issueComment = new IssueComment(issue.getKey());
		issueComment.show();
		if (issueComment.isOK()) {
			Task.Backgroundable comment = new Task.Backgroundable(project, "Commenting Issue", false) {
				public void run(final ProgressIndicator indicator) {
					setStatusMessage("Commenting issue " + issue.getKey() + "...");
					try {
						JiraServerCfg jiraServer = jiraIssueListModelBuilder.getServer();
						if (jiraServer != null) {
							jiraServerFacade.addComment(jiraServer, issue, issueComment.getComment());
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
		final JIRAIssue issue = jiraIssueListModel.getSelectedIssue();
		final WorkLogCreate workLogCreate = new WorkLogCreate(jiraServerFacade, issue, project);
		workLogCreate.show();
		if (workLogCreate.isOK()) {

			Task.Backgroundable logWork = new Task.Backgroundable(project, "Logging Work", false) {
				public void run(final ProgressIndicator indicator) {
					setStatusMessage("Logging work for issue " + issue.getKey() + "...");
					try {
						Calendar cal = Calendar.getInstance();
						cal.setTime(workLogCreate.getStartDate());
						JiraServerCfg jiraServer = jiraIssueListModelBuilder.getServer();

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
		createChangeListAction();
		final JiraServerCfg server = jiraIssueListModelBuilder.getServer();

		Task.Backgroundable startWorkOnIssue = new Task.Backgroundable(project, "Starting Work on Issue", false) {

			public void run(final ProgressIndicator indicator) {
				JIRAIssue issue = jiraIssueListModel.getSelectedIssue();
				setStatusMessage("Assigning issue " + issue.getKey() + " to myself...");
				try {
					jiraServerFacade.setAssignee(server, issue, server.getUsername());
					List<JIRAAction> actions = jiraServerFacade.getAvailableActions(server, issue);
					boolean found = false;
					for (JIRAAction a : actions) {
						if (a.getId() == Constants.JiraActionId.START_PROGRESS.getId()) {
							setStatusMessage("Starting progress on " + issue.getKey() + "...");
							jiraServerFacade.progressWorkflowAction(server, issue, a);
							JIRAIssueProgressTimestampCache.getInstance().setTimestamp(server, issue);
							setStatusMessage("Started progress on " + issue.getKey());
							found = true;
							jiraIssueListModelBuilder.updateIssue(issue);
							break;
						}
					}
					if (!found) {
						setStatusMessage("Progress on "
								+ issue.getKey()
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


	public static synchronized IssuesToolWindowPanel getInstance(final Project project,
			final ProjectConfigurationBean projectConfigurationBean,
			final CfgManager cfgManager) {
		IssuesToolWindowPanel window = project.getUserData(WINDOW_PROJECT_KEY);

		if (window == null) {
			window = new IssuesToolWindowPanel(project, IdeaHelper.getPluginConfiguration(),
					projectConfigurationBean, cfgManager);
			project.putUserData(WINDOW_PROJECT_KEY, window);
		}
		return window;
	}

	private void refreshFilterModel() {

		try {
			IdeaHelper.getProjectComponent(project, JIRAFilterListBuilder.class).rebuildModel();
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
		Task.Backgroundable task = new Task.Backgroundable(project, "Retrieving issues", false) {
			public void run(final ProgressIndicator indicator) {
				try {
					messagePane.setStatus("Loading issues...");
					jiraIssueListModelBuilder.addIssuesToModel(JIRA_ISSUE_PAGE_SIZE, true);
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
//		toolBarPanel.add(new JLabel("Search"), cc.xy(2 + 2, 1));

		return toolBarPanel;
	}

	private JComponent createFilterContent() {


		serversPanel = new JPanel(new BorderLayout());

		JScrollPane filterListScrollPane = new JScrollPane(createJiraServersTree(jiraFilterListModel),
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		manualFiltereditScrollPane = new JScrollPane(createManualFilterEditPanel(),
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);		

		filterListScrollPane.setWheelScrollingEnabled(true);
		splitFilterPane = new Splitter(false, 1.0f);
		splitFilterPane.setOrientation(true);
		serversPanel.add(filterListScrollPane, BorderLayout.CENTER);
		serversPanel.add(createServersToolbar(), BorderLayout.NORTH);

		//create manual filter panel
		splitFilterPane.setFirstComponent(serversPanel);

		return splitFilterPane;
	}

	private JComponent createManualFilterEditPanel() {
		manualFilterPanel = new JPanel(new FormLayout("left:pref, left:pref, pref:grow", "pref:grow, pref:grow"));

		CellConstraints cc = new CellConstraints();

		manualFilterPanel.add(new JLabel("Custom Filter "), cc.xy(1,1));

		HyperlinkLabel hyperlinkLabel = new HyperlinkLabel("edit");
		manualFilterPanel.add(hyperlinkLabel, cc.xy(2,1));

		hyperlinkLabel.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {

				JiraServerCfg jiraServer = jiraFilterListModel.getJiraSelectedServer();
				jiraIssueFilterPanel = new JIRAIssueFilterPanel(project, null, jiraFilterListModel, jiraServer);


				if (jiraServer != null) {
					if (jiraServerCache.containsKey(jiraServer)) {
						jiraIssueFilterPanel.setJiraServer(jiraServerCache.get(jiraServer),
								jiraFilterListModel.getJiraSelectedManualFilter().getQueryFragment());
					}
					jiraIssueFilterPanel.show();

					if (jiraIssueFilterPanel.getExitCode() == 0) {
						JIRAManualFilter manualFilter =	jiraFilterListModel.getJiraSelectedManualFilter();
						jiraFilterListModel.clearManualFilter(jiraServer);
						manualFilter.getQueryFragment().addAll(jiraIssueFilterPanel.getFilter());
						jiraFilterListModel.selectManualFilter(jiraServer,  manualFilter);
							showManualFilterPanel(true);
					} else {
						//cancel
					}


				}

			}
		});

		return manualFilterPanel;

	}

	private void showManualFilterPanel(boolean visible) {
		splitFilterPane.setOrientation(true);

		if (visible) {
			CellConstraints cc = new CellConstraints();
			JLabel label = new JLabel(jiraFilterListModel.getJiraSelectedManualFilter().toHTML());
			manualFilterPanel.add(label, cc.xy(1,2));
			splitFilterPane.setSecondComponent(manualFiltereditScrollPane);
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

	private JComponent createJiraServersTree(JIRAFilterListModel listModel) {
		return new JIRAFilterTree(listModel);
	}

	public void configurationUpdated(final ProjectConfiguration aProjectConfiguration) {
		refreshModels();
	}

	private void refreshModels() {
		Task.Backgroundable task = new Task.Backgroundable(project, "Retrieving JIRA information", false) {
			public void run(final ProgressIndicator indicator) {
				jiraServerCache.clear();
				for (JiraServerCfg server : IdeaHelper.getCfgManager()
						.getAllEnabledJiraServers(CfgUtil.getProjectId(project))) {
					JIRAServer jiraServer = new JIRAServer(server, jiraServerFacade);
					if (!jiraServer.checkServer()) {
						setStatusMessage("Unable to connect to server. " + jiraServer.getErrorMessage(), true);
						EventQueue.invokeLater(
								new MissingPasswordHandlerJIRA(jiraServerFacade, jiraServer.getServer(), null));
						continue;
					}
					//@todo remove  saved filters download or merge with existing in listModel
					String serverStr = "[" + server.getName() + "] ";
					setStatusMessage(serverStr + "Retrieving saved filters...");
					jiraServer.getSavedFilters();
					setStatusMessage(serverStr + "Retrieving projects...");
					jiraServer.getProjects();
					setStatusMessage(serverStr + "Retrieving issue types...");
					jiraServer.getIssueTypes();
					setStatusMessage(serverStr + "Retrieving statuses...");
					jiraServer.getStatuses();
					setStatusMessage(serverStr + "Retrieving resolutions...");
					jiraServer.getResolutions();
					setStatusMessage(serverStr + "Retrieving priorities...");
					jiraServer.getPriorieties();
					setStatusMessage(serverStr + "Retrieving projects...");
					jiraServer.getProjects();
					setStatusMessage(serverStr + "Metadata query finished");
					jiraServerCache.put(server, jiraServer);
				}
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						refreshFilterModel();
						jiraFilterListModel.fireModelChanged();
					}
				});
			}
		};
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
	}

	public void createIssue() {
		JIRAIssueListModelBuilder builder = IdeaHelper.getProjectComponent(project, JIRAIssueListModelBuilderImpl.class);
		if (builder == null) {
			return;
		}

		final JiraServerCfg server = builder.getServer();

		if (server != null) {
			final IssueCreate issueCreate = new IssueCreate(jiraServerCache.get(server));

			issueCreate.initData();
			issueCreate.show();
			if (issueCreate.isOK()) {

				Task.Backgroundable createTask = new Task.Backgroundable(project, "Creating Issue", false) {
					public void run(final ProgressIndicator indicator) {
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
							jiraIssueListModel.notifyListeners();
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
}
