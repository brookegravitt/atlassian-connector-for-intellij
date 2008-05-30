package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.crucible.api.PredefinedFilter;

public class AllClosedAction extends PredefinedFilterAction {
    public AllClosedAction() {
        super(PredefinedFilter.AllClosed);
    }
}