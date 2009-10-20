package com.atlassian.theplugin.idea.action.bamboo;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.bamboo.BuildToolWindow;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class RunTestsAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		BuildToolWindow window = IdeaHelper.getBuildToolWindow(e);
		window.runTests(e, false);
	}

	public void update(AnActionEvent e) {
		BuildToolWindow window = IdeaHelper.getBuildToolWindow(e);
		if (window != null) {
			e.getPresentation().setEnabled(window.canRunTests(e.getPlace()));
		}
		super.update(e);
	}
}
