package com.atlassian.theplugin.idea.action.reviews;

import com.atlassian.theplugin.commons.crucible.api.model.Action;

/**
 * User: jgorycki
 * Date: Dec 3, 2008
 * Time: 2:18:33 PM
 */
public class ApproveReviewAction extends AbstractTransitionReviewAction {
	protected Action getRequestedTransition() {
		return Action.APPROVE;
	}
}
