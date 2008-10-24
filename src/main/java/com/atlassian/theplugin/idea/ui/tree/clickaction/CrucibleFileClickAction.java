package com.atlassian.theplugin.idea.ui.tree.clickaction;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListenerImpl;
import com.atlassian.theplugin.idea.crucible.comments.ReviewActionEventBroker;
import com.atlassian.theplugin.idea.crucible.events.FocusOnFileComments;
import com.atlassian.theplugin.idea.crucible.events.ShowFileEvent;
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.intellij.openapi.project.Project;

public class CrucibleFileClickAction implements AtlassianClickAction {
	private Project project;
	private ReviewAdapter review;
	private CrucibleFileInfo file;

	public CrucibleFileClickAction(Project project, ReviewAdapter review, CrucibleFileInfo file) {
		this.project = project;
		this.review = review;
		this.file = file;
	}

	public void execute(AtlassianTreeNode node, int noOfClicks) {
		ReviewActionEventBroker broker = IdeaHelper.getReviewActionEventBroker(project);
		switch (noOfClicks) {
			case 1:
				broker.trigger(
						new FocusOnFileComments(CrucibleReviewActionListenerImpl.ANONYMOUS, review, file));
				break;
			case 2:
				broker.trigger(new ShowFileEvent(CrucibleReviewActionListenerImpl.ANONYMOUS, review, file));
				break;
			default:
				break;
		}
	}

}
