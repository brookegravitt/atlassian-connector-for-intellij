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

import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssueImpl;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@State(name = "atlassian-ide-plugin-workspace-issues",
		storages = {@Storage(id = "atlassian-ide-plugin-workspace-issues-id", file = "$WORKSPACE_FILE$")})
public class JiraWorkspaceConfiguration implements PersistentStateComponent<JiraWorkspaceConfiguration> {
	private Map<String, JiraFilterConfigurationBean> filters = new HashMap<String, JiraFilterConfigurationBean>();
	private JiraViewConfigurationBean view = new JiraViewConfigurationBean();
	private LinkedList<IssueRecentlyOpenBean> recentlyOpenIssues = new LinkedList<IssueRecentlyOpenBean>();
	static final int RECENLTY_OPEN_ISSUES_LIMIT = 10;
	private ActiveJiraIssueImpl activeJiraIssue;
	private final Project project;

	public JiraWorkspaceConfiguration() {
		project = null;
	}

	public JiraWorkspaceConfiguration(@NotNull Project project) {
		this.project = project;
	}

	public void copyConfiguration(JiraWorkspaceConfiguration jiraConfiguration) {
		this.filters = jiraConfiguration.filters;
		this.view = jiraConfiguration.view;
		this.recentlyOpenIssues = jiraConfiguration.recentlyOpenIssues;
		this.activeJiraIssue = jiraConfiguration.activeJiraIssue;
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

	public LinkedList<IssueRecentlyOpenBean> getRecentlyOpenIssues() {
		return recentlyOpenIssues;
	}

	public void setRecentlyOpenIssues(final LinkedList<IssueRecentlyOpenBean> recentlyOpenIssues) {
		this.recentlyOpenIssues = recentlyOpenIssues;
	}

	public void addRecentlyOpenIssue(final JIRAIssue issue, ServerCfg jiraServer) {
		if (issue != null) {
			String issueKey = issue.getKey();
			String serverId = jiraServer.getServerId().toString();

			// add element and make sure it is not duplicated and it is insterted at the top
			IssueRecentlyOpenBean r = new IssueRecentlyOpenBean(serverId, issueKey);

			recentlyOpenIssues.remove(r);
			recentlyOpenIssues.addFirst(r);

			while (recentlyOpenIssues.size() > RECENLTY_OPEN_ISSUES_LIMIT) {
				recentlyOpenIssues.removeLast();
			}
		}
	}

	@Transient
	public JiraFilterConfigurationBean getJiraFilterConfiguaration(String id) {
		JiraFilterConfigurationBean filter = filters.get(id);
		if (filter == null) {
			filter = new JiraFilterConfigurationBean();
			filters.put(id, filter);
		}
		return filter;
	}

	@Transient
	public void setFilterConfigurationBean(String serverId, JiraFilterConfigurationBean filterConfiguration) {
		filters.put(serverId, filterConfiguration);
	}

	public JiraWorkspaceConfiguration getState() {
		return this;
	}

	public void loadState(final JiraWorkspaceConfiguration jiraProjectConfiguration) {
		copyConfiguration(jiraProjectConfiguration);
	}

	@Transient
	public ActiveJiraIssue getActiveJiraIssue() {
		return activeJiraIssue;
	}

	@Transient
	public void setActiveJiraIssue(final ActiveJiraIssue activeJiraIssue) {
		this.activeJiraIssue = (ActiveJiraIssueImpl) activeJiraIssue;
	}

	public void init() {
		if (activeJiraIssue != null) {
			//restart timer
			final ActiveJiraIssueImpl issue = new ActiveJiraIssueImpl(project,
					activeJiraIssue.getServer(), activeJiraIssue.getIssue(), new DateTime(),
					activeJiraIssue.getTimeSpent().getSeconds());
			setActiveJiraIssue(issue);
		}
	}
}
