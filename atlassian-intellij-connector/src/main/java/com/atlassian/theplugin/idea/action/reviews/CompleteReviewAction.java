package com.atlassian.theplugin.idea.action.reviews;

import com.atlassian.theplugin.commons.crucible.api.model.Action;

/**
 * User: jgorycki
 * Date: Dec 3, 2008
 * Time: 2:18:25 PM
 */
public class CompleteReviewAction extends AbstractCompleteReviewAction {
	protected Action getRequestedAction() {
		return Action.COMPLETE;
	}

	protected boolean getCompletionStatus() {
		return true;
	}
}
