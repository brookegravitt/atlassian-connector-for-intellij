package com.atlassian.theplugin.jira.api;

import java.util.Map;
import java.net.URL;

public class JIRAStatusBean extends AbstractJIRAConstantBean {
    public JIRAStatusBean(Map map) {
        super(map);
    }

	public JIRAStatusBean(long id, String name, URL iconUrl) {
		super(id, name, iconUrl);
	}

	public String getQueryStringFragment() {
        return "status=" + getId();
    }
}
