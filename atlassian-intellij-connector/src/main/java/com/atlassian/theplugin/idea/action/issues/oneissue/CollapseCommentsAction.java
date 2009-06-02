package com.atlassian.theplugin.idea.action.issues.oneissue;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: jgorycki
 * Date: Dec 29, 2008
 * Time: 12:59:49 PM
 */
public class CollapseCommentsAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		IdeaHelper.getIssueDetailsToolWindow(e).setCommentsExpanded(e.getPlace(), false);
	}
}
