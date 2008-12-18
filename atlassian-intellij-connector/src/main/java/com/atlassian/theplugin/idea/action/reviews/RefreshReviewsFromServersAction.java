package com.atlassian.theplugin.idea.action.reviews;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.ReviewsToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: pmaruszak
 */
public class RefreshReviewsFromServersAction extends AbstractCrucibleToolbarAction {

	@Override
	public void actionPerformed(final AnActionEvent e) {
		final ReviewsToolWindowPanel panel = IdeaHelper.getReviewsToolWindowPanel(e);
		if (panel != null) {
			panel.refresh();
		}
	}

	protected boolean onUpdate(AnActionEvent e) {
		return true;
	}
}
