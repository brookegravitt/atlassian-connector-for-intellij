package com.atlassian.theplugin.idea.action.crucible;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.atlassian.theplugin.idea.crucible.CruciblePatchAddWorker;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;

public class AddPatchToReviewAction extends AnAction {

    public void actionPerformed(AnActionEvent event) {
        final ChangeList[] changes = DataKeys.CHANGE_LISTS.getData(event.getDataContext());
        final Project project = DataKeys.PROJECT.getData(event.getDataContext());
        final PermId permId = IdeaHelper.getCrucibleToolWindowPanel(event).getSelectedReviewId();

        new Thread(new Runnable() {
            public void run() {
                ApplicationManager.getApplication().invokeAndWait(
                        new CruciblePatchAddWorker(CrucibleServerFacadeImpl.getInstance(), permId, project, changes),
                        ModalityState.defaultModalityState());
            }
        }).start();
    }

    public void update(AnActionEvent event) {
        super.update(event);
        final ChangeList[] changes = DataKeys.CHANGE_LISTS.getData(event.getDataContext());
        if (changes != null && changes.length == 1) {
            event.getPresentation().setEnabled(true);
        } else {
            event.getPresentation().setEnabled(false);
        }
    }
}