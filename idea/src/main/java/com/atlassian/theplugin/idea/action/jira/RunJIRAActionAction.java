package com.atlassian.theplugin.idea.action.jira;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ide.BrowserUtil;
import com.atlassian.theplugin.jira.api.JIRAAction;
import com.atlassian.theplugin.jira.api.JIRAIssue;

public class RunJIRAActionAction extends AnAction {

	private JIRAIssue issue;
	private JIRAAction action;

	public RunJIRAActionAction(JIRAIssue issue, JIRAAction jiraAction) {
		super(jiraAction.getName());
		this.issue = issue;
		action = jiraAction;
	}

	public void actionPerformed(AnActionEvent event) {
		launchBrowser();
	}

	public void launchBrowser() {
		BrowserUtil.launchBrowser(issue.getServerUrl()
			+ "/secure/WorkflowUIDispatcher.jspa?id="
			+ issue.getId()
			+ "&"
			+ action.getQueryStringFragment());
	}

}
