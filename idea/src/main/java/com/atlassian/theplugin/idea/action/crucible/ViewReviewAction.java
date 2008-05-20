package com.atlassian.theplugin.idea.action.crucible;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.IdeaHelper;

/**
 * Created by IntelliJ IDEA.
 * User: marek
 * Date: May 20, 2008
 * Time: 2:42:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class ViewReviewAction extends AnAction {
    public void actionPerformed(AnActionEvent event) {
        IdeaHelper.getCrucibleToolWindowPanel(event).viewReview();
    }
}
