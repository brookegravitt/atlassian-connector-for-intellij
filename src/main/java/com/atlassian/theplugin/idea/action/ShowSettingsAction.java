package com.atlassian.theplugin.idea.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.application.ApplicationManager;
import com.atlassian.theplugin.idea.ThePluginApplicationComponent;

/**
 * Simple action to show the settings for the plugin.
 */
public class ShowSettingsAction extends AnAction
{
	public void actionPerformed(AnActionEvent event) {
		ShowSettingsUtil.getInstance().editConfigurable(ProjectManager.getInstance().getDefaultProject(), ApplicationManager.getApplication().getComponent(ThePluginApplicationComponent.class));
	}
}