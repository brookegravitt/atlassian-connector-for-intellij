package com.atlassian.theplugin.jira.api;

public class JIRAActionBean extends AbstractJIRAConstantBean implements JIRAAction {
    public JIRAActionBean(long id, String name) {
		super(id, name, null);
    }

    public String getQueryStringFragment() {
        return "action=" + id;
    }

	public String toString() {
		return name;
	}
}
