package com.atlassian.theplugin.idea.ui.tree.clickaction;

import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.crucible.comments.ReviewActionEventBroker;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.events.FocusOnFileComments;
import com.atlassian.theplugin.idea.crucible.events.ShowFileEvent;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.intellij.openapi.project.Project;

public class CrucibleFileClickAction implements AtlassianClickAction {
	private Project project;
	private ReviewData review;
	private CrucibleFileInfo file;

	public CrucibleFileClickAction(Project project, ReviewData review, CrucibleFileInfo file) {
		this.project = project;
		this.review = review;
		this.file = file;
	}

	public void execute(AtlassianTreeNode node, int noOfClicks) {
		ReviewActionEventBroker broker = IdeaHelper.getReviewActionEventBroker(project);
		switch (noOfClicks) {
			case 1:
				broker.trigger(
						new FocusOnFileComments(CrucibleReviewActionListener.ANONYMOUS, review, file));
				break;
			case 2:
				broker.trigger(new ShowFileEvent(CrucibleReviewActionListener.ANONYMOUS, review, file));
				break;
			default:
				break;
		}
	}

}
