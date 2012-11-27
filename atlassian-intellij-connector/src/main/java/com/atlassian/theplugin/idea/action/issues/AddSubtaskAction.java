package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: kalamon
 * Date: 27.11.12
 * Time: 14:37
 */
public class AddSubtaskAction extends JIRAAbstractAction {
    @Override
    public void onUpdate(AnActionEvent e) {
        final JiraIssueAdapter issue = e.getData(Constants.ISSUE_KEY);
        boolean enabled = issue != null && issue.usesRest() && !issue.isSubTask();
        e.getPresentation().setEnabled(enabled);
        e.getPresentation().setVisible(enabled);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final JiraIssueAdapter issue = e.getData(Constants.ISSUE_KEY);
        if (issue == null || !issue.usesRest() || issue.isSubTask()) {
            return;
        }
        IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(e);
        if (panel != null) {
            panel.createSubtask(issue);
        }
    }
}
