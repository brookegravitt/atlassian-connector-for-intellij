package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.commons.util.UrlUtil;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.atlassian.theplugin.idea.jira.SearchIssueDialog;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public class QuickSearchAction extends AnAction {
	@Override
	public void actionPerformed(AnActionEvent e) {
		final Project project = IdeaHelper.getCurrentProject(e.getDataContext());
		if (project == null) {
			return;
		}

		IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(project);

		if (panel == null) {
			return;
		}

		final JiraServerData server = panel.getSelectedServer();
		if (server != null) {
			SearchIssueDialog dlg = new SearchIssueDialog(project);
			dlg.show();
			if (dlg.isOK()) {
				String query = dlg.getSearchQueryString();
				if (query != null) {
					if (query.toUpperCase().matches("[A-Z]+\\-\\d+")) {
						panel.openIssue(query.toUpperCase(), server, false);

					} else {
                        panel.openTextQuery(server, query);
					}
				}
			}
		}
	}

	@Override
	public void update(AnActionEvent event) {
		IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(event);

		boolean enabled = panel != null && panel.getSelectedServer() != null;
		event.getPresentation().setEnabled(enabled);
	}
}
