package com.atlassian.theplugin.jira.api;

public interface JIRAProject extends JIRAQueryFragment {
    String getKey();

    String getUrl();

    String getLead();

    String getDescription();
}
