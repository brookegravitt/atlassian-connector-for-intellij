package com.atlassian.theplugin.idea.action.reviews;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: jgorycki
 * Date: Dec 5, 2008
 * Time: 2:26:34 PM
 */
public class CollapseAllAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		IdeaHelper.getReviewsToolWindowPanel(e).collapseAllRightTreeNodes();
	}
}
