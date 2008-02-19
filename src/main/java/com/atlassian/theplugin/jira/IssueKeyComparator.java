package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.jira.api.JIRAIssue;

import java.util.Comparator;
import java.io.Serializable;

public class IssueKeyComparator implements Comparator, Serializable {
    public int compare(Object o, Object o1) {
        JIRAIssue issue = (JIRAIssue) o;
        String key = issue.getKey();
        JIRAIssue issue1 = (JIRAIssue) o1;
        String key1 = issue1.getKey();

        // first, try to compare on projects
        if (!issue.getProjectKey().equals(issue1.getProjectKey())) {
            return issue.getProjectKey().compareTo(issue1.getProjectKey());
        }

        // otherwise, if the same project - sort on issue ID
        Integer count = new Integer(key.substring(key.indexOf("-") + 1));
        Integer count1 = new Integer(key1.substring(key1.indexOf("-") + 1));

        return count.compareTo(count1);
    }
}
