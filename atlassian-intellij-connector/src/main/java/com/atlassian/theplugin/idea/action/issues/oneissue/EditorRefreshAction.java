package com.atlassian.theplugin.idea.action.issues.oneissue;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: jgorycki
 * Date: Dec 30, 2008
 * Time: 5:00:05 PM
 */
public class EditorRefreshAction extends AbstractEditorIssueAction {
	public void actionPerformed(AnActionEvent e) {
		IdeaHelper.getIssueDetailsToolWindow(e).refresh(e.getPlace());
	}
}
