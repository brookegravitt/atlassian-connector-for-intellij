package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeListManager;

public class CreateChangeListAction extends AnAction {
	@Override
	public void actionPerformed(AnActionEvent anActionEvent) {
		final IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(anActionEvent);
		final JIRAIssue issue = anActionEvent.getData(Constants.ISSUE_KEY);
		if (panel != null && issue != null) {
			panel.createChangeListAction(issue);
		}
	}

	@Override
	public void update(AnActionEvent anActionEvent) {
	    super.update(anActionEvent);

		final JIRAIssue issue = anActionEvent.getData(Constants.ISSUE_KEY);
		anActionEvent.getPresentation().setEnabled(issue != null);

	    if (issue != null) {
	        String changeListName = issue.getKey() + " - " + issue.getSummary();
			final Project project = anActionEvent.getData(DataKeys.PROJECT);
	        if (ChangeListManager.getInstance(project).findChangeList(changeListName) == null) {
	            anActionEvent.getPresentation().setText("Create ChangeList");
	        } else {
	            anActionEvent.getPresentation().setText("Activate ChangeList");
	        }
	    }
	}
}
