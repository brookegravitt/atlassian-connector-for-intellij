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
	private Map<String, JiraFilterConfigurationBean> filters = new HashMap<String, JiraFilterConfigurationBean>();
	private JiraViewConfigurationBean view = new JiraViewConfigurationBean();

	public JiraProjectConfiguration() {
	}

	public void copyConfiguration(JiraProjectConfiguration jiraConfiguration) {
		this.filters = jiraConfiguration.filters;
		this.view = jiraConfiguration.view;
	}

	public Map<String, JiraFilterConfigurationBean> getFilters() {
		return filters;
	}

	public void setFilters(final Map<String, JiraFilterConfigurationBean> filters) {
		this.filters = filters;
	}

	public JiraViewConfigurationBean getView() {
		return view;
	}

	public void setView(final JiraViewConfigurationBean view) {
		this.view = view;
	}

	@Transient
	public JiraFilterConfigurationBean getJiraFilterConfiguaration(String id) {
		JiraFilterConfigurationBean filter = filters.get(id);
		if (filter == null) {
			filters.put(id, new JiraFilterConfigurationBean());
			filter = filters.get(id);
		}
		return filter;
	}

	@Transient
	public void setFilterConfigurationBean(String serverId, JiraFilterConfigurationBean filterConfiguration) {
		filters.put(serverId, filterConfiguration);
	}

}
