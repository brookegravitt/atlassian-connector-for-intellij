package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.crucible.api.PredefinedFilter;

public class ClosedAction extends PredefinedFilterAction {
    public ClosedAction() {
        super(PredefinedFilter.Closed);
    }
}
