package com.atlassian.theplugin.jira.api;

import java.util.Map;

public class JIRASavedFilterBean implements JIRASavedFilter {
    private String name;
    private String author;
    private String project;
    private long id;

    public JIRASavedFilterBean(Map projMap) {
        name = (String) projMap.get("name");
        author = (String) projMap.get("author");
        project = (String) projMap.get("project");
        id = Long.valueOf((String) projMap.get("id"));
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

	public String getAuthor() {
		return author;
	}

	public String getProject() {
		return project;
	}

	public String getQueryStringFragment() {
        return Long.toString(id);
    }
}