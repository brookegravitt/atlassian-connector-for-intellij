package com.atlassian.theplugin.jira.api;

import java.util.Map;

public class JIRAVersionBean extends AbstractJIRAConstantBean {
	protected boolean isReleased = false;

	public JIRAVersionBean(Map map) {
		super(map);
		isReleased = Boolean.valueOf((String) map.get("released"));
	}

	public JIRAVersionBean(long id, String name) {
		this.id = id;
		this.name = name;
	}

	public boolean isReleased() {
		return isReleased;
	}

	public String getQueryStringFragment() {
		return "version=" + getId();
	}
}
