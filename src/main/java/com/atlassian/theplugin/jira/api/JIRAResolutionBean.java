package com.atlassian.theplugin.jira.api;

import java.util.Map;

public class JIRAResolutionBean extends AbstractJIRAConstantBean {

    public JIRAResolutionBean(Map map) {
		super(map);
    }

	public JIRAResolutionBean(long id, String name) {
		super(id, name, null);
	}

    public String getQueryStringFragment() {
        return "resolution=" + id;
    }
}