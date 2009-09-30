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

package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.commons.jira.api.JIRAAction;
import com.atlassian.theplugin.commons.jira.api.JIRAConstant;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;

import javax.management.timer.Timer;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JiraIssueCachedAdapter {
	private JiraIssueAdapter issue;
	private boolean useIconDescription;

	private static Map<JiraIssueAdapter, JiraIssueCachedAdapter> issueAdapterMap = new HashMap<JiraIssueAdapter, JiraIssueCachedAdapter>();

	private List<JIRAAction> issueActionCache;
	private long issueActionCacheTimestamp = 0;

	public JiraIssueCachedAdapter(JiraIssueAdapter issue, boolean useIconDescription) {
		this.issue = issue;
		this.useIconDescription = useIconDescription;
	}



    public JiraIssueAdapter getIssue() {
		return issue;
	}

	public boolean isUseIconDescription() {
		return useIconDescription;
	}

	public String getServerUrl() {
		return issue.getServerUrl();
	}

	public String getProjectUrl() {
		return issue.getProjectUrl();
	}

	public String getIssueUrl() {
		return issue.getIssueUrl();
	}

	public String getKey() {
		return issue.getKey();
	}

	public Long getId() {
		return issue.getId();
	}
	
	public String getProjectKey() {
		return issue.getProjectKey();
	}

	public String getStatus() {
		return issue.getStatus();
	}

	public JiraIcon getStatusInfo() {
		return new JiraIcon(issue.getStatus(), issue.getStatusTypeUrl());		
	}

	public long getStatusId() {
		return issue.getStatusId();
	}

	public String getPriority() {
		return issue.getPriority() != null ? issue.getPriority() : "";
	}
	
	public JiraIcon getPriorityInfo() {
		return new JiraIcon(issue.getPriority(), issue.getPriorityIconUrl());
	}

	public long getPriorityId() {
		return issue.getPriorityId();
	}

	public String getSummary() {
		return issue.getSummary();
	}

	public String getType() {
		return issue.getType();
	}

	public JiraIcon getTypeInfo() {
		return new JiraIcon(issue.getType(), issue.getTypeIconUrl());
	}

	public long getTypeId() {
		return issue.getTypeId();
	}

	public String getDescription() {
		return issue.getDescription();
	}

	public JIRAConstant getTypeConstant() {
		return issue.getTypeConstant();
	}

	public JIRAConstant getStatusConstant() {
		return issue.getStatusConstant();
	}

	public String getAssignee() {
		return issue.getAssignee();
	}

	public String getReporter() {
		return issue.getReporter();
	}

	public String getResolution() {
		return issue.getResolution();
	}

	public String getCreated() {
		return issue.getCreated();
	}

	public String getUpdated() {
		return issue.getUpdated();
	}

	public synchronized List<JIRAAction> getCachedActions() {
		if (issueActionCache == null) {
			return null;
		}
		Date now = new Date();
		// let's set cache validity interval to one minute
		if (now.getTime() - issueActionCacheTimestamp > Timer.ONE_MINUTE) {
			return null;
		}
		return issueActionCache;
	}

	public synchronized void setCachedActions(List<JIRAAction> actions) {
		issueActionCache = actions;
		issueActionCacheTimestamp = new Date().getTime();
	}

	public synchronized void clearCachedActions() {
		issueActionCache = null;
	}

	public static JiraIssueCachedAdapter get(JiraIssueAdapter issue) {
		JiraIssueCachedAdapter a = issueAdapterMap.get(issue);
		if (a == null) {
			a = new JiraIssueCachedAdapter(issue, true);
			issueAdapterMap.put(issue, a);
		}
		return a;
	}
	public static void clearCache() {
		issueAdapterMap.clear();
	}

	public static void clearCache(final JiraIssueAdapter issue) {
		issueAdapterMap.remove(issue);
	}
}
