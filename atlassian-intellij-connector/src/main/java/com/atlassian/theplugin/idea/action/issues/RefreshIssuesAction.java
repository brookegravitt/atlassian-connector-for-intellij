package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilder;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilderImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

/**
 * User: pmaruszak
 */
public class RefreshIssuesAction extends JIRAAbstractAction {
	@Override
	public void actionPerformed(final AnActionEvent e) {
		final IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(e);
		if (panel != null) {
			panel.refreshIssues();
		}
	}

	public void onUpdate(AnActionEvent event) {
		final Project project = IdeaHelper.getCurrentProject(event.getDataContext());
		JIRAIssueListModelBuilder builder = IdeaHelper.getProjectComponent(project, JIRAIssueListModelBuilderImpl.class);
		boolean enabled = builder != null && builder.getServer() != null;
		event.getPresentation().setEnabled(enabled);
	}
}
