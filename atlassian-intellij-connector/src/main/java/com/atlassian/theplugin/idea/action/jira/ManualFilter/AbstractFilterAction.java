package com.atlassian.theplugin.idea.action.jira.ManualFilter;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * @author pmaruszak
 * @date Aug 14, 2009
 */
public abstract class AbstractFilterAction extends AnAction {


    abstract boolean isEnabled(AnActionEvent event);

    @Override
        public void update(AnActionEvent event) {
          final IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(event);


        if (panel != null && panel.getSelectedServer() != null
                && !panel.getJiraFilterTree().isRecentlyOpenSelected() && isEnabled(event)) {
            event.getPresentation().setEnabled(true);
        } else {
            event.getPresentation().setEnabled(false);
        }



        }          
}
