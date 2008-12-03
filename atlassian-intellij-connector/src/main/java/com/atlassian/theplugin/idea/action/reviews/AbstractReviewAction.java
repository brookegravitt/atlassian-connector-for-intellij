package com.atlassian.theplugin.idea.action.reviews;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.commons.crucible.api.model.Action;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.idea.crucible.CrucibleChangeStateWorker;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.Constants;

/**
 * User: jgorycki
 * Date: Dec 3, 2008
 * Time: 2:36:22 PM
 */
public abstract class AbstractReviewAction extends AnAction {
	protected abstract Action getRequestedAction();

	public void actionPerformed(final AnActionEvent event) {
		ReviewAdapter review = event.getData(Constants.REVIEW_KEY);
		if (review != null) {
			new CrucibleChangeStateWorker(IdeaHelper.getCurrentProject(event), review, getRequestedAction()).run();
		}
	}

	public void update(AnActionEvent event) {
		super.update(event);
		ReviewAdapter review = event.getData(Constants.REVIEW_KEY);
		if (review == null) {
			event.getPresentation().setEnabled(false);
		} else {
			try {
				if (review.getActions().isEmpty()) {
					event.getPresentation().setEnabled(false);
					event.getPresentation().setVisible(false);
				} else {
					for (Action action : review.getActions()) {
						if (action.equals(getRequestedAction())) {
							event.getPresentation().setEnabled(true);
							event.getPresentation().setVisible(true);
							break;
						} else {
							event.getPresentation().setEnabled(false);
							event.getPresentation().setVisible(false);
						}
					}
				}
			} catch (ValueNotYetInitialized valueNotYetInitialized) {
				valueNotYetInitialized.printStackTrace();
			}
		}
	}
}
