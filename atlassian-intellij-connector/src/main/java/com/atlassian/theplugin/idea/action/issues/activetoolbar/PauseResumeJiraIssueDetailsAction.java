package com.atlassian.theplugin.idea.action.issues.activetoolbar;

import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: kalamon
 * Date: Aug 12, 2009
 * Time: 5:14:17 PM
 */
public class PauseResumeJiraIssueDetailsAction extends PauseResumeJiraIssueAction {
    @Override
    public void onUpdate(AnActionEvent event, boolean enabled) {
        final JiraIssueAdapter selectedIssue = ActiveIssueUtils.getSelectedJiraIssue(event);
        updateState(isSelectedIssueActive(event, selectedIssue), event);
    }
}
