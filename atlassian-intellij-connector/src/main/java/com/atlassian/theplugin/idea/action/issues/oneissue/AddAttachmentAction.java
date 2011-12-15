package com.atlassian.theplugin.idea.action.issues.oneissue;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class AddAttachmentAction extends AbstractEditorIssueAction {
	public void actionPerformed(AnActionEvent e) {
		String place = e.getPlace();
		IdeaHelper.getIssueDetailsToolWindow(e).addAttachment(place);
	}
}
