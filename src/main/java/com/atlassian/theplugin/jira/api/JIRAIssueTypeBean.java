package com.atlassian.theplugin.jira.api;

import java.util.Map;
import java.net.URL;

public class JIRAIssueTypeBean extends AbstractJIRAConstantBean {
    private boolean subTask = false;
    public JIRAIssueTypeBean(Map map) {
        super(map);
        subTask = Boolean.valueOf((String) map.get("subTask"));
    }

	public JIRAIssueTypeBean(long id, String name, URL iconUrl) {
		super(id, name, iconUrl);
	}

	public String getQueryStringFragment() {
        return "type=" + getId();
    }

    public boolean isSubTask() {
        return subTask;
    }
}
