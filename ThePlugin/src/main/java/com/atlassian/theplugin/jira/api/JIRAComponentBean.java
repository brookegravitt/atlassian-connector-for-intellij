package com.atlassian.theplugin.jira.api;

import java.util.Map;

public class JIRAComponentBean extends AbstractJIRAConstantBean {
    public JIRAComponentBean(Map map) {
        super(map);
    }
	
	public JIRAComponentBean(long id, String name) {
		super(id, name, null);
	}

	// returns from this object a fragment of a query string that the IssueNavigator will understand
	public String getQueryStringFragment() {
        return "component=" + getId();
	}
}