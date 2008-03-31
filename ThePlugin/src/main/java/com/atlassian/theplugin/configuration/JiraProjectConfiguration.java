package com.atlassian.theplugin.configuration;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-03-19
 * Time: 09:54:10
 * To change this template use File | Settings | File Templates.
 */
public class JiraProjectConfiguration {
	private ProjectToolWindowTableConfiguration tableConfiguration = new ProjectToolWindowTableConfiguration();
	private long selectedServerId = 0;

	public JiraProjectConfiguration() {
	}

	public ProjectToolWindowTableConfiguration getTableConfiguration() {
		return tableConfiguration;
	}

	public void setTableConfiguration(ProjectToolWindowTableConfiguration tableConfiguration) {
		this.tableConfiguration = tableConfiguration;
	}

	public void copyConfiguration(JiraProjectConfiguration jiraConfiguration) {
		tableConfiguration.copyConfiguration(jiraConfiguration.getTableConfiguration());
		setSelectedServerId(jiraConfiguration.getSelectedServerId());
	}

	public long getSelectedServerId() {
		return selectedServerId;
	}

	public void setSelectedServerId(long selectedServerId) {
		this.selectedServerId = selectedServerId;
	}
}
