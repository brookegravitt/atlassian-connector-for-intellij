package com.atlassian.theplugin.idea.jira;

public enum JIRAIssueGroupBy {
	NONE("None"),
	PROJECT("Project"),
	TYPE("Type"),
	STATUS("Status"),
	PRIORITY("Priority");

	private String name;

	private JIRAIssueGroupBy(String name) {
	    this.name = name;
	}

	public String toString() {
		return name;
	}
}
