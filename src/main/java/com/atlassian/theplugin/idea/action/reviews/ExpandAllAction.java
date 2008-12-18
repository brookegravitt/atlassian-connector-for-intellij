package com.atlassian.theplugin.idea.action.reviews;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.ReviewsToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: jgorycki
 * Date: Dec 5, 2008
 * Time: 2:26:30 PM
 */
public class ExpandAllAction extends AbstractCrucibleToolbarAction {
	public void actionPerformed(AnActionEvent e) {
		ReviewsToolWindowPanel panel = IdeaHelper.getReviewsToolWindowPanel(e);
		if (panel != null) {
			panel.expandAllRightTreeNodes();
		}
	}

	protected boolean onUpdate(AnActionEvent e) {
		return true;
	}
}
