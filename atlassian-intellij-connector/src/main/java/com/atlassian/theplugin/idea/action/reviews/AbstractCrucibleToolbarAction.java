package com.atlassian.theplugin.idea.action.reviews;

import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.atlassian.theplugin.idea.crucible.ReviewListToolWindowPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;

/**
 * User: jgorycki
 * Date: Dec 18, 2008
 * Time: 2:36:35 PM
 */
public abstract class AbstractCrucibleToolbarAction extends AnAction {

	protected boolean onUpdate(AnActionEvent e) {
		return true;
	}

	protected void onUpdateFinished(AnActionEvent e, boolean enabled) {
	}

	@Override
	public final void update(AnActionEvent e) {
		super.update(e);
		Boolean windowEnabled = e.getData(Constants.REVIEW_WINDOW_ENABLED_KEY);

		if (windowEnabled == null) {
			// this is required for global action - if there is no focus
			// on any of the toolwindows, then we have to query the toolwindow directly
			ReviewListToolWindowPanel panel = IdeaHelper.getReviewListToolWindowPanel(e);
			windowEnabled = panel != null && (Boolean) panel.getData(Constants.REVIEW_WINDOW_ENABLED);
		}

		boolean result = false;
		if (windowEnabled != null && windowEnabled) {
			result = onUpdate(e);
		}

		Project project = e.getData(DataKeys.PROJECT);
		if (project != null) {
			if (!VcsIdeaHelper.isUnderVcsControl(e) && !result) {
				result = false;
			}
		} else {
			result = false;
		}

		e.getPresentation().setEnabled(result);
		onUpdateFinished(e, result);
	}
}
