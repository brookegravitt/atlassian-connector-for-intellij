package com.atlassian.theplugin.idea.action;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.ProjectManager;

/**
 * Simple action to show the settings for the plugin.
 */
public class ShowSettingsAction extends AnAction {
	public void actionPerformed(AnActionEvent event) {
		ShowSettingsUtil.getInstance().editConfigurable(
                ProjectManager.getInstance().getDefaultProject(),
                IdeaHelper.getAppComponent());
	}
}