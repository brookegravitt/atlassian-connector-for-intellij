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

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.connector.commons.jira.JiraUserNotFoundException;
import com.atlassian.connector.commons.jira.beans.JIRAUserBean;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.jira.IntelliJJiraServerFacade;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.JiraServerFacade;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.remoteapi.jira.JiraCaptchaRequiredException;
import com.atlassian.theplugin.configuration.IssueRecentlyOpenBean;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.util.PluginUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * User: pmaruszak
 */
public class RecentlyOpenIssuesCache {
	// ordered map
	private final LinkedHashMap<IssueRecentlyOpenBean, JiraIssueAdapter> items =
            new LinkedHashMap<IssueRecentlyOpenBean, JiraIssueAdapter>();
	private final ProjectCfgManager projectCfgManager;
	private JiraWorkspaceConfiguration jiraWorkspaceConf;

	public RecentlyOpenIssuesCache(final ProjectCfgManager cfgManager, final JiraWorkspaceConfiguration jiraConf) {
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
	public List<JiraIssueAdapter> loadRecenltyOpenIssues() {
		if (jiraWorkspaceConf != null) {
			items.clear();
			final List<IssueRecentlyOpenBean> recentlyOpen =
					new LinkedList<IssueRecentlyOpenBean>(jiraWorkspaceConf.getRecentlyOpenIssuess());
			// we put elements in the map in reverse order (most fresh element is at the end)
			// this is because map.put (used when adding new element) place alement at the end
			// I don't know the way to put element at the top of the ordered map.
			Collections.reverse(recentlyOpen);
			for (IssueRecentlyOpenBean i : recentlyOpen) {
				try {
					JiraIssueAdapter loadedIssue = loadJiraIssue(i);
					if (loadedIssue != null) {
					items.put(i, loadedIssue);
					}
                } catch (JIRAException e) {
                    if (e instanceof JiraCaptchaRequiredException) {
                        PluginUtil.getLogger().warn("Due to multiple failed login attempts,"
                                + " you have been temporarily banned from using the remote API."
                                + " To re-enable the remote API please log into your server via the web interface.\n"
                                + e.getMessage());
                    } else {
					    PluginUtil.getLogger().warn(e.getMessage());
                    }

				}
			}
		}
		return reverseList(new LinkedList<JiraIssueAdapter>(items.values()));
	}

	/**
	 * It is non blocking method and can be called in the UI thread
	 *
	 * @return list of recenlty viewed issues from the local cache
	 */
	public List<JiraIssueAdapter> getLoadedRecenltyOpenIssues() {
		return reverseList(new LinkedList<JiraIssueAdapter>(items.values()));
	}

	/**
	 * Returns recently viewed issue according to provided parameters
	 * Non blocking method. Can be called in the UI thread.
	 *
	 * @param issueKey issue key to look for
	 * @param serverId server to search
	 * @param issueUrl
     * @return recently viewed issue from the local cache or null in case issue was not found in the cache
	 */
	public JiraIssueAdapter getLoadedRecenltyOpenIssue(final String issueKey, final ServerId serverId, String issueUrl) {
		return items.get(new IssueRecentlyOpenBean(serverId, issueKey, issueUrl));
	}

	/**
	 * Add issue to cache and configuration.
	 * This is the only method user has to call to store recently viewed issue.
	 *
	 * @param issue Issue to add
	 */
	public void addIssue(final JiraIssueAdapter issue) {
		final IssueRecentlyOpenBean recenltyOpenIssueBean =
				new IssueRecentlyOpenBean(issue.getJiraServerData().getServerId(), issue.getKey(), issue.getIssueUrl());

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
	public void updateIssue(final JiraIssueAdapter issue) {
		final IssueRecentlyOpenBean recentlyOpenIssueBean =
				new IssueRecentlyOpenBean(issue.getJiraServerData().getServerId(), issue.getKey(), issue.getIssueUrl());
		if (items.containsKey(recentlyOpenIssueBean)) {
			// old value is replaced
			items.put(recentlyOpenIssueBean, issue);
		}
	}

	private List<JiraIssueAdapter> reverseList(final LinkedList<JiraIssueAdapter> jiraIssues) {
		Collections.reverse(jiraIssues);
		return jiraIssues;
	}

	private JiraIssueAdapter loadJiraIssue(final IssueRecentlyOpenBean recentlyOpen) throws JIRAException {
		final ServerId recentServer = recentlyOpen.getServerId();
		if (recentServer == null) {
			return null;
		}
		final JiraServerData jiraServer = projectCfgManager.getEnabledJiraServerr(recentServer);
		if (jiraServer != null) {
			return IntelliJJiraServerFacade.getInstance().getIssue(jiraServer, recentlyOpen.getIssueKey());
		}
		return null;
	}

    public static final class JIRAUserNameCache {

        private Map<JiraServerData, Map<String, JIRAUserBean>> serverMap =
                new HashMap<JiraServerData, Map<String, JIRAUserBean>>();
        private JiraServerFacade facade;

        private JIRAUserNameCache() {
            facade = IntelliJJiraServerFacade.getInstance();
        }

        private static JIRAUserNameCache instance = new JIRAUserNameCache();

        public static JIRAUserNameCache getInstance() {
            return instance;
        }

        public JIRAUserBean getUser(final JiraServerData jiraServerData, final String userId)
                throws JIRAException, JiraUserNotFoundException {
            Map<String, JIRAUserBean> userMap = serverMap.get(jiraServerData);
            if (userMap == null) {
                userMap = new HashMap<String, JIRAUserBean>();
                serverMap.put(jiraServerData, userMap);
            }
            JIRAUserBean user = userMap.get(userId);
            if (user == null) {
                user = facade.getUser(jiraServerData, userId);
                userMap.put(userId, user);
            }
            return user;
        }
    }
}
