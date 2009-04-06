package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueActionProvider;
import com.atlassian.theplugin.idea.jira.JiraIssueAdapter;
import com.atlassian.theplugin.idea.jira.PerformIssueActionForm;
import com.atlassian.theplugin.jira.JIRAIssueProgressTimestampCache;
import com.atlassian.theplugin.jira.JIRAServerFacade;
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
		new IssueActionOrLaunchBrowserRunnable(project).run();
	}

	public void launchBrowser() {
		JiraIssueAdapter.get(issue).clearCachedActions();
		BrowserUtil.launchBrowser(issue.getServerUrl()
				+ "/secure/WorkflowUIDispatcher.jspa?id="
				+ issue.getId()
				+ "&"
				+ action.getQueryStringFragment());
	}

	private class IssueActionOrLaunchBrowserRunnable {
		private Project project;

		IssueActionOrLaunchBrowserRunnable(Project project) {
			this.project = project;
		}

		public void run() {
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
					return;
				}

				showInfo("Running action [" + action.getName() + "] on issue [" + issue.getKey() + "]...", false);

				if (fields.isEmpty()) {
					try {
						facade.progressWorkflowAction(server, issue, action);
						performPostActionActivity(server);
					} catch (JIRAException e) {
						showInfo("Unable to run action [" + action.getName() + "] on issue [" + issue.getKey() + "]: "
								+ e.getMessage(), true);
					}
				} else {
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							// show action fields dialog
							final PerformIssueActionForm dialog =
									new PerformIssueActionForm(project, issue, fields, action.getName());
							dialog.show();

							if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {

								// perform workflow action in the background thread
								ProgressManager.getInstance().run(
										new Task.Backgroundable(project, "Running workflow action", false) {
											public void run(final ProgressIndicator indicator) {
												try {
													facade.progressWorkflowAction(server, issue, action, dialog.getFields());
													performPostActionActivity(server);
												} catch (JIRAException e) {
													showInfo("Unable to run action [" + action.getName() + "] on issue ["
															+ issue.getKey() + "]: " + e.getMessage(), true);
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

			showInfo("Action [" + action.getName() + "] on issue " + issue.getKey() + " run succesfully", false);

			if (jiraIssueListModelBuilder != null) {
				jiraIssueListModelBuilder.updateIssue(issue, server);
			}
		}
	}
}
