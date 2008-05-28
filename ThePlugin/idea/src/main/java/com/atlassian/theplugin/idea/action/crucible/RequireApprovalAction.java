package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.crucible.api.PredefinedFilter;

public class RequireApprovalAction extends PredefinedFilterAction {
    public RequireApprovalAction() {
        super(PredefinedFilter.RequireMyApproval);
    }
}
