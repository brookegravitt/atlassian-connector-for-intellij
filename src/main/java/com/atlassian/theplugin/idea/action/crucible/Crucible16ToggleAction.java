package com.atlassian.theplugin.idea.action.crucible;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.commons.crucible.CrucibleVersion;

public class Crucible16ToggleAction extends ToggleAction {
    public boolean isSelected(AnActionEvent event) {
        return false;
    }

    public void setSelected(AnActionEvent event, boolean b) {
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