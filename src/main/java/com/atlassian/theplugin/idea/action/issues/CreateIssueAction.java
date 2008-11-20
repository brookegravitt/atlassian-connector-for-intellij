package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class CreateIssueAction extends JIRAAbstractAction {
	public void actionPerformed(AnActionEvent e) {
		IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(e);
		if (panel != null) {
			panel.createIssue();
		}
	}

	public void onUpdate(AnActionEvent event) {
//		Project project = IdeaHelper.getCurrentProject(event.getDataContext());
//		JIRAIssueListModelBuilder builder = IdeaHelper.getProjectComponent(project, JIRAIssueListModelBuilderImpl.class);
//		boolean enabled = builder != null && builder.getServer() != null;
//		event.getPresentation().setEnabled(enabled);enabled
	}
}
