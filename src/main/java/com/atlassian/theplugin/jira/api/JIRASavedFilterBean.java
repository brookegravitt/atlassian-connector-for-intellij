package com.atlassian.theplugin.jira.api;

import java.util.Map;
import java.util.HashMap;

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

	public Map<String, String> getMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("name", getName());
		map.put("id", Long.toString(id));
		map.put("author", getAuthor());
		map.put("project", getProject());
		map.put("filterTypeClass", this.getClass().getName());
		return map;
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