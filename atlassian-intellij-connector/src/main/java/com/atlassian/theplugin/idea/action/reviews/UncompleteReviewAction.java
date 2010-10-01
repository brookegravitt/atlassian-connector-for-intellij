package com.atlassian.theplugin.idea.action.reviews;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;

/**
 * @author jgorycki
 */
public class UncompleteReviewAction extends AbstractCompleteReviewAction {
	@Override
	protected CrucibleAction getRequestedAction() {
		return CrucibleAction.UNCOMPLETE;
	}

	@Override
	protected boolean getCompletionStatus() {
		return false;
	}
}
