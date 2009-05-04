package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.UrlUtil;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;

public class QuickSearchAction extends AnAction {
	@Override
	public void actionPerformed(AnActionEvent e) {
		final Project project = IdeaHelper.getCurrentProject(e.getDataContext());
		if (project == null) {
			return;
		}

		IssueListToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(project);

		if (panel == null) {
			return;
		}

		final ServerData server = panel.getSelectedServer();
		if (server != null) {
			String query = Messages.showInputDialog(project,
					"Quick Search (entering just issue key will open this issue directly in IDE):",
					"Search", IconLoader.getIcon("/actions/find.png"));
			if (query != null) {
				if (query.matches("[A-Z]+\\-\\d+")) {
					panel.openIssue(query, server);

				} else {
					BrowserUtil.launchBrowser(server.getUrl()
							+ "/secure/QuickSearch.jspa?searchString=" + UrlUtil.encodeUrl(query));
				}
			}
		}
	}

	@Override
	public void update(AnActionEvent event) {
		IssueListToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(event);

		boolean enabled = panel != null && panel.getSelectedServer() != null;
		event.getPresentation().setEnabled(enabled);
	}
}
