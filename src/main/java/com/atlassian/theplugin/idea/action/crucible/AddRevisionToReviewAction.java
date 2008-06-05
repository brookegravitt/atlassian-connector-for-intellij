package com.atlassian.theplugin.idea.action.crucible;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.atlassian.theplugin.idea.crucible.CrucibleRevisionReviewCreator;
import com.atlassian.theplugin.idea.crucible.CrucibleRevisionAddWorker;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.CrucibleVersion;
import com.atlassian.theplugin.commons.crucible.api.ReviewData;
import com.atlassian.theplugin.commons.crucible.api.PermId;

public class AddRevisionToReviewAction extends Crucible16RepositoryAction {
    public void actionPerformed(AnActionEvent event) {
        final ChangeList[] changes = DataKeys.CHANGE_LISTS.getData(event.getDataContext());
        final PermId permId = IdeaHelper.getCrucibleToolWindowPanel(event).getSelectedReviewId();

        new Thread(new Runnable() {
            public void run() {
                        ApplicationManager.getApplication().invokeAndWait(
                        new CrucibleRevisionAddWorker(CrucibleServerFacadeImpl.getInstance(), permId, changes),
                        ModalityState.defaultModalityState());
            }
        }).start();
    }
}
