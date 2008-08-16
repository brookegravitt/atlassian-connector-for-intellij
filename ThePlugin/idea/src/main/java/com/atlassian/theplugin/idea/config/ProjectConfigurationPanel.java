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

import com.atlassian.theplugin.idea.config.serverconfig.ServerConfigPanel;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;

import javax.swing.*;
import java.awt.*;

public class ProjectConfigurationPanel extends JPanel {
	private final FooterPanel footerPanel = new FooterPanel();
	private final JTabbedPane contentPanel = new JTabbedPane();
	private final ServerConfigPanel serverConfigPanel;

	public ProjectConfiguration getProjectConfiguration() {
		return projectConfiguration;
	}

	private final ProjectConfiguration projectConfiguration;

	public ProjectConfigurationPanel(ProjectConfiguration projectConfiguration) {
		this.projectConfiguration = projectConfiguration;
		serverConfigPanel = new ServerConfigPanel(projectConfiguration.getServers());
		initLayout();
	}

	private void initLayout() {
		setLayout(new BorderLayout());

		contentPanel.setOpaque(true);
		contentPanel.setBackground(new Color(Constants.BG_COLOR_R, Constants.BG_COLOR_G, Constants.BG_COLOR_B));

		// add servers tab
		contentPanel.add(serverConfigPanel.getTitle(), serverConfigPanel);

		add(contentPanel, BorderLayout.CENTER);
		add(footerPanel, BorderLayout.SOUTH);
	}

}
