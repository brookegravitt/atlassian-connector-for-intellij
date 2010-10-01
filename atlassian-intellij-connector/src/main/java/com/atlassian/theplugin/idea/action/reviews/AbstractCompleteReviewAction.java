package com.atlassian.theplugin.idea.action.reviews;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.CrucibleCompleteWorker;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: jgorycki
 * Date: Dec 3, 2008
 * Time: 2:29:34 PM
 */
public abstract class AbstractCompleteReviewAction extends AnAction {

	protected abstract CrucibleAction getRequestedAction();

	protected abstract boolean getCompletionStatus();

	@Override
	public void actionPerformed(final AnActionEvent event) {
		ReviewAdapter review = event.getData(Constants.REVIEW_KEY);
		if (review != null) {
			new CrucibleCompleteWorker(IdeaHelper.getCurrentProject(event), review, getCompletionStatus()).run();
		}
	}

	@Override
	public void update(AnActionEvent event) {
		super.update(event);
		ReviewAdapter review = event.getData(Constants.REVIEW_KEY);
		if (review == null) {
			event.getPresentation().setEnabled(false);
		} else {
			if (review.getActions().contains(getRequestedAction())) {
				for (Reviewer reviewer : review.getReviewers()) {
					if (reviewer.getUsername().equals(review.getServerData().getUsername())) {
						if (reviewer.isCompleted() == !getCompletionStatus()) {
							event.getPresentation().setEnabled(true);
							event.getPresentation().setVisible(true);
						} else {
							event.getPresentation().setEnabled(false);
							event.getPresentation().setVisible(false);
						}
						break;
					}
				}
			} else {
				event.getPresentation().setEnabled(false);
				event.getPresentation().setVisible(false);
			}
		}
	}
}
