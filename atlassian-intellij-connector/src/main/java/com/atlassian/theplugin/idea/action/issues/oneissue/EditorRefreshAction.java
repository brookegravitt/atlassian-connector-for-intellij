package com.atlassian.theplugin.idea.action.issues.oneissue;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: jgorycki
 * Date: Dec 30, 2008
 * Time: 5:00:05 PM
 */
public class EditorRefreshAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		IdeaHelper.getIssueToolWindow(e).refresh(e.getPlace());
	}
}
