package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.events.ShowDiffEvent;
import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;

public class DiffRevisionsAction extends ReviewTreeAction {

	protected void executeTreeAction(final AtlassianTreeWithToolbar tree) {
		ReviewActionData actionData = new ReviewActionData(tree);
		if (actionData.review != null && actionData.file != null) {
			IdeaHelper.getReviewActionEventBroker()
					.trigger(new ShowDiffEvent(CrucibleReviewActionListener.ANONYMOUS, actionData.file));
		}
	}
}
