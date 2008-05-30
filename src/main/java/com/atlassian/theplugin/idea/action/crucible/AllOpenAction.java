package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.crucible.api.PredefinedFilter;

public class AllOpenAction extends PredefinedFilterAction {
    public AllOpenAction() {
        super(PredefinedFilter.AllOpen);
    }
}