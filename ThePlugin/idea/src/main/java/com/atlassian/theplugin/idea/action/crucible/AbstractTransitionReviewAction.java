package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Action;
import com.atlassian.theplugin.commons.crucible.api.model.Transition;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public abstract class AbstractTransitionReviewAction extends AnAction {
    protected abstract Action getRequestedTransition();

    public void actionPerformed(AnActionEvent event) {

    }

    public void update(AnActionEvent event) {
        super.update(event);
        if (IdeaHelper.getCrucibleToolWindowPanel(event) != null) {

            if (IdeaHelper.getCrucibleToolWindowPanel(event).getSelectedReview() == null) {
                event.getPresentation().setEnabled(false);
            } else {
                ReviewData rd = IdeaHelper.getCrucibleToolWindowPanel(event).getSelectedReview();
                try {
                    if (rd.getTransitions().isEmpty()) {
                        event.getPresentation().setEnabled(false);
                        event.getPresentation().setVisible(false);
                    } else {
                        for (Transition transition : rd.getTransitions()) {
                            if (Action.fromValue(transition.getActionName()).equals(getRequestedTransition())) {
                                event.getPresentation().setEnabled(true);
                                event.getPresentation().setVisible(true);
                            } else {
                                event.getPresentation().setEnabled(false);
                                event.getPresentation().setVisible(false);
                            }
                            break;
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