package com.atlassian.theplugin.idea.action.issues.oneissue;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: jgorycki
 * Date: Dec 29, 2008
 * Time: 12:59:35 PM
 */
public class RefreshCommentsAction extends AbstractEditorIssueAction {
	public void actionPerformed(AnActionEvent e) {
		IdeaHelper.getIssueDetailsToolWindow(e).refreshComments(e.getPlace());
	}
}
