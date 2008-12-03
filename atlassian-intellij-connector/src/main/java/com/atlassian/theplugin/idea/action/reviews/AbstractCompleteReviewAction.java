package com.atlassian.theplugin.idea.action.reviews;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.Action;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.idea.crucible.CrucibleCompleteWorker;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.Constants;

/**
 * User: jgorycki
 * Date: Dec 3, 2008
 * Time: 2:29:34 PM
 */
public abstract class AbstractCompleteReviewAction extends AnAction {

	protected abstract Action getRequestedAction();

	protected abstract boolean getCompletionStatus();

	public void actionPerformed(final AnActionEvent event) {
		ReviewAdapter review = event.getData(Constants.REVIEW_KEY);
		if (review != null) {
			new CrucibleCompleteWorker(IdeaHelper.getCurrentProject(event), review, getCompletionStatus()).run();
		}
	}

	public void update(AnActionEvent event) {
		super.update(event);
		ReviewAdapter review = event.getData(Constants.REVIEW_KEY);
		if (review == null) {
			event.getPresentation().setEnabled(false);
		} else {
			try {
				if (review.getActions().contains(getRequestedAction())) {
					for (Reviewer reviewer : review.getReviewers()) {
						if (reviewer.getUserName().equals(review.getServer().getUsername())) {
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
			} catch (ValueNotYetInitialized valueNotYetInitialized) {
				valueNotYetInitialized.printStackTrace();
			}
		}
	}
}
