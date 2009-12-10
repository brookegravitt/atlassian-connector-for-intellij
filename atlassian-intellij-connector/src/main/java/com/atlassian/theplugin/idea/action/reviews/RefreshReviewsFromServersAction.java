package com.atlassian.theplugin.idea.action.reviews;

import com.atlassian.theplugin.crucible.model.UpdateReason;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.ReviewListToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: pmaruszak
 */
public class RefreshReviewsFromServersAction extends AbstractCrucibleToolbarAction {
                             	@Override
	public void actionPerformed(final AnActionEvent e) {
		final ReviewListToolWindowPanel panel = IdeaHelper.getReviewListToolWindowPanel(e);
		if (panel != null) {
			panel.refresh(UpdateReason.REFRESH);
		}
	}
}
