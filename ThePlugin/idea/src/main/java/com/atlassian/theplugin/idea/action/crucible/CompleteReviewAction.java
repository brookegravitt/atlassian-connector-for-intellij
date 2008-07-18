package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.Action;

public class CompleteReviewAction extends AbstractCompleteReviewAction {
    protected Action getRequestedAction() {
        return Action.COMPLETE;
    }

    protected boolean getCompletionStatus() {
        return true;
    }
}
