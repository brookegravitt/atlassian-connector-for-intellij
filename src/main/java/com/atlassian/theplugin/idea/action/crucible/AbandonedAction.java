package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.crucible.api.PredefinedFilter;

public class AbandonedAction extends PredefinedFilterAction {
    public AbandonedAction() {
        super(PredefinedFilter.Abandoned);
    }
}
