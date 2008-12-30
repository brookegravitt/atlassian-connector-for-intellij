package com.atlassian.theplugin.idea.action.issues.oneissue;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.jira.IssueToolWindow;

/**
 * User: jgorycki
 * Date: Dec 30, 2008
 * Time: 4:11:04 PM
 */
public class EditorCloseAction extends AnAction {
	@Override
	public void actionPerformed(AnActionEvent e) {
		IssueToolWindow.closeToolWindow(e);
	}
}
