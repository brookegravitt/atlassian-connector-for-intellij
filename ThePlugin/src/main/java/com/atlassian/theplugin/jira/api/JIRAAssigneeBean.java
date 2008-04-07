package com.atlassian.theplugin.jira.api;

import java.util.Map;

public class JIRAAssigneeBean extends JIRAUserBean {
	public JIRAAssigneeBean() {		
	}

	public JIRAAssigneeBean(long id, String name, String value) {
		super(id, name, value);
	}

	public JIRAAssigneeBean(Map map) {
		super(map);
	}

	public String getQueryStringFragment() {
		return "assignee=" + getValue();
	}
}