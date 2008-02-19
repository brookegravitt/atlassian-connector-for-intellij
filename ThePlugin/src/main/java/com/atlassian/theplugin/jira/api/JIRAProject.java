package com.atlassian.theplugin.jira.api;

public interface JIRAProject extends JIRAQueryFragment {
    long getId();

    String getName();

    String getKey();

    String getUrl();

    String getLead();

    String getDescription();
}
