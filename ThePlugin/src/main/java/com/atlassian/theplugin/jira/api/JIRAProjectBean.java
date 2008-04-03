package com.atlassian.theplugin.jira.api;

import java.util.Map;

public class JIRAProjectBean implements JIRAProject {
    private String name;
    private String key;
    private String url;
    private long id;
    private String description;
    private String lead;

    public JIRAProjectBean(Map projMap) {
        name = (String) projMap.get("name");
        key = (String) projMap.get("key");
        description = (String) projMap.get("description");
        url = (String) projMap.get("url");
        lead = (String) projMap.get("lead");
        id = Long.valueOf((String) projMap.get("id"));
    }

	public JIRAProjectBean(long id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getName() {
        return name; 
    }

    public String getKey() {
        return key;
    }

    public String getUrl() {
        return url;
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getQueryStringFragment() {
        return "pid=" + id;
    }

    public String getLead() {
        return lead;
    }
}
