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

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.ProjectId;
import com.atlassian.theplugin.commons.cfg.Server;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.ProjectConfigurationComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ProjectCfgManager {
	private final ProjectConfigurationComponent projectConfigurationComponent;
	private final CfgManager cfgManager;
	private final ProjectId projectId;

	public ProjectCfgManager(Project project, ProjectConfigurationComponent projectConfigurationComponent, CfgManager cfgManager) {
		this.projectId = CfgUtil.getProjectId(project);
		this.projectConfigurationComponent = projectConfigurationComponent;
		this.cfgManager = cfgManager;
	}


	@NotNull
	public ProjectConfigurationComponent getProjectConfigurationComponent() {
		return projectConfigurationComponent;
	}

	@NotNull
	public CfgManager getCfgManager() {
		return cfgManager;
	}

	public ServerData getServerData(Server serverCfg) {
		return cfgManager.getServerData(projectId, serverCfg);
	}

	public ServerCfg getServer(final ServerData serverData) {
		return cfgManager.getServer(projectId, serverData);
	}
}
