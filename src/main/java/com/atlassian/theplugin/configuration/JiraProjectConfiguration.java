package com.atlassian.theplugin.configuration;

import java.util.ArrayList;
import java.util.List;

public class JiraProjectConfiguration {
	private ProjectToolWindowTableConfiguration tableConfiguration = new ProjectToolWindowTableConfiguration();
	private long selectedServerId = 0;
	private List<FilterMapBean> query = new ArrayList<FilterMapBean>();

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
		setQuery(jiraConfiguration.getQuery());
	}

	public long getSelectedServerId() {
		return selectedServerId;
	}

	public void setSelectedServerId(long selectedServerId) {
		this.selectedServerId = selectedServerId;
	}

	public List<FilterMapBean> getQuery() {
		return query;
	}

	public void setQuery(List<FilterMapBean> query) {
		this.query = query;
	}
}
