package com.atlassian.theplugin.idea.action;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;

public class ShowGlobalSettingsAction extends AnAction {

	@Override
	public void update(final AnActionEvent event) {
		event.getPresentation().setEnabled(IdeaHelper.getCurrentProject(event) != null);
	}

	@Override
	public void actionPerformed(final AnActionEvent event) {
		Project project = IdeaHelper.getCurrentProject(event);
		if (project != null) {
			Configurable component = IdeaHelper.getAppComponent();
			ShowSettingsUtil.getInstance().editConfigurable(project, component);
		}
	}
}
