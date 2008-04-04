package com.atlassian.theplugin.jira.api;

import java.util.Map;

public interface JIRAQueryFragment {
   
    String getQueryStringFragment();

	long getId();

	String getName();

	Map<String, String> getMap();
}
