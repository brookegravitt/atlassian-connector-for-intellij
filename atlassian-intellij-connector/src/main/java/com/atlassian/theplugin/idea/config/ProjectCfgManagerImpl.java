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
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.StringUtil;
import com.atlassian.theplugin.configuration.WorkspaceConfigurationBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArraySet;

public class ProjectCfgManagerImpl implements ProjectCfgManager {

	private final WorkspaceConfigurationBean workspaceConfiguration;
	private ProjectConfiguration projectConfiguration = new ProjectConfiguration();
	private Collection<ConfigurationListener> listeners = new ArrayList<ConfigurationListener>(100);


	/**
	 * Do NOT use the constructor. It is called by PICO.
	 *
	 * @param workspaceConfiguration used for default credentials purposes only (can be null for tests)
	 */
	public ProjectCfgManagerImpl(WorkspaceConfigurationBean workspaceConfiguration) {
		this.workspaceConfiguration = workspaceConfiguration;
	}

	/**
	 * This method has a package scope and should be used only for 'saving and modifying' purposes.
	 * This method can also be used in JUnit tests.
	 *
	 * @return project configuration inner object
	 */
	ProjectConfiguration getProjectConfiguration() {
		return projectConfiguration;
	}

	public void updateProjectConfiguration(final ProjectConfiguration configuration) {

		if (configuration == null) {
			throw new NullPointerException("Project configuration cannot be null");
		}

		ProjectConfiguration oldConfiguration = null;
		if (getProjectConfiguration() != null) {
			oldConfiguration = new ProjectConfiguration(getProjectConfiguration());
		}

		projectConfiguration = configuration;

		notifyListeners(projectConfiguration, oldConfiguration);
	}

	///////////////////////////////////////////////////////////////////
	///////////////////// DEFAULT CREDENTIALS /////////////////////////
	///////////////////////////////////////////////////////////////////

	public boolean isDefaultCredentialsAsked() {
		return workspaceConfiguration.isDefaultCredentialsAsked();
	}

	public void setDefaultCredentialsAsked(final boolean defaultCredentialsAsked) {
		workspaceConfiguration.setDefaultCredentialsAsked(defaultCredentialsAsked);
	}

	@NotNull
	public UserCfg getDefaultCredentials() {
		return new UserCfg(workspaceConfiguration.getDefaultCredentials().getUsername(),
				StringUtil.decode(workspaceConfiguration.getDefaultCredentials().getEncodedPassword()));
	}

	public void setDefaultCredentials(@NotNull final UserCfg defaultCredentials) {
		workspaceConfiguration.setDefaultCredentials(
				new UserCfgBean(defaultCredentials.getUserName(),
						StringUtil.encode(defaultCredentials.getPassword())));
	}

	///////////////////////////////////////////////////////////////////
	///////////////////// SERVER DATA STUFF ///////////////////////////
	///////////////////////////////////////////////////////////////////
	//todo remove all that functions when refactoring serverdata
	// all methods should return server data instead of servercfg
	// servercfg should not be used outside configuration

	@NotNull
	public ServerData getServerData(@NotNull Server serverCfg) {
		return getServerDataImpl(serverCfg);
	}


	@NotNull
	private ServerData getServerDataImpl(@NotNull Server serverCfg) {
		return ServerData.create(serverCfg, getDefaultCredentials());
	}

	//////////////////////////////////////////////////////////////////
	///////////////////// GET SERVER METHODS /////////////////////////
	//////////////////////////////////////////////////////////////////

	// SINGLE SERVER

	public ServerCfg getServer(final ServerId serverId) {
		for (ServerCfg server : getAllServers()) {
			if (serverId != null && server.getServerId().equals(serverId)) {
				return server;
			}
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
	public ServerData getEnabledServerr(final ServerId serverId) {
		final ServerCfg serverCfg = getServer(serverId);

		if (serverCfg != null && serverCfg.isEnabled()) {
			return getServerDataImpl(serverCfg);
		}
		return null;
	}

	public ServerData getServerr(final ServerId serverId) {
		for (ServerCfg server : getAllServers()) {
			if (serverId != null && server.getServerId().equals(serverId)) {
				return getServerData(server);
			}
		}
		return null;
	}


	public JiraServerCfg getJiraServer(final ServerId serverId) {
		for (JiraServerCfg server : getAllJiraServers()) {
			if (serverId != null && server.getServerId().equals(serverId)) {
				return server;
			}
		}
		return null;
	}


	public ServerData getJiraServerr(final ServerIdImpl serverId) {
		for (JiraServerCfg server : getAllJiraServers()) {
			if (serverId != null && server.getServerId().equals(serverId)) {
				return getServerData(server);
			}
		}
		return null;

	}


	public ServerData getCrucibleServerr(final ServerId serverId) {
		for (CrucibleServerCfg server : getAllCrucibleServers()) {
			if (serverId != null && server.getServerId().equals(serverId)) {
				return getServerData(server);
			}
		}
		return null;
	}


	public ServerData getEnabledCrucibleServerr(final ServerId serverId) {
		for (CrucibleServerCfg server : getAllEnabledCrucibleServers()) {
			if (serverId != null && server.getServerId().equals(serverId)) {
				return getServerData(server);
			}
		}
		return null;
	}

	// MULTIPLE SERVERS

	public Collection<ServerCfg> getAllServers() {
		return new ArrayList<ServerCfg>(projectConfiguration.getServers());
	}

	public Collection<ServerCfg> getAllServers(ServerType serverType) {

		Collection<ServerCfg> tmp = getAllServers();

		Collection<ServerCfg> ret = new ArrayList<ServerCfg>();

		for (ServerCfg serverCfg : tmp) {
			if (serverCfg.getServerType() == serverType) {
				ret.add(serverCfg);
			}
		}
		return ret;
	}

	public Collection<ServerData> getAllServerss(final ServerType serverType) {
		Collection<ServerCfg> tmp = getAllServers();
		Collection<ServerData> ret = new ArrayList<ServerData>();

		for (ServerCfg serverCfg : tmp) {
			if (serverCfg.getServerType() == serverType) {
				ret.add(getServerData(serverCfg));
			}
		}
		return ret;
	}

	public Collection<ServerCfg> getAllEnabledServers() {

		Collection<ServerCfg> ret = new ArrayList<ServerCfg>();
		for (ServerCfg serverCfg : getAllServers()) {
			if (serverCfg.isEnabled()) {
				ret.add(serverCfg);
			}
		}
		return ret;
	}


	public Collection<ServerCfg> getAllEnabledServers(ServerType serverType) {

		Collection<ServerCfg> tmp = getAllEnabledServers();
		Collection<ServerCfg> ret = new ArrayList<ServerCfg>();

		for (ServerCfg serverCfg : tmp) {
			if (serverCfg.getServerType() == serverType) {
				ret.add(serverCfg);
			}
		}
		return ret;
	}

	public Collection<ServerData> getAllEnabledServerss(final ServerType serverType) {
		Collection<ServerCfg> tmp = getAllEnabledServers();
		Collection<ServerData> ret = new ArrayList<ServerData>();

		for (ServerCfg serverCfg : tmp) {
			if (serverCfg.getServerType() == serverType) {
				ret.add(getServerData(serverCfg));
			}
		}
		return ret;
	}

	public Collection<ServerCfg> getAllEnabledServersWithDefaultCredentials() {
		Collection<ServerCfg> tmp = getAllEnabledServers();
		Collection<ServerCfg> ret = new ArrayList<ServerCfg>();
		for (ServerCfg serverCfg : tmp) {
			if (serverCfg.isUseDefaultCredentials() && serverCfg.isEnabled()) {
				ret.add(serverCfg);
			}
		}
		return ret;
	}

	public Collection<ServerCfg> getAllEnabledServersWithDefaultCredentials(final ServerType serverType) {
		Collection<ServerCfg> tmp = getAllEnabledServers();
		Collection<ServerCfg> ret = new ArrayList<ServerCfg>();

		for (ServerCfg serverCfg : tmp) {
			if (serverCfg.isUseDefaultCredentials() && serverCfg.isEnabled() && serverCfg.getServerType() == serverType) {
				ret.add(serverCfg);
			}
		}
		return ret;
	}

	public Collection<BambooServerCfg> getAllBambooServers() {
		Collection<ServerCfg> tmp = getAllServers();

		ArrayList<BambooServerCfg> ret = new ArrayList<BambooServerCfg>();

		for (ServerCfg serverCfg : tmp) {
			if (serverCfg.getServerType() == ServerType.BAMBOO_SERVER && serverCfg instanceof BambooServerCfg) {
				ret.add((BambooServerCfg) serverCfg);
			}
		}
		return ret;
	}

	public Collection<JiraServerCfg> getAllJiraServers() {
		Collection<ServerCfg> tmp = getAllServers();
		ArrayList<JiraServerCfg> ret = new ArrayList<JiraServerCfg>();

		for (ServerCfg serverCfg : tmp) {
			if (serverCfg.getServerType() == ServerType.JIRA_SERVER && serverCfg instanceof JiraServerCfg) {
				ret.add((JiraServerCfg) serverCfg);
			}
		}
		return ret;
	}


	public Collection<ServerData> getAllJiraServerss() {
		Collection<ServerCfg> tmp = getAllServers();
		ArrayList<ServerData> ret = new ArrayList<ServerData>();

		for (ServerCfg serverCfg : tmp) {
			if (serverCfg.getServerType() == ServerType.JIRA_SERVER && serverCfg instanceof JiraServerCfg) {
				ret.add(getServerData(serverCfg));
			}
		}
		return ret;
	}


	public Collection<CrucibleServerCfg> getAllCrucibleServers() {

		Collection<ServerCfg> tmp = getAllServers();

		ArrayList<CrucibleServerCfg> ret = new ArrayList<CrucibleServerCfg>();

		for (ServerCfg serverCfg : tmp) {
			if (serverCfg.getServerType() == ServerType.CRUCIBLE_SERVER && serverCfg instanceof CrucibleServerCfg) {
				ret.add((CrucibleServerCfg) serverCfg);
			}
		}
		return ret;
	}

	public Collection<ServerData> getAllCrucibleServerss() {
		Collection<ServerCfg> tmp = getAllServers();

		ArrayList<ServerData> ret = new ArrayList<ServerData>();

		for (ServerCfg serverCfg : tmp) {
			if (serverCfg.getServerType() == ServerType.CRUCIBLE_SERVER && serverCfg instanceof CrucibleServerCfg) {
				ret.add(getServerData(serverCfg));
			}
		}
		return ret;
	}


	public Collection<FishEyeServerCfg> getAllFishEyeServers() {

		Collection<ServerCfg> tmp = getAllServers();

		ArrayList<FishEyeServerCfg> ret = new ArrayList<FishEyeServerCfg>();

		for (ServerCfg serverCfg : tmp) {
			if (serverCfg.getServerType() == ServerType.FISHEYE_SERVER && serverCfg instanceof FishEyeServerCfg) {
				ret.add((FishEyeServerCfg) serverCfg);
			}
		}
		return ret;
	}

	public Collection<BambooServerCfg> getAllEnabledBambooServers() {

		Collection<ServerCfg> tmp = getAllEnabledServers();
		Collection<BambooServerCfg> ret = new ArrayList<BambooServerCfg>();

		for (ServerCfg serverCfg : tmp) {
			if (serverCfg.getServerType() == ServerType.BAMBOO_SERVER && serverCfg instanceof BambooServerCfg) {
				BambooServerCfg bambooServerCfg = (BambooServerCfg) serverCfg;
				ret.add(bambooServerCfg);
			}
		}
		return ret;
	}


	public Collection<ServerData> getAllEnabledBambooServerss() {
		Collection<ServerCfg> tmp = getAllEnabledServers();
		Collection<ServerData> ret = new ArrayList<ServerData>();

		for (ServerCfg serverCfg : tmp) {
			if (serverCfg.getServerType() == ServerType.BAMBOO_SERVER && serverCfg instanceof BambooServerCfg) {
				ret.add(getServerData(serverCfg));
			}
		}
		return ret;
	}

	public Collection<JiraServerCfg> getAllEnabledJiraServers() {

		Collection<ServerCfg> tmp = getAllEnabledServers();
		Collection<JiraServerCfg> ret = new ArrayList<JiraServerCfg>();

		for (ServerCfg serverCfg : tmp) {
			if (serverCfg.getServerType() == ServerType.JIRA_SERVER && serverCfg instanceof JiraServerCfg) {
				JiraServerCfg jiraServerCfg = (JiraServerCfg) serverCfg;
				ret.add(jiraServerCfg);
			}
		}
		return ret;
	}


	public Collection<ServerData> getAllEnabledJiraServerss() {
		Collection<ServerCfg> tmp = getAllEnabledServers();
		Collection<ServerData> ret = new ArrayList<ServerData>();

		for (ServerCfg serverCfg : tmp) {
			if (serverCfg.getServerType() == ServerType.JIRA_SERVER && serverCfg instanceof JiraServerCfg) {
				JiraServerCfg jiraServerCfg = (JiraServerCfg) serverCfg;
				ret.add(getServerData(jiraServerCfg));
			}
		}
		return ret;
	}

	public Collection<CrucibleServerCfg> getAllEnabledCrucibleServers() {

		Collection<ServerCfg> tmp = getAllEnabledServers();
		Collection<CrucibleServerCfg> ret = new ArrayList<CrucibleServerCfg>();

		for (ServerCfg serverCfg : tmp) {
			if (serverCfg.getServerType() == ServerType.CRUCIBLE_SERVER && serverCfg instanceof CrucibleServerCfg) {
				CrucibleServerCfg crucibleServerCfg = (CrucibleServerCfg) serverCfg;
				ret.add(crucibleServerCfg);
			}
		}
		return ret;

	}


	public Collection<ServerData> getAllEnabledCrucibleServerss() {
		Collection<ServerCfg> tmp = getAllEnabledServers();
		Collection<ServerData> ret = new ArrayList<ServerData>();

		for (ServerCfg serverCfg : tmp) {
			if (serverCfg.getServerType() == ServerType.CRUCIBLE_SERVER && serverCfg instanceof CrucibleServerCfg) {
				CrucibleServerCfg crucibleServerCfg = (CrucibleServerCfg) serverCfg;
				ret.add(getServerData(crucibleServerCfg));
			}
		}
		return ret;

	}

	///////////////////////////////////////////////////////////////
	///////////////////// DEFAULT SERVERS /////////////////////////
	///////////////////////////////////////////////////////////////

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


	public boolean isDefaultJiraServerValid() {
		return getProjectConfiguration().isDefaultJiraServerValid();
	}

	public String getDefaultCrucibleRepo() {
		return getProjectConfiguration().getDefaultCrucibleRepo();
	}

	public String getDefaultCrucibleProject() {
		return getProjectConfiguration().getDefaultCrucibleProject();
	}

	public String getDefaultFishEyeRepo() {
		return getProjectConfiguration().getDefaultFishEyeRepo();
	}

	public String getFishEyeProjectPath() {
		return getProjectConfiguration().getFishEyeProjectPath();
	}

	//////////////////////////////////////////////////////////////////
	///////////////////// CONFIG LISTENERS ///////////////////////////
	//////////////////////////////////////////////////////////////////

	public void addProjectConfigurationListener(final ConfigurationListener configurationListener) {

		if (configurationListener == null) {
			return;
		}

		if (listeners == null) {
			listeners = new CopyOnWriteArraySet<ConfigurationListener>(); //MiscUtil.buildHashSet();
		}
		listeners.add(configurationListener);
	}

	public boolean removeProjectConfigurationListener(final ConfigurationListener configurationListener) {

		if (configurationListener == null) {
			return false;
		}

		return listeners.remove(configurationListener);
	}

	//////////////////////////////////////////////////////////////////
	///////////////////// ADD REMOVE SERVERS /////////////////////////
	//////////////////////////////////////////////////////////////////

	// todo methods used only for tests. can we replace them with mocks?

	/**
	 * Package scope (only for JUnit tests)
	 *
	 * @param serverCfg
	 */
	public void addServer(final ServerCfg serverCfg) {

		if (serverCfg == null) {
			return;
		}

		if (projectConfiguration == null) {
			projectConfiguration = new ProjectConfiguration();
		}

		if (!projectConfiguration.getServers().contains(serverCfg)) {
			projectConfiguration.getServers().add(serverCfg);
		}
	}

	/**
	 * Package scope (only for JUnit tests)
	 *
	 * @param serverId id of the server to remove
	 * @return removed server or null if nothing removed
	 */
	ServerCfg removeServer(final ServerId serverId) {

		if (serverId == null) {
			return null;
		}

		Iterator<ServerCfg> it = projectConfiguration.getServers().iterator();
		while (it.hasNext()) {
			ServerCfg serverCfg = it.next();
			if (serverCfg.getServerId().equals(serverId)) {
				it.remove();
				return serverCfg;
			}
		}
		return null;
	}

	//////////////////////////////////////////////////////////////////
	///////////////////// LISTENERS ACTIONS //////////////////////////
	//////////////////////////////////////////////////////////////////

	private interface ProjectListenerAction {
		void run(final ConfigurationListener projectListener);
	}


	private void notifyListeners(ProjectConfiguration newConfiguration, ProjectConfiguration oldConfiguration) {

		ProjectListenerAction[] actions = {
				new UpdateConfigurationListenerAction(newConfiguration),
				new ConfigurationTypeChangedAction(newConfiguration, oldConfiguration),
				new ServerChangedAction(newConfiguration, oldConfiguration),
				new ServerAddedAction(newConfiguration, oldConfiguration),
				new ServerRemovedAction(newConfiguration, oldConfiguration),
				new ServerEnabledDisabledAction(newConfiguration, oldConfiguration)
		};

		for (ProjectListenerAction action : actions) {
			notifyListeners(action);
		}
	}

	private void notifyListeners(ProjectListenerAction listenerAction) {
		if (listeners != null) {
			for (ConfigurationListener projectListener : listeners) {
				listenerAction.run(projectListener);
			}
		}
	}

	private static class UpdateConfigurationListenerAction implements ProjectListenerAction {

		private final ProjectConfiguration projectConfiguration;

		public UpdateConfigurationListenerAction(final ProjectConfiguration projectConfiguration) {
			this.projectConfiguration = projectConfiguration;
		}

		public void run(final ConfigurationListener projectListener
		) {
			projectListener.configurationUpdated(projectConfiguration);
		}
	}

	private class ServerChangedAction implements ProjectListenerAction {
		protected final ProjectConfiguration newConfiguration;
		protected ProjectConfiguration oldConfiguration;

		public ServerChangedAction(ProjectConfiguration newConfiguration, ProjectConfiguration oldConfiguration) {
			this.newConfiguration = newConfiguration;
			this.oldConfiguration = oldConfiguration;
		}

		public void run(ConfigurationListener projectListener) {
			if (oldConfiguration == null || newConfiguration == null) {
				return;
			}

			for (ServerCfg oldServer : oldConfiguration.getServers()) {
				ServerCfg newServer = newConfiguration.getServerCfg(oldServer.getServerId());

				// server general update
				if (newServer != null && !oldServer.equals(newServer)) {
					projectListener.serverDataChanged(oldServer.getServerId());

					// server url or credentials updated
					if (checkCredentialsChanged(oldServer, newServer)
							|| checkUrlChanged(oldServer, newConfiguration.getServerCfg(oldServer.getServerId()))) {
						projectListener.serverConnectionDataChanged(oldServer.getServerId());
					}

					// server name updated
					if (!oldServer.getName().equals(newServer.getName())) {
						projectListener.serverNameChanged(oldServer.getServerId());
					}

				}
			}
		}

		protected boolean checkCredentialsChanged(final ServerCfg oldServer, final ServerCfg newServer) {
			if (newServer == null) {
				return false;
			}

			return !oldServer.getUserName().equals(newServer.getUserName())
					|| !oldServer.getPassword().equals(newServer.getPassword())
					|| oldServer.isUseDefaultCredentials() != newServer.isUseDefaultCredentials();

		}

		private boolean checkUrlChanged(final ServerCfg oldServer, final ServerCfg newServer) {
			if (newServer == null) {
				return false;
			}
			return !oldServer.getUrl().equals(newServer.getUrl());
		}
	}

	private class ServerAddedAction implements ProjectListenerAction {
		private final ProjectConfiguration newConfiguration;
		private final ProjectConfiguration oldConfiguration;

		public ServerAddedAction(ProjectConfiguration newConfiguration, ProjectConfiguration oldConfiguration) {
			this.newConfiguration = newConfiguration;
			this.oldConfiguration = oldConfiguration;
		}

		public void run(ConfigurationListener projectListener) {
			if (oldConfiguration == null || newConfiguration == null) {
				return;
			}

			for (ServerCfg newServer : newConfiguration.getServers()) {
				if (oldConfiguration.getServerCfg(newServer.getServerId()) == null) {
//					projectListener.serverAdded(newServer);
					projectListener.serverAdded(getServerData(newServer));
				}
			}
		}
	}

	private class ServerRemovedAction implements ProjectListenerAction {
		private final ProjectConfiguration newConfiguration;
		private final ProjectConfiguration oldConfiguration;

		public ServerRemovedAction(ProjectConfiguration newConfiguration, ProjectConfiguration oldConfiguration) {
			this.newConfiguration = newConfiguration;
			this.oldConfiguration = oldConfiguration;
		}

		public void run(ConfigurationListener projectListener) {
			if (oldConfiguration == null || newConfiguration == null) {
				return;
			}

			for (ServerCfg oldServer : oldConfiguration.getServers()) {
				if (newConfiguration.getServerCfg(oldServer.getServerId()) == null) {
//					projectListener.serverRemoved(oldServer);
					projectListener.serverRemoved(getServerData(oldServer));
				}
			}
		}
	}

	private class ServerEnabledDisabledAction implements ProjectListenerAction {

		private final ProjectConfiguration newConfiguration;
		private final ProjectConfiguration oldConfiguration;

		public ServerEnabledDisabledAction(ProjectConfiguration newConfiguration, ProjectConfiguration oldConfiguration) {
			this.newConfiguration = newConfiguration;
			this.oldConfiguration = oldConfiguration;
		}

		public void run(ConfigurationListener projectListener) {
			if (oldConfiguration == null || newConfiguration == null) {
				return;
			}

			for (ServerCfg oldServer : oldConfiguration.getServers()) {
				ServerCfg newServer = newConfiguration.getServerCfg(oldServer.getServerId());
				if (newServer != null) {
					if (!oldServer.isEnabled() && newServer.isEnabled()) {
						projectListener.serverEnabled(getServerData(oldServer));
					} else if (oldServer.isEnabled() && !newServer.isEnabled()) {
						projectListener.serverDisabled(oldServer.getServerId());
					}
				}
			}
		}

	}

	private static class ConfigurationTypeChangedAction implements ProjectListenerAction {
		private final ProjectConfiguration newConfiguration;
		private final ProjectConfiguration oldConfiguration;

		public ConfigurationTypeChangedAction(
				ProjectConfiguration newConfiguration, ProjectConfiguration oldConfiguration) {
			this.newConfiguration = newConfiguration;
			this.oldConfiguration = oldConfiguration;
		}

		public void run(ConfigurationListener projectListener) {
			if (oldConfiguration == null || newConfiguration == null) {
				return;
			}

			// Collections.constainsAll is used in both directions below instead of Collection.equlas
			// as equals for Collection compares only references
			// and we cannot be sure if used implementation overrides equals correctly
			// and e.g. equals for List requires the same order of elements

			// JIRA servers changed
			Collection<JiraServerCfg> newJiraServers = newConfiguration.getAllJIRAServers();
			Collection<JiraServerCfg> oldJiraServers = oldConfiguration.getAllJIRAServers();
			if (!newJiraServers.containsAll(oldJiraServers) || !oldJiraServers.containsAll(newJiraServers)) {
				projectListener.jiraServersChanged(newConfiguration);
			}

			// Bamboo servers changed
			Collection<BambooServerCfg> newBambooServers = newConfiguration.getAllBambooServers();
			Collection<BambooServerCfg> oldBambooServers = oldConfiguration.getAllBambooServers();
			if (!newBambooServers.containsAll(oldBambooServers) || !oldBambooServers.containsAll(newBambooServers)) {
				projectListener.bambooServersChanged(newConfiguration);
			}

			// Crucible servers changed
			Collection<CrucibleServerCfg> newCrucibleServers = newConfiguration.getAllCrucibleServers();
			Collection<CrucibleServerCfg> oldCrucibleServers = oldConfiguration.getAllCrucibleServers();
			if (!newCrucibleServers.containsAll(oldCrucibleServers) || !oldCrucibleServers.containsAll(newCrucibleServers)) {
				projectListener.crucibleServersChanged(newConfiguration);
			}

			// Fisheye servers changed
			Collection<FishEyeServerCfg> newFisheyeServers = newConfiguration.getAllFisheyeServers();
			Collection<FishEyeServerCfg> oldFisheyeServers = oldConfiguration.getAllFisheyeServers();
			if (!newFisheyeServers.containsAll(oldFisheyeServers) || !oldFisheyeServers.containsAll(newFisheyeServers)) {
				projectListener.fisheyeServersChanged(newConfiguration);
			}
		}
	}

}
