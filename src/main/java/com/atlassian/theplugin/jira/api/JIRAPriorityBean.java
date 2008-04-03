package com.atlassian.theplugin.jira.api;

import java.util.Map;
import java.net.URL;

public class JIRAPriorityBean extends AbstractJIRAConstantBean {
    public JIRAPriorityBean(Map map) {
        super(map);
    }

	public JIRAPriorityBean(long id, String name, URL iconUrl) {
		super(id, name, iconUrl);
	}

	public String getQueryStringFragment() {
        return "priority=" + getId();
    }
}