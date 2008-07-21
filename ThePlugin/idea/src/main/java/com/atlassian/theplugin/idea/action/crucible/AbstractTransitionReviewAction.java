package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Action;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.CrucibleCompleteWorker;
import com.atlassian.theplugin.idea.crucible.CrucibleChangeStateWorker;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;

public abstract class AbstractTransitionReviewAction extends AnAction {
    protected abstract Action getRequestedTransition();
    private ReviewData rd;

    public void actionPerformed(AnActionEvent event) {

        new Thread(new Runnable() {
            public void run() {
                        ApplicationManager.getApplication().invokeAndWait(
                        new CrucibleChangeStateWorker(rd, getRequestedTransition()),
                        ModalityState.defaultModalityState());
            }
        }).start();
    }

    public void update(AnActionEvent event) {
        super.update(event);
        if (IdeaHelper.getCrucibleToolWindowPanel(event) != null) {

            if (IdeaHelper.getCrucibleToolWindowPanel(event).getSelectedReview() == null) {
                event.getPresentation().setEnabled(false);
            } else {
                rd = IdeaHelper.getCrucibleToolWindowPanel(event).getSelectedReview();
                try {
                    if (rd.getTransitions().isEmpty()) {
                        event.getPresentation().setEnabled(false);
                        event.getPresentation().setVisible(false);
                    } else {
                        for (Action transition : rd.getTransitions()) {
                            if (transition.equals(getRequestedTransition())) {
                                event.getPresentation().setEnabled(true);
                                event.getPresentation().setVisible(true);
                                break;
                            } else {
                                event.getPresentation().setEnabled(false);
                                event.getPresentation().setVisible(false);
                            }                            
                        }
                    }
                } catch (ValueNotYetInitialized valueNotYetInitialized) {
                    valueNotYetInitialized.printStackTrace();
                }
            }
        } else {
            event.getPresentation().setEnabled(false);
            event.getPresentation().setVisible(false);
        }
    }
}