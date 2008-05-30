package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.crucible.api.PredefinedFilter;


public class DraftsAction extends PredefinedFilterAction {
    public DraftsAction() {
        super(PredefinedFilter.Drafts);
    }
}
