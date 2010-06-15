package com.atlassian.theplugin.jira.model;

import com.atlassian.connector.commons.jira.beans.JIRAAssigneeBean;
import com.atlassian.connector.commons.jira.beans.JIRAComponentBean;
import com.atlassian.connector.commons.jira.beans.JIRAFixForVersionBean;
import com.atlassian.connector.commons.jira.beans.JIRAIssueTypeBean;
import com.atlassian.connector.commons.jira.beans.JIRAPriorityBean;
import com.atlassian.connector.commons.jira.beans.JIRAProjectBean;
import com.atlassian.connector.commons.jira.beans.JIRAQueryFragment;
import com.atlassian.connector.commons.jira.beans.JIRAReporterBean;
import com.atlassian.connector.commons.jira.beans.JIRAResolutionBean;
import com.atlassian.connector.commons.jira.beans.JIRAStatusBean;
import com.atlassian.connector.commons.jira.beans.JIRAVersionBean;
import com.atlassian.connector.commons.jira.cache.CacheConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * User: pmaruszak
 */
public final class JiraCustomFilter implements JIRAQueryFragment {
	private static final int HASH_NUMBER = 31;

    public enum QueryElement {
		PROJECT("Project"),
		ISSUE_TYPE("Issue Type"),
		FIX_FOR("Fix For"),
		COMPONENTS("Components"),
		AFFECTS_VERSIONS("Affects Versions"),
		REPORTER("Reporter"),
		ASSIGNEE("Assignee"),
		STATUS("Status"),
		RESOLUTIONS("Resolutions"),
		PRIORITIES("Priorities"),
		UNKNOWN("Unknown");

		private String name;

		QueryElement(final String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

	}

    public boolean isEmpty() {
        return queryFragment == null || queryFragment.size() <= 0;
    }
	private List<JIRAQueryFragment> queryFragment = new ArrayList<JIRAQueryFragment>();

	private String name;
    private String uid;

    public JiraCustomFilter() {
         this.uid = UUID.randomUUID().toString();
         this.name = "Custom Filter";
    }

	JiraCustomFilter(final String uid, final String name, List<JIRAQueryFragment> queryFragment) {
        this.uid = uid;
        this.name = name;
		this.queryFragment = queryFragment;
	}

    public String getQueryStringFragment() {
        return null;
    }

    public long getId() {
        return 0;
    }

    public String getName() {
		return name;
	}

    public HashMap<String, String> getMap() {
        return null;
    }

    public JIRAQueryFragment getClone() {
        return null;
    }

    public void setName(final String name) {
		this.name = name;
	}


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<JIRAQueryFragment> getQueryFragment() {
		return queryFragment;
	}

	public void setQueryFragment(final List<JIRAQueryFragment> queryFragment) {
		this.queryFragment = queryFragment;

	}

	public Map<QueryElement, ArrayList<String>> groupBy(final boolean skipAnyValues) {
		TreeMap<QueryElement, ArrayList<String>> map = new TreeMap<QueryElement, ArrayList<String>>();

		for (JIRAQueryFragment fragment : queryFragment) {
			QueryElement qe = QueryElement.UNKNOWN;
			if (fragment instanceof JIRAProjectBean) {
				qe = QueryElement.PROJECT;
			} else if (fragment instanceof JIRAIssueTypeBean) {
				qe = QueryElement.ISSUE_TYPE;
			} else if (fragment instanceof JIRAStatusBean) {
				qe = QueryElement.STATUS;
			} else if (fragment instanceof JIRAPriorityBean) {
				qe = QueryElement.PRIORITIES;
			} else if (fragment instanceof JIRAResolutionBean) {
				qe = QueryElement.RESOLUTIONS;
			} else if (fragment instanceof JIRAFixForVersionBean) {
				qe = QueryElement.FIX_FOR;
			} else if (fragment instanceof JIRAComponentBean) {
				qe = QueryElement.COMPONENTS;
			} else if (fragment instanceof JIRAVersionBean) {
				qe = QueryElement.AFFECTS_VERSIONS;
			} else if (fragment instanceof JIRAAssigneeBean) {
				qe = QueryElement.ASSIGNEE;
			} else if (fragment instanceof JIRAReporterBean) {
				qe = QueryElement.REPORTER;
			}
			addValueToMap(map, qe, fragment, skipAnyValues);
		}
		return map;

	}

	private void addValueToMap(final TreeMap<QueryElement, ArrayList<String>> map, final QueryElement key,
			final JIRAQueryFragment fragment, final boolean skipAnyValues) {

		if (!skipAnyValues || fragment.getId() != CacheConstants.ANY_ID) {
			if (!map.containsKey(key)) {
				map.put(key, new ArrayList<String>());
			}
			map.get(key).add(fragment.getName());
		}
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JiraCustomFilter that = (JiraCustomFilter) o;

        if (!name.equals(that.name)) {
            return false;
        }
//        if (queryFragment != null ? !queryFragment.equals(that.queryFragment) : that.queryFragment != null)
//            return false;
        if (!uid.equals(that.uid)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0; //queryFragment != null ? queryFragment.hashCode() : 0;
        result = 31 * result + name.hashCode();
        result = 31 * result + uid.hashCode();
        return result;
    }
}
