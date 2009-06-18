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
import com.atlassian.connector.intellij.configuration.UserCfgBean;
import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.StringUtil;
import com.atlassian.theplugin.configuration.WorkspaceConfigurationBean;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class ProjectCfgManagerImpl implements ProjectCfgManager {
	//	private final ProjectConfigurationComponent projectConfigurationComponent;
	private final CfgManager cfgManager;
	private final ProjectId projectId;
	private final WorkspaceConfigurationBean projectConfigurationBean;

	/**
	 * Do NOT use the constructor. It is called by PICO.
	 *
	 * @param project				  can be null for tests purposes
	 * @param cfgManager
	 * @param projectConfigurationBean used for default credentials purposes only (can be null for tests)
	 */
	public ProjectCfgManagerImpl(Project project, CfgManager cfgManager, WorkspaceConfigurationBean projectConfigurationBean) {
		this.projectConfigurationBean = projectConfigurationBean;
		this.projectId = CfgUtil.getProjectId(project);
		this.cfgManager = cfgManager;
	}

	public boolean isDefaultCredentialsAsked() {
		return projectConfigurationBean.isDefaultCredentialsAsked();
	}

	public void setDefaultCredentialsAsked(final boolean defaultCredentialsAsked) {
		projectConfigurationBean.setDefaultCredentialsAsked(defaultCredentialsAsked);
	}

	@NotNull
	public ServerData getServerData(@NotNull Server serverCfg) {
		return getServerDataImpl(serverCfg);
	}

	public ServerCfg getServer(final ServerData serverData) {
		return cfgManager.getServer(projectId, serverData);
	}

	public ServerCfg getServer(final ServerId serverId) {
		return cfgManager.getServer(projectId, serverId);
	}

	public ProjectConfiguration getProjectConfiguration() {
		return cfgManager.getProjectConfiguration(projectId);
	}

	@NotNull
	public UserCfg getDefaultCredentials() {
		return new UserCfg(projectConfigurationBean.getDefaultCredentials().getUsername(),
				StringUtil.decode(projectConfigurationBean.getDefaultCredentials().getEncodedPassword()));
	}

	@NotNull
	private ServerData getServerDataImpl(@NotNull Server serverCfg) {
		return ServerData.create(serverCfg, getDefaultCredentials());
	}

	public ServerData getServerData(final ServerId serverId) {
		final ServerCfg serverCfg = cfgManager.getServer(projectId, serverId);

		if (serverCfg != null) {
			return getServerDataImpl(serverCfg);
		}
		return null;
	}

	/**
	 * Returns ServerData for enabled server with serverId specified by parameter
	 *
	 * @param serverId
	 * @return ServerData for enabled server with serverId specified by parameter
	 */
	@Nullable
	public ServerData getEnabledServerData(final ServerId serverId) {
		final ServerCfg serverCfg = cfgManager.getServer(projectId, serverId);

		if (serverCfg != null && serverCfg.isEnabled()) {
			return getServerDataImpl(serverCfg);
		}
		return null;
	}

	public Collection<BambooServerCfg> getAllEnabledBambooServers() {
		return cfgManager.getAllEnabledBambooServers(projectId);
	}

	public Collection<JiraServerCfg> getAllEnabledJiraServers() {
		return cfgManager.getAllEnabledJiraServers(projectId);
	}

	public Collection<CrucibleServerCfg> getAllEnabledCrucibleServers() {
		return cfgManager.getAllEnabledCrucibleServers(projectId);
	}

	public Collection<CrucibleServerCfg> getAllCrucibleServers() {
		return cfgManager.getAllCrucibleServers(projectId);
	}

	public Collection<ServerCfg> getAllEnabledServers() {
		return cfgManager.getAllEnabledServers(projectId);
	}

	public Collection<ServerCfg> getAllEnabledServers(ServerType serverType) {
		return cfgManager.getAllEnabledServers(projectId, serverType);
	}

	public Collection<ServerCfg> getAllServers(ServerType serverType) {
		return cfgManager.getAllServers(projectId, serverType);
	}

	public void updateProjectConfiguration(final ProjectConfiguration projectConfiguration) {
		cfgManager.updateProjectConfiguration(projectId, projectConfiguration);
	}

	public void setDefaultCredentials(@NotNull final UserCfg defaultCredentials) {
		projectConfigurationBean.setDefaultCredentials(
				new UserCfgBean(defaultCredentials.getUserName(),
						StringUtil.encode(defaultCredentials.getPassword())));
	}


	public Collection<ServerData> getAllEnabledServersWithDefaultCredentials(final ServerType serverType) {
		Collection<ServerData> servers = new ArrayList<ServerData>();
		for (ServerCfg server : cfgManager.getAllEnabledServers(projectId, serverType)) {
			if (server.isUseDefaultCredentials() && server.isEnabled()) {
				servers.add(getServerData(server));
			}
		}
		return servers;
	}

	@Nullable
	public ServerData getDefaultCrucibleServer() {
		ProjectConfiguration prjCfg = getProjectConfiguration();
		if (prjCfg != null) {
			CrucibleServerCfg crucibleServer = prjCfg.getDefaultCrucibleServer();
			if (crucibleServer != null) {
				return getServerData(crucibleServer);
			}
		}
		return null;
	}


	@Nullable
	public ServerData getDefaultJiraServer() {
		ProjectConfiguration prjCfg = getProjectConfiguration();
		if (prjCfg != null) {
			JiraServerCfg jiraServer = prjCfg.getDefaultJiraServer();
			if (jiraServer != null) {
				return getServerData(jiraServer);
			}
		}
		return null;
	}

	@Nullable
	public ServerData getDefaultFishEyeServer() {
		ProjectConfiguration prjCfg = getProjectConfiguration();
		if (prjCfg != null) {
			FishEyeServer fishEyeServer = prjCfg.getDefaultFishEyeServer();
			if (fishEyeServer != null) {
				return getServerData(fishEyeServer);
			}
		}
		return null;
	}

	public void addProjectConfigurationListener(final ConfigurationListener configurationListener) {
		cfgManager.addProjectConfigurationListener(projectId, configurationListener);
	}

	public void removeProjectConfigurationListener(final ConfigurationListener configurationListener) {
		cfgManager.removeProjectConfigurationListener(projectId, configurationListener);
	}

	// todo remove that method
	public void removeProject() {
		cfgManager.removeProject(projectId);
	}

	public void addProjectSpecificServer(final ServerCfg serverCfg) {
		cfgManager.addProjectSpecificServer(projectId, serverCfg);
	}

	public Collection<ServerCfg> getAllServers() {
		return cfgManager.getAllServers(projectId);
	}

	public ServerCfg removeServer(final ServerId serverId) {
		return cfgManager.removeProjectSpecificServer(projectId, serverId);
	}
}
