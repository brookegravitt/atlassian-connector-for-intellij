package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.PluginToolWindow;
import com.atlassian.theplugin.idea.jira.IssueCreate;
import com.atlassian.theplugin.jira.JIRAServer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public class CreateIssueAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        Project project = IdeaHelper.getCurrentProject(e.getDataContext());
        JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer();

        String errorMsg = null;

        if (jiraServer != null) {
            IssueCreate issueCreate = new IssueCreate(IdeaHelper.getCurrentJIRAServer(),
					IdeaHelper.getAppComponent().getJiraServerFacade());
            issueCreate.show();
        } else {
            errorMsg = "Select a JIRA server before creating issues.";
        }

        if (errorMsg != null) {
            PluginToolWindow.focusPanel(e, PluginToolWindow.ToolWindowPanels.JIRA);
            Messages.showErrorDialog(project, errorMsg, "Create JIRA Issue");
        }
    }

	public void update(AnActionEvent event) {
		super.update(event);
		if (IdeaHelper.getCurrentJIRAServer() != null) {
			event.getPresentation().setEnabled(IdeaHelper.getCurrentJIRAServer().isValidServer());
		} else {
			event.getPresentation().setEnabled(false);
		}
	}
}