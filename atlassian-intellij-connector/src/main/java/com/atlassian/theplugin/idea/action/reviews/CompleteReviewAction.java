package com.atlassian.theplugin.idea.action.reviews;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;

/**
 * User: jgorycki
 * Date: Dec 3, 2008
 * Time: 2:18:25 PM
 */
public class CompleteReviewAction extends AbstractCompleteReviewAction {
	@Override
	protected CrucibleAction getRequestedAction() {
		return CrucibleAction.COMPLETE;
	}

	@Override
	protected boolean getCompletionStatus() {
		return true;
	}
}
