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
package com.atlassian.theplugin.jira.cache;

import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.configuration.IssueRecentlyOpenBean;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.config.IntelliJProjectCfgManager;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.util.PluginUtil;

import java.util.*;

/**
 * User: pmaruszak
 */
public class RecentlyOpenIssuesCache {
	// ordered map
	private final LinkedHashMap<IssueRecentlyOpenBean, JIRAIssue> items = new LinkedHashMap<IssueRecentlyOpenBean, JIRAIssue>();
	private final IntelliJProjectCfgManager projectCfgManager;
	private JiraWorkspaceConfiguration jiraWorkspaceConf;

	public RecentlyOpenIssuesCache(final IntelliJProjectCfgManager cfgManager, final JiraWorkspaceConfiguration jiraConf) {
		this.projectCfgManager = cfgManager;
		this.jiraWorkspaceConf = jiraConf;
	}

	/**
	 * Loads recenlty viewed issues from the server into cache.
	 * BLOCKING METHOD. SHOULD BE CALLED IN THE BACKGROUND.
	 * This method should be called only if you want to init or refresh the cache.
	 *
	 * @return local (cached) list of recently viewed issues
	 */
	public List<JIRAIssue> loadRecenltyOpenIssues() {
		if (jiraWorkspaceConf != null) {
			items.clear();
			final List<IssueRecentlyOpenBean> recentlyOpen =
					new LinkedList<IssueRecentlyOpenBean>(jiraWorkspaceConf.getRecentlyOpenIssues());
			// we put elements in the map in reverse order (most fresh element is at the end)
			// this is because map.put (used when adding new element) place alement at the end
			// I don't know the way to put element at the top of the ordered map.
			Collections.reverse(recentlyOpen);
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
		return reverseList(new LinkedList<JIRAIssue>(items.values()));
	}

	/**
	 * It is non blocking method and can be called in the UI thread
	 *
	 * @return list of recenlty viewed issues from the local cache
	 */
	public List<JIRAIssue> getLoadedRecenltyOpenIssues() {
		return reverseList(new LinkedList<JIRAIssue>(items.values()));
	}

	/**
	 * Returns recently viewed issue according to provided parameters
	 * Non blocking method. Can be called in the UI thread.
	 *
	 * @param issueKey issue key to look for
	 * @param serverId server to search
	 * @return recently viewed issue from the local cache or null in case issue was not found in the cache
	 */
	public JIRAIssue getLoadedRecenltyOpenIssue(final String issueKey, final String serverId) {
		return items.get(new IssueRecentlyOpenBean(serverId, issueKey));
	}

	/**
	 * Add issue to cache and configuration.
	 * This is the only method user has to call to store recently viewed issue.
	 *
	 * @param issue Issue to add
	 */
	public void addIssue(final JIRAIssue issue) {
		final IssueRecentlyOpenBean recenltyOpenIssueBean =
				new IssueRecentlyOpenBean(issue.getServer().getServerId(), issue.getKey());

		items.remove(recenltyOpenIssueBean);
		items.put(recenltyOpenIssueBean, issue);

		// reduce map size (remove items from the beginning of the list - the eldest ones)
		while (items.size() > JiraWorkspaceConfiguration.RECENLTY_OPEN_ISSUES_LIMIT) {
			Iterator iter = items.values().iterator();
			if (iter.hasNext()) {
				iter.next();
				iter.remove();
			}
		}

		if (jiraWorkspaceConf != null) {
			jiraWorkspaceConf.addRecentlyOpenIssue(issue);
		}
	}

	/**
	 * Updates issue in the cache. Issue is updated only if it exists.
	 * Issue is not added if it does not exist in the cache.
	 * Issue position on the list is not changed when updating.
	 * This is not blocking method and can be called in the UI thread
	 *
	 * @param issue issue to update
	 */
	public void updateIssue(final JIRAIssue issue) {
		final IssueRecentlyOpenBean recentlyOpenIssueBean =
				new IssueRecentlyOpenBean(issue.getServer().getServerId(), issue.getKey());
		if (items.containsKey(recentlyOpenIssueBean)) {
			// old value is replaced
			items.put(recentlyOpenIssueBean, issue);
		}
	}

	private List<JIRAIssue> reverseList(final LinkedList<JIRAIssue> jiraIssues) {
		Collections.reverse(jiraIssues);
		return jiraIssues;
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
}
