package com.atlassian.theplugin.idea.action.bamboo;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.bamboo.TestResultsToolWindow;

public class ToggleAllTestsAction extends AnAction {
    public void actionPerformed(AnActionEvent event) {
        TestResultsToolWindow.TestTree exp = TestResultsToolWindow.getTestTree(event.getPlace());
        exp.toggleAll();
    }
}
