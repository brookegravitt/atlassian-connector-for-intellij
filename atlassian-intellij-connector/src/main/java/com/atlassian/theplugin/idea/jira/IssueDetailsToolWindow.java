package com.atlassian.theplugin.idea.jira;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.connector.commons.jira.JIRAAction;
import com.atlassian.connector.commons.jira.JIRAActionField;
import com.atlassian.connector.commons.jira.JIRAActionFieldBean;
import com.atlassian.connector.commons.jira.JiraUserNotFoundException;
import com.atlassian.connector.commons.jira.beans.JIRAAttachment;
import com.atlassian.connector.commons.jira.beans.JIRAComment;
import com.atlassian.connector.commons.jira.beans.JIRAConstant;
import com.atlassian.connector.commons.jira.beans.JIRAUserBean;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.jira.IntelliJJiraServerFacade;
import com.atlassian.theplugin.commons.jira.JiraActionFieldType;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.JiraServerFacade;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.cache.CachedIconLoader;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.MultiTabToolWindow;
import com.atlassian.theplugin.idea.PluginToolWindowPanel;
import com.atlassian.theplugin.idea.action.issues.RunIssueActionAction;
import com.atlassian.theplugin.idea.action.issues.oneissue.RunJiraActionGroup;
import com.atlassian.theplugin.idea.jira.renderers.JIRAIssueListOrTreeRendererPanel;
import com.atlassian.theplugin.idea.ui.BoldLabel;
import com.atlassian.theplugin.idea.ui.CommentPanel;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.idea.ui.EditableIssueField;
import com.atlassian.theplugin.idea.ui.ScrollablePanel;
import com.atlassian.theplugin.idea.ui.StackTracePanel;
import com.atlassian.theplugin.idea.ui.UserLabel;
import com.atlassian.theplugin.idea.ui.tree.paneltree.SelectableLabel;
import com.atlassian.theplugin.idea.util.Html2text;
import com.atlassian.theplugin.jira.cache.RecentlyOpenIssuesCache;
import com.atlassian.theplugin.jira.model.JIRAIssueListModel;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilder;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelListener;
import com.atlassian.theplugin.jira.model.JIRAServerModelIdea;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * User: jgorycki
 * Date: Dec 23, 2008
 * Time: 3:59:21 PM
 */
public final class IssueDetailsToolWindow extends MultiTabToolWindow {
	private static final String TOOL_WINDOW_TITLE = "Issues - JIRA";
	private static final String[] NONE = {"None"};

    protected static final int ROW_HEIGHT = 16;

	private static JiraServerFacade facade = IntelliJJiraServerFacade.getInstance();
	private final Project project;
	private final JIRAIssueListModelBuilder jiraIssueListModelBuilder;
	private PluginConfiguration pluginConfiguration;
	private ProjectCfgManager projectCfgManager;
    private final JIRAServerModelIdea jiraCache;


    private ContentPanel selectedContent = null;

    public IssueDetailsToolWindow(@NotNull final Project project,
			@NotNull JIRAIssueListModelBuilder jiraIssueListModelBuilder,
			@NotNull final PluginConfiguration pluginConfiguration,
			@NotNull ProjectCfgManager projectCfgManager,
            @NotNull JIRAServerModelIdea jiraCache) {
		super(false);
		this.project = project;
		this.jiraIssueListModelBuilder = jiraIssueListModelBuilder;
		this.pluginConfiguration = pluginConfiguration;
		this.projectCfgManager = projectCfgManager;
        this.jiraCache = jiraCache;
    }

    public boolean setStatusErrorMessage(String key, String message, JIRAException e) {
        IssuePanel panel = getContentPanel(key);
        if (panel != null) {
            panel.setStatusErrorMessage(message, e);
        }

        return panel != null;
    }

    private final class IssueContentParameters implements ContentParameters {
		// mutable because model may update the issue and we want to know about it (we have listener in place)
		private JiraIssueAdapter issue;
		private final JIRAIssueListModel model;

		private IssueContentParameters(JiraIssueAdapter issue, JIRAIssueListModel model) {
			this.issue = issue;
			this.model = model;
		}
	}

	public void showIssue(final JiraIssueAdapter issue, JIRAIssueListModel model) {
		final IssueContentParameters issueContentParameters = new IssueContentParameters(issue, model);
		showToolWindow(project, issueContentParameters,
				TOOL_WINDOW_TITLE, Constants.JIRA_ISSUE_PANEL_ICON, Constants.JIRA_ISSUE_TAB_ICON,
				new ContentListener(getContentKey(issueContentParameters)), null);
	}

	protected ContentPanel createContentPanel(ContentParameters params, ToolWindowHandler handler) {
		pluginConfiguration.getGeneralConfigurationData().bumpCounter("i");
		selectedContent = new IssuePanel((IssueContentParameters) params);
		return selectedContent;
	}

	protected String getContentKey(ContentParameters params) {
		IssueContentParameters icp = (IssueContentParameters) params;
		return getContentKey(icp.issue);
	}

	protected String getContentKey(JiraIssueAdapter issueCached) {
		JiraServerData jiraServerData = issueCached.getJiraServerData();
		return jiraServerData != null ? jiraServerData.getUrl() + jiraServerData.getUsername() + issueCached.getKey() : "";
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

//	public boolean isServerEnabled(String key) {
//		IssuePanel ip = getContentPanel(key);
//		ServerCfg serverCfg = projectCfgManager.getJiraServerData(
//				ip != null && ip.params != null && ip.params.issue != null ? ip.params.issue.getJiraServerData() : null);
//
//		return ip != null && ip.params != null && serverCfg != null && serverCfg.isEnabled();
//	}

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

	public void editDescription(String key) {
		final IssuePanel ip = getContentPanel(key);
		if (ip != null) {
			updateIssueField(ip.params.issue, new JIRAActionFieldBean("description", "Description"));
		}
	}

	public void addAttachment(String key) {
		IssuePanel ip = getContentPanel(key);
		if (ip != null) {
			JiraIssueAdapter issue = ip.params.issue;
			JFileChooser fc = new JFileChooser();
			if (fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
				return;
			}
			File file = fc.getSelectedFile();

			addAttachment(issue, file, IssueDetailsToolWindow.AttachmentAddedFrom.ISSUE_DETAILS_WINDOW);
		}
	}

	private void addAttachment(final File file) {
		try {
			IssuePanel ip = (IssuePanel) selectedContent;
			if (ip != null) {
				JiraIssueAdapter issue = ip.params.issue;
				addAttachment(issue, file, AttachmentAddedFrom.ISSUE_DETAILS_WINDOW);
			}
		} catch (ClassCastException e) {
			// oops
			e.printStackTrace();
		}
	}

	// used to determine where to show errors caused by adding attachment failures:
	public enum AttachmentAddedFrom {
		ISSUE_DETAILS_WINDOW, ISSUE_LIST_WINDOW
	}

	public void addAttachment(final JiraIssueAdapter issue, final File file, final AttachmentAddedFrom fromWindow) {
		final String name = file.getName();
		final byte[] contents = getFileContentsAsBytes(file);
		if (contents == null) {
			Messages.showErrorDialog("Unable to open file " + file.getName(), "Error");
			return;
		}
		final String issueKey = issue.getKey();
		final JiraServerData jiraServerData = issue.getJiraServerData();
		ProgressManager.getInstance().run(
				new Task.Backgroundable(project, "Uploading attachment " + name) {
					@Override
					public void run(@NotNull ProgressIndicator progressIndicator) {
						try {
							facade.addAttachment(jiraServerData, issueKey, name, contents);
						} catch (final JIRAException e) {
							EventQueue.invokeLater(new Runnable() {
								public void run() {
									switch (fromWindow) {
										case ISSUE_LIST_WINDOW :
											try {
												IdeaHelper.getIssueListToolWindowPanel(project).setStatusErrorMessage(
														"Error: " + e.getMessage(), e);
											} catch (NullPointerException npe) {
												npe.printStackTrace();
											}
											break;
										case ISSUE_DETAILS_WINDOW:
											setStatusErrorMessage(getContentKey(issue), "Error: " + e.getMessage(), e);
											break;
										default:
											throw new RuntimeException("AttachmentAddedFrom not handled");
									}
								}
							});
						}
					}
					@Override
					public void onSuccess() {
						IssuePanel ip = getContentPanel(getContentKey(issue));
						if (ip != null) {
							ip.attachementsPanel.refresh();
						}
					}
				}
		);
	}
	
	private static byte[] getFileContentsAsBytes(final File file) {
		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			byte[] buf = new byte[(int) file.length()];
			bis.read(buf);
			bis.close();
			return buf;
		} catch (IOException ex) {
			return null;
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
			JiraIssueAdapter issue = ip.params.issue;
			BrowserUtil.launchBrowser(issue.getServerUrl() + "/secure/EditIssue!default.jspa?key=" + issue.getKey());
		}
	}

	public void refresh(String panelKey) {
		IssuePanel ip = getContentPanel(panelKey);
		if (ip != null) {
			ip.refresh();
		}
	}

	public void refresh(JiraIssueAdapter issue) {
		IssuePanel ip = getContentPanel(getContentKey(issue));
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
					selectedContent = ip;
					ip.reloadAvailableActions();
				}
			}
		}
	}

	public void updateIssueField(final JiraIssueAdapter issue, final JIRAActionField field) {
		List<JIRAActionField> fields = new ArrayList<JIRAActionField>();
		fields.add(field);
		final List<JIRAActionField> preFilledFields = JiraActionFieldType.fillFieldValues(issue, fields);

		final PerformIssueActionForm dialog =
				new PerformIssueActionForm(project, issue, preFilledFields, "Update Issue [" + issue.getKey() + "]");
		dialog.show();
		if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
			ProgressManager.getInstance().run(
					new Task.Backgroundable(project, "Updating issue", false) {
						public void run(final ProgressIndicator indicator) {
							if (indicator != null) {
								indicator.setFraction(0.0);
								indicator.setIndeterminate(true);
							}
							JiraServerData server = issue.getJiraServerData();
							try {
								facade.setFields(server, issue, dialog.getFields());
								if (dialog.getComment() != null && dialog.getComment().length() > 0) {
									facade.addComment(server, issue.getKey(), dialog.getComment());
								}
								jiraIssueListModelBuilder.reloadIssue(issue.getKey(), server);
							} catch (JIRAException e) {
								setStatusErrorMessage(getContentKey(issue), "Unable to update issue ["
										+ issue.getKey() + "]: " + e.getMessage(), e);
							}
						}
					});
		}
	}


	public class IssuePanel extends ContentPanel implements DataProvider, IssueActionProvider {
		private DescriptionAndCommentsPanel descriptionAndCommentsPanel;
		private DetailsPanel detailsPanel;
		private SummaryPanel summaryPanel;
        private AttachementsPanel attachementsPanel;
		private final IssueContentParameters params;
		private int stackTraceCounter = 0;
		//private IssueDetailsToolWindow.IssuePanel.LocalConfigListener configurationListener = new LocalConfigListener();
		private Task.Backgroundable getSubTasksTask;
        private Task.Backgroundable getIssueLinksTask;
		private DefaultListModel subtaskListModel;
		private IssueDetailsToolWindow.IssuePanel.LocalModelListener modelListener;
		private StatusBarPane statusBarPane;

		public IssuePanel(IssueContentParameters params) {
			this.params = params;

			JTabbedPane tabs = new JTabbedPane();
			detailsPanel = new DetailsPanel();
			statusBarPane = new StatusBarPane(" ");
			tabs.addTab("Details", detailsPanel);
			descriptionAndCommentsPanel = new DescriptionAndCommentsPanel(tabs, 1);
			tabs.addTab("Comments(0)", descriptionAndCommentsPanel);
            attachementsPanel = new AttachementsPanel(tabs, 2);
            tabs.addTab("Attachments(0)", attachementsPanel);

			summaryPanel = new SummaryPanel();
			setLayout(new BorderLayout());
			add(summaryPanel, BorderLayout.NORTH);
			add(tabs, BorderLayout.CENTER);
			add(statusBarPane, BorderLayout.SOUTH);

			if (params.model != null) {
				modelListener = new LocalModelListener();
				params.model.addModelListener(modelListener);
			}

			//projectCfgManager.addProjectConfigurationListener(configurationListener);

			addComponentListener(new ComponentAdapter() {
				@Override
				public void componentHidden(ComponentEvent componentEvent) {
					if (getSubTasksTask != null) {
						getSubTasksTask.onCancel();
					}
                    if (getIssueLinksTask != null) {
                        getIssueLinksTask.onCancel();
                    }
				}
			});

			getAvailableActionsGroup().clearActions(project);
		}

		public Project getProject() {
			return project;
		}

		public String getTitle() {
			return params.issue.getKey();
		}

		public void setStatusInfoMessage(final String message) {
			// ignore for now - should we display it?
		}

		public void setStatusErrorMessage(final String message) {
			statusBarPane.setErrorMessage(message);
		}

		public void setStatusErrorMessage(final String error, final Throwable exception) {
			statusBarPane.setErrorMessage(error, exception);
		}

		public JiraServerData getSelectedServer() {
            return params != null && params.issue != null
                    ? params.issue.getJiraServerData()
					: null;
		}

		public void unregister() {
			if (params.model != null) {
				params.model.removeModelListener(modelListener);
			}
			//projectCfgManager.removeProjectConfigurationListener(configurationListener);
		}

		private JiraServerData getJiraServerData() {
			if (params != null && params.issue != null) {
				if (params.issue.getJiraServerData() != null) {
					return params.issue.getJiraServerData();
				}
			}

			return null;
		}

		public void refresh() {
			retrieveIssueFromModel();

			JiraServerData jiraServerData = getJiraServerData();

			if (params.issue != null && jiraServerData != null) {
				ProgressManager.getInstance().run(new Task.Backgroundable(project, "Retrieving issue", false) {
					private boolean retrieved = false;

					public void run(@NotNull final ProgressIndicator indicator) {
						try {
							params.issue = facade.getIssue(params.issue.getJiraServerData(), params.issue.getKey());
                            IdeaHelper.getProjectCfgManager(project)
                             .addProjectConfigurationListener(params.issue.getLocalConfigurationListener());
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
							EventQueue.invokeLater(new Runnable() {
								public void run() {
									//issueReloaded();
									jiraIssueListModelBuilder.updateIssue(params.issue);
								}
							});
						}
					}
				});
			}
		}

		private void retrieveIssueFromModel() {
			JiraServerData jiraServerData = getJiraServerData();
			for (JiraIssueAdapter i : params.model.getIssues()) {
				if (i.getKey().equals(params.issue.getKey()) && i.getServerUrl().equals(jiraServerData.getUrl())) {
					params.issue = i;
                     IdeaHelper.getProjectCfgManager(project)
                             .removeProjectConfigurationListener(params.issue.getLocalConfigurationListener());
                     IdeaHelper.getProjectCfgManager(project)
                             .addProjectConfigurationListener(params.issue.getLocalConfigurationListener());
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
            attachementsPanel.refresh();

			if (selectedContent == this) {
				reloadAvailableActions();
			}
		}

		void reloadAvailableActions() {
			final RunJiraActionGroup actionGroup = getAvailableActionsGroup();

			final JiraIssueAdapter issue = params.issue;
			if (issue != null) {
				actionGroup.clearActions(project);
				java.util.List<JIRAAction> actions = JiraIssueCachedAdapter.get(issue).getCachedActions();
				if (actions != null) {
					for (JIRAAction a : actions) {
						actionGroup.addAction(project,
								new RunIssueActionAction(this, facade, issue, a, jiraIssueListModelBuilder));
					}
				} else {
					Thread t = new Thread() {
						@Override
						public void run() {
							try {
								JiraServerData jiraServerData =
										params != null && params.issue != null ? params.issue.getJiraServerData() : null;

								if (jiraServerData != null) {
									final java.util.List<JIRAAction> actions = facade
											.getAvailableActions(jiraServerData, issue);

									JiraIssueCachedAdapter.get(issue).setCachedActions(actions);
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

		private RunJiraActionGroup getAvailableActionsGroup() {
			return (RunJiraActionGroup) ActionManager
					.getInstance().getAction("ThePlugin.JiraIssues.RunActionGroup");
		}

		public Object getData(@NonNls final String dataId) {
			if (dataId.equals(Constants.ISSUE)) {
				return params.issue;
			}
			if (dataId.equals(Constants.SERVER)) {
				return getJiraServerData();
			}

			if (dataId.equals(Constants.STATUS_BAR_PANE)) {
				return statusBarPane;
			}
			return null;
		}

        private class DetailsPanel extends JPanel {

			private JLabel affectsVersions = new JLabel("Fetching...");
			private EditableIssueField affectsVersionsEditLabel;
			private JLabel fixVersions = new JLabel("Fetching...");
			private EditableIssueField fixVersionsEditLabel;
			private JLabel components = new JLabel("Fetching...");
			private EditableIssueField componentsEditLabel;
			private JLabel affectsVersionsLabel = new BoldLabel("Affects Version/s");
			private JLabel fixVersionsLabel = new BoldLabel("Fix Version/s");
			private JLabel componentsLabel = new BoldLabel("Component/s");
			private JLabel originalEstimate = new JLabel("Fetching...");
			private EditableIssueField originalEstimateEditLabel;
			private JLabel remainingEstimate = new JLabel("Fetching...");
			private EditableIssueField remainingEstimateEditLabel;
			private JLabel timeSpent = new JLabel("Fetching...");
			private EditableIssueField issueType;
			private JLabel issueStatus;
			private EditableIssueField issuePriority;
            private EditableIssueField issueAssigneeEditLabel;
			private UserLabel issueAssignee = new UserLabel();
			private EditableIssueField issueReporterEditLabel;
			private UserLabel issueReporter = new UserLabel();
			private JLabel issueResolution;
			private JLabel issueCreationTime;
			private JLabel issueUpdateTime;
            private JEditorPane issueEnvironment;
			private EditableIssueField issueEnvironmentEditLabel;
			private static final float SPLIT_RATIO = 0.3f;
			private static final int SUBTASKS_LABEL_HEIGHT = 24;
            private IssueLinksPanel issueLinksPanel;
            private static final int ISSUELINKS_LABEL_HEIGHT = 14;

            public void fillIssueLinksPanelWithIssues(Collection<JiraIssueAdapter> issues) {
                issueLinksPanel.fillPanelWithIssues(issues);
            }

			protected EditableIssueField createEditableField(final JComponent component, final String fieldId,
					final String displayName) {
						return new EditableIssueField(component, new EditableIssueField.EditIssueFieldHandler() {
							public void handleClickedEditButton() {
								updateIssueField(params.issue, new JIRAActionFieldBean(fieldId, displayName));
							}
						}
				);
			}

			public DetailsPanel() {
				super(new BorderLayout());
				issueAssigneeEditLabel = createEditableField(issueAssignee, "assignee", "Assignee");
				issueReporterEditLabel = createEditableField(issueReporter, "reporter", "Reporter");
				subtaskListModel = new DefaultListModel();
				affectsVersionsEditLabel = createEditableField(affectsVersions, "versions", "Affects Versions");
				affectsVersionsEditLabel.setButtonVisible(false);
				fixVersionsEditLabel = createEditableField(fixVersions, "fixVersions", "Fix Versions");
				fixVersionsEditLabel.setButtonVisible(false);
				componentsEditLabel = createEditableField(components, "components", "Components");
				componentsEditLabel.setButtonVisible(false);
				originalEstimateEditLabel = createEditableField(originalEstimate, "timetracking", "Original Estimate");
				originalEstimateEditLabel.setButtonVisible(false);
				remainingEstimateEditLabel = createEditableField(remainingEstimate, "timetracking", "Remaining Estimate");
				remainingEstimateEditLabel.setButtonVisible(false);
				add(createBody(), BorderLayout.CENTER);
			}

			private JPanel createBody() {
				boolean hasSubTasks = params.issue.getSubTaskKeys().size() > 0;
                boolean hasIssueLinks = params.issue.getIssueLinks() != null
                        && params.issue.getIssueLinks().size() > 0;

				JPanel panel = new JPanel(new BorderLayout());

				JPanel details = createDetailsPanel();

                JComponent subtasks = hasSubTasks ? createSubtasksPanel() : null;
                issueLinksPanel = hasIssueLinks ? new IssueLinksPanel(this) : null;

                JComponent secondComponent = subtasks != null ? subtasks : issueLinksPanel;
                if (subtasks != null && issueLinksPanel != null) {
                    Splitter split = new Splitter(false, 0.5f);
                    split.setHonorComponentsMinimumSize(true);
                    split.setFirstComponent(subtasks);
                    split.setSecondComponent(issueLinksPanel);
                    secondComponent = split;
                }
                if (secondComponent != null) {
                    JScrollPane scrollPane = new JScrollPane(details);
                    scrollPane.setBackground(Color.WHITE);
                    Splitter split = new Splitter(false, SPLIT_RATIO);
                    split.setFirstComponent(scrollPane);
                    split.setSecondComponent(secondComponent);
					split.setHonorComponentsMinimumSize(true);
                    panel.add(split, BorderLayout.CENTER);
				} else {
					panel.setOpaque(true);
					panel.setBackground(Color.WHITE);
                    JScrollPane scrollPane = new JScrollPane(details);
                    scrollPane.setBackground(Color.WHITE);
                    panel.add(scrollPane, BorderLayout.CENTER);
				}

				return panel;
			}

            public JList createListForDisplayingIssues() {
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
                return list;
            }

            private DefaultListModel issueLinksListModel = new DefaultListModel();

            public class IssueLinksPanel extends JPanel {
                private Map<String, DefaultListModel> issuesToModelMap = new HashMap<String, DefaultListModel>();

                public void fillPanelWithIssues(Collection<JiraIssueAdapter> issues) {
                   for (JiraIssueAdapter issue : issues) {
                       issuesToModelMap.get(issue.getKey()).addElement(issue);
                   }
                }

                public void createSubPanels() {
                    issuesToModelMap.clear();
                    setLayout(new GridBagLayout());
                    removeAll();

                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.gridx = 0;
                    gbc.gridy = 0;
                    gbc.weightx = 1.0;
                    gbc.weighty = 1.0;
                    gbc.fill = GridBagConstraints.BOTH;

                    JLabel label = new JLabel("Issue links");
                    Dimension size = new Dimension(label.getPreferredSize().width, SUBTASKS_LABEL_HEIGHT);
                    label.setMinimumSize(size);
                    label.setPreferredSize(size);
                    gbc.weighty = 0.0;
                    this.add(label, gbc);
                    gbc.gridy++;

                    JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
                    this.add(separator, gbc);
                    gbc.gridy++;


                    Map<String, Map<String, List<String>>> issueLinks = params.issue.getIssueLinks();
                    if (issueLinks != null) {
                        for (Map<String, List<String>> issueLinkType : issueLinks.values()) {
                            for (String linkDescription : issueLinkType.keySet()) {
                                label = new JLabel(linkDescription);
                                label.setPreferredSize(
                                        new Dimension(label.getPreferredSize().width, ISSUELINKS_LABEL_HEIGHT));
                                gbc.weighty = 0.0;
                                this.add(label, gbc);
                                gbc.gridy++;

                                final JList list = createListForDisplayingIssues();
                                DefaultListModel model = new DefaultListModel();
                                list.setModel(model);

                                for (String issueKey : issueLinkType.get(linkDescription)) {
                                    issuesToModelMap.put(issueKey, model);
                                }

                                JScrollPane scrollPane = new JScrollPane(list);
                                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                                gbc.weighty = 1.0;
                                this.add(scrollPane, gbc);
                                gbc.gridy++;
                            }
                        }
                    }
                }

                public IssueLinksPanel(final DetailsPanel detailsPanel) {
                    super();
                    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                    this.setOpaque(true);
                    this.setBackground(Color.WHITE);

                    createSubPanels();

                    List<String> keys = new ArrayList<String>();
                    Map<String, Map<String, List<String>>> issueLinks = params.issue.getIssueLinks();
                    if (issueLinks != null) {
                        for (Map<String, List<String>> value : issueLinks.values()) {
                            for (List<String> value2 : value.values()) {
                                for (String issueKey : value2) {
                                    keys.add(issueKey);
                                }
                            }
                        }
                    }

                    if (keys.size() > 0) {
                        if (getIssueLinksTask == null) {
                            getIssueLinksTask = new FetchTaskLinksBackgroundTask(keys, issueLinksListModel, detailsPanel);
                            ProgressManager.getInstance().run(getIssueLinksTask);
                        }
                    }
                }
            }

            private class FetchSubtasksBackgroundTask extends Task.Backgroundable {
                private List<String> keys;
                protected DefaultListModel model;
                protected List<JiraIssueAdapter> subtasks = new ArrayList<JiraIssueAdapter>();

                public FetchSubtasksBackgroundTask(final List<String> keys, final DefaultListModel model,
                        final String title) {
                    super(project, title, true);
                    this.keys = keys;
                    this.model = model;
                }

                public void run(@NotNull ProgressIndicator progressIndicator) {
                    Collection<JiraIssueAdapter> subtasksInModel = params.model.getSubtasks(params.issue);
                    Map<String, JiraIssueAdapter> subKeysInModel = new HashMap<String, JiraIssueAdapter>();
                    for (JiraIssueAdapter sub : subtasksInModel) {
                        subKeysInModel.put(sub.getKey(), sub);
                    }
                    for (String key : keys) {
                        try {
                            if (subKeysInModel.keySet().contains(key)) {
                                subtasks.add(subKeysInModel.get(key));
                            } else {
                                JiraIssueAdapter subtask = facade.getIssue(params.issue.getJiraServerData(), key);
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
                    model.clear();
                    for (JiraIssueAdapter subtask : subtasks) {
                        model.addElement(subtask);
                    }
                    getSubTasksTask = null;
                }
            }

            class FetchTaskLinksBackgroundTask extends FetchSubtasksBackgroundTask {
                private final DetailsPanel detailsPanel;

                public FetchTaskLinksBackgroundTask(final List<String> keys, final DefaultListModel model,
                        final DetailsPanel detailsPanel) {
                    super(keys, model, "Fetching issuelinks for issue " + params.issue.getKey());
                    this.detailsPanel = detailsPanel;
                }

                @Override
                public void onCancel() {
                    getIssueLinksTask = null;
                }

                @Override
                public void onSuccess() {
                    detailsPanel.fillIssueLinksPanelWithIssues(subtasks);
                    getIssueLinksTask = null;
                }
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

				panel.setOpaque(true);
                panel.setBackground(Color.WHITE);

				final java.util.List<String> keys = params.issue.getSubTaskKeys();
				if (keys.size() > 0) {
					final JList list = createListForDisplayingIssues();
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
						getSubTasksTask = new FetchSubtasksBackgroundTask(keys, subtaskListModel,
                                "Fetching subtasks for issue " + params.issue.getKey());
						ProgressManager.getInstance().run(getSubTasksTask);
					}
				}
				return panel;
			}

			private void openSelectedSubtask(JList list) {
				Object o = list.getSelectedValue();
				if (o != null && o instanceof JiraIssueAdapter) {
					IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(project);
					if (panel != null) {
						panel.openIssue(((JiraIssueAdapter) o).getKey(), params.issue.getJiraServerData(), false);
					}
				}
			}

			private JPanel createDetailsPanel() {
				JPanel panel = new ScrollablePanel();

				panel.setLayout(new GridBagLayout());
                panel.setOpaque(true);
                panel.setBackground(Color.WHITE);
                
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
							IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(project);
							if (panel != null) {
								panel.openIssue(params.issue.getParentIssueKey(), params.issue.getJiraServerData(), false);
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
				panel.add(issueAssigneeEditLabel, gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				panel.add(new BoldLabel("Reporter"), gbc1);
				panel.add(issueReporterEditLabel, gbc2);
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
                String env = params.issue.getEnvironment();
                if (env != null && env.length() > 0) {
                    panel.add(new BoldLabel("Environment"), gbc1);
                    panel.add(issueEnvironment, gbc2);
//					panel.add(issueEnvironmentEditLabel, gbc2);
                    gbc1.gridy++;
                    gbc2.gridy++;
                }
				panel.add(affectsVersionsLabel, gbc1);
				panel.add(affectsVersionsEditLabel, gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				panel.add(fixVersionsLabel, gbc1);
				panel.add(fixVersionsEditLabel, gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				panel.add(componentsLabel, gbc1);
				panel.add(componentsEditLabel, gbc2);

				gbc1.gridy++;
				gbc2.gridy++;
				panel.add(new BoldLabel("Original Estimate"), gbc1);
				panel.add(originalEstimateEditLabel, gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				panel.add(new BoldLabel("Remaining Estimate"), gbc1);
				panel.add(remainingEstimateEditLabel, gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				panel.add(new BoldLabel("Time Spent"), gbc1);
				panel.add(timeSpent, gbc2);

				addFillerPanel(panel, gbc1, false);

				return panel;
			}

			private void fillBaseIssueDetails() {
				issueType = createEditableField(new JLabel(params.issue.getType(),
								CachedIconLoader.getIcon(params.issue.getTypeIconUrl()),
								SwingConstants.LEFT), "issuetype", "Type");
				issueStatus = new JLabel(params.issue.getStatus(),
						CachedIconLoader.getIcon(params.issue.getStatusTypeUrl()),
						SwingConstants.LEFT);
				issuePriority = createEditableField(new JLabel(params.issue.getPriority(),
								CachedIconLoader.getIcon(params.issue.getPriorityIconUrl()),
								SwingConstants.LEFT), "priority", "Priority");
				// bleeeee :( - assignee ID (String value) equals "-1" for unassigned issues. Oh my...
				if (params.issue.getAssigneeId().equals("-1")) {
					issueAssignee.setText("Unassigned");
				} else {
					issueAssignee.setUserName(params.issue.getServerUrl(), params.issue.getAssignee(),
							params.issue.getAssigneeId(), true);
				}
				if (params.issue.getReporterId().equals("-1")) {
					issueReporter.setText("Anonymous");
				} else {
					issueReporter.setUserName(params.issue.getServerUrl(), params.issue.getReporter(),
							params.issue.getReporterId(), true);
				}
//				issueResolution = createEditableField(new JLabel(params.issue.getResolution()), "resolution", "Resolution");
				issueResolution = new JLabel(params.issue.getResolution());
				issueCreationTime = new JLabel(JiraTimeFormatter.formatTimeFromJiraTimeString(params.issue.getCreated()));
				issueUpdateTime = new JLabel(JiraTimeFormatter.formatTimeFromJiraTimeString((params.issue.getUpdated())));
                issueEnvironment = new JEditorPane();
                issueEnvironment.setMargin(new Insets(0, 0, 0, 0));
                issueEnvironment.setText(Html2text.translate(params.issue.getEnvironment()));
//				issueEnvironmentEditLabel = createEditableField(issueEnvironment, "environment", "Environment");
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
						if (params != null && params.issue != null && params.issue.getJiraServerData() != null) {
							// damn it! the XML view of the list of issues does not
							// have estimates and time spent :(

							final JiraIssueAdapter issueDetails =
                                    facade.getIssueDetails(params.issue.getJiraServerData(), params.issue);
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
							boolean isError = (errorString != null);
							affectsVersionsEditLabel.setButtonVisible(!isError);
							fixVersionsEditLabel.setButtonVisible(!isError);
							componentsEditLabel.setButtonVisible(!isError);
							if (!isError) {
								colorLabels(Color.BLACK);

								setAffectsVersions(getStringArray(params.issue.getAffectsVersions()));
								setFixVersions(getStringArray(params.issue.getFixVersions()));
								setComponents(getStringArray(params.issue.getComponents()));
								setOriginalEstimate(params.issue.getOriginalEstimate());
								setRemainingEstimate(params.issue.getRemainingEstimate());
								setTimeSpent(params.issue.getTimeSpent());
							} else {
								colorLabels(Color.RED);
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

				private void colorLabels(Color color) {
					getAffectVersionsLabel().setForeground(color);
					getFixVersionsLabel().setForeground(color);
					getComponentsLabel().setForeground(color);
					originalEstimate.setForeground(color);
					remainingEstimate.setForeground(color);
					timeSpent.setForeground(color);
				}
			}

			private void setTimeSpent(String t) {
				boolean isTimeNull = (t == null);
				if (isTimeNull) {
					timeSpent.setText("None");
				} else {
					timeSpent.setText(t);
				}
				originalEstimateEditLabel.setButtonVisible(isTimeNull);
				remainingEstimateEditLabel.setButtonVisible(!isTimeNull);
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

			private Map<JiraIssueAdapter, JIRAIssueListOrTreeRendererPanel> rendererMap =
					new HashMap<JiraIssueAdapter, JIRAIssueListOrTreeRendererPanel>();

			private class SubtaskListCellRenderer extends DefaultListCellRenderer {
				public Component getListCellRendererComponent(JList list, Object value, int index,
						boolean isSelected, boolean cellHasFocus) {
					if (value != null && value instanceof JiraIssueAdapter) {
						JiraIssueAdapter issue = (JiraIssueAdapter) value;
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
							if (e.getDescription().equals("edit")) {
								updateIssueField(params.issue, new JIRAActionFieldBean("summary", "Summary"));
							} else {
								BrowserUtil.launchBrowser(e.getURL().toString());
							}
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
						+ params.issue.getKey() + "</a> " + params.issue.getSummary()
						+ " <i><a href=\"edit\">edit</a></i></body></html>";
				summary.setText(txt);
			}
		}

		public int incrementStackTraceCounter() {
			return ++stackTraceCounter;
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
                final ServerId id = params.issue.getJiraServerData().getServerId();
                CommentPanel p = new CommentPanel(comments.getComponents().length + 1, c,
                        projectCfgManager.getJiraServerr(id), tabs, IssuePanel.this);
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
								if (params != null && params.issue != null && params.issue.getJiraServerData() != null) {
									facade.addComment(params.issue.getJiraServerData(), params.issue.getKey(),
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

                int selectedIndex = tabs.getSelectedIndex();
				while (tabs.getTabCount() > 3) {
					tabs.remove(3);
				}

				String stack = Html2text.translate(params.issue.getDescription());
				if (StackTraceDetector.containsStackTrace(stack)) {
					tabs.add("Stack Trace: Description", new StackTracePanel(stack, project));
				}
                if (selectedIndex < tabs.getTabCount()) {
                    tabs.setSelectedIndex(selectedIndex);
                }
			}

			private class RefreshDescriptionAndCommentsRunnable implements Runnable {
				public void run() {
					try {
						if (params != null && params.issue != null && params.issue.getJiraServerData() != null) {
							java.util.List<JIRAComment> cmts = null;

							JiraIssueAdapter oneIssue = facade.getIssue(params.issue.getJiraServerData(),
                                    params.issue.getKey());
							if (oneIssue != null) {
								descriptionPanel.setDescription(oneIssue.getDescription());
								cmts = oneIssue.getComments();
							}

							if (cmts == null) {
								// oh well, no comments in XML - can it even happen? Fall back to SOAP
								cmts = facade.getComments(params.issue.getJiraServerData(), params.issue);
							}

							for (JIRAComment c : cmts) {
								try {
									JIRAUserBean u = RecentlyOpenIssuesCache.JIRAUserNameCache.getInstance()
											.getUser(getJiraServerData(), c.getAuthor());
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
                        statusBarPane.setErrorMessage("Unable to retrieve comments", e);
					}
				}
			}
		}

		class MyTransferHandler extends TransferHandler {

			public boolean canImport(JComponent comp, DataFlavor[] flavors) {
				return true;
			}

			public boolean importData(JComponent comp, Transferable t) {

				IssueDetailsToolWindow panel = IdeaHelper.getIssueDetailsToolWindow(project);
				if (panel == null) {
					return false;
				}

				for (DataFlavor flavor : t.getTransferDataFlavors()) {
					try {
						if (flavor.equals(DataFlavor.javaFileListFlavor)) {
							List l = (List) t.getTransferData(DataFlavor.javaFileListFlavor);
							Iterator iter = l.iterator();
							while (iter.hasNext()) {
								File file = (File) iter.next();
								panel.addAttachment(file);
							}
							return true;
						} else if (flavor.equals(DataFlavor.stringFlavor)) {
							String str = (String) t.getTransferData(flavor);
							String[] files = str.split("\r\n");
							for (String fileOrURL : files) {
								try {
									URL url = new URL(fileOrURL);
									File file = new File(url.toURI());
									panel.addAttachment(file);
								} catch (MalformedURLException ex) {
									continue;
								} catch (URISyntaxException e) {
									continue;
								}
							}
							return true;
						}
					} catch (IOException ex) {
						return false;
					} catch (UnsupportedFlavorException e) {
						return false;
					}
				}
				return true;
			}
		};

        private final class AttachementsPanel extends JPanel {

            private static final float SPLIT_RATIO = 0.6f;
            private JTabbedPane tabs;
            private int tabIndex;

            private AttachementsPanel(JTabbedPane tabs, int tabIndex) {
                this.tabs = tabs;
                this.tabIndex = tabIndex;
				setTransferHandler(transferHandler);
            }

			private TransferHandler transferHandler = new MyTransferHandler();

            public void refresh() {
                tabs.setTitleAt(tabIndex, "Refreshing attachments...");
                Runnable r = new Runnable() {
                    public void run() {
                        try {
                            final Collection<JIRAAttachment> atts =
                                    facade.getIssueAttachements(params.issue.getJiraServerData(), params.issue);

                            // get user names in the cache if they are not already there
                            for (JIRAAttachment att : atts) {
                                RecentlyOpenIssuesCache.JIRAUserNameCache.getInstance()
                                        .getUser(params.issue.getJiraServerData(), att.getAuthor());
                            }

                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    removeAll();
                                    fillContent(atts);
                                }
                            });
                        } catch (JIRAException e) {
                            setErrorMessage("Unable to retrieve attachements", e);
                        } catch (JiraUserNotFoundException e) {
                            setErrorMessage("Unable to retrieve attachement's author", e);
                        }
                    }
                };
                Thread t = new Thread(r, "Retrieving attachements for issue " + params.issue.getKey());
                t.start();
            }

            private class AttachmentListModel extends DefaultListModel {
                public AttachmentListModel(Collection<JIRAAttachment> attachments) {
                    int i = 0;
                    for (JIRAAttachment attachment : attachments) {
                        add(i++, attachment);
                    }
                }
            }

            private JEditorPane previewEditor;

            public void fillContent(Collection<JIRAAttachment> attachments) {
                if (attachments == null || attachments.size() == 0) {
					setLayout(new BorderLayout());
					JLabel label = new JLabel("No attachements in " + params.issue.getKey());
					label.setHorizontalAlignment(SwingConstants.CENTER);
                    add(label, BorderLayout.PAGE_START);
					label.setTransferHandler(transferHandler);
					label = new JLabel("Drag files here to attach them");
					label.setHorizontalAlignment(SwingConstants.CENTER);
					add(label, BorderLayout.CENTER);
					label.setTransferHandler(transferHandler);
                    tabs.setTitleAt(tabIndex, "Attachments(0)");
                    return;
                }

                tabs.setTitleAt(tabIndex, "Attachments(" + attachments.size() + ")");

                Splitter split = new Splitter(false, SPLIT_RATIO);
                split.setShowDividerControls(true);

                JPanel listPanel = new JPanel();
                listPanel.setLayout(new BorderLayout());

                final JList attachmentList = new JList() {
                    @Override
                    public boolean getScrollableTracksViewportWidth() {
                        return true;
                    }
                };
                attachmentList.setModel(new AttachmentListModel(attachments));
                attachmentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                attachmentList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        if (e.getValueIsAdjusting()) {
                            return;
                        }
                        if (attachmentList.getSelectionModel().isSelectionEmpty()) {
                            attachmentList.setToolTipText(null);
                            fillPreview(null);
                        } else {
                            attachmentList.setToolTipText("Press \"Enter\" or double-click to open in the browser");
                            fillPreview((JIRAAttachment) attachmentList.getSelectedValue());
                        }
                    }
                });
                attachmentList.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2 && !attachmentList.getSelectionModel().isSelectionEmpty()) {
                            launchAttachment((JIRAAttachment) attachmentList.getSelectedValue());
                        }
                    }
                });
                attachmentList.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER
                                && !attachmentList.getSelectionModel().isSelectionEmpty()) {
                            launchAttachment((JIRAAttachment) attachmentList.getSelectedValue());
                        }
                    }
                });

                attachmentList.setCellRenderer(new ListCellRenderer() {
                    public Component getListCellRendererComponent(JList list, Object value, int index,
                            boolean isSelected, boolean cellHasFocus) {
                        try {
                            ATTACHMENT_RENDERER_PANEL.setAttachment(params.issue, (JIRAAttachment) value);
                        } catch (Exception e) {
                            // ignore - not really possible
                        }
                        ATTACHMENT_RENDERER_PANEL.setSelected(isSelected);
                        ATTACHMENT_RENDERER_PANEL.validate();
                        return ATTACHMENT_RENDERER_PANEL;
                    }
                });
                final JScrollPane scrollList = new JScrollPane(attachmentList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                previewEditor = new JEditorPane();
                previewEditor.setEditable(false);
                previewEditor.setOpaque(true);
                previewEditor.setBackground(Color.WHITE);
                previewEditor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
                previewEditor.addHyperlinkListener(new HyperlinkListener() {
                    public void hyperlinkUpdate(HyperlinkEvent e) {
                        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                            BrowserUtil.launchBrowser(e.getURL().toString());
                        }
                    }
                });

                final JScrollPane scrollPreview = new JScrollPane(previewEditor,
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                listPanel.add(scrollList, BorderLayout.CENTER);

                split.setFirstComponent(listPanel);

                JPanel attViewPanel = new JPanel();
                attViewPanel.setLayout(new BorderLayout());
                attViewPanel.add(new JLabel("Preview"), BorderLayout.NORTH);
                attViewPanel.add(scrollPreview, BorderLayout.CENTER);

                split.setSecondComponent(attViewPanel);
                split.setShowDividerControls(false);

                setLayout(new BorderLayout());
                add(split, BorderLayout.CENTER);

                if (attachments.size() == 1) {
                    attachmentList.getSelectionModel().setSelectionInterval(0, 0);
                }
                validate();
            }

            private void fillPreview(JIRAAttachment a) {
                previewEditor.setContentType("text/html");
                if (a == null) {
                    previewEditor.setText("<html><body><br><br><center>Nothing selected</center></body></html>");
                } else if (a.getMimetype().startsWith("image/")) {
                    previewEditor.setText(
                            "<html><body><center><a href=\"" + constructAttachmentUrl(a, false)
                                    + "\"><img src=\"" + constructAttachmentUrl(a, true)
                                    + "\" alt=\"" + constructAttachmentUrl(a, false) + "\"></a></center></body></html>");
                } else if (a.getMimetype().startsWith("text/")) {
                    try {
                        Document doc = previewEditor.getDocument();
                        doc.putProperty(Document.StreamDescriptionProperty, null);
                        previewEditor.setText("Loading attachment text...");
                        previewEditor.setPage(constructAttachmentUrl(a, true));
                    } catch (IOException e) {
                        LoggerImpl.getInstance().error(e);
                    }
                } else {
                    previewEditor.setText(
                            "<html><body><center>Unable to preview attachment type <b>" + a.getMimetype()
                                    + "</b><br><a href=\"" + constructAttachmentUrl(a, false)
                                    + "\">Click here to open the attachment in the browser</a></center></body></html>");
                }
            }

            private void launchAttachment(JIRAAttachment a) {
                BrowserUtil.launchBrowser(constructAttachmentUrl(a, false));
            }

            private String constructAttachmentUrl(JIRAAttachment a, boolean appendAuth) {
                StringBuilder sb = new StringBuilder();
                sb.append(params.issue.getServerUrl())
                        .append("/secure/attachment/").append(a.getId())
                        .append("/").append(a.getFilename());
                if (appendAuth) {
                    sb.append("?os_username=").append(params.issue.getJiraServerData().getUsername());
                    sb.append("&os_password=").append(params.issue.getJiraServerData().getPassword());
                }
                return sb.toString();
            }

            private void setErrorMessage(final String message, final Exception e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        removeAll();
                        setLayout(new BorderLayout());
                        tabs.setTitleAt(tabIndex, "Unable to retrieve attachments");
                        JLabel label = new JLabel(message + ": " + e.getMessage());
                        label.setHorizontalAlignment(SwingConstants.CENTER);
                        add(label, BorderLayout.CENTER);
                    }
                });
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



//		private class LocalConfigListener extends ConfigurationListenerAdapter {
//
//			public void jiraServersChanged(final ProjectConfiguration newConfiguration) {
//				(params.issue).setJiraServerData(IdeaHelper.getProjectCfgManager(project).
//                        getJiraServerr(params.issue.getJiraServerData().getServerId()));
//            }
//
//		}

		private class LocalModelListener implements JIRAIssueListModelListener {

			public void issueUpdated(final JiraIssueAdapter issue) {
			}

			public void modelChanged(final JIRAIssueListModel model) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						retrieveIssueFromModel();
						issueReloaded();
					}
				});
			}

			public void issuesLoaded(final JIRAIssueListModel model, final int loadedIssues) {

			}
		}
	}

    private static final AttachmentRendererPanel ATTACHMENT_RENDERER_PANEL = new AttachmentRendererPanel();

    private static final class AttachmentRendererPanel extends JPanel {

        private SelectableLabel name = new SelectableLabel(true, true, "NOTHING YET", ROW_HEIGHT, false, false);
        private SelectableLabel author = new SelectableLabel(true, true, "NOTHING HERE ALSO", ROW_HEIGHT, true, false);
        private SelectableLabel date = new SelectableLabel(true, true, "NEITHER HERE", ROW_HEIGHT, false, false);

        private AttachmentRendererPanel() {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.weightx = 1.0;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(name, gbc);
            gbc.gridx++;
            gbc.weightx = 0.0;
            gbc.anchor = GridBagConstraints.LINE_END;
            gbc.fill = GridBagConstraints.NONE;
            author.setHorizontalAlignment(SwingConstants.RIGHT);
            add(author, gbc);
            gbc.gridx++;
            add(date, gbc);
        }

        void setAttachment(JiraIssueAdapter issue, JIRAAttachment attachment) throws JIRAException, JiraUserNotFoundException {
            name.setText(" " + attachment.getFilename());
            JIRAUserBean u = RecentlyOpenIssuesCache.JIRAUserNameCache.getInstance()
                    .getUser(issue.getJiraServerData(), attachment.getAuthor());
            author.setText(u.getName());
            DateFormat dfo = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
            String commitDate = dfo.format(attachment.getCreated().getTime());
            date.setText(", " + commitDate + " ");
        }

        void setSelected(boolean selected) {
            name.setSelected(selected);
            author.setSelected(selected);
            date.setSelected(selected);
        }
    }

	public static void addFillerPanel(JPanel parent, GridBagConstraints gbc, boolean horizontal) {
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
		filler.setBackground(parent.getBackground());
//		filler.setBorder(BorderFactory.createEmptyBorder());
		filler.setOpaque(false);
		parent.add(filler, gbc);
	}
}