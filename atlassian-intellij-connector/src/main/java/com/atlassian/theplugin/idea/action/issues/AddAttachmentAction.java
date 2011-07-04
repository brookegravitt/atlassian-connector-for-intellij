package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class AddAttachmentAction extends JIRAAbstractAction {
	public void actionPerformed(AnActionEvent event) {
		IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(event);
		if (panel == null) {
			return;
		}
		JiraIssueAdapter issue = panel.getSelectedIssue();

		panel.addAttachmentToIssue(issue);
	}

	public void onUpdate(AnActionEvent event) {
	}

}
