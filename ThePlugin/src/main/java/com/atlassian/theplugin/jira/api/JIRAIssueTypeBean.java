package com.atlassian.theplugin.jira.api;

import java.util.Map;

public class JIRAIssueTypeBean extends AbstractJIRAConstantBean {
    private boolean subTask = false;
    public JIRAIssueTypeBean(Map map) {
        super(map);
        subTask = Boolean.valueOf((String) map.get("subTask"));
    }

    public String getQueryStringFragment() {
        return "type=" + getId();
    }

    public boolean isSubTask() {
        return subTask;
    }
}
