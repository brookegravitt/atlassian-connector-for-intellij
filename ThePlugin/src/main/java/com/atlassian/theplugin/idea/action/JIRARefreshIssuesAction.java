package com.atlassian.theplugin.idea.action;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Simple action to show the settings for the plugin.
 */
public class JIRARefreshIssuesAction extends AnAction {
	public void actionPerformed(AnActionEvent event) {
        IdeaHelper.getJIRAToolWindowPanel(event).refreshIssues();
    }
}