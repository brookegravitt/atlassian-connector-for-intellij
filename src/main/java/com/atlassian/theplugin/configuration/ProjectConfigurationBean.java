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
