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
package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author Jacek Jaroczynski
 *         Used only for tests. todo Remove in refactoring process. Tests should use mock instead of own implementation
 */
public class ProjectCfgManagerAdapter implements ProjectCfgManager {
	@NotNull
	public ServerData getServerData(@NotNull final Server serverCfg) {
		return null;
	}

	public Collection<BambooServerCfg> getAllEnabledBambooServers() {
		return null;
	}

	public Collection<ServerCfg> getAllServers() {
		return null;
	}

	public Collection<ServerCfg> getAllServers(final ServerType serverType) {
		return null;
	}

	public Collection<ServerCfg> getAllEnabledServers() {
		return null;
	}

	public Collection<ServerCfg> getAllEnabledServers(final ServerType serverType) {
		return null;
	}

	public Collection<ServerCfg> getAllEnabledServersWithDefaultCredentials() {
		return null;
	}

	public Collection<ServerCfg> getAllEnabledServersWithDefaultCredentials(final ServerType serverType) {
		return null;
	}

	public Collection<BambooServerCfg> getAllBambooServers() {
		return null;
	}

	public Collection<JiraServerCfg> getAllJiraServers() {
		return null;
	}

	public Collection<CrucibleServerCfg> getAllCrucibleServers() {
		return null;
	}

	public Collection<JiraServerCfg> getAllEnabledJiraServers() {
		return null;
	}

	public Collection<CrucibleServerCfg> getAllEnabledCrucibleServers() {
		return null;
	}

	@Nullable
	public ServerData getDefaultJiraServer() {
		return null;
	}

	@Nullable
	public ServerData getDefaultCrucibleServer() {
		return null;
	}

	@Nullable
	public ServerData getDefaultFishEyeServer() {
		return null;
	}

	public String getDefaultCrucibleRepo() {
		return null;
	}

	public String getDefaultCrucibleProject() {
		return null;
	}

	public String getDefaultFishEyeRepo() {
		return null;
	}

	public String getFishEyeProjectPath() {
		return null;
	}

	public ServerCfg getServer(final ServerId serverId) {
		return null;
	}

	public void addProjectConfigurationListener(final ConfigurationListener configurationListener) {

	}

	public boolean removeProjectConfigurationListener(final ConfigurationListener configurationListener) {
		return false;
	}

	public ServerCfg getServer(final ServerData serverData) {
		return null;
	}

	public boolean isDefaultJiraServerValid() {
		return false;
	}
}
