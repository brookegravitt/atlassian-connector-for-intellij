package com.atlassian.theplugin.idea.action.issues;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ide.BrowserUtil;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.commons.util.UrlUtil;

public class QuickSearchAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(e);
        Project project = IdeaHelper.getCurrentProject(e.getDataContext());
        JIRAServer jiraServer = panel != null ? panel.getCurrentJIRAServer() : null;

        if (jiraServer != null) {
            String query = Messages.showInputDialog(project, "Quick Search:",
                    "Search", IconLoader.getIcon("/actions/find.png"));
            if (query != null) {
                BrowserUtil.launchBrowser(jiraServer.getServer().getUrl()
                        + "/secure/QuickSearch.jspa?searchString=" + UrlUtil.encodeUrl(query));
            }
        }
	}

	public void update(AnActionEvent e) {
		super.update(e);
		IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(e);
        JIRAServer jiraServer = panel != null ? panel.getCurrentJIRAServer() : null;
		e.getPresentation().setEnabled(jiraServer != null);
	}
}
