package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAAssigneeBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAComponentBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAFixForVersionBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAIssueTypeBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAPriorityBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAProjectBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAQueryFragment;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAReporterBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAResolutionBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAStatusBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAVersionBean;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * User: pmaruszak
 */
public final class JiraCustomFilter {
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

	public String getName() {
		return name;
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

		if (!skipAnyValues || fragment.getId() != JIRAServerCache.ANY_ID) {
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

		return !(name != null ? !name.equals(that.name) : that.name != null)
				&& !(queryFragment != null ? !queryFragment.equals(that.queryFragment) : that.queryFragment != null);

	}

	@Override
	public int hashCode() {
		int result;
		result = (queryFragment != null ? queryFragment.hashCode() : 0);
		result = HASH_NUMBER * result + (name != null ? name.hashCode() : 0);
		return result;
	}
}
