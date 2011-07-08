package com.atlassian.theplugin.idea.action.fisheye;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractFisheyeAction extends AnAction {
	@Override
	public void update(final AnActionEvent event) {
		final boolean fishEyeConfigured = isFishEyeConfigured(event);
		System.out.println("isFishEyeConfigured == " + fishEyeConfigured);

		event.getPresentation().setVisible(fishEyeConfigured);
		event.getPresentation().setEnabled(fishEyeConfigured);

	}

	@Nullable
	protected ServerData getFishEyeServerCfg(final AnActionEvent event) {
		final Project project = IdeaHelper.getCurrentProject(event);
		if (project == null) {
			return null;
		}
		final ProjectCfgManager projectCfgManager = IdeaHelper.getProjectCfgManager(event);
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
		final ProjectCfgManager projectCfgManager = IdeaHelper.getProjectCfgManager(project);
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

		final ProjectCfgManager projectCfgManager = IdeaHelper.getProjectCfgManager(project);
		if (projectCfgManager == null) {
			return false;
		}

		if (projectCfgManager.getDefaultFishEyeServer() == null || projectCfgManager.getDefaultFishEyeRepo() == null) {
			return false;
		}

		return true;
	}

}