package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.idea.crucible.CrucibleRevisionAddWorker;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.Change;


public class ViewFisheyeChangesetAction extends AnAction {
    public void actionPerformed(AnActionEvent event) {
        final ChangeList[] changes = DataKeys.CHANGE_LISTS.getData(event.getDataContext());

        if (changes.length == 1) {
            String rev = "";
            for (Change change : changes[0].getChanges()) {
                rev = change.getAfterRevision().getRevisionNumber().asString();
                break;
            }
            final String finalRev = rev;
            new Thread(new Runnable() {
                public void run() {
                    ApplicationManager.getApplication().invokeAndWait(
                            new CrucibleRevisionAddWorker(CrucibleServerFacadeImpl.getInstance(), finalRev),
                            ModalityState.defaultModalityState());
                }
            }).start();
        }
    }
}
