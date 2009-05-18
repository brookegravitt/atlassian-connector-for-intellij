package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.ConfigurationListenerAdapter;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.MultiTabToolWindow;
import com.atlassian.theplugin.idea.PluginToolWindowPanel;
import com.atlassian.theplugin.idea.action.issues.RunIssueActionAction;
import com.atlassian.theplugin.idea.action.issues.activetoolbar.ActiveIssueUtils;
import com.atlassian.theplugin.idea.action.issues.oneissue.RunJiraActionGroup;
import com.atlassian.theplugin.idea.jira.renderers.JIRAIssueListOrTreeRendererPanel;
import com.atlassian.theplugin.idea.ui.*;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.JIRAUserNameCache;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.jira.model.JIRAIssueListModel;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilder;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelListener;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.ui.content.ContentManagerListener;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * User: jgorycki
 * Date: Dec 23, 2008
 * Time: 3:59:21 PM
 */
public final class IssueDetailsToolWindow extends MultiTabToolWindow {
	private static final String TOOL_WINDOW_TITLE = "Issues - JIRA";
	private static final String[] NONE = {"None"};

	private static JIRAServerFacade facade = JIRAServerFacadeImpl.getInstance();
	private final Project project;
	private final JIRAIssueListModelBuilder jiraIssueListModelBuilder;
	private PluginConfiguration pluginConfiguration;
	private final CfgManager cfgManager;

	public IssueDetailsToolWindow(@NotNull final Project project,
			@NotNull JIRAIssueListModelBuilder jiraIssueListModelBuilder,
			@NotNull final PluginConfiguration pluginConfiguration,
			@NotNull CfgManager cfgManager) {
		super(false);
		this.project = project;
		this.jiraIssueListModelBuilder = jiraIssueListModelBuilder;
		this.pluginConfiguration = pluginConfiguration;
		this.cfgManager = cfgManager;
	}

	private final class IssueContentParameters implements ContentParameters {
		// mutable because model may update the issue and we want to know about it (we have listener in place)
		private JIRAIssue issue;
		private final JIRAIssueListModel model;

		private IssueContentParameters(JIRAIssue issue, JIRAIssueListModel model) {
			this.issue = issue;
			this.model = model;
		}
	}

	public void showIssue(final JIRAIssue issue, JIRAIssueListModel model) {
		final IssueContentParameters issueContentParameters = new IssueContentParameters(issue, model);
		showToolWindow(project, issueContentParameters,
				TOOL_WINDOW_TITLE, Constants.JIRA_ISSUE_PANEL_ICON, Constants.JIRA_ISSUE_TAB_ICON,
				new ContentListener(getContentKey(issueContentParameters)));
	}

	protected ContentPanel createContentPanel(ContentParameters params) {
		pluginConfiguration.getGeneralConfigurationData().bumpCounter("i");
		return new IssuePanel((IssueContentParameters) params);
	}

	protected String getContentKey(ContentParameters params) {
		IssueContentParameters icp = (IssueContentParameters) params;
		ServerData server = icp.issue.getServer();
		return server != null ? server.getUrl() + server.getUserName() + icp.issue.getKey() : "";
	}

	public void setCommentsExpanded(String key, boolean expanded) {
		IssuePanel ip = getContentPanel(key);
		if (ip != null) {
			ip.descriptionAndCommentsPanel.setAllVisible(expanded);
		}
	}

	public void closeToolWindow(AnActionEvent e) {
		closeToolWindow(TOOL_WINDOW_TITLE, e);
	}

	public boolean isServerEnabled(String key) {
		IssuePanel ip = getContentPanel(key);
		ServerCfg serverCfg = cfgManager.getServer(CfgUtil.getProjectId(project),
				ip != null && ip.params != null && ip.params.issue != null ? ip.params.issue.getServer() : null);

		return ip != null && ip.params != null && serverCfg != null && serverCfg.isEnabled();
	}

	public void refreshComments(String key) {
		IssuePanel ip = getContentPanel(key);
		if (ip != null) {
			ip.descriptionAndCommentsPanel.refreshDescriptionAndComments();
		}
	}

	public void addComment(String key) {
		IssuePanel ip = getContentPanel(key);
		if (ip != null) {
			ip.descriptionAndCommentsPanel.addComment();
		}
	}

	public void viewIssueInBrowser(String key) {
		IssuePanel ip = getContentPanel(key);
		if (ip != null) {
			BrowserUtil.launchBrowser(ip.params.issue.getIssueUrl());
		}
	}

	public void editIssueInBrowser(String key) {
		IssuePanel ip = getContentPanel(key);
		if (ip != null) {
			JIRAIssue issue = ip.params.issue;
			BrowserUtil.launchBrowser(issue.getServerUrl() + "/secure/EditIssue!default.jspa?key=" + issue.getKey());
		}
	}

	public JIRAIssue getIssue(String key) {
		IssuePanel ip = getContentPanel(key);
		if (ip != null) {
			return ip.params.issue;
		}
		return null;
	}

	public void refresh(String key) {
		IssuePanel ip = getContentPanel(key);
		if (ip != null) {
			ip.refresh();
		}
	}


	private final class ContentListener implements ContentManagerListener {
		private final String contentKey;

		private ContentListener(final String contentKey) {
			this.contentKey = contentKey;
		}

		public void contentAdded(final ContentManagerEvent contentManagerEvent) {
		}

		public void contentRemoved(final ContentManagerEvent contentManagerEvent) {
		}

		public void contentRemoveQuery(final ContentManagerEvent contentManagerEvent) {
		}

		public void selectionChanged(final ContentManagerEvent contentManagerEvent) {
			if (contentKey.equals(contentManagerEvent.getContent().getTabName())
					&& contentManagerEvent.getContent().isSelected()) {
				IssuePanel ip = getContentPanel(contentKey);
				if (ip != null) {
					ip.reloadAvailableActions();
				}
			}
		}
	}

	private class IssuePanel extends ContentPanel implements DataProvider, IssueActionProvider {
		private DescriptionAndCommentsPanel descriptionAndCommentsPanel;
		private DetailsPanel detailsPanel;
		private SummaryPanel summaryPanel;
		private final IssueContentParameters params;
		private int stackTraceCounter = 0;
		private IssueDetailsToolWindow.IssuePanel.LocalConfigListener configurationListener = new LocalConfigListener();
		private Task.Backgroundable getSubTasksTask;
		private DefaultListModel subtaskListModel;
		private IssueDetailsToolWindow.IssuePanel.LocalModelListener modelListener;

		public IssuePanel(IssueContentParameters params) {
			this.params = params;

			JTabbedPane tabs = new JTabbedPane();
			detailsPanel = new DetailsPanel();
			tabs.addTab("Details", detailsPanel);
			descriptionAndCommentsPanel = new DescriptionAndCommentsPanel(tabs, 1);
			tabs.addTab("Comments(0)", descriptionAndCommentsPanel);

			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 1.0;
			gbc.weighty = 0.0;
			gbc.insets = new Insets(Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN, 0, 0);
			summaryPanel = new SummaryPanel();
			add(summaryPanel, gbc);
			gbc.gridy++;
			gbc.weighty = 1.0;
			gbc.insets = new Insets(0, 0, 0, 0);
			add(tabs, gbc);

			if (params.model != null) {
				modelListener = new LocalModelListener();
				params.model.addModelListener(modelListener);
			}

			cfgManager.addProjectConfigurationListener(CfgUtil.getProjectId(project), configurationListener);

			addComponentListener(new ComponentAdapter() {
				@Override
				public void componentHidden(ComponentEvent componentEvent) {
					if (getSubTasksTask != null) {
						getSubTasksTask.onCancel();
					}
				}
			});

			refresh();
		}

		public String getTitle() {
			return params.issue.getKey();
		}

		public void setStatusInfoMessage(final String message) {
			// ignore for now - should we display it?
		}

		public void setStatusErrorMessage(final String message) {
			// ignore for now - should we display it?
		}

		public void setStatusErrorMessage(final String error, final Throwable exception) {

		}

		public ServerData getSelectedServer() {
			return params != null && params.issue != null ? params.issue.getServer()
					: null;
		}

		public void unregister() {
			if (params.model != null) {
				params.model.removeModelListener(modelListener);
			}
			cfgManager.removeProjectConfigurationListener(CfgUtil.getProjectId(project), configurationListener);
		}

		private ServerData getJiraServerCfg() {
			if (params != null && params.issue != null) {
				if (params.issue.getServer() != null) {
					return params.issue.getServer();
				}
			}

			return null;
		}

		public void refresh() {
			retrieveIssueFromModel();

			ServerData jiraServerCfg = getJiraServerCfg();

			if (params.issue != null && jiraServerCfg != null) {
				ProgressManager.getInstance().run(new Task.Backgroundable(project, "Retrieving issue", false) {
					private boolean retrieved = false;

					public void run(@NotNull final ProgressIndicator indicator) {
						try {
							params.issue = facade.getIssue(params.issue.getServer(), params.issue.getKey());
							retrieved = true;
						} catch (final JIRAException e) {
							EventQueue.invokeLater(new Runnable() {
								public void run() {
									PluginUtil.getLogger().warn("Error retrieving issue in details panel", e);
									DialogWithDetails.showExceptionDialog(project, e.getMessage(), e);
								}
							});
						}
					}

					public void onSuccess() {
						if (retrieved) {
							ActiveIssueUtils.checkIssueState(project, params.issue);
							issueReloaded();
							jiraIssueListModelBuilder.updateIssue(params.issue);
						}
					}
				});
			}
		}

		private void retrieveIssueFromModel() {
			ServerData jiraServerCfg = getJiraServerCfg();
			for (JIRAIssue i : params.model.getIssues()) {
				if (i.getKey().equals(params.issue.getKey()) && i.getServerUrl().equals(jiraServerCfg.getUrl())) {
					params.issue = i;
					// todo check active issue
//					ActiveIssueUtils.checkIssueState(project, i);
					break;
				}
			}
		}

		private void issueReloaded() {
			descriptionAndCommentsPanel.refreshDescriptionAndComments();
			detailsPanel.refresh();
			summaryPanel.refresh();
			reloadAvailableActions();
		}

		void reloadAvailableActions() {
			final RunJiraActionGroup actionGroup = (RunJiraActionGroup) ActionManager
					.getInstance().getAction("ThePlugin.JiraIssues.RunActionGroup");

			final JIRAIssue issue = params.issue;
			if (issue != null) {
				java.util.List<JIRAAction> actions = JiraIssueAdapter.get(issue).getCachedActions();
				if (actions != null) {
					actionGroup.clearActions(project);
					for (JIRAAction a : actions) {
						actionGroup.addAction(project,
								new RunIssueActionAction(this, facade, issue, a, jiraIssueListModelBuilder));
					}
				} else {
					Thread t = new Thread() {
						@Override
						public void run() {
							try {
								ServerData jiraServer =
										params != null && params.issue != null ? params.issue.getServer() : null;

								if (jiraServer != null) {
									final java.util.List<JIRAAction> actions = facade
											.getAvailableActions(jiraServer, issue);

									JiraIssueAdapter.get(issue).setCachedActions(actions);
									SwingUtilities.invokeLater(new Runnable() {
										public void run() {
											actionGroup.clearActions(project);
											for (JIRAAction a : actions) {
												actionGroup.addAction(
														project,
														new RunIssueActionAction(IssueDetailsToolWindow.IssuePanel.this, facade,
																issue,
																a, jiraIssueListModelBuilder));
											}
										}
									});
								}
							} catch (JIRAException e) {
								// if we can not read actions, group will be disabled
								LoggerImpl.getInstance()
										.error("Unable to read available actions for issue: " + issue.getKey());
							}
						}
					};
					t.start();
				}
			}
		}

		public Object getData(@NonNls final String dataId) {
			if (dataId.equals(Constants.ISSUE)) {
				return params.issue;
			}
			if (dataId.equals(Constants.SERVER)) {
				return getJiraServerCfg();
			}
			return null;
		}

		private class DetailsPanel extends JPanel {

			private JLabel affectsVersions = new JLabel("Fetching...");
			private JLabel fixVersions = new JLabel("Fetching...");
			private JLabel components = new JLabel("Fetching...");
			private JLabel affectsVersionsLabel = new BoldLabel("Affects Version/s");
			private JLabel fixVersionsLabel = new BoldLabel("Fix Version/s");
			private JLabel componentsLabel = new BoldLabel("Component/s");
			private JLabel originalEstimate = new JLabel("Fetching...");
			private JLabel remainingEstimate = new JLabel("Fetching...");
			private JLabel timeSpent = new JLabel("Fetching...");
			private JLabel issueType;
			private JLabel issueStatus;
			private JLabel issuePriority;
			private JComponent issueAssignee;
			private JComponent issueReporter;
			private JLabel issueResolution;
			private JLabel issueCreationTime;
			private JLabel issueUpdateTime;
			private static final float SPLIT_RATIO = 0.3f;
			private static final int SUBTASKS_LABEL_HEIGHT = 24;

			public DetailsPanel() {
				super(new BorderLayout());

				subtaskListModel = new DefaultListModel();

				add(createBody(), BorderLayout.CENTER);
			}

			private JPanel createBody() {
				boolean haveSubTasks = params.issue.getSubTaskKeys().size() > 0;

				JPanel panel = new JPanel(new BorderLayout());

				JPanel details = createDetailsPanel();

				if (haveSubTasks) {
					Splitter split = new Splitter(false, SPLIT_RATIO);
					split.setFirstComponent(new JScrollPane(details));
					split.setHonorComponentsMinimumSize(true);
					JComponent subtasks = createSubtasksPanel();
					split.setSecondComponent(subtasks);
					panel.add(split, BorderLayout.CENTER);
				} else {
					panel.setOpaque(true);
					panel.setBackground(Color.WHITE);
					panel.add(new JScrollPane(details), BorderLayout.CENTER);
				}

				return panel;
			}

			private JComponent createSubtasksPanel() {
				BorderLayout borderLayout = new BorderLayout();
				JPanel panel = new JPanel(borderLayout);
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.weightx = 1.0;
				gbc.weighty = 1.0;
				gbc.fill = GridBagConstraints.HORIZONTAL;

				panel.setOpaque(false);
				final java.util.List<String> keys = params.issue.getSubTaskKeys();
				if (keys.size() > 0) {
					final JList list = new JList() {
						@Override
						public boolean getScrollableTracksViewportWidth() {
							return true;
						}
					};
					list.setCellRenderer(new SubtaskListCellRenderer());
					list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					list.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent event) {
							if (event.getClickCount() == 2) {
								openSelectedSubtask(list);
							}
						}
					});
					list.addKeyListener(new KeyAdapter() {
						@Override
						public void keyPressed(KeyEvent event) {
							if (event.getKeyCode() == KeyEvent.VK_ENTER) {
								openSelectedSubtask(list);
							}
						}
					});
					subtaskListModel.clear();
					list.setModel(subtaskListModel);
					JLabel subtasksLabel = new JLabel("Subtasks");
					subtasksLabel.setPreferredSize(
							new Dimension(subtasksLabel.getPreferredSize().width, SUBTASKS_LABEL_HEIGHT));
					panel.add(subtasksLabel, BorderLayout.NORTH);
					JScrollPane scrollPane = new JScrollPane(list);
					scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
					panel.add(scrollPane, BorderLayout.CENTER);
					if (getSubTasksTask == null) {
						createFetchSubtasksBackgroundTask(keys);
						ProgressManager.getInstance().run(getSubTasksTask);
					}
				}

				return panel;
			}

			private void openSelectedSubtask(JList list) {
				Object o = list.getSelectedValue();
				if (o != null && o instanceof JIRAIssue) {
					IssueListToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(project);
					if (panel != null) {
						panel.openIssue(((JIRAIssue) o).getKey(), params.issue.getServer());
					}
				}
			}

			private void createFetchSubtasksBackgroundTask(final List<String> keys) {
				getSubTasksTask = new Task.Backgroundable(project,
						"Fetching subtasks for issue " + params.issue.getKey(), true) {
					private List<JIRAIssue> subtasks = new ArrayList<JIRAIssue>();

					public void run(@NotNull ProgressIndicator progressIndicator) {
						Collection<JIRAIssue> subtasksInModel = params.model.getSubtasks(params.issue);
						Map<String, JIRAIssue> subKeysInModel = new HashMap<String, JIRAIssue>();
						for (JIRAIssue sub : subtasksInModel) {
							subKeysInModel.put(sub.getKey(), sub);
						}
						for (String key : keys) {
							try {
								if (subKeysInModel.keySet().contains(key)) {
									subtasks.add(subKeysInModel.get(key));
								} else {
									JIRAIssue subtask = facade.getIssue(params.issue.getServer(), key);
									if (subtask != null) {
										subtasks.add(subtask);
									}
								}
							} catch (JIRAException e) {
								LoggerImpl.getInstance().error(e);
							}
						}
					}

					@Override
					public void onCancel() {
						getSubTasksTask = null;
					}

					@Override
					public void onSuccess() {
						subtaskListModel.clear();
						for (JIRAIssue subtask : subtasks) {
							subtaskListModel.addElement(subtask);
						}
						getSubTasksTask = null;
					}
				};
			}

			private JPanel createDetailsPanel() {
				JPanel panel = new ScrollablePanel();

				panel.setLayout(new GridBagLayout());
				panel.setOpaque(false);

				GridBagConstraints gbc1 = new GridBagConstraints();
				GridBagConstraints gbc2 = new GridBagConstraints();
				gbc1.anchor = GridBagConstraints.FIRST_LINE_START;
				gbc2.anchor = GridBagConstraints.FIRST_LINE_START;
				gbc1.insets = new Insets(Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN,
						Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
				gbc2.insets = new Insets(Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN,
						Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
				gbc2.fill = GridBagConstraints.HORIZONTAL;
				gbc2.weightx = 1.0;
				gbc1.gridx = 0;
				gbc2.gridx = gbc1.gridx + 1;
				gbc1.gridy = 0;
				gbc2.gridy = 0;

				panel.add(new BoldLabel("Type"), gbc1);

				fillBaseIssueDetails();

				panel.add(issueType, gbc2);
				gbc1.gridy++;
				gbc2.gridy++;

				gbc1.insets = new Insets(0, Constants.DIALOG_MARGIN,
						Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
				gbc2.insets = new Insets(0, Constants.DIALOG_MARGIN,
						Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
				if (params.issue.isSubTask()) {
					String parent = params.issue.getParentIssueKey();
					panel.add(new BoldLabel("Parent Issue"), gbc1);
					panel.add(new MyHyperlinkLabel(parent, new HyperlinkListener() {
						public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
							IssueListToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(project);
							if (panel != null) {
								panel.openIssue(params.issue.getParentIssueKey(), params.issue.getServer());
							}
						}
					}), gbc2);
					gbc1.gridy++;
					gbc2.gridy++;
				}
				panel.add(new BoldLabel("Status"), gbc1);
				panel.add(issueStatus, gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				panel.add(new BoldLabel("Priority"), gbc1);
				panel.add(issuePriority, gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				panel.add(new BoldLabel("Assignee"), gbc1);
				panel.add(issueAssignee, gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				panel.add(new BoldLabel("Reporter"), gbc1);
				panel.add(issueReporter, gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				panel.add(new BoldLabel("Resolution"), gbc1);
				panel.add(issueResolution, gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				panel.add(new BoldLabel("Created"), gbc1);
				panel.add(issueCreationTime, gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				panel.add(new BoldLabel("Updated"), gbc1);
				panel.add(issueUpdateTime, gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				panel.add(affectsVersionsLabel, gbc1);
				panel.add(affectsVersions, gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				panel.add(fixVersionsLabel, gbc1);
				panel.add(fixVersions, gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				panel.add(componentsLabel, gbc1);
				panel.add(components, gbc2);

				gbc1.gridy++;
				gbc2.gridy++;
				panel.add(new BoldLabel("Original Estimate"), gbc1);
				panel.add(originalEstimate, gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				panel.add(new BoldLabel("Remaining Estimate"), gbc1);
				panel.add(remainingEstimate, gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				panel.add(new BoldLabel("Time Spent"), gbc1);
				panel.add(timeSpent, gbc2);

				addFillerPanel(panel, gbc1, false);

				return panel;
			}

			private void fillBaseIssueDetails() {
				issueType = new JLabel(params.issue.getType(),
						CachedIconLoader.getIcon(params.issue.getTypeIconUrl()),
						SwingConstants.LEFT);
				issueStatus = new JLabel(params.issue.getStatus(),
						CachedIconLoader.getIcon(params.issue.getStatusTypeUrl()),
						SwingConstants.LEFT);
				issuePriority = new JLabel(params.issue.getPriority(),
						CachedIconLoader.getIcon(params.issue.getPriorityIconUrl()),
						SwingConstants.LEFT);
				// bleeeee :( - assignee ID (String value) equals "-1" for unassigned issues. Oh my...
				if (!params.issue.getAssigneeId().equals("-1")) {
					issueAssignee = new UserLabel(params.issue.getServerUrl(), params.issue.getAssignee(),
							params.issue.getAssigneeId(), true);
				} else {
					issueAssignee = new JLabel("Unassigned");
				}
				issueReporter = new UserLabel(params.issue.getServerUrl(), params.issue.getReporter(),
						params.issue.getReporterId(), true);
				issueResolution = new JLabel(params.issue.getResolution());
				issueCreationTime = new JLabel(JiraTimeFormatter.formatTimeFromJiraTimeString(params.issue.getCreated()));
				issueUpdateTime = new JLabel(JiraTimeFormatter.formatTimeFromJiraTimeString((params.issue.getUpdated())));
			}

			public JLabel getAffectVersionsLabel() {
				return affectsVersions;
			}

			public JLabel getFixVersionsLabel() {
				return fixVersions;
			}

			public JLabel getComponentsLabel() {
				return components;
			}

			public void setAffectsVersions(String[] versions) {
				if (versions.length < 2) {
					affectsVersionsLabel.setText("Affects Version");
				} else {
					affectsVersionsLabel.setText("Affects Versions");
				}
				setLabelText(getAffectVersionsLabel(), versions);
			}

			public void setFixVersions(String[] versions) {
				if (versions.length < 2) {
					fixVersionsLabel.setText("Fix Version");
				} else {
					fixVersionsLabel.setText("Fix Versions");
				}
				setLabelText(getFixVersionsLabel(), versions);
			}

			public void setComponents(String[] components) {
				if (components.length < 2) {
					componentsLabel.setText("Component");
				} else {
					componentsLabel.setText("Components");
				}

				setLabelText(getComponentsLabel(), components);
			}

			private void setLabelText(JLabel label, String[] texts) {
				if (texts.length == 0) {
					label.setText(NONE[0]);
				} else {

					StringBuffer txt = new StringBuffer();
					for (int i = 0; i < texts.length; ++i) {
						if (i > 0) {
							txt.append(", ");
						}
						txt.append(texts[i]);
					}
					label.setText(txt.toString());
				}
			}

			private String[] getStringArray(java.util.List<JIRAConstant> l) {
				if (l == null) {
					return NONE;
				}
				java.util.List<String> sl = new ArrayList<String>(l.size());
				for (JIRAConstant c : l) {
					sl.add(c.getName());
				}
				return sl.toArray(new String[l.size()]);
			}


			public synchronized void getIssueDetails() {
				Runnable runnable = new IssueDetailsRunnable();
				new Thread(runnable, "atlassian-idea-plugin get issue details").start();
			}

			private class IssueDetailsRunnable implements Runnable {
				private String[] errorString = null;

				public void run() {

					try {
						if (params != null && params.issue != null && params.issue.getServer() != null) {
							// damn it! the XML view of the list of issues does not
							// have estimates and time spent :(

							final JIRAIssue issueDetails = facade.getIssueDetails(params.issue.getServer(), params.issue);
							params.issue.setAffectsVersions(issueDetails.getAffectsVersions());
							params.issue.setFixVersions(issueDetails.getFixVersions());
							params.issue.setComponents(issueDetails.getComponents());
						}
					} catch (JIRAException e) {
						errorString = new String[]{"Unable to retrieve"};
					}
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							removeAll();
							rendererMap.clear();
							add(createBody(), BorderLayout.CENTER);
							if (errorString == null) {
								setAffectsVersions(getStringArray(params.issue.getAffectsVersions()));
								setFixVersions(getStringArray(params.issue.getFixVersions()));
								setComponents(getStringArray(params.issue.getComponents()));
								setOriginalEstimate(params.issue.getOriginalEstimate());
								setRemainingEstimate(params.issue.getRemainingEstimate());
								setTimeSpent(params.issue.getTimeSpent());
							} else {
								getAffectVersionsLabel().setForeground(Color.RED);
								getFixVersionsLabel().setForeground(Color.RED);
								getComponentsLabel().setForeground(Color.RED);
								originalEstimate.setForeground(Color.RED);
								remainingEstimate.setForeground(Color.RED);
								timeSpent.setForeground(Color.RED);
								setAffectsVersions(errorString);
								setFixVersions(errorString);
								setComponents(errorString);
								setOriginalEstimate(errorString[0]);
								setRemainingEstimate(errorString[0]);
								setTimeSpent(errorString[0]);

							}
						}
					});
				}
			}

			private void setTimeSpent(String t) {
				if (t != null) {
					timeSpent.setText(t);
				} else {
					timeSpent.setText("None");
				}
			}

			private void setRemainingEstimate(String t) {
				if (t != null) {
					remainingEstimate.setText(t);
				} else {
					remainingEstimate.setText("None");
				}
			}

			private void setOriginalEstimate(String t) {
				if (t != null) {
					originalEstimate.setText(t);
				} else {
					originalEstimate.setText("None");
				}
			}

			public void refresh() {
				getIssueDetails();
			}

			private Map<JIRAIssue, JIRAIssueListOrTreeRendererPanel> rendererMap =
					new HashMap<JIRAIssue, JIRAIssueListOrTreeRendererPanel>();

			private class SubtaskListCellRenderer extends DefaultListCellRenderer {
				public Component getListCellRendererComponent(JList list, Object value, int index,
						boolean isSelected, boolean cellHasFocus) {
					if (value != null && value instanceof JIRAIssue) {
						JIRAIssue issue = (JIRAIssue) value;
						JIRAIssueListOrTreeRendererPanel r = rendererMap.get(issue);
						if (r == null) {
							r = new JIRAIssueListOrTreeRendererPanel(issue);
							rendererMap.put(issue, r);
						}
						r.setParameters(isSelected, true);
						return r;
					}
					return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				}
			}
		}

		private class SummaryPanel extends JPanel {

			private JEditorPane summary;

			public SummaryPanel() {
				setLayout(new GridBagLayout());
				GridBagConstraints gbc = new GridBagConstraints();

				gbc.gridy = 0;
				gbc.gridx = 0;
				gbc.anchor = GridBagConstraints.LINE_START;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.weightx = 1.0;
				summary = new JEditorPane();
				summary.setContentType("text/html");
				summary.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
				refresh();
				summary.setEditable(false);
				summary.addHyperlinkListener(new HyperlinkListener() {
					public void hyperlinkUpdate(HyperlinkEvent e) {
						if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
							BrowserUtil.launchBrowser(e.getURL().toString());
						}
					}
				});

				summary.setFont(summary.getFont().deriveFont(Font.BOLD));
				summary.setOpaque(false);
				JPanel p = new JPanel();
				p.setLayout(new GridBagLayout());
				GridBagConstraints gbcp = new GridBagConstraints();
				gbcp.fill = GridBagConstraints.BOTH;
				gbcp.weightx = 1.0;
				gbcp.weighty = 1.0;
				gbcp.gridx = 0;
				gbcp.gridy = 0;
				p.add(summary, gbcp);
				add(p, gbc);

				gbc.gridy++;

				ActionManager manager = ActionManager.getInstance();
				ActionGroup group = (ActionGroup) manager.getAction("ThePlugin.JiraIssues.OneIssueToolBar");
				ActionToolbar toolbar = manager.createActionToolbar(getContentKey(params), group, true);
				toolbar.setTargetComponent(IssueDetailsToolWindow.IssuePanel.this);

				JComponent comp = toolbar.getComponent();
				add(comp, gbc);
			}

			public void refresh() {
				String txt = "<html><body><a href=\"" + params.issue.getIssueUrl() + "\">"
						+ params.issue.getKey() + "</a> " + params.issue.getSummary() + "</body></html>";
				summary.setText(txt);
			}
		}

		private class DescriptionAndCommentsPanel extends JPanel {

			private final Splitter splitPane = new Splitter(false, PluginToolWindowPanel.PANEL_SPLIT_RATIO);

			private JScrollPane scroll = new JScrollPane();
			private ScrollablePanel comments = new ScrollablePanel();

			private Border border = BorderFactory.createTitledBorder("Comments");
			private final JTabbedPane tabs;
			private final int tabIndex;
			private DescriptionPanel descriptionPanel;

			public DescriptionAndCommentsPanel(JTabbedPane tabs, int tabIndex) {
				this.tabs = tabs;
				this.tabIndex = tabIndex;

				JPanel rightPanel = new JPanel();
				rightPanel.setLayout(new GridBagLayout());

				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.weightx = 1.0;
				gbc.fill = GridBagConstraints.HORIZONTAL;

				ActionManager manager = ActionManager.getInstance();
				ActionGroup group = (ActionGroup) manager.getAction("ThePlugin.JiraIssues.CommentsToolBar");
				ActionToolbar toolbar = manager.createActionToolbar(getContentKey(params), group, true);
				toolbar.setTargetComponent(IssueDetailsToolWindow.IssuePanel.this);

				JComponent comp = toolbar.getComponent();
				rightPanel.add(comp, gbc);

				gbc.gridx = 0;
				gbc.gridy = 1;
				gbc.insets = new Insets(0, 0, 0, 0);
				gbc.fill = GridBagConstraints.BOTH;

				gbc.weighty = 1.0;
				comments.setLayout(new VerticalFlowLayout());
				comments.setOpaque(true);
				comments.setBackground(Color.WHITE);
				scroll.setViewportView(comments);
				scroll.getViewport().setOpaque(true);
				scroll.getViewport().setBackground(Color.WHITE);
				scroll.setOpaque(true);
				scroll.setBackground(Color.WHITE);
				scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				scroll.setBorder(BorderFactory.createEmptyBorder());

				JPanel wrap = new JPanel();
				wrap.setBorder(border);
				wrap.setLayout(new BorderLayout());
				wrap.add(scroll, BorderLayout.CENTER);
				rightPanel.add(wrap, gbc);

				descriptionPanel = new DescriptionPanel();
				splitPane.setFirstComponent(descriptionPanel);
				splitPane.setSecondComponent(rightPanel);

				setLayout(new BorderLayout());
				splitPane.setShowDividerControls(false);
				add(splitPane, BorderLayout.CENTER);
			}

			public void setTitle(String title) {
				border = BorderFactory.createTitledBorder(title);
				setBorder(border);
			}

			public void addComment(JIRAComment c) {
				CommentPanel p = new CommentPanel(comments.getComponents().length + 1, c, params.issue.getServer(), tabs);
				comments.add(p);
			}

			public void clearComments() {
				comments.removeAll();
			}

			public void setAllVisible(boolean visible) {
				for (Component c : comments.getComponents()) {
					((CommentPanel) c).getShowHideButton().setState(visible);
				}
			}

			public void scrollToFirst() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						scroll.getVerticalScrollBar().setValue(0);
					}
				});
			}

			public void addComment() {
				final IssueCommentDialog issueCommentDialog = new IssueCommentDialog(params.issue.getKey());
				issueCommentDialog.show();
				if (issueCommentDialog.isOK()) {
					Runnable runnable = new Runnable() {
						public void run() {
							try {
								if (params != null && params.issue != null && params.issue.getServer() != null) {
									facade.addComment(params.issue.getServer(), params.issue.getKey(),
											issueCommentDialog.getComment());
									EventQueue.invokeLater(new Runnable() {
										public void run() {
											refreshDescriptionAndComments();
										}
									});
								}
							} catch (JIRAException e) {
								final String msg = e.getMessage();
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										Messages.showMessageDialog(
												"Failed to add comment to issue " + params.issue.getKey() + ": " + msg,
												"Error", Messages.getErrorIcon());
									}
								});
							}
						}
					};
					new Thread(runnable, "atlassian-idea-plugin comment issue from editor").start();
				}
			}

			public void refreshDescriptionAndComments() {

				tabs.setTitleAt(tabIndex, "Refreshing comments...");
				Runnable runnable = new RefreshDescriptionAndCommentsRunnable();
				new Thread(runnable, "atlassian-idea-plugin refresh comments").start();
			}

			private void resetStackTraces() {
				stackTraceCounter = 0;

				while (tabs.getTabCount() > 2) {
					tabs.remove(2);
				}

				String stack = Html2text.translate(params.issue.getDescription());
				if (StackTraceDetector.containsStackTrace(stack)) {
					tabs.add("Stack Trace: Description", new StackTracePanel(stack));
				}
			}

			private class RefreshDescriptionAndCommentsRunnable implements Runnable {
				public void run() {
					try {
						if (params != null && params.issue != null && params.issue.getServer() != null) {
							java.util.List<JIRAComment> cmts = null;

							JIRAIssue oneIssue = facade.getIssue(params.issue.getServer(), params.issue.getKey());
							if (oneIssue != null) {
								descriptionPanel.setDescription(oneIssue.getDescription());
								cmts = oneIssue.getComments();
							}

							if (cmts == null) {
								// oh well, no comments in XML - can it even happen? Fall back to SOAP
								cmts = facade.getComments(params.issue.getServer(), params.issue);
							}

							for (JIRAComment c : cmts) {
								try {
									JIRAUserBean u = JIRAUserNameCache.getInstance()
											.getUser(getJiraServerCfg(), c.getAuthor());
									c.setAuthorFullName(u.getName());
								} catch (JiraUserNotFoundException e) {
									c.setAuthorFullName(c.getAuthor());
								}
							}

							final java.util.List<JIRAComment> finalCmtsYesIKnowThisIsStupidButYouKnowCheckstyle = cmts;
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									clearComments();
									resetStackTraces();
									int size = finalCmtsYesIKnowThisIsStupidButYouKnowCheckstyle.size();
									if (size > 0) {
										for (JIRAComment c : finalCmtsYesIKnowThisIsStupidButYouKnowCheckstyle) {
											addComment(c);
										}
									}
									tabs.setTitleAt(tabIndex, "Comments(" + size + ")");
								}
							});
						}
					} catch (JIRAException e) {
						tabs.setTitleAt(tabIndex, "Unable to retrieve comments");
					}
				}
			}
		}

		private final class MyHyperlinkLabel extends JPanel {
			private MyHyperlinkLabel(String label, HyperlinkListener listener) {
				super(new GridBagLayout());
				setOpaque(false);

				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.weightx = 0.0;
				gbc.weighty = 0.0;
				gbc.fill = GridBagConstraints.NONE;

				HyperlinkLabel link = new HyperlinkLabel(label);
				link.setOpaque(false);
				link.addHyperlinkListener(listener);

				add(link, gbc);
				addFillerPanel(this, gbc, true);
			}
		}

		private class UserLabel extends JPanel {
			private JLabel label;

			UserLabel(final String serverUrl, final String userName, final String userNameId, boolean useLink) {
				setOpaque(true);
				setLayout(new GridBagLayout());
				GridBagConstraints gbc = new GridBagConstraints();

				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.weightx = 0.0;
				gbc.weighty = 0.0;
				gbc.fill = GridBagConstraints.NONE;

				label = new JLabel();
				setBackground(Color.WHITE);
				label.setBorder(BorderFactory.createEmptyBorder());
				label.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
				String userNameFixed = userName.replace(" ", "&nbsp;");
				if (useLink) {
					label.setText("<html><body><font color=\"#0000ff\"><u>" + userNameFixed + "</u></font></body></html>");
					addListener(serverUrl, userNameId);
				} else {
					label.setText("<html><body>" + userNameFixed + "</body></html>");
				}
				add(label, gbc);
				addFillerPanel(this, gbc, true);
			}

			private void addListener(final String serverUrl, final String userNameId) {
				label.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						BrowserUtil.launchBrowser(serverUrl + "/secure/ViewProfile.jspa?name=" + userNameId);
					}

					public void mouseEntered(MouseEvent e) {
						setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					}

					public void mouseExited(MouseEvent e) {
						setCursor(Cursor.getDefaultCursor());
					}
				});
			}
		}

		private class DescriptionPanel extends JPanel {
			private JEditorPane body;

			public DescriptionPanel() {
				setLayout(new GridBagLayout());
				GridBagConstraints gbc = new GridBagConstraints();

				gbc.gridx = 0;
				gbc.gridy = 0;

				gbc.insets = new Insets(0, 0, 0, 0);
				gbc.fill = GridBagConstraints.BOTH;
				gbc.weightx = 1.0;
				gbc.weighty = 1.0;

				body = new JEditorPane();
				JScrollPane sp = new JScrollPane(body,
						ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				sp.setBorder(BorderFactory.createEmptyBorder());
				sp.setOpaque(false);
				body.setEditable(false);
				body.addHyperlinkListener(new HyperlinkListener() {
					public void hyperlinkUpdate(HyperlinkEvent e) {
						if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
							BrowserUtil.launchBrowser(e.getURL().toString());
						}
					}
				});

				body.setOpaque(true);
				body.setBackground(Color.WHITE);
				body.setMargin(new Insets(Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN / 2,
						Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN / 2));
				body.setContentType("text/html");
				body.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
				setDescription(params.issue.getDescription());
				sp.getViewport().setOpaque(false);
				add(sp, gbc);

				Border b = BorderFactory.createTitledBorder("Description");
				setBorder(b);
				Insets i = b.getBorderInsets(this);
				int minHeight = i.top + i.bottom;
				setMinimumSize(new Dimension(0, minHeight));
			}

			public void setDescription(String description) {
				if (description != null) {
					String descriptionFixed = description.replace("/>", ">");
					body.setText("<html><body>" + descriptionFixed + "</body></html>");
					body.setCaretPosition(0);
				} else {
					body.setText("");
				}
			}
		}

		private class CommentPanel extends JPanel {

			private ShowHideButton btnShowHide;

			private static final int COMMENT_GAP = 6;

			private HeaderListener headerListener;

			public CommentPanel(int cmtNumber, final JIRAComment comment, final ServerData server, JTabbedPane tabs) {
				setOpaque(true);
				setBackground(Color.WHITE);

				int upperMargin = cmtNumber == 1 ? 0 : COMMENT_GAP;

				setLayout(new GridBagLayout());
				GridBagConstraints gbc;

				JEditorPane commentBody = new JEditorPane();
				btnShowHide = new ShowHideButton(commentBody, this);
				headerListener = new HeaderListener();

				gbc = new GridBagConstraints();
				gbc.gridx++;
				gbc.gridy = 0;
				gbc.anchor = GridBagConstraints.WEST;
				gbc.insets = new Insets(upperMargin, 0, 0, 0);
				add(btnShowHide, gbc);

				gbc.gridx++;
				gbc.insets = new Insets(upperMargin, Constants.DIALOG_MARGIN / 2, 0, 0);
				UserLabel ul = new UserLabel(server.getUrl(), comment.getAuthorFullName(),
						comment.getAuthor(), false);
				ul.setFont(ul.getFont().deriveFont(Font.BOLD));
				add(ul, gbc);

				final JLabel hyphen = new WhiteLabel();
				hyphen.setText("-");
				gbc.gridx++;
				gbc.insets = new Insets(upperMargin, Constants.DIALOG_MARGIN / 2, 0, Constants.DIALOG_MARGIN / 2);
				add(hyphen, gbc);

				final JLabel creationDate = new WhiteLabel();
				creationDate.setForeground(Color.GRAY);
				creationDate.setFont(creationDate.getFont().deriveFont(Font.ITALIC));

				DateFormat df = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy", Locale.US);
				DateFormat dfo = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
				String t;
				try {
					t = dfo.format(df.parse(comment.getCreationDate().getTime().toString()));
				} catch (java.text.ParseException e) {
					t = "Invalid date: " + comment.getCreationDate().getTime().toString();
				}

				creationDate.setText(t);
				gbc.gridx++;
				gbc.insets = new Insets(upperMargin, 0, 0, 0);
				add(creationDate, gbc);

				String dehtmlizedBody = Html2text.translate(comment.getBody());
				if (StackTraceDetector.containsStackTrace(dehtmlizedBody)) {
					tabs.add("Comment Stack Trace #" + (++stackTraceCounter), new StackTracePanel(dehtmlizedBody));

					gbc.gridx++;
					gbc.insets = new Insets(upperMargin, Constants.DIALOG_MARGIN / 2, 0, 0);
					JLabel traceNumber = new WhiteLabel();
					traceNumber.setText("Stack Trace #" + stackTraceCounter);
					traceNumber.setForeground(Color.RED);

					add(traceNumber, gbc);
				}

				// filler
				gbc.gridx++;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.weightx = 1.0;
				JPanel filler = new JPanel();
				filler.setBackground(Color.WHITE);
				filler.setOpaque(true);
				gbc.insets = new Insets(upperMargin, 0, 0, 0);
				add(filler, gbc);

				int gridwidth = gbc.gridx + 1;

				commentBody.setEditable(false);
				commentBody.setOpaque(true);
				commentBody.setBackground(Color.WHITE);
				commentBody.setMargin(new Insets(0, 2 * Constants.DIALOG_MARGIN, 0, 0));
				commentBody.setContentType("text/html");
				commentBody.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
				// JEditorPane does not do XHTML :(
				String bodyFixed = comment.getBody().replace("/>", ">");
				commentBody.setText("<html><head></head><body>" + bodyFixed + "</body></html>");
				commentBody.addHyperlinkListener(new HyperlinkListener() {
					public void hyperlinkUpdate(HyperlinkEvent e) {
						if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
							BrowserUtil.launchBrowser(e.getURL().toString());
						}
					}
				});
				gbc.gridx = 0;
				gbc.gridy = 1;
				gbc.gridwidth = gridwidth;
				gbc.weightx = 1.0;
				gbc.weighty = 1.0;
				gbc.fill = GridBagConstraints.BOTH;
				gbc.insets = new Insets(0, 0, 0, 0);
				add(commentBody, gbc);

				addMouseListener(headerListener);
			}

			private class HeaderListener extends MouseAdapter {
				public void mouseClicked(MouseEvent e) {
					btnShowHide.click();
				}
			}

			public ShowHideButton getShowHideButton() {
				return btnShowHide;
			}
		}

		private class StackTracePanel extends JPanel {
			public StackTracePanel(String stack) {

				TextConsoleBuilderFactory factory = TextConsoleBuilderFactory.getInstance();
				TextConsoleBuilder builder = factory.createBuilder(project);
				ConsoleView console = builder.getConsole();
				console.print(stack, ConsoleViewContentType.NORMAL_OUTPUT);

				setLayout(new GridBagLayout());
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.weightx = 1.0;
				gbc.weighty = 1.0;
				gbc.fill = GridBagConstraints.BOTH;
				add(console.getComponent(), gbc);
			}
		}

		private class LocalConfigListener extends ConfigurationListenerAdapter {

			public void jiraServersChanged(final ProjectConfiguration newConfiguration) {
				((JIRAIssueBean) params.issue).setServer(params.issue.getServer());
			}
		}

		private class LocalModelListener implements JIRAIssueListModelListener {
			private boolean singleIssueChanged = false;

			public void issueUpdated(final JIRAIssue issue) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						singleIssueChanged = true;
//						if (issue.equals(params.issue)) {
//							ActiveIssueUtils.checkIssueState(project, params.issue);
//						}
					}
				});

			}

			public void modelChanged(final JIRAIssueListModel model) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						retrieveIssueFromModel();
						issueReloaded();
						if (!singleIssueChanged) {
							ActiveIssueUtils.checkIssueState(project, params.issue);
						}
						singleIssueChanged = false;
					}
				});
			}

			public void issuesLoaded(final JIRAIssueListModel model, final int loadedIssues) {

			}
		}
	}

	private static void addFillerPanel(JPanel parent, GridBagConstraints gbc, boolean horizontal) {
		if (horizontal) {
			gbc.gridx++;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
		} else {
			gbc.gridy++;
			gbc.weighty = 1.0;
			gbc.fill = GridBagConstraints.VERTICAL;
		}
		JPanel filler = new JPanel();
		filler.setOpaque(false);
		parent.add(filler, gbc);
	}
}
