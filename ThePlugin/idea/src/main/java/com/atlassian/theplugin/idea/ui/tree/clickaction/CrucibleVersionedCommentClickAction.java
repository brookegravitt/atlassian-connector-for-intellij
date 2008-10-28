package com.atlassian.theplugin.idea.ui.tree.clickaction;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewListenerImpl;
import com.atlassian.theplugin.idea.crucible.events.CrucibleEvent;
import com.atlassian.theplugin.idea.crucible.events.FocusOnLineCommentEvent;
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.intellij.openapi.project.Project;

public class CrucibleVersionedCommentClickAction implements AtlassianClickAction {
	private Project project;

	public CrucibleVersionedCommentClickAction(Project project) {
		this.project = project;
	}

	public void execute(final AtlassianTreeNode node, final int noOfClicks) {
		VersionedCommentTreeNode anode = (VersionedCommentTreeNode) node;
		CrucibleEvent event = new FocusOnLineCommentEvent(CrucibleReviewListenerImpl.ANONYMOUS,
					anode.getReview(), anode.getFile(), anode.getComment(), noOfClicks > 1);
		IdeaHelper.getReviewActionEventBroker(project).trigger(event);
	}

}
