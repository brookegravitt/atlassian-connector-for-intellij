package com.atlassian.theplugin.idea.action.jira;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ide.BrowserUtil;
import com.atlassian.theplugin.jira.api.JIRAAction;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.idea.jira.JiraIssueAdapter;

public class RunJIRAActionAction extends AnAction {

	private JiraIssueAdapter adapter;
	private JIRAAction action;

	public RunJIRAActionAction(JiraIssueAdapter issueAdapter, JIRAAction jiraAction) {
		super(jiraAction.getName());
		adapter = issueAdapter;
		action = jiraAction;
	}

	public void actionPerformed(AnActionEvent event) {
		launchBrowser();
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
