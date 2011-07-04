package com.atlassian.theplugin.idea.jira;

public enum JiraIssueGroupBy {
	NONE("None"),
	PROJECT("Project"),
	TYPE("Type"),
	STATUS("Status"),
	PRIORITY("Priority"),
	LAST_UPDATED("Last Updated");

	private String name;

	JiraIssueGroupBy(String name) {
	    this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

    public static JiraIssueGroupBy getDefaultGroupBy() {
        return TYPE;
    }
}
