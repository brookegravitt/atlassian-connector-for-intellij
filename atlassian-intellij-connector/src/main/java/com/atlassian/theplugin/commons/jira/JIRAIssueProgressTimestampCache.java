package com.atlassian.theplugin.commons.jira;

import com.atlassian.theplugin.commons.jira.api.JIRAIssue;
import com.atlassian.theplugin.commons.remoteapi.ServerData;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class JIRAIssueProgressTimestampCache {

	private Map<ServerData, Map<JIRAIssue, Date>> serverMap = new HashMap<ServerData, Map<JIRAIssue, Date>>();

	private JIRAIssueProgressTimestampCache() {
	}

	private static JIRAIssueProgressTimestampCache instance = new JIRAIssueProgressTimestampCache();

	public static JIRAIssueProgressTimestampCache getInstance() {
		return instance;
	}

	private Map<JIRAIssue, Date> getIssueMap(ServerData server) {
		Map<JIRAIssue, Date> issueMap = serverMap.get(server);
		if (issueMap == null) {
			issueMap = new HashMap<JIRAIssue, Date>();
			serverMap.put(server, issueMap);
		}

		return issueMap;
	}
	public Date getTimestamp(ServerData server, JIRAIssue issue) {
		return getIssueMap(server).get(issue);
	}

	public void setTimestamp(ServerData server, JIRAIssue issue) {
		getIssueMap(server).put(issue, new Date());
	}

	public void removeTimestamp(ServerData server, JIRAIssue issue) {
		Map<JIRAIssue, Date> issueMap = getIssueMap(server);
		if (issueMap.containsKey(issue)) {
			getIssueMap(server).remove(issue);
		}
	}
}
