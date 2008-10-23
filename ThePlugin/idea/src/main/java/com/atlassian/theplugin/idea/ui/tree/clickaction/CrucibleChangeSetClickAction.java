package com.atlassian.theplugin.idea.ui.tree.clickaction;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.ReviewAdapter;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListenerImpl;
import com.atlassian.theplugin.idea.crucible.comments.ReviewActionEventBroker;
import com.atlassian.theplugin.idea.crucible.events.FocusOnGeneralComments;
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.intellij.openapi.project.Project;

public class CrucibleChangeSetClickAction implements AtlassianClickAction {

	private Project project;
	private ReviewAdapter review;

	public CrucibleChangeSetClickAction(Project project, ReviewAdapter review) {
		this.project = project;
		this.review = review;
	}

	public void execute(AtlassianTreeNode node, int noOfClicks) {
		switch (noOfClicks) {
			case 1:
			case 2:
				ReviewActionEventBroker broker = IdeaHelper.getReviewActionEventBroker(project);
				broker.trigger(new FocusOnGeneralComments(CrucibleReviewActionListenerImpl.ANONYMOUS, review));
				break;
			default:
		}
	}
}
