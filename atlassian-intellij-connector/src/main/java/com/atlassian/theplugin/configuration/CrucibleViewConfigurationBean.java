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

import com.atlassian.theplugin.idea.crucible.CrucibleReviewGroupBy;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Jacek Jaroczynski
 */
public class CrucibleViewConfigurationBean {
	private CrucibleReviewGroupBy groupBy;
	private Collection<String> searchServers = new ArrayList<String>();

	public CrucibleReviewGroupBy getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(CrucibleReviewGroupBy groupBy) {
		this.groupBy = groupBy;
	}

	public Collection<String> getSearchServers() {
		return searchServers;
	}

	public void setSearchServers(Collection<String> servers) {
		searchServers = servers;
	}

	public void copyConfiguration(CrucibleViewConfigurationBean conf) {
		this.groupBy = conf.getGroupBy();
		this.searchServers = conf.searchServers;
	}
}
