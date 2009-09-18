package com.atlassian.theplugin.idea.action.crucible.comment;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.ReviewDetailsToolWindow;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * User: kalamon
 * Date: Aug 7, 2009
 * Time: 1:00:07 PM
 */
public class OnlyNavigateToUnreadAction extends ToggleAction {
    private final static Icon ON_ICON = IconLoader.getIcon("/icons/crucible/comments/ico_comment_toggle_skip.png");
    private final static Icon OFF_ICON = IconLoader.getIcon("/icons/crucible/comments/ico_comment_toggle_unskip.png");

    @Override
    public void update(AnActionEvent event) {
        ReviewDetailsToolWindow panel = IdeaHelper.getReviewDetailsToolWindow(event);
        if (panel != null && panel.getToggleOnlyUnreadCommentsNavigation()) {
            event.getPresentation().setIcon(ON_ICON);
            event.getPresentation().setDescription("Skip Read Comments");
        } else {
            event.getPresentation().setIcon(OFF_ICON);
            event.getPresentation().setDescription("All Comments");
        }
    }

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
