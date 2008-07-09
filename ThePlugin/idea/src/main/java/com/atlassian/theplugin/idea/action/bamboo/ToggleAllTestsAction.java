package com.atlassian.theplugin.idea.action.bamboo;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.atlassian.theplugin.idea.bamboo.TestResultsToolWindow;

public class ToggleAllTestsAction extends ToggleAction {
	public boolean isSelected(AnActionEvent event) {
		TestResultsToolWindow.TestTree tree = TestResultsToolWindow.getTestTree(event.getPlace());
		if (tree == null) {
			return !TestResultsToolWindow.TestTree.PASSED_TESTS_VISIBLE_DEFAULT;
		}
		return !tree.isPassedTestsVisible();
	}

	public void setSelected(AnActionEvent event, boolean b) {
		TestResultsToolWindow.TestTree tree = TestResultsToolWindow.getTestTree(event.getPlace());
		tree.setPassedTestsVisible(!b);
	}
}
