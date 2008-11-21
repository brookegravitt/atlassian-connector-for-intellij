package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.idea.jira.JiraIssueAdapter;
import com.atlassian.theplugin.jira.JIRAIssueProgressTimestampCache;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilderImpl;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilder;
import com.atlassian.theplugin.jira.api.JIRAAction;
import com.atlassian.theplugin.jira.api.JIRAActionField;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import java.util.List;

public class RunIssueActionAction extends AnAction {
	private final JIRAIssue issue;
	private JIRAAction action;
	private JIRAServerFacade facade;
	private IssuesToolWindowPanel window;

	public RunIssueActionAction(IssuesToolWindowPanel toolWindow, JIRAServerFacade facade,
	                            JIRAIssue issue, JIRAAction jiraAction) {
		super(jiraAction.getName());
		this.issue = issue;
		action = jiraAction;
		window = toolWindow;
		this.facade = facade;
	}

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
			try {
				window.setStatusMessage(
						"Retrieving fields for action \""
								+ action.getName()
								+ "\" in issue "
								+ issue.getKey()
								+ "...");
				JIRAIssueListModelBuilder builder =
						IdeaHelper.getProjectComponent(project, JIRAIssueListModelBuilderImpl.class);
				if (builder == null) {
					return;
				}

				JiraServerCfg server = builder.getServer();
				if (server != null) {
					List<JIRAActionField> fields = facade.getFieldsForAction(server, issue, action);
					if (fields.isEmpty()) {
						window.setStatusMessage(
								"Running action \""
										+ action.getName()
										+ "\" on issue "
										+ issue.getKey()
										+ "...");
						facade.progressWorkflowAction(server, issue, action);
						if (action.getId() == Constants.JiraActionId.START_PROGRESS.getId()) {
							JIRAIssueProgressTimestampCache.getInstance().setTimestamp(server, issue);
						} else if (action.getId() == Constants.JiraActionId.STOP_PROGRESS.getId()) {
							JIRAIssueProgressTimestampCache.getInstance().removeTimestamp(server, issue);
						}
						JIRAIssueListModelBuilder issueListModelBuilder =
								IdeaHelper.getProjectComponent(project, JIRAIssueListModelBuilderImpl.class);
						JiraIssueAdapter.get(issue).clearCachedActions();
						window.setStatusMessage(
								"Action \""
										+ action.getName()
										+ "\" on issue "
										+ issue.getKey()
										+ " run succesfully");

						if (issueListModelBuilder != null) {
							issueListModelBuilder.updateIssue(issue);
						}
					} else {
						window.setStatusMessage(
								"Action \""
										+ action.getName()
										+ "\" on issue "
										+ issue.getKey()
										+ " is interactive, launching browser");
						launchBrowser();
					}
				}
			} catch (JIRAException e) {
				window.setStatusMessage(
						"Unable to run action "
								+ action.getName()
								+ " on issue "
								+ issue.getKey()
								+ ": "
								+ e.getMessage(),
						true);
			}
		}
	}
}
