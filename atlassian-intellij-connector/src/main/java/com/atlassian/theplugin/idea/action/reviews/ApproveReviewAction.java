package com.atlassian.theplugin.idea.action.reviews;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;

/**
 * User: jgorycki
 * Date: Dec 3, 2008
 * Time: 2:18:33 PM
 */
public class ApproveReviewAction extends AbstractTransitionReviewAction {
	@Override
	protected CrucibleAction getRequestedTransition() {
		return CrucibleAction.APPROVE;
	}
}
