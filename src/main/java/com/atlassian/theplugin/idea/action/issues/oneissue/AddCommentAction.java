package com.atlassian.theplugin.idea.action.issues.oneissue;

import com.atlassian.theplugin.idea.jira.IssueToolWindow;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: jgorycki
 * Date: Dec 29, 2008
 * Time: 12:59:54 PM
 */
public class AddCommentAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		IssueToolWindow.addComment(e.getPlace());
	}
}
