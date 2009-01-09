package com.atlassian.theplugin.idea.action.bamboo.onebuild;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.action.bamboo.AbstractRunBuildAction;
import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;
import com.atlassian.theplugin.idea.bamboo.BuildToolWindow;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

/**
 * Used in the panel with build details
 */
public class RunBuildAction extends AbstractRunBuildAction {

	protected BambooBuildAdapterIdea getBuild(final AnActionEvent e) {
		BuildToolWindow btw = IdeaHelper.getBuildToolWindow(e);
		if (btw != null) {
			return btw.getBuild(e.getPlace());
		}

		return null;
	}

	protected void setStatusMessage(final Project project, final String message) {

	}
}
