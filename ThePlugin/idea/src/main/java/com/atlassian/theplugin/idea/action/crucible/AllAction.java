package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.crucible.api.PredefinedFilter;

public class AllAction extends PredefinedFilterAction {
    public AllAction() {
        super(PredefinedFilter.All);
    }
}