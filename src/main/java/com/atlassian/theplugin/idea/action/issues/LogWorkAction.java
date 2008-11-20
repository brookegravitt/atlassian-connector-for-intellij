package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class LogWorkAction extends JIRAAbstractAction {
	public void actionPerformed(AnActionEvent e) {
		IdeaHelper.getIssuesToolWindowPanel(e).logWorkForIssue();
	}

	public void onUpdate(AnActionEvent event) {		
	}
}
