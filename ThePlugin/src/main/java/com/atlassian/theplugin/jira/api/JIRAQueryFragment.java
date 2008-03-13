package com.atlassian.theplugin.jira.api;

public interface JIRAQueryFragment {
    // returns from this object a fragment of a query string that the IssueNavigator will understand    
    String getQueryStringFragment();    
}
