package com.atlassian.theplugin.idea.action.issues;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.IdeaHelper;

public class LogWorkAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		IdeaHelper.getIssuesToolWindowPanel(e).logWorkForIssue();
	}
}
