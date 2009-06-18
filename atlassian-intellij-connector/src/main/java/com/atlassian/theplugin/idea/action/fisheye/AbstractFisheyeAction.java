package com.atlassian.theplugin.idea.action.fisheye;

import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractFisheyeAction extends AnAction {
	@Override
	public void update(final AnActionEvent event) {
		event.getPresentation().setVisible(isFishEyeConfigured(event));
	}

	@Nullable
	protected ServerData getFishEyeServerCfg(final AnActionEvent event) {
		final Project project = IdeaHelper.getCurrentProject(event);
		if (project == null) {
			return null;
		}
		final ProjectCfgManagerImpl projectCfgManager = IdeaHelper.getProjectCfgManager(event);
		if (projectCfgManager == null) {
			return null;
		}

		final ServerData fishEyeServer = projectCfgManager.getDefaultFishEyeServer();
		if (fishEyeServer == null) {
			Messages.showInfoMessage(project,
					"Cannot determine enabled default FishEye server. Make sure you have configured it correctly.",
					"Configuration problem");
			return null;
		}
		return fishEyeServer;
	}

	@Nullable
	protected String getFishEyeRepository(final AnActionEvent event) {
		final Project project = IdeaHelper.getCurrentProject(event);
		if (project == null) {
			return null;
		}
		final ProjectCfgManagerImpl projectCfgManager = IdeaHelper.getProjectCfgManager(project);
		if (projectCfgManager == null) {
			return null;
		}

		final String repository = projectCfgManager.getDefaultFishEyeRepo();
		if (repository == null) {
			Messages.showInfoMessage(project,
					"Cannot determine default FishEye repository. Make sure you have configured it correctly.",
					"Configuration problem");
			return null;
		}
		return repository;

	}


	protected boolean isFishEyeConfigured(final AnActionEvent event) {
		final Project project = IdeaHelper.getCurrentProject(event);
		if (project == null) {
			return false;
		}

		final ProjectCfgManagerImpl projectCfgManager = IdeaHelper.getProjectCfgManager(project);
		if (projectCfgManager == null) {
			return false;
		}

		if (projectCfgManager.getDefaultFishEyeServer() == null || projectCfgManager.getDefaultFishEyeRepo() == null) {
			return false;
		}

		return true;
	}

}