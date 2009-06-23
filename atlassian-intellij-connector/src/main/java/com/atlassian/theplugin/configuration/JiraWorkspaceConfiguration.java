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

import com.atlassian.theplugin.idea.jira.RemainingEstimateUpdateMode;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssueBean;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.annotations.Transient;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@State(name = "atlassian-ide-plugin-workspace-issues",
		storages = {@Storage(id = "atlassian-ide-plugin-workspace-issues-id", file = "$WORKSPACE_FILE$")})
public class JiraWorkspaceConfiguration implements PersistentStateComponent<JiraWorkspaceConfiguration> {
	private Map<String, JiraFilterConfigurationBean> filters = new HashMap<String, JiraFilterConfigurationBean>();
	private JiraViewConfigurationBean view = new JiraViewConfigurationBean();
	private LinkedList<IssueRecentlyOpenBean> recentlyOpenIssues = new LinkedList<IssueRecentlyOpenBean>();
	public static final int RECENLTY_OPEN_ISSUES_LIMIT = 10;
	private ActiveJiraIssueBean activeJiraIssue;
	private long selectedWorkflowAction;
	private boolean activeIssueProgressWorkflowAction;
	private boolean activeIssueLogWork;
	private boolean activeIssueCommitChanges;
	private int activeIssueAfterCommit;
	private boolean logWorkOnCommit;
	private RemainingEstimateUpdateMode remainingEstimateUpdateMode;

	public JiraWorkspaceConfiguration() {
	}

	public void copyConfiguration(JiraWorkspaceConfiguration jiraConfiguration) {
		this.filters = jiraConfiguration.filters;
		this.view = jiraConfiguration.view;
		this.recentlyOpenIssues = jiraConfiguration.recentlyOpenIssues;
		this.activeJiraIssue = jiraConfiguration.activeJiraIssue;
		this.selectedWorkflowAction = jiraConfiguration.selectedWorkflowAction;
		this.activeIssueProgressWorkflowAction = jiraConfiguration.activeIssueProgressWorkflowAction;
		this.activeIssueLogWork = jiraConfiguration.activeIssueLogWork;
		this.activeIssueCommitChanges = jiraConfiguration.activeIssueCommitChanges;
		this.activeIssueAfterCommit = jiraConfiguration.activeIssueAfterCommit;
		this.logWorkOnCommit = jiraConfiguration.logWorkOnCommit;
		this.remainingEstimateUpdateMode = jiraConfiguration.remainingEstimateUpdateMode;
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

	public void addRecentlyOpenIssue(final JIRAIssue issue) {
		if (recentlyOpenIssues == null) {
			recentlyOpenIssues = new LinkedList<IssueRecentlyOpenBean>();
		}

		if (issue != null) {
			String issueKey = issue.getKey();
			String serverId = issue.getServer().getServerId();

			// add element and make sure it is not duplicated and it is insterted at the top
			IssueRecentlyOpenBean r = new IssueRecentlyOpenBean(serverId, issueKey);

			if (recentlyOpenIssues != null) {
				recentlyOpenIssues.remove(r);
				recentlyOpenIssues.addFirst(r);

				while (recentlyOpenIssues.size() > RECENLTY_OPEN_ISSUES_LIMIT) {
					recentlyOpenIssues.removeLast();
				}
			}
		}
	}

	public long getSelectedWorkflowAction() {
		return selectedWorkflowAction;
	}

	public void setSelectedWorkflowAction(long selectedWorkflowAction) {
		this.selectedWorkflowAction = selectedWorkflowAction;
	}

	public boolean isActiveIssueProgressWorkflowAction() {
		return activeIssueProgressWorkflowAction;
	}

	public void setActiveIssueProgressWorkflowAction(boolean activeIssueProgressWorkflowAction) {
		this.activeIssueProgressWorkflowAction = activeIssueProgressWorkflowAction;
	}

	public boolean isActiveIssueLogWork() {
		return activeIssueLogWork;
	}

	public void setActiveIssueLogWork(boolean activeIssueLogWork) {
		this.activeIssueLogWork = activeIssueLogWork;
	}

	public boolean isActiveIssueCommitChanges() {
		return activeIssueCommitChanges;
	}

	public void setActiveIssueCommitChanges(boolean activeIssueCommitChanges) {
		this.activeIssueCommitChanges = activeIssueCommitChanges;
	}

	public int getActiveIssueAfterCommit() {
		return activeIssueAfterCommit;
	}

	public void setActiveIssueAfterCommit(int activeIssueAfterCommit) {
		this.activeIssueAfterCommit = activeIssueAfterCommit;
	}

	public boolean isLogWorkOnCommit() {
		return logWorkOnCommit;
	}

	public void setLogWorkOnCommit(boolean logWorkOnCommit) {
		this.logWorkOnCommit = logWorkOnCommit;
	}

	public RemainingEstimateUpdateMode getRemainingEstimateUpdateMode() {
		return remainingEstimateUpdateMode != null ? remainingEstimateUpdateMode : RemainingEstimateUpdateMode.AUTO;
	}

	public void setRemainingEstimateUpdateMode(RemainingEstimateUpdateMode remainingEstimateUpdateMode) {
		this.remainingEstimateUpdateMode = remainingEstimateUpdateMode;
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


	public ActiveJiraIssueBean getActiveJiraIssue() {
		return activeJiraIssue;
	}

	public void setActiveJiraIssue(final ActiveJiraIssueBean activeJiraIssue) {
		this.activeJiraIssue = activeJiraIssue;
	}
}
