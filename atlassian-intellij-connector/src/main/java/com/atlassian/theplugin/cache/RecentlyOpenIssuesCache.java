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

import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.configuration.IssueRecentlyOpenBean;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.config.IntelliJProjectCfgManager;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * User: pmaruszak
 */
public class RecentlyOpenIssuesCache {
	// ordered map
	private final Map<IssueRecentlyOpenBean, JIRAIssue> items = new LinkedHashMap<IssueRecentlyOpenBean, JIRAIssue>();
	private final Project project;
	private final IntelliJProjectCfgManager projectCfgManager;

	public RecentlyOpenIssuesCache(final Project project, final IntelliJProjectCfgManager cfgManager) {
		this.project = project;
		this.projectCfgManager = cfgManager;
	}

	/**
	 * Reloads cache from the model and server in the background task
	 */
	public void init() {
		ProgressManager.getInstance().run(new Task.Backgroundable(project, "Retrieving recently viewed issues", false) {
			public void run(@NotNull final ProgressIndicator progressindicator) {
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
		final String recentServer = recentlyOpen.getServerId();
		if (recentServer == null) {
			return null;
		}
		final ServerData jiraServer = projectCfgManager.getEnabledServerData(new ServerId(recentServer));
		if (jiraServer != null) {
			return JIRAServerFacadeImpl.getInstance().getIssue(jiraServer, recentlyOpen.getIssueKey());
		}
		return null;
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
		final IssueRecentlyOpenBean recenltyOpenIssueBean =
				new IssueRecentlyOpenBean(issue.getServer().getServerId(), issue.getKey());

		items.remove(recenltyOpenIssueBean);
		items.put(recenltyOpenIssueBean, issue);

		// todo reverse order
		// todo limit number to 10

		final JiraWorkspaceConfiguration conf = IdeaHelper.getProjectComponent(project, JiraWorkspaceConfiguration.class);
		if (conf != null) {
			conf.addRecentlyOpenIssue(issue);
		}
	}

	public JIRAIssue getLoadedRecenltyOpenIssue(final String issueKey, final String serverId) {
		for (JIRAIssue issue : getLoadedRecenltyOpenIssues()) {
			if (issue.getKey().equals(issueKey) && issue.getServer().getServerId().equals(serverId)) {
				return issue;
			}
		}
		return null;
	}

	public void updateIssue(final JIRAIssue issue) {
		final IssueRecentlyOpenBean recentlyOpenIssueBean =
				new IssueRecentlyOpenBean(issue.getServer().getServerId(), issue.getKey());
		if (items.containsKey(recentlyOpenIssueBean)) {
			// old value is replaced
			items.put(recentlyOpenIssueBean, issue);
		}
	}
}
