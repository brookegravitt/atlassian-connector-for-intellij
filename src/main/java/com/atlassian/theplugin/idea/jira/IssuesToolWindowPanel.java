package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.ConfigurationListener;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.editor.vfs.JiraIssueVirtualFile;
import com.atlassian.theplugin.idea.jira.tree.JIRAFilterTree;
import com.atlassian.theplugin.idea.jira.tree.JIRAIssueTreeBuilder;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.jira.model.*;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: pmaruszak
 */
public final class IssuesToolWindowPanel extends JPanel implements ConfigurationListener {
	private static final Key<IssuesToolWindowPanel> WINDOW_PROJECT_KEY = Key.create(IssuesToolWindowPanel.class.getName());
	private static final float SPLIT_RATIO = 0.3f;
	private Project project;
	private PluginConfigurationBean pluginConfiguration;
	private ProjectConfigurationBean projectConfigurationBean;
	private CfgManager cfgManager;
	private JPanel serversPanel = new JPanel(new BorderLayout());
	private JPanel issuesPanel = new JPanel(new BorderLayout());
	private final Splitter splitPane = new Splitter(true, SPLIT_RATIO);
	private static final String SERVERS_TOOL_BAR = "ThePlugin.JiraServers.ServersToolBar";
	private JIRAFilterListModel jiraFilterListModel;
	private JIRAIssueTreeBuilder issueTreeBuilder;
	private JTree issueTree;
	private JIRAIssueListModelBuilder jiraIssueListModelBuilder;
	private Splitter splitFilterPane;
	private JIRAIssueGroupBy groupBy;
	private static final int JIRA_ISSUE_PAGE_SIZE = 25;
	private JIRAServer currentJIRAServer;
	private JPanel manualFilterPanel;
	private JIRAIssueFilterPanel jiraIssueFilterPanel;
	JScrollPane manualFiltereditScrollPane;
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
		IdeaHelper.getProjectComponent(project, JIRAFilterListBuilder.class).setListModel(jiraFilterListModel);
		IdeaHelper.getProjectComponent(project, JIRAFilterListBuilder.class).setProjectId(CfgUtil.getProjectId(project));
		IdeaHelper.getProjectComponent(project, JIRAFilterListBuilder.class)
				.setProjectConfigurationBean(projectConfigurationBean);

		jiraIssueListModel.addModelListener(new JIRAIssueListModelListener() {
			public void modelChanged(JIRAIssueListModel model) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JIRAServer server = jiraServerCache.get(jiraIssueListModelBuilder.getServer());
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
				currentJIRAServer = jiraServerCache.get(jiraServer);
				IdeaHelper.setCurrentJIRAServer(currentJIRAServer);
				showManualFilterPanel(true);
			}

			public void selectedSavedFilter(final JiraServerCfg jiraServer, final JIRASavedFilter savedFilter) {
				showManualFilterPanel(false);
				currentJIRAServer = jiraServerCache.get(jiraServer);
				IdeaHelper.setCurrentJIRAServer(currentJIRAServer);
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

		final JPopupMenu jPopupMenu = popup.getComponent();
		jPopupMenu.show(e.getComponent(), e.getX(), e.getY());
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

	public void viewIssueInBrowser(AnActionEvent event) {
		JIRAIssue issue = jiraIssueListModel.getSelectedIssue();
		if (issue != null) {
			BrowserUtil.launchBrowser(issue.getIssueUrl());
		}
	}

	public void editIssueInBrowser(AnActionEvent event) {
		JIRAIssue issue = jiraIssueListModel.getSelectedIssue();
		if (issue != null) {
			BrowserUtil.launchBrowser(issue.getServerUrl() + "/secure/EditIssue!default.jspa?key=" + issue.getKey());
		}
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
			setMessage("Some Jira servers did not return saved filters", true);
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
					setMessage(e.getMessage(), true);
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
		manualFilterPanel = new JPanel(new BorderLayout());
		manualFilterPanel.add(new JLabel("Manual filter: "), BorderLayout.CENTER);
		HyperlinkLabel hl = new HyperlinkLabel("edit filter");
		hl.addHyperlinkListener(new HyperlinkListener() {
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
						    jiraFilterListModel.setManualFilter(jiraServer, manualFilter);

//								updateIssues(IdeaHelper.getCurrentJIRAServer(project));
//								filters.setManualFilter(serializeQuery());
//								filters.setSavedFilterUsed(false);
//								JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer(project);
//
//								if (jiraServer != null) {
//									projectConfiguration.
//											getJiraConfiguration().setFiltersBean(
//											jiraServer.getServer().getServerId().toString(), filters);
//								}						

					} else {
						//cancel
					}


				}

			}
		});
		manualFilterPanel.add(hl, BorderLayout.CENTER);
		return manualFilterPanel;

	}
	private void showManualFilterPanel(boolean visible){
		splitFilterPane.setOrientation(true);

		if (visible){

			splitFilterPane.setSecondComponent(manualFiltereditScrollPane);
			splitFilterPane.setProportion(0.5f);

		} else {
			splitFilterPane.setSecondComponent(null);
			splitFilterPane.setProportion(0.9f);
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
					final JIRAServerFacade jiraServerFacade = JIRAServerFacadeImpl.getInstance();
					JIRAServer jiraServer = new JIRAServer(server, jiraServerFacade);
//					if (!jiraServer.checkServer()) {
//						//setStatusMessage("Unable to connect to server. " + jiraServer.getErrorMessage(), true);
//						EventQueue.invokeLater(
//								new MissingPasswordHandlerJIRA(jiraServerFacade, jiraServer.getServer(), this));
//						return;}
					//@todo remove  saved filters download or merge with existing in listModel
					String serverStr = "[" + server.getName() + "] ";
					setMessage(serverStr + "Retrieving saved filters...");
					jiraServer.getSavedFilters();
					setMessage(serverStr + "Retrieving projects...");
					jiraServer.getProjects();
					setMessage(serverStr + "Retrieving issue types...");
					jiraServer.getIssueTypes();
					setMessage(serverStr + "Retrieving statuses...");
					jiraServer.getStatuses();
					setMessage(serverStr + "Retrieving resolutions...");
					jiraServer.getResolutions();
					setMessage(serverStr + "Retrieving priorities...");
					jiraServer.getPriorieties();
					setMessage(serverStr + "Retrieving projects...");
					jiraServer.getProjects();
					setMessage(serverStr + "Metadata query finished");
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

	public void setMessage(final String message) {
		messagePane.setMessage(message);
	}

	public void setMessage(final String msg, final boolean isError) {
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

	public JIRAServer getCurrentJIRAServer() {
		return currentJIRAServer;
	}

	public boolean canCreateIssue() {
		return currentJIRAServer != null;
	}

	public void createIssue() {
		if (currentJIRAServer != null) {
			final IssueCreate issueCreate = new IssueCreate(currentJIRAServer);
			final JIRAServerFacade jiraServerFacade = JIRAServerFacadeImpl.getInstance();

			issueCreate.initData();
			issueCreate.show();
			if (issueCreate.isOK()) {

				Task.Backgroundable createTask = new Task.Backgroundable(project, "Creating Issue", false) {
					public void run(final ProgressIndicator indicator) {
						setMessage("Creating new issue...");
						String message;
						boolean isError = false;
						try {
							JIRAIssue issueToCreate = issueCreate.getJIRAIssue();
							JIRAIssue createdIssue = jiraServerFacade.createIssue(currentJIRAServer.getServer(),
									issueToCreate);

							JIRAIssueBean newIssue = (JIRAIssueBean) jiraServerFacade.getIssueDetails(
									currentJIRAServer.getServer(), createdIssue);
							newIssue = currentJIRAServer.fixupIssue(newIssue, issueToCreate);

							message =
									"New issue created: <a href="
											+ newIssue.getIssueUrl()
											+ ">"
											+ newIssue.getKey()
											+ "</a>";

							jiraIssueListModel.addIssue(newIssue);

							jiraIssueListModel.notifyListeners();
						} catch (JIRAException e) {
							message = "Failed to create new issue: " + e.getMessage();
							isError = true;
						}

						final String msg = message;
						final boolean error = isError;
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								setMessage(msg, error);
							}
						});
					}
				};

				ProgressManager.getInstance().run(createTask);
			}
		}

	}
}
