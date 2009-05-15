/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.theplugin.idea.config;

import com.atlassian.theplugin.commons.UiTaskExecutor;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacade;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.fisheye.FishEyeServerFacade;
import com.atlassian.theplugin.idea.AboutForm;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.config.serverconfig.ServerConfigPanel;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class ProjectConfigurationPanel extends JPanel {
	private final FooterPanel footerPanel = new FooterPanel();
	private final JTabbedPane contentPanel = new JTabbedPane();
	private final ServerConfigPanel serverConfigPanel;
	private final ProjectDefaultsConfigurationPanel defaultsConfigurationPanel;
	private final AboutForm aboutBox;

	private ProjectConfiguration projectConfiguration;

	private static final int WIDTH = 800;

	private static final int HEIGHT = 700;

	public ProjectConfiguration getProjectConfiguration() {
		serverConfigPanel.saveData();
		return projectConfiguration;
	}

	public UserCfg getDefaultCredentials() {
		return serverConfigPanel.getDefaultUser();
	}

	public ProjectConfigurationPanel(@NotNull final Project project, @NotNull final ProjectConfiguration projectConfiguration,
			@NotNull final CrucibleServerFacade crucibleServerFacade, @NotNull final FishEyeServerFacade fishEyeServerFacade,
			final BambooServerFacade bambooServerFacade, final JIRAServerFacade jiraServerFacade,
			@NotNull final UiTaskExecutor uiTaskExecutor, final ServerCfg selectedServer,
			/*final IntelliJProjectCfgManager projectCfgManager, */@NotNull UserCfg defaultCredentials,
			final boolean defaultCredentialsAsked) {
		this.projectConfiguration = projectConfiguration;
		serverConfigPanel = new ServerConfigPanel(this, project, defaultCredentials,
				projectConfiguration, selectedServer, defaultCredentialsAsked);
		defaultsConfigurationPanel = new ProjectDefaultsConfigurationPanel(project, projectConfiguration, crucibleServerFacade,
				fishEyeServerFacade, bambooServerFacade, jiraServerFacade, uiTaskExecutor, defaultCredentials);
		aboutBox = new AboutForm();

		initLayout();
	}

	public boolean isDefaultCredentialsAsked() {
		return serverConfigPanel.isDefaultCredentialsAsked();
	}
	private void initLayout() {
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setPreferredSize(getMinimumSize());

		contentPanel.setOpaque(true);
		contentPanel.setBackground(new Color(Constants.BG_COLOR_R, Constants.BG_COLOR_G, Constants.BG_COLOR_B));
		contentPanel.getModel().addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				if (contentPanel.getSelectedComponent() == defaultsConfigurationPanel) {
					defaultsConfigurationPanel.setData(projectConfiguration);
				}
			}
		});

		// add servers tab
		contentPanel.add(serverConfigPanel.getTitle(), serverConfigPanel);
		contentPanel.add("Defaults", defaultsConfigurationPanel);
		contentPanel.add("About", aboutBox.getRootPane());

		add(contentPanel, BorderLayout.CENTER);
		add(footerPanel, BorderLayout.SOUTH);
	}

	public void saveData(boolean finalizeData) {
		if (finalizeData) {
			serverConfigPanel.finalizeData();
		}
		serverConfigPanel.saveData();
		if (!projectConfiguration.isDefaultFishEyeServerValid()) {
			projectConfiguration.setDefaultFishEyeServerId(null);
			Messages.showInfoMessage(this, "Default FishEye server settings have been cleared.", "Information");
		}
		if (!projectConfiguration.isDefaultCrucibleServerValid()) {
			projectConfiguration.setDefaultCrucibleServerId(null);
			Messages.showInfoMessage(this, "Default Crucible server settings have been cleared.", "Information");
		}

		if (!projectConfiguration.isDefaultJiraServerValid()) {
			projectConfiguration.setDefaultJiraServerId(null);
			Messages.showInfoMessage(this, "Default JIRA server settings have been cleared.", "Information");
		}



	}


	public void setData(ProjectConfiguration aProjectConfiguration) {
		projectConfiguration = aProjectConfiguration;
		serverConfigPanel.setData(projectConfiguration.getServers());
		defaultsConfigurationPanel.setData(projectConfiguration);
	}

	public void setDefaultCredentials(final UserCfg userCfg) {
		defaultsConfigurationPanel.setDefaultCredentials(userCfg);
	}
}
