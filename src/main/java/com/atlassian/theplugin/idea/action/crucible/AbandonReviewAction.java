package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.Action;

public class AbandonReviewAction extends AbstractTransitionReviewAction {
    protected Action getRequestedTransition() {
        return Action.ABANDON;
    }
}
