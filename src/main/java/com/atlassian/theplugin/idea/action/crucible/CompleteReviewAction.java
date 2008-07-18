package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Transition;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: marek
 * Date: Jul 17, 2008
 * Time: 4:47:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompleteReviewAction extends AnAction {
    public void actionPerformed(AnActionEvent event) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void update(AnActionEvent event) {
        super.update(event);
        if (IdeaHelper.getCrucibleToolWindowPanel(event) != null) {

            if (IdeaHelper.getCrucibleToolWindowPanel(event).getSelectedReview() == null) {
                event.getPresentation().setEnabled(false);
            } else {
                ReviewData rd = IdeaHelper.getCrucibleToolWindowPanel(event).getSelectedReview();

                try {
                    for (Transition transition : rd.getTransitions()) {
                        if (transition.getActionName().equals("action:Complete")) {
                            event.getPresentation().setEnabled(true);
                        }
                    }
                } catch (ValueNotYetInitialized valueNotYetInitialized) {
                    valueNotYetInitialized.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }


        } else {
            event.getPresentation().setEnabled(false);
        }

    }
}
