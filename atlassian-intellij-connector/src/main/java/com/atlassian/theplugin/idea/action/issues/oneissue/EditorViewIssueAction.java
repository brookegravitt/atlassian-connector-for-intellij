package com.atlassian.theplugin.idea.action.issues.oneissue;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.jira.IssueToolWindow;

/**
 * User: jgorycki
 * Date: Dec 29, 2008
 * Time: 4:15:41 PM
 */
public class EditorViewIssueAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		IssueToolWindow.viewIssueInBrowser(e.getPlace());
	}
}
