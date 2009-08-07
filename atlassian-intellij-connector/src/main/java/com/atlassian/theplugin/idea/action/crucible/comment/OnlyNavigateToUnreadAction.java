package com.atlassian.theplugin.idea.action.crucible.comment;

import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.ReviewDetailsToolWindow;

/**
 * User: kalamon
 * Date: Aug 7, 2009
 * Time: 1:00:07 PM
 */
public class OnlyNavigateToUnreadAction extends ToggleAction {

    public boolean isSelected(AnActionEvent event) {
        ReviewDetailsToolWindow panel = IdeaHelper.getReviewDetailsToolWindow(event);
        return panel != null && panel.getToggleOnlyUnreadCommentsNavigation();
    }

    public void setSelected(AnActionEvent event, boolean b) {
        ReviewDetailsToolWindow panel = IdeaHelper.getReviewDetailsToolWindow(event);
        if (panel != null) {
            panel.setToggleOnlyUnreadCommentsNavigation(b);
        }
    }
}
