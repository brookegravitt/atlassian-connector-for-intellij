package com.atlassian.theplugin.idea.action.issues.oneissue;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: pstefaniak
 * Date: Mar 23, 2010
 */
public class EditDescriptionAction extends AbstractEditorIssueAction {
	public void actionPerformed(AnActionEvent e) {
		IdeaHelper.getIssueDetailsToolWindow(e).editDescription(e.getPlace());
	}
}
