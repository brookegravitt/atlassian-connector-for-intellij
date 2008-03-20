package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.PluginToolWindow;
import com.atlassian.theplugin.jira.JIRAServer;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;

import java.net.URLEncoder;

public class QuickSearchAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        Project project = IdeaHelper.getCurrentProject(e.getDataContext());
        JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer();

        if (jiraServer != null) {
            String query = Messages.showInputDialog(project, "Quick Search:",
                    "Search", IconLoader.getIcon("/actions/find.png"));
            if (query != null) {
                BrowserUtil.launchBrowser(jiraServer.getServer().getUrlString()
                        + "/secure/QuickSearch.jspa?searchString=" + URLEncoder.encode(query));
            }
        } else {
            PluginToolWindow.focusPanel(e, PluginToolWindow.ToolWindowPanels.JIRA);
            Messages.showErrorDialog(project, "Please select a JIRA server before searching.", "JIRA Quick Search");
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
