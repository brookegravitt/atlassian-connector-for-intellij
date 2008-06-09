package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.crucible.CrucibleVersion;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.vcs.changes.ChangeList;


public class Crucible16RepositoryAction extends AnAction {
    public void actionPerformed(AnActionEvent event) {
    }

    public void update(AnActionEvent event) {
        super.update(event);
        if (IdeaHelper.getCrucibleToolWindowPanel(event) != null) {
            event.getPresentation().setVisible(
                    (IdeaHelper.getCrucibleToolWindowPanel(event).getCrucibleVersion() == CrucibleVersion.CRUCIBLE_16));
            final ChangeList[] changes = DataKeys.CHANGE_LISTS.getData(event.getDataContext());
            if (changes != null && changes.length > 0) {
                event.getPresentation().setEnabled(true);
            } else {
                event.getPresentation().setEnabled(false);
            }
        } else {
            event.getPresentation().setVisible(false);
        }
    }
}