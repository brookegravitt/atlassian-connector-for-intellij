package com.atlassian.theplugin.idea.action.bamboo;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.bamboo.TestResultsToolWindow;
import com.atlassian.theplugin.idea.IdeaHelper;

public class RunFailedTestsAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		TestResultsToolWindow window = IdeaHelper.getProjectComponent(e, TestResultsToolWindow.class);
		window.runFailedTests(false);
	}

	public void update(AnActionEvent e) {
		TestResultsToolWindow window = IdeaHelper.getProjectComponent(e, TestResultsToolWindow.class);
		if (window != null) {
			e.getPresentation().setEnabled(window.canRunFailedTests());
		}
		super.update(e);
	}
}
