package com.atlassian.theplugin.idea.action.issues;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.IdeaHelper;

public class AddCommentAction extends AnAction {
	public void actionPerformed(AnActionEvent anActionEvent) {
	    IdeaHelper.getIssuesToolWindowPanel(anActionEvent).addCommentToIssue();
	}

}
