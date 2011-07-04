package com.atlassian.theplugin.idea.action.issues.activetoolbar;

import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssueBean;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.IconLoader;

/**
 * User: kalamon
 * Date: Aug 12, 2009
 * Time: 2:36:37 PM
 */
public class PauseResumeJiraIssueAction extends AbstractActiveJiraIssueAction {
    public void onUpdate(AnActionEvent event) {
    }

    @Override
    public void onUpdate(AnActionEvent event, boolean enabled) {
        final JiraIssueAdapter selectedIssue = ActiveIssueUtils.getSelectedJiraIssue(event);
        updateState(isSelectedIssueActive(event, selectedIssue), event);
        
    }

    public void actionPerformed(AnActionEvent event) {
        JiraIssueAdapter selectedIssue = ActiveIssueUtils.getSelectedJiraIssue(event);
        if (selectedIssue != null && isSelectedIssueActive(event, selectedIssue)) {
            setIssuePaused(ActiveIssueUtils.getActiveJiraIssue(event));
        }
    }

    protected void setIssuePaused(ActiveJiraIssue issue) {
        if (issue != null) {
            ((ActiveJiraIssueBean) issue).setPaused(!issue.isPaused());
        }
    }

    protected void updateState(boolean selectedIssueActive, AnActionEvent event) {
        if (!selectedIssueActive) {
            event.getPresentation().setEnabled(false);
            event.getPresentation().setVisible(false);
        } else {
            final ActiveJiraIssue activeIssue = ActiveIssueUtils.getActiveJiraIssue(event);
            if (activeIssue != null) {
                event.getPresentation().setEnabled(true);
                event.getPresentation().setVisible(true);
                event.getPresentation().setText(activeIssue.isPaused() ? "Resume Work" : "Pause Work");
                event.getPresentation().setIcon(activeIssue.isPaused()
                        ? IconLoader.getIcon("/icons/ico_activateissue.png")
                        : IconLoader.getIcon("/icons/ico_pauseissue.png"));
            } else {
                event.getPresentation().setEnabled(false);
                event.getPresentation().setVisible(false);
            }
        }
    }
}
