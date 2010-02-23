package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: pmaruszak
 */
public class RefreshIssuesAction extends JIRAAbstractAction {
	@Override
	public void actionPerformed(final AnActionEvent e) {
		final IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(e);
		if (panel != null) {
            e.getPresentation().setEnabled(false);
			panel.refreshIssues(true);
		}
	}

    @Override
    public void onUpdate(AnActionEvent event) {        
    }
}
