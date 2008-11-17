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
import java.util.List;
import java.util.Map;

public class JiraFilterConfigurationBean {
	public static final String MANUAL_FILTER_LABEL = "manualFilter";

	private Map<String, List<JiraFilterEntryBean>> manualFilter = new HashMap<String, List<JiraFilterEntryBean>>();

	public JiraFilterConfigurationBean() {
	}

	public Map<String, List<JiraFilterEntryBean>> getManualFilter() {
		return manualFilter;
	}

	public void setManualFilter(Map<String, List<JiraFilterEntryBean>> manualFilter) {
		this.manualFilter = manualFilter;
	}

	@Transient
	public List<JiraFilterEntryBean> getManualFilterForName(String name) {
		return manualFilter.get(name);
	}

	@Transient
	public void setManualFilterForName(String name, List<JiraFilterEntryBean> filter) {
		manualFilter.put(name, filter);
	}
}