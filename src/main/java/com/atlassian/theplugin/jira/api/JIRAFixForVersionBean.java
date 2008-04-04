package com.atlassian.theplugin.jira.api;

import java.util.Map;

public class JIRAFixForVersionBean extends JIRAVersionBean {

	public JIRAFixForVersionBean(Map map) {
		super(map);
	}

	public JIRAFixForVersionBean(long id, String name) {
		super(id, name);
	}

	public JIRAFixForVersionBean(JIRAVersionBean version) {
		super(version.getId(), version.getName());
	}

	public String getQueryStringFragment() {
		return "fixfor=" + getId();
	}
}