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

package com.atlassian.theplugin.configuration;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.atlassian.connector.intellij.configuration.UserCfgBean;
import org.jetbrains.annotations.NotNull;

@State(name = "atlassian-ide-plugin-workspace",
		storages = {@Storage(id = "atlassian-ide-plugin-workspace-id", file = "$WORKSPACE_FILE$")})
public class ProjectConfigurationBean implements PersistentStateComponent<ProjectConfigurationBean> {

	private BambooProjectConfiguration bambooConfiguration = new BambooProjectConfiguration();


	private String activeToolWindowTab = ""; //PluginToolWindow.ToolWindowPanels.JIRA.toString();
	private CrucibleWorkspaceConfiguration crucibleConfiguration = new CrucibleWorkspaceConfiguration();

	private UserCfgBean defaultCredentials = new UserCfgBean();


	public ProjectConfigurationBean() {

	}

	public BambooProjectConfiguration getBambooConfiguration() {
		return bambooConfiguration;
	}

	public void setBambooConfiguration(BambooProjectConfiguration bambooConfiguration) {
		this.bambooConfiguration = bambooConfiguration;
	}

	public CrucibleWorkspaceConfiguration getCrucibleConfiguration() {
		return crucibleConfiguration;
	}

	public void setCrucibleConfiguration(CrucibleWorkspaceConfiguration crucibleConfiguration) {
		this.crucibleConfiguration = crucibleConfiguration;
	}

	public void copyConfiguration(ProjectConfigurationBean state) {
		bambooConfiguration.copyConfiguration(state.getBambooConfiguration());
		crucibleConfiguration.copyConfiguration(state.getCrucibleConfiguration());
		this.activeToolWindowTab = state.getActiveToolWindowTab();
		defaultCredentials = new UserCfgBean(state.defaultCredentials.getUsername(),
				state.defaultCredentials.getEncodedPassword());
	}

	public String getActiveToolWindowTab() {
		return activeToolWindowTab;
	}

	public void setActiveToolWindowTab(String activeToolWindowTab) {
		this.activeToolWindowTab = activeToolWindowTab;
	}

	public ProjectConfigurationBean getState() {
		return this;
	}

	public void loadState(ProjectConfigurationBean state) {
		copyConfiguration(state);
//		projectConfigurationBean.copyConfiguration(state);
	}

	@NotNull
	public UserCfgBean getDefaultCredentials() {
		return defaultCredentials;
	}

	public void setDefaultCredentials(@NotNull final UserCfgBean defaultCredentials) {
		this.defaultCredentials = defaultCredentials;
	}
}
