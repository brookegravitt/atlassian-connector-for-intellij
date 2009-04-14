package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueActionProvider;
import com.atlassian.theplugin.idea.jira.JiraIssueAdapter;
import com.atlassian.theplugin.idea.jira.PerformIssueActionForm;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.jira.JIRAIssueProgressTimestampCache;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JiraActionFieldType;
import com.atlassian.theplugin.jira.api.JIRAAction;
import com.atlassian.theplugin.jira.api.JIRAActionField;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilder;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

import java.awt.*;
import java.util.List;

public class RunIssueActionAction extends AnAction {
	private final JIRAIssue issue;
	private JIRAAction action;
	private JIRAServerFacade facade;
	private IssueActionProvider window;
	private JIRAIssueListModelBuilder jiraIssueListModelBuilder;

	public RunIssueActionAction(IssueActionProvider toolWindow, JIRAServerFacade facade,
			JIRAIssue issue, JIRAAction jiraAction, final JIRAIssueListModelBuilder jiraIssueListModelBuilder) {
		super(jiraAction.getName());
		this.issue = issue;
		action = jiraAction;
		window = toolWindow;
		this.facade = facade;
		this.jiraIssueListModelBuilder = jiraIssueListModelBuilder;
	}

	@Override
	public void actionPerformed(AnActionEvent event) {
		runIssueActionOrLaunchBrowser(IdeaHelper.getCurrentProject(event));
	}

	public void runIssueActionOrLaunchBrowser(Project project) {
		ProgressManager.getInstance().run(new IssueActionOrLaunchBrowserRunnable(project));
	}

	public void launchBrowser() {
		JiraIssueAdapter.get(issue).clearCachedActions();
		BrowserUtil.launchBrowser(issue.getServerUrl()
				+ "/secure/WorkflowUIDispatcher.jspa?id="
				+ issue.getId()
				+ "&"
				+ action.getQueryStringFragment());
	}

	private class IssueActionOrLaunchBrowserRunnable extends Task.Modal {
		private Project project;

		IssueActionOrLaunchBrowserRunnable(Project project) {
			super(project, "Running Issue Action", true);
			this.project = project;
		}

		public void run(final ProgressIndicator indicator) {
			showInfo("Retrieving fields for action \"" + action.getName() + "\" in issue " + issue.getKey() + "...", false);

			final JiraServerCfg server = issue.getServer();

			if (server != null) {
				final List<JIRAActionField> fields;
				try {
					fields = facade.getFieldsForAction(server, issue, action);
				} catch (JIRAException e) {
					showInfo(
							"Cannot retrieve fields for action [" + action.getName() + "] on issue [" + issue.getKey() + "]"
									+ e.getMessage(), true);
					showInfo(e);
					return;
				}

				showInfo("Retrieving issue details", false);
				final JIRAIssue detailedIssue;
				try {
					JIRAIssue issueWithTime = facade.getIssue(issue.getServer(), issue.getKey());
					detailedIssue = facade.getIssueDetails(issue.getServer(), issueWithTime);
				} catch (JIRAException e) {
					showInfo("Cannot retrieve issue details for [" + issue.getKey() + "]: " + e.getMessage(), true);
					showInfo(e);
					return;
				}

				showInfo("Running action [" + action.getName() + "] on issue [" + issue.getKey() + "]...", false);


				showInfo("Retrieving values for action fields", false);
				final List<JIRAActionField> preFilleddfields = JiraActionFieldType.fillFieldValues(detailedIssue, fields);

				if (preFilleddfields.isEmpty()) {
					try {
						facade.progressWorkflowAction(server, issue, action);
						performPostActionActivity(server);
					} catch (JIRAException e) {
						showInfo("Unable to run action [" + action.getName() + "] on issue [" + issue.getKey() + "]: "
								+ e.getMessage(), true);
						showInfo(e);
					}
				} else {

					EventQueue.invokeLater(new Runnable() {
						public void run() {
							// show action fields dialog
							final PerformIssueActionForm dialog =
									new PerformIssueActionForm(project, detailedIssue, preFilleddfields, action.getName());
							dialog.show();
							if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
								// perform workflow action in the background thread
								ProgressManager.getInstance().run(
										new Task.Backgroundable(project, "Running workflow action", false) {
											public void run(final ProgressIndicator indicator) {
												try {
													facade.progressWorkflowAction(server, issue, action, dialog.getFields());
												} catch (JIRAException e) {
													showInfo("Unable to run action [" + action.getName() + "] on issue ["
															+ issue.getKey() + "]: " + e.getMessage(), true);
													showInfo(e);
													return;
												}
												try {
													if (dialog.getComment() != null && dialog.getComment().length() > 0) {
														facade.addComment(server, issue.getKey(), dialog.getComment());
													}
												} catch (JIRAException e) {
													showInfo("Unable to add comment to action [" + action.getName()
															+ "] on issue [" + issue.getKey() + "]: " + e.getMessage(), true);
													showInfo(e);
												}
												try {
													performPostActionActivity(server);
												} catch (JIRAException e) {
													showInfo(e);
												}
											}
										});
							} else {
								showInfo("Running workflow action [" + action.getName() + "] cancelled", false);
							}
						}
					});
				}
			}
		}

		private void showInfo(final String s, final boolean isError) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					window.setStatusMessage(s, isError);

				}
			});
		}

		private void showInfo(final Throwable e) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					DialogWithDetails.showExceptionDialog(project, e.getMessage(), e);
				}
			});
		}

		/**
		 * Should be called in the background thread
		 *
		 * @param server
		 * @throws JIRAException
		 */
		private void performPostActionActivity(final JiraServerCfg server) throws JIRAException {
			if (action.getId() == Constants.JiraActionId.START_PROGRESS.getId()) {
				JIRAIssueProgressTimestampCache.getInstance().setTimestamp(server, issue);
			} else if (action.getId() == Constants.JiraActionId.STOP_PROGRESS.getId()) {
				JIRAIssueProgressTimestampCache.getInstance().removeTimestamp(server, issue);
			}

			JiraIssueAdapter.get(issue).clearCachedActions();


			if (jiraIssueListModelBuilder != null) {
				jiraIssueListModelBuilder.updateIssue(issue, server);
			}
			showInfo("Action [" + action.getName() + "] on issue " + issue.getKey() + " run succesfully", false);
		}
	}
}
