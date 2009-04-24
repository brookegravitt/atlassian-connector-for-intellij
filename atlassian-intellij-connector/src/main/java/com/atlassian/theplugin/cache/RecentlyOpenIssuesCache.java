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
package com.atlassian.theplugin.cache;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.configuration.IssueRecentlyOpenBean;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.config.ProjectCfgManager;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.JIRAIssueListModel;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelListener;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * User: pmaruszak
 */
public class RecentlyOpenIssuesCache {
	private final Map<IssueRecentlyOpenBean, JIRAIssue> items = new LinkedHashMap<IssueRecentlyOpenBean, JIRAIssue>();
	private final LocalModelListener localModelListener = new LocalModelListener();
	private final Project project;
	private final ProjectCfgManager cfgManager;
	private final JIRAIssueListModel issueModel;

	public RecentlyOpenIssuesCache(final Project project, final ProjectCfgManager cfgManager,
			final JIRAIssueListModel issueModel) {
		this.project = project;
		this.cfgManager = cfgManager;
		this.issueModel = issueModel;
		this.issueModel.addModelListener(localModelListener);
	}

	/**
	 * Reloads cache from the model and server in the background task
	 */
	public void init() {
		ProgressManager.getInstance().run(new Task.Backgroundable(project, "Retrieving recently viewed issues", false) {
			public void run(final ProgressIndicator progressindicator) {
				loadRecenltyOpenIssues();
			}
		});
	}

	public LinkedList<JIRAIssue> loadRecenltyOpenIssues() {
		final JiraWorkspaceConfiguration conf = IdeaHelper.getProjectComponent(project, JiraWorkspaceConfiguration.class);
		if (conf != null) {
			items.clear();
			final Collection<IssueRecentlyOpenBean> recentlyOpen = conf.getRecentlyOpenIssues();
			if (recentlyOpen != null) {
				for (IssueRecentlyOpenBean i : recentlyOpen) {
					try {
						JIRAIssue loadedIssue = loadJiraIssue(i);
						if (loadedIssue != null) {
							items.put(i, loadedIssue);
						}
					} catch (JIRAException e) {
						PluginUtil.getLogger().warn(e.getMessage());
					}
				}
			}
		}

		return new LinkedList<JIRAIssue>(items.values());
	}

	private JIRAIssue loadJiraIssue(final IssueRecentlyOpenBean recentlyOpen) throws JIRAException {

		ServerData jiraServer = cfgManager.getCfgManager().getServerData(
				CfgUtil.getJiraServerCfgbyServerId(project, cfgManager, recentlyOpen.getServerId()));

		JIRAIssue issue = null;

		if (jiraServer != null) {
			JIRAServerFacade facade = JIRAServerFacadeImpl.getInstance();
			issue = facade.getIssue(jiraServer, recentlyOpen.getIssueKey());
		}
		return issue;
	}

	public LinkedList<JIRAIssue> getLoadedRecenltyOpenIssues() {
		return new LinkedList<JIRAIssue>(items.values());
	}

	/**
	 * Add issue to cache and configuration. This is the only method user has to call to store recently viewed issue.
	 *
	 * @param issue Issue to add
	 */
	public void addIssue(final JIRAIssue issue) {
		items.put(new IssueRecentlyOpenBean(issue.getServer().getServerId().toString(), issue.getKey()), issue);

		final JiraWorkspaceConfiguration conf = IdeaHelper.getProjectComponent(project, JiraWorkspaceConfiguration.class);
		if (conf != null) {
			conf.addRecentlyOpenIssue(issue);
		}
	}

	public JIRAIssue getLoadedRecenltyOpenIssue(final String issueKey, final String serverId) {
		for (JIRAIssue issue : getLoadedRecenltyOpenIssues()) {
			if (issue.getKey().equals(issueKey) && issue.getServer().getServerId().toString().equals(serverId)) {
				return issue;
			}
		}
		return null;
	}

	private class LocalModelListener implements JIRAIssueListModelListener {

		public void modelChanged(final JIRAIssueListModel model) {
		}

		public void issuesLoaded(final JIRAIssueListModel model, final int loadedIssues) {
		}
	}

	public void close() {
		issueModel.removeModelListener(localModelListener);
	}
}
