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

package com.atlassian.theplugin.commons.cfg;

import com.atlassian.theplugin.commons.ConfigurationListener;
import com.atlassian.theplugin.commons.ServerType;

import java.util.Collection;


public interface CfgManager extends BambooCfgManager {

	ProjectConfiguration getProjectConfiguration(ProjectId projectId);

	Collection<ServerCfg> getAllServers(ProjectId projectId);
    Collection<ServerCfg> getProjectSpecificServers(ProjectId projectId);
    Collection<ServerCfg> getGlobalServers();
    Collection<ServerCfg> getAllEnabledServers(ProjectId projectId);
	Collection<ServerCfg> getAllEnabledServers(ProjectId projectId, ServerType serverType);
	void updateProjectConfiguration(ProjectId projectId, ProjectConfiguration projectConfiguration);
	void updateGlobalConfiguration(GlobalConfiguration globalConfiguration);
    void addProjectSpecificServer(ProjectId projectId, ServerCfg serverCfg);
    void addGlobalServer(ServerCfg serverCfg);
	ProjectConfiguration removeProject(ProjectId projectId);
	ServerCfg removeGlobalServer(ServerId serverId);
	ServerCfg removeProjectSpecificServer(ProjectId projectId, ServerId serverId);
	ServerCfg getServer(ProjectId projectId, ServerId serverId);


	void addListener(ProjectId projectId, ConfigurationListener configurationListener);
	void removeListener(ProjectId projectId, ConfigurationListener configurationListener);

	Collection<CrucibleServerCfg> getAllEnabledCrucibleServers(final ProjectId projectId);

	Collection<JiraServerCfg> getAllEnabledJiraServers(final ProjectId projectId);
}
