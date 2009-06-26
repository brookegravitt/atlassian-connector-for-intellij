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

import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.idea.jira.JiraIssueGroupBy;
import com.intellij.util.xmlb.annotations.Transient;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;


public class JiraViewConfigurationBean {
	private ServerIdImpl viewServerIdd;
	private String viewFilterId;
	private JiraIssueGroupBy groupBy;
	private boolean collapseSubtasksUnderParent;

	// selected projects per server in the Issue Crea+te Dialog
	private HashMap<ServerIdImpl, JiraProjectDefaultComponentsBean> serverDefaultss =
			new HashMap<ServerIdImpl, JiraProjectDefaultComponentsBean>();

	public JiraViewConfigurationBean() {
	}

	public ServerIdImpl getViewServerIdd() {
		return viewServerIdd;
	}

	public void setViewServerIdd(final ServerIdImpl viewServerIdd) {
		this.viewServerIdd = viewServerIdd;
	}

	public String getViewFilterId() {
		return viewFilterId;
	}

	public void setViewFilterId(final String viewFilterId) {
		this.viewFilterId = viewFilterId;
	}

	public JiraIssueGroupBy getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(final JiraIssueGroupBy groupBy) {
		this.groupBy = groupBy;
	}

	public boolean isCollapseSubtasksUnderParent() {
		return collapseSubtasksUnderParent;
	}

	public void setCollapseSubtasksUnderParent(boolean collapseSubtasksUnderParent) {
		this.collapseSubtasksUnderParent = collapseSubtasksUnderParent;
	}

	public HashMap<ServerIdImpl, JiraProjectDefaultComponentsBean> getServerDefaultss() {
		return serverDefaultss;
	}

	public void setServerDefaultss(final HashMap<ServerIdImpl, JiraProjectDefaultComponentsBean> serverDefaultss) {
		this.serverDefaultss = serverDefaultss;
	}

	@Transient
	public void addServerDefault(final ServerId jiraServer, final String projectKey, final Collection<Long> components) {
		LinkedHashSet<Long> comp = new LinkedHashSet<Long>(components);

		JiraProjectDefaultComponentsBean tmp = new JiraProjectDefaultComponentsBean(projectKey, comp);

		serverDefaultss.put((ServerIdImpl) jiraServer, tmp);
	}
}