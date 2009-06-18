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

package com.atlassian.theplugin.idea.action;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.exception.ThePluginException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.PluginToolWindow;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.atlassian.theplugin.idea.config.ProjectConfigurationComponent;
import com.atlassian.theplugin.util.Util;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;

import java.util.Collection;

/**
 * Simple action to show the settings for the plugin.
 */
public class ShowProjectSettingsAction extends AnAction {
	@Override
	public void update(final AnActionEvent event) {
		event.getPresentation().setEnabled(IdeaHelper.getCurrentProject(event) != null);
	}

	@Override
	public void actionPerformed(AnActionEvent event) {
		Project project = IdeaHelper.getCurrentProject(event);
		if (project != null) {
			ProjectConfigurationComponent component = project.getComponent(ProjectConfigurationComponent.class);
			ServerData server = event.getData(Constants.SERVER_KEY);
			if (server != null) {
				component.setSelectedServer(IdeaHelper.getProjectCfgManager(event).getServer(server));
			} else {
				PluginToolWindow toolWindow = IdeaHelper.getProjectComponent(project, PluginToolWindow.class);
				if (toolWindow != null) {
					if (toolWindow.getSelectedContent() != null) {
						component.setSelectedServer(findBestServerToSelect(toolWindow.getSelectedContent(),
								IdeaHelper.getProjectCfgManager(event)));
					}
				}
			}
			final ShowSettingsUtil settingsUtil = ShowSettingsUtil.getInstance();
			if (settingsUtil != null) {
				settingsUtil.editConfigurable(project, component);
			}
		}
	}

	private ServerCfg findBestServerToSelect(final PluginToolWindow.ToolWindowPanels selectedContent,
			final ProjectCfgManagerImpl cfgManager) {

		final ServerType serverType;
		try {
			serverType = Util.toolWindowPanelsToServerType(selectedContent);
		} catch (ThePluginException e) {
			// unknown tool window tab
			return null;
		}

		Collection<ServerCfg> servers = cfgManager.getAllEnabledServers(serverType);

		if (!servers.isEmpty()) {
			return servers.iterator().next();
		}

		servers = cfgManager.getAllServers(serverType);

		if (!servers.isEmpty()) {
			return servers.iterator().next();
		}

		return null;
	}

}