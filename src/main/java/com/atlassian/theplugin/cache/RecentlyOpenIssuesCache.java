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
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
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
import com.intellij.util.containers.HashMap;

import java.util.Collection;
import java.util.Map;

/**
 * User: pmaruszak
 */
public class RecentlyOpenIssuesCache {
	Map<IssueRecentlyOpenBean, JIRAIssue> items = new HashMap<IssueRecentlyOpenBean, JIRAIssue>();
	final LocalModelListener localModelListener = new LocalModelListener();
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

	public void init() {
		final JiraWorkspaceConfiguration conf = IdeaHelper.getProjectComponent(project, JiraWorkspaceConfiguration.class);
		if (conf != null) {
			final Collection<IssueRecentlyOpenBean> recentlyOpen = conf.getRecentlyOpenIssues();
			invalidate();
			Task.Backgroundable task = new Task.Backgroundable(project, "Retrieving recently open issues", false) {

				public void run(final ProgressIndicator progressindicator) {
					if (recentlyOpen != null) {
						for (IssueRecentlyOpenBean i : recentlyOpen) {
							try {
								getJIRAIssue(i);
							} catch (JIRAException e) {
								PluginUtil.getLogger().warn(e.getMessage());
							}
						}
					}
				}
			};
			ProgressManager.getInstance().run(task);
		}

	}

	public void invalidate() {
		items.clear();
	}

	public JIRAIssue getJIRAIssue(final IssueRecentlyOpenBean recentlyOpen) throws JIRAException {
		if (items.containsKey(recentlyOpen)) {
			return items.get(recentlyOpen);
		} else {
			JiraServerCfg jiraServer = CfgUtil.getJiraServerCfgbyServerId(project, cfgManager, recentlyOpen.getServerId());
			if (jiraServer != null) {
				JIRAIssue issue = null;

				if (issueModel != null) {
					issue = getIssueFromModel(recentlyOpen, jiraServer);
				}

				if (issue == null) {
					JIRAServerFacade facade = JIRAServerFacadeImpl.getInstance();
					issue = facade.getIssue(jiraServer, recentlyOpen.getIssueKey());
					items.put(recentlyOpen, issue);
				}

				return issue;

			}
			return null;
		}

	}

	private JIRAIssue getIssueFromModel(final IssueRecentlyOpenBean recentlyOpen, final JiraServerCfg jiraServer) {
		for (JIRAIssue issue : issueModel.getIssues()) {
			if (issue != null && issue.getKey().equals(recentlyOpen.getIssueKey()) && issue.getServer().equals(jiraServer)) {
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
