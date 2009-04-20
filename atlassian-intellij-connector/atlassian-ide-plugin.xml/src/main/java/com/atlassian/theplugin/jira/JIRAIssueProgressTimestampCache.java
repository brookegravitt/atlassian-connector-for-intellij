package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class JIRAIssueProgressTimestampCache {

	private Map<JiraServerCfg, Map<JIRAIssue, Date>> serverMap = new HashMap<JiraServerCfg, Map<JIRAIssue, Date>>();

	private JIRAIssueProgressTimestampCache() {
	}

	private static JIRAIssueProgressTimestampCache instance = new JIRAIssueProgressTimestampCache();

	public static JIRAIssueProgressTimestampCache getInstance() {
		return instance;
	}

	private Map<JIRAIssue, Date> getIssueMap(JiraServerCfg server) {
		Map<JIRAIssue, Date> issueMap = serverMap.get(server);
		if (issueMap == null) {
			issueMap = new HashMap<JIRAIssue, Date>();
			serverMap.put(server, issueMap);
		}

		return issueMap;
	}
	public Date getTimestamp(JiraServerCfg server, JIRAIssue issue) {
		return getIssueMap(server).get(issue);
	}

	public void setTimestamp(JiraServerCfg server, JIRAIssue issue) {
		getIssueMap(server).put(issue, new Date());
	}

	public void removeTimestamp(JiraServerCfg server, JIRAIssue issue) {
		Map<JIRAIssue, Date> issueMap = getIssueMap(server);
		if (issueMap.containsKey(issue)) {
			getIssueMap(server).remove(issue);
		}
	}
}
