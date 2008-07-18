package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.Action;

public class UncompleteReviewAction extends AbstractCompleteReviewAction {
    protected Action getRequestedAction() {
        return Action.UNCOMPLETE;
    }

    protected boolean getCompletionStatus() {
        return false;
    }
}
