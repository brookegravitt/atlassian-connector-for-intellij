package com.atlassian.theplugin.idea.action.crucible;

import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.commons.crucible.api.PredefinedFilter;
import com.atlassian.theplugin.idea.IdeaHelper;

public class PredefinedFilterAction extends ToggleAction {
    protected PredefinedFilter filter;

    public PredefinedFilterAction(PredefinedFilter filter) {
        this.filter = filter;
    }

    public boolean isSelected(AnActionEvent event) {
        if (IdeaHelper.getPluginConfiguration().getCrucibleConfigurationData().getFilters()[filter.ordinal()] == null) {
            IdeaHelper.getPluginConfiguration().getCrucibleConfigurationData().getFilters()[filter.ordinal()] = false;
        }
        return IdeaHelper.getPluginConfiguration().getCrucibleConfigurationData().getFilters()[filter.ordinal()];
    }

    public void setSelected(AnActionEvent event, boolean b) {
        IdeaHelper.getPluginConfiguration().getCrucibleConfigurationData().getFilters()[filter.ordinal()] = b;
    }
}