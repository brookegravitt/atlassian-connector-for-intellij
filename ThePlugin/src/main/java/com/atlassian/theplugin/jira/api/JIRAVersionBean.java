package com.atlassian.theplugin.jira.api;

import java.util.Map;

public class JIRAVersionBean extends AbstractJIRAConstantBean {
	private boolean isReleased = false;

	public JIRAVersionBean(Map map) {
		super(map);
		isReleased = Boolean.valueOf((String) map.get("released"));
	}

	public boolean isReleased() {
		return isReleased;
	}

	// returns from this object a fragment of a query string that the IssueNavigator will understand
	public String getQueryStringFragment() {
        return "version=" + getId();
	}
}
