package com.atlassian.theplugin.idea.action.reviews;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.crucible.CrucibleJoinReviewWorker;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;

/**
 * User: jgorycki
 * Date: Dec 3, 2008
 * Time: 2:34:48 PM
 */
public class JoinReviewAction extends AnAction {
	public void actionPerformed(final AnActionEvent event) {
		final ReviewAdapter review = event.getData(Constants.REVIEW_KEY);
		if (review != null) {
			new Thread(new Runnable() {
				public void run() {
					ApplicationManager.getApplication().invokeAndWait(
							new CrucibleJoinReviewWorker(review),
							ModalityState.defaultModalityState());
				}
			}).start();
		}
	}

	public void update(final AnActionEvent event) {
		final ReviewAdapter review = event.getData(Constants.REVIEW_KEY);
		if (review == null) {
			event.getPresentation().setEnabled(false);
		} else {
			if (review.isAllowReviewerToJoin()) {
				try {
					String userName = review.getServerData().getUsername();
					if (review.getAuthor().getUsername().equals(userName)) {
						event.getPresentation().setVisible(false);
						event.getPresentation().setEnabled(false);
						return;
					}
					if (review.getModerator().getUsername().equals(userName)) {
						event.getPresentation().setVisible(false);
						event.getPresentation().setEnabled(false);
						return;
					}

					for (Reviewer reviewer : review.getReviewers()) {
						if (userName.equals(reviewer.getUsername())) {
							event.getPresentation().setVisible(false);
							event.getPresentation().setEnabled(false);
							return;
						}
					}
				} catch (ValueNotYetInitialized valueNotYetInitialized) {
					event.getPresentation().setVisible(false);
					event.getPresentation().setEnabled(false);
				}
			} else {
				event.getPresentation().setVisible(false);
				event.getPresentation().setEnabled(false);
			}
		}
	}
}
