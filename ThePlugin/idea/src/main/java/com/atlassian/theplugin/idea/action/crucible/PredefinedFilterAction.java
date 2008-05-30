package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.crucible.CrucibleVersion;
import com.atlassian.theplugin.commons.crucible.api.PredefinedFilter;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;

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
        IdeaHelper.getCrucibleToolWindowPanel(event).showPredefinedFilter(filter, b);
    }

    public void update(AnActionEvent event) {
        super.update(event);
        if (IdeaHelper.getCrucibleToolWindowPanel(event) != null) {
            event.getPresentation().setVisible(
                    (IdeaHelper.getCrucibleToolWindowPanel(event).getCrucibleVersion() == CrucibleVersion.CRUCIBLE_16));
        } else {
            event.getPresentation().setVisible(false);
        }
    }
}