package com.atlassian.theplugin.jira.api;

public interface JIRASavedFilter extends JIRAQueryFragment {
    long getId();

    String getName();

    String getAuthor();

    String getProject();
}