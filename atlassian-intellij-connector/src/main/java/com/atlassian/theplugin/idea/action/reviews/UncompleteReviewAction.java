package com.atlassian.theplugin.idea.action.reviews;

import com.atlassian.theplugin.commons.crucible.api.model.Action;

/**
 * User: jgorycki
 * Date: Dec 3, 2008
 * Time: 2:18:30 PM
 */
public class UncompleteReviewAction extends AbstractCompleteReviewAction {
	protected Action getRequestedAction() {
		return Action.UNCOMPLETE;
	}

	protected boolean getCompletionStatus() {
		return false;
	}
}
