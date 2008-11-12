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

import com.atlassian.theplugin.idea.jira.JIRAIssueGroupBy;


public class JiraViewConfigurationBean {
	private String viewServerId;
	private String viewFilterId;
	private JIRAIssueGroupBy groupBy;

	public JiraViewConfigurationBean() {
	}

	public String getViewServerId() {
		return viewServerId;
	}

	public void setViewServerId(final String viewServerId) {
		this.viewServerId = viewServerId;
	}

	public String getViewFilterId() {
		return viewFilterId;
	}

	public void setViewFilterId(final String viewFilterId) {
		this.viewFilterId = viewFilterId;
	}

	public JIRAIssueGroupBy getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(final JIRAIssueGroupBy groupBy) {
		this.groupBy = groupBy;
	}
}