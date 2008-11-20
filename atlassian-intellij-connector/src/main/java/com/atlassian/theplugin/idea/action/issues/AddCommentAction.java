package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class AddCommentAction extends JIRAAbstractAction {
	public void actionPerformed(AnActionEvent anActionEvent) {
	    IdeaHelper.getIssuesToolWindowPanel(anActionEvent).addCommentToIssue();
	}

	public void onUpdate(AnActionEvent event) {		
	}
}
