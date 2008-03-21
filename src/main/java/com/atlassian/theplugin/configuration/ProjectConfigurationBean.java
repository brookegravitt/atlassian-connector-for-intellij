package com.atlassian.theplugin.configuration;

import com.atlassian.theplugin.idea.PluginToolWindow;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-03-17
 * Time: 10:28:55
 * To change this template use File | Settings | File Templates.
 */
public class ProjectConfigurationBean {

	private BambooProjectConfiguration bambooConfiguration = new BambooProjectConfiguration();
	private JiraProjectConfiguration jiraConfiguration = new JiraProjectConfiguration();
	private String activeToolWindowTab = PluginToolWindow.ToolWindowPanels.JIRA.toString();

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

	public void copyConfiguration(ProjectConfigurationBean state) {
		bambooConfiguration.copyConfiguration(state.getBambooConfiguration());
		jiraConfiguration.copyConfiguration(state.getJiraConfiguration());
		this.activeToolWindowTab = state.getActiveToolWindowTab();
	}

	public String getActiveToolWindowTab() {
		return activeToolWindowTab;
	}

	public void setActiveToolWindowTab(String activeToolWindowTab) {
		this.activeToolWindowTab = activeToolWindowTab;
	}
}
