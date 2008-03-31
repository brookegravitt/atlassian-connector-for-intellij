package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.PluginToolWindow;
import com.atlassian.theplugin.idea.jira.IssueComment;
import com.atlassian.theplugin.idea.jira.JIRAToolWindowPanel;
import com.atlassian.theplugin.idea.jira.JiraIssueAdapter;
import com.atlassian.theplugin.jira.JIRAServer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.util.List;

public class CommentIssueAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        Project project = IdeaHelper.getCurrentProject(e.getDataContext());
        JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer();
        JIRAToolWindowPanel toolWindowPanel = IdeaHelper.getJIRAToolWindowPanel(e);

        String errorMsg = null;

        if (jiraServer != null) {
            List<JiraIssueAdapter> l = toolWindowPanel.getIssues();
            if (l.isEmpty()) {
                errorMsg = "Search for issues to comment on first.";
            } else {
				IssueComment issueComment = new IssueComment(
						IdeaHelper.getAppComponent().getJiraServerFacade(),
						IdeaHelper.getCurrentJIRAServer(), l);

                if (toolWindowPanel.getCurrentIssue() != null) {
                    issueComment.setIssue(toolWindowPanel.getCurrentIssue());
                }
                issueComment.show();
            }
        } else {
            errorMsg = "Select a JIRA server and query for issues before commenting.";
        }


        if (errorMsg != null) {
            PluginToolWindow.focusPanel(e, PluginToolWindow.ToolWindowPanels.JIRA);
            Messages.showErrorDialog(project, errorMsg, "JIRA Comment Issue");
        }
    }
}
