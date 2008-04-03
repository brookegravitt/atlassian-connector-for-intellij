package com.atlassian.theplugin.jira.api;

import java.util.Map;

public class JIRAResolutionBean implements JIRAResolution {
    private String name;
    private long id;

    public JIRAResolutionBean(Map projMap) {
        name = (String) projMap.get("name");
        id = Long.valueOf((String) projMap.get("id"));
    }

	public JIRAResolutionBean(long id, String name) {
		this.name = name;
		this.id = id;
	}

	public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public String getQueryStringFragment() {
        return "resolution=" + id;
    }
}