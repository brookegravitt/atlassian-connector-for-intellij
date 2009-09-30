package com.atlassian.theplugin.commons.jira;

import com.atlassian.theplugin.commons.jira.api.JIRAIssue;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class JIRAIssueProgressTimestampCache {

	private Map<JiraServerData, Map<JiraIssueAdapter, Date>> serverMap = new HashMap<JiraServerData, Map<JiraIssueAdapter, Date>>();

	private JIRAIssueProgressTimestampCache() {
	}

	private static JIRAIssueProgressTimestampCache instance = new JIRAIssueProgressTimestampCache();

	public static JIRAIssueProgressTimestampCache getInstance() {
		return instance;
	}

	private Map<JiraIssueAdapter, Date> getIssueMap(JiraServerData server) {
		Map<JiraIssueAdapter, Date> issueMap = serverMap.get(server);
		if (issueMap == null) {
			issueMap = new HashMap<JiraIssueAdapter, Date>();
			serverMap.put(server, issueMap);
		}

		return issueMap;
	}
	public Date getTimestamp(JiraServerData server, JiraIssueAdapter issue) {
		return getIssueMap(server).get(issue);
	}

	public void setTimestamp(JiraServerData jiraServerData, JiraIssueAdapter issue) {
		getIssueMap(jiraServerData).put(issue, new Date());
	}

	public void removeTimestamp(JiraServerData server, JIRAIssue issue) {
		Map<JiraIssueAdapter, Date> issueMap = getIssueMap(server);
		if (issueMap.containsKey(issue)) {
			getIssueMap(server).remove(issue);
		}
	}
}
