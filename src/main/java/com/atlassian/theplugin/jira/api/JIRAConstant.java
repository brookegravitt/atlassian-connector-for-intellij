package com.atlassian.theplugin.jira.api;

import java.net.URL;

public interface JIRAConstant extends JIRAQueryFragment {
    long getId();

    String getName();

    URL getIconUrl();
}
