package com.atlassian.theplugin.idea.action.bamboo.changes;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.bamboo.BuildChangesToolWindow;

public class ExpandFilesAction extends AnAction {
	public void actionPerformed(AnActionEvent event) {
		BuildChangesToolWindow.getChangesTree(event.getPlace()).expand();
	}
}
