package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.events.ShowFileEvent;
import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ShowFileAction extends ReviewTreeAction {

	protected void executeTreeAction(final AtlassianTreeWithToolbar tree) {
		ReviewActionData actionData = new ReviewActionData(tree);
		if (actionData.review != null && actionData.file != null) {
			IdeaHelper.getReviewActionEventBroker()
					.trigger(new ShowFileEvent(CrucibleReviewActionListener.ANONYMOUS, actionData.review, actionData.file));
		}
	}
}