package com.atlassian.theplugin.configuration;

import com.intellij.util.xmlb.annotations.Transient;

import java.util.HashMap;
import java.util.Map;

public class JiraProjectConfiguration {
	private ProjectToolWindowTableConfiguration tableConfiguration = new ProjectToolWindowTableConfiguration();
	private long selectedServerId = 0;
	private Map<Long, JiraFiltersBean> query = new HashMap<Long, JiraFiltersBean>();

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

	public Map<Long, JiraFiltersBean> getQuery() {
		return query;
	}

	public void setQuery(Map<Long, JiraFiltersBean> query) {
		this.query = query;
	}

	@Transient
	public JiraFiltersBean getJiraFilters(long serverId) {
		Long id = Long.valueOf(serverId);
		if (query.containsKey(id)) {
			return query.get(id);
		} else {
			return null;
		}
	}

	@Transient
	public void setFiltersBean(long serverId, JiraFiltersBean filters) {
		query.put(Long.valueOf(serverId), filters);		
	}
}
