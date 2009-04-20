package com.atlassian.theplugin.idea.action.reviews;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;

/**
 * User: jgorycki
 * Date: Dec 3, 2008
 * Time: 2:34:41 PM
 */
public class AbandonReviewAction extends AbstractTransitionReviewAction {
	@Override
	protected CrucibleAction getRequestedTransition() {
		return CrucibleAction.ABANDON;
	}
}
