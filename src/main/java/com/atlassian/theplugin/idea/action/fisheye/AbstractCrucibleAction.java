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
package com.atlassian.theplugin.idea.action.fisheye;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import java.util.Collection;

public abstract class AbstractCrucibleAction extends AnAction {


	@Override
	public void update(final AnActionEvent event) {
		boolean isEnabled = false;
		final Project project = IdeaHelper.getCurrentProject(event);

		if (project != null) {
			Collection<CrucibleServerCfg> servers = IdeaHelper.getCfgManager()
					.getAllEnabledCrucibleServers(CfgUtil.getProjectId(project));
			for (CrucibleServerCfg crucibleServerCfg : servers) {
				if (crucibleServerCfg.isFisheyeInstance()) {
					isEnabled = true;
					break;
				}
			}
		}
		event.getPresentation().setVisible(isEnabled);
	}

	protected CrucibleServerCfg getCrucibleServerCfg(final AnActionEvent event) {
		Project project = IdeaHelper.getCurrentProject(event);
		if (project != null) {
			Collection<ServerCfg> servers = IdeaHelper.getCfgManager().getProjectSpecificServers(CfgUtil.getProjectId(project));
			for (ServerCfg server : servers) {
				if (server.getServerType().equals(ServerType.CRUCIBLE_SERVER) && server.isEnabled() && server.isComplete()) {
					return (CrucibleServerCfg) server;
				}
			}
		}
		return null;
	}
}
