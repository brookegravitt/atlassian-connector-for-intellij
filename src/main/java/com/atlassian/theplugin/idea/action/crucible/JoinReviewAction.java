package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.CrucibleJoinReviewWorker;
import com.atlassian.theplugin.idea.crucible.ReviewDataImpl;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;


public class JoinReviewAction extends AnAction {
	private ReviewDataImpl rd;

	public void actionPerformed(final AnActionEvent event) {
		new Thread(new Runnable() {
			public void run() {
				ApplicationManager.getApplication().invokeAndWait(
						new CrucibleJoinReviewWorker(rd),
						ModalityState.defaultModalityState());
			}
		}).start();
	}

	public void update(final AnActionEvent event) {
		if (IdeaHelper.getCrucibleToolWindowPanel(event) != null) {
			if (IdeaHelper.getCrucibleToolWindowPanel(event).getSelectedReview() == null) {
				event.getPresentation().setEnabled(false);
			} else {
				rd = IdeaHelper.getCrucibleToolWindowPanel(event).getSelectedReview();
				if (rd.isAllowReviewerToJoin()) {
					try {
						String userName = rd.getServer().getUsername();
						if (rd.getAuthor().getUserName().equals(userName)) {
							event.getPresentation().setVisible(false);
							event.getPresentation().setEnabled(false);
							return;
						}
						if (rd.getModerator().getUserName().equals(userName)) {
							event.getPresentation().setVisible(false);
							event.getPresentation().setEnabled(false);
							return;
						}

						for (Reviewer reviewer : rd.getReviewers()) {
							if (userName.equals(reviewer.getUserName())) {
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
		} else {
			event.getPresentation().setEnabled(false);
			event.getPresentation().setVisible(false);
		}
	}
}
