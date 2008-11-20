package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.util.UrlUtil;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilder;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilderImpl;
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

		JIRAIssueListModelBuilder builder = IdeaHelper.getProjectComponent(project, JIRAIssueListModelBuilderImpl.class);
		if (builder == null) {
			return;
		}

		final JiraServerCfg server = builder.getServer();
        if (server != null) {
            String query = Messages.showInputDialog(project, "Quick Search:",
                    "Search", IconLoader.getIcon("/actions/find.png"));
            if (query != null) {
				if (query.matches("[A-Z]+\\-\\d+")) {
					final IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(project);
					if (panel != null) {
						panel.openIssue(query);
					}

				} else {
					BrowserUtil.launchBrowser(server.getUrl()
							+ "/secure/QuickSearch.jspa?searchString=" + UrlUtil.encodeUrl(query));
				}
			}
        }
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		Project project = IdeaHelper.getCurrentProject(e.getDataContext());
		JIRAIssueListModelBuilder builder = IdeaHelper.getProjectComponent(project, JIRAIssueListModelBuilderImpl.class);
		boolean enabled = builder != null && builder.getServer() != null;
		e.getPresentation().setEnabled(enabled);
	}
}
