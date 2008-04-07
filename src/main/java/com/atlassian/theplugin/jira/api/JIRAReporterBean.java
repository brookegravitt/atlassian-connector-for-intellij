package com.atlassian.theplugin.jira.api;

import java.util.Map;

public class JIRAReporterBean extends JIRAUserBean {
	public JIRAReporterBean() {
		super();
	}

	public JIRAReporterBean(long id, String name, String value) {
		super(id, name, value);
	}

	public JIRAReporterBean(Map map) {
		super(map);
	}

	public String getQueryStringFragment() {
		return "reporter=" + getValue();
	}
}
