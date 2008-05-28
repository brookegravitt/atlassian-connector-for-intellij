package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.crucible.api.PredefinedFilter;

public class OpenAction extends PredefinedFilterAction {
    public OpenAction() {
        super(PredefinedFilter.Open);
    }
}
