package com.atlassian.theplugin.idea.action.reviews;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.CrucibleReviewWindow;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.atlassian.theplugin.idea.crucible.CrucibleConstants;
import com.atlassian.theplugin.idea.crucible.ReviewsToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/**
 * User: jgorycki
 * Date: Dec 3, 2008
 * Time: 12:28:16 PM
 */
public class OpenReviewAction extends AbstractCrucibleToolbarAction {
	public void actionPerformed(AnActionEvent e) {
		Project project = IdeaHelper.getCurrentProject(e);

		if (project != null && e.getPlace().equals(ReviewsToolWindowPanel.PLACE_PREFIX + project.getName())) {
			if (!VcsIdeaHelper.isUnderVcsControl(e)) {
				Messages.showInfoMessage(project, CrucibleConstants.CRUCIBLE_MESSAGE_NOT_UNDER_VCS,
						CrucibleConstants.CRUCIBLE_TITLE_NOT_UNDER_VCS);

			} else {
				ReviewAdapter review = e.getData(Constants.REVIEW_KEY);
				if (review != null) {
					CrucibleReviewWindow.getInstance(project).showCrucibleReviewWindow(review);
				}
			}
		}
	}

	@Override
	public boolean onUpdate(AnActionEvent e) {
		return e.getData(Constants.REVIEW_KEY) != null;
	}
}
