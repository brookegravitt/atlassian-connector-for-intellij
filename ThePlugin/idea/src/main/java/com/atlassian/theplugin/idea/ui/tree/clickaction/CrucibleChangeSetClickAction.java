package com.atlassian.theplugin.idea.ui.tree.clickaction;

import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.crucible.comments.ReviewActionEventBroker;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.events.FocusOnGeneralComments;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.intellij.openapi.project.Project;

public class CrucibleChangeSetClickAction implements AtlassianClickAction {

	private Project project;
	private Review review;

	public CrucibleChangeSetClickAction(Project project, Review review) {
		this.project = project;
		this.review = review;
	}

	public void execute(AtlassianTreeNode node, int noOfClicks) {
		switch (noOfClicks) {
			case 1:
			case 2:
				ReviewActionEventBroker broker = IdeaHelper.getReviewActionEventBroker(project);
				broker.trigger(new FocusOnGeneralComments(CrucibleReviewActionListener.ANONYMOUS, review));
				break;
			default:
		}
	}
}
