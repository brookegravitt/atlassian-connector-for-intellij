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

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.connector.intellij.bamboo.BambooServerFacade;
import com.atlassian.connector.intellij.fisheye.FishEyeServerFacade;
import com.atlassian.theplugin.commons.UiTaskExecutor;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.jira.JiraServerFacade;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.configuration.WorkspaceConfigurationBean;
import com.atlassian.theplugin.idea.AboutForm;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.config.serverconfig.ServerConfigPanel;
import com.atlassian.theplugin.idea.ui.ScrollablePanel;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class ProjectConfigurationPanel extends ScrollablePanel {
	private final FooterPanel footerPanel = new FooterPanel();
	private final JTabbedPane contentPanel = new JTabbedPane();
	private final ServerConfigPanel serverConfigPanel;
	private final ProjectDefaultsConfigurationPanel defaultsConfigurationPanel;
	private final AboutForm aboutBox;

	private Project project;
	private ProjectConfiguration projectConfiguration;
	private UserCfg defaultCredentials;
	private boolean defaultCredentialsAsked;
	private WorkspaceConfigurationBean projectConfigurationBean;

	private static final int WIDTH = 800;

	private static final int HEIGHT = 700;

	public ProjectConfiguration getProjectConfiguration() {
		serverConfigPanel.saveData();
		return projectConfiguration;
	}

	public UserCfg getDefaultCredentials() {
		return serverConfigPanel.getDefaultUser();
	}

	public ProjectConfigurationPanel(@NotNull final Project project,
			@NotNull final ProjectConfiguration projectConfiguration,
			@NotNull final FishEyeServerFacade fishEyeServerFacade,
			final BambooServerFacade bambooServerFacade,
			final JiraServerFacade jiraServerFacade,
			@NotNull final UiTaskExecutor uiTaskExecutor, final ServerData selectedServer,
			/*final IntelliJProjectCfgManager projectCfgManager, */
			@NotNull final UserCfg defaultCredentials,
			final boolean defaultCredentialsAsked,
			WorkspaceConfigurationBean projectConfigurationBean) {
		this.project = project;
		this.projectConfiguration = projectConfiguration;
		this.defaultCredentials = defaultCredentials;
		this.defaultCredentialsAsked = defaultCredentialsAsked;
		this.projectConfigurationBean = projectConfigurationBean;
		serverConfigPanel = new ServerConfigPanel(project, defaultCredentials,
				projectConfiguration, selectedServer, defaultCredentialsAsked);
		defaultsConfigurationPanel = new ProjectDefaultsConfigurationPanel(project, projectConfiguration,
				fishEyeServerFacade, bambooServerFacade, jiraServerFacade, uiTaskExecutor, defaultCredentials);
		aboutBox = new AboutForm();

		initLayout();
	}

	public boolean isDefaultCredentialsAsked() {
		return this.defaultCredentialsAsked;
	}

	private void initLayout() {
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setPreferredSize(getMinimumSize());

//		contentPanel.setOpaque(true);
//		contentPanel.setBackground(new Color(Constants.BG_COLOR_R, Constants.BG_COLOR_G, Constants.BG_COLOR_B));
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

	public void askForDefaultCredentials() {
		final ServerCfg selectedServer = serverConfigPanel.getSelectedServer();

        // PL-1703 - when adding a StAC server, there is no selected server,
        // as two servers are added simultaneously 
        if (selectedServer == null) {
            return;
        }

		// PL-1617 - Ugly ugly. I am not sure why this is b0rked sometimes,
		// but one of these seems to be null for apparent reason every once in a while
		ProjectCfgManager cfgMgr = IdeaHelper.getProjectCfgManager(project);
		if (cfgMgr == null) {
			LoggerImpl.getInstance().warn("askDefaultCredentials() - cfgMgr is null");
		}

		final boolean alreadyExists =
				cfgMgr != null
						&& cfgMgr.getServerr(selectedServer.getServerId()) != null;

		ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
			public void run() {

				final ModalityState modalityState = ModalityState.stateForComponent(ProjectConfigurationPanel.this);
				ApplicationManager.getApplication().invokeLater(new Runnable() {
					public void run() {

						if (!alreadyExists && !defaultCredentialsAsked
								&& (defaultCredentials == null
								|| (defaultCredentials.getPassword().equals("")
								&& defaultCredentials.getPassword().equals(""))
								&& selectedServer.getUsername().length() > 0)) {
							int answer = Messages.showYesNoDialog(project,
									"<html>Do you want to set server <b>" + selectedServer.getName()
											+ "</b> <i>username</i> and <i>password</i>"
											+ " as default credentials for the "
											+ PluginUtil.PRODUCT_NAME + "?</html>",
									"Set as default",
									Messages.getQuestionIcon());

							ProjectCfgManagerImpl cfgMgr = (ProjectCfgManagerImpl) IdeaHelper.getProjectCfgManager(project);

							if (answer == DialogWrapper.OK_EXIT_CODE) {
								UserCfg credentials = new UserCfg(selectedServer.getUsername(),
										selectedServer.getPassword(), true);
								if (cfgMgr != null) {
									cfgMgr.setDefaultCredentials(credentials);
								}
								defaultsConfigurationPanel.setDefaultCredentials(credentials);
							}
							if (cfgMgr != null) {
								cfgMgr.setDefaultCredentialsAsked(true);
							}
							ProjectConfigurationPanel.this.defaultCredentialsAsked = true;
						}
					}
				}, modalityState);
			}
		});
	}
}
