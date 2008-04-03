package com.atlassian.theplugin.jira.api;

import java.util.Map;

public class JIRAPriorityBean extends AbstractJIRAConstantBean {
    public JIRAPriorityBean(Map map) {
        super(map);
    }

    public String getQueryStringFragment() {
        return "priority=" + getId();
    }
}