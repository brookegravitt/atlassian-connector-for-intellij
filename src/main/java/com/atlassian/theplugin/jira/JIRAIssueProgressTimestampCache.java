package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.jira.api.JIRAIssue;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;

public class JIRAIssueProgressTimestampCache {

	private Map<JIRAServer, Map<JIRAIssue, Date>> serverMap = new HashMap<JIRAServer, Map<JIRAIssue, Date>>();

	private JIRAIssueProgressTimestampCache() {
	}

	private static JIRAIssueProgressTimestampCache instance = new JIRAIssueProgressTimestampCache();

	public static JIRAIssueProgressTimestampCache getInstance() {
		return instance;
	}

	private Map<JIRAIssue, Date> getIssueMap(JIRAServer server) {
		Map<JIRAIssue, Date> issueMap = serverMap.get(server);
		if (issueMap == null) {
			issueMap = new HashMap<JIRAIssue, Date>();
			serverMap.put(server, issueMap);
		}

		return issueMap;
	}
	public Date getTimestamp(JIRAServer server, JIRAIssue issue) {
		return getIssueMap(server).get(issue);
	}

	public void setTimestamp(JIRAServer server, JIRAIssue issue) {
		getIssueMap(server).put(issue, new Date());
	}

	public void removeTimestamp(JIRAServer server, JIRAIssue issue) {
		Map<JIRAIssue, Date> issueMap = getIssueMap(server);
		if (issueMap.containsKey(issue)) {
			getIssueMap(server).remove(issue);
		}
	}
}
