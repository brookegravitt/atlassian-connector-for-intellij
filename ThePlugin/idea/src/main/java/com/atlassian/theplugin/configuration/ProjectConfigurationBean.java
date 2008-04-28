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

public class ProjectConfigurationBean {

	private BambooProjectConfiguration bambooConfiguration = new BambooProjectConfiguration();
	private JiraProjectConfiguration jiraConfiguration = new JiraProjectConfiguration();
	private String activeToolWindowTab = ""; //PluginToolWindow.ToolWindowPanels.JIRA.toString();
	private CrucibleProjectConfiguration crucibleConfiguration = new CrucibleProjectConfiguration();

	public ProjectConfigurationBean() {

	}

	public BambooProjectConfiguration getBambooConfiguration() {
		return bambooConfiguration;
	}

	public void setBambooConfiguration(BambooProjectConfiguration bambooConfiguration) {
		this.bambooConfiguration = bambooConfiguration;
	}

	public JiraProjectConfiguration getJiraConfiguration() {
		return jiraConfiguration;
	}

	public void setJiraConfiguration(JiraProjectConfiguration jiraConfiguration) {
		this.jiraConfiguration = jiraConfiguration;
	}

	public CrucibleProjectConfiguration getCrucibleConfiguration() {
		return crucibleConfiguration;
	}

	public void setCrucibleConfiguration(CrucibleProjectConfiguration crucibleConfiguration) {
		this.crucibleConfiguration = crucibleConfiguration;
	}

	public void copyConfiguration(ProjectConfigurationBean state) {
		bambooConfiguration.copyConfiguration(state.getBambooConfiguration());
		jiraConfiguration.copyConfiguration(state.getJiraConfiguration());
		crucibleConfiguration.copyConfiguration(state.getCrucibleConfiguration());
		this.activeToolWindowTab = state.getActiveToolWindowTab();
	}

	public String getActiveToolWindowTab() {
		return activeToolWindowTab;
	}

	public void setActiveToolWindowTab(String activeToolWindowTab) {
		this.activeToolWindowTab = activeToolWindowTab;
	}
}
