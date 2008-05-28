package com.atlassian.theplugin.jira.api;

import java.util.Map;
import java.util.HashMap;

public class JIRAActionBean implements JIRAAction {
    private String name;
    private long id;

    public JIRAActionBean(long id, String name) {
        this.id = id;
        this.name = name;
    }
    public String getQueryStringFragment() {
        return "action=" + id;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("name", getName());
        map.put("id", Long.toString(id));
        return map;
    }

	public String toString() {
		return name;
	}
}
