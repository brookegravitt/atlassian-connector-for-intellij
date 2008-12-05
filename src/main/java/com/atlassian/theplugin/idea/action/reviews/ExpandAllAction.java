package com.atlassian.theplugin.idea.action.reviews;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.IdeaHelper;

/**
 * User: jgorycki
 * Date: Dec 5, 2008
 * Time: 2:26:30 PM
 */
public class ExpandAllAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		IdeaHelper.getReviewsToolWindowPanel(e).expandAll();
	}
}
