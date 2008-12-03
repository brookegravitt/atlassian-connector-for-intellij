package com.atlassian.theplugin.idea.action.reviews;

import com.atlassian.theplugin.commons.crucible.api.model.Action;

/**
 * User: jgorycki
 * Date: Dec 3, 2008
 * Time: 2:18:40 PM
 */
public class CloseReviewAction extends AbstractTransitionReviewAction {
	protected Action getRequestedTransition() {
        return Action.CLOSE;
    }
}
