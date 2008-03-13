package com.atlassian.theplugin.jira.api;

import java.util.Map;

public class JIRAStatusBean extends AbstractJIRAConstantBean {
    public JIRAStatusBean(Map map) {
        super(map);
    }

    public String getQueryStringFragment() {
        return "status=" + getId();
    }
}
