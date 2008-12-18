package com.atlassian.theplugin.idea.action.reviews;

import com.atlassian.theplugin.idea.Constants;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: jgorycki
 * Date: Dec 18, 2008
 * Time: 2:36:35 PM
 */
public abstract class AbstractCrucibleToolbarAction extends AnAction {

	protected abstract boolean onUpdate(AnActionEvent e);

	@Override
	public final void update(AnActionEvent e) {
		super.update(e);
		Boolean windowEnabled = e.getData(Constants.REVIEW_WINDOW_ENABLED_KEY);
		boolean actionEnabled = onUpdate(e);
		if (windowEnabled != null && windowEnabled) {
			e.getPresentation().setEnabled(actionEnabled);
		} else {
			e.getPresentation().setEnabled(false);
		}
	}
}
