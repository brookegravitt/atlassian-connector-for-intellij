package com.atlassian.theplugin.idea.action.bamboo;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.bamboo.TestResultsToolWindow;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class DebugFailedTestsAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		TestResultsToolWindow window = IdeaHelper.getProjectComponent(e, TestResultsToolWindow.class);
		if (window != null) {
			window.runFailedTests(e, true);
		}
	}

	public void update(AnActionEvent e) {
		TestResultsToolWindow window = IdeaHelper.getProjectComponent(e, TestResultsToolWindow.class);
		if (window != null) {
			e.getPresentation().setEnabled(window.canRunFailedTests(e));
		}
		super.update(e);
	}
}