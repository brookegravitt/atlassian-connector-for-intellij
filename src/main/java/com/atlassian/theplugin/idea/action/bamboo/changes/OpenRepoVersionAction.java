package com.atlassian.theplugin.idea.action.bamboo.changes;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class OpenRepoVersionAction extends AnAction {
	public void actionPerformed(AnActionEvent event) {
	}

	public void update(AnActionEvent event) {
		event.getPresentation().setEnabled(false);
		super.update(event);
	}
}
