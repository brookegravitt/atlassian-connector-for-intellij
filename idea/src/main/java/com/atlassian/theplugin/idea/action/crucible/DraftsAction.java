package com.atlassian.theplugin.idea.action.crucible;

import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.commons.crucible.api.PredefinedFilter;


public class DraftsAction extends PredefinedFilterAction {
    public DraftsAction() {
        super(PredefinedFilter.Drafts);
    }
}
