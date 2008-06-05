package com.atlassian.theplugin.idea.action.crucible;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.atlassian.theplugin.idea.crucible.CrucibleRevisionReviewCreator;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;

public class PostCommitReviewAction extends Crucible16RepositoryAction {
    public void actionPerformed(AnActionEvent event) {
        final ChangeList[] changes = DataKeys.CHANGE_LISTS.getData(event.getDataContext());

        new Thread(new Runnable() {
            public void run() {
                        ApplicationManager.getApplication().invokeAndWait(
                        new CrucibleRevisionReviewCreator(CrucibleServerFacadeImpl.getInstance(), changes),
                        ModalityState.defaultModalityState());
            }
        }).start();
    }
}
