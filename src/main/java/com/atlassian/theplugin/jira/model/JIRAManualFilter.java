package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.jira.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * User: pmaruszak
 */
public final class JIRAManualFilter {
	private enum QueryElement {
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

	private List<JIRAQueryFragment> queryFragment;

	private String name;

	JIRAManualFilter(final String name, List<JIRAQueryFragment> queryFragment) {
		this.name = name;
		this.queryFragment = queryFragment;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public List<JIRAQueryFragment> getQueryFragment() {
		return queryFragment;
	}

	public void setQueryFragment(final List<JIRAQueryFragment> queryFragment) {
		this.queryFragment = queryFragment;

	}

	public String toHTML() {
		String html = "<html><table>";
		TreeMap<QueryElement, ArrayList<String>> map = groupBy();

		for (QueryElement element : map.keySet()) {
			html += "<tr>&nbsp;<td align='right'>" + element.getName() + ":</td>&nbsp;<td align='left'>";
			for (String value : map.get(element)) {
				html += value + ", ";
			}
			html = html.substring(0, html.length() - 2);
			html += "</td></tr>";
		}

		html += "</table></html>";
		return html;
	}

	private TreeMap<QueryElement, ArrayList<String>> groupBy() {
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
			addValueToMap(map, qe, fragment);
		}
		return map;

	}

	private void addValueToMap(final TreeMap<QueryElement, ArrayList<String>> map, final QueryElement key,
							   final JIRAQueryFragment fragment) {
		if (fragment.getId() != JIRAServerCache.ANY_ID) {
			if (!map.containsKey(key)) {
				map.put(key, new ArrayList<String>());
			}
			map.get(key).add(fragment.getName());
		}
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		JIRAManualFilter that = (JIRAManualFilter) o;

		if (name != null ? !name.equals(that.name) : that.name != null) {
			return false;
		}
		if (queryFragment != null ? !queryFragment.equals(that.queryFragment) : that.queryFragment != null) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		int result;
		result = (queryFragment != null ? queryFragment.hashCode() : 0);
		result = 31 * result + (name != null ? name.hashCode() : 0);
		return result;
	}
}
