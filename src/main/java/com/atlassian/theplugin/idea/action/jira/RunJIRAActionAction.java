package com.atlassian.theplugin.idea.action.jira;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ide.BrowserUtil;
import com.atlassian.theplugin.jira.api.JIRAAction;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAActionField;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.idea.jira.JiraIssueAdapter;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ui.AbstractTableToolWindowPanel;

import java.util.List;

public class RunJIRAActionAction extends AnAction {

	private JiraIssueAdapter adapter;
	private JIRAAction action;
	private JIRAServerFacade facade;
	private AbstractTableToolWindowPanel window;

	public RunJIRAActionAction(AbstractTableToolWindowPanel toolWindow,
							   JIRAServerFacade facade, JiraIssueAdapter issueAdapter, JIRAAction jiraAction) {
		super(jiraAction.getName());
		adapter = issueAdapter;
		action = jiraAction;
		window = toolWindow;
		this.facade = facade;
	}

	public void actionPerformed(AnActionEvent event) {
		runIssueActionOrLaunchBrowser();
	}

	public void runIssueActionOrLaunchBrowser() {
		new Thread(new Runnable() {
			public void run() {
				try {
					window.setStatusMessage(
						"Retrieving fields for action \""
							+ action.getName()
							+ "\" in issue "
							+ adapter.getKey()
							+ "...");
					List<JIRAActionField> fields =
						facade.getFieldsForAction(
							IdeaHelper.getCurrentJIRAServer().getServer(), adapter.getIssue(), action);
					if (fields.isEmpty()) {
						window.setStatusMessage(
							"Running action \""
							+ action.getName()
							+ "\" on issue "
							+ adapter.getKey()
							+ "...");
						// todo transition
					} else {
						window.setStatusMessage(
							"Action \""
								+ action.getName()
								+ "\" on issue "
								+ adapter.getKey()
								+ " is interactive, launching browser");
						launchBrowser();
					}
				} catch (JIRAException e) {
					window.setStatusMessage(
						"Unable to run action "
							+ action.getName()
							+ " on issue "
							+ adapter.getKey()
							+ ": "
							+ e.getMessage(),
						true);
				}
			}
		})
		.start();
	}

	public void launchBrowser() {
		adapter.clearCachedActions();
		BrowserUtil.launchBrowser(adapter.getServerUrl()
			+ "/secure/WorkflowUIDispatcher.jspa?id="
			+ adapter.getId()
			+ "&"
			+ action.getQueryStringFragment());
	}

}
