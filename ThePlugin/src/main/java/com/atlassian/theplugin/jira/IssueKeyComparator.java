package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.idea.jira.JiraIssueAdapter;

import java.io.Serializable;
import java.util.Comparator;

public class IssueKeyComparator implements Comparator, Serializable {
	static final long serialVersionUID = 903490105978352608L;
	
	public int compare(Object o, Object o1) {
		String key = ((JiraIssueAdapter) o).getKey();
        String key1 = ((JiraIssueAdapter) o1).getKey();

        // first, try to compare on projects
        if (!((JiraIssueAdapter) o).getProjectKey().equals(((JiraIssueAdapter) o1).getProjectKey())) {
            return ((JiraIssueAdapter) o).getProjectKey().compareTo(((JiraIssueAdapter) o1).getProjectKey());
        }

        // otherwise, if the same project - sort on issue ID
        Integer count = new Integer(key.substring(key.indexOf("-") + 1));
        Integer count1 = new Integer(key1.substring(key1.indexOf("-") + 1));

        return count.compareTo(count1);
    }
}
