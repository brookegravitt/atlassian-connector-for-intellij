package com.atlassian.theplugin.jira.api;

public interface JIRASavedFilter extends JIRAQueryFragment {
    String getAuthor();

    String getProject();
}