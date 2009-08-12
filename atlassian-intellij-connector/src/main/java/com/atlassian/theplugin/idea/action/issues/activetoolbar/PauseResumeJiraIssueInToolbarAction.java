package com.atlassian.theplugin.idea.action.issues.activetoolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: kalamon
 * Date: Aug 12, 2009
 * Time: 5:14:25 PM
 */
public class PauseResumeJiraIssueInToolbarAction extends PauseResumeJiraIssueAction {
    @Override
    public void onUpdate(AnActionEvent event, boolean enabled) {
        updateState(true, event);
    }
}
