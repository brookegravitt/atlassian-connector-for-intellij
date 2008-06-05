package com.atlassian.theplugin.idea.action.crucible;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.atlassian.theplugin.idea.crucible.CruciblePatchUploader;
import com.atlassian.theplugin.idea.crucible.CrucibleRevisionReviewCreator;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.CrucibleVersion;

import java.util.List;
import java.util.ArrayList;


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
                if (IdeaHelper.getCrucibleToolWindowPanel(event).getSelectedReviewId() != null) {
                    event.getPresentation().setEnabled(true);
                } else {
                    event.getPresentation().setEnabled(false);
                }
            } else {
                event.getPresentation().setEnabled(false);
            }
        } else {
            event.getPresentation().setVisible(false);
        }


    }
}