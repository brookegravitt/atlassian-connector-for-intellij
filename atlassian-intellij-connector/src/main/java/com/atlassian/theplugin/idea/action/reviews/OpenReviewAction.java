package com.atlassian.theplugin.idea.action.reviews;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.atlassian.theplugin.idea.crucible.CrucibleConstants;
import com.atlassian.theplugin.idea.crucible.ReviewListToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/**
 * @author jgorycki
 */
public class OpenReviewAction extends AbstractCrucibleToolbarAction {
	@Override
	public void actionPerformed(AnActionEvent e) {
		Project project = IdeaHelper.getCurrentProject(e);

		if (project != null && e.getPlace().equals(ReviewListToolWindowPanel.PLACE_PREFIX + project.getName())) {
			if (!VcsIdeaHelper.isUnderVcsControl(e)) {
				Messages.showInfoMessage(project, CrucibleConstants.CRUCIBLE_MESSAGE_NOT_UNDER_VCS,
						CrucibleConstants.CRUCIBLE_TITLE_NOT_UNDER_VCS);

			} else {
				ReviewAdapter review = e.getData(Constants.REVIEW_KEY);
				if (review != null) {
					IdeaHelper.getReviewDetailsToolWindow(project).showReview(review, true);
				}
			}
		}
	}

	@Override
	public boolean onUpdate(AnActionEvent e) {
		return (e.getData(Constants.REVIEW_KEY) != null);
	}
}
