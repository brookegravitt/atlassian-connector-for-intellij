package com.atlassian.theplugin.idea.action.issues;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilder;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilderImpl;

public class CreateChangeListAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(e);
		if (panel != null) {
			panel.createChangeListAction();
		}
	}

	public void update(AnActionEvent anActionEvent) {
	    super.update(anActionEvent);

		JIRAIssueListModelBuilder builder = IdeaHelper.getProjectComponent(
				IdeaHelper.getCurrentProject(anActionEvent), JIRAIssueListModelBuilderImpl.class);
		if (builder == null || builder.getModel() == null) {
			anActionEvent.getPresentation().setEnabled(false);
			return;
		}
		JIRAIssue issue = builder.getModel().getSelectedIssue();

	    if (issue != null) {
	        String changeListName = issue.getKey() + " - " + issue.getSummary();

	        Project project = DataKeys.PROJECT.getData(anActionEvent.getDataContext());
	        if (ChangeListManager.getInstance(project).findChangeList(changeListName) == null) {
	            anActionEvent.getPresentation().setText("Create ChangeList");
	        } else {
	            anActionEvent.getPresentation().setText("Activate ChangeList");
	        }
	    }
	}
}
