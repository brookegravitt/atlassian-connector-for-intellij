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
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.fisheye.FishEyeServerFacade;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.config.serverconfig.ServerConfigPanel;
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
	private final OwainConfigurationPanel defaultsConfigurationPanel;

	private ProjectConfiguration projectConfiguration;

	public ProjectConfiguration getProjectConfiguration() {
		serverConfigPanel.saveData();
		return projectConfiguration;
	}

	public ProjectConfigurationPanel(@NotNull final Project project, @NotNull final ProjectConfiguration projectConfiguration,
			@NotNull final CrucibleServerFacade crucibleServerFacade, @NotNull final FishEyeServerFacade fishEyeServerFacade,
			@NotNull final UiTaskExecutor uiTaskExecutor) {
		this.projectConfiguration = projectConfiguration;
		serverConfigPanel = new ServerConfigPanel(project, projectConfiguration.getServers());
		defaultsConfigurationPanel = new OwainConfigurationPanel(projectConfiguration, crucibleServerFacade,
				fishEyeServerFacade, uiTaskExecutor);
		initLayout();
	}

	private void initLayout() {
		setLayout(new BorderLayout());

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
	}


	public void setData(ProjectConfiguration aProjectConfiguration) {
		projectConfiguration = aProjectConfiguration;
		serverConfigPanel.setData(projectConfiguration.getServers());
		defaultsConfigurationPanel.setData(projectConfiguration);
	}
}
