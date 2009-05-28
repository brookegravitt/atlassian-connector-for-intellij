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
package com.atlassian.theplugin.cfg;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ProjectId;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.intellij.openapi.project.Project;

public final class CfgUtil {

	private static final ProjectId DEFAULT_PROJECT = new ProjectId();

	private CfgUtil() {
		// this is utility class
	}

	public static ProjectId getProjectId(Project project) {
		if (project != null) {
			final String res1 = project.getPresentableUrl();
			if (res1 != null) {
				return new ProjectId(res1);
			}

			final String res2 = project.getName();
			if (res2 != null) {
				return new ProjectId(res2);
			}
		}

		return DEFAULT_PROJECT;
	}

	public static JiraServerCfg getJiraServerCfgbyServerId(final Project project,
			final ProjectCfgManagerImpl projectCfgManager, final String serverId) {
		for (JiraServerCfg server : projectCfgManager.getCfgManager().getAllEnabledJiraServers(CfgUtil.getProjectId(project))) {
			if (server.getServerId().toString().equals(serverId)) {
				return server;

			}
		}
		return null;
	}

	public static JiraServerCfg getJiraServerCfgByUrl(final Project project, final ProjectCfgManagerImpl projectCfgManager,
			final String serverUrl) {
		for (JiraServerCfg server : projectCfgManager.getCfgManager().getAllEnabledJiraServers(CfgUtil.getProjectId(project))) {
			if (server.getUrl().equals(serverUrl)) {
				return server;

			}
		}
		return null;
	}
}
