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
